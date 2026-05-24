package io.github.naimjeg.damagenexus.core;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
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

    void logModifier(String modifierName);

    void logPreNexus(float amount);

    void logOperation(String sourceId, DamagePhase phase, String type, float value);

    void logOperation(int sourceId, DamagePhase phase, String type, float value);

    void logRejectedMutation(String action, DamagePhase currentPhase, String reason);

    void logCalculationStart();

    void logChannelResult(String channelId, float baseAmount, float result);

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

    void logPostDamage(String victimName, float actualDamage);

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
        public void logModifier(String modifierName) {}

        @Override
        public void logPreNexus(float amount) {}

        @Override
        public void logOperation(String sourceId, DamagePhase phase, String type, float value) {}

        @Override
        public void logOperation(int sourceId, DamagePhase phase, String type, float value) {}

        @Override
        public void logRejectedMutation(String action, DamagePhase currentPhase, String reason) {}

        @Override
        public void logCalculationStart() {}

        @Override
        public void logChannelResult(String channelId, float baseAmount, float result) {}

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
        public void logPostDamage(String victimName, float actualDamage) {}

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
        public void logModifier(String modifierName) {
            LOGGER.info("{}   -> {}", prefix(), modifierName);
        }

        @Override
        public void logPreNexus(float amount) {
            LOGGER.info("{} PRE original={}", prefix(), fmt(amount));
        }

        @Override
        public void logOperation(String sourceId, DamagePhase phase, String type, float value) {
            if (operations == null) {
                operations = new ArrayList<>(4);
            }

            operations.add(new DamageOperation(sourceId, phase, type, value));
        }

        @Override
        public void logOperation(int sourceId, DamagePhase phase, String type, float value) {
            logOperation("#" + sourceId, phase, type, value);
        }

        @Override
        public void logRejectedMutation(String action, DamagePhase currentPhase, String reason) {
            LOGGER.warn(
                    "{} rejected action={} phase={} reason={}",
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
                LOGGER.info(
                        "{}   op [{}] {} {} = {}",
                        prefix(),
                        op.phase(),
                        op.type(),
                        op.source(),
                        fmt(op.value())
                );
            }
        }

        @Override
        public void logChannelResult(String channelId, float baseAmount, float result) {
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
            LOGGER.info("{}   offensive_total={}", prefix(), fmt(total));
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
            LOGGER.info("{} DEFENSE final_after_mitigation={}", prefix(), fmt(total));
        }

        @Override
        public void logPostDamage(String victimName, float actualDamage) {
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