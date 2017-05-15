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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * BoundTokens that are shared across unit tests.
 */
class BoundTokensTestUtil {

    /**
     * The {@code BoundToken} version of {@link Token#ZERO_OR_MORE_CHARACTERS}
     */
    static final BoundToken ZERO_OR_MORE = new BoundToken(Token.ZERO_OR_MORE_CHARACTERS, "*");

    /**
     * The {@code BoundToken} version of {@link Token#ZERO_OR_MORE_CHARACTERS}, in a single element List
     */
    static final List<BoundToken> ZERO_OR_MORE_L = Arrays.asList(
            new BoundToken(Token.ZERO_OR_MORE_CHARACTERS, "*"));

    /**
     * The {@code BoundToken} version of {@link Token#EXACTLY_ONE_CHARACTER}
     */
    static final BoundToken EXACTLY_ONE = new BoundToken(Token.EXACTLY_ONE_CHARACTER, "?");

    /**
     * The {@code BoundToken} version of {@link Token#EXACTLY_ONE_CHARACTER}, in a single element List
     */
    static final List<BoundToken> EXACTLY_ONE_L = Arrays.asList(
            new BoundToken(Token.EXACTLY_ONE_CHARACTER, "?"));

    /**
     * The {@code BoundToken} version of {@link Token#PATH_SEPARATOR}
     */
    static final BoundToken PATH_SEP = new BoundToken(Token.PATH_SEPARATOR, "/");

    /**
     * The {@code BoundToken} version of {@link Token#PATH_SEPARATOR}, in a single element List
     */
    static final List<BoundToken> PATH_SEP_L = Arrays.asList(
            new BoundToken(Token.PATH_SEPARATOR, "/"));

    /**
     * The {@code BoundToken} version of {@link Token#DIRECTORY}, in a single element List
     */
    static final BoundToken DIR = new BoundToken(Token.DIRECTORY, "**");

    /**
     * The {@code BoundToken} version of {@link Token#DIRECTORY}, represented as a List containing two
     * {@link #ZERO_OR_MORE} BoundTokens.
     */
    static final List<BoundToken> DIR_L = Arrays.asList(ZERO_OR_MORE, ZERO_OR_MORE);

    /**
     * Convenience method for creating a {@link Token#LITERAL literal} token for each character in {@code s}.  It does
     * not evaluate the characters in {@code s} for whether or not they should actually be made literals.  That is the
     * responsibility of the developer.  (For example, this method will happily make literal tokens of "*", "?", and
     * "/", which are not allowed by {@link Token#parse(CharSequence)}.)
     *
     * @param s the string to represent as a List of BoundTokens
     * @return a List containing LITERAL BoundTokens for each character in {@code s}
     */
    static List<BoundToken> literalsForString(String s) {
        ArrayList<BoundToken> literals = new ArrayList<>();
        s.chars().forEach(c -> literals.add(new BoundToken(Token.LITERAL, String.valueOf((char) c))));
        return literals;
    }

    /**
     * Asserts that the values in the expected and actual Lists are equal.  This method will assert that
     * the lists are the same size before comparing their values.
     *
     * @param expected the expected List of BoundTokens
     * @param actual   the actual List of BoundTokens, normally representing a test result.
     */
    static void assertTokenListEquals(List<BoundToken> expected, List<BoundToken> actual) {
        assertExpectedListCount(expected.size(), actual);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Expected token: '" + expected.get(i) + "' but found '" + actual.get(i) + "'",
                    expected.get(i), actual.get(i));
        }
    }

    /**
     * Asserts that the supplied list of BoundTokens has the expected count.
     *
     * @param expectedCount the expected number of BoundTokens in {@code actual}
     * @param actual        a List of BoundTokens, normally representing the result of a test.
     */
    static void assertExpectedListCount(int expectedCount, List<BoundToken> actual) {
        assertEquals("Expected " + expectedCount + " BoundTokens, found " + actual.size() + ": " +
                        actual.stream().map(bt -> "['" + bt.token.name() + "', '" + bt.bound + "']")
                                .collect(Collectors.joining(", ")),
                expectedCount, actual.size());
    }
}
