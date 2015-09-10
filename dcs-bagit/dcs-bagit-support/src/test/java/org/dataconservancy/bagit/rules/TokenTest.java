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
        assertEquals(Token.ZERO_OR_MORE_CHARACTERS, Token.parse("*"));
        assertEquals(Token.EXACTLY_ONE_CHARACTER, Token.parse("?"));
        assertEquals(Token.LITERAL, Token.parse("f"));
        assertEquals(Token.PATH_SEPARATOR, Token.parse("/"));
    }

    /**
     * Tokens are strings with special meanings.  Insure all multi-character tokens can be parsed.
     */
    @Test
    public void testParseMultipleCharacterStrings() throws Exception {
        assertEquals(Token.DIRECTORY, Token.parse("**"));
        assertEquals(Token.LITERAL, Token.parse("foobarbaz"));
    }

    /**
     * Attempting to parse a string with multiple tokens is an error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseSingleStringContainingDifferentTokens() throws Exception {
        Token.parse("*/?**abc");
    }

    /**
     * Attempting to parse a string with multiple tokens is an error. (Just another case similar to above)
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseLiteralEndingWithPathSep() throws Exception {
        Token.parse("directory/");
    }

    /**
     * Zero length strings would be parsed as a literal.
     */
    @Test
    public void testParseZeroLengthString() throws Exception {
        assertEquals(Token.LITERAL, Token.parse(""));
    }

    /**
     * Empty strings would be parsed as a literal.
     */
    @Test
    public void testParseEmptyString() throws Exception {
        assertEquals(Token.LITERAL, Token.parse(" "));
    }

    /**
     * Parsing {@code null} results in an error
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() throws Exception {
        assertEquals(Token.LITERAL, Token.parse(null));
    }
}