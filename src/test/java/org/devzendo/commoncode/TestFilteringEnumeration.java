package org.devzendo.commoncode;

import org.devzendo.commoncode.collection.FilteringEnumeration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import static java.util.Collections.enumeration;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TestFilteringEnumeration {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final Enumeration<Integer> oneToFour = enumeration(range(1, 5).boxed().collect(toList()));

    private final Enumeration<Integer> empty = enumeration(Collections.emptyList());

    private final class AlwaysFilteringEnumeration<Integer> extends FilteringEnumeration<Integer> {
        public AlwaysFilteringEnumeration(final Enumeration<Integer> delegate) {
            super(delegate);
        }

        @Override
        public boolean test(final Integer integer) {
            return true;
        }
    }

    @Test
    public void oneToFourEnumerates() {
        assertThat(list(oneToFour)).containsExactly(1, 2, 3, 4);
    }

    @Test
    public void filteringEnumerationFilters() {
        final FilteringEnumeration<Integer> evenFilter = new FilteringEnumeration<Integer>(oneToFour) {
            @Override
            public boolean test(final Integer integer) {
                return integer % 2 == 0;
            }
        };
        assertThat(list(evenFilter)).containsExactly(2, 4);
    }

    @Test
    public void throwsIfYouAskForNextWhenYouKnowThereIsNone() {
        final AlwaysFilteringEnumeration always = new AlwaysFilteringEnumeration(empty);

        thrown.expect(NoSuchElementException.class);
        assertThat(always.hasMoreElements()).isFalse();

        always.nextElement();
    }

    @Test
    public void behavesLikeEnumeration() {
        final AlwaysFilteringEnumeration always = new AlwaysFilteringEnumeration(oneToFour);

        assertThat(always.hasMoreElements()).isTrue();
        assertThat(always.nextElement()).isEqualTo(1);
        assertThat(always.hasMoreElements()).isTrue();
        assertThat(always.nextElement()).isEqualTo(2);
        assertThat(always.hasMoreElements()).isTrue();
        assertThat(always.nextElement()).isEqualTo(3);
        assertThat(always.hasMoreElements()).isTrue();
        assertThat(always.nextElement()).isEqualTo(4);
        assertThat(always.hasMoreElements()).isFalse();
    }
}
