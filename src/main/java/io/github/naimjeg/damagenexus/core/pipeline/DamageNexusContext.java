package io.github.naimjeg.damagenexus.core.pipeline;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.ICombatLogger;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.core.trace.DamageMutationType;
import io.github.naimjeg.damagenexus.event.neoforge.VanillaCritHandler;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;

import java.util.concurrent.atomic.AtomicLong;

public class DamageNexusContext {

    public final LivingIncomingDamageEvent neoforgeEvent;
    public final LivingEntity attacker;
    public final LivingEntity victim;
    public final DamageSource source;
    public final boolean isManaged;
    public final boolean isVanillaJumpCrit;

    public final float eventOriginalAmount;
    public final float initialBaseAmount;

    private final VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot;

    private final DamageComponent[] damagePacket =
            new DamageComponent[DamageChannelRegistry.channelCount()];

    private final int[] activeChannelIndexes =
            new int[damagePacket.length];

    private int activeChannelCount = 0;

    private float[] globalPreMultipliers = null;
    private FloatArrayList globalPostMultipliers = null;
    private FloatArrayList globalMitigations = null;

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
        this(event, attacker, victim, event.getOriginalAmount(), null);
    }

    public DamageNexusContext(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim,
            float initialBaseAmount,
            VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot
    ) {
        this.neoforgeEvent = event;
        this.attacker = attacker;
        this.victim = victim;
        this.source = event.getSource();

        this.eventOriginalAmount = event.getOriginalAmount();
        this.initialBaseAmount = isFinite(initialBaseAmount)
                ? Math.max(0.0f, initialBaseAmount)
                : Math.max(0.0f, event.getOriginalAmount());

        this.vanillaSnapshot = vanillaSnapshot;

        this.damageId = DAMAGE_ID_COUNTER.incrementAndGet();

        this.debugger = ModConfig.isDebugMode()
                ? new ICombatLogger.ActiveLogger(this.damageId)
                : ICombatLogger.NO_OP;

        this.isManaged = checkCompatibility(event.getSource());

        if (attacker instanceof Player) {
            int pendingTargetId = VanillaCritHandler.PENDING_CRIT_TARGET.get();
            this.isVanillaJumpCrit = pendingTargetId == victim.getId();

            VanillaCritHandler.PENDING_CRIT_TARGET.set(-1);
        } else {
            this.isVanillaJumpCrit = false;
        }

        DamageChannel initialChannel =
                DamageChannelRegistry.determineInitialChannel(this.source);

        getOrCreateComponent(initialChannel).addBase(this.initialBaseAmount);

        if (this.debugger.enabled()) {
            this.debugger.logBegin(
                    getEntityLogName(attacker, "Environment"),
                    getEntityLogName(victim, "Unknown"),
                    getDamageSourceId(this.source),
                    initialChannel.id().toString(),
                    this.initialBaseAmount
            );
        }
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

    /*
     * ---------------------------------------------------------------------
     * Offensive canonical API
     * ---------------------------------------------------------------------
     */

    public void addBaseDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
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

        if (value == 0.0f) {
            return;
        }

        getOrCreateComponent(channel).addBase(value);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.BASE_DAMAGE,
                value
        );
    }

    public void addChannelPreMultiplier(
            DamageChannel channel,
            int modifierId,
            float value,
            String sourceId
    ) {
        if (!canModifyOffense("addChannelPreMultiplier")) {
            return;
        }

        if (!requireMultiplierPhase("addChannelPreMultiplier")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addChannelPreMultiplier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        ensureGlobalPreCapacity();

        if (!isValidPreModifierId(modifierId)) {
            debugger.logRejectedMutation(
                    "addChannelPreMultiplier",
                    currentProcessingPhase,
                    "invalid pre bucket id " + modifierId
            );
            return;
        }

        if (value == 0.0f) {
            return;
        }

        getOrCreateComponent(channel).addPreMultiplier(modifierId, value);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.CHANNEL_PRE_MULTIPLIER,
                value
        );
    }

    public void addChannelPostMultiplier(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        if (!canModifyOffense("addChannelPostMultiplier")) {
            return;
        }

        if (!requireMultiplierPhase("addChannelPostMultiplier")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addChannelPostMultiplier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        if (value == 0.0f) {
            return;
        }

        getOrCreateComponent(channel).addPostMultiplier(value);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.CHANNEL_POST_MULTIPLIER,
                value
        );
    }

    public void addGlobalPreMultiplier(
            int modifierId,
            float value,
            String sourceId
    ) {
        if (!canModifyOffense("addGlobalPreMultiplier")) {
            return;
        }

        if (!requireMultiplierPhase("addGlobalPreMultiplier")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addGlobalPreMultiplier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        ensureGlobalPreCapacity();

        if (!isValidPreModifierId(modifierId)) {
            debugger.logRejectedMutation(
                    "addGlobalPreMultiplier",
                    currentProcessingPhase,
                    "invalid pre bucket id " + modifierId
            );
            return;
        }

        if (value == 0.0f) {
            return;
        }

        globalPreMultipliers[modifierId] += value;

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.GLOBAL_PRE_MULTIPLIER,
                value
        );
    }

    public void addGlobalPostMultiplier(
            float value,
            String sourceId
    ) {
        if (!canModifyOffense("addGlobalPostMultiplier")) {
            return;
        }

        if (!isGlobalPostMultiplierPhase()) {
            debugger.logRejectedMutation(
                    "addGlobalPostMultiplier",
                    currentProcessingPhase,
                    "expected phase CONDITIONAL_MULTI or GLOBAL_ADJUSTMENT"
            );
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addGlobalPostMultiplier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        if (value == 0.0f) {
            return;
        }

        if (globalPostMultipliers == null) {
            globalPostMultipliers = new FloatArrayList(4);
        }

        globalPostMultipliers.add(value);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.GLOBAL_POST_MULTIPLIER,
                value
        );
    }

    public void convertDamage(
            DamageChannel from,
            DamageChannel to,
            float ratio,
            String sourceId
    ) {
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

        float clampedRatio = Math.max(0.0f, Math.min(1.0f, ratio));

        if (clampedRatio == 0.0f) {
            return;
        }

        DamageComponent sourceComponent = getOrCreateComponent(from);
        float amountToConvert = sourceComponent.getBaseAmount() * clampedRatio;

        if (amountToConvert <= 0.0f) {
            return;
        }

        sourceComponent.addBase(-amountToConvert);
        getOrCreateComponent(to).addBase(amountToConvert);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.CONVERSION,
                amountToConvert
        );
    }

    public void gainDamageAsExtra(
            DamageChannel basedOn,
            DamageChannel to,
            float ratio,
            String sourceId
    ) {
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

        float safeRatio = Math.max(0.0f, ratio);

        if (safeRatio == 0.0f) {
            return;
        }

        DamageComponent sourceComponent = getOrCreateComponent(basedOn);
        float extraAmount = sourceComponent.getBaseAmount() * safeRatio;

        if (extraAmount <= 0.0f) {
            return;
        }

        getOrCreateComponent(to).addBase(extraAmount);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.BASE_DAMAGE,
                extraAmount
        );

    }

    /*
     * ---------------------------------------------------------------------
     * Defensive canonical API
     * ---------------------------------------------------------------------
     */

    public void multiplyArmorEffectiveness(
            float multiplier,
            String sourceId
    ) {
        if (!canModifyDefense("multiplyArmorEffectiveness")) {
            return;
        }

        if (!requirePhase("multiplyArmorEffectiveness", DamagePhase.MITIGATION_SETUP)) {
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
        this.armorEffectivenessMultiplier =
                Math.max(0.0f, this.armorEffectivenessMultiplier);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.ARMOR_EFFECTIVENESS_MULTIPLIER,
                safeMultiplier
        );
    }

    public float getArmorEffectivenessMultiplier() {
        return this.armorEffectivenessMultiplier;
    }

    public void addTemporaryResistance(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        if (!canModifyDefense("addTemporaryResistance")) {
            return;
        }

        if (!requirePhase("addTemporaryResistance", DamagePhase.MITIGATION_SETUP)) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addTemporaryResistance",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        if (value == 0.0f) {
            return;
        }

        DamageComponent component = findActiveComponent(channel);

        if (component == null) {
            debugger.logRejectedMutation(
                    "addTemporaryResistance",
                    currentProcessingPhase,
                    "target channel is not active: " + channel.id()
            );
            return;
        }

        component.addTemporaryResistance(value);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.TEMPORARY_RESISTANCE,
                value
        );
    }

    public void addChannelMitigation(
            DamageChannel channel,
            float reductionPercent,
            String sourceId
    ) {
        if (!canModifyDefense("addChannelMitigation")) {
            return;
        }

        if (!requirePhase("addChannelMitigation", DamagePhase.MITIGATION_SETUP)) {
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

        if (safeReduction == 0.0f) {
            return;
        }

        DamageComponent component = findActiveComponent(channel);

        if (component == null) {
            debugger.logRejectedMutation(
                    "addChannelMitigation",
                    currentProcessingPhase,
                    "target channel is not active: " + channel.id()
            );
            return;
        }

        component.addMitigation(safeReduction);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.CHANNEL_MITIGATION,
                safeReduction
        );
    }

    public void addGlobalMitigation(
            float reductionPercent,
            String sourceId
    ) {
        if (!canModifyDefense("addGlobalMitigation")) {
            return;
        }

        if (!requirePhase("addGlobalMitigation", DamagePhase.MITIGATION_SETUP)) {
            return;
        }

        if (!isFinite(reductionPercent)) {
            debugger.logRejectedMutation(
                    "addGlobalMitigation",
                    currentProcessingPhase,
                    "non-finite reduction"
            );
            return;
        }

        float safeReduction = Math.max(-1.0f, Math.min(1.0f, reductionPercent));

        if (safeReduction == 0.0f) {
            return;
        }

        if (globalMitigations == null) {
            globalMitigations = new FloatArrayList(4);
        }

        globalMitigations.add(safeReduction);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.GLOBAL_MITIGATION,
                safeReduction
        );
    }

    public void overrideFinalDamage(
            float amount,
            String sourceId
    ) {
        if (!requirePhase("overrideFinalDamage", DamagePhase.FINAL_OVERRIDE)) {
            return;
        }

        if (!isFinite(amount)) {
            debugger.logRejectedMutation(
                    "overrideFinalDamage",
                    currentProcessingPhase,
                    "non-finite amount"
            );
            return;
        }

        this.finalEventDamage = Math.max(0.0f, amount);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.FINAL_OVERRIDE,
                this.finalEventDamage
        );
    }

    public VanillaDamageCapture.OffensiveSnapshot getVanillaSnapshot() {
        return vanillaSnapshot;
    }

    public float getEventOriginalAmount() {
        return eventOriginalAmount;
    }

    public float getInitialBaseAmount() {
        return initialBaseAmount;
    }

    /*
     * ---------------------------------------------------------------------
     * State flags
     * ---------------------------------------------------------------------
     */

    public void markCritical() {
        this.isCritical = true;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

    public void setArmorHandled() {
        this.armorHandled = true;
    }

    public boolean isArmorHandled() {
        return this.armorHandled;
    }

    void setCurrentProcessingPhase(DamagePhase phase) {
        this.currentProcessingPhase = phase;
    }

    public DamagePhase getCurrentProcessingPhase() {
        return currentProcessingPhase;
    }

    public float getCalculatedFinalDamage() {
        return this.finalEventDamage;
    }

    /*
     * ---------------------------------------------------------------------
     * Attribute helpers
     * ---------------------------------------------------------------------
     */

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

    public void finalizeOffensiveDamage() {
        if (offensiveLocked) {
            return;
        }

        float finalTotal = 0.0f;

        debugger.logCalculationStart();

        for (int i = 0; i < activeChannelCount; i++) {
            DamageComponent component = damagePacket[activeChannelIndexes[i]];

            component.calculateFinalOffensive(
                    globalPreMultipliers,
                    globalPostMultipliers
            );

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

        if (!isFinite(finalTotal)) {
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

        if (!isFinite(finalMitigatedTotal)) {
            finalMitigatedTotal = 0.0f;
        }

        this.finalEventDamage = Math.max(0.0f, finalMitigatedTotal);
        this.defenseCalculated = true;
        this.defensiveLocked = true;
    }

    public void applyIncomingDamageToEvent() {
        if (!defenseCalculated) {
            debugger.logRejectedMutation(
                    "applyIncomingDamageToEvent",
                    currentProcessingPhase,
                    "defensive damage has not been calculated"
            );
            return;
        }

        neoforgeEvent.setAmount(this.finalEventDamage);
        debugger.logDefensiveSummary(this.finalEventDamage);

        suppressVanillaReductions();
    }

    /*
     * ---------------------------------------------------------------------
     * Internal helpers
     * ---------------------------------------------------------------------
     */

    private DamageComponent findActiveComponent(DamageChannel rawChannel) {
        DamageChannel channel = DamageChannelRegistry.resolve(rawChannel);

        for (int i = 0; i < activeChannelCount; i++) {
            DamageComponent component = damagePacket[activeChannelIndexes[i]];

            if (component.channel.equals(channel)) {
                return component;
            }
        }

        return null;
    }

    private void ensureGlobalPreCapacity() {
        PreMultiplierBucketRegistry.requireFrozen();

        if (globalPreMultipliers == null) {
            globalPreMultipliers =
                    new float[PreMultiplierBucketRegistry.bucketCount()];
        }
    }

    private boolean isValidPreModifierId(int modifierId) {
        return modifierId >= 0
                && modifierId < PreMultiplierBucketRegistry.bucketCount();
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

    private boolean canModifyDefense(String action) {
        if (!defensiveLocked) {
            return true;
        }

        debugger.logRejectedMutation(
                action,
                currentProcessingPhase,
                "defensive damage already calculated"
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
        if (currentProcessingPhase == DamagePhase.TYPE_SCALING
                || currentProcessingPhase == DamagePhase.CRITICAL_HIT
                || currentProcessingPhase == DamagePhase.CONDITIONAL_MULTI
                || currentProcessingPhase == DamagePhase.GLOBAL_ADJUSTMENT) {
            return true;
        }

        debugger.logRejectedMutation(
                action,
                currentProcessingPhase,
                "expected phase TYPE_SCALING, CRITICAL_HIT, CONDITIONAL_MULTI, or GLOBAL_ADJUSTMENT"
        );

        return false;
    }

    private boolean isGlobalPostMultiplierPhase() {
        return currentProcessingPhase == DamagePhase.CONDITIONAL_MULTI
                || currentProcessingPhase == DamagePhase.GLOBAL_ADJUSTMENT;
    }

    private boolean checkCompatibility(DamageSource source) {
        return source.getEntity() != null
                || source.is(DamageTypeTags.IS_FIRE);
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

    private static String getEntityLogName(
            LivingEntity entity,
            String fallback
    ) {
        return entity != null
                ? entity.getName().getString()
                : fallback;
    }

    private static String getDamageSourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("unknown");
    }

    private static boolean isFinite(float value) {
        return !Float.isNaN(value)
                && !Float.isInfinite(value);
    }
}