/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.bagit.rules;

/**
 * Binds a {@link Token} to the string that it represents.  Most Tokens have their strings bound already, by the
 * {@link org.dataconservancy.bagit.rules.Token#getTokenString()} method:
 * <dl>
 *     <dt>{@code PATH_SEPARATOR}:</dt><dd>{@code /}</dd>
 *     <dt>{@code EXACTLY_ONE_CHARACTER}:</dt><dd>{@code ?}</dd>
 *     <dt>{@code ZERO_OR_MORE_CHARACTERS}:</dt><dd>{@code *}</dd>
 *     <dt>{@code DIRECTORY}:</dt><dd>{@code **}</dd>
 * </dl>
 * The exception is the {@link Token#LITERAL LITERAL token}, because it isn't known, <em>a priori</em>, what the
 * literal characters will be.
 * <p>
 * Therefore this class is mostly redundant, and may fail the smell test, but it serves to bind the string
 * representation to all Tokens, useful really for only the {@code LITERAL} token.
 * </p>
 */
class BoundToken {

    String bound;
    Token token;

    BoundToken(Token token, String toBind) {
        this.token = token;
        this.bound = toBind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundToken that = (BoundToken) o;

        if (bound != null ? !bound.equals(that.bound) : that.bound != null) return false;
        if (token != that.token) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bound != null ? bound.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BoundToken{" +
                "bound='" + bound + '\'' +
                ", token=" + token +
                '}';
    }
}
