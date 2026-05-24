/**
 * THIS IS A EXAMPLE USAGE OF PHASE:
 *      FINAL_OVERRIDE
 */

//package io.github.naimjeg.damagenexus.builtin.modifier;
//
//import io.github.naimjeg.damagenexus.api.IDamageModifier;
//import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
//import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
//import io.github.naimjeg.damagenexus.registry.ModAttributes;
//
//public class DodgeModifier implements IDamageModifier {
//    @Override
//    public void apply(DamageNexusContext ctx) {
//        float dodgeChance = ctx.getVictimAttrOrZero(ModAttributes.DODGE_CHANCE);
//
//        if (ctx.victim.getRandom().nextFloat() < dodgeChance) {
//            ctx.overrideFinalDamage(0.0f, "damagenexus:dodge");
//        }
//    }
//
//    @Override
//    public DamagePhase getPhase() {
//        return DamagePhase.FINAL_OVERRIDE;
//    }
//
//    @Override
//    public int getPriority() {
//        return 1000;
//    }
//}