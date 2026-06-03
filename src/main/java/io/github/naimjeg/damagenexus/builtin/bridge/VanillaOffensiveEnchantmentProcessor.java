package io.github.naimjeg.damagenexus.builtin.bridge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.display.DamageContributionDescriptor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.bridge.vanilla.VanillaDamageCapture;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public final class VanillaOffensiveEnchantmentProcessor implements DamagePhaseProcessor {

    private static final float EPSILON = 0.0001f;

    @Override
    public void apply(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        if (snapshot == null) {
            return;
        }

        float delta = snapshot.enchantDelta();

        if (!Float.isFinite(delta) || Math.abs(delta) <= EPSILON) {
            return;
        }

        String traceId = traceId(ctx);

        DamageMutationResult result = ctx.tryAddBaseDamage(
                ctx.getInitialChannel(),
                ctx.getVanillaOffensiveEnchantmentBucket(),
                delta,
                traceId
        );

        ctx.contributions().record(
                result,
                () -> DamageContributionDescriptor.vanillaBase(
                        Identifier.fromNamespaceAndPath(
                                DamageNexus.MODID,
                                "vanilla_offensive_enchantment/"
                                        + ctx.getVanillaOffensiveEnchantmentBucket()
                                        .name()
                                        .toLowerCase(Locale.ROOT)
                        ),
                        DamagePhase.BASE_MODIFICATION,
                        ctx.getInitialChannel().id(),
                        ctx.getVanillaOffensiveEnchantmentBucket(),
                        delta,
                        traceId
                )
        );
    }

    private static String traceId(DamageNexusContext ctx) {
        return ctx.vanillaSourceProfile().projectile()
                ? "vanilla:projectile_enchantment"
                : "vanilla:melee_enchantment";
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        VanillaDamageCapture.OffensiveSnapshot snapshot =
                ctx.getVanillaSnapshot();

        return ctx.shouldRebuildVanillaOffensiveEnchantment()
                && snapshot != null
                && Math.abs(snapshot.enchantDelta()) > EPSILON;
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.BASE_MODIFICATION;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.VANILLA_OFFENSIVE_ENCHANTMENT;
    }
}