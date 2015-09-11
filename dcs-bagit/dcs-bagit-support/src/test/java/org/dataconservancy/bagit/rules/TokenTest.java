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


import org.junit.Test;

import java.util.Arrays;

import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.DIR;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.DIR_L;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.EXACTLY_ONE;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.EXACTLY_ONE_L;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.PATH_SEP;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.PATH_SEP_L;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.ZERO_OR_MORE;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.ZERO_OR_MORE_L;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.assertTokenListEquals;
import static org.junit.Assert.assertEquals;

/**
 * Insures that the {@link Token} class properly parses tokens.
 */
public class TokenTest {

    /**
     * Tokens are strings with special meanings.  Insure all single-character tokens can be parsed.
     */
    @Test
    public void testParseSingleCharacterString() throws Exception {

        // With parse(...)
        assertEquals(ZERO_OR_MORE, Token.parse("*"));
        assertEquals(EXACTLY_ONE, Token.parse("?"));
        assertEquals(new BoundToken(Token.LITERAL, "f"), Token.parse("f"));
        assertEquals(PATH_SEP, Token.parse("/"));

        // With parseString(...)
        assertTokenListEquals(ZERO_OR_MORE_L, Token.parseString("*"));
        assertTokenListEquals(EXACTLY_ONE_L, Token.parseString("?"));
        assertTokenListEquals(Arrays.asList(new BoundToken(Token.LITERAL, "f")), Token.parseString("f"));
        assertTokenListEquals(PATH_SEP_L, Token.parseString("/"));
    }

    /**
     * Tokens are strings with special meanings.  Insure all multi-character tokens can be parsed.
     */
    @Test
    public void testParseMultipleCharacterStrings() throws Exception {
        // With parse(...)
        assertEquals(DIR, Token.parse("**"));
        assertEquals(new BoundToken(Token.LITERAL, "foobarbaz"), Token.parse("foobarbaz"));

        // With parseString(...)
        assertTokenListEquals(DIR_L, Token.parseString("**"));
        assertTokenListEquals(
                Arrays.asList(new BoundToken(Token.LITERAL, "f"), new BoundToken(Token.LITERAL, "o"),
                        new BoundToken(Token.LITERAL, "o"), new BoundToken(Token.LITERAL, "b"),
                        new BoundToken(Token.LITERAL, "a"), new BoundToken(Token.LITERAL, "r"),
                        new BoundToken(Token.LITERAL, "b"), new BoundToken(Token.LITERAL, "a"),
                        new BoundToken(Token.LITERAL, "z")), Token.parseString("foobarbaz"));
    }

    /**
     * Attempting to parse a string with multiple tokens is an error.
     * Legal with {@link #testParseStringSingleStringContainingDifferentTokens}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseSingleStringContainingDifferentTokens() throws Exception {
        // With parse(...)
        Token.parse("*/?**abc");
    }

    /**
     * Attempting to parseString a string with multiple tokens is ok.
     * An error with {@link #testParseSingleStringContainingDifferentTokens()}
     */
    @Test
    public void testParseStringSingleStringContainingDifferentTokens() throws Exception {
        // With parseString(...)
        assertTokenListEquals(Arrays.asList(ZERO_OR_MORE, PATH_SEP, EXACTLY_ONE, ZERO_OR_MORE, ZERO_OR_MORE,
                new BoundToken(Token.LITERAL, "a"), new BoundToken(Token.LITERAL, "b"),
                new BoundToken(Token.LITERAL, "c")), Token.parseString("*/?**abc"));
    }

    /**
     * Attempting to parse a string with multiple tokens is an error.  Essentially the same test as
     * {@link #testParseSingleStringContainingDifferentTokens()}.  Note this is legal with
     * {@link #testParseStringLiteralEndingWithPathSep()}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseLiteralEndingWithPathSep() throws Exception {
        // With parse(...)
        Token.parse("directory/");
    }

    /**
     * Legal form of {@link #testParseLiteralEndingWithPathSep()}.
     */
    @Test
    public void testParseStringLiteralEndingWithPathSep() throws Exception {
        // With parseString(...)
        assertTokenListEquals(Arrays.asList(new BoundToken(Token.LITERAL, "d"), new BoundToken(Token.LITERAL, "i"),
                new BoundToken(Token.LITERAL, "r"), new BoundToken(Token.LITERAL, "e"),
                new BoundToken(Token.LITERAL, "c"), new BoundToken(Token.LITERAL, "t"),
                new BoundToken(Token.LITERAL, "o"), new BoundToken(Token.LITERAL, "r"),
                new BoundToken(Token.LITERAL, "y"), PATH_SEP), Token.parseString("directory/"));
    }

    /**
     * Parsing zero length strings results in an error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseZeroLengthString() throws Exception {
        // With parse(...)
        assertEquals(new BoundToken(Token.LITERAL, ""), Token.parse(""));
    }

    /**
     * Parsing zero length strings results in an error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringZeroLengthString() throws Exception {
        // With parseString(...)
        assertTokenListEquals(Arrays.asList(new BoundToken(Token.LITERAL, "")), Token.parseString(""));
    }

    /**
     * Empty strings would be parsed as a literal.
     */
    @Test
    public void testParseEmptyString() throws Exception {
        // With parse(...)
        assertEquals(new BoundToken(Token.LITERAL, " "), Token.parse(" "));

        // With parseString(...)
        assertTokenListEquals(Arrays.asList(new BoundToken(Token.LITERAL, " ")), Token.parseString(" "));
    }

    /**
     * Parsing {@code null} results in an error
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() throws Exception {
        // With parse(...)
        assertEquals(new BoundToken(Token.LITERAL, null), Token.parse(null));
    }

    /**
     * Parsing {@code null} with parseString is also an error
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringNull() throws Exception {
        // With parseString(...)
        assertTokenListEquals(Arrays.asList(new BoundToken(Token.LITERAL, null)), Token.parseString(null));
    }

}