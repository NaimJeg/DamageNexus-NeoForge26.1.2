package io.github.naimjeg.damagenexus.core.pipeline;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.PreEventDeltaKind;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageSourceProfile;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.ICombatLogger;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.core.trace.DamageMutationType;
import io.github.naimjeg.damagenexus.core.trace.DamageNexusTransactionTracker;
import io.github.naimjeg.damagenexus.event.neoforge.VanillaCritHandler;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class DamageNexusContext {

    private static final Logger LOGGER = LogUtils.getLogger();

    public final LivingIncomingDamageEvent neoforgeEvent;
    public final LivingEntity attacker;
    public final LivingEntity victim;
    public final DamageSource source;
    public final boolean isManaged;
    public final boolean isVanillaJumpCrit;

    private final DamageApplicationBucket initialBaseBucket;
    private final DamageApplicationBucket vanillaOffensiveMobEffectBucket;
    private final DamageApplicationBucket vanillaOffensiveEnchantmentBucket;

    private boolean damageCancelled = false;
    private String cancelSourceId = null;

    private final float vanillaOffensiveMobEffectDelta;

    private final boolean rebuildVanillaOffensiveMobEffects;

    public final float eventOriginalAmount;
    public final float initialBaseAmount;

    private final DamageChannel initialChannel;

    public final float victimHealthBefore;
    public final float victimAbsorptionBefore;
    public final int victimInvulnerableTimeBefore;
    public final long gameTime;
    private float offensiveTotal = 0.0f;

    private final VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot;

    private final boolean rebuildVanillaOffensiveEnchantment;
    private final boolean rebuildVanillaPreEventDelta;

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

    private final VanillaDamageSourceProfile vanillaSourceProfile;

    private final Identifier sourceTypeId;
    private final String sourceMsgId;

    public final ICombatLogger debugger;

    private static final Identifier MACE_SMASH_ID =
            Identifier.fromNamespaceAndPath("minecraft", "mace_smash");

    private static final Identifier SPEAR_ID =
            Identifier.fromNamespaceAndPath("minecraft", "spear");

    private static final String MACE_SMASH_MSG_ID = "mace_smash";
    private static final String SPEAR_MSG_ID = "spear";

    private static final AtomicLong DAMAGE_ID_COUNTER = new AtomicLong();
    public final long damageId;

    public DamageNexusContext(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim
    ) {
        this(
                event,
                attacker,
                victim,
                event.getOriginalAmount(),
                null,
                false,
                false,
                false,
                0.0f
        );
    }

    public record DamageNexusTransaction(
            long damageId,
            LivingEntity attacker,
            LivingEntity victim,
            DamageSource source,

            float eventOriginalAmount,
            float initialBaseAmount,
            float offensiveTotal,
            float finalEventAmount,

            float eventAmountBeforeSet,
            float eventAmountAfterSet,

            float victimHealthBefore,
            float victimAbsorptionBefore,
            int victimInvulnerableTimeBefore,
            long gameTime
    ) {}

    public DamageNexusContext(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim,
            float initialBaseAmount,
            VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot,
            boolean rebuildVanillaOffensiveMobEffects,
            boolean rebuildVanillaOffensiveEnchantment,
            boolean rebuildVanillaPreEventDelta,
            float vanillaOffensiveMobEffectDelta
    ) {
        this(
                event,
                attacker,
                victim,
                initialBaseAmount,
                vanillaSnapshot,
                rebuildVanillaOffensiveMobEffects,
                rebuildVanillaOffensiveEnchantment,
                rebuildVanillaPreEventDelta,
                vanillaOffensiveMobEffectDelta,
                null,
                null,
                null
        );
    }


    public DamageNexusContext(
            LivingIncomingDamageEvent event,
            LivingEntity attacker,
            LivingEntity victim,
            float initialBaseAmount,
            VanillaDamageCapture.OffensiveSnapshot vanillaSnapshot,
            boolean rebuildVanillaOffensiveMobEffects,
            boolean rebuildVanillaOffensiveEnchantment,
            boolean rebuildVanillaPreEventDelta,
            float vanillaOffensiveMobEffectDelta,
            DamageApplicationBucket initialBaseBucket,
            DamageApplicationBucket vanillaOffensiveMobEffectBucket,
            DamageApplicationBucket vanillaOffensiveEnchantmentBucket
    ) {
        this.neoforgeEvent = event;
        this.attacker = attacker;
        this.victim = victim;
        this.source = event.getSource();

        this.vanillaSourceProfile =
                VanillaDamageSourceProfile.create(
                        this.source,
                        attacker,
                        victim
                );

        this.initialBaseBucket =
                initialBaseBucket != null
                        ? initialBaseBucket
                        : defaultInitialBaseBucket(this.vanillaSourceProfile);

        this.vanillaOffensiveMobEffectBucket =
                vanillaOffensiveMobEffectBucket != null
                        ? vanillaOffensiveMobEffectBucket
                        : DamageApplicationBucket.VANILLA_MELEE_BASE;

        this.vanillaOffensiveEnchantmentBucket =
                vanillaOffensiveEnchantmentBucket != null
                        ? vanillaOffensiveEnchantmentBucket
                        : defaultOffensiveEnchantmentBucket(this.vanillaSourceProfile);

        this.eventOriginalAmount = event.getOriginalAmount();
        this.initialBaseAmount = isFinite(initialBaseAmount)
                ? Math.max(0.0f, initialBaseAmount)
                : Math.max(0.0f, event.getOriginalAmount());

        this.victimHealthBefore = victim.getHealth();
        this.victimAbsorptionBefore = victim.getAbsorptionAmount();
        this.victimInvulnerableTimeBefore = victim.invulnerableTime;
        this.gameTime = victim.level().getGameTime();

        this.vanillaSnapshot = vanillaSnapshot;
        this.rebuildVanillaOffensiveEnchantment = rebuildVanillaOffensiveEnchantment;
        this.rebuildVanillaPreEventDelta = rebuildVanillaPreEventDelta;

        this.damageId = DAMAGE_ID_COUNTER.incrementAndGet();

        this.debugger = ModConfig.isDebugMode()
                ? new ICombatLogger.ActiveLogger(this.damageId)
                : ICombatLogger.NO_OP;

        this.isManaged = checkCompatibility(event.getSource());

        this.initialChannel =
                DamageChannelRegistry.determineInitialChannel(this.source);

        this.sourceTypeId = this.source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElse(null);

        this.sourceMsgId = this.source.type().msgId();

        this.rebuildVanillaOffensiveMobEffects = rebuildVanillaOffensiveMobEffects;
        this.vanillaOffensiveMobEffectDelta =
                Float.isFinite(vanillaOffensiveMobEffectDelta)
                        ? vanillaOffensiveMobEffectDelta
                        : 0.0f;

        if (attacker instanceof Player) {
            int pendingTargetId = VanillaCritHandler.pendingTargetId();
            this.isVanillaJumpCrit = pendingTargetId == victim.getId();

            VanillaCritHandler.clear();
        } else {
            this.isVanillaJumpCrit = false;
        }

        if (this.debugger.enabled()) {
            this.debugger.logBegin(
                    getEntityLogName(attacker, "Environment"),
                    getEntityLogName(victim, "Unknown"),
                    getDamageSourceId(this.source),
                    initialChannel.id().toString(),
                    this.eventOriginalAmount,
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
            DamageApplicationBucket bucket,
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

        getOrCreateComponent(channel).addBase(bucket, value);

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.BASE_DAMAGE,
                value
        );
    }

    public void addBaseDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        addBaseDamage(
                channel,
                DamageApplicationBucket.DN_RULE_BASE,
                value,
                sourceId
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
                    "invalid pre preMultiplierBucketId id " + modifierId
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
                    "invalid pre preMultiplierBucketId id " + modifierId
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

    public void addApplicationPreMultiplier(
            DamageApplicationBucket bucket,
            int modifierId,
            float value,
            String sourceId
    ) {
        if (!canModifyOffense("addApplicationPreMultiplier")) {
            return;
        }

        if (!requireMultiplierPhase("addApplicationPreMultiplier")) {
            return;
        }

        if (!isFinite(value)) {
            debugger.logRejectedMutation(
                    "addApplicationPreMultiplier",
                    currentProcessingPhase,
                    "non-finite value"
            );
            return;
        }

        ensureGlobalPreCapacity();

        if (!isValidPreModifierId(modifierId)) {
            debugger.logRejectedMutation(
                    "addApplicationPreMultiplier",
                    currentProcessingPhase,
                    "invalid pre preMultiplierBucketId id " + modifierId
            );
            return;
        }

        if (value == 0.0f) {
            return;
        }

        for (int i = 0; i < activeChannelCount; i++) {
            DamageComponent component = damagePacket[activeChannelIndexes[i]];
            component.addApplicationPreMultiplier(bucket, modifierId, value);
        }

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.GLOBAL_PRE_MULTIPLIER,
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

        if (!requirePhase("convertDamage", DamagePhase.TYPE_SCALING)) {
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

        DamageComponent sourceComponent = findActiveComponent(from);

        if (sourceComponent == null) {
            return;
        }

        DamageComponent targetComponent = getOrCreateComponent(to);

        float amountToConvert =
                sourceComponent.convertBaseTo(
                        targetComponent,
                        clampedRatio
                );

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

    public void addTrueDamage(
            DamageChannel channel,
            float value,
            String sourceId
    ) {
        addBaseDamage(
                channel,
                DamageApplicationBucket.DN_TRUE_DAMAGE,
                value,
                sourceId
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

        if (!requirePhase("gainDamageAsExtra", DamagePhase.TYPE_SCALING)) {
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

        DamageComponent sourceComponent = findActiveComponent(basedOn);

        if (sourceComponent == null) {
            return;
        }

        float extraAmount = sourceComponent.getBaseAmount() * safeRatio;

        if (extraAmount <= 0.0f) {
            return;
        }

        getOrCreateComponent(to).addBase(
                DamageApplicationBucket.DN_RULE_BASE,
                extraAmount
        );

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

    public void cancelDamage(String sourceId) {
        if (defensiveLocked) {
            debugger.logRejectedMutation(
                    "cancelDamage",
                    currentProcessingPhase,
                    "defensive damage already calculated"
            );
            return;
        }

        this.damageCancelled = true;
        this.cancelSourceId = sourceId;
        this.finalEventDamage = 0.0f;

        debugger.logMutation(
                sourceId,
                currentProcessingPhase,
                DamageMutationType.FINAL_OVERRIDE,
                0.0f
        );
    }

    public boolean isDamageCancelled() {
        return damageCancelled;
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

    public VanillaDamageSourceProfile vanillaSourceProfile() {
        return vanillaSourceProfile;
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

    void finalizeOffensiveDamage() {
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

        this.offensiveTotal = Math.max(0.0f, finalTotal);

        debugger.logOffensiveSummary(Math.max(0.0f, finalTotal));
    }

    void calculateDefensiveDamage() {
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

    void applyIncomingDamageToEvent() {
        if (!defenseCalculated) {
            debugger.logRejectedMutation(
                    "applyIncomingDamageToEvent",
                    currentProcessingPhase,
                    "defensive damage has not been calculated"
            );
            return;
        }

        float eventAmountBeforeSet = neoforgeEvent.getAmount();

        neoforgeEvent.setAmount(this.finalEventDamage);

        float eventAmountAfterSet = neoforgeEvent.getAmount();

        debugger.logApply(
                eventOriginalAmount,
                initialBaseAmount,
                offensiveTotal,
                finalEventDamage
        );

        DamageNexusTransactionTracker.record(
                new DamageNexusTransaction(
                        damageId,
                        attacker,
                        victim,
                        source,

                        eventOriginalAmount,
                        initialBaseAmount,
                        offensiveTotal,
                        finalEventDamage,

                        eventAmountBeforeSet,
                        eventAmountAfterSet,

                        victimHealthBefore,
                        victimAbsorptionBefore,
                        victimInvulnerableTimeBefore,
                        gameTime
                )
        );

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

    public boolean suppressesDefaultCritical() {
        return isVanillaSpearAttack()
                || isVanillaMaceSmash();
    }

    public boolean isVanillaMaceSmash() {
        return hasPreEventKind(PreEventDeltaKind.SPECIAL_ATTACK_SCALING)
                || isDamageSource(MACE_SMASH_ID)
                || isDamageSourceMsg(MACE_SMASH_MSG_ID);
    }

    public boolean isVanillaSpearAttack() {
        VanillaDamageCapture.OffensiveSnapshot snapshot = getVanillaSnapshot();

        if (snapshot != null) {
            if (snapshot.weapon().has(DataComponents.KINETIC_WEAPON)) {
                return true;
            }

            return switch (snapshot.preEventDelta().kind()) {
                case SPEAR_STAB_BONUS,
                     SPEAR_CHARGE_BONUS,
                     SPEAR_ATTACK_BONUS -> true;
                default -> false;
            };
        }

        return isDamageSource(SPEAR_ID)
                || isDamageSourceMsg(SPEAR_MSG_ID);
    }

    private boolean hasPreEventKind(PreEventDeltaKind kind) {
        VanillaDamageCapture.OffensiveSnapshot snapshot = getVanillaSnapshot();

        return snapshot != null
                && snapshot.preEventDelta().kind() == kind;
    }

    private boolean isDamageSource(Identifier expectedId) {
        return sourceTypeId != null && sourceTypeId.equals(expectedId);
    }

    private boolean isDamageSourceMsg(String expectedMsgId) {
        return expectedMsgId.equals(sourceMsgId);
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

    public float getOffensiveTotal() {
        return offensiveTotal;
    }

    public float getFinalEventDamage() {
        return finalEventDamage;
    }

    public boolean shouldRebuildVanillaOffensiveEnchantment() {
        return rebuildVanillaOffensiveEnchantment;
    }

    public boolean shouldRebuildVanillaPreEventDelta() {
        return rebuildVanillaPreEventDelta;
    }

    private static String getEntityLogName(
            LivingEntity entity,
            String fallback
    ) {
        return entity != null
                ? entity.getName().getString()
                : fallback;
    }

    void applyCancelledDamageToEvent() {
        this.finalEventDamage = 0.0f;
        this.offensiveTotal = 0.0f;
        this.defenseCalculated = true;
        this.defensiveLocked = true;
        applyIncomingDamageToEvent();
    }


    public DamageChannel getInitialChannel() {
        return initialChannel;
    }

    private static String getDamageSourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("unknown");
    }

    public boolean shouldRebuildVanillaOffensiveMobEffects() {
        return rebuildVanillaOffensiveMobEffects;
    }

    public float getVanillaOffensiveMobEffectDelta() {
        return vanillaOffensiveMobEffectDelta;
    }


    private static DamageApplicationBucket defaultInitialBaseBucket(
            VanillaDamageSourceProfile profile
    ) {
        if (profile == null) {
            return DamageApplicationBucket.DN_RULE_BASE;
        }

        if (profile.projectile()) {
            return DamageApplicationBucket.VANILLA_PROJECTILE_BASE;
        }

        if (profile.shouldApplyMeleeOffensiveMobEffects()
                || profile.directLivingAttack()) {
            return DamageApplicationBucket.VANILLA_MELEE_BASE;
        }

        return DamageApplicationBucket.DN_RULE_BASE;
    }

    private static DamageApplicationBucket defaultOffensiveEnchantmentBucket(
            VanillaDamageSourceProfile profile
    ) {
        if (profile != null && profile.projectile()) {
            return DamageApplicationBucket.VANILLA_PROJECTILE_ENCHANTMENT;
        }

        return DamageApplicationBucket.VANILLA_MELEE_ENCHANTMENT;
    }

    private static boolean isFinite(float value) {
        return !Float.isNaN(value)
                && !Float.isInfinite(value);
    }

    public DamageApplicationBucket getInitialBaseBucket() {
        return initialBaseBucket;
    }

    public DamageApplicationBucket getVanillaOffensiveMobEffectBucket() {
        return vanillaOffensiveMobEffectBucket;
    }

    public DamageApplicationBucket getVanillaOffensiveEnchantmentBucket() {
        return vanillaOffensiveEnchantmentBucket;
    }
}
