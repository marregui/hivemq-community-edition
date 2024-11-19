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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hivemq.bootstrap.lazysingleton.LazySingleton;
import com.hivemq.config.InternalConfig;
import org.jetbrains.annotations.NotNull;
import com.hivemq.persistence.local.xodus.bucket.Bucket;
import com.hivemq.util.ThreadFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.hivemq.config.InternalConfig.SINGLE_WRITER_INTERVAL_TO_CHECK_PENDING_TASKS_AND_SCHEDULE_MSEC;

@LazySingleton
public class SingleWriterService {

    private static final @NotNull Logger log = LoggerFactory.getLogger(SingleWriterService.class);
    private static final int AMOUNT_OF_PRODUCERS = 5;
    private static final int RETAINED_MESSAGE_QUEUE_INDEX = 0;
    private static final int CLIENT_SESSION_QUEUE_INDEX = 1;
    private static final int SUBSCRIPTION_QUEUE_INDEX = 2;
    private static final int QUEUED_MESSAGES_QUEUE_INDEX = 3;
    private static final int ATTRIBUTE_STORE_QUEUE_INDEX = 4;
    @VisibleForTesting
    public final @NotNull ExecutorService @NotNull [] callbackExecutors;
    @VisibleForTesting
    final @NotNull ScheduledExecutorService checkScheduler;
    private final int persistenceBucketCount;
    private final int threadPoolSize;
    private final int creditsPerExecution;
    private final long shutdownGracePeriod;
    private final @NotNull AtomicLong nonemptyQueueCounter = new AtomicLong(0);
    private final @NotNull AtomicInteger runningThreadsCount = new AtomicInteger(0);
    private final @NotNull AtomicLong globalTaskCount = new AtomicLong(0);
    private final @NotNull ProducerQueues @NotNull [] producers = new ProducerQueues[AMOUNT_OF_PRODUCERS];
    private final int amountOfQueues;
    @VisibleForTesting
    @NotNull ExecutorService singleWriterExecutor;

    @Inject
    public SingleWriterService() {
        persistenceBucketCount = InternalConfig.PERSISTENCE_BUCKET_COUNT.get();
        threadPoolSize = InternalConfig.SINGLE_WRITER_THREAD_POOL_SIZE.get();
        creditsPerExecution = InternalConfig.SINGLE_WRITER_CREDITS_PER_EXECUTION.get();
        shutdownGracePeriod = InternalConfig.PERSISTENCE_SHUTDOWN_GRACE_PERIOD_MSEC.get();

        final ThreadFactory threadFactory = ThreadFactoryUtil.create("single-writer-%d");
        singleWriterExecutor = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
        amountOfQueues = validAmountOfQueues(threadPoolSize, persistenceBucketCount);
        for (int i = 0; i < producers.length; i++) {
            producers[i] = new ProducerQueues(this, amountOfQueues);
        }
        callbackExecutors = new ExecutorService[amountOfQueues];
        for (int i = 0; i < amountOfQueues; i++) {
            final ThreadFactory callbackThreadFactory = ThreadFactoryUtil.create("single-writer-callback-" + i);
            final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(callbackThreadFactory);
            callbackExecutors[i] = executorService;
        }
        final ThreadFactory checkThreadFactory =
                new ThreadFactoryBuilder().setNameFormat("single-writer-scheduled-check-%d").build();
        checkScheduler = Executors.newSingleThreadScheduledExecutor(checkThreadFactory);
    }

