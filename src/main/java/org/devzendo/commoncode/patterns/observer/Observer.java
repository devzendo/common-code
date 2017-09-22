/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
