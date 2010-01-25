package org.devzendo.commoncode.patterns.observer;

import org.apache.log4j.Logger;
import org.devzendo.commoncode.CCTestCase;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the generic observer list
 *
 * @author matt
 *
 */
public final class TestObserverList extends CCTestCase {
    private static final Logger LOGGER = Logger.getLogger(TestObserverList.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Empty lists have no listeners
     */
    @Test
    public void empty() {
        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        Assert.assertEquals(0, list.getNumberOfObservers());
    }

    /**
     * A list with one observer
     */
    @SuppressWarnings("unchecked")
    @Test
    public void add() {
        final Observer<ObservableEvent> obs = EasyMock.createMock(Observer.class);
        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        list.addObserver(obs);
        Assert.assertEquals(1, list.getNumberOfObservers());
    }

    /**
     * Removing a observer makes the count go down
     */
    @SuppressWarnings("unchecked")
    @Test
    public void remove() {
        final Observer<ObservableEvent> obs = EasyMock.createMock(Observer.class);
        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        list.addObserver(obs);
        Assert.assertEquals(1, list.getNumberOfObservers());
        list.removeListener(obs);
        Assert.assertEquals(0, list.getNumberOfObservers());
    }

    /**
     * Test whether a observer is attached, and whether an unattached one
     * isn't.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void exists() {
        final Observer<ObservableEvent> obs = EasyMock.createMock(Observer.class);
        final Observer<ObservableEvent> obs2 = EasyMock.createMock(Observer.class);
        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        Assert.assertFalse(list.isObserverAttached(obs));
        Assert.assertFalse(list.isObserverAttached(obs2));
        list.addObserver(obs);
        Assert.assertTrue(list.isObserverAttached(obs));
        Assert.assertFalse(list.isObserverAttached(obs2));
        list.removeListener(obs);
        Assert.assertFalse(list.isObserverAttached(obs));
        Assert.assertFalse(list.isObserverAttached(obs2));
    }

    /**
     * Dispatch a marker event, detect propagation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void dispatch() {
        final ObservableEvent oe = EasyMock.createMock(ObservableEvent.class);

        final Observer<ObservableEvent> observer = EasyMock.createMock(Observer.class);
        observer.eventOccurred(oe);
        EasyMock.replay(observer);

        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        list.addObserver(observer);

        list.eventOccurred(oe);

        EasyMock.verify(observer);
    }

    /**
     * After removing a observer, no more events are dispatched to it
     */
    @SuppressWarnings("unchecked")
    @Test
    public void removeDoesntDispatchToRemoved() {
        final ObservableEvent oe = EasyMock.createMock(ObservableEvent.class);

        final Observer<ObservableEvent> observer = EasyMock.createMock(Observer.class);
        observer.eventOccurred(oe);
        EasyMock.replay(observer);

        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        list.addObserver(observer);

        list.eventOccurred(oe);

        list.removeListener(observer);
        list.eventOccurred(oe);

        EasyMock.verify(observer);
    }

    /**
     * Dispatch a marker event, detect propagation to multiple listeners
     */
    @SuppressWarnings("unchecked")
    @Test
    public void multipleListenerDispatch() {
        final ObservableEvent oe = EasyMock.createMock(ObservableEvent.class);

        final Observer<ObservableEvent> listener1 = EasyMock.createMock(Observer.class);
        listener1.eventOccurred(oe);
        final Observer<ObservableEvent> listener2 = EasyMock.createMock(Observer.class);
        listener2.eventOccurred(oe);
        EasyMock.replay(listener1, listener2);

        final ObserverList<ObservableEvent> list = new ObserverList<ObservableEvent>();
        list.addObserver(listener1);
        list.addObserver(listener2);
        Assert.assertEquals(2, list.getNumberOfObservers());

        list.eventOccurred(oe);

        EasyMock.verify(listener1, listener2);
    }

    /**
     * Dispatch a subclass of ObservableEvent, detect propagation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void dispatchSubInterface() {
        final StringEvent se1 = new StringEvent("Event1");

        final Observer<StringEvent> observer = EasyMock.createMock(Observer.class);
        observer.eventOccurred(se1);
        EasyMock.replay(observer);

        final ObserverList<StringEvent> list = new ObserverList<StringEvent>();
        list.addObserver(observer);

        list.eventOccurred(se1);

        EasyMock.verify(observer);
    }

    private class StringEvent implements ObservableEvent {
        private final String eventData;

        public StringEvent(final String data) {
            this.eventData = data;
        }

        @SuppressWarnings("unused")
        public String getEventData() {
            return eventData;
        }
    }
}
