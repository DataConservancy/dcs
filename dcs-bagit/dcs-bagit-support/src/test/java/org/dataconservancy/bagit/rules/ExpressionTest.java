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

import java.util.ArrayList;
import java.util.List;

import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.PATH_SEP;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.PATH_SEP_L;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.ZERO_OR_MORE;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.assertTokenListEquals;
import static org.dataconservancy.bagit.rules.BoundTokensTestUtil.literalsForString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionTest {

    @Test
    public void testSimple() throws Exception {
        Expression exp = new Expression("src/test/resources/*IT.java");

        // == src/test/resources/*IT.java
        List<BoundToken> expected = literalsForString("src");
        expected.add(PATH_SEP);
        expected.addAll(literalsForString("test"));
        expected.add(PATH_SEP);
        expected.addAll(literalsForString("resources"));
        expected.add(PATH_SEP);
        expected.add(ZERO_OR_MORE);
        expected.addAll(literalsForString("IT.java"));

        List<BoundToken> actual = exp.getTokens();

        assertTokenListEquals(expected, actual);

        // depth is an index
        assertEquals(3, exp.depth());

        // get path segment by depth test
        assertTokenListEquals(literalsForString("src"), exp.getPathSegment(0));
        assertTokenListEquals(literalsForString("test"), exp.getPathSegment(1));
        assertTokenListEquals(literalsForString("resources"), exp.getPathSegment(2));

        expected = new ArrayList<>();
        expected.add(ZERO_OR_MORE);
        expected.addAll(literalsForString("IT.java"));
        assertTokenListEquals(expected, exp.getPathSegment(3));

        // out of bounds tests
        assertTrue(exp.getPathSegment(exp.depth() + 5).isEmpty());
        assertTrue(exp.getPathSegment(-1).isEmpty());
    }

    @Test
    public void testWithEmptyRoot() throws Exception {
        Expression exp = new Expression("/");
        assertEquals(0, exp.depth());

        // TODO decide what to do with the automatic addition of '**'
        // for example, the Expression "/" is tokenized as "/**".
        // any path ending in "/" is going to be tokenized with a trailing "**",
        // and the user may not intend that behavior (for example if they are just wanting
        // to express a path (not a pattern).
        assertTokenListEquals(PATH_SEP_L, exp.getTokens());
        assertTrue(exp.getPathSegment(0).isEmpty());
    }

    @Test
    public void testWithSingleFileRoot() throws Exception {
        Expression exp = new Expression("/foo.txt");
        assertEquals(0, exp.depth());

        // "/foo.txt"
        List<BoundToken> expected = new ArrayList<>();
        expected.add(PATH_SEP);
        expected.addAll(literalsForString("foo.txt"));

        assertTokenListEquals(expected, exp.getTokens());
        assertFalse(exp.getPathSegment(0).isEmpty());
        assertEquals(literalsForString("foo.txt"), exp.getPathSegment(0));
    }
}