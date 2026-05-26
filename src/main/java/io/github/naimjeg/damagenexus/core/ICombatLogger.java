package io.github.naimjeg.damagenexus.core;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.core.rule.StackingTrace;
import io.github.naimjeg.damagenexus.core.trace.DamageMutationType;
import io.github.naimjeg.damagenexus.core.trace.RuleSkipReason;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface ICombatLogger {

    boolean enabled();

    void logBegin(
            String attackerName,
            String victimName,
            String sourceId,
            String initialChannel,
            float originalAmount
    );

    void logPhase(DamagePhase phase);

    void logProcessorRun(
            DamagePhase phase,
            DamagePhaseProcessor processor
    );

    void logProcessorSkip(
            DamagePhase phase,
            DamagePhaseProcessor processor
    );

    void logRuleCollected(
            DamagePhase phase,
            DamageRuleDefinition rule,
            RuleExecutionContext exec
    );

    void logRuleSkipped(
            DamagePhase phase,
            DamageRuleDefinition rule,
            RuleSkipReason reason
    );

    void logRulePhaseMismatch(
            DamagePhase runningPhase,
            DamageRuleDefinition rule
    );

    void logRuleRoleMismatch(
            DamagePhase phase,
            DamageRuleDefinition rule,
            RuleExecutionContext exec
    );

    void logRuleConditionFailed(
            DamagePhase phase,
            DamageRuleDefinition rule,
            DamageRuleCondition condition
    );

    void logRuleExecuted(
            DamagePhase phase,
            DamageRuleDefinition rule
    );

    void logStackingDrop(StackingTrace trace);

    void logPreNexus(float amount);

    void logMutation(
            String sourceId,
            DamagePhase phase,
            DamageMutationType type,
            float value
    );

    void logRejectedMutation(
            String action,
            DamagePhase currentPhase,
            String reason
    );

    void logCalculationStart();

    void logChannelResult(
            String channelId,
            float baseAmount,
            float result
    );

    void logOffensiveSummary(float total);

    void logArmor(
            String channelId,
            float damageBefore,
            float baseArmor,
            float armorEffectiveness,
            float effectiveArmor,
            float reductionPercent
    );

    void logResistance(
            String channelId,
            float attributeRating,
            float temporaryRating,
            float totalRating,
            float reductionPercent
    );

    void logEnchantmentProtection(
            String enchantmentId,
            int level,
            float scoreDelta,
            float ratingDelta
    );

    void logDefensiveSummary(float total);

    void logPostDamage(
            String victimName,
            float actualDamage
    );

    void logEnd();

    ICombatLogger NO_OP = new ICombatLogger() {
        @Override
        public boolean enabled() {
            return false;
        }

        @Override
        public void logBegin(
                String attackerName,
                String victimName,
                String sourceId,
                String initialChannel,
                float originalAmount
        ) {}

        @Override
        public void logPhase(DamagePhase phase) {}

        @Override
        public void logProcessorRun(
                DamagePhase phase,
                DamagePhaseProcessor processor
        ) {}

        @Override
        public void logProcessorSkip(
                DamagePhase phase,
                DamagePhaseProcessor processor
        ) {}

        @Override
        public void logRuleCollected(
                DamagePhase phase,
                DamageRuleDefinition rule,
                RuleExecutionContext exec
        ) {}

        @Override
        public void logRuleSkipped(
                DamagePhase phase,
                DamageRuleDefinition rule,
                RuleSkipReason reason
        ) {}

        @Override
        public void logRulePhaseMismatch(
                DamagePhase runningPhase,
                DamageRuleDefinition rule
        ) {}

        @Override
        public void logRuleRoleMismatch(
                DamagePhase phase,
                DamageRuleDefinition rule,
                RuleExecutionContext exec
        ) {}

        @Override
        public void logRuleConditionFailed(
                DamagePhase phase,
                DamageRuleDefinition rule,
                DamageRuleCondition condition
        ) {}

        @Override
        public void logRuleExecuted(
                DamagePhase phase,
                DamageRuleDefinition rule
        ) {}

        @Override
        public void logStackingDrop(StackingTrace trace) {}

        @Override
        public void logPreNexus(float amount) {}

        @Override
        public void logMutation(
                String sourceId,
                DamagePhase phase,
                DamageMutationType type,
                float value
        ) {}

        @Override
        public void logRejectedMutation(
                String action,
                DamagePhase currentPhase,
                String reason
        ) {}

        @Override
        public void logCalculationStart() {}

        @Override
        public void logChannelResult(
                String channelId,
                float baseAmount,
                float result
        ) {}

        @Override
        public void logOffensiveSummary(float total) {}

        @Override
        public void logArmor(
                String channelId,
                float damageBefore,
                float baseArmor,
                float armorEffectiveness,
                float effectiveArmor,
                float reductionPercent
        ) {}

        @Override
        public void logResistance(
                String channelId,
                float attributeRating,
                float temporaryRating,
                float totalRating,
                float reductionPercent
        ) {}

        @Override
        public void logEnchantmentProtection(
                String enchantmentId,
                int level,
                float scoreDelta,
                float ratingDelta
        ) {}

        @Override
        public void logDefensiveSummary(float total) {}

        @Override
        public void logPostDamage(
                String victimName,
                float actualDamage
        ) {}

        @Override
        public void logEnd() {}
    };

    final class ActiveLogger implements ICombatLogger {
        private static final Logger LOGGER = LogUtils.getLogger();

        private final long damageId;
        private List<DamageOperation> operations = null;

        public ActiveLogger(long damageId) {
            this.damageId = damageId;
        }

        @Override
        public boolean enabled() {
            return true;
        }

        private String prefix() {
            return "[DN#" + damageId + "]";
        }

        private static String fmt(float value) {
            return String.format(Locale.ROOT, "%.3f", value);
        }

        private static String pct(float value) {
            return String.format(Locale.ROOT, "%.3f%%", value * 100.0f);
        }

        @Override
        public void logBegin(
                String attackerName,
                String victimName,
                String sourceId,
                String initialChannel,
                float originalAmount
        ) {
            LOGGER.info(
                    "{} BEGIN attacker={} victim={} source={} channel={} original={}",
                    prefix(),
                    attackerName,
                    victimName,
                    sourceId,
                    initialChannel,
                    fmt(originalAmount)
            );
        }

        @Override
        public void logPhase(DamagePhase phase) {
            LOGGER.info("{} PHASE {}", prefix(), phase);
        }

        @Override
        public void logProcessorRun(
                DamagePhase phase,
                DamagePhaseProcessor processor
        ) {
            LOGGER.info(
                    "{} [{}] PROCESSOR_RUN processor={} priority={}",
                    prefix(),
                    phase,
                    processor.getClass().getSimpleName(),
                    processor.getPriority()
            );
        }

        @Override
        public void logProcessorSkip(
                DamagePhase phase,
                DamagePhaseProcessor processor
        ) {
            LOGGER.info(
                    "{} [{}] PROCESSOR_SKIP processor={} priority={} reason=CAN_HANDLE_FALSE",
                    prefix(),
                    phase,
                    processor.getClass().getSimpleName(),
                    processor.getPriority()
            );
        }

        @Override
        public void logRuleCollected(
                DamagePhase phase,
                DamageRuleDefinition rule,
                RuleExecutionContext exec
        ) {
            LOGGER.info(
                    "{} [{}] RULE_COLLECT rule={} provider={} role={} slot={}",
                    prefix(),
                    phase,
                    rule.id(),
                    exec.providerType(),
                    exec.role(),
                    exec.equipmentSlot()
            );
        }

        @Override
        public void logRuleSkipped(
                DamagePhase phase,
                DamageRuleDefinition rule,
                RuleSkipReason reason
        ) {
            LOGGER.info(
                    "{} [{}] RULE_SKIP rule={} reason={}",
                    prefix(),
                    phase,
                    rule.id(),
                    reason
            );
        }

        @Override
        public void logRulePhaseMismatch(
                DamagePhase runningPhase,
                DamageRuleDefinition rule
        ) {
            LOGGER.info(
                    "{} [{}] RULE_SKIP rule={} reason={} rule_phase={}",
                    prefix(),
                    runningPhase,
                    rule.id(),
                    RuleSkipReason.PHASE_MISMATCH,
                    rule.phase()
            );
        }

        @Override
        public void logRuleRoleMismatch(
                DamagePhase phase,
                DamageRuleDefinition rule,
                RuleExecutionContext exec
        ) {
            LOGGER.info(
                    "{} [{}] RULE_SKIP rule={} reason={} rule_role={} runtime_role={} provider={}",
                    prefix(),
                    phase,
                    rule.id(),
                    RuleSkipReason.ROLE_MISMATCH,
                    rule.role(),
                    exec.role(),
                    exec.providerType()
            );
        }

        @Override
        public void logRuleConditionFailed(
                DamagePhase phase,
                DamageRuleDefinition rule,
                DamageRuleCondition condition
        ) {
            LOGGER.info(
                    "{} [{}] RULE_SKIP rule={} reason={} condition={}",
                    prefix(),
                    phase,
                    rule.id(),
                    RuleSkipReason.CONDITION_FAILED,
                    condition.type()
            );
        }

        @Override
        public void logRuleExecuted(
                DamagePhase phase,
                DamageRuleDefinition rule
        ) {
            LOGGER.info(
                    "{} [{}] RULE_EXECUTE rule={} trace_name={}",
                    prefix(),
                    phase,
                    rule.id(),
                    rule.traceName()
            );
        }

        @Override
        public void logStackingDrop(StackingTrace trace) {
            LOGGER.info(
                    "{} [{}] STACKING_DROP policy={} kept={} kept_value={} dropped={} dropped_value={}",
                    prefix(),
                    trace.phase(),
                    trace.policy(),
                    trace.kept(),
                    fmt(trace.keptValue()),
                    trace.dropped(),
                    fmt(trace.droppedValue())
            );
        }

        @Override
        public void logPreNexus(float amount) {
            LOGGER.info(
                    "{} PRE original={}",
                    prefix(),
                    fmt(amount)
            );
        }

        @Override
        public void logMutation(
                String sourceId,
                DamagePhase phase,
                DamageMutationType type,
                float value
        ) {
            if (operations == null) {
                operations = new ArrayList<>(4);
            }

            operations.add(new DamageOperation(
                    sourceId,
                    phase,
                    type,
                    value
            ));
        }

        @Override
        public void logRejectedMutation(
                String action,
                DamagePhase currentPhase,
                String reason
        ) {
            LOGGER.warn(
                    "{} MUTATION_REJECTED action={} phase={} reason={}",
                    prefix(),
                    action,
                    currentPhase,
                    reason
            );
        }

        @Override
        public void logCalculationStart() {
            LOGGER.info("{} OFFENSE", prefix());

            if (operations == null || operations.isEmpty()) {
                return;
            }

            for (DamageOperation op : operations) {
                if (op.phase() == DamagePhase.MITIGATION_SETUP
                        || op.phase() == DamagePhase.FINAL_OVERRIDE) {
                    continue;
                }

                LOGGER.info(
                        "{}   mutation [{}] type={} source={} value={}",
                        prefix(),
                        op.phase(),
                        op.type(),
                        op.source(),
                        fmt(op.value())
                );
            }
        }

        @Override
        public void logChannelResult(
                String channelId,
                float baseAmount,
                float result
        ) {
            LOGGER.info(
                    "{}   channel={} base={} offensive={}",
                    prefix(),
                    channelId,
                    fmt(baseAmount),
                    fmt(result)
            );
        }

        @Override
        public void logOffensiveSummary(float total) {
            LOGGER.info(
                    "{}   offensive_total={}",
                    prefix(),
                    fmt(total)
            );
        }

        @Override
        public void logArmor(
                String channelId,
                float damageBefore,
                float baseArmor,
                float armorEffectiveness,
                float effectiveArmor,
                float reductionPercent
        ) {
            LOGGER.info(
                    "{}   armor channel={} damage_before={} base_armor={} armor_eff={} effective_formula_armor={} reduction={}",
                    prefix(),
                    channelId,
                    fmt(damageBefore),
                    fmt(baseArmor),
                    fmt(armorEffectiveness),
                    fmt(effectiveArmor),
                    pct(reductionPercent)
            );
        }

        @Override
        public void logResistance(
                String channelId,
                float attributeRating,
                float temporaryRating,
                float totalRating,
                float reductionPercent
        ) {
            LOGGER.info(
                    "{}   resistance channel={} attr_rating={} temp_rating={} total_rating={} reduction={}",
                    prefix(),
                    channelId,
                    fmt(attributeRating),
                    fmt(temporaryRating),
                    fmt(totalRating),
                    pct(reductionPercent)
            );
        }

        @Override
        public void logEnchantmentProtection(
                String enchantmentId,
                int level,
                float scoreDelta,
                float ratingDelta
        ) {
            LOGGER.info(
                    "{}   enchant_protection enchant={} level={} score_delta={} temp_rating_delta={}",
                    prefix(),
                    enchantmentId,
                    level,
                    fmt(scoreDelta),
                    fmt(ratingDelta)
            );
        }

        @Override
        public void logDefensiveSummary(float total) {
            LOGGER.info("{} DEFENSE", prefix());

            if (operations != null) {
                for (DamageOperation op : operations) {
                    if (op.phase() == DamagePhase.MITIGATION_SETUP
                            || op.phase() == DamagePhase.FINAL_OVERRIDE) {
                        LOGGER.info(
                                "{}   mutation [{}] type={} source={} value={}",
                                prefix(),
                                op.phase(),
                                op.type(),
                                op.source(),
                                fmt(op.value())
                        );
                    }
                }
            }

            LOGGER.info(
                    "{}   final_after_mitigation={}",
                    prefix(),
                    fmt(total)
            );
        }

        @Override
        public void logPostDamage(
                String victimName,
                float actualDamage
        ) {
            LOGGER.info(
                    "{} POST victim={} actual_damage={}",
                    prefix(),
                    victimName,
                    fmt(actualDamage)
            );
        }

        @Override
        public void logEnd() {
            LOGGER.info("{} END", prefix());
        }
    }
}