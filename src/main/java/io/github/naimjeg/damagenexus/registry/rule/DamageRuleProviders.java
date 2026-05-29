package io.github.naimjeg.damagenexus.registry.rule;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleProvider;
import io.github.naimjeg.damagenexus.builtin.rule.provider.DatapackDamageRuleProvider;
import io.github.naimjeg.damagenexus.builtin.rule.provider.ItemDamageRuleProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DamageRuleProviders {

    private static final List<DamageRuleProvider> PROVIDERS =
            new ArrayList<>();

    private static boolean bootstrapped = false;

    private DamageRuleProviders() {}

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        registerBuiltin(new ItemDamageRuleProvider());
        registerBuiltin(new DatapackDamageRuleProvider());
        //registerBuiltin(new VanillaEnchantmentRuleProvider());
        //registerBuiltin(new VanillaMobEffectRuleProvider());

        bootstrapped = true;
    }

    public static synchronized void register(DamageRuleProvider provider) {
        Objects.requireNonNull(provider, "provider");

        if (PROVIDERS.contains(provider)) {
            return;
        }

        PROVIDERS.add(provider);
    }

    public static synchronized List<DamageRuleProvider> all() {
        return List.copyOf(PROVIDERS);
    }

    private static synchronized void registerBuiltin(DamageRuleProvider provider) {
        Class<?> providerClass = provider.getClass();

        for (DamageRuleProvider existing : PROVIDERS) {
            if (existing.getClass() == providerClass) {
                return;
            }
        }

        PROVIDERS.add(provider);
    }
}