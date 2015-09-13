/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.bagit.rules;

import java.util.List;

/**
 * Responsible for matching an Expression representing a path against a pattern.  This is quite possibly the most
 * heavy-weight string parsing library you'll ever encounter.  It is inspired by Ant-style pattern matching, and
 * attempts to follow the same rules as the Ant <a href="https://ant.apache.org/manual/dirtasks.html">
 * implementation</a>:
 * <p>
 * <pre>
 * These patterns look very much like the patterns used in DOS and UNIX:
 *
 * '*' matches zero or more characters, '?' matches one character.
 *
 * In general, patterns are considered relative paths, relative to a task dependent base directory (the dir attribute in
 * the case of <fileset>). Only files found below that base directory are considered. So while a pattern like
 * ../foo.java is possible, it will not match anything when applied since the base directory's parent is never scanned
 * for files.
 *
 * Examples:
 *
 * .java  matches  .java, x.java and FooBar.java, but not FooBar.xml (does not end with .java).
 *
 * ?.java  matches  x.java, A.java, but not .java or xyz.java (both don't have one character before .java).
 *
 * Combinations of *'s and ?'s are allowed.
 *
 * Matching is done per-directory. This means that first the first directory in the pattern is matched against the first
 * directory in the path to match. Then the second directory is matched, and so on. For example, when we have the
 * pattern /?abc&#x2f;&#x2a;&#x2f;&#x2a;.java and the path /xabc/foobar/test.java, the first ?abc is matched with
 * xabc, then * is matched with foobar, and finally *.java is matched with test.java. They all match, so the path
 * matches the pattern.
 *
 * To make things a bit more flexible, we add one extra feature, which makes it possible to match multiple directory
 * levels. This can be used to match a complete directory tree, or a file anywhere in the directory tree. To do this,
 * &#x2a;&#x2a; must be used as the name of a directory. When &#x2a;&#x2a; is used as the name of a directory in the
 * pattern, it matches zero or more directories. For example: /test/&#x2a;&#x2a; matches all files/directories under
 * &#x2f;test/, such as /test/x.java, or /test/foo/bar/xyz.html, but not /xyz.xml.
 *
 * There is one "shorthand": if a pattern ends with / or \, then &#x2a;&#x2a; is appended. For example,
 * mypackage/test/ is interpreted as if it were mypackage/test/&#x2a;&#x2a;.
 * </pre>
 * </p>
 * <p>
 * Even though a "path" and a "pattern" are both instances of an {@link Expression}, their semantics differ.  A
 * "path" only contains literal and path separator tokens.  A "pattern" may contain literals, path separators, and
 * matching tokens like '*' and '?'.  Path segments are the tokens between consecutive path separators, addressable
 * by their zero-indexed {@link org.dataconservancy.bagit.rules.Expression#depth() depth}.  For example, the Expression
 * '/foo/bar/baz.txt' has three path segments, 'foo' (depth = 0), 'bar' (depth = 1), and 'baz.txt' (depth = 2).  The
 * depth of the Expression is 2.
 * </p>
 * <p>
 * Note that methods on this class are package-private, and are not meant to be exposed publicly.
 * </p>
 */
public class ExpressionMatcher {

    /**
     * Convenience reference to a {@code BoundToken} that matches zero or more characters (i.e. '*').
     * See {@link #zero_plus} for the {@code char} analog.
     */
    private static final BoundToken ZERO_OR_MORE = new BoundToken(Token.ZERO_OR_MORE_CHARACTERS,
            Token.ZERO_OR_MORE_CHARACTERS.getTokenString());

    /**
     * Convenience reference to a {@code BoundToken} that matches exactly one character (i.e. '?').
     * See {@link #exactly_one} for the {@code char} analog.
     */
    private static final BoundToken EXACTLY_ONE = new BoundToken(Token.EXACTLY_ONE_CHARACTER,
            Token.EXACTLY_ONE_CHARACTER.getTokenString());

