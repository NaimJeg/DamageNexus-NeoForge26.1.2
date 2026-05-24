package io.github.naimjeg.damagenexus.builtin.modifier;

import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.enums.ModifierType;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModConstants;
import net.minecraft.world.entity.player.Player;

public class VanillaCritModifier implements IDamageModifier {

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.markCritical();

        ctx.addGlobalModifier(
                ModifierType.PRE_MULTIPLIER,
                ModConstants.CRIT_DAMAGE,
                0.5f
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.attacker instanceof Player
                && !ctx.isCritical()
                && ctx.isVanillaJumpCrit;
    }

    @Override
    public DamagePhase getPhase() { return DamagePhase.CRITICAL_HIT; }

    @Override
    public int getPriority() { return 0; }
}