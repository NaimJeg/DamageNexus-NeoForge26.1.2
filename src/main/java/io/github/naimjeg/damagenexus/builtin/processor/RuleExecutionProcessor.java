package io.github.naimjeg.damagenexus.builtin.processor;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleProvider;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleExecutor;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleStackingResolver;
import io.github.naimjeg.damagenexus.core.rule.DamageRuleStackingResult;
import io.github.naimjeg.damagenexus.core.rule.StackingTrace;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleProviders;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RuleExecutionProcessor implements DamagePhaseProcessor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<String> LOGGED_PROVIDER_FAILURES =
            ConcurrentHashMap.newKeySet();

    private static final Comparator<RuntimeDamageRule> PRIORITY_DESC =
            (a, b) -> Integer.compare(
                    b.definition().priority(),
                    a.definition().priority()
            );

    private final DamagePhase phase;

    public RuleExecutionProcessor(DamagePhase phase) {
        this.phase = phase;
    }

    @Override
    public void apply(DamageNexusContext ctx) {
        List<RuntimeDamageRule> rules = new ArrayList<>();

        for (DamageRuleProvider provider : DamageRuleProviders.all()) {
            if (provider == null) {
                continue;
            }

            if (!safeSupportsPhase(ctx, provider, phase)) {
                continue;
            }

            safeCollect(ctx, provider, phase, rules);
        }

        if (rules.isEmpty()) {
            return;
        }

        DamageRuleStackingResult result =
                DamageRuleStackingResolver.resolve(rules);

        rules = new ArrayList<>(result.rules());

        if (ctx.trace().enabled()) {
            for (StackingTrace trace : result.traces()) {
                ctx.trace().rules().stackingDrop(trace);
            }
        }

        rules.sort(PRIORITY_DESC);

        for (RuntimeDamageRule rule : rules) {
            DamageRuleExecutor.execute(
                    ctx,
                    phase,
                    rule
            );

            if (ctx.isDamageCancelled()) {
                return;
            }
        }
    }

    private static boolean safeSupportsPhase(
            DamageNexusContext ctx,
            DamageRuleProvider provider,
            DamagePhase phase
    ) {
        try {
            return provider.supportsPhase(phase);
        } catch (Throwable throwable) {
            handleProviderFailure(
                    ctx,
                    provider,
                    phase,
                    "supportsPhase",
                    throwable
            );

            return false;
        }
    }

    private static void safeCollect(
            DamageNexusContext ctx,
            DamageRuleProvider provider,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        int before = out.size();

        try {
            provider.collect(ctx, phase, out);
        } catch (Throwable throwable) {
            /*
             * Provider collection is not transactional.
             *
             * If a provider partially appended rules before throwing, discard
             * only that provider's partial additions. Rules collected from
             * earlier providers remain valid.
             */
            while (out.size() > before) {
                out.remove(out.size() - 1);
            }

            handleProviderFailure(
                    ctx,
                    provider,
                    phase,
                    "collect",
                    throwable
            );
        }
    }

    private static void handleProviderFailure(
            DamageNexusContext ctx,
            DamageRuleProvider provider,
            DamagePhase phase,
            String stage,
            Throwable throwable
    ) {
        if (ModConfig.strictRuleErrors()) {
            if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            if (throwable instanceof Error error) {
                throw error;
            }

            throw new IllegalStateException(
                    "[DamageNexus] Rule provider failure at "
                            + stage
                            + ": provider="
                            + providerName(provider)
                            + " phase="
                            + phase,
                    throwable
            );
        }

        String providerName = providerName(provider);
        String key = phase + "|" + stage + "|" + providerName + "|"
                + throwable.getClass().getName();

        if (LOGGED_PROVIDER_FAILURES.add(key)) {
            LOGGER.error(
                    "[DamageNexus] Rule provider failed. phase={} stage={} provider={}. "
                            + "Provider output for this phase was skipped. "
                            + "Set strictRuleErrors=true to fail fast.",
                    phase,
                    stage,
                    providerName,
                    throwable
            );
        }

        ctx.trace().mutations().rejected(
                "rule_provider/" + stage,
                phase,
                "provider=" + providerName
                        + " threw "
                        + throwable.getClass().getSimpleName()
                        + ": "
                        + String.valueOf(throwable.getMessage())
        );
    }

    private static String providerName(DamageRuleProvider provider) {
        return provider == null
                ? "<null>"
                : provider.getClass().getName();
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged();
    }

    @Override
    public DamagePhase getPhase() {
        return phase;
    }

    @Override
    public int getPriority() {
        return switch (phase) {
            case MITIGATION_SETUP -> 1002;
            default -> 500;
        };
    }
}
