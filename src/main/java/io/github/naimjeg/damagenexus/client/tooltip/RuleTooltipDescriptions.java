package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class RuleTooltipDescriptions {

    private static final RuleTooltipContext CONTEXT = new RuleTooltipContext();

    private static final Map<Identifier, RuleTooltipProvider<? extends AffixCondition>> CONDITION_PROVIDERS =
            new HashMap<>();

    private static final Map<Identifier, RuleTooltipProvider<? extends AffixEffect>> EFFECT_PROVIDERS =
            new HashMap<>();

    private RuleTooltipDescriptions() {}

    public static <T extends AffixCondition> void registerCondition(
            Identifier type,
            RuleTooltipProvider<T> provider
    ) {
        CONDITION_PROVIDERS.put(type, provider);
    }

    public static <T extends AffixEffect> void registerEffect(
            Identifier type,
            RuleTooltipProvider<T> provider
    ) {
        EFFECT_PROVIDERS.put(type, provider);
    }

    @SuppressWarnings("unchecked")
    public static MutableComponent describeCondition(
            AffixCondition condition,
            RuleTooltipMode mode
    ) {
        RuleTooltipProvider<AffixCondition> provider =
                (RuleTooltipProvider<AffixCondition>) CONDITION_PROVIDERS.get(condition.type());

        if (provider == null) {
            return Component.translatableWithFallback(
                    "condition." + condition.type().getNamespace() + "." + condition.type().getPath(),
                    condition.type().toString()
            );
        }

        return provider.describe(condition, CONTEXT, mode);
    }

    @SuppressWarnings("unchecked")
    public static MutableComponent describeEffect(
            AffixEffect effect,
            RuleTooltipMode mode
    ) {
        RuleTooltipProvider<AffixEffect> provider =
                (RuleTooltipProvider<AffixEffect>) EFFECT_PROVIDERS.get(effect.type());

        if (provider == null) {
            return Component.translatableWithFallback(
                    "effect." + effect.type().getNamespace() + "." + effect.type().getPath(),
                    effect.type().toString()
            );
        }

        return provider.describe(effect, CONTEXT, mode);
    }
}