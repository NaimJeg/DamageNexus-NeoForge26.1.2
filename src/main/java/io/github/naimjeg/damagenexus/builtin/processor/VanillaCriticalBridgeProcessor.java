package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModAttributes;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

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

        recordVanillaCritMultiplier(
                ctx,
                DamageApplicationBucket.VANILLA_MELEE_BASE,
                value
        );

        recordVanillaCritMultiplier(
                ctx,
                DamageApplicationBucket.VANILLA_MELEE_ENCHANTMENT,
                value
        );

        recordVanillaCritMultiplier(
                ctx,
                DamageApplicationBucket.VANILLA_WEAPON_SPECIAL,
                value
        );
    }

    private static void recordVanillaCritMultiplier(
            DamageNexusContext ctx,
            DamageApplicationBucket bucket,
            float value
    ) {
        DamageMutationResult result = ctx.tryAddApplicationPreMultiplier(
                bucket,
                PreMultiplierBuckets.CRIT_DAMAGE,
                value,
                TRACE_ID
        );

        ctx.contributions().record(
                result,
                () -> DamageContributionDescriptor.vanillaMultiplier(
                        Identifier.fromNamespaceAndPath(
                                DamageNexus.MODID,
                                "vanilla_critical_hit/"
                                        + bucket.name().toLowerCase(Locale.ROOT)
                        ),
                        DamagePhase.CRITICAL_HIT,
                        ctx.getInitialChannel().id(),
                        bucket,
                        PreMultiplierBuckets.CRIT_DAMAGE_ID,
                        value,
                        TRACE_ID
                )
        );
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.attacker() instanceof Player
                && ctx.isVanillaJumpCrit()
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