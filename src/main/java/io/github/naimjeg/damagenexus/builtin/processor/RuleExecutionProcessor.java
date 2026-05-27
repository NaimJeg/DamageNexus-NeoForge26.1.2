package io.github.naimjeg.damagenexus.builtin.processor;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RuleExecutionProcessor implements DamagePhaseProcessor {

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
            provider.collect(ctx, phase, rules);
        }

        if (rules.isEmpty()) {
            return;
        }

        DamageRuleStackingResult result =
                DamageRuleStackingResolver.resolve(rules);

        rules = result.rules();

        if (ctx.debugger.enabled()) {
            for (StackingTrace trace : result.traces()) {
                ctx.debugger.logStackingDrop(trace);
            }
        }

        rules.sort(PRIORITY_DESC);

        for (RuntimeDamageRule rule : rules) {
            DamageRuleExecutor.execute(
                    ctx,
                    phase,
                    rule
            );
        }
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged;
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