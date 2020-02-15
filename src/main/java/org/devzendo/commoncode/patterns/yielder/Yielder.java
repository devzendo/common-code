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

package org.devzendo.commoncode.patterns.yielder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Yielder<T> {
    private SynchronousQueue<T> queue = new SynchronousQueue<T>();
    private AtomicBoolean hasNext = new AtomicBoolean(true);

    public void yield(T t) {
        try {
            queue.put(t);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void finishedYielding() {
        hasNext.set(false);
    }

    private T take() {
        if (!hasNext.get()) {
            throw new NoSuchElementException();
        }
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return hasNext.get();
            }

            @Override
            public T next() {
                if (!hasNext.get()) {
                    throw new NoSuchElementException();
                }
                return take();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove from a Yielder's Iterator");
            }
        };
    }

    /**
     * Start yielding by calling generate() in a new Thread.
     */
    public void start() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    generate();
                } finally {
                    finishedYielding();
                }
            }
        });
        thread.setName("Yielder " + this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Implement this, and call yield(T t) with your items, then finish.
     */
    public abstract void generate();
}
