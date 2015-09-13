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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An Expression is a String that represents a hierarchical path.  An Expression may represent a path, or a pattern
 * meant to match a path.
 * <p>
 * * Even though a "path" and a "pattern" are both instances of an {@link Expression}, their semantics differ.  A
 * "path" only contains literal and path separator tokens.  A "pattern" may contain literals, path separators, and
 * matching tokens like '*' and '?'.  Path segments are the tokens between consecutive path separators, addressable
 * by their zero-indexed {@link org.dataconservancy.bagit.rules.Expression#depth() depth}.  For example, the Expression
 * '/foo/bar/baz.txt' has three path segments, 'foo' (depth = 0), 'bar' (depth = 1), and 'baz.txt' (depth = 2).  The
 * depth of the Expression is 2.
 * </p>
 */
public class Expression {

    /**
     * Tokens that make up this Expression, with the left-most token at the head of the list.
     */
    final private List<BoundToken> tokens;

    /**
     * Tokens that make up this Expression, except any leading or trailing path separator tokens are stripped.
     * This is more amenable to streams operations.
     */
    final private List<BoundToken> sanitized;

    /**
     * Map of path segments, keyed by their depth.  A path segment is a List of BoundTokens that lie between
     * consecutive path separators.  So a path segment will never contain a path separator character.
     */
    final private ConcurrentHashMap<Integer, List<BoundToken>> segments = new ConcurrentHashMap<>();

    /**
     * Creates a new {@code Expression} instance from the supplied string.  Normally an Expression represents a
     * hierarchical path, so the supplied string will resemble a pattern matching a path, or an actual path.
     *
     * @param expression a string representing an expression.
     */
    public Expression(String expression) {
        this.tokens = ExpressionTokenizer.tokenize(expression);
        this.sanitized = this.tokens.stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        if (this.sanitized.get(0).token == Token.PATH_SEPARATOR) {
            this.sanitized.remove(0);
        }

        if (this.sanitized.get(this.sanitized.size() - 1).token == Token.PATH_SEPARATOR) {
            this.sanitized.remove(this.sanitized.size() - 1);
        }
    }

    /**
     * The entire list of tokens that make up this {@code Expression}, including all path separators.
     *
     * @return the tokens that make up this {@code Expression}
     */
    List<BoundToken> getTokens() {
        return tokens;
    }

    /**
     * A zero-based index representing the depth of the {@code Expression}.
     * <dl>
     *     <dt>{@code /}</dt><dd>depth == 0</dd>
     *     <dt>{@code dir/}</dt><dd>depth == 0</dd>
     *     <dt>{@code /dir}</dt><dd>depth == 0</dd>
     *     <dt>{@code /dir/foo}</dt><dd>depth == 1</dd>
     *     <dt>{@code /dir/foo/bar.txt}</dt><dd>depth == 2</dd>
     *     <dt>{@code &#x2a;&#x2a;/&#x2a;.java}</dt><dd>depth == 1</dd>
     * </dl>
     *
     * @return the depth of this {@code Expression}, always 0 or greater.
     */
    public int depth() {
        return (int) sanitized.stream().filter(bt -> bt.token == Token.PATH_SEPARATOR).count();
    }

    /**
     * A path segment are the tokens that occur between two consecutive path separators.  This method obtains the
     * tokens for the path segment specified {@code depth}.  Path separator tokens will not be included in the returned
     * list.
     *
     * @param depth the zero-indexed depth of the path segment to retrieve
     * @return the tokens making up the path segment, or an empty List if the depth is out of bounds
     */
    public List<BoundToken> getPathSegment(int depth) {
        return segments.computeIfAbsent(depth, (d) -> {
            List<BoundToken> pathSegments = new ArrayList<>();
            int i = 0;
            for (BoundToken t : sanitized) {
                if (i > d) {
                    // done recording tokens, break
                    break;
                }

                if (t.token == Token.PATH_SEPARATOR) {
                    // increment depth
                    i++;
                    // continue, we don't record path separators
                    continue;
                }


                if (d - i == 0) {
                    // record the token
                    pathSegments.add(t);
                }
            }

            return pathSegments;
        });
    }

    @Override
    public String toString() {
        return tokens.stream()
                .collect(StringBuilder::new, (s, bt) -> s.append(bt.bound), StringBuilder::append).toString();
    }
}
