package io.github.naimjeg.damagenexus.core;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DamageComponent {
    public final DamageChannel channel;

    private final float[] baseAmounts =
            new float[DamageApplicationBucket.COUNT];

    private float[] channelPreMultipliers = null;
    private float[][] applicationPreMultipliers = null;

    private FloatArrayList postMultipliers = null;
    private FloatArrayList mitigationMultipliers = null;

    private float finalizedOffensiveAmount = 0.0f;
    private float postMitigationAmount = 0.0f;
    private float bypassMitigationAmount = 0.0f;

    private float temporaryResistanceRating = 0.0f;

    public DamageComponent(DamageChannel channel) {
        this.channel = channel;
    }



    public void addBase(float amount) {
        addBase(DamageApplicationBucket.DN_RULE_BASE, amount);
    }

    public void addPreMultiplier(int modifierId, float value) {
        PreMultiplierBucketRegistry.requireFrozen();

        if (channelPreMultipliers == null) {
            channelPreMultipliers =
                    new float[PreMultiplierBucketRegistry.bucketCount()];
        }

        if (modifierId < 0 || modifierId >= channelPreMultipliers.length) {
            throw new IndexOutOfBoundsException(
                    "Invalid pre preMultiplierBucketId id: " + modifierId
            );
        }

        channelPreMultipliers[modifierId] += value;
    }

    public void addPostMultiplier(float value) {
        if (postMultipliers == null) {
            postMultipliers = new FloatArrayList(4);
        }

        postMultipliers.add(value);
    }

    public void addMitigation(float reductionPercent) {
        if (mitigationMultipliers == null) {
            mitigationMultipliers = new FloatArrayList(4);
        }

        mitigationMultipliers.add(reductionPercent);
    }

    public void addTemporaryResistance(float amount) {
        this.temporaryResistanceRating += amount;
    }

    public float getTemporaryResistanceRating() {
        return temporaryResistanceRating;
    }

    public void calculateFinalOffensive(
            float[] globalPre,
            FloatArrayList globalPost
    ) {
        PreMultiplierBucketRegistry.requireFrozen();

        float mitigatedTotal = 0.0f;
        float trueTotal = 0.0f;

        for (DamageApplicationBucket bucket : DamageApplicationBucket.values()) {
            float amount = baseAmounts[bucket.ordinal()];

            if (amount == 0.0f) {
                continue;
            }

            amount = applyApplicationPreMultipliers(bucket, amount);
            amount = applyRegularPreMultipliers(globalPre, amount, bucket);

            if (postMultipliers != null && bucket.affectedByMitigation()) {
                for (int i = 0; i < postMultipliers.size(); i++) {
                    amount *= 1.0f + postMultipliers.getFloat(i);
                }
            }

            if (globalPost != null && bucket.affectedByMitigation()) {
                for (int i = 0; i < globalPost.size(); i++) {
                    amount *= 1.0f + globalPost.getFloat(i);
                }
            }

            if (!Float.isFinite(amount)) {
                amount = 0.0f;
            }

            amount = Math.max(0.0f, amount);

            if (bucket.affectedByMitigation()) {
                mitigatedTotal += amount;
            } else {
                trueTotal += amount;
            }
        }

        finalizedOffensiveAmount = Math.max(0.0f, mitigatedTotal + trueTotal);
        postMitigationAmount = Math.max(0.0f, mitigatedTotal);
        bypassMitigationAmount = Math.max(0.0f, trueTotal);
    }

    private float applyApplicationPreMultipliers(
            DamageApplicationBucket bucket,
            float amount
    ) {
        if (applicationPreMultipliers == null) {
            return amount;
        }

        float[] multipliers = applicationPreMultipliers[bucket.ordinal()];

        if (multipliers == null) {
            return amount;
        }

        int count = PreMultiplierBucketRegistry.bucketCount();

        for (int i = 0; i < count; i++) {
            float value = i < multipliers.length ? multipliers[i] : 0.0f;

            if (value != 0.0f) {
                amount *= 1.0f + value;
            }
        }

        return amount;
    }

    private float applyRegularPreMultipliers(
            float[] globalPre,
            float amount,
            DamageApplicationBucket bucket
    ) {
        if (!bucket.affectedByMitigation()) {
            return amount;
        }

        int count = PreMultiplierBucketRegistry.bucketCount();

        for (int i = 0; i < count; i++) {
            float local =
                    channelPreMultipliers != null && i < channelPreMultipliers.length
                            ? channelPreMultipliers[i]
                            : 0.0f;

            float global =
                    globalPre != null && i < globalPre.length
                            ? globalPre[i]
                            : 0.0f;

            float sum = local + global;

            if (sum != 0.0f) {
                amount *= 1.0f + sum;
            }
        }

        return amount;
    }

    public float convertBaseTo(
            DamageComponent target,
            float ratio
    ) {
        if (target == null) {
            return 0.0f;
        }

        float safeRatio = Math.max(0.0f, Math.min(1.0f, ratio));

        if (safeRatio <= 0.0f) {
            return 0.0f;
        }

        float movedTotal = 0.0f;

        for (DamageApplicationBucket bucket : DamageApplicationBucket.values()) {
            int index = bucket.ordinal();

            float sourceAmount = baseAmounts[index];

            if (sourceAmount <= 0.0f) {
                continue;
            }

            float movedAmount = sourceAmount * safeRatio;

            if (movedAmount <= 0.0f) {
                continue;
            }

            baseAmounts[index] -= movedAmount;
            target.addBase(bucket, movedAmount);

            movedTotal += movedAmount;
        }

        return movedTotal;
    }

    public void calculateFinalDefensive(FloatArrayList globalMitigations) {
        float total = postMitigationAmount;

        if (mitigationMultipliers != null) {
            for (int i = 0; i < mitigationMultipliers.size(); i++) {
                float reduction = mitigationMultipliers.getFloat(i);
                total *= Math.max(0.0f, 1.0f - reduction);
            }
        }

        if (globalMitigations != null) {
            for (int i = 0; i < globalMitigations.size(); i++) {
                float reduction = globalMitigations.getFloat(i);
                total *= Math.max(0.0f, 1.0f - reduction);
            }
        }

        if (!Float.isFinite(total)) {
            total = 0.0f;
        }

        postMitigationAmount = Math.max(0.0f, total) + bypassMitigationAmount;
    }

    public float getBaseAmount() {
        float total = 0.0f;

        for (float amount : baseAmounts) {
            total += amount;
        }

        return total;
    }

    public float getBaseAmount(DamageApplicationBucket bucket) {
        if (bucket == null) {
            return 0.0f;
        }

        return baseAmounts[bucket.ordinal()];
    }

    public boolean hasBaseDamage() {
        return getBaseAmount() > 0.0f;
    }

    public boolean hasFinalizedOffensiveDamage() {
        return finalizedOffensiveAmount > 0.0f;
    }

    public boolean hasPostMitigationDamage() {
        return postMitigationAmount > 0.0f;
    }

    /**
     * Returns true if this component currently represents a real damage channel
     * at any pipeline stage.
     *
     * This is intentionally stage-agnostic:
     * - before offensive finalization: baseAmount is authoritative
     * - after offensive finalization: finalizedOffensiveAmount is authoritative
     * - after defensive calculation: postMitigationAmount is authoritative
     */
    public boolean hasAnyPositiveDamage() {
        return getBaseAmount() > 0.0f
                || finalizedOffensiveAmount > 0.0f
                || postMitigationAmount > 0.0f;
    }

    public float getCurrentBestKnownAmount() {
        if (postMitigationAmount > 0.0f) {
            return postMitigationAmount;
        }

        if (finalizedOffensiveAmount > 0.0f) {
            return finalizedOffensiveAmount;
        }

        return Math.max(0.0f, getBaseAmount());
    }

    public void addBase(
            DamageApplicationBucket bucket,
            float amount
    ) {
        if (bucket == null) {
            bucket = DamageApplicationBucket.DN_RULE_BASE;
        }

        baseAmounts[bucket.ordinal()] += amount;
    }

    public void addApplicationPreMultiplier(
            DamageApplicationBucket bucket,
            int modifierId,
            float value
    ) {
        PreMultiplierBucketRegistry.requireFrozen();

        if (applicationPreMultipliers == null) {
            applicationPreMultipliers =
                    new float[DamageApplicationBucket.COUNT][];
        }

        int bucketIndex = bucket.ordinal();

        if (applicationPreMultipliers[bucketIndex] == null) {
            applicationPreMultipliers[bucketIndex] =
                    new float[PreMultiplierBucketRegistry.bucketCount()];
        }

        applicationPreMultipliers[bucketIndex][modifierId] += value;
    }

    public float getFinalizedOffensiveAmount() {
        return finalizedOffensiveAmount;
    }

    public float getPostMitigationAmount() {
        return postMitigationAmount;
    }
}