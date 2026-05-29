package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;

public class ResistanceMitigationProcessor implements DamagePhaseProcessor {

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

            float K = Math.max(0.0001f, ModConfig.resistanceKValue);
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
    public boolean canHandle(DamageNexusContext ctx) {
        return ctx.isManaged
                && !ctx.source.is(DamageTypeTags.BYPASSES_EFFECTS)
                && !ctx.source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.DN_RESISTANCE_MITIGATION;
    }
}