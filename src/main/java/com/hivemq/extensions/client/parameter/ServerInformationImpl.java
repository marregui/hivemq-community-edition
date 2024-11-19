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
package com.hivemq.extensions.client.parameter;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import com.hivemq.config.SysInfo;
import com.hivemq.config.ListenerConfigService;
import org.jetbrains.annotations.NotNull;
import com.hivemq.extension.sdk.api.client.parameter.Listener;
import com.hivemq.extension.sdk.api.client.parameter.ServerInformation;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Set;

@Singleton
public class ServerInformationImpl implements ServerInformation {

    @NotNull
    private final ListenerConfigService listenerConfigService;

    @Inject
    public ServerInformationImpl(
            @NotNull final ListenerConfigService listenerConfigService) {
        this.listenerConfigService = listenerConfigService;
    }

    @NotNull
    @Override
    public String getVersion() {
        return SysInfo.VERSION;
    }

    @NotNull
    @Override
    public File getHomeFolder() {
        return SysInfo.INSTANCE.getHiveMQHomeFolder();
    }

    @NotNull
    @Override
    public File getDataFolder() {
        return SysInfo.INSTANCE.getDataFolder();
    }

    @NotNull
    @Override
    public File getLogFolder() {
        return SysInfo.INSTANCE.getLogFolder();
    }

    @NotNull
    @Override
    public File getExtensionsFolder() {
        return SysInfo.INSTANCE.getExtensionsFolder();
    }

    @NotNull
    @Override
    public Set<Listener> getListener() {
        final List<com.hivemq.config.entity.Listener> listeners =
                listenerConfigService.getListeners();
        final ImmutableSet.Builder<Listener> builder = ImmutableSet.builder();
        for (final com.hivemq.config.entity.Listener listener : listeners) {
            builder.add(new ListenerImpl(listener));
        }
        return builder.build();
    }

}
