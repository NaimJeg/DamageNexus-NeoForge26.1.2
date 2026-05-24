package io.github.naimjeg.damagenexus.builtin.processor;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.DamageProcessorPriorities;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;

public class ArmorMitigationProcessor implements DamagePhaseProcessor {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void apply(DamageNexusContext ctx) {
        float baseArmor = ctx.getVictimAttrOrZero(Attributes.ARMOR);
        float effectiveness = ctx.getArmorEffectivenessMultiplier();
        float armor = Math.max(0.0f, baseArmor * effectiveness);

        if (armor <= 0.0f) {
            ctx.setArmorHandled();
            return;
        }

        float toughness = ctx.getVictimAttrOrZero(Attributes.ARMOR_TOUGHNESS);
        float toughnessFactor = 2.0f + (toughness / 4.0f);
        float kValue = Math.max(0.0001f, ModConfig.asymptoticKValue);

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);

            DamageChannelRegistry.ChannelData channelData =
                    DamageChannelRegistry.getData(component.channel);

            if (!channelData.affectedByArmor()) {
                continue;
            }

            float currentDmg = component.getPostMitigationAmount();

            if (currentDmg <= 0.0f) {
                continue;
            }

            float effectiveArmor =
                    Math.max(0.0f, armor - (currentDmg / toughnessFactor));

            float reductionPercent =
                    effectiveArmor / (effectiveArmor + kValue);

            reductionPercent = Math.max(0.0f, Math.min(0.995f, reductionPercent));

            if (reductionPercent <= 0.0f) {
                if (ModConfig.isDebugMode()) {
                    ctx.trace().calculation().armor(
                            component.channel.id().toString(),
                            currentDmg,
                            baseArmor,
                            effectiveness,
                            effectiveArmor,
                            0.0f
                    );
                }

                continue;
            }

            ctx.tryAddChannelMitigation(
                    component.channel,
                    reductionPercent,
                    "dn:armor_reduction"
            );

            if (ModConfig.isDebugMode()) {
                ctx.trace().calculation().armor(
                        component.channel.id().toString(),
                        currentDmg,
                        baseArmor,
                        effectiveness,
                        effectiveArmor,
                        reductionPercent
                );
            }
        }

        ctx.setArmorHandled();
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return !ctx.source().is(DamageTypeTags.BYPASSES_ARMOR)
                && !ctx.isArmorHandled();
    }

    @Override
    public DamagePhase getPhase() {
        return DamagePhase.MITIGATION_SETUP;
    }

    @Override
    public int getPriority() {
        return DamageProcessorPriorities.DN_ARMOR_MITIGATION;
    }
}
