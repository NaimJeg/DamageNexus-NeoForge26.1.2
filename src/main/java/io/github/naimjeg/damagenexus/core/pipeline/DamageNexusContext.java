package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageSourceProfile;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.core.trace.DamageContributionCollector;
import io.github.naimjeg.damagenexus.core.trace.DamageMutationType;
import io.github.naimjeg.damagenexus.diagnostics.logging.CombatTrace;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.concurrent.atomic.AtomicLong;

public class DamageNexusContext {

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

    private final DamagePipelineResult result = new DamagePipelineResult();
    private final DamagePacketState packet = new DamagePacketState();
    private final DamageCombatState combat = new DamageCombatState();

    private final float eventOriginalAmount;
    private final float initialBaseAmount;

    private final DamagePhaseState phaseState = new DamagePhaseState();

    private final CombatTrace trace;

    private static final AtomicLong DAMAGE_ID_COUNTER = new AtomicLong();

    private final long damageId;

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
    }

    public DamageComponent getOrCreateComponent(DamageChannel rawChannel) {
        return packet.getOrCreateComponent(rawChannel);
    }

    public int getActiveComponentCount() {
        return packet.activeComponentCount();
    }

    public DamageComponent getActiveComponent(int activeIndex) {
        return packet.activeComponent(activeIndex);
    }

    private DamageComponent findActiveComponent(DamageChannel rawChannel) {
        return packet.findActiveComponent(rawChannel);
    }

    /*
     * ---------------------------------------------------------------------
     * Offensive canonical API
     * ---------------------------------------------------------------------
     */

    public DamageMutationResult tryAddBaseDamage(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("addBaseDamage");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "addBaseDamage",
                        DamagePhase.BASE_MODIFICATION
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (channel == null) {
            return reject(
                    "addBaseDamage",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null channel"
            );
        }

        if (!isFinite(value)) {
            return reject(
                    "addBaseDamage",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        DamageApplicationBucket effectiveBucket =
                bucket != null
                        ? bucket
                        : DamageApplicationBucket.DN_RULE_BASE;

        getOrCreateComponent(channel).addBase(effectiveBucket, value);

        trace.mutations().baseDamage(
                sourceId,
                phaseState.currentPhase(),
                effectiveBucket,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddBaseDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        return tryAddBaseDamage(
                channel,
                DamageApplicationBucket.DN_RULE_BASE,
                value,
                sourceId
        );
    }

    public DamageMutationResult tryAddChannelPreMultiplier(
            DamageChannel channel,
            int modifierId,
            float value,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("addChannelPreMultiplier");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requireMultiplierPhaseResult("addChannelPreMultiplier");

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (channel == null) {
            return reject(
                    "addChannelPreMultiplier",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null channel"
            );
        }

        if (!isFinite(value)) {
            return reject(
                    "addChannelPreMultiplier",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        PreMultiplierBucketRegistry.requireFrozen();

        if (!isValidPreModifierId(modifierId)) {
            return rejectInvalidPreBucket(
                    "addChannelPreMultiplier",
                    modifierId
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        getOrCreateComponent(channel).addPreMultiplier(modifierId, value);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.CHANNEL_PRE_MULTIPLIER,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddChannelPostMultiplier(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("addChannelPostMultiplier");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requireMultiplierPhaseResult("addChannelPostMultiplier");

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (channel == null) {
            return reject(
                    "addChannelPostMultiplier",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null channel"
            );
        }

        if (!isFinite(value)) {
            return reject(
                    "addChannelPostMultiplier",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        getOrCreateComponent(channel).addPostMultiplier(value);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.CHANNEL_POST_MULTIPLIER,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddGlobalPreMultiplier(
            int modifierId,
            float value,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("addGlobalPreMultiplier");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requireMultiplierPhaseResult("addGlobalPreMultiplier");

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (!isFinite(value)) {
            return reject(
                    "addGlobalPreMultiplier",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        PreMultiplierBucketRegistry.requireFrozen();

        if (!isValidPreModifierId(modifierId)) {
            return rejectInvalidPreBucket(
                    "addGlobalPreMultiplier",
                    modifierId
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        packet.addGlobalPreMultiplier(modifierId, value);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.GLOBAL_PRE_MULTIPLIER,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddGlobalPostMultiplier(
            float value,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("addGlobalPostMultiplier");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requireGlobalPostMultiplierPhaseResult(
                        "addGlobalPostMultiplier"
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (!isFinite(value)) {
            return reject(
                    "addGlobalPostMultiplier",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        packet.addGlobalPostMultiplier(value);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.GLOBAL_POST_MULTIPLIER,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddApplicationPreMultiplier(
            DamageApplicationBucket bucket,
            int modifierId,
            float value,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("addApplicationPreMultiplier");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requireMultiplierPhaseResult("addApplicationPreMultiplier");

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (bucket == null) {
            return reject(
                    "addApplicationPreMultiplier",
                    DamageMutationResult.REJECTED_NULL_APPLICATION_BUCKET,
                    "null application bucket"
            );
        }

        if (!isFinite(value)) {
            return reject(
                    "addApplicationPreMultiplier",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        PreMultiplierBucketRegistry.requireFrozen();

        if (!isValidPreModifierId(modifierId)) {
            return rejectInvalidPreBucket(
                    "addApplicationPreMultiplier",
                    modifierId
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        for (int i = 0; i < packet.activeComponentCount(); i++) {
            DamageComponent component = packet.activeComponent(i);
            component.addApplicationPreMultiplier(bucket, modifierId, value);
        }

        trace.mutations().applicationPreMultiplier(
                sourceId,
                phaseState.currentPhase(),
                bucket,
                modifierId,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryConvertDamage(
            DamageChannel from,
            DamageChannel to,
            float ratio,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("convertDamage");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "convertDamage",
                        DamagePhase.TYPE_SCALING
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (from == null || to == null) {
            return reject(
                    "convertDamage",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null source or target channel"
            );
        }

        if (!isFinite(ratio)) {
            return reject(
                    "convertDamage",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite ratio"
            );
        }

        float clampedRatio = Math.max(0.0f, Math.min(1.0f, ratio));

        if (clampedRatio == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        DamageComponent sourceComponent = findActiveComponent(from);

        if (sourceComponent == null) {
            return DamageMutationResult.NO_OP_EMPTY_SOURCE;
        }

        DamageComponent targetComponent = getOrCreateComponent(to);

        float amountToConvert =
                sourceComponent.convertBaseTo(
                        targetComponent,
                        clampedRatio
                );

        if (amountToConvert <= 0.0f) {
            return DamageMutationResult.NO_OP_EMPTY_SOURCE;
        }

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.CONVERSION,
                amountToConvert
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddTrueDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        return tryAddBaseDamage(
                channel,
                DamageApplicationBucket.DN_TRUE_DAMAGE,
                value,
                sourceId
        );
    }

    public DamageMutationResult tryGainDamageAsExtra(
            DamageChannel basedOn,
            DamageChannel to,
            float ratio,
            String sourceId
    ) {
        DamageMutationResult offenseResult =
                canModifyOffenseResult("gainDamageAsExtra");

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "gainDamageAsExtra",
                        DamagePhase.TYPE_SCALING
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (basedOn == null || to == null) {
            return reject(
                    "gainDamageAsExtra",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null source or target channel"
            );
        }

        if (!isFinite(ratio)) {
            return reject(
                    "gainDamageAsExtra",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite ratio"
            );
        }

        float safeRatio = Math.max(0.0f, ratio);

        if (safeRatio == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        DamageComponent sourceComponent = findActiveComponent(basedOn);

        if (sourceComponent == null) {
            return DamageMutationResult.NO_OP_EMPTY_SOURCE;
        }

        float extraAmount = sourceComponent.getBaseAmount() * safeRatio;

        if (extraAmount <= 0.0f) {
            return DamageMutationResult.NO_OP_EMPTY_SOURCE;
        }

        getOrCreateComponent(to).addBase(
                DamageApplicationBucket.DN_RULE_BASE,
                extraAmount
        );

        trace.mutations().baseDamage(
                sourceId,
                phaseState.currentPhase(),
                DamageApplicationBucket.DN_RULE_BASE,
                extraAmount
        );

        return DamageMutationResult.APPLIED;
    }

    /*
     * ---------------------------------------------------------------------
     * Defensive canonical API
     * ---------------------------------------------------------------------
     */

    public DamageMutationResult tryMultiplyArmorEffectiveness(
            float multiplier,
            String sourceId
    ) {
        DamageMutationResult defenseResult =
                canModifyDefenseResult("multiplyArmorEffectiveness");

        if (!defenseResult.applied()) {
            return defenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "multiplyArmorEffectiveness",
                        DamagePhase.MITIGATION_SETUP
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (!isFinite(multiplier)) {
            return reject(
                    "multiplyArmorEffectiveness",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite multiplier"
            );
        }

        float safeMultiplier = Math.max(0.0f, multiplier);

        combat.multiplyArmorEffectiveness(safeMultiplier);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.ARMOR_EFFECTIVENESS_MULTIPLIER,
                safeMultiplier
        );

        return DamageMutationResult.APPLIED;
    }

    public float getArmorEffectivenessMultiplier() {
        return combat.armorEffectivenessMultiplier();
    }

    public DamageMutationResult tryAddTemporaryResistance(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        DamageMutationResult defenseResult =
                canModifyDefenseResult("addTemporaryResistance");

        if (!defenseResult.applied()) {
            return defenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "addTemporaryResistance",
                        DamagePhase.MITIGATION_SETUP
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (channel == null) {
            return reject(
                    "addTemporaryResistance",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null channel"
            );
        }

        if (!isFinite(value)) {
            return reject(
                    "addTemporaryResistance",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        DamageComponent component = findActiveComponent(channel);

        if (component == null) {
            return rejectInactiveChannel(
                    "addTemporaryResistance",
                    channel
            );
        }

        component.addTemporaryResistance(value);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.TEMPORARY_RESISTANCE,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddChannelMitigation(
            DamageChannel channel,
            float reductionPercent,
            String sourceId
    ) {
        DamageMutationResult defenseResult =
                canModifyDefenseResult("addChannelMitigation");

        if (!defenseResult.applied()) {
            return defenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "addChannelMitigation",
                        DamagePhase.MITIGATION_SETUP
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (channel == null) {
            return reject(
                    "addChannelMitigation",
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null channel"
            );
        }

        if (!isFinite(reductionPercent)) {
            return reject(
                    "addChannelMitigation",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite reduction"
            );
        }

        float safeReduction = Math.max(-1.0f, Math.min(1.0f, reductionPercent));

        if (safeReduction == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        DamageComponent component = findActiveComponent(channel);

        if (component == null) {
            return rejectInactiveChannel(
                    "addChannelMitigation",
                    channel
            );
        }

        component.addMitigation(safeReduction);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.CHANNEL_MITIGATION,
                safeReduction
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddGlobalMitigation(
            float reductionPercent,
            String sourceId
    ) {
        DamageMutationResult defenseResult =
                canModifyDefenseResult("addGlobalMitigation");

        if (!defenseResult.applied()) {
            return defenseResult;
        }

        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "addGlobalMitigation",
                        DamagePhase.MITIGATION_SETUP
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (!isFinite(reductionPercent)) {
            return reject(
                    "addGlobalMitigation",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite reduction"
            );
        }

        float safeReduction = Math.max(-1.0f, Math.min(1.0f, reductionPercent));

        if (safeReduction == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        packet.addGlobalMitigation(safeReduction);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.GLOBAL_MITIGATION,
                safeReduction
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryOverrideFinalDamage(
            float amount,
            String sourceId
    ) {
        DamageMutationResult phaseResult =
                requirePhaseResult(
                        "overrideFinalDamage",
                        DamagePhase.FINAL_OVERRIDE
                );

        if (!phaseResult.applied()) {
            return phaseResult;
        }

        if (!isFinite(amount)) {
            return reject(
                    "overrideFinalDamage",
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite amount"
            );
        }

        float safeAmount = Math.max(0.0f, amount);
        result.setFinalEventDamage(safeAmount);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.FINAL_OVERRIDE,
                safeAmount
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryCancelDamage(String sourceId) {
        if (phaseState.defensiveLocked()
                && phaseState.currentPhase() != DamagePhase.FINAL_OVERRIDE) {
            trace.mutations().mutation(
                    sourceId,
                    phaseState.currentPhase(),
                    DamageMutationType.CANCEL_DAMAGE,
                    0.0f
            );

            return DamageMutationResult.REJECTED_DEFENSE_LOCKED;
        }

        result.cancel(sourceId);

        trace.mutations().mutation(
                sourceId,
                phaseState.currentPhase(),
                DamageMutationType.CANCEL_DAMAGE,
                0.0f
        );

        return DamageMutationResult.APPLIED;
    }

    public DamageMutationResult tryAddVanillaBaseReconstructedDamage(
            DamageChannel channel,
            DamageApplicationBucket bucket,
            float value,
            String sourceId
    ) {
        DamageMutationResult phaseResult =
                requirePhaseResult(
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
                requirePhaseResult(
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
        DamageMutationResult offenseResult =
                canModifyOffenseResult(operationName);

        if (!offenseResult.applied()) {
            return offenseResult;
        }

        if (channel == null) {
            return reject(
                    operationName,
                    DamageMutationResult.REJECTED_NULL_CHANNEL,
                    "null channel"
            );
        }

        if (!isFinite(value)) {
            return reject(
                    operationName,
                    DamageMutationResult.REJECTED_NON_FINITE,
                    "non-finite value"
            );
        }

        if (value == 0.0f) {
            return DamageMutationResult.NO_OP_ZERO;
        }

        DamageApplicationBucket effectiveBucket =
                bucket != null
                        ? bucket
                        : DamageApplicationBucket.DN_RULE_BASE;

        getOrCreateComponent(channel).addBase(effectiveBucket, value);

        trace.mutations().baseDamage(
                sourceId,
                phaseState.currentPhase(),
                effectiveBucket,
                value
        );

        return DamageMutationResult.APPLIED;
    }

    /*
     * ---------------------------------------------------------------------
     * State / vanilla accessors
     * ---------------------------------------------------------------------
     */

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

    void setCurrentProcessingPhase(DamagePhase phase) {
        this.phaseState.setCurrentPhase(phase);
    }

    public DamagePhase getCurrentProcessingPhase() {
        return phaseState.currentPhase();
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

    /*
     * ---------------------------------------------------------------------
     * Finalization
     * ---------------------------------------------------------------------
     */

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
            logRejectedMutationStatic(
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

    /*
     * ---------------------------------------------------------------------
     * Internal helpers
     * ---------------------------------------------------------------------
     */

    private void emitContributions() {
        if (!trace.enabled()) {
            return;
        }

        if (contributions.isEmpty()) {
            return;
        }

        trace.contributions().emit(
                contributions.applied(),
                contributions.rejected()
        );
    }

    private boolean isValidPreModifierId(int modifierId) {
        return modifierId >= 0
                && modifierId < PreMultiplierBucketRegistry.bucketCount();
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

    private DamageMutationResult reject(
            String action,
            DamageMutationResult result,
            String staticReason
    ) {
        logRejectedMutationStatic(
                action,
                staticReason
        );

        return result;
    }

    private DamageMutationResult rejectInvalidPreBucket(
            String action,
            int modifierId
    ) {
        logRejectedMutationInvalidPreBucket(
                action,
                modifierId
        );

        return DamageMutationResult.REJECTED_INVALID_PRE_MULTIPLIER_BUCKET;
    }

    private DamageMutationResult rejectInactiveChannel(
            String action,
            DamageChannel channel
    ) {
        logRejectedMutationInactiveChannel(
                action,
                channel
        );

        return DamageMutationResult.REJECTED_INACTIVE_CHANNEL;
    }

    private DamageMutationResult canModifyOffenseResult(String action) {
        if (!phaseState.offensiveLocked()) {
            return DamageMutationResult.APPLIED;
        }

        return reject(
                action,
                DamageMutationResult.REJECTED_OFFENSE_LOCKED,
                "offensive damage already finalized"
        );
    }

    private DamageMutationResult canModifyDefenseResult(String action) {
        if (!phaseState.defensiveLocked()) {
            return DamageMutationResult.APPLIED;
        }

        return reject(
                action,
                DamageMutationResult.REJECTED_DEFENSE_LOCKED,
                "defensive damage already calculated"
        );
    }

    private DamageMutationResult requirePhaseResult(
            String action,
            DamagePhase expected
    ) {
        if (phaseState.currentPhase() == expected) {
            return DamageMutationResult.APPLIED;
        }

        logRejectedMutationExpectedPhase(
                action,
                expected
        );

        return DamageMutationResult.REJECTED_WRONG_PHASE;
    }

    private DamageMutationResult requireMultiplierPhaseResult(String action) {
        if (phaseState.currentPhase() == DamagePhase.TYPE_SCALING
                || phaseState.currentPhase() == DamagePhase.CRITICAL_HIT
                || phaseState.currentPhase() == DamagePhase.CONDITIONAL_MULTI
                || phaseState.currentPhase() == DamagePhase.GLOBAL_ADJUSTMENT) {
            return DamageMutationResult.APPLIED;
        }

        logRejectedMutationStatic(
                action,
                "expected phase TYPE_SCALING, CRITICAL_HIT, CONDITIONAL_MULTI, or GLOBAL_ADJUSTMENT"
        );

        return DamageMutationResult.REJECTED_WRONG_PHASE;
    }

    private DamageMutationResult requireGlobalPostMultiplierPhaseResult(
            String action
    ) {
        if (phaseState.currentPhase() == DamagePhase.CONDITIONAL_MULTI
                || phaseState.currentPhase() == DamagePhase.GLOBAL_ADJUSTMENT) {
            return DamageMutationResult.APPLIED;
        }

        logRejectedMutationStatic(
                action,
                "expected phase CONDITIONAL_MULTI or GLOBAL_ADJUSTMENT"
        );

        return DamageMutationResult.REJECTED_WRONG_PHASE;
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

    private void logRejectedMutationStatic(
            String action,
            String reason
    ) {
        if (!trace.enabled()) {
            return;
        }

        trace.mutations().rejected(
                action,
                phaseState.currentPhase(),
                reason
        );
    }

    private void logRejectedMutationInvalidPreBucket(
            String action,
            int modifierId
    ) {
        if (!trace.enabled()) {
            return;
        }

        trace.mutations().rejected(
                action,
                phaseState.currentPhase(),
                "invalid pre-multiplier bucket id " + modifierId
        );
    }

    private void logRejectedMutationInactiveChannel(
            String action,
            DamageChannel channel
    ) {
        if (!trace.enabled()) {
            return;
        }

        trace.mutations().rejected(
                action,
                phaseState.currentPhase(),
                "target channel is not active: "
                        + (channel == null ? "<null>" : channel.id())
        );
    }

    private void logRejectedMutationExpectedPhase(
            String action,
            DamagePhase expected
    ) {
        if (!trace.enabled()) {
            return;
        }

        trace.mutations().rejected(
                action,
                phaseState.currentPhase(),
                "expected phase " + expected
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

    private static boolean isFinite(float value) {
        return !Float.isNaN(value)
                && !Float.isInfinite(value);
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

    private final DamageContributionCollector contributions =
            new DamageContributionCollector();

    public DamageContributionCollector contributions() {
        return contributions;
    }

}
