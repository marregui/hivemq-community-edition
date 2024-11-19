/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.config;

import com.hivemq.config.entity.Listener;
import com.hivemq.config.entity.TcpListener;
import com.hivemq.config.entity.TlsTcpListener;
import com.hivemq.config.entity.TlsWebsocketListener;
import com.hivemq.config.entity.WebsocketListener;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static util.TlsTestUtil.createDefaultTLS;

public class ListenerConfigServiceImplTest {

    private ListenerConfigService listenerConfigService;

    @Before
    public void setUp() throws Exception {
        listenerConfigService = new ListenerConfigService();
    }

    /*
     * Adding listeners
     */
    @Test
    public void test_add_listeners() {

        final TcpListener tcpListener = new TcpListener(1883, "localhost");
        final WebsocketListener websocketListener =
                new WebsocketListener.Builder().port(1884).bindAddress("localhost").build();

        final TlsTcpListener tlsTcpListener = new TlsTcpListener(1885, "localhost", createDefaultTLS());

        final TlsWebsocketListener tlsWebsocketListener =
                new TlsWebsocketListener.Builder().port(1886).bindAddress("localhost").tls(createDefaultTLS()).build();

        listenerConfigService.addListener(tcpListener);
        listenerConfigService.addListener(websocketListener);
        listenerConfigService.addListener(tlsTcpListener);
        listenerConfigService.addListener(tlsWebsocketListener);

        final List<Listener> listeners = listenerConfigService.getListeners();

        assertEquals(4, listeners.size());

        assertEquals(1, listenerConfigService.getTcpListeners().size());
        assertEquals(1, listenerConfigService.getTlsTcpListeners().size());
        assertEquals(1, listenerConfigService.getWebsocketListeners().size());
        assertEquals(1, listenerConfigService.getTlsWebsocketListeners().size());

        assertSame(listenerConfigService.getTcpListeners().get(0), tcpListener);
        assertSame(listenerConfigService.getTlsTcpListeners().get(0), tlsTcpListener);
        assertSame(listenerConfigService.getWebsocketListeners().get(0), websocketListener);
        assertSame(listenerConfigService.getTlsWebsocketListeners().get(0), tlsWebsocketListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_invalid_listener_type() {

        listenerConfigService.addListener(new Listener() {
            @Override
            public int getPort() {
                return 0;
            }

            @Override
            public void setPort(final int port) {

            }

            @Override
            public @NotNull String getBindAddress() {
                return null;
            }

            @Override
            public @NotNull String readableName() {
                return null;
            }

            @Override
            public @NotNull String getName() {
                return "name";
            }

        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_invalid_listener_type_subclass_of_tcplistener() {

        listenerConfigService.addListener(new TcpListener(1883, "localhost") {
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_invalid_listener_type_subclass_of_tlstcplistener() {

        listenerConfigService.addListener(new TlsTcpListener(1883, "localhost", createDefaultTLS()) {
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_invalid_listener_type_subclass_of_websocketlistener() {

        final WebsocketListener subclass = new WebsocketListener(123, null, null, false, null, null) {
        };

        listenerConfigService.addListener(subclass);
    }

    @Test
    public void test_get_listeners_immutable() {

        listenerConfigService.addListener(new TcpListener(1883, "localhost"));

        final List<Listener> listeners = listenerConfigService.getListeners();

        try {
            listeners.add(new TcpListener(1884, "localhost"));
            fail();
        } catch (final Exception e) {
            //Expected
        }

        try {
            listeners.clear();
            fail();
        } catch (final Exception e) {
            //Expected
        }
    }
}
