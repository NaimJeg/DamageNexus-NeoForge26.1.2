package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.enums.ModifierType;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import net.minecraft.world.entity.player.Player;

public class VanillaCriticalBridgeProcessor implements DamagePhaseProcessor {

    @Override
    public void apply(DamageNexusContext ctx) {
        ctx.markCritical();

        ctx.addGlobalPreMultiplier(
                PreMultiplierBuckets.CRIT_DAMAGE,
                0.5f,
                "dn:critical_hit"
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