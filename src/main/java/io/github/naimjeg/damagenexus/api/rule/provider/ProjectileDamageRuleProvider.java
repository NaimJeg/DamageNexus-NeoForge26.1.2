package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageSourceProfile;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ProjectileDamageRuleProvider implements DamageRuleProvider {

    @Override
    public boolean supportsPhase(DamagePhase phase) {
        return true;
    }

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        VanillaDamageSourceProfile profile = ctx.vanillaSourceProfile();

        if (profile == null || !profile.projectile()) {
            return;
        }

        Entity projectileEntity = profile.directAttacker();

        if (!(projectileEntity instanceof Projectile)) {
            return;
        }

        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        ItemStack projectileWeapon = snapshot.weapon();

        if (projectileWeapon == null || projectileWeapon.isEmpty()) {
            return;
        }

        LivingEntity owner =
                profile.livingAttacker();

        RuleExecutionContext exec =
                RuleExecutionContext.projectileSource(
                        DamageRuleRole.OFFENSIVE,
                        owner,
                        projectileWeapon,
                        projectileEntity
                );

        collectStackRules(
                ctx,
                phase,
                out,
                projectileWeapon,
                exec
        );
    }

    private void collectStackRules(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out,
            ItemStack stack,
            RuleExecutionContext exec
    ) {
        List<DamageRuleDefinition> rules =
                stack.getOrDefault(
                        ModDataComponents.DAMAGE_RULES.get(),
                        List.of()
                );

        if (rules.isEmpty()) {
            return;
        }

        List<DamageRuleDefinition> validRules =
                DamageRuleValidator.filterValid(
                        rules,
                        "projectile_damage_rules/" + stack.getHoverName().getString()
                );

        for (DamageRuleDefinition rule : validRules) {
            if (rule.phase() != phase) {
                continue;
            }

            if (!rule.role().canRunAs(exec.role())) {
                continue;
            }

            RuntimeDamageRule runtimeRule =
                    new RuntimeDamageRule(rule, exec);

            ctx.debugger.logRuleCollected(
                    phase,
                    rule,
                    exec
            );

            out.add(runtimeRule);
        }
    }
}