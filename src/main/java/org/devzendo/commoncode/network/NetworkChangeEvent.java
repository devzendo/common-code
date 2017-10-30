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

package org.devzendo.commoncode.network;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.devzendo.commoncode.patterns.observer.ObservableEvent;

import java.net.NetworkInterface;

/**
 * A NetworkChangeEvent describes a change in a NetworkInterface. Along with the NetworkInterface in question, the
 * event describes whether the NetworkInterface was added, removed or just changed (up/down state or address), and
 * also the current up/down state of the interface.
 */
public class NetworkChangeEvent implements ObservableEvent {
    private final NetworkInterface networkInterface;

    private final String networkInterfaceName;
    private final NetworkChangeType changeType;
    private final NetworkStateType stateType;

    /**
     * What has changed in the NetworkInterface?
     */
    public enum NetworkChangeType {
        /**
         * The NetworkInterface was added to the Enumeration provided by the NetworkInterfaceSupplier since last poll.
         */
        INTERFACE_ADDED,

        /**
         * The NetworkInterface was removed from the Enumeration provided by the NetworkInterfaceSupplier since last
         * poll. In this case, although you will be notified of the NetworkInterface that has been removed, the monitor
         * does not know its current state (since it has been removed) - the NetworkInterface you receive in this event
         * is a copy of the one seen on the previous poll.
         */
        INTERFACE_REMOVED,

        /**
         * The NetworkInterface has changed its state from INTERFACE_UP to INTERFACE_DOWN (or INTERFACE_UNKNOWN_STATE).
         */
        INTERFACE_STATE_CHANGED
    }

    /**
     * What is the current state of the NetworkInterface?
     */
    public enum NetworkStateType {
        /**
         * The NetworkInterface is now up.
         */
        INTERFACE_UP,

        /**
         * The NetworkInterface is now down.
         */
        INTERFACE_DOWN,

        /**
         * It has not been possible to detect the state of the interface (an exception was thrown when querying it).
         */
        INTERFACE_UNKNOWN_STATE
    }

    public NetworkChangeEvent(final NetworkInterface networkInterface, final String networkInterfaceName, final NetworkChangeType changeType, final NetworkStateType stateType) {
        this.networkInterface = networkInterface;
        this.networkInterfaceName = networkInterfaceName;
        this.changeType = changeType;
        this.stateType = stateType;
    }

    @Override
    public String toString() {
        return networkInterfaceName + ": " + changeType + " / " + stateType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).
                append(networkInterface).
                append(networkInterfaceName).
                append(changeType).
                append(stateType).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final NetworkChangeEvent other = (NetworkChangeEvent) obj;

        return new EqualsBuilder().
                append(this.networkInterface, other.networkInterface).
                append(this.networkInterfaceName, other.networkInterfaceName).
                append(this.changeType, other.changeType).
                append(this.stateType, other.stateType).
                isEquals();
    }

    /**
     * Which NetworkInterface does this event concern? Please note that if the event is an INTERFACE_REMOVED, this
     * NetworkInterface object refers to the instance retrieved in the previous poll, not this one. (Since in this
     * poll, it no longer exists).
     * @return the NetworkInterface that has changed its state.
     */
    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    /**
     * What is the name of the NetworkInterface that this event concerns?
     * @return the name of the NetworkInterface.
     */
    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    /**
     * How has this NetworkInterface changed?
     * @return the change type enumeration describing this change.
     */
    public NetworkChangeType getChangeType() {
        return changeType;
    }

    /**
     * What is the state of this NetworkInterface?
     * @return the current state, if it was possible to query it.
     */
    public NetworkStateType getStateType() {
        return stateType;
    }
}
