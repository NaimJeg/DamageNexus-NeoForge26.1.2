package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModAttributes;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import net.minecraft.world.entity.player.Player;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
public class VanillaCriticalBridgeProcessor implements DamagePhaseProcessor {

    private static final String TRACE_ID = "vanilla:critical_hit";
    private static final float VANILLA_CRIT_DAMAGE = 0.5f;

    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.suppressesDefaultCritical()
                || ctx.isVanillaSpearAttack()
                || ctx.isVanillaMaceSmash()) {
            return;
        }

        ctx.markCritical();

        float critDamageAttr =
                ctx.getAttackerAttrOrZero(ModAttributes.CRIT_DAMAGE_ADDITIVE);

        float value = VANILLA_CRIT_DAMAGE + critDamageAttr;

        ctx.addApplicationPreMultiplier(
                DamageApplicationBucket.VANILLA_MELEE_BASE,
                PreMultiplierBuckets.CRIT_DAMAGE,
                value,
                TRACE_ID
        );

        ctx.addApplicationPreMultiplier(
                DamageApplicationBucket.VANILLA_MELEE_ENCHANTMENT,
                PreMultiplierBuckets.CRIT_DAMAGE,
                value,
                TRACE_ID
        );

        ctx.addApplicationPreMultiplier(
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL,
                PreMultiplierBuckets.CRIT_DAMAGE,
                value,
                TRACE_ID
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.attacker instanceof Player
                && ctx.isVanillaJumpCrit
                && !ctx.isCritical()
                && !ctx.isVanillaMaceSmash()
                && !ctx.isVanillaSpearAttack()
                && !ctx.suppressesDefaultCritical();
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.CRITICAL_HIT;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_CRITICAL_BRIDGE;
    }
}