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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.NetworkInterface;

import static org.assertj.core.api.Assertions.assertThat;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.local;

public class TestNetworkChangeEvent {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    public static final String LOCAL = "local";

    private final NetworkInterface localUp = local(true);
    private final NetworkInterface localDown = local(false);
    private final NetworkChangeEvent localUpAdded = new NetworkChangeEvent(localUp, LOCAL, NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED, NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
    private final NetworkChangeEvent localDownAdded = new NetworkChangeEvent(localDown, LOCAL, NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED, NetworkChangeEvent.NetworkStateType.INTERFACE_DOWN);
    private final NetworkChangeEvent localUpRemoved = new NetworkChangeEvent(localUp, LOCAL, NetworkChangeEvent.NetworkChangeType.INTERFACE_REMOVED, NetworkChangeEvent.NetworkStateType.INTERFACE_UP);

    @Test
    public void stringForm() {
        assertThat(localUpAdded.toString()).isEqualTo("local: INTERFACE_ADDED / INTERFACE_UP");
    }

    @Test
    public void hashCodeConsidersUpDownState() {
        assertThat(localUpAdded.hashCode()).isNotEqualTo(localDownAdded.hashCode());
    }

    @Test
    public void hashCodeConsidersAddedRemovedState() {
        assertThat(localUpAdded.hashCode()).isNotEqualTo(localUpRemoved.hashCode());
    }

    @Test
    public void getters() {
        assertThat(localUpAdded.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
        assertThat(localUpAdded.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(localUpAdded.getNetworkInterfaceName()).isEqualTo(LOCAL);
        assertThat(localUpAdded.getNetworkInterface()).isEqualTo(localUp);
    }

    @Test
    public void simpleEqualityTests() {
        assertThat(localUpAdded.equals(localUpAdded)).isTrue();
        assertThat(localDownAdded.equals(localUpAdded)).isFalse();
        assertThat(localUpAdded.equals(null)).isFalse();
        assertThat(localUpAdded.equals("A hat")).isFalse();
    }
}
