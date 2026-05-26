package io.github.naimjeg.damagenexus.builtin.processor;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class LegacyVanillaArmorProcessor implements DamagePhaseProcessor {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void apply(DamageNexusContext ctx) {
        float armor = ctx.getVictimAttrOrZero(Attributes.ARMOR);
        float toughness = ctx.getVictimAttrOrZero(Attributes.ARMOR_TOUGHNESS);

        if (armor <= 0.0f) return;

        for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
            DamageComponent component = ctx.getActiveComponent(i);
            float currentDmg = component.getPostMitigationAmount();
            if (currentDmg <= 0) continue;

            float f = 2.0f + toughness / 4.0f;
            float f1 = Math.max(armor * 0.2f, Math.min(armor - currentDmg / f, 20.0f));

            float reductionPercent = f1 / 25.0f;

            ctx.addChannelMitigation(component.channel, reductionPercent, "minecraft:armor");

            if (ModConfig.isDebugMode()) {
                LOGGER.info("      [VanillaArmorFallback] Channel {}",
                        component.channel.id());
            }
        }
        ctx.setArmorHandled();
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) {
        return !ctx.source.is(DamageTypeTags.BYPASSES_ARMOR) && !ctx.isArmorHandled();
    }

    @Override
    public DamagePhase getPhase() { return DamagePhase.MITIGATION_SETUP; }
    @Override
    public int getPriority() { return 0; }
}