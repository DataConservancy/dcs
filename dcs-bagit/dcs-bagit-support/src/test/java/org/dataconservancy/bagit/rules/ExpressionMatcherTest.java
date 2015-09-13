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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionMatcherTest {

    private ExpressionMatcher underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new ExpressionMatcher();
    }

    @Test
    public void testMatchExpressionWip3() throws Exception {
        Expression pattern = new Expression("**/Foo??.java");
        Expression path = new Expression("src/test/java/FooIT.java");
        Expression nonMatchingPath = new Expression("src/test/java/FooI.java");

        // sanity
        assertTrue(underTest.match(path, path));

        assertTrue(underTest.match(pattern, path));

        assertFalse(underTest.match(pattern, nonMatchingPath));
    }


    @Test
    public void testMatchExpressionWip2() throws Exception {
        Expression pattern = new Expression("**/*IT.java");
        Expression path = new Expression("src/test/java/FooIT.java");
        Expression nonMatchingPath = new Expression("src/test/java/Bar.java");

//        // sanity - insure that "*IT.java" will match "FooIT.java"
//        List<BoundToken> tokenPattern = new ArrayList<>();
//        tokenPattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
//        tokenPattern.addAll(BoundTokensTestUtil.literalsForString("IT.java"));
//
//        List<BoundToken> tokenPath = new ArrayList<>();
//        tokenPath.addAll(BoundTokensTestUtil.literalsForString("FooIT.java"));
//        assertTrue(underTest.match(tokenPattern, tokenPath));

        // sanity
        assertTrue(underTest.match(path, path));

        assertTrue(underTest.match(pattern, path));

        assertFalse(underTest.match(nonMatchingPath, path));
    }

    @Test
    public void testMatchExpressionWip() throws Exception {
        Expression pattern = new Expression("**/FooIT.java");
        Expression path = new Expression("src/test/java/FooIT.java");
        Expression nonMatchingPath = new Expression("src/test/java/BarIT.java");

        // sanity
        assertTrue(underTest.match(path, path));

        assertTrue(underTest.match(pattern, path));

        assertFalse(underTest.match(pattern, nonMatchingPath));
    }

    @Test
    public void testMatchWithOnlyLiterals() throws Exception {
        List<BoundToken> pattern = BoundTokensTestUtil.literalsForString("bar");
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("bar");

        // sanity
        assertTrue(underTest.match(pattern, path));

        assertFalse(underTest.match(BoundTokensTestUtil.literalsForString("foo"), path));
    }

    @Test
    public void testNoMatchBeginningZeroPlus() throws Exception {
        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.java"));

        List<BoundToken> path = BoundTokensTestUtil.literalsForString("src");

        assertFalse(underTest.match(pattern, path));
    }

    @Test
    public void testLiteralsWithExactlyOne() throws Exception {
        // pattern:  "?tart?IT.jav*"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("tart"));
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.jav"));
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("startXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: startXIT.jav (sanity, should pass)
        path = BoundTokensTestUtil.literalsForString("startXIT.jav");
        assertTrue(underTest.match(pattern, path));

        // path: strtXIT.java (first literal 'tart' doesn't match)
        path = BoundTokensTestUtil.literalsForString("strtXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: startXITT.java (middle literal 'IT.jav' doesn't match)
        path = BoundTokensTestUtil.literalsForString("startXITT.java");
        assertFalse(underTest.match(pattern, path));
    }

    @Test
    public void testLiteralsWithZeroPlus() throws Exception {
        // pattern:  "*tart*IT.jav*"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("tart"));
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.jav"));
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("startXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: startXIT.jav (sanity, should pass)
        path = BoundTokensTestUtil.literalsForString("startXIT.jav");
        assertTrue(underTest.match(pattern, path));

        // path: tartXIT.java (sanity, should pass)
        path = BoundTokensTestUtil.literalsForString("tartXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: tartXIT.jav (sanity, should pass)
        path = BoundTokensTestUtil.literalsForString("tartXIT.jav");
        assertTrue(underTest.match(pattern, path));

        // path: strtXIT.java (first literal 'tart' doesn't match)
        path = BoundTokensTestUtil.literalsForString("strtXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: startXITT.java (middle literal 'IT.jav' doesn't match)
        path = BoundTokensTestUtil.literalsForString("startXITT.java");
        assertFalse(underTest.match(pattern, path));
    }


    @Test
    public void testMultipleSingleCharacterTokens() throws Exception {
        // pattern:  "?tart?IT.jav?"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("tart"));
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.jav"));
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("startXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: FootartXIT.java (too many characters for first token)
        path = BoundTokensTestUtil.literalsForString("FootartXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: tartXIT.java (no characters for first token)
        path = BoundTokensTestUtil.literalsForString("tartXIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: StartItUpIT.java (too many characters for middle token)
        path = BoundTokensTestUtil.literalsForString("StartItUpIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: StartIT.java (no characters for middle token)
        path = BoundTokensTestUtil.literalsForString("StartIT.java");
        assertFalse(underTest.match(pattern, path));

        // path: StartXIT.jav (no characters for last token)
        path = BoundTokensTestUtil.literalsForString("StartXIT.jav");
        assertFalse(underTest.match(pattern, path));

        // path: StartXIT.javaa (too many characters for last token)
        path = BoundTokensTestUtil.literalsForString("StartXIT.javaa");
        assertFalse(underTest.match(pattern, path));
    }

    @Test
    public void testMatchLiteralFirstExactlyOneNoMatch() throws Exception {
        // pattern:  "Start?IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(BoundTokensTestUtil.literalsForString("Start"));
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.java"));

        // path: startXIT.java (sanity, should pass)
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("StartXIT.java");
        assertTrue(underTest.match(pattern, path));

        // path: StartFooIT.java (won't match)
        path = BoundTokensTestUtil.literalsForString("StartFooIT.java");

        assertFalse(underTest.match(pattern, path));
    }

    @Test
    public void testMatchLiteralFirstExactlyOne() throws Exception {
        // pattern:  "Start?IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(BoundTokensTestUtil.literalsForString("Start"));
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.java"));

        // path: StartXIT.java
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("StartXIT.java");

        assertTrue(underTest.match(pattern, path));
    }

    @Test
    public void testMatchLiteralFirstZeroPlus() throws Exception {
        // pattern:  "Start*IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(BoundTokensTestUtil.literalsForString("Start"));
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.java"));

        // path: StartCarIT.java
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("StartCarIT.java");

        assertTrue(underTest.match(pattern, path));
    }

    @Test
    public void testMatchTokenFirst() throws Exception {
        // pattern:  "*File*IT.java"

        List<BoundToken> pattern = new ArrayList<>();
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("File"));
        pattern.add(BoundTokensTestUtil.ZERO_OR_MORE);
        pattern.addAll(BoundTokensTestUtil.literalsForString("IT.java"));

        // path: UnixFileSmallIT.java
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("UnixFileSmallIT.java");

        assertTrue(underTest.match(pattern, path));
    }

    @Test
    public void testMatchConsecutiveMatchTokens() throws Exception {
        // pattern: "Foo??.java"
        List<BoundToken> pattern = new ArrayList<>();
        pattern.addAll(BoundTokensTestUtil.literalsForString("Foo"));
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.add(BoundTokensTestUtil.EXACTLY_ONE);
        pattern.addAll(BoundTokensTestUtil.literalsForString(".java"));

        // path: FooIT.java
        List<BoundToken> path = BoundTokensTestUtil.literalsForString("FooIT.java");

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