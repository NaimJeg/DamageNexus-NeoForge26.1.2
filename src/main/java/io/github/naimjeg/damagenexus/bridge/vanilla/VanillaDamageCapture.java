package io.github.naimjeg.damagenexus.bridge.vanilla;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class VanillaDamageCapture {

    private static final float EPSILON = 0.0001f;

    private static final ThreadLocal<OffensiveEnchantFrame> OFFENSIVE_ENCHANT =
            new ThreadLocal<>();

    private VanillaDamageCapture() {}

    public static void captureModifyDamage(
            ServerLevel level,
            ItemStack weapon,
            Entity victim,
            DamageSource source,
            float inputDamage,
            float outputDamage
    ) {
        float delta = outputDamage - inputDamage;

        OFFENSIVE_ENCHANT.set(new OffensiveEnchantFrame(
                source.getEntity(),
                victim,
                source,
                weapon.copy(),
                inputDamage,
                outputDamage,
                delta
        ));

        if (ModConfig.isDebugMode() && Math.abs(delta) > EPSILON) {
            DamageNexus.LOGGER.info(
                    "[DN-VanillaCapture] modifyDamage weapon={} input={} output={} delta={} source={}",
                    weapon.getHoverName().getString(),
                    inputDamage,
                    outputDamage,
                    delta,
                    source.type().msgId()
            );
        }
    }

    public static @Nullable OffensiveSnapshot consumeOffensiveSnapshot(
            DamageSource source,
            LivingEntity victim,
            float eventOriginalDamage
    ) {
        OffensiveEnchantFrame frame = OFFENSIVE_ENCHANT.get();

        if (frame == null) {
            return null;
        }

        try {
            if (frame.source() != source || frame.victim() != victim) {
                return null;
            }

            PreEventDelta preEventDelta = classifyPreEventDelta(
                    source,
                    victim,
                    frame.outputDamage(),
                    eventOriginalDamage
            );

            return new OffensiveSnapshot(
                    frame.attacker(),
                    frame.victim(),
                    frame.source(),
                    frame.weapon(),
                    frame.inputDamage(),
                    frame.outputDamage(),
                    frame.enchantDelta(),
                    eventOriginalDamage,
                    preEventDelta
            );
        } finally {
            OFFENSIVE_ENCHANT.remove();
        }
    }

    public static void clear() {
        OFFENSIVE_ENCHANT.remove();
    }

    private static PreEventDelta classifyPreEventDelta(
            DamageSource source,
            LivingEntity victim,
            float postEnchantDamage,
            float eventOriginalDamage
    ) {
        float delta = eventOriginalDamage - postEnchantDamage;

        if (Math.abs(delta) <= EPSILON) {
            return PreEventDelta.none(postEnchantDamage, eventOriginalDamage);
        }

        float ratio = Math.abs(postEnchantDamage) > EPSILON
                ? eventOriginalDamage / postEnchantDamage
                : 1.0f;

        Entity attacker = source.getEntity();

        if (victim instanceof Player && attacker instanceof Mob) {
            Difficulty difficulty = victim.level().getDifficulty();

            float expected = switch (difficulty) {
                case PEACEFUL -> 0.0f;
                case EASY -> Math.min(postEnchantDamage, postEnchantDamage * 0.5f + 1.0f);
                case NORMAL -> postEnchantDamage;
                case HARD -> postEnchantDamage * 1.5f;
            };

            if (Math.abs(expected - eventOriginalDamage) <= EPSILON) {
                return new PreEventDelta(
                        PreEventDeltaKind.DIFFICULTY_SCALING,
                        postEnchantDamage,
                        eventOriginalDamage,
                        delta,
                        ratio,
                        "mob_damage_to_player difficulty=" + difficulty.getSerializedName()
                );
            }
        }

        if (source.type().msgId().contains("mace")
                || source.type().msgId().contains("smash")) {
            return new PreEventDelta(
                    PreEventDeltaKind.SPECIAL_ATTACK_SCALING,
                    postEnchantDamage,
                    eventOriginalDamage,
                    delta,
                    ratio,
                    "special_attack source=" + source.type().msgId()
            );
        }

        return new PreEventDelta(
                PreEventDeltaKind.UNKNOWN,
                postEnchantDamage,
                eventOriginalDamage,
                delta,
                ratio,
                "unclassified source=" + source.type().msgId()
        );
    }

    public record OffensiveEnchantFrame(
            @Nullable Entity attacker,
            Entity victim,
            DamageSource source,
            ItemStack weapon,
            float inputDamage,
            float outputDamage,
            float enchantDelta
    ) {}

    public record OffensiveSnapshot(
            @Nullable Entity attacker,
            Entity victim,
            DamageSource source,
            ItemStack weapon,
            float preEnchantDamage,
            float postEnchantDamage,
            float enchantDelta,
            float eventOriginalDamage,
            PreEventDelta preEventDelta
    ) {
        public boolean hasEnchantDelta() {
            return Math.abs(enchantDelta) > EPSILON;
        }

        public boolean hasPreEventDelta() {
            return preEventDelta.kind() != PreEventDeltaKind.NONE;
        }
    }

    public record PreEventDelta(
            PreEventDeltaKind kind,
            float postEnchantDamage,
            float eventOriginalDamage,
            float delta,
            float ratio,
            String reason
    ) {
        public static PreEventDelta none(float postEnchantDamage, float eventOriginalDamage) {
            return new PreEventDelta(
                    PreEventDeltaKind.NONE,
                    postEnchantDamage,
                    eventOriginalDamage,
                    0.0f,
                    1.0f,
                    "none"
            );
        }
    }
}