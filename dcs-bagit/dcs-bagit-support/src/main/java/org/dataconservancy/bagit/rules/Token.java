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
import java.util.stream.Collectors;

import static org.dataconservancy.bagit.rules.Message.ERR_NULL;

/**
 * Tokens are strings that make up a location expressions.  Location expressions are patterns that are matched against
 * paths.  Location expressions are inspired by Apache Ant file pattern matching.
 */
enum Token {

    /**
     * A token matching exactly one character in an expression.
     */
    EXACTLY_ONE_CHARACTER("?"),

    /**
     * A token that will match multiple directory levels in an expression.
     */
    DIRECTORY("**"),

    /**
     * A token matching zero or more characters in an expression. <em>Must always be defined sometime after
     * {@link #DIRECTORY}</em>
     */
    ZERO_OR_MORE_CHARACTERS("*"),


    /**
     * A token that separates path segments in an expression.
     */
    PATH_SEPARATOR("/"),

    /**
     * A special token with a {@code null} token string.  <em>Must always be defined last</em>
     */
    LITERAL();

    private static final String ERR_MULTIPLE_TOKENS = "Candidate sequence '%s' contains multiple tokens.  " +
            "Try splitting up the tokens and submitting the tokens one at a time.";

    /**
     * String representation of the token, if there is one.
     */
    private String tokenString;

    /**
     * Construct a Token with no string representation.  Currently reserved for {@link #LITERAL} tokens.
     */
    private Token() {
        this.tokenString = null;
    }

    /**
     * Construct a token with the supplied string representation.
     *
     * @param tokenString the string representation of the token.
     * @throws java.lang.IllegalArgumentException if the {@code tokenString} is {@code null}
     */
    private Token(String tokenString) {
        if (tokenString == null) {
            throw new IllegalArgumentException(String.format(ERR_NULL, "tokenString"));
        }
        this.tokenString = tokenString;
    }

    /**
     * Obtain the string form of the token, may be {@code null}.  {@link #LITERAL} tokens will not
     * have a string form, because a literal is the set of characters that <em>do not</em> represent a token.
     *
     * @return the string form of the token, or {@code null} in the case of {@code LITERAL} tokens.
     */
    String getTokenString() {
        return tokenString;
    }

    /**
     * Attempts to parse a string which represents a <em>single</em> token into a {@code Token}
     *
     * @param candidate the candidate token string
     * @return a {@code Token} if {@code candidate} represents a valid token
     * @throws java.lang.IllegalArgumentException if {@code candidate} does not represent a valid token
     */
    static BoundToken parse(CharSequence candidate) {
        if (candidate == null || candidate.length() == 0) {
            throw new IllegalArgumentException(String.format(ERR_NULL, "candidate"));
        }

        for (Token m : Token.values()) {

            // See if the candidate token string equals the string representation
            // of the token (except LITERAL), and return it
            if (m.tokenString != null && m.tokenString.equals(candidate)) {
                return new BoundToken(m, candidate.toString());
            }

            // Check to see if the candidate token string _contains_ the string representation
            // of the token (except LITERAL).  If so, that means that the candidate contains multiple
            // tokens, which isn't allowed.
            if (candidate.length() > 1 &&
                    m.tokenString != null &&
                    candidate.chars().anyMatch(
                            c -> m.tokenString.contains(Character.toString((char) c)))) {
                throw new IllegalArgumentException(String.format(ERR_MULTIPLE_TOKENS, candidate));
            }
        }

        // None of our Token string representations equaled the candidate string.
        // The candidate string did not _contain_ any of the Token string representations
        // We must be left with a LITERAL.

        return new BoundToken(Token.LITERAL, candidate.toString());
    }

    static List<BoundToken> parseString(CharSequence candidate) {
        if (candidate == null || candidate.length() == 0) {
            throw new IllegalArgumentException(String.format(ERR_NULL, "candidate"));
        }

        return
            candidate.chars().mapToObj(c -> {
                // This code block maps each character in the sequence to a BoundToken.

                // Cast the int to a char, and parse it as a String
                String s = String.valueOf((char) c);
                BoundToken bound = null;

                // Iterate over every Token (except LITERAL), and see if the string matches
                for (Token t : Token.values()) {
                    if (t.getTokenString() != null && t.getTokenString().equals(s)) {
                        bound = new BoundToken(t, s);
                    }
                }

                // If there was no match, then we must have a LITERAL.
                if (bound == null) {
                    bound = new BoundToken(LITERAL, s);
                }

                return bound;
            }).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenString='" + tokenString + '\'' +
                '}';
    }
}
