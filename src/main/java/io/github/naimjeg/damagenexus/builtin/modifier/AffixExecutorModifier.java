package io.github.naimjeg.damagenexus.builtin.modifier;

import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.affix.AffixEntry;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AffixExecutorModifier implements IDamageModifier {

    private final DamagePhase phase;

    public AffixExecutorModifier(DamagePhase phase) {
        this.phase = phase;
    }

//    @Override
//    public void apply(DamageNexusContext ctx) {
//        if (ctx.attacker == null) {
//            return;
//        }
//
//        ItemStack weapon = ctx.attacker.getMainHandItem();
//
//        if (weapon.isEmpty()) {
//            return;
//        }
//
//        List<AffixEntry> affixes =
//                weapon.getOrDefault(
//                        ModDataComponents.ITEM_AFFIXES.get(),
//                        List.of()
//                );
//
//        if (affixes.isEmpty()) {
//            return;
//        }
//
//        int matched = 0;
//
//        for (AffixEntry affix : affixes) {
//            if (affix.phase() == phase) {
//                matched++;
//            }
//        }
//
//        if (matched > 0) {
//            ctx.debugger.logOperation(
//                    "affix:executor",
//                    phase,
//                    "AFFIX_PHASE_MATCH",
//                    matched
//            );
//        }
//    }
    /// TODO: THIS IS A TEST VERSION
    @Override
    public void apply(DamageNexusContext ctx) {
        if (ctx.attacker == null) {
            return;
        }

        ItemStack weapon = ctx.attacker.getMainHandItem();

        if (weapon.isEmpty()) {
            ctx.debugger.logOperation(
                    "affix:executor",
                    phase,
                    "NO_WEAPON",
                    0.0f
            );
            return;
        }

        List<AffixEntry> affixes =
                weapon.getOrDefault(
                        ModDataComponents.ITEM_AFFIXES.get(),
                        List.of()
                );

        if (affixes.isEmpty()) {
            return;
        }

        int matched = 0;

        for (AffixEntry affix : affixes) {

            if (affix.phase() == phase) {
                matched++;
            }
        }

        if (matched > 0) {
            ctx.debugger.logOperation(
                    "affix:executor",
                    phase,
                    "AFFIX_PHASE_MATCH",
                    matched
            );
        }

        for (AffixEntry affix : affixes) {
            affix.tryExecute(ctx, phase);
        }
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged && ctx.attacker != null;
    }

    @Override
    public DamagePhase getPhase() {
        return phase;
    }

    @Override
    public int getPriority() {
        return 500;
    }
}