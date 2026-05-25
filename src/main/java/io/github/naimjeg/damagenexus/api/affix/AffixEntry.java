package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;

public record AffixEntry(
        Identifier id,
        DamagePhase phase,
        AffixDisplay display,
        List<AffixCondition> conditions,
        List<AffixEffect> effects
) {
    private static final Codec<DamagePhase> PHASE_CODEC =
            Codec.STRING.xmap(
                    name -> DamagePhase.valueOf(name.toUpperCase(Locale.ROOT)),
                    phase -> phase.name().toLowerCase(Locale.ROOT)
            );

    public static final Codec<AffixEntry> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("id")
                            .forGetter(AffixEntry::id),

                    PHASE_CODEC
                            .fieldOf("phase")
                            .forGetter(AffixEntry::phase),

                    AffixDisplay.CODEC
                            .optionalFieldOf("display", AffixDisplay.EMPTY)
                            .forGetter(AffixEntry::display),


                    AffixCondition.CODEC
                            .listOf()
                            .optionalFieldOf("conditions", List.of())
                            .forGetter(AffixEntry::conditions),

                    AffixEffect.CODEC
                            .listOf()
                            .optionalFieldOf("effects", List.of())
                            .forGetter(AffixEntry::effects)
            ).apply(instance, AffixEntry::new));

    public AffixEntry {
        conditions = List.copyOf(conditions);
        effects = List.copyOf(effects);
    }

    public void tryExecute(DamageNexusContext ctx, DamagePhase runningPhase) {
        if (this.phase != runningPhase) {
            return;
        }

        for (AffixCondition condition : conditions) {
            if (!condition.test(ctx)) {
                ctx.debugger.logOperation(
                        id.toString(),
                        runningPhase,
                        "AFFIX_CONDITION_FAILED:" + condition.type(),
                        0.0f
                );
                return;
            }
        }

        ctx.debugger.logOperation(
                id.toString(),
                runningPhase,
                "AFFIX_TRIGGERED",
                1.0f
        );

        for (AffixEffect effect : effects) {
            effect.apply(ctx);
        }
    }

}