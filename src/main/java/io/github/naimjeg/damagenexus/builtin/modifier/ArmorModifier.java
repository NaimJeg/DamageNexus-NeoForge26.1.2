package io.github.naimjeg.damagenexus.builtin.modifier;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.core.DamageComponent;
import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class ArmorModifier implements IDamageModifier {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void apply(DamageNexusContext ctx) {
        boolean safetyCheckPassed = true;
        if (!safetyCheckPassed) {
            return;
        }

        float baseArmor = ctx.getVictimAttrOrZero(Attributes.ARMOR);
        float effectiveness = ctx.getArmorEffectivenessMultiplier();
        float armor = Math.max(0.0f, baseArmor * effectiveness);

        float toughness = ctx.getVictimAttrOrZero(Attributes.ARMOR_TOUGHNESS);

        if (armor > 0.0f) {
            for (int i = 0; i < ctx.getActiveComponentCount(); i++) {
                DamageComponent component = ctx.getActiveComponent(i);
                float currentDmg = component.getPostMitigationAmount();
                if (currentDmg <= 0) continue;

                float toughnessFactor = 2.0f + (toughness / 4.0f);
                float effectiveArmor = Math.max(0.0f, armor - (currentDmg / toughnessFactor));
                float kValue = ModConfig.asymptoticKValue;

                float reductionPercent = effectiveArmor / (effectiveArmor + kValue);
                reductionPercent = Math.min(reductionPercent, 0.995f);

                ctx.addChannelMitigation(component.channel, reductionPercent, "dn:armor_reduction");

                if (ModConfig.isDebugMode()) {
                    ctx.debugger.logArmor(
                            component.channel.id().toString(),
                            currentDmg,
                            baseArmor,
                            effectiveness,
                            effectiveArmor,
                            reductionPercent
                    );
                }
            }
        }
        ctx.setArmorHandled();
    }

    @Override
    public boolean canHandle(DamageNexusContext ctx) { return !ctx.source.is(DamageTypeTags.BYPASSES_ARMOR); }
    @Override
    public DamagePhase getPhase() { return DamagePhase.MITIGATION_SETUP; }
    @Override
    public int getPriority() { return 1000; }
}