    /**
     * The {@code char} analog of {@link #EXACTLY_ONE}
     */
    private final char exactly_one;

    /**
     * The {@code char} analog of {@link #ZERO_OR_MORE}
     */
    private final char zero_plus;

    /**
     * Constructs a new instance of a matcher.
     * TODO: probably could be private and methods be made static.
     */
    ExpressionMatcher() {
        if (EXACTLY_ONE.isSingleChar()) {
            exactly_one = EXACTLY_ONE.asChar();
        } else {
            throw new RuntimeException("Implementation doesn't handle multi-character token: " +
                    Token.EXACTLY_ONE_CHARACTER);
        }

        if (ZERO_OR_MORE.isSingleChar()) {
            zero_plus = ZERO_OR_MORE.asChar();
        } else {
            throw new RuntimeException("Implementation doesn't handle multi-character token: " +
                    Token.ZERO_OR_MORE_CHARACTERS);

        }
    }

    /**
     * Match the supplied path against the pattern.  Matching is applied 'per-directory' as described
     * {@link org.dataconservancy.bagit.rules.ExpressionMatcher above}.  This is the main entry point into the pattern
     * matching logic.
     *
     * @param pattern the pattern meant to match a path
     * @param path the path to match against the pattern
     * @return true if the pattern matches
     */
    boolean match(Expression pattern, Expression path) {

        // the path should just be made up of path separators and literals
        if (!isPath(path.getTokens())) {
            return false;  // probably should be an IAE
        }

        if (pattern.depth() > path.depth()) {
            // if the pattern depth is greater than the path we're supposed to be matching,
            // then we can't match, so short-circuit
            return false;  // probably should be an IAE
        }

        if (pattern.depth() == path.depth()) {
            boolean match = true;
            // we have alignment, simply match each path segment from the pattern against the path.
            for (int i = 0; i <= pattern.depth(); i++) {
                match &= match(pattern.getPathSegment(i), path.getPathSegment(i));
            }

            return match;
        }

        int pathOff = 0;
        int expOff = 0;
        int nextLiteral = nextLiteral(pattern, expOff);

        return matchPathSegment(pattern, path, expOff, pathOff, nextLiteral);
    }

    /**
     * Attempt to match all of the path segments in {@code path} against {@code pattern}, starting from
     * {@code pathDepth} and {@code patternDepth}.  The {@code nextLiteral} parameter contains the depth of the next
     * path segment in {@code pattern} containing a literal (or -1 if there isn't any).
     *
     * @param pattern the expression containing a matching tokens (i.e. pattern semantics)
     * @param path the expression containing only literals or path separators (i.e. path semantics)
     * @param patternDepth the depth to begin matching the pattern
     * @param pathDepth the depth to begin matching the path
     * @param nextLiteral the depth of the next pattern segment that contains a literal, or -1 if it doesn't exist
     * @return true if all of the segments (starting from pathDepth) in the path can be matched in the pattern (starting
     *         from patternDepth)
     */
    private boolean matchPathSegment(Expression pattern, Expression path, int patternDepth, int pathDepth,
                                     int nextLiteral) {

        // if we're out of literals...
        if (nextLiteral == -1) {
            // See if there are remaining segments to match, and match them.
            boolean match = true;
            for (int i = pathDepth; i <= path.depth(); i++) {
                match &= match(pattern.getPathSegment(patternDepth), path.getPathSegment(i));
            }

            return match;
        }

        // match the pattern segment containing literals against every path segment until we get a match
        int rightAnchor = nextMatch(path, pathDepth, pattern.getPathSegment(nextLiteral));

        // if we don't match ...
        if (rightAnchor == -1) {
            return false;
        }

        // make sure that every path segment from the left anchor to the right anchor matches the current path expression
        boolean match = true;
        for (int i = pathDepth; i < rightAnchor; i++) {
            match &= match(pattern.getPathSegment(patternDepth), path.getPathSegment(i));
        }

        // if they match up to the anchor, keep going
        if (match) {
            pathDepth = rightAnchor;
            patternDepth++;
            nextLiteral = nextLiteral(pattern, nextLiteral + 1);
            return matchPathSegment(pattern, path, patternDepth, pathDepth, nextLiteral);
        }

        return false;
    }

