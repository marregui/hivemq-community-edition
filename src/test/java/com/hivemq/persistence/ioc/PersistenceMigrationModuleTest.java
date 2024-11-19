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
package com.hivemq.persistence.ioc;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import com.hivemq.bootstrap.lazysingleton.LazySingletonModule;
import com.hivemq.config.SysInfo;
import com.hivemq.config.MqttConfigService;
import com.hivemq.persistence.PersistenceStartup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertSame;

/**
 * @author Florian Limpöck
 * @since 4.1.0
 */
public class PersistenceMigrationModuleTest {

    @Mock
    private SysInfo sysInfo;

    @Mock
    private MqttConfigService mqttConfigService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_startup_singleton() {
        final Injector injector =
                Guice.createInjector(new PersistenceMigrationModule(new MetricRegistry()), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(SysInfo.class).toInstance(sysInfo);
                        bindScope(LazySingleton.class, LazySingletonModule.SCOPE);
                        bind(MqttConfigService.class).toInstance(mqttConfigService);
                    }
                });

        final PersistenceStartup instance1 = injector.getInstance(PersistenceStartup.class);
        final PersistenceStartup instance2 = injector.getInstance(PersistenceStartup.class);

        assertSame(instance1, instance2);
    }
}
