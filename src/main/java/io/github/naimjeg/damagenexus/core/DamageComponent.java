package io.github.naimjeg.damagenexus.core;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.registry.DamageModifierRegistry;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DamageComponent {
    public final DamageChannel channel;

    private float baseAmount = 0.0f;

    private float[] preMultipliers = null;
    private FloatArrayList postMultipliers = null;
    private FloatArrayList mitigationMultipliers = null;

    private float temporaryResistanceRating = 0.0f;

    private float finalizedOffensiveAmount = 0.0f;
    private float postMitigationAmount = 0.0f;

    public DamageComponent(DamageChannel channel) {
        this.channel = channel;
    }

    public float getBaseAmount() {
        return baseAmount;
    }

    public void addBase(float amount) {
        this.baseAmount += amount;
    }

    public void addPreMultiplier(int modifierId, float value) {
        DamageModifierRegistry.requireFrozen();

        if (preMultipliers == null) {
            preMultipliers = new float[DamageModifierRegistry.preModifierCount()];
        }

        if (modifierId < 0 || modifierId >= preMultipliers.length) {
            throw new IndexOutOfBoundsException("Invalid pre modifier id: " + modifierId);
        }

        preMultipliers[modifierId] += value;
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

    public void calculateFinalOffensive(float[] globalPre, FloatArrayList globalPost) {
        DamageModifierRegistry.requireFrozen();

        float total = baseAmount;
        int preCount = DamageModifierRegistry.preModifierCount();

        for (int i = 0; i < preCount; i++) {
            float local =
                    preMultipliers != null && i < preMultipliers.length
                            ? preMultipliers[i]
                            : 0.0f;

            float global =
                    globalPre != null && i < globalPre.length
                            ? globalPre[i]
                            : 0.0f;

            float sum = local + global;

            if (sum != 0.0f) {
                total *= 1.0f + sum;
            }
        }

        if (postMultipliers != null) {
            for (int i = 0; i < postMultipliers.size(); i++) {
                total *= 1.0f + postMultipliers.getFloat(i);
            }
        }

        if (globalPost != null) {
            for (int i = 0; i < globalPost.size(); i++) {
                total *= 1.0f + globalPost.getFloat(i);
            }
        }

        if (Float.isNaN(total) || Float.isInfinite(total)) {
            total = 0.0f;
        }

        finalizedOffensiveAmount = Math.max(0.0f, total);
        postMitigationAmount = finalizedOffensiveAmount;
    }

    public void calculateFinalDefensive(FloatArrayList globalMitigations) {
        float total = finalizedOffensiveAmount;

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

        if (Float.isNaN(total) || Float.isInfinite(total)) {
            total = 0.0f;
        }

        postMitigationAmount = Math.max(0.0f, total);
    }

    public float getFinalizedOffensiveAmount() {
        return finalizedOffensiveAmount;
    }

    public float getPostMitigationAmount() {
        return postMitigationAmount;
    }
}