/*
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.collection;

import org.devzendo.commoncode.collection.Lists;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class TestLists {
    @Test
    public void joinNothing() {
        final List<Integer> j = Lists.<Integer>join();
        assertThat(j, hasSize(0));
    }

    @Test
    public void emptiness() {
        final List<Integer> e1 = Collections.emptyList();
        final List<Integer> e2 = Collections.emptyList();
        final List<Integer> j = Lists.<Integer>join(e1, e2);
        assertThat(j, hasSize(0));
    }

    @Test
    public void joinOne() {
        final List<Integer> one = Arrays.asList(1, 2, 3);
        final List<Integer> j = Lists.<Integer>join(one);
        assertThat(j, hasSize(3));
        assertThat(j, Matchers.contains(1, 2, 3));
    }

    @Test
    public void leftEmpty() {
        final List<Integer> empty = Collections.emptyList();
        final List<Integer> rhs = Arrays.asList(1, 2, 3);
        final List<Integer> j = Lists.<Integer>join(empty, rhs);
        assertThat(j, hasSize(3));
        assertThat(j, Matchers.contains(1, 2, 3));
    }

    @Test
    public void rightEmpty() {
        final List<Integer> lhs = Arrays.asList(1, 2, 3);
        final List<Integer> empty = Collections.emptyList();
        final List<Integer> j = Lists.<Integer>join(lhs, empty);
        assertThat(j, hasSize(3));
        assertThat(j, Matchers.contains(1, 2, 3));
    }

    @Test
    public void joinTwo() {
        final List<Integer> lhs = Arrays.asList(1, 2, 3);
        final List<Integer> rhs = Arrays.asList(4, 5, 6);
        final List<Integer> j = Lists.<Integer>join(lhs, rhs);
        assertThat(j, hasSize(6));
        assertThat(j, Matchers.contains(1, 2, 3, 4, 5, 6));
    }
}
