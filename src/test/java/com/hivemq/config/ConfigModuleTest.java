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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.persistence.clientsession.SharedSubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.TestConfigurationBootstrap;

import static org.junit.Assert.assertSame;

@SuppressWarnings("deprecation")
public class ConfigModuleTest {

    @Mock
    SharedSubscriptionService sharedSubscriptionService;

    private Injector injector;
    private TestConfigurationBootstrap testConfigurationBootstrap;


    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        testConfigurationBootstrap = new TestConfigurationBootstrap();
        final ConfigService fullConfigService =
                testConfigurationBootstrap.getFullConfigurationService();

        injector = Guice.createInjector(new ConfigModule(fullConfigService),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(SharedSubscriptionService.class).toInstance(sharedSubscriptionService);
                    }
                });
    }

    @Test
    public void test_listener_configuration_service_singleton() throws Exception {

        final ListenerConfigService instance = injector.getInstance(ListenerConfigService.class);
        final ListenerConfigService instance2 = injector.getInstance(ListenerConfigService.class);

        assertSame(instance, instance2);
        assertSame(testConfigurationBootstrap.getListenerConfigurationService(), instance);
    }

    @Test
    public void test_mqtt_configuration_service_singleton() throws Exception {

        final MqttConfigService instance = injector.getInstance(MqttConfigService.class);
        final MqttConfigService instance2 = injector.getInstance(MqttConfigService.class);

        assertSame(instance, instance2);
        assertSame(testConfigurationBootstrap.getMqttConfigurationService(), instance);
    }

    @Test
    public void test_throttling_configuration_service_singleton() throws Exception {

        final RestrictionsConfigService instance = injector.getInstance(RestrictionsConfigService.class);
        final RestrictionsConfigService instance2 = injector.getInstance(RestrictionsConfigService.class);

        assertSame(instance, instance2);
        assertSame(testConfigurationBootstrap.getRestrictionsConfigurationService(), instance);
    }

    @Test
    public void test_configuration_service_singleton() throws Exception {

        final ConfigService instance = injector.getInstance(ConfigService.class);
        final ConfigService instance2 = injector.getInstance(ConfigService.class);

        assertSame(instance, instance2);
        assertSame(testConfigurationBootstrap.getConfigurationService(), instance);
    }

    @Test
    public void test_configuration_service_same_as_full_configuration_service() throws Exception {

        final ConfigService instance = injector.getInstance(ConfigService.class);
        final ConfigService instance2 = injector.getInstance(ConfigService.class);

        assertSame(instance, instance2);
        assertSame(testConfigurationBootstrap.getFullConfigurationService(), instance);
    }

    @Test
    public void test_configuration_service_bindings_same_as_direct_binding() throws Exception {

        final ConfigService configService = injector.getInstance(ConfigService.class);

        assertSame(
                configService.listenerConfiguration(),
                injector.getInstance(ListenerConfigService.class));
        assertSame(configService.mqttConfiguration(), injector.getInstance(MqttConfigService.class));
        assertSame(
                configService.restrictionsConfiguration(),
                injector.getInstance(RestrictionsConfigService.class));
    }
}
