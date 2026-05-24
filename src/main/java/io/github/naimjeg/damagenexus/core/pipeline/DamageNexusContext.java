package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.enums.ModifierType;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.registry.DamageModifierRegistry;
import io.github.naimjeg.damagenexus.core.ICombatLogger;
import io.github.naimjeg.damagenexus.event.neoforge.VanillaCritHandler;
import io.github.naimjeg.damagenexus.registry.ModAttachments;

import io.github.naimjeg.damagenexus.registry.ModConstants;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.concurrent.atomic.AtomicLong;

public class DamageNexusContext {

    public final LivingIncomingDamageEvent neoforgeEvent;
    public final LivingEntity attacker;
    public final LivingEntity victim;
    public final DamageSource source;
    public final boolean isManaged;
    public final boolean isVanillaJumpCrit;

    private final DamageComponent[] damagePacket =
            new DamageComponent[DamageChannelRegistry.channelCount()];

    private final int[] activeChannelIndexes =
            new int[damagePacket.length];

    private int activeChannelCount = 0;

    private float[] globalPreMultipliers = null;
    private FloatArrayList globalPostMultipliers = null;
    private FloatArrayList globalMitigations = null;

    private static final String BASE_ADDITIVE_KEY = "damagenexus:base_additive";

    private DamagePhase currentProcessingPhase = DamagePhase.BASE_MODIFICATION;
    private boolean isCritical = false;
    private boolean armorHandled = false;

    private boolean offensiveLocked = false;
    private boolean defensiveLocked = false;
    private boolean defenseCalculated = false;
    private float finalEventDamage = 0.0f;

    private float armorEffectivenessMultiplier = 1.0f;

    public final ICombatLogger debugger;
    private static final AtomicLong DAMAGE_ID_COUNTER = new AtomicLong();
    public final long damageId;

    public DamageNexusContext(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim
    ) {
        this.neoforgeEvent = event;
        this.attacker = attacker;
        this.victim = victim;
        this.source = event.getSource();

        this.damageId = DAMAGE_ID_COUNTER.incrementAndGet();

        this.debugger = ModConfig.isDebugMode()
                ? new ICombatLogger.ActiveLogger(this.damageId)
                : ICombatLogger.NO_OP;

        this.isManaged = checkCompatibility(event.getSource());

        if (attacker instanceof Player) {
            int pendingTargetId = VanillaCritHandler.PENDING_CRIT_TARGET.get();
            this.isVanillaJumpCrit = (pendingTargetId == victim.getId());

            VanillaCritHandler.PENDING_CRIT_TARGET.set(-1);
        } else {
            this.isVanillaJumpCrit = false;
        }

        DamageChannel initialChannel =
                DamageChannelRegistry.determineInitialChannel(this.source);

        getOrCreateComponent(initialChannel).addBase(event.getOriginalAmount());

        if (this.debugger.enabled()) {
            this.debugger.logBegin(
                    getEntityLogName(attacker, "Environment"),
                    getEntityLogName(victim, "Unknown"),
                    getDamageSourceId(this.source),
                    initialChannel.id().toString(),
                    event.getOriginalAmount()
            );
        }

        this.debugger.logOperation(
                "vanilla:initial_hit",
                DamagePhase.BASE_MODIFICATION,
                "BASE_DAMAGE",
                event.getOriginalAmount()
        );
    }

    public DamageComponent getOrCreateComponent(DamageChannel rawChannel) {
        DamageChannel channel = DamageChannelRegistry.resolve(rawChannel);

        int index = channel.index();

        if (index < 0 || index >= damagePacket.length) {
            channel = DamageChannelRegistry.getUntyped();
            index = channel.index();
        }

        DamageComponent component = damagePacket[index];

        if (component == null) {
            component = new DamageComponent(channel);
            damagePacket[index] = component;
            activeChannelIndexes[activeChannelCount++] = index;
        }

        return component;
    }

    public int getActiveComponentCount() {
        return activeChannelCount;
    }

    public DamageComponent getActiveComponent(int activeIndex) {
        if (activeIndex < 0 || activeIndex >= activeChannelCount) {
            throw new IndexOutOfBoundsException("Invalid active component index: " + activeIndex);
        }

        int channelIndex = activeChannelIndexes[activeIndex];
        return damagePacket[channelIndex];
    }


