package org.devzendo.commoncode.network;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.devzendo.commoncode.patterns.observer.ObservableEvent;

import java.net.NetworkInterface;

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
public class NetworkChangeEvent implements ObservableEvent {
    private final NetworkInterface networkInterface;

    private final String networkInterfaceName;
    private final NetworkChangeType changeType;
    private final NetworkStateType stateType;

    public enum NetworkChangeType {
        INTERFACE_ADDED, INTERFACE_REMOVED, INTERFACE_STATE_CHANGED
    }

    public enum NetworkStateType {
        INTERFACE_UP, INTERFACE_DOWN, INTERFACE_UNKNOWN_STATE
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

    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    public NetworkChangeType getChangeType() {
        return changeType;
    }

    public NetworkStateType getStateType() {
        return stateType;
    }
}