    /**
     * Search the supplied pattern starting at {@code depth} for path segments that contain literals.  Useful for
     * finding the depth of path segment 'Foo??.java' in the pattern expression '&#x2a;&#x2a;/Foo??.java'.
     *
     * @param pattern an expression with pattern semantics
     * @param depth the depth to begin searching from
     * @return the index of the next path segment (i.e. depth) that contains literals, or -1 if not found
     */
    int nextLiteral(Expression pattern, int depth) {
        if (depth > pattern.depth()) {
            return -1;
        }

        for (int i = depth; i <= pattern.depth(); i++) {
            if (containsLiterals(pattern.getPathSegment(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Attempts to match every path segment starting from {@code path.getPathSegment(depth)} against the
     * {@code pattern}.  The {@code path} is an {@code Expression} with path semantics (i.e. only containing literals
     * and path separators).  Each path segment (starting from {@code depth}) is matched against {@code pattern}.
     *
     * @param path an Expression with path semantics
     * @param depth the depth of the expression to begin matching from
     * @param pattern the pattern each path segment of {@code path} is matched against.
     * @return the index of the first path segment (i.e. depth) that matched {@code pattern}, or -1 if no match
     */
    int nextMatch(Expression path, int depth, List<BoundToken> pattern) {
        for (int i = depth; i <= path.depth(); i++) {
            if (match(pattern, path.getPathSegment(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Expected input are two Lists of BoundTokens.  Each List is expected to be a path segment; that is, a List
     * will contain all BoundTokens between two consecutive path separators, not including the separators.  Therefore
     * the path segment will not ever contain a path separator ('/'), nor should it contain a directory match
     * token ('**').
     * <p>
     * Essentially this method is evaluating a <em>pattern</em> that may contain literals, '*', and '?' against a
     * string of literals.
     * </p>
     *
     * @param patternPathSegment the pattern
     * @param pathPathSegment    the string (i.e. path) to match the pattern against
     * @return true if the pattern matches the path
     */
    boolean match(List<BoundToken> patternPathSegment, List<BoundToken> pathPathSegment) {

        // first, handle the short-circuit cases:
        //  patternPathSegment only contains '*' ; doesn't matter what pathPathSegment has, all tokens match
        //  patternPathSegment contains '?' and pathPathSegment only has a single token, the single token matches
        //  patternPathSegment is all literals ; see if the pathPathSegment equals

        if (isZeroOrMore(patternPathSegment)) {
            return true;
        }

        if (pathPathSegment.size() == 1 && isExactlyOne(patternPathSegment)) {
            return true;
        }

        if (allLiterals(patternPathSegment)) {
            return tokenEquals(patternPathSegment, pathPathSegment);
        }

        // Otherwise, we have a multiple-token pattern that contains a mixture of literals
        // and at least one of '*' or '?'

        CharSequence pattern = toCharSeq(patternPathSegment);
        CharSequence path = toCharSeq(pathPathSegment);

        int fPatternIndex = 0;
        int fPathIndex = 0;
        int tokenIndex = findNextToken(pattern, fPatternIndex);
        int literalIndex = findNextLiteral(pattern, fPatternIndex);

        int leftAnchor = 0;

        return match(pattern, path, fPathIndex, tokenIndex, literalIndex, leftAnchor);
    }

    /**
     * A recursive method for matching a {@code path} against a {@code pattern}.  The method terminates when there are
     * no more literals or tokens to be matched, or as soon as it determines a match isn't possible and returns early.
     * <p>
     * Developers, when reading this implementation, keep in mind that anchors are always indexes into the {@code path},
     * while {@code tokenIndex} and {@code literalIndex} are always indexes into {@code pattern}. The first major
     * decision made is whether the method is attempting to match a token (e.g. '?' in "Foo??.java") or match a literal
     * (e.g. "Foo", ".java" in "Foo??.java").
     * </p>
     * <p>
     * When matching a token, the first decision to make is whether you are going to match forward from the current
     * token, or work backward from the end of the pattern.  When matching a literal, the objective is to determine the
     * anchors of the literal in the path and attempt to match it against the pattern.
     * </p>
     *
     * @param pattern the pattern to match against
     * @param path the path to match
     * @param fPathIndex the index into the {@code path} that has matched
     * @param tokenIndex the index into {@code pattern} of the next token to be matched
     * @param literalIndex the index into th {@code pattern} of the next literal to be matched
     * @param leftAnchor not used TODO remove
     * @return true if {@code path} matches {@code pattern}
     */
    private boolean match(CharSequence pattern, CharSequence path, int fPathIndex, int tokenIndex, int literalIndex, int leftAnchor) {
        // Index description:
        // - fPathIndex, left and right anchors are always indexes in the path
        // - token and literal are always indexes in the pattern.


        int rightAnchor = Integer.MIN_VALUE;

        if (tokenIndex == Integer.MAX_VALUE && literalIndex == Integer.MAX_VALUE) {
            // we've matched everything?
            return true;
        }

        if (tokenIndex < literalIndex) {

            // We are matching a token (because tokenIndex < literalIndex)
            //
            // If we are matching the last token in the pattern, we work backward in the path.
            // If we are matching a token, and there are still more tokens left, we work forward in the path.
            //
            // - Find the left and right anchors in the path.
            //   - Find right anchor
            //     - Find the next literal in the pattern (using the literalIndex, and the [end of string|next token index])
            //     - Match that literal in the path (from offset fPathIndex)
            //     - Set the right anchor at the start of the literal.
            // - Find left anchor
            //   - Equal to the forward path index (fPathIndex)
            //
            // - If the token is a '*', we match.
            // - If the token is a '?', and rightAnchor - leftAnchor == 1, we match.
            //
            // - If we match:
            //   - set the fPathIndex to the rightAnchor (because fPathIndex keeps track of what we've matched in the path)
            //   - set the next token index (or Integer.MIN_VALUE if the pattern is exhausted, or out of tokens)
            //   - leave literalIndex alone, because we didn't match a literal this go-around, we matched a token.

            // Find the right anchor.
            int nextTokenIndex = findNextToken(pattern, tokenIndex + 1);

            leftAnchor = fPathIndex;

            CharSequence literal = null;

            if (nextTokenIndex != Integer.MAX_VALUE && nextTokenIndex != Integer.MAX_VALUE) {
                // we are not at the last token, work forward
                // in the case of consecutive tokens (e.g. "??"), the literalIndex will be greater than the nextTokenIndex
                int literalLen = Math.max(nextTokenIndex - literalIndex, 1);
                literal = (literalLen <= 0) ? "" : pattern.subSequence(literalIndex, literalIndex + literalLen);

                if ((rightAnchor = matchNextLiteral(path, fPathIndex, literal)) == Integer.MIN_VALUE) {
                    // we didn't match the literal in the path, so we won't match
                    return false;
                }
            } else {
                // we are at the last token, work backward from the end of the pattern by matching the literal at
                // the end of the pattern, then checking the remaining characters in the path with the pattern token

                // the special case is if the token we are matching is the last character of the pattern, in which
                // case there won't be a literal to match.  in this case, the right anchor will be set to
                // the end of the path.

                if (tokenIndex == pattern.length() - 1) {
                    rightAnchor = path.length();
                } else {
                    literal = pattern.subSequence(tokenIndex + 1, pattern.length());

                    // if we don't match the literal in the path, then we don't match
                    if ((rightAnchor = matchNextLiteral(path, fPathIndex, literal)) == Integer.MIN_VALUE) {
                        return false;
                    }
                }
            }

            // - If the token is a '*', we match.
            // - If the token is a '?', and rightAnchor - leftAnchor == 1, we match.

            // if the next token is inside of the right anchor, we have multiple tokens (e.g. '??') in a row.
            if (pattern.charAt(tokenIndex) == exactly_one) {
                if (nextTokenIndex < rightAnchor) {
                    if (pattern.subSequence(tokenIndex, literalIndex).chars().allMatch(c -> ((char) c) == '?')) {
                        fPathIndex = ++leftAnchor;
                        tokenIndex = findNextToken(pattern, tokenIndex + 1);
                        return match(pattern, path, fPathIndex, tokenIndex, literalIndex, leftAnchor);
                    } else {
                        return false;
                    }
                }

                if (rightAnchor - leftAnchor == 1) {
                    fPathIndex = rightAnchor;
                    tokenIndex = findNextToken(pattern, tokenIndex + 1);
                    return match(pattern, path, fPathIndex, tokenIndex, literalIndex, leftAnchor);
                } else {
                    return false;
                }
            }

            if (pattern.charAt(tokenIndex) == zero_plus) { //||
                //pattern.charAt(tokenIndex) == exactly_one && (rightAnchor - leftAnchor == 1) // ) {
                //      || ((nextTokenIndex < rightAnchor) && pattern.subSequence(tokenIndex, literalIndex).chars().allMatch(c -> ((char) c) == '?'))) {

                // - If we match:
                //   - set the fPathIndex to the rightAnchor (because fPathIndex keeps track of what we've matched in the path)
                //   - set the next token index (or Integer.MAX_VALUE if the pattern is exhausted, or out of tokens)
                //   - leave literalIndex alone, because we didn't match a literal this go-around, we matched a token.

                fPathIndex = rightAnchor;
                tokenIndex = findNextToken(pattern, tokenIndex + 1);

                return match(pattern, path, fPathIndex, tokenIndex, literalIndex, leftAnchor);

            } else {
                return false;
            }

        } else if (literalIndex < tokenIndex) {

            // We are matching a literal (because literalIndex < tokenIndex)
            CharSequence literalToMatch;

            if (literalIndex == Integer.MIN_VALUE) {
                // we're out of literals, so we just have to match that last token
                rightAnchor = path.length();
                if (pattern.charAt(tokenIndex) == zero_plus ||
                        pattern.charAt(tokenIndex) == exactly_one && rightAnchor - leftAnchor == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                leftAnchor = fPathIndex;

                // if we can't find the right anchor, then we can't match.
                if ((rightAnchor = findRightAnchor(pattern, path, fPathIndex, literalIndex)) == Integer.MAX_VALUE) {
                    return false;
                }

                literalToMatch = pattern.subSequence(literalIndex, Math.min(tokenIndex, pattern.length()));
            }

            // does the literal in the pattern match the literal between the anchors?
            if (path.subSequence(leftAnchor, rightAnchor).equals(literalToMatch)) {

                // - If we match:
                //   - set the fPathIndex to the rightAnchor (because fPathIndex keeps track of what we've matched in the path)
                //   - leave the next token index alone, because we didn't match a token this go around, we matched a literal
                //   - set the literal index to the beginning of the next literal

                fPathIndex = rightAnchor;
                literalIndex = findNextLiteral(pattern, literalIndex + literalToMatch.length());

                return match(pattern, path, fPathIndex, tokenIndex, literalIndex, leftAnchor);
            }
        }
        return false;
    }

    /**
     * Attempt to find the next occurrence of a token in {@code pattern}, starting from {@code offset}.
     * <p>
     * Remember that even though paths and patterns are both instances of {@link Expression}, the semantics of
     * a 'path' are that it contains only literals and path separators, and differs from a 'pattern' which can contain
     * matching tokens like '*' and '?'.
     * </p>
     *
     * @param pattern the pattern to search through
     * @param offset the offset into pattern to start searching from
     * @return the offset in the pattern with the next occurrence of a token, or {@code Integer.MAX_VALUE} if not
     *         found.
     * @see #findNextLiteral(CharSequence, int)
     */
    int findNextToken(CharSequence pattern, int offset) {
        if (offset < 0 || offset >= pattern.length() || pattern.length() == 0) {
            return Integer.MAX_VALUE;
        }

        for (int i = offset; i < pattern.length(); i++) {
            if (pattern.charAt(i) == exactly_one || pattern.charAt(i) == zero_plus) {
                return i;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Attempt to find the next occurrence of a literal in {@code pattern}, starting from {@code offset}.
     * <p>
     * Remember that even though paths and patterns are both instances of {@link Expression}, the semantics of
     * a 'path' are that it contains only literals and path separators, and differs from a 'pattern' which can contain
     * matching tokens like '*' and '?'.
     * </p>
     *
     * @param pattern the pattern to search through
     * @param offset the offset into pattern to start searching from
     * @return the offset in the pattern with the next occurrence of a literal, or {@code Integer.MAX_VALUE} if not
     *         found.
     * @see #findNextToken(CharSequence, int)
     */
    int findNextLiteral(CharSequence pattern, int offset) {
        if (offset < 0 || offset >= pattern.length() || pattern.length() == 0) {
            return Integer.MAX_VALUE;
        }

        for (int i = offset; i < pattern.length(); i++) {
            if (pattern.charAt(i) != exactly_one && pattern.charAt(i) != zero_plus) {
                return i;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Attempts to match the literal in the path, from the supplied offset, and returns the offset where the
     * literal occurs.
     *
     * @param path the path being searched for a literal string
     * @param offset the offset in path to start searching from
     * @param literal the literal string to find
     * @return the offset of {@code literal} in {@code path}, or {@code Integer.MIN_VALUE} if not found
     */
    int matchNextLiteral(CharSequence path, int offset, CharSequence literal) {
        if (offset < 0 || offset >= path.length() || path.length() == 0) {
            return Integer.MIN_VALUE;
        }

        CharSequence sub = path.subSequence(offset, path.length());

        int litIdx = 0;
        int subIdx = 0;
        while (litIdx < literal.length() && subIdx < sub.length()) {
            if (literal.charAt(litIdx) == sub.charAt(subIdx)) {
                // increment literal index if there's a match
                litIdx++;
            } else {
                // reset litIdx to 0
                litIdx = 0;
            }
            ;

            subIdx++; // always increment the substring index
        }

        // we matched the literal if the literal index is the same as its CharSequence
        if (literal.length() - litIdx == 0) {
            // then the offset into the path of the beginning of the literal is
            // offset + ( subIdx - literal.length() )
            return offset + (subIdx - literal.length());
        }

        return Integer.MIN_VALUE;  // literal wasn't found.
    }

    /**
     * Returns true if every token in the path segment is a {@link Token#ZERO_OR_MORE_CHARACTERS}.
     *
     * @param pathSegment the path segment containing arbitrary tokens
     * @return true if every token in the path segment is a {@code ZERO_OR_MORE_CHARACTERS} token.
     */
    private boolean isZeroOrMore(List<BoundToken> pathSegment) {
        return pathSegment.size() == 1 && pathSegment.get(0).token == Token.ZERO_OR_MORE_CHARACTERS;
    }

    /**
     * Returns true if the path segment contains a single token, and the token is a {@link Token#EXACTLY_ONE_CHARACTER}.
     *
     * @param pathSegment the path segment containing arbitrary tokens
     * @return true if the single token in the path segment is a {@code EXACTLY_ONE_CHARACTER} token.
     */
    private boolean isExactlyOne(List<BoundToken> pathSegment) {
        return pathSegment.size() == 1 && pathSegment.get(0).token == Token.EXACTLY_ONE_CHARACTER;
    }

    /**
     * Answers a {@code CharSequence} that contains the value of each token, in the same order, as supplied by
     * {@code tokens}.
     *
     * @param tokens a List of arbitrary tokens
     * @return the sequence of token values
     */
    private CharSequence toCharSeq(List<BoundToken> tokens) {
        return tokens.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
    }

    /**
     * Returns true if each list is equal in size, and contains
     * {@link org.dataconservancy.bagit.rules.BoundToken#equals(Object) equal} tokens, in the same order.
     *
     * @param one the first list of arbitrary tokens
     * @param two the second list of arbitrary tokens
     * @return true if the lists contain equal content
     */
    private boolean tokenEquals(List<BoundToken> one, List<BoundToken> two) {
        if (one.size() != two.size()) {
            return false;
        }

        for (int i = 0; i < one.size(); i++) {
            if (!one.get(i).equals(two.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param pattern
     * @param path
     * @param pathOff
     * @param patternOff
     * @return
     */
    int findRightAnchor(CharSequence pattern, CharSequence path, int pathOff, int patternOff) {
        // - Find the next token in the pattern from patternOff
        // - The token index (or end of string) is the end of the literal.

        // Assumes that patternOff isn't positioned at a token, and that patternOff + 1 isn't a token either
        int nextToken = Math.min(findNextToken(pattern, patternOff), pattern.length());

        CharSequence literalToMatch = pattern.subSequence(patternOff, nextToken);

        // - Match that literal in the path (from offset fPathIndex)
        // - If the literalToMatch isn't found in 'path', or if the matched literal is an empty string, return Integer.MAX_VALUE
        if (literalToMatch.length() == 0) {
            return Integer.MAX_VALUE;
        }
        int tmpIdx = matchNextLiteral(path, pathOff, literalToMatch);
        if (tmpIdx == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }

        //     - Set the right anchor at the end of the literal.
        return tmpIdx + literalToMatch.length();
    }

    /**
     * Returns true if <em>any</em> of the tokens in {@code pathTokens} represent a <em>literal</em>.
     *
     * @param pathTokens the tokens to check, normally these are the tokens from a single path segment.
     * @return true if any of the supplied token is a literal.
     */
    boolean containsLiterals(List<BoundToken> pathTokens) {
        return (pathTokens.stream().filter(bt -> bt.token == Token.LITERAL).count() > 0);
    }

    /**
     * Returns true if <em>all</em> of the tokens in {@code pathTokens} are <em>literal</em>.
     *
     * @param pathTokens the tokens to check; normally these are tokens from a single path segment.
     * @return
     */
    boolean allLiterals(List<BoundToken> pathTokens) {
        return (pathTokens.stream().filter(bt -> bt.token == Token.LITERAL).count() == pathTokens.size());
    }

    /**
     * Returns true if <em>all</em> of the tokens in {@code pathTokens} represent a <em>literal</em> or
     * <em>path separator</em>.  This method is used to determine of a List of tokens represents a path or a pattern.
     * <p>
     * For example, tokens for {@code /foo/bar/baz.txt} would return {@code true}; tokens for {@code Foo??.java} would
     * return {@code false}, because the '?' characters are not a literal or path separator token.
     * </p>
     *
     * @param pathTokens the tokens to check, normally these are tokens for a path or pattern
     * @return true if the list of tokens contains only literals or separators
     */
    boolean isPath(List<BoundToken> pathTokens) {
        return (pathTokens.stream().filter(
                bt -> bt.token == Token.LITERAL || bt.token == Token.PATH_SEPARATOR).count() == pathTokens.size());
    }

}
