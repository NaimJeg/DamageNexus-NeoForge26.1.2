package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.api.context.DamageMutationResult;
import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageSourceProfile;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.contribution.DamageContributionCollector;
import io.github.naimjeg.damagenexus.diagnostics.logging.CombatTrace;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.concurrent.atomic.AtomicLong;

public final class DamageNexusContext implements DamageRuleContext {

    private static final AtomicLong DAMAGE_ID_COUNTER = new AtomicLong();
    private final LivingIncomingDamageEvent neoforgeEvent;
    private final LivingEntity attacker;
    private final LivingEntity victim;
    private final DamageSource source;
    private final boolean isManaged;
    private final boolean isVanillaJumpCrit;

    private final DamageEventSnapshot event;
    private final DamageDiagnosticsContext diagnostics;
    private final DamageEventWriter eventWriter;
    private final DamageSourceContext sourceContext;
    private final DamageContextMutations mutations;

    private final DamagePipelineResult result = new DamagePipelineResult();
    private final DamagePacketState packet = new DamagePacketState();
    private final DamageCombatState combat = new DamageCombatState();

    private final float eventOriginalAmount;
    private final float initialBaseAmount;

    private final DamagePhaseState phaseState = new DamagePhaseState();

    private final CombatTrace trace;
    private final long damageId;
    private final DamageContributionCollector contributions =
            new DamageContributionCollector();

    DamageNexusContext(DamageNexusContextSpec spec) {
        this.event = DamageEventSnapshot.create(
                spec.event(),
                spec.attacker(),
                spec.victim(),
                spec.initialBaseAmount()
        );

        this.neoforgeEvent = this.event.neoforgeEvent();
        this.attacker = this.event.attacker();
        this.victim = this.event.victim();
        this.source = this.event.source();

        this.eventOriginalAmount = this.event.eventOriginalAmount();
        this.initialBaseAmount = this.event.initialBaseAmount();

        this.sourceContext = DamageSourceContext.create(
                this.source,
                this.attacker,
                this.victim,
                spec.sourceProfile(),
                spec.vanillaSnapshot(),
                spec.rebuildVanillaOffensiveMobEffects(),
                spec.rebuildVanillaOffensiveEnchantment(),
                spec.rebuildVanillaPreEventDelta(),
                spec.vanillaOffensiveMobEffectDelta(),
                spec.initialBaseBucket(),
                spec.vanillaOffensiveMobEffectBucket(),
                spec.vanillaOffensiveEnchantmentBucket()
        );

        this.isManaged = sourceContext.managed();
        this.isVanillaJumpCrit = sourceContext.vanillaJumpCrit();

        this.damageId = DAMAGE_ID_COUNTER.incrementAndGet();

        this.diagnostics = DamageDiagnosticsContext.create(
                this.damageId,
                this.attacker,
                this.victim
        );

        this.trace = diagnostics.trace();

        this.eventWriter = new DamageEventWriter(
                this.event,
                this.diagnostics,
                this.trace
        );

        if (this.trace.enabled()) {
            this.trace.transaction().begin(
                    getEntityLogName(this.attacker, "Environment"),
                    getEntityLogName(this.victim, "Unknown"),
                    DamageSourceContext.damageSourceId(this.source),
                    sourceContext.initialChannel().id().toString(),
                    this.eventOriginalAmount,
                    this.initialBaseAmount
            );
        }

        this.mutations = new DamageContextMutations(
                this.packet,
                this.combat,
                this.result,
                this.phaseState,
                this.trace
        );
    }

    private static String getEntityLogName(
            LivingEntity entity,
            String fallback
    ) {
        return entity != null
                ? entity.getName().getString()
                : fallback;
    }


    @Override
    public DamagePhase currentPhase() {
        return phaseState.currentPhase();
    }

    public DamageComponent getOrCreateComponent(DamageChannel rawChannel) {
        return packet.getOrCreateComponent(rawChannel);
    }

    /*
     * ---------------------------------------------------------------------
     * Offensive canonical API
     * ---------------------------------------------------------------------
     */

    public int getActiveComponentCount() {
        return packet.activeComponentCount();
    }

    public DamageComponent getActiveComponent(int activeIndex) {
        return packet.activeComponent(activeIndex);
    }

    private DamageComponent findActiveComponent(DamageChannel rawChannel) {
        return packet.findActiveComponent(rawChannel);
    }

