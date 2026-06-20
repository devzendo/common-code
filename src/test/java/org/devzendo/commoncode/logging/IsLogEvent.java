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

package org.devzendo.commoncode.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsLogEvent extends TypeSafeMatcher<LogEvent> {
    private final Level level;
    private final String message;

    public IsLogEvent(final Level level, final String message) {
        this.level = level;
        this.message = message;
    }

    @Override
    protected boolean matchesSafely(final LogEvent item) {
        boolean levelmatches = item.getLevel().equals(level);
        boolean messagematches = item.getMessage().getFormattedMessage().contentEquals(message);
        String expected = message;
        String actual = item.getMessage().getFormattedMessage();

        System.out.println("expected len=" + expected.length());
        System.out.println("actual   len=" + actual.length());
        for (int i = 0; i < Math.max(expected.length(), actual.length()); i++) {
            char e = i < expected.length() ? expected.charAt(i) : '\0';
            char a = i < actual.length() ? actual.charAt(i) : '\0';
            if (e != a) {
                System.out.printf("DIFF at %d: expected=U+%04X (%s) actual=U+%04X (%s)%n",
                        i, (int) e, e, (int) a, a);
            }
        }
        return levelmatches && messagematches;
    }

    @Override
    public void describeMismatchSafely(final LogEvent item, final Description mismatchDescription) {
        mismatchDescription.appendText("has level ")
                .appendValue(item.getLevel())
                .appendText(" and message '")
                .appendValue(item.getMessage().getFormattedMessage())
                .appendText("'");
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("logEvent(")
                .appendValue(level)
                .appendText(", '")
                .appendValue(message)
                .appendText("')");
    }

    @Factory
    public static Matcher<LogEvent> logEvent(final Level level, final String message) {
        return new IsLogEvent(level, message);
    }
}