    public void addDamageModifier(DamageChannel channel, ModifierType type, int modifierId, float value) {
        if (!canModifyOffense("addDamageModifier")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addDamageModifier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        DamageComponent comp = getOrCreateComponent(channel);

        switch (type) {
            case BASE_ADDITIVE -> {
                comp.addPreMultiplier(ModConstants.BASE_ADDITIVE, value);
            }

            case PRE_MULTIPLIER -> {
                comp.addPreMultiplier(modifierId, value);
            }

            case POST_MULTIPLIER -> {
                comp.addPostMultiplier(value);
            }
        }

        debugger.logOperation(
                modifierId,
                currentProcessingPhase,
                type.name(),
                value
        );
    }

    public void addChannelBaseAdditive(DamageChannel channel, float value, String sourceId) {
        if (!canModifyOffense("addChannelBaseAdditive")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addChannelBaseAdditive",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        getOrCreateComponent(channel).addPreMultiplier(
                ModConstants.BASE_ADDITIVE,
                value
        );

        debugger.logOperation(
                sourceId,
                currentProcessingPhase,
                "BASE_ADDITIVE",
                value
        );
    }

    public void addChannelPreModifier(DamageChannel channel, int modifierId, float value, String sourceId) {
        if (offensiveLocked) {
            throw new IllegalStateException("Damage already finalized!");
        }

        getOrCreateComponent(channel).addPreMultiplier(modifierId, value);

        debugger.logOperation(sourceId, currentProcessingPhase, "PRE_MULTIPLIER", value);
    }

    public void addChannelPostMultiplier(DamageChannel channel, float value, String sourceId) {
        if (offensiveLocked) {
            throw new IllegalStateException("Damage already finalized!");
        }

        getOrCreateComponent(channel).addPostMultiplier(value);

        debugger.logOperation(sourceId, currentProcessingPhase, "POST_MULTIPLIER", value);
    }

    public void addGlobalModifier(ModifierType type, int modifierId, float value) {
        if (!canModifyOffense("addGlobalModifier")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addGlobalModifier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        switch (type) {
            case BASE_ADDITIVE -> {
                ensureGlobalPreCapacity();

                int id = ModConstants.BASE_ADDITIVE;
                if (id < 0 || id >= globalPreMultipliers.length) {
                    debugger.logRejectedMutation(
                            "addGlobalModifier",
                            currentProcessingPhase,
                            "invalid BASE_ADDITIVE modifier id"
                    );
                    return;
                }

                globalPreMultipliers[id] += value;
            }

            case PRE_MULTIPLIER -> {
                ensureGlobalPreCapacity();

                if (!isValidPreModifierId(modifierId)) {
                    debugger.logRejectedMutation(
                            "addGlobalModifier",
                            currentProcessingPhase,
                            "invalid pre modifier id " + modifierId
                    );
                    return;
                }

                globalPreMultipliers[modifierId] += value;
            }

            case POST_MULTIPLIER -> {
                if (globalPostMultipliers == null) {
                    globalPostMultipliers = new FloatArrayList(4);
                }

                globalPostMultipliers.add(value);
            }
        }

        debugger.logOperation(
                modifierId,
                currentProcessingPhase,
                type.name(),
                value
        );
    }

    private void ensureGlobalPreCapacity() {
        DamageModifierRegistry.requireFrozen();

        if (globalPreMultipliers == null) {
            globalPreMultipliers = new float[DamageModifierRegistry.preModifierCount()];
        }
    }

    private boolean isValidPreModifierId(int modifierId) {
        return modifierId >= 0 && modifierId < DamageModifierRegistry.preModifierCount();
    }

    public void addGlobalPreModifier(Identifier key, float value) {
        int id = DamageModifierRegistry.getPreModifierId(key);
        addGlobalModifier(ModifierType.PRE_MULTIPLIER, id, value);
    }

    public void addGlobalPostMultiplier(float value) {
        addGlobalModifier(ModifierType.POST_MULTIPLIER, -1, value);
    }

    public void multiplyArmorEffectiveness(float multiplier) {
        if (currentProcessingPhase != DamagePhase.MITIGATION_SETUP) {
            debugger.logRejectedMutation(
                    "multiplyArmorEffectiveness",
                    currentProcessingPhase,
                    "expected phase MITIGATION_SETUP"
            );
            return;
        }

        if (!isFinite(multiplier)) {
            debugger.logRejectedMutation(
                    "multiplyArmorEffectiveness",
                    currentProcessingPhase,
                    "non-finite multiplier"
            );
            return;
        }

        float safeMultiplier = Math.max(0.0f, multiplier);

        this.armorEffectivenessMultiplier *= safeMultiplier;
        this.armorEffectivenessMultiplier = Math.max(0.0f, this.armorEffectivenessMultiplier);

        debugger.logOperation(
                "armor_effectiveness",
                currentProcessingPhase,
                "EFFECTIVENESS_MULT",
                safeMultiplier
        );
    }

    public float getArmorEffectivenessMultiplier() {
        return this.armorEffectivenessMultiplier;
    }

    public void addGlobalMitigation(float reductionPercent) {
        if (defensiveLocked) {
            debugger.logRejectedMutation(
                    "addGlobalMitigation",
                    currentProcessingPhase,
                    "defensive damage already calculated"
            );
            return;
        }

        if (globalMitigations == null) {
            globalMitigations = new FloatArrayList(4);
        }

        globalMitigations.add(reductionPercent);

        debugger.logOperation("global_mitigation", currentProcessingPhase, "GLOBAL_MITIGATION", reductionPercent);
    }

    public void addChannelMitigation(DamageChannel channel, float reductionPercent, String sourceId) {
        if (defensiveLocked) {
            debugger.logRejectedMutation(
                    "addChannelMitigation",
                    currentProcessingPhase,
                    "defensive damage already calculated"
            );
            return;
        }

        if (currentProcessingPhase != DamagePhase.MITIGATION_SETUP) {
            debugger.logRejectedMutation(
                    "addChannelMitigation",
                    currentProcessingPhase,
                    "expected phase MITIGATION_SETUP"
            );
            return;
        }

        if (!isFinite(reductionPercent)) {
            debugger.logRejectedMutation(
                    "addChannelMitigation",
                    currentProcessingPhase,
                    "non-finite reduction"
            );
            return;
        }

        float safeReduction = Math.max(-1.0f, Math.min(1.0f, reductionPercent));

        DamageComponent comp = getOrCreateComponent(channel);
        comp.addMitigation(safeReduction);

        debugger.logOperation(sourceId, currentProcessingPhase, "CHANNEL_MITIGATION", safeReduction);
    }

    public void addBaseDamage(DamageChannel channel, String sourceId, float value) {
        if (!canModifyOffense("addBaseDamage")) {
            return;
        }

        if (!requirePhase("addBaseDamage", DamagePhase.BASE_MODIFICATION)) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addBaseDamage",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        getOrCreateComponent(channel).addBase(value);

        debugger.logOperation(
                sourceId,
                currentProcessingPhase,
                "BASE_DAMAGE",
                value
        );
    }

    public void convertDamage(DamageChannel from, DamageChannel to, float ratio, String sourceId) {
        if (!canModifyOffense("convertDamage")) {
            return;
        }

        if (!requirePhase("convertDamage", DamagePhase.GLOBAL_ADJUSTMENT)) {
            return;
        }

        if (!isFinite(ratio)) {
            debugger.logRejectedMutation(
                    "convertDamage",
                    currentProcessingPhase,
                    "non-finite ratio"
            );
            return;
        }

        DamageComponent sourceComp = getOrCreateComponent(from);

        float clampedRatio = Math.max(0.0f, Math.min(1.0f, ratio));
        float amountToConvert = sourceComp.getBaseAmount() * clampedRatio;

        if (amountToConvert <= 0.0f) {
            return;
        }

        sourceComp.addBase(-amountToConvert);
        getOrCreateComponent(to).addBase(amountToConvert);

        debugger.logOperation(
                sourceId,
                currentProcessingPhase,
                "CONVERSION",
                amountToConvert
        );
    }

    public void gainDamageAsExtra(DamageChannel basedOn, DamageChannel to, float ratio, String sourceId) {
        if (!canModifyOffense("gainDamageAsExtra")) {
            return;
        }

        if (!requirePhase("gainDamageAsExtra", DamagePhase.GLOBAL_ADJUSTMENT)) {
            return;
        }

        if (!isFinite(ratio)) {
            debugger.logRejectedMutation(
                    "gainDamageAsExtra",
                    currentProcessingPhase,
                    "non-finite ratio"
            );
            return;
        }

        DamageComponent sourceComp = getOrCreateComponent(basedOn);
        float extraAmount = sourceComp.getBaseAmount() * Math.max(0.0f, ratio);

        if (extraAmount <= 0.0f) {
            return;
        }

        getOrCreateComponent(to).addBase(extraAmount);

        debugger.logOperation(
                sourceId,
                currentProcessingPhase,
                "BASE_DAMAGE",
                extraAmount
        );
    }

    public void markCritical() { this.isCritical = true; }
    public boolean isCritical() { return this.isCritical; }

    public void setArmorHandled() { this.armorHandled = true; }
    public boolean isArmorHandled() { return this.armorHandled; }

    void setCurrentProcessingPhase(DamagePhase phase) { this.currentProcessingPhase = phase; }

    public float getAttackerAttrOrZero(Holder<Attribute> attrHolder) {
        if (attacker == null || attrHolder == null) {
            return 0.0f;
        }

        if (!attacker.getAttributes().hasAttribute(attrHolder)) {
            return 0.0f;
        }

        return (float) attacker.getAttributeValue(attrHolder);
    }

    public float getVictimAttrOrZero(Holder<Attribute> attrHolder) {
        if (victim == null || attrHolder == null) {
            return 0.0f;
        }

        if (!victim.getAttributes().hasAttribute(attrHolder)) {
            return 0.0f;
        }

        return (float) victim.getAttributeValue(attrHolder);
    }

    private boolean checkCompatibility(DamageSource source) {
        return source.getEntity() != null || source.is(DamageTypeTags.IS_FIRE);
    }

    public void finalizeOffensiveDamage() {
        if (offensiveLocked) return;

        float finalTotal = 0.0f;

        debugger.logCalculationStart();

        for (int i = 0; i < activeChannelCount; i++) {
            DamageComponent component = damagePacket[activeChannelIndexes[i]];

            component.calculateFinalOffensive(globalPreMultipliers, globalPostMultipliers);

            float componentFinal = component.getFinalizedOffensiveAmount();
            finalTotal += componentFinal;

            if (debugger.enabled()) {
                debugger.logChannelResult(
                        component.channel.id().toString(),
                        component.getBaseAmount(),
                        componentFinal
                );
            }
        }

        if (Float.isNaN(finalTotal) || Float.isInfinite(finalTotal)) {
            finalTotal = 0.0f;
        }

        offensiveLocked = true;
        
        debugger.logOffensiveSummary(Math.max(0.0f, finalTotal));
    }

    public void calculateDefensiveDamage() {
        float finalMitigatedTotal = 0.0f;

        for (int i = 0; i < activeChannelCount; i++) {
            DamageComponent component = damagePacket[activeChannelIndexes[i]];
            component.calculateFinalDefensive(globalMitigations);
            finalMitigatedTotal += component.getPostMitigationAmount();
        }

        if (Float.isNaN(finalMitigatedTotal) || Float.isInfinite(finalMitigatedTotal)) {
            finalMitigatedTotal = 0.0f;
        }

        this.finalEventDamage = Math.max(0.0f, finalMitigatedTotal);
        this.defenseCalculated = true;
        this.defensiveLocked = true;
    }

    public void overrideFinalDamage(float amount, String sourceId) {
        if (currentProcessingPhase != DamagePhase.FINAL_OVERRIDE) {
            debugger.logRejectedMutation("overrideFinalDamage", currentProcessingPhase, "expected phase FINAL_OVERRIDE");
            return;
        }

        if (!isFinite(amount)) return;

        this.finalEventDamage = Math.max(0.0f, amount);
        debugger.logOperation(sourceId, currentProcessingPhase, "FINAL_OVERRIDE", amount);
    }

    public void applyIncomingDamageToEvent() {
        if (!defenseCalculated) {
            debugger.logRejectedMutation(
                    "applyDamageToEvent",
                    currentProcessingPhase,
                    "defensive damage has not been calculated"
            );
            return;
        }

        neoforgeEvent.setAmount(this.finalEventDamage);
        debugger.logDefensiveSummary(this.finalEventDamage);

        suppressVanillaReductions();
    }
    private void suppressVanillaReductions() {
        neoforgeEvent.addReductionModifier(
                DamageContainer.Reduction.ARMOR,
                (container, vanillaReduction) -> 0.0f
        );

        neoforgeEvent.addReductionModifier(
                DamageContainer.Reduction.ENCHANTMENTS,
                (container, vanillaReduction) -> 0.0f
        );

        neoforgeEvent.addReductionModifier(
                DamageContainer.Reduction.MOB_EFFECTS,
                (container, vanillaReduction) -> 0.0f
        );

        neoforgeEvent.addReductionModifier(
                DamageContainer.Reduction.INNATE_RESISTANCE,
                (container, vanillaReduction) -> 0.0f
        );
    }

    private boolean canModifyOffense(String action) {
        if (!offensiveLocked) {
            return true;
        }

        debugger.logRejectedMutation(
                action,
                currentProcessingPhase,
                "offensive damage already finalized"
        );

        return false;
    }

    private boolean requirePhase(String action, DamagePhase expected) {
        if (currentProcessingPhase == expected) {
            return true;
        }

        debugger.logRejectedMutation(
                action,
                currentProcessingPhase,
                "expected phase " + expected
        );

        return false;
    }

    private boolean requireMultiplierPhase(String action) {
        if (currentProcessingPhase == DamagePhase.CRITICAL_HIT
                || currentProcessingPhase == DamagePhase.CONDITIONAL_MULTI
                || currentProcessingPhase == DamagePhase.TYPE_SCALING) {
            return true;
        }

        debugger.logRejectedMutation(
                action,
                currentProcessingPhase,
                "expected multiplier-compatible phase"
        );

        return false;
    }

    private static String getEntityLogName(LivingEntity entity, String fallback) {
        return entity != null ? entity.getName().getString() : fallback;
    }

    private static String getDamageSourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("unknown");
    }

    public float getCalculatedFinalDamage() {
        return this.finalEventDamage;
    }

    private boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }
}