    @Override
    public DamageMutationResult tryAddBaseDamage(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId
    ) {
        return mutations.tryAddBaseDamage(
                channel,
                bucket,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddBaseDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        return mutations.tryAddBaseDamage(
                channel,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddChannelPreMultiplier(
            DamageChannel channel,
            int modifierId,
            float value,
            String sourceId
    ) {
        return mutations.tryAddChannelPreMultiplier(
                channel,
                modifierId,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddApplicationPreMultiplier(
            DamageApplicationBucket bucket,
            int modifierId,
            float value,
            String sourceId
    ) {
        return mutations.tryAddApplicationPreMultiplier(
                bucket,
                modifierId,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddGlobalPreMultiplier(
            int modifierId,
            float value,
            String sourceId
    ) {
        return mutations.tryAddGlobalPreMultiplier(
                modifierId,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddChannelPostMultiplier(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        return mutations.tryAddChannelPostMultiplier(
                channel,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddGlobalPostMultiplier(
            float value,
            String sourceId
    ) {
        return mutations.tryAddGlobalPostMultiplier(
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryConvertDamage(
            DamageChannel from,
            DamageChannel to,
            float ratio,
            String sourceId
    ) {
        return mutations.tryConvertDamage(
                from,
                to,
                ratio,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryGainExtraDamage(
            DamageChannel from,
            DamageChannel to,
            float ratio,
            String sourceId
    ) {
        return mutations.tryGainExtraDamage(
                from,
                to,
                ratio,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddTrueDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        return mutations.tryAddTrueDamage(
                channel,
                value,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryMultiplyArmorEffectiveness(
            float multiplier,
            String sourceId
    ) {
        return mutations.tryMultiplyArmorEffectiveness(
                multiplier,
                sourceId
        );
    }

    public float getArmorEffectivenessMultiplier() {
        return combat.armorEffectivenessMultiplier();
    }

    @Override
    public DamageMutationResult tryAddTemporaryResistance(
            DamageChannel channel,
            float rating,
            String sourceId
    ) {
        return mutations.tryAddTemporaryResistance(
                channel,
                rating,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddChannelMitigation(
            DamageChannel channel,
            float reductionPercent,
            String sourceId
    ) {
        return mutations.tryAddChannelMitigation(
                channel,
                reductionPercent,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryAddGlobalMitigation(
            float reductionPercent,
            String sourceId
    ) {
        return mutations.tryAddGlobalMitigation(
                reductionPercent,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryOverrideFinalDamage(
            float amount,
            String sourceId
    ) {
        return mutations.tryOverrideFinalDamage(
                amount,
                sourceId
        );
    }

    @Override
    public DamageMutationResult tryCancelDamage(String sourceId) {
        return mutations.tryCancelDamage(sourceId);
    }

    /*
     * ---------------------------------------------------------------------
     * State / vanilla accessors
     * ---------------------------------------------------------------------
     */

    public DamageMutationResult tryAddVanillaBaseReconstructedDamage(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId
    ) {
        DamageMutationResult phaseResult =
                mutations.requirePhase(
                        "addVanillaBaseReconstructedDamage",
                        DamagePhase.BASE_MODIFICATION
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        return tryAddVanillaReconstructedDamageInternal(
                channel,
                bucket,
                value,
                sourceId,
                "addVanillaBaseReconstructedDamage"
        );
    }

    public DamageMutationResult tryAddVanillaCriticalBonusDamage(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId
    ) {
        DamageMutationResult phaseResult =
                mutations.requirePhase(
                        "addVanillaCriticalBonusDamage",
                        DamagePhase.CRITICAL_HIT
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        return tryAddVanillaReconstructedDamageInternal(
                channel,
                bucket,
                value,
                sourceId,
                "addVanillaCriticalBonusDamage"
        );
    }

    private DamageMutationResult tryAddVanillaReconstructedDamageInternal(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId,
            String operationName
    ) {
        return mutations.addBaseDamageWithoutPhaseCheck(
                operationName,
                channel,
                bucket,
                value,
                sourceId
        );
    }

    public boolean isDamageCancelled() {
        return result.damageCancelled();
    }

    public float getOffensiveTotal() {
        return result.offensiveTotal();
    }

    public float getFinalEventDamage() {
        return result.finalEventDamage();
    }

    public VanillaDamageCapture.OffensiveSnapshot getVanillaSnapshot() {
        return sourceContext.vanillaSnapshot();
    }

    public float getEventOriginalAmount() {
        return eventOriginalAmount;
    }

    public float getInitialBaseAmount() {
        return initialBaseAmount;
    }

    public VanillaDamageSourceProfile vanillaSourceProfile() {
        return sourceContext.vanillaSourceProfile();
    }

    public void markCritical() {
        combat.markCritical();
    }

    public boolean isCritical() {
        return combat.critical();
    }

    public void setArmorHandled() {
        combat.markArmorHandled();
    }

    public boolean isArmorHandled() {
        return combat.armorHandled();
    }

    public DamagePhase getCurrentProcessingPhase() {
        return phaseState.currentPhase();
    }

    /*
     * ---------------------------------------------------------------------
     * Finalization
     * ---------------------------------------------------------------------
     */

    void setCurrentProcessingPhase(DamagePhase phase) {
        this.phaseState.setCurrentPhase(phase);
    }

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

    void finalizeOffensiveDamage() {
        if (phaseState.offensiveLocked()) {
            return;
        }

        float finalTotal = 0.0f;

        trace.calculation().offenseStart();

        for (int i = 0; i < packet.activeComponentCount(); i++) {
            DamageComponent component = packet.activeComponent(i);

            component.calculateFinalOffensive(
                    packet.globalPreMultipliers(),
                    packet.globalPostMultipliers()
            );

            float componentFinal = component.getFinalizedOffensiveAmount();
            finalTotal += componentFinal;

            if (trace.enabled()) {
                trace.calculation().channelResult(
                        component.channel.id().toString(),
                        component.getBaseAmount(),
                        componentFinal
                );
            }
        }

        result.setOffensiveTotal(finalTotal);
        phaseState.lockOffense();

        trace.calculation().offensiveSummary(result.offensiveTotal());
    }

    /*
     * ---------------------------------------------------------------------
     * Internal helpers
     * ---------------------------------------------------------------------
     */

    void calculateDefensiveDamage() {
        float finalMitigatedTotal = 0.0f;

        for (int i = 0; i < packet.activeComponentCount(); i++) {
            DamageComponent component = packet.activeComponent(i);

            component.calculateFinalDefensive(packet.globalMitigations());
            finalMitigatedTotal += component.getPostMitigationAmount();

            if (trace.enabled()) {
                logBucketBreakdown(component);
            }
        }

        result.setFinalEventDamage(finalMitigatedTotal);
        phaseState.markDefenseCalculatedAndLocked();
    }

    void applyIncomingDamageToEvent() {
        if (!phaseState.defenseCalculated()) {
            mutations.rejectState(
                    "applyIncomingDamageToEvent",
                    "defensive damage has not been calculated"
            );
            return;
        }

        emitContributions();

        eventWriter.applyIncomingDamage(result);
    }

    void applyCancelledDamageToEvent() {
        if (!result.damageCancelled()) {
            result.cancel("pipeline/cancelled");
        }

        phaseState.markDefenseCalculatedAndLocked();

        applyIncomingDamageToEvent();
    }

    private void emitContributions() {
        if (!trace.enabled()) {
            return;
        }

        if (contributions.isEmpty()) {
            return;
        }

        trace.contributions().emit(
                contributions.applied(),
                contributions.rejected(),
                contributions.noOps()
        );
    }


    public boolean suppressesDefaultCritical() {
        return sourceContext.suppressesDefaultCritical();
    }

    public boolean isVanillaMaceSmash() {
        return sourceContext.isVanillaMaceSmash();
    }

    public boolean isVanillaSpearAttack() {
        return sourceContext.isVanillaSpearAttack();
    }

    private void logBucketBreakdown(DamageComponent component) {
        String channelId = component.channel.id().toString();

        for (DamageApplicationBucket bucket : DamageApplicationBucket.values()) {
            float base = component.getBaseAmount(bucket);
            float offensive = component.getFinalizedOffensiveAmount(bucket);
            float postMitigation = component.getPostMitigationAmount(bucket);

            if (base == 0.0f
                    && offensive == 0.0f
                    && postMitigation == 0.0f) {
                continue;
            }

            trace.calculation().bucketResult(
                    channelId,
                    bucket,
                    base,
                    offensive,
                    postMitigation,
                    bucket.affectedByMitigation()
            );
        }
    }

    /*
     * ---------------------------------------------------------------------
     * Public simple accessors
     * ---------------------------------------------------------------------
     */

    public boolean isVanillaJumpCrit() {
        return isVanillaJumpCrit;
    }

    public DamageEventSnapshot eventSnapshot() {
        return event;
    }

    DamageSourceContext sourceContext() {
        return sourceContext;
    }

    public boolean isManaged() {
        return isManaged;
    }

    public LivingEntity attacker() {
        return attacker;
    }

    public LivingEntity victim() {
        return victim;
    }

    public DamageSource source() {
        return source;
    }

    public CombatTrace trace() {
        return trace;
    }

    public long damageId() {
        return damageId;
    }

    public DamageChannel getInitialChannel() {
        return sourceContext.initialChannel();
    }

    @Override
    public boolean hasActiveDamageInChannel(DamageChannel channel) {
        if (channel == null) {
            return false;
        }

        for (int i = 0; i < packet.activeComponentCount(); i++) {
            DamageComponent component = packet.activeComponent(i);

            if (component.channel.id().equals(channel.id())
                    && component.hasAnyPositiveDamage()) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldRebuildVanillaOffensiveMobEffects() {
        return sourceContext.shouldRebuildVanillaOffensiveMobEffects();
    }

    public boolean shouldRebuildVanillaOffensiveEnchantment() {
        return sourceContext.shouldRebuildVanillaOffensiveEnchantment();
    }

    public boolean shouldRebuildVanillaPreEventDelta() {
        return sourceContext.shouldRebuildVanillaPreEventDelta();
    }

    public float getVanillaOffensiveMobEffectDelta() {
        return sourceContext.vanillaOffensiveMobEffectDelta();
    }

    public DamageApplicationBucket getInitialBaseBucket() {
        return sourceContext.initialBaseBucket();
    }

    public DamageApplicationBucket getVanillaOffensiveMobEffectBucket() {
        return sourceContext.vanillaOffensiveMobEffectBucket();
    }

    public DamageApplicationBucket getVanillaOffensiveEnchantmentBucket() {
        return sourceContext.vanillaOffensiveEnchantmentBucket();
    }

    public DamageContributionCollector contributions() {
        return contributions;
    }

}

