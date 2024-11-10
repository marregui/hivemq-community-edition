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
package com.hivemq.persistence;

import com.google.common.util.concurrent.SettableFuture;
import com.hivemq.configuration.service.InternalConfigurations;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Lukas Brandl
 */
public class SingleWriterServiceTest {

    SingleWriterService SingleWriterService;

    @Before
    public void setUp() throws Exception {
        InternalConfigurations.SINGLE_WRITER_THREAD_POOL_SIZE.set(4);
        InternalConfigurations.SINGLE_WRITER_CREDITS_PER_EXECUTION.set(200);
        InternalConfigurations.PERSISTENCE_SHUTDOWN_GRACE_PERIOD_MSEC.set(200);
        InternalConfigurations.PERSISTENCE_BUCKET_COUNT.set(64);

        SingleWriterService = new SingleWriterService();
    }

    @After
    public void tearDown() throws Exception {
        SingleWriterService.stop();
    }

    @Test
    public void test_increment_non_empty_queue_count() throws Exception {
        SingleWriterService.singleWriterExecutor = new NoOpExecutor();

        assertEquals(0, SingleWriterService.getNonemptyQueueCounter().get());
        assertEquals(0, SingleWriterService.getRunningThreadsCount().get());

        SingleWriterService.incrementNonemptyQueueCounter();
        assertEquals(1, SingleWriterService.getNonemptyQueueCounter().get());
        assertEquals(1, SingleWriterService.getRunningThreadsCount().get());

        SingleWriterService.incrementNonemptyQueueCounter();
        assertEquals(2, SingleWriterService.getNonemptyQueueCounter().get());
        assertEquals(2, SingleWriterService.getRunningThreadsCount().get());

        SingleWriterService.incrementNonemptyQueueCounter();
        assertEquals(3, SingleWriterService.getNonemptyQueueCounter().get());
        assertEquals(3, SingleWriterService.getRunningThreadsCount().get());

        SingleWriterService.incrementNonemptyQueueCounter();
        assertEquals(4, SingleWriterService.getNonemptyQueueCounter().get());
        assertEquals(4, SingleWriterService.getRunningThreadsCount().get());

        SingleWriterService.incrementNonemptyQueueCounter();
        assertEquals(5, SingleWriterService.getNonemptyQueueCounter().get());
        assertEquals(4, SingleWriterService.getRunningThreadsCount().get());
    }

    @Test
    public void test_valid_amount_of_queues() throws Exception {


        assertEquals(1, SingleWriterService.validAmountOfQueues(1, 64));
        assertEquals(2, SingleWriterService.validAmountOfQueues(2, 64));
        assertEquals(4, SingleWriterService.validAmountOfQueues(4, 64));
        assertEquals(8, SingleWriterService.validAmountOfQueues(5, 64));
        assertEquals(8, SingleWriterService.validAmountOfQueues(8, 64));
        assertEquals(64, SingleWriterService.validAmountOfQueues(64, 64));
    }

    @Test
    public void stop_shutdownAllThreads() {
        SingleWriterService.stop();
        assertTrue(SingleWriterService.checkScheduler.isShutdown());
        assertTrue(SingleWriterService.singleWriterExecutor.isShutdown());

        for (final ExecutorService callbackExecutor : SingleWriterService.callbackExecutors) {
            assertTrue(callbackExecutor.isShutdown());
        }
    }

    private static class NoOpExecutor implements ExecutorService {

        @Override
        public void shutdown() {
        }

        @NotNull
        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(final long timeout, @NotNull final TimeUnit unit) throws InterruptedException {
            return false;
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull final Callable<T> task) {
            return null;
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull final Runnable task, final T result) {
            return null;
        }

        @NotNull
        @Override
        public Future<?> submit(@NotNull final Runnable task) {
            return SettableFuture.create();
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull final Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            return null;
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(
                @NotNull final Collection<? extends Callable<T>> tasks,
                final long timeout,
                @NotNull final TimeUnit unit) throws InterruptedException {
            return null;
        }

        @NotNull
        @Override
        public <T> T invokeAny(@NotNull final Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public <T> T invokeAny(
                @NotNull final Collection<? extends Callable<T>> tasks,
                final long timeout,
                @NotNull final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public void execute(@NotNull final Runnable command) {

        }
    }
}
