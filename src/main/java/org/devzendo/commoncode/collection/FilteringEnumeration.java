package org.devzendo.commoncode.collection;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

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
public abstract class FilteringEnumeration<E> implements Enumeration<E>, Predicate<E> {

    private final Enumeration<E> delegate;

    // This is set by the findNext method..
    private E nextElement = null;

    public FilteringEnumeration(final Enumeration<E> delegate) {
        this.delegate = delegate;
    }

    // Finds the next element in the delegate enumeration which passes the predicate, and
    // stores in nextElement.
    private void findNext() {
        if (nextElement != null)
            return;

        while (delegate.hasMoreElements()) {
            final E element = delegate.nextElement();
            if (test(element)) {
                nextElement = element;
                break;
            }
        }
    }

    @Override
    public boolean hasMoreElements() {
        findNext();
        return nextElement != null;
    }

    @Override
    public E nextElement() {
        findNext();

        if (nextElement == null)
            throw new NoSuchElementException();

        final E result = nextElement;
        nextElement = null;
        return result;
    }
}
