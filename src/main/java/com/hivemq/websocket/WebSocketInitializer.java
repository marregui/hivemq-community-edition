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
package com.hivemq.websocket;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.hivemq.configuration.service.entity.WebsocketListener;
import org.jetbrains.annotations.NotNull;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import static com.hivemq.bootstrap.netty.ChannelHandlerNames.HTTP_OBJECT_AGGREGATOR;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.HTTP_SERVER_CODEC;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.MQTT_WEBSOCKET_ENCODER;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.WEBSOCKET_BINARY_FRAME_HANDLER;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.WEBSOCKET_CONTINUATION_FRAME_HANDLER;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.WEBSOCKET_SERVER_PROTOCOL_HANDLER;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.WEBSOCKET_TEXT_FRAME_HANDLER;

public class WebSocketInitializer {

    public static final int WEBSOCKET_MAX_CONTENT_LENGTH = 65536;

    private final @NotNull WebsocketListener websocketListener;

    public WebSocketInitializer(final @NotNull WebsocketListener websocketListener) {
        this.websocketListener = websocketListener;
    }

    public void addHandlers(final Channel ch, final @NotNull String handlerBefore) {
        ch.pipeline()
                .addAfter(handlerBefore, HTTP_SERVER_CODEC, new HttpServerCodec())
                .addAfter(HTTP_SERVER_CODEC,
                        HTTP_OBJECT_AGGREGATOR,
                        new HttpObjectAggregator(WEBSOCKET_MAX_CONTENT_LENGTH))
                .addAfter(HTTP_OBJECT_AGGREGATOR,
                        WEBSOCKET_SERVER_PROTOCOL_HANDLER,
                        new WebSocketServerProtocolHandler(websocketListener.getPath(),
                                getSubprotocolString(),
                                websocketListener.getAllowExtensions(),
                                Integer.MAX_VALUE))
                .addAfter(WEBSOCKET_SERVER_PROTOCOL_HANDLER,
                        WEBSOCKET_BINARY_FRAME_HANDLER,
                        new WebSocketBinaryFrameHandler())
                .addAfter(WEBSOCKET_BINARY_FRAME_HANDLER,
                        WEBSOCKET_CONTINUATION_FRAME_HANDLER,
                        new WebSocketContinuationFrameHandler())
                .addAfter(WEBSOCKET_BINARY_FRAME_HANDLER, WEBSOCKET_TEXT_FRAME_HANDLER, new WebSocketTextFrameHandler())
                .addAfter(WEBSOCKET_TEXT_FRAME_HANDLER, MQTT_WEBSOCKET_ENCODER, new MQTTWebsocketEncoder());
    }

    @VisibleForTesting
    @NotNull String getSubprotocolString() {
        return Joiner.on(",").join(websocketListener.getSubprotocols());
    }
}
