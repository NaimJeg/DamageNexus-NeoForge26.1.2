package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddBaseDamageOperation;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddTemporaryResistanceOperation;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.util.IdentifierText;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Optional;

public final class VanillaEnchantmentRuleProvider implements DamageRuleProvider {

    private static final float EPSILON = 0.0001f;

    private static final Identifier OFFENSIVE_ENCHANTMENT_ID =
            Identifier.fromNamespaceAndPath("vanilla", "offensive_enchantment");

    private static final Identifier DAMAGE_PROTECTION_ID =
            Identifier.fromNamespaceAndPath("vanilla", "damage_protection");

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        switch (phase) {
            case BASE_MODIFICATION -> collectOffensiveEnchantment(ctx, out);
            case MITIGATION_SETUP -> collectDamageProtection(ctx, out);
            default -> {
            }
        }
    }

    private void collectOffensiveEnchantment(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out
    ) {
        if (!ctx.shouldRebuildVanillaOffensiveEnchantment()) {
            return;
        }

        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float delta = snapshot.enchantDelta();

        if (!Float.isFinite(delta) || Math.abs(delta) <= EPSILON) {
            return;
        }

        DamageRuleDefinition rule = new DamageRuleDefinition(
                OFFENSIVE_ENCHANTMENT_ID,
                DamageRuleRole.OFFENSIVE,
                DamagePhase.BASE_MODIFICATION,
                1000,
                new DamageRuleDisplay(
                        Optional.of("Vanilla Offensive Enchantment"),
                        Optional.of("Vanilla enchantment damage converted into a DamageNexus runtime rule.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddBaseDamageOperation(
                        ctx.getInitialChannel().id(),
                        delta
                )),
                DamageRuleStacking.STACK,
                Optional.of(OFFENSIVE_ENCHANTMENT_ID),
                Optional.of("Vanilla Offensive Enchantment")
        );

        RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                rule,
                RuleExecutionContext.vanillaEnchantment(
                        DamageRuleRole.OFFENSIVE,
                        ctx.attacker,
                        snapshot.weapon(),
                        null
                )
        );

        ctx.debugger.logRuleCollected(
                DamagePhase.BASE_MODIFICATION,
                rule,
                runtimeRule.executionContext()
        );

        out.add(runtimeRule);
    }

    private void collectDamageProtection(
            DamageNexusContext ctx,
            List<RuntimeDamageRule> out
    ) {
        if (ctx.victim == null) {
            return;
        }

        if (!(ctx.victim.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (ctx.source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return;
        }

        if (ctx.source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return;
        }

        float score = EnchantmentHelper.getDamageProtection(
                serverLevel,
                ctx.victim,
                ctx.source
        );

        if (!Float.isFinite(score) || score <= 0.0f) {
            return;
        }

        float rating = score * ModConfig.ratingPerProtScore;

        if (!Float.isFinite(rating) || rating == 0.0f) {
            return;
        }

        if (ctx.debugger.enabled()) {
            ctx.debugger.logEnchantmentProtection(
                    DAMAGE_PROTECTION_ID.toString(),
                    0,
                    score,
                    rating
            );
        }

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            DamageRuleDefinition rule = new DamageRuleDefinition(
                    Identifier.fromNamespaceAndPath(
                            "vanilla",
                            "damage_protection_" + IdentifierText.path(component.channel.id())
                    ),
                    DamageRuleRole.DEFENSIVE,
                    DamagePhase.MITIGATION_SETUP,
                    1000,
                    new DamageRuleDisplay(
                            Optional.of("Vanilla Damage Protection"),
                            Optional.of("Vanilla protection enchantments converted into temporary resistance.")
                    ),
                    List.of(new AlwaysCondition()),
                    List.of(new AddTemporaryResistanceOperation(
                            component.channel.id(),
                            rating
                    )),
                    DamageRuleStacking.STACK,
                    Optional.of(DAMAGE_PROTECTION_ID),
                    Optional.of("Vanilla Damage Protection")
            );

            RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                    rule,
                    RuleExecutionContext.vanillaEnchantment(
                            DamageRuleRole.DEFENSIVE,
                            ctx.victim,
                            ItemStack.EMPTY,
                            null
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