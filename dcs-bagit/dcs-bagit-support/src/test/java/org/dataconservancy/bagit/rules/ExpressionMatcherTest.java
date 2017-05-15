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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.DIR_L;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.EXACTLY_ONE;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.ZERO_OR_MORE;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.assertTokenListEquals;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.literalsForString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Many, many tests against various methods in the ExpressionMatcher class.
 * Most of the test methods in this class contain multiple assertions.  Normally there will be one assertion for a
 * sanity check - an assertion that should always be true.  Often there will be multiple sanity checks.
 * <p>
 * Because ExpressionMatcher is package-private, it can be hard to tell what the entry points into the ExpressionMatcher
 * class are, and this test class doesn't help you determine that. Clients of ExpressionMatcher should be calling
 * either:
 * <ul>
 *   <li>{@link org.dataconservancy.bagit.rules.ExpressionMatcher#match(Expression, Expression)}</li>
 *   <li>{@link org.dataconservancy.bagit.rules.ExpressionMatcher#match(java.util.List, java.util.List)}</li>
 * </ul>
 * This test class covers not only these entry point methods, but other utility methods as well.
 * </p>
 */
public class ExpressionMatcherTest {

    private ExpressionMatcher underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new ExpressionMatcher();
    }

    /**
     * Attempts a match using an Expression that starts with '**' and contains consecutive '?' matching tokens.
     */
    @Test
    public void testMatchExpressionLeadingDirectoryAndConsecutiveExactlyOne() throws Exception {
        // The pattern to match against: leading '**' and consecutive '??'
        Expression pattern = new Expression("**/Foo??.java");

        // This path should match the pattern: src/test/java matches '**' and FooIT.java matches 'Foo??.java'
        Expression path = new Expression("src/test/java/FooIT.java");

        // This path should not match (the consecutive token '??' will remain unmatched)
        Expression nonMatchingPath = new Expression("src/test/java/FooI.java");

        // sanity: a path should match itself.
        assertTrue(underTest.match(path, path));

        assertTrue(underTest.match(pattern, path));
        assertFalse(underTest.match(pattern, nonMatchingPath));
    }

    /**
     * Attempts a match using an Expression that starts with '**' and contains a '*' matching token.
     */
    @Test
    public void testMatchExpressionLeadingDirectoryAndZeroPlus() throws Exception {
        // The pattern to match against: leading '**' and a '*'
        Expression pattern = new Expression("**/*IT.java");

        // This path should match the pattern: src/test/java matches '**' and FooIT.java matches '*IT.java'
        Expression path = new Expression("src/test/java/FooIT.java");

        // This path should not match (the path segment Bar.java will remain unmatched)
        Expression nonMatchingPath = new Expression("src/test/java/Bar.java");

        // sanity: a path should match itself
        assertTrue(underTest.match(path, path));

        assertTrue(underTest.match(pattern, path));
        assertFalse(underTest.match(nonMatchingPath, path));
    }

    /**
     * Attempts a match using an Expression that starts with a '**' matching token.
     */
    @Test
    public void testMatchExpressionLeadingDirectory() throws Exception {
        // The pattern to match against: leading '**'
        Expression pattern = new Expression("**/FooIT.java");

        // This path should match the pattern: src/test/java matches '**', and FooIT.java matches the 'FooIT.java' literal
        Expression path = new Expression("src/test/java/FooIT.java");

        // This path should not match
        Expression nonMatchingPath = new Expression("src/test/java/BarIT.java");

        // sanity: a path should match itself
        assertTrue(underTest.match(path, path));

        assertTrue(underTest.match(pattern, path));
        assertFalse(underTest.match(pattern, nonMatchingPath));
    }

    /**
     * Attempts a match using equal lists of {@code List&lt;BoundToken>} containing only literals (no matching tokens or
     * path separators)
     */
    @Test
    public void testMatchWithOnlyLiterals() throws Exception {
        List<BoundToken> pattern = literalsForString("bar");
        List<BoundToken> path = literalsForString("bar");
        assertTokenListEquals(path, pattern);

        // sanity: non-equal literal token lists should not match
        assertFalse(underTest.match(literalsForString("foo"), path));

        // test to make sure that equal literal token lists will match
        assertTrue(underTest.match(pattern, path));
    }

    /**
     * Verifies that a literal will not match a pattern that contains leading directory match tokens followed by
     * a non-matching literal.  A complicated way of saying that we verify that the pattern "*IT.java" won't match
     * "src".
     */
    @Test
    public void testNoMatchBeginningZeroPlus() throws Exception {
        // pattern: *IT.java
        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("IT.java"));

        // path: src
        List<BoundToken> path = literalsForString("src");

        assertFalse(underTest.match(pattern, path));
    }

    /**
     * Attempts a match {@code List&lt;BoundToken>} leading with a '?' matching token, ending with a '*' matching
     * token, and with a single '?' token in the middle.
     */
    @Test
    public void testLiteralsWithExactlyOne() throws Exception {
        // pattern:  "?tart?IT.jav?"
        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(EXACTLY_ONE);
        pattern.addAll(literalsForString("tart"));
        pattern.add(EXACTLY_ONE);
        pattern.addAll(literalsForString("IT.jav"));
        pattern.add(EXACTLY_ONE);

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = literalsForString("startXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: strtXIT.java (first literal 'tart' doesn't match)
        path = literalsForString("strtXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: startXITT.java (middle literal 'IT.jav' doesn't match)
        path = literalsForString("startXITT.java");
        assertFalse(underTest.match(pattern, path));

        // path: startXIT.jav (last token '?' doesn't match - missing character in path)
        path = literalsForString("startXIT.jav");
        assertFalse(underTest.match(pattern, path));

        // path: startXIT.javaa (last literal 'a' in path doesn't match)
        path = literalsForString("startXIT.javaa");
        assertFalse(underTest.match(pattern, path));
    }

    /**
     * Attempts a match {@code List&lt;BoundToken>} leading with a '*' matching token, ending with a '*' matching
     * token, and with a single '*' token in the middle.
     */
    @Test
    public void testLiteralsWithZeroPlus() throws Exception {
        // pattern:  "*tart*IT.jav*"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("tart"));
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("IT.jav"));
        pattern.add(ZERO_OR_MORE);

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = literalsForString("startXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: startXIT.jav (sanity, should pass)
        path = literalsForString("startXIT.jav");
        assertTrue(underTest.match(pattern, path));

        // path: tartXIT.java (sanity, should pass)
        path = literalsForString("tartXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: tartXIT.jav (sanity, should pass)
        path = literalsForString("tartXIT.jav");
        assertTrue(underTest.match(pattern, path));

        // path: strtXIT.java (first literal 'tart' doesn't match)
        path = literalsForString("strtXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: startXITT.java (middle literal 'IT.jav' doesn't match)
        path = literalsForString("startXITT.java");
        assertFalse(underTest.match(pattern, path));
    }

    /**
     * Attempts various path matches against a pattern that contains three matching '?' tokens, at the
     * beginning, middle, and end of the pattern.
     */
    @Test
    public void testMultipleSingleCharacterTokens() throws Exception {
        // pattern:  "?tart?IT.jav?"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(EXACTLY_ONE);
        pattern.addAll(literalsForString("tart"));
        pattern.add(EXACTLY_ONE);
        pattern.addAll(literalsForString("IT.jav"));
        pattern.add(EXACTLY_ONE);

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = literalsForString("startXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: FootartXIT.java (too many characters for first token)
        path = literalsForString("FootartXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: tartXIT.java (no characters for first token)
        path = literalsForString("tartXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: StartItUpIT.java (too many characters for middle token)
        path = literalsForString("StartItUpIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: StartIT.java (no characters for middle token)
        path = literalsForString("StartIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: StartXIT.jav (no characters for last token)
        path = literalsForString("StartXIT.jav");
        assertFalse(underTest.match(pattern, path));

        // path: StartXIT.javaa (too many characters for last token)
        path = literalsForString("StartXIT.javaa");
        assertFalse(underTest.match(pattern, path));
    }

    /**
     * Attempts to match a path against a pattern containing a single matching token '?' in the middle.
     */
    @Test
    public void testMatchLiteralFirstExactlyOneNoMatch() throws Exception {
        // pattern:  "Start?IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(literalsForString("Start"));
        pattern.add(EXACTLY_ONE);
        pattern.addAll(literalsForString("IT.java"));

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = literalsForString("StartXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: StartFooIT.java (won't match)
        path = literalsForString("StartFooIT.java");

        assertFalse(underTest.match(pattern, path));
    }

    /**
     * Attempts to match a path against a pattern containing a single matching token '*' in the middle.
     */
    @Test
    public void testMatchLiteralFirstZeroPlus() throws Exception {
        // pattern:  "Start*IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(literalsForString("Start"));
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("IT.java"));

        // path: StartCarIT.java ('*' should match 'Car')
        List<BoundToken> path = literalsForString("StartCarIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: StartIT.java ('*' should match zero characters)
        path = literalsForString("StartIT.java");
        assertTrue(underTest.match(pattern, path));
    }

    @Test
    public void testMatchTokenFirst() throws Exception {
        // pattern:  "*File*IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("File"));
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("IT.java"));

        // path: UnixFileSmallIT.java
        List<BoundToken> path = literalsForString("UnixFileSmallIT.java");

        assertTrue(underTest.match(pattern, path));
    }

    @Test
    public void testMatchConsecutiveMatchTokens() throws Exception {
        // pattern: "Foo??.java"
        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(literalsForString("Foo"));
        pattern.add(EXACTLY_ONE);
        pattern.add(EXACTLY_ONE);
        pattern.addAll(literalsForString(".java"));

        // path: FooIT.java
        List<BoundToken> path = literalsForString("FooIT.java");

        assertTrue(underTest.match(pattern, path));
    }

    /**
     * Attempt to match a directory against the directory match token '**'
     */
    @Test
    public void testMatchZeroPlusAndLiteral() throws Exception {
        // pattern: "**"
        List<BoundToken> pattern = DIR_L;

        // path: "src"
        List<BoundToken> path = literalsForString("src");

        assertTrue(underTest.match(pattern, path));
    }

    /**
     * Insures that a pattern like 'Foo**IT.java' - while almost certainly a mistake by the person who created the
     * pattern - is a valid pattern. Make sure it matches.
     */
    @Test
    public void testMatchMultipleZeroPlusTokens() throws Exception {
        // pattern: "Foo**IT.java"
        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(literalsForString("Foo"));
        pattern.add(ZERO_OR_MORE);
        pattern.add(ZERO_OR_MORE);
        pattern.addAll(literalsForString("IT.java"));

        // path: "FooIT.java" should match - '**' matches zero characters
        List<BoundToken> path = literalsForString("FooIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: "FooBarBazIT.java" should match - '**' matches "BarBaz"
        path = literalsForString("FooBarBazIT.java");
        assertTrue(underTest.match(pattern, path));
    }

    @Test
    public void testFindNextToken() throws Exception {
        assertEquals(19, underTest.findNextToken("src/test/resources/*IT.java", 0));
        assertEquals(0, underTest.findNextToken("*File*IT.java", 0));
        assertEquals(5, underTest.findNextToken("*File*IT.java", 1));
    }

    @Test
    public void testFindNextLiteral() throws Exception {
        assertEquals(0, underTest.findNextLiteral("src/test/resources/*IT.java", 0));
        assertEquals(1, underTest.findNextLiteral("*File*IT.java", 0));
        assertEquals(2, underTest.findNextLiteral("*File*IT.java", 2));
        assertEquals(5, underTest.findNextLiteral("Foo??.java", 3));
    }

    @Test
    public void testFindNextLiteralString() throws Exception {
        assertEquals(1, underTest.matchNextLiteral("*File*IT.java", 0, "File"));
        assertEquals(6, underTest.matchNextLiteral("*File*IT.java", 0, "IT"));
        assertEquals(6, underTest.matchNextLiteral("*File*IT.java", 0, "IT.java"));
        assertEquals(6, underTest.matchNextLiteral("*File*IT.java", 4, "IT.java"));
        assertEquals(Integer.MIN_VALUE, underTest.matchNextLiteral("*FileIT.java", 0, "doodle"));
    }

    @Test
    public void testFindRightAnchorFromBeginning() throws Exception {
        String pattern = "File*IT.java";
        String path = "FileUnixIT.java";

        assertEquals("File".length(), underTest.findRightAnchor(pattern, path, 0,0 ));
    }

    @Test
    public void testFindRightAnchorFromMiddle() throws Exception {
        String pattern = "File*IT.java";
        String path = "FileUnixIT.java";

        // find the right anchor after we've matched pattern "File*" to path "FileUnix"
        assertEquals(path.length(), underTest.findRightAnchor(pattern, path, "FileUnix".length(), "File*".length()));
    }

    @Test
    public void testFindRightAnchorMultipleTokens() throws Exception {
        String pattern = "Foo*Bar*Baz";
        String path = "FooXBarYBaz";

        assertEquals(3, underTest.findRightAnchor(pattern, path, 0, 0));
        assertEquals(7, underTest.findRightAnchor(pattern, path, "FooX".length(), "Foo*".length()));
        assertEquals(11, underTest.findRightAnchor(pattern, path, "FooXBarY".length(), "Foo*Bar*".length()));
    }

    @Test
    public void testFindRightAnchorFoo() throws Exception {
        String pattern = "Foo??.java";
        String path = "src";

        // behavior when the path is not in the pattern
        assertEquals(Integer.MAX_VALUE, underTest.findRightAnchor(pattern, path, 0, 0));
    }

    @Test
    public void testFindRightAnchorFooIT() throws Exception {
        String pattern = "Foo??.java";
        String path = "FooIT.java";

        // behavior when the pattern offset is positioned at a token
        assertEquals(Integer.MAX_VALUE, underTest.findRightAnchor(pattern, path, 0, 3));
    }

    @Test
    public void testRightAnchorBar() throws Exception {
        String pattern = "*File*IT.java";
        String path = "UnixFileSmallIT.java";

        // behavior when the path offset is already positioned at the right anchor
        assertEquals(8, underTest.findRightAnchor(pattern, path, 4, 1));
    }

    /**
     * Insures that the match token '**' - represented as a single BoundToken containing a DIRECTORY, or two
     * consecutive BoundTokens containing a ZERO_OR_MORE_CHARACTERS - are both considered a "directory match" token
     * by the ExpressionMatcher.
     *
     * @throws Exception
     */
    @Test
    public void testIsDirectoryMatch() throws Exception {
        List<BoundToken> directory = Arrays.asList(new BoundToken(Token.DIRECTORY, Token.DIRECTORY.getTokenString()));
        List<BoundToken> consecutiveZeroOrMore = Arrays.asList(
                new BoundToken(Token.ZERO_OR_MORE_CHARACTERS, Token.ZERO_OR_MORE_CHARACTERS.getTokenString()),
                new BoundToken(Token.ZERO_OR_MORE_CHARACTERS, Token.ZERO_OR_MORE_CHARACTERS.getTokenString()));

        assertTrue(underTest.isDirectoryMatchToken(directory));
        assertTrue(underTest.isDirectoryMatchToken(consecutiveZeroOrMore));
    }

    void assertListsEqual(List<String> expected, List<String> actual) {
        assertExpectedCount(expected.size(), actual);

        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Expected path segments to be equal.  Expected: '" + expected.get(i) +
                    "', Actual: '" + actual.get(i) + "'", expected.get(i), actual.get(i));
        }
    }

    void assertExpectedCount(int expectedCount, List<String> actual) {
        assertEquals("Expected List to contain " + expectedCount + " elements.  Contained " + actual.size() + ": " +
                        actual.stream().map(v -> "'" + v + "'").collect(Collectors.joining(", ")),
                expectedCount, actual.size());
    }
}