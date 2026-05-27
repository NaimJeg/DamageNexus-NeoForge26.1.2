package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddBaseDamageOperation;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddChannelMitigationOperation;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.util.IdentifierText;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;
import java.util.Optional;

public final class VanillaMobEffectRuleProvider implements DamageRuleProvider {

    private static final float EPSILON = 0.0001f;

    private static final Identifier STRENGTH_WEAKNESS_ID =
            Identifier.fromNamespaceAndPath("vanilla", "mob_effect/strength_weakness");

    private static final Identifier RESISTANCE_ID =
            Identifier.fromNamespaceAndPath("vanilla", "mob_effect/resistance");

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        switch (phase) {
            case BASE_MODIFICATION -> collectStrengthWeakness(ctx, out);
            case MITIGATION_SETUP -> collectResistance(ctx, out);
            default -> {
            }
        }
    }

    private void collectStrengthWeakness(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out
    ) {
        if (!ctx.shouldRebuildVanillaOffensiveMobEffects()) {
            return;
        }

        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float delta = ctx.getVanillaOffensiveMobEffectDelta();

        if (!Float.isFinite(delta) || Math.abs(delta) <= EPSILON) {
            return;
        }

        DamageRuleDefinition rule = new DamageRuleDefinition(
                STRENGTH_WEAKNESS_ID,
                DamageRuleRole.OFFENSIVE,
                DamagePhase.BASE_MODIFICATION,
                990,
                new DamageRuleDisplay(
                        Optional.of("Vanilla Strength / Weakness"),
                        Optional.of("Vanilla offensive mob effect damage converted into a DamageNexus runtime rule.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddBaseDamageOperation(
                        ctx.getInitialChannel().id(),
                        delta
                )),
                DamageRuleStacking.STACK,
                Optional.of(STRENGTH_WEAKNESS_ID),
                Optional.of("Vanilla Strength / Weakness")
        );

        RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                rule,
                RuleExecutionContext.vanillaMobEffect(
                        DamageRuleRole.OFFENSIVE,
                        ctx.attacker
                )
        );

        ctx.debugger.logRuleCollected(
                DamagePhase.BASE_MODIFICATION,
                rule,
                runtimeRule.executionContext()
        );

        out.add(runtimeRule);
    }

    private void collectResistance(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.victim == null) {
            return;
        }

        if (ctx.source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return;
        }

        if (ctx.source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            return;
        }

        MobEffectInstance resistance =
                ctx.victim.getEffect(MobEffects.RESISTANCE);

        if (resistance == null) {
            return;
        }

        int level = resistance.getAmplifier() + 1;

        float reduction =
                Math.max(0.0f, Math.min(1.0f, level * 0.20f));

        if (!Float.isFinite(reduction) || reduction <= 0.0f) {
            return;
        }

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            DamageRuleDefinition rule = new DamageRuleDefinition(
                    Identifier.fromNamespaceAndPath(
                            "vanilla",
                            "mob_effect_resistance_" + IdentifierText.path(component.channel.id())
                    ),
                    DamageRuleRole.DEFENSIVE,
                    DamagePhase.MITIGATION_SETUP,
                    1000,
                    new DamageRuleDisplay(
                            Optional.of("Vanilla Resistance"),
                            Optional.of("Vanilla Resistance effect converted into DamageNexus mitigation.")
                    ),
                    List.of(new AlwaysCondition()),
                    List.of(new AddChannelMitigationOperation(
                            component.channel.id(),
                            reduction
                    )),
                    DamageRuleStacking.STACK,
                    Optional.of(RESISTANCE_ID),
                    Optional.of("Vanilla Resistance")
            );

            RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                    rule,
                    RuleExecutionContext.vanillaMobEffect(
                            DamageRuleRole.DEFENSIVE,
                            ctx.victim
                    )
            );

            ctx.debugger.logRuleCollected(
                    DamagePhase.MITIGATION_SETUP,
                    rule,
                    runtimeRule.executionContext()
            );

            out.add(runtimeRule);
        }
    }
}