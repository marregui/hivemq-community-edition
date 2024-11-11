package com.hivemq.persistence;

import com.hivemq.util.Exceptions;
import org.jetbrains.annotations.NotNull;

import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SingleWriterTask implements Runnable {

    private static final int MIN_PROBABILITY_IN_PERCENT = 5;
    private static final @NotNull SplittableRandom RANDOM = new SplittableRandom();

    private final int @NotNull [] probabilities;
    private final @NotNull AtomicLong nonemptyQueueCounter;
    private final @NotNull AtomicLong globalTaskCount;
    private final @NotNull AtomicInteger runningThreadsCount;
    private final ProducerQueues @NotNull [] producers;

    public SingleWriterTask(
            final @NotNull AtomicLong nonemptyQueueCounter,
            final @NotNull AtomicLong globalTaskCount,
            final @NotNull AtomicInteger runningThreadsCount,
            final ProducerQueues @NotNull [] producers) {

        this.nonemptyQueueCounter = nonemptyQueueCounter;
        this.globalTaskCount = globalTaskCount;
        this.runningThreadsCount = runningThreadsCount;
        this.producers = producers;
        probabilities = new int[producers.length];
    }

    @Override
    public void run() {
        try {
            final SplittableRandom random = RANDOM.split();
            // It is possible that all tasks stop running while there are still non-empty queues.
            // We have yet to determine if there is a lock free way to avoid this.
            outerLoop:
            while (nonemptyQueueCounter.get() >= runningThreadsCount.getAndDecrement()) {
                runningThreadsCount.incrementAndGet();
                final long countSnapShot = globalTaskCount.get();
                if (countSnapShot == 0) {
                    continue;
                }
                // Calculate the percentage portion of total tasks per persistence.
                for (int i = 0; i < producers.length; i++) {
                    probabilities[i] = (int) ((producers[i].getTaskCount().get() * 100) / countSnapShot);
                }
                int sumWithoutMins = 0;
                // Set to min probability if necessary
                for (int i = 0; i < probabilities.length; i++) {
                    if (probabilities[i] < MIN_PROBABILITY_IN_PERCENT) {
                        probabilities[i] = MIN_PROBABILITY_IN_PERCENT;
                    } else {
                        sumWithoutMins += probabilities[i];
                    }
                }
                int surplus = 0;
                for (int i = 0; i < probabilities.length; i++) {
                    surplus += probabilities[i];
                }
                surplus -= 100;
                if (surplus > 0) { // Normalize to a 100% sum
                    // We reduce the probability of all persistences that are not at the minimum, be a portion of the overhead.
                    // The portion is based on there portion of the sum of all probabilities, ignoring those with minimum probability.
                    for (int i = 0; i < probabilities.length; i++) {
                        if (probabilities[i] > MIN_PROBABILITY_IN_PERCENT) {
                            probabilities[i] -= surplus / (sumWithoutMins / probabilities[i]);
                        }
                    }
                }
                final int randomInt = random.nextInt(100);
                int offset = 0;
                for (int i = 0; i < probabilities.length; i++) {
                    if (randomInt <= probabilities[i] + offset) {
                        producers[i].execute(random);
                        continue outerLoop;
                    }
                    offset += probabilities[i];
                }
            }
        } catch (final Throwable t) {
            // Exceptions in the executed tasks, are passed to there result future.
            // So we only end up here if there is an exception in the probability calculation.
            // We decrement the running thread count so that a new thread will start running, as soon as a new task is added to any queue.
            runningThreadsCount.decrementAndGet();
            Exceptions.rethrowError("Exception in single writer executor. ", t);
        }
    }
}
