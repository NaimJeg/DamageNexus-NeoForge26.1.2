package io.github.naimjeg.damagenexus.registry.rule;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleProvider;
import io.github.naimjeg.damagenexus.builtin.rule.provider.ItemDamageRuleProvider;
import io.github.naimjeg.damagenexus.builtin.rule.provider.VanillaEnchantmentRuleProvider;

import java.util.ArrayList;
import java.util.List;

public final class DamageRuleProviders {

    private static final List<DamageRuleProvider> PROVIDERS =
            new ArrayList<>();

    private static boolean bootstrapped = false;

    private DamageRuleProviders() {}

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        register(new ItemDamageRuleProvider());
        //register(new VanillaEnchantmentRuleProvider());

        bootstrapped = true;
    }

    public static void register(DamageRuleProvider provider) {
        PROVIDERS.add(provider);
    }

    public static List<DamageRuleProvider> all() {
        return List.copyOf(PROVIDERS);
    }
}