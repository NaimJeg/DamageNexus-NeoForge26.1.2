package io.github.naimjeg.damagenexus.builtin.modifier;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class ResistanceModifier implements IDamageModifier {

    @Override
    public void apply(DamageNexusContext ctx) {


        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            float currentDmg = component.getPostMitigationAmount();
            if (currentDmg <= 0.0f) {
                continue;
            }

            Holder<Attribute> attrHolder =
                    DamageChannelRegistry.getResistanceAttribute(component.channel);

            float attributeRating =
                    attrHolder != null
                            ? ctx.getVictimAttrOrZero(attrHolder)
                            : 0.0f;

            float tempRating =
                    component.getTemporaryResistanceRating();

            float totalRating =
                    attributeRating + tempRating;

            if (totalRating == 0.0f) {
                if (ctx.debugger.enabled()) {
                    ctx.debugger.logResistance(
                            component.channel.id().toString(),
                            attributeRating,
                            tempRating,
                            totalRating,
                            0.0f
                    );
                }

                continue;
            }

            float K = ModConfig.resistanceKValue;
            float reduction =
                    totalRating >= 0.0f
                            ? totalRating / (totalRating + K)
                            : totalRating / K;

            reduction = Math.max(-1.0f, Math.min(0.95f, reduction));

            ctx.addChannelMitigation(
                    component.channel,
                    reduction,
                    "dn:resistance"
            );

            if (ctx.debugger.enabled()) {
                ctx.debugger.logResistance(
                        component.channel.id().toString(),
                        attributeRating,
                        tempRating,
                        totalRating,
                        reduction
                );
            }
        }
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return 999;
    }
}