    @PostConstruct
    public void postConstruct() {
        // Periodically check if there are pending tasks in the queues
        checkScheduler.scheduleAtFixedRate(() -> {
                    try {

                        if (runningThreadsCount.getAndIncrement() == 0 && !singleWriterExecutor.isShutdown()) {
                            singleWriterExecutor.submit(new SingleWriterTask(nonemptyQueueCounter,
                                    globalTaskCount,
                                    runningThreadsCount,
                                    producers));
                        } else {
                            runningThreadsCount.decrementAndGet();
                        }
                    } catch (final Exception e) {
                        log.error("Exception in single writer check task ", e);
                    }
                },
                SINGLE_WRITER_INTERVAL_TO_CHECK_PENDING_TASKS_AND_SCHEDULE_MSEC.get(),
                SINGLE_WRITER_INTERVAL_TO_CHECK_PENDING_TASKS_AND_SCHEDULE_MSEC.get(),
                TimeUnit.MILLISECONDS);
    }

    @VisibleForTesting
    int validAmountOfQueues(final int processorCount, final int bucketCount) {
        for (int i = processorCount; i < bucketCount; i++) {
            if (bucketCount % i == 0) {
                return i;
            }
        }
        return persistenceBucketCount;
    }

    void incrementNonemptyQueueCounter() {
        nonemptyQueueCounter.incrementAndGet();
        if (runningThreadsCount.getAndIncrement() < threadPoolSize) {
            singleWriterExecutor.submit(new SingleWriterTask(nonemptyQueueCounter,
                    globalTaskCount,
                    runningThreadsCount,
                    producers));
        } else {
            runningThreadsCount.decrementAndGet();
        }
    }

    @NotNull
    public ExecutorService callbackExecutor(@NotNull final String key) {
        final int bucketsPerQueue = persistenceBucketCount / amountOfQueues;
        final int bucketIndex = Bucket.getBucket(key, persistenceBucketCount);
        final int queueIndex = bucketIndex / bucketsPerQueue;
        return callbackExecutors[queueIndex];
    }

    public void decrementNonemptyQueueCounter() {
        nonemptyQueueCounter.decrementAndGet();
    }

    public @NotNull ProducerQueues getRetainedMessageQueue() {
        return producers[RETAINED_MESSAGE_QUEUE_INDEX];
    }

    public @NotNull ProducerQueues getClientSessionQueue() {
        return producers[CLIENT_SESSION_QUEUE_INDEX];
    }

    public @NotNull ProducerQueues getSubscriptionQueue() {
        return producers[SUBSCRIPTION_QUEUE_INDEX];
    }

    public @NotNull ProducerQueues getQueuedMessagesQueue() {
        return producers[QUEUED_MESSAGES_QUEUE_INDEX];
    }

    public @NotNull ProducerQueues getAttributeStoreQueue() {
        return producers[ATTRIBUTE_STORE_QUEUE_INDEX];
    }

    public int getPersistenceBucketCount() {
        return persistenceBucketCount;
    }

    public int getCreditsPerExecution() {
        return creditsPerExecution;
    }

    public long getShutdownGracePeriod() {
        return shutdownGracePeriod;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public @NotNull AtomicLong getGlobalTaskCount() {
        return globalTaskCount;
    }

    public @NotNull AtomicLong getNonemptyQueueCounter() {
        return nonemptyQueueCounter;
    }

    public @NotNull AtomicInteger getRunningThreadsCount() {
        return runningThreadsCount;
    }

    public @NotNull ExecutorService @NotNull [] getCallbackExecutors() {
        return callbackExecutors;
    }

    public void stop() {
        final long start = System.currentTimeMillis();
        if (log.isTraceEnabled()) {
            log.trace("Shutting down single writer");
        }

        singleWriterExecutor.shutdown();

        try {
            singleWriterExecutor.awaitTermination(shutdownGracePeriod, TimeUnit.SECONDS);
            if (log.isTraceEnabled()) {
                log.trace("Finished single writer shutdown in {} ms", (System.currentTimeMillis() - start));
            }
        } catch (final InterruptedException e) {
            //ignore
        }
        singleWriterExecutor.shutdownNow();
        for (final ExecutorService callbackExecutor : callbackExecutors) {
            callbackExecutor.shutdownNow();
        }
        checkScheduler.shutdownNow();
    }
}
