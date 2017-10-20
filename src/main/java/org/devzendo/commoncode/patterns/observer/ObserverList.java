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


package org.devzendo.commoncode.patterns.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic observer list. See GoF Observer pattern.
 * 
 * Allows some subject to maintain a list of observers
 * for observable events that it may produce. When the event occurs it is
 * dispatched to all registered observers.
 * 
 * @author matt
 *
 * @param <T> the type of ObservableEvent to register an interest in
 */
public final class ObserverList<T extends ObservableEvent> {
    private List<Observer<T>> observerList = new ArrayList<Observer<T>>();
    
    /**
     * How many observers are attached to this list?
     * @return the number of observers attached to this observer list.
     */
    public int getNumberOfObservers() {
        synchronized (observerList) {
            return observerList.size();
        }
    }

    /**
     * Add a new observer to the list.
     * @param observer an observer of the correct type to add to the observer
     * list.
     */
    public void addObserver(final Observer<T> observer) {
        synchronized (observerList) {
            observerList.add(observer);
        }
    }

    /**
     * Remove an observer from the list.
     * @param observer the observer of the correct type to remove from the
     * observer list. 
     */
    public void removeListener(final Observer<T> observer) {
        synchronized (observerList) {
            observerList.remove(observer);
        }
    }

    /**
     * Dispatch an event that has occurred to all registered observers.
     * @param observableEvent the observable event to dispatch.
     */
    public void eventOccurred(final T observableEvent) {
        List<Observer<T>> clone = null; 
        synchronized (observerList) {
            clone = new ArrayList<Observer<T>>();
            clone.addAll(observerList);
        }
        for (Observer<T> listener : clone) {
            listener.eventOccurred(observableEvent);
        }
    }

    /**
     * Is a given observer attached to the list?
     * @param observer an observer to test for attachment
     * @return true if attached, else false
     */
    public boolean isObserverAttached(final Observer<ObservableEvent> observer) {
        synchronized (observerList) {
            return observerList.contains(observer);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        synchronized (observerList) {
            for (Observer<T> observer : observerList) {
                sb.append(observer.getClass().getSimpleName());
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
