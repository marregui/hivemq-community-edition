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
package com.hivemq.topics.tree;


import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hivemq.topics.SubscriberWithQoS;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MatchingNodeSubscriptions {

    @Nullable SubscriberWithQoS @Nullable [] nonSharedSubscribersArray;
    @Nullable Map<String, SubscriberWithQoS> nonSharedSubscribers;
    @NotNull Map<String, SubscriptionGroup> sharedSubscribers;

    MatchingNodeSubscriptions() {
        sharedSubscribers = Map.of();
    }

    private static @NotNull String sharedSubscriptionKey(
            final @NotNull String sharedName, final @NotNull String topicFilter) {
        return sharedName + '/' + topicFilter;
    }

    private static boolean isEmptyArray(final @Nullable Object @Nullable [] array) {
        if (array == null) {
            return true;
        }
        for (final Object object : array) {
            if (object != null) {
                return false;
            }
        }
        return true;
    }

    private static int countArraySize(final @Nullable Object @Nullable [] array) {
        if (array == null) {
            return 0;
        }
        int count = 0;
        for (final Object object : array) {
            if (object != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Attempts to add the subscription information and updates the counters based on how the addition went and
     * what subscription information was stored previously.
     *
     * @param subscriberToAdd                subscription information that is considered for addition to the node of the
     *                                       topic tree.
     * @param topicFilter                    topic filter for the to-be-added subscription represented as a string;
     *                                       is not stored with subscription for memory saving reasons.
     * @param counters                       container with subscription counters that are updated upon subscription
     *                                       addition.
     * @param subscriberMapCreationThreshold a threshold to decide if the nonSharedSubscribersMap should be initialized
     *                                       instead of nonSharedSubscribersArray.
     * @return whether the subscription information was replaced with {@param subscriberToAdd}.
     */
    public boolean addSubscriber(
            final @NotNull SubscriberWithQoS subscriberToAdd,
            final @NotNull String topicFilter,
            final @NotNull SubscriptionCounters counters,
            final int subscriberMapCreationThreshold) {

        final SubscriptionInfoPresenceStatus subscriptionInfoPresenceStatus =
                storeSubscriberInStructures(subscriberToAdd, topicFilter, subscriberMapCreationThreshold);

        if (subscriptionInfoPresenceStatus == null) {
            counters.getSubscriptionCounter().inc();
        }

        return subscriptionInfoPresenceStatus != null;
    }

    /**
     * Attempts to remove the subscription information from the topic tree for the provided subscriber that is part of
     * the
     * shared subscribers group with the sharedName (if set) and subscribed to the topicFilter.
     *
     * @param subscriber  the client identifier of the subscriber whose subscription is to be removed.
     * @param sharedName  the name of the group that the shared subscriber belongs to (if set).
     * @param topicFilter topic filter for the to-be-removed subscription represented as a string.
     * @param counters    container with subscription counters that are updated upon subscription removal.
     */
    public void removeSubscriber(
            final @NotNull String subscriber,
            final @Nullable String sharedName,
            final @Nullable String topicFilter,
            final @NotNull SubscriptionCounters counters) {

        final SubscriptionInfoRemovalStatus subscriptionInfoRemovalStatus =
                removeSubscriberFromStructures(subscriber, sharedName, topicFilter);

        if (subscriptionInfoRemovalStatus != null) {
            counters.getSubscriptionCounter().dec();
        }
    }

    public void populateWithSubscriberNamesUsingFilter(
            final @NotNull Predicate<SubscriberWithQoS> itemFilter,
            final @NotNull ImmutableSet.Builder<String> subscribers) {

        populateUsingFilter(itemFilter, null, subscribers);
    }

    public void populateWithSubscribersUsingFilter(
            final @NotNull Predicate<SubscriberWithQoS> itemFilter,
            final @NotNull ImmutableSet.Builder<SubscriberWithQoS> subscribers) {

        populateUsingFilter(itemFilter, subscribers, null);
    }

    private void populateUsingFilter(
            final @NotNull Predicate<SubscriberWithQoS> itemFilter,
            final @Nullable ImmutableSet.Builder<SubscriberWithQoS> subscribersBuilder,
            final @Nullable ImmutableSet.Builder<String> subscriberNamesBuilder) {

        assert subscribersBuilder != null || subscriberNamesBuilder != null;

        getAllSubscriptionsStream().filter(itemFilter).forEach(subscriber -> {
            if (subscribersBuilder != null) {
                subscribersBuilder.add(subscriber);
            } else {
                subscriberNamesBuilder.add(subscriber.getSubscriber());
            }
        });
    }

    public int getSubscriberCount() {
        final int nonSharedSubscribersCount = nonSharedSubscribers != null ?
                nonSharedSubscribers.size() :
                countArraySize(nonSharedSubscribersArray);

        return nonSharedSubscribersCount + sharedSubscribers.size();
    }

    public @NotNull Stream<SubscriberWithQoS> getSharedSubscriptionsStream() {
        return sharedSubscribers.values()
                .stream()
                .flatMap(subscriptionGroup -> subscriptionGroup.getSubscriptionsInfos().stream());
    }

    public @Nullable Stream<SubscriberWithQoS> getNonSharedSubscriptionsStream() {
        if (nonSharedSubscribers == null && nonSharedSubscribersArray == null) {
            return null;
        }
        if (nonSharedSubscribers == null) {
            return Stream.of(nonSharedSubscribersArray).filter(Objects::nonNull);
        } else {
            return nonSharedSubscribers.values().stream();
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //                                                                   //
    //                  INTERNAL STRUCTURES MANAGEMENT                   //
    //                                                                   //
    ///////////////////////////////////////////////////////////////////////

    private @NotNull Stream<SubscriberWithQoS> getAllSubscriptionsStream() {
        final Stream<SubscriberWithQoS> sharedSubscriptionStream = getSharedSubscriptionsStream();
        final Stream<SubscriberWithQoS> nonSharedSubscriptionStream = getNonSharedSubscriptionsStream();
        if (nonSharedSubscriptionStream == null) {
            return sharedSubscriptionStream;
        }
        return Stream.concat(sharedSubscriptionStream, nonSharedSubscriptionStream);
    }

    
    public @NotNull Set<SubscriberWithQoS> getSubscribers() {
        return getAllSubscriptionsStream().collect(Collectors.toSet());
    }

    public boolean isEmpty() {
        return (nonSharedSubscribers == null || nonSharedSubscribers.isEmpty()) &&
                (nonSharedSubscribersArray == null || isEmptyArray(nonSharedSubscribersArray)) &&
                sharedSubscribers.isEmpty();
    }

    private @Nullable SubscriptionInfoPresenceStatus storeSubscriberInStructures(
            final @NotNull SubscriberWithQoS subscriberToAdd,
            final @NotNull String topicFilter,
            final int subscriberMapCreationThreshold) {

        if (subscriberToAdd.isSharedSubscription() && subscriberToAdd.getSharedName() != null) {
            if (sharedSubscribers.isEmpty()) {
                sharedSubscribers = new HashMap<>(subscriberMapCreationThreshold);
            }
            final SubscriberWithQoS prev =
                    sharedSubscribers.computeIfAbsent(sharedSubscriptionKey(subscriberToAdd.getSharedName(),
                            topicFilter), key -> new SubscriptionGroup()).put(subscriberToAdd);

            return prev == null ? null : new SubscriptionInfoPresenceStatus(prev.equals(subscriberToAdd));
        }

        // Possible initialization of map and moving the data
        final int exactSubscribersCount = nonSharedSubscribers != null ?
                nonSharedSubscribers.values().size() :
                countArraySize(nonSharedSubscribersArray);

        if (nonSharedSubscribers == null && exactSubscribersCount > subscriberMapCreationThreshold) {
            nonSharedSubscribers = new HashMap<>(subscriberMapCreationThreshold + 1);
            if (nonSharedSubscribersArray != null) {
                for (final SubscriberWithQoS subscriber : nonSharedSubscribersArray) {
                    if (subscriber != null) {
                        nonSharedSubscribers.put(subscriber.getSubscriber(), subscriber);
                    }
                }
                //The array can be removed, because the map is used from now on.
                nonSharedSubscribersArray = null;
            }
        }

        if (nonSharedSubscribers != null) {
            final SubscriberWithQoS prev =
                    nonSharedSubscribers.put(subscriberToAdd.getSubscriber(), subscriberToAdd);
            return prev == null ? null : new SubscriptionInfoPresenceStatus(prev.equals(subscriberToAdd));
        }

        if (nonSharedSubscribersArray == null) {
            nonSharedSubscribersArray = new SubscriberWithQoS[]{subscriberToAdd};
            return null;
        }

        //Let's try to find an existing subscription first
        for (int i = 0; i < nonSharedSubscribersArray.length; i++) {
            if (nonSharedSubscribersArray[i] != null &&
                    subscriberToAdd.getSubscriber().equals(nonSharedSubscribersArray[i].getSubscriber())) {
                //This entry is already present in the array, we can override and abort
                final SubscriptionInfoPresenceStatus subscriptionInfoPresenceStatus =
                        new SubscriptionInfoPresenceStatus(this.nonSharedSubscribersArray[i].equals(subscriberToAdd));

                this.nonSharedSubscribersArray[i] = subscriberToAdd;
                return subscriptionInfoPresenceStatus;
            }
        }

        //Let's try to find an empty slot in the array
        final int emptySlotIndex = Arrays.asList(nonSharedSubscribersArray).indexOf(null);
        if (emptySlotIndex >= 0) {
            nonSharedSubscribersArray[emptySlotIndex] = subscriberToAdd;
        } else { //or allocate a new array
            final SubscriberWithQoS[] newArray = new SubscriberWithQoS[nonSharedSubscribersArray.length + 1];
            System.arraycopy(nonSharedSubscribersArray, 0, newArray, 0, nonSharedSubscribersArray.length);
            newArray[nonSharedSubscribersArray.length] = subscriberToAdd;
            nonSharedSubscribersArray = newArray;
        }

        return null;
    }

    private @Nullable SubscriptionInfoRemovalStatus removeSubscriberFromStructures(
            final @NotNull String subscriber, final @Nullable String sharedName, final @Nullable String topicFilter) {

        SubscriberWithQoS remove = null;
        if (sharedName != null && topicFilter != null) { // shared subscription removal
            final String sharedSubscriptionKey = sharedSubscriptionKey(sharedName, topicFilter);
            final SubscriptionGroup group = sharedSubscribers.get(sharedSubscriptionKey);
            if (group != null) {
                remove = group.remove(subscriber);

                if (group.size() == 0) {
                    sharedSubscribers.remove(sharedSubscriptionKey);
                }
            }
        } else { // non-shared subscription removal
            if (nonSharedSubscribers != null) {
                remove = nonSharedSubscribers.remove(subscriber);
            } else if (nonSharedSubscribersArray != null) {
                for (int i = 0; i < nonSharedSubscribersArray.length; i++) {
                    final SubscriberWithQoS arrayEntry = nonSharedSubscribersArray[i];
                    if (arrayEntry != null && subscriber.equals(arrayEntry.getSubscriber())) {
                        if (subscriber.equals(arrayEntry.getSubscriber())) {
                            nonSharedSubscribersArray[i] = null;
                            remove = arrayEntry;
                            break;
                        }
                    }
                }
            }
        }

        return remove == null ? null : new SubscriptionInfoRemovalStatus(remove.isSharedSubscription());
    }

    /**
     * Holds information about the subscriptions in a group identified by the shared name and the topic filter.
     * Used as a means of optimizing storage and retrieval of shared name/topic filter combinations that
     * have high chance of duplication if there are many shared subscribers in the same group and for the same topic
     * filter.
     */
    private static class SubscriptionGroup {

        private final @NotNull Map<String, SubscriberWithQoS> subscriptions = new HashMap<>();

        @Nullable SubscriberWithQoS put(final @NotNull SubscriberWithQoS subscription) {
            return subscriptions.put(subscription.getSubscriber(), subscription);
        }

        @Nullable SubscriberWithQoS remove(final @NotNull String subscriber) {
            return subscriptions.remove(subscriber);
        }

        @NotNull Collection<SubscriberWithQoS> getSubscriptionsInfos() {
            return subscriptions.values();
        }

        int size() {
            return subscriptions.size();
        }
    }

    private static class SubscriptionInfoPresenceStatus {

        final boolean subscriptionInfoSame;

        private SubscriptionInfoPresenceStatus(final boolean subscriptionInfoSame) {
            this.subscriptionInfoSame = subscriptionInfoSame;
        }
    }

    private static class SubscriptionInfoRemovalStatus {

        final boolean wasShared;

        private SubscriptionInfoRemovalStatus(final boolean wasShared) {
            this.wasShared = wasShared;
        }
    }
}
