package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.affix.effect.*;
import io.github.naimjeg.damagenexus.registry.affix.AffixEffectTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class DefaultEffectTooltips {

    private DefaultEffectTooltips() {}

    public static void register() {
        RuleTooltipDescriptions.registerEffect(
                AffixEffectTypes.ADD_BASE_DAMAGE,
                (AddBaseDamageEffect effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[-] ")
                                .append(Component.translatableWithFallback(
                                        "effect.damagenexus.normal.add_base_damage",
                                        "+" + ctx.number(effect.value()) + " " + ctx.channelNamePlain(effect.channel()) + " Damage",
                                        ctx.number(effect.value()),
                                        ctx.channelName(effect.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_base_damage",
                            "+" + ctx.channelNamePlain(effect.channel()) + " Damage",
                            ctx.channelName(effect.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                AffixEffectTypes.ADD_CHANNEL_PRE_MULTIPLIER,
                (AddChannelPreMultiplierEffect effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[x] ")
                                .append(Component.translatableWithFallback(
                                        "effect.damagenexus.normal.add_channel_pre_multiplier",
                                        "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage",
                                        ctx.percent(effect.value()),
                                        ctx.channelName(effect.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_channel_pre_multiplier",
                            "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage",
                            ctx.percent(effect.value()),
                            ctx.channelName(effect.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                AffixEffectTypes.ADD_CHANNEL_POST_MULTIPLIER,
                (AddChannelPostMultiplierEffect effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.translatableWithFallback(
                                "effect.damagenexus.normal.add_channel_post_multiplier",
                                "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage [x]",
                                ctx.percent(effect.value()),
                                ctx.channelName(effect.channel())
                        );
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_channel_post_multiplier",
                            "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage",
                            ctx.percent(effect.value()),
                            ctx.channelName(effect.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                AffixEffectTypes.ADD_GLOBAL_POST_MULTIPLIER,
                (AddGlobalPostMultiplierEffect effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.translatableWithFallback(
                                "effect.damagenexus.normal.add_global_post_multiplier",
                                "+" + ctx.percent(effect.value()) + "% Damage [x]",
                                ctx.percent(effect.value())
                        );
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_global_post_multiplier",
                            "+" + ctx.percent(effect.value()) + "% Damage",
                            ctx.percent(effect.value())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                AffixEffectTypes.OVERRIDE_FINAL_DAMAGE,
                (OverrideFinalDamageEffect effect, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "effect.damagenexus.override_final_damage",
                                "Set final damage to " + ctx.number(effect.value()),
                                ctx.number(effect.value())
                        )
        );

        RuleTooltipDescriptions.registerEffect(
                AffixEffectTypes.ADD_TEMPORARY_RESISTANCE,
                (AddTemporaryResistanceEffect effect, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "effect.damagenexus.add_temporary_resistance",
                                "+" + ctx.number(effect.value()) + " " + ctx.channelNamePlain(effect.channel()) + " Resistance",
                                ctx.number(effect.value()),
                                ctx.channelName(effect.channel())
                        )
        );
    }
}