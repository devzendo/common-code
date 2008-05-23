package uk.me.gumbley.commoncode.patterns.observer;

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
}
