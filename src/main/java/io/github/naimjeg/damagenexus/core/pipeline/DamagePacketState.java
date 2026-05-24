// src/main/java/io/github/naimjeg/damagenexus/core/pipeline/DamagePacketState.java
package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.PreMultiplierSet;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

public final class DamagePacketState {

    private final DamageComponent[] componentsByChannelIndex =
            new DamageComponent[DamageChannelRegistry.channelCount()];

    private final int[] activeChannelIndexes =
            new int[componentsByChannelIndex.length];

    private int activeChannelCount = 0;

    private PreMultiplierSet globalPreMultipliers = null;
    private FloatArrayList globalPostMultipliers = null;
    private FloatArrayList globalMitigations = null;

    public DamageComponent getOrCreateComponent(DamageChannel rawChannel) {
        DamageChannel channel = DamageChannelRegistry.resolve(rawChannel);
        int index = channel.index();

        if (index < 0 || index >= componentsByChannelIndex.length) {
            channel = DamageChannelRegistry.getUntyped();
            index = channel.index();
        }

        DamageComponent component = componentsByChannelIndex[index];

        if (component == null) {
            component = new DamageComponent(channel);
            componentsByChannelIndex[index] = component;
            activeChannelIndexes[activeChannelCount++] = index;
        }

        return component;
    }

    public DamageComponent findActiveComponent(DamageChannel rawChannel) {
        DamageChannel channel = DamageChannelRegistry.resolve(rawChannel);

        for (int i = 0; i < activeChannelCount; i++) {
            DamageComponent component =
                    componentsByChannelIndex[activeChannelIndexes[i]];

            if (component.channel.equals(channel)) {
                return component;
            }
        }

        return null;
    }

    public int activeComponentCount() {
        return activeChannelCount;
    }

    public DamageComponent activeComponent(int activeIndex) {
        if (activeIndex < 0 || activeIndex >= activeChannelCount) {
            throw new IndexOutOfBoundsException(
                    "Invalid active component index: " + activeIndex
            );
        }

        return componentsByChannelIndex[activeChannelIndexes[activeIndex]];
    }

    public void addGlobalPreMultiplier(int modifierId, float value) {
        PreMultiplierBucketRegistry.requireFrozen();

        if (globalPreMultipliers == null) {
            globalPreMultipliers = new PreMultiplierSet();
        }

        globalPreMultipliers.add(modifierId, value);
    }

    public void addGlobalPostMultiplier(float value) {
        if (globalPostMultipliers == null) {
            globalPostMultipliers = new FloatArrayList(4);
        }

        globalPostMultipliers.add(value);
    }

    public void addGlobalMitigation(float reductionPercent) {
        if (globalMitigations == null) {
            globalMitigations = new FloatArrayList(4);
        }

        globalMitigations.add(reductionPercent);
    }

    public PreMultiplierSet globalPreMultipliers() {
        return globalPreMultipliers;
    }

    public FloatArrayList globalPostMultipliers() {
        return globalPostMultipliers;
    }

    public FloatArrayList globalMitigations() {
        return globalMitigations;
    }
}