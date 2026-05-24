package io.github.naimjeg.damagenexus;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_MODE;
    public static final ModConfigSpec.DoubleValue ASYMPTOTIC_K_VALUE;
    public static final ModConfigSpec.DoubleValue RESISTANCE_K_VALUE;
    public static final ModConfigSpec.DoubleValue RATING_PER_PROT_SCORE;

    public static boolean debugMode = false;
    public static float asymptoticKValue = 15.0f;
    public static float resistanceKValue = 50.0f;
    public static float ratingPerProtScore = 3.5f;

    static {
        BUILDER.push("developer_settings");

        DEBUG_MODE = BUILDER
                .comment("Enable detailed combat transaction logging. Set to true ONLY for debugging.",
                        "WARNING: This will output a lot of logs during combat!")
                .define("debugMode", false);

        BUILDER.pop();

        BUILDER.push("combat_formulas");

        ASYMPTOTIC_K_VALUE = BUILDER
                .comment("The K-Value for the asymptotic armor formula. Formula: Reduction = effectiveArmor / (effectiveArmor + K).",
                        "Lower values make armor strong early on (closer to vanilla). Higher values stretch the progression.",
                        "Default: 15.0")
                .defineInRange("asymptoticKValue", 15.0D, 1.0D, 1000.0D);

        RESISTANCE_K_VALUE = BUILDER
                .comment("The K-Value for the elemental resistance formula. Formula: Reduction = rating / (rating + K).",
                        "Default: 50.0")
                .defineInRange("resistanceKValue", 50.0D, 1.0D, 1000.0D);

        RATING_PER_PROT_SCORE = BUILDER
                .comment("How many resistance rating points are granted per 1 point of Vanilla Protection Score.",
                        "Vanilla Protection IV grants 4 score. If this is 3.5, it grants 14 rating per piece.",
                        "Default: 3.5")
                .defineInRange("ratingPerProtScore", 3.5D, 0.0D, 100.0D);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        bakeConfig();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        bakeConfig();
    }

    private static void bakeConfig() {
        debugMode = DEBUG_MODE.get();
        asymptoticKValue = ASYMPTOTIC_K_VALUE.get().floatValue();
        resistanceKValue = RESISTANCE_K_VALUE.get().floatValue();
        ratingPerProtScore = RATING_PER_PROT_SCORE.get().floatValue();

        DamageNexus.LOGGER.info("[DamageNexus] Config baked: ResK={}, ProtScoreRatio={}",
                resistanceKValue, ratingPerProtScore);
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
}