package org.devzendo.commoncode.patterns.observer;

/**
 * Implemented by objects that want to be attached to an ObserverList and
 * notified of the occurrence of a specific observable event.
 * 
 * @author matt
 *
 * @param <O> the subtype of ObservableEvent that this observer is interested
 * in.
 */
public interface Observer<O extends ObservableEvent> {
    /**
     * An event has occurred and this is the notification.
     * @param observableEvent the event information.
     */
    void eventOccurred(O observableEvent);
}
