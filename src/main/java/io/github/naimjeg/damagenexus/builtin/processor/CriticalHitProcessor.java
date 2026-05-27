package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModAttributes;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import net.minecraft.world.entity.player.Player;

public class CriticalHitProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "dn:critical_hit";

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.suppressesDefaultCritical()
                || ctx.isVanillaSpearAttack()
                || ctx.isCritical()) {
            return;
        }

        float critChance = ctx.getAttackerAttrOrZero(ModAttributes.CRIT_CHANCE);

        if (ctx.attacker.getRandom().nextFloat() < critChance) {
            ctx.markCritical();

            float critDamageAttr =
                    ctx.getAttackerAttrOrZero(ModAttributes.CRIT_DAMAGE_ADDITIVE);

            ctx.addGlobalPreMultiplier(
                    PreMultiplierBuckets.CRIT_DAMAGE,
                    0.5f + critDamageAttr,
                    TRACE_ID
            );
        }
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.attacker instanceof Player
                && !ctx.isCritical()
                && !ctx.isVanillaSpearAttack()
                && !ctx.isVanillaMaceSmash()
                && !ctx.suppressesDefaultCritical();
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.CRITICAL_HIT;
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}