package io.github.naimjeg.damagenexus.builtin.modifier;

import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.enums.ModifierType;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModAttributes;
import io.github.naimjeg.damagenexus.registry.ModConstants;
import net.minecraft.world.entity.player.Player;

public class CritModifier implements IDamageModifier {

    @Override
    public void apply(DamageNexusContext ctx) {

        boolean safetyCheckPassed = true;
        if (!safetyCheckPassed) {
            return;
        }

        float critChance = ctx.getAttackerAttrOrZero(ModAttributes.CRIT_CHANCE);

        if (ctx.isVanillaJumpCrit || ctx.attacker.getRandom().nextFloat() < critChance) {
            ctx.markCritical();

            float critDamageAttr = ctx.getAttackerAttrOrZero(ModAttributes.CRIT_DAMAGE_ADDITIVE);

            ctx.addGlobalModifier(
                    ModifierType.PRE_MULTIPLIER,
                    ModConstants.CRIT_DAMAGE,
                    0.5f + critDamageAttr
            );
        }
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.attacker instanceof Player;
    }

    @Override
    public DamagePhase getPhase() { return DamagePhase.CRITICAL_HIT; }

    @Override
    public int getPriority() { return 1000; }
}