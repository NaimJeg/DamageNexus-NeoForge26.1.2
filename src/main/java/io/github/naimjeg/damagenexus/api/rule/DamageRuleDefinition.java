package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DamageRuleDefinition(
        Identifier id,
        DamageRuleRole role,
        DamagePhase phase,
        int priority,
        DamageRuleDisplay display,
        List<DamageRuleCondition> conditions,
        List<DamageRuleOperation> operations,
        DamageRuleStacking stacking,
        Optional<Identifier> stackingGroup,
        Optional<String> traceLabel
) {
    private static final Codec<DamagePhase> PHASE_CODEC =
            Codec.STRING.xmap(
                    name -> DamagePhase.valueOf(name.toUpperCase(Locale.ROOT)),
                    phase -> phase.name().toLowerCase(Locale.ROOT)
            );

    public static final Codec<DamageRuleDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("id")
                            .forGetter(DamageRuleDefinition::id),

                    DamageRuleRole.CODEC
                            .optionalFieldOf("role", DamageRuleRole.OFFENSIVE)
                            .forGetter(DamageRuleDefinition::role),

                    PHASE_CODEC
                            .fieldOf("phase")
                            .forGetter(DamageRuleDefinition::phase),

                    Codec.INT
                            .optionalFieldOf("priority", 500)
                            .forGetter(DamageRuleDefinition::priority),

                    DamageRuleDisplay.CODEC
                            .optionalFieldOf("display", DamageRuleDisplay.EMPTY)
                            .forGetter(DamageRuleDefinition::display),

                    DamageRuleCondition.CODEC
                            .listOf()
                            .optionalFieldOf("conditions", List.of())
                            .forGetter(DamageRuleDefinition::conditions),

                    DamageRuleOperation.CODEC
                            .listOf()
                            .optionalFieldOf("operations", List.of())
                            .forGetter(DamageRuleDefinition::operations),

                    DamageRuleStacking.CODEC
                            .optionalFieldOf("stacking", DamageRuleStacking.STACK)
                            .forGetter(DamageRuleDefinition::stacking),

                    Identifier.CODEC
                            .optionalFieldOf("stacking_group")
                            .forGetter(DamageRuleDefinition::stackingGroup),

                    Codec.STRING
                            .optionalFieldOf("trace_label")
                            .forGetter(DamageRuleDefinition::traceLabel)
            ).apply(instance, DamageRuleDefinition::new));

    public DamageRuleDefinition {
        conditions = List.copyOf(conditions);
        operations = List.copyOf(operations);
    }

    public Identifier source() {
        return id;
    }

    public String traceName() {
        return traceLabel.orElse(id.toString());
    }

    public Identifier stackingKey() {
        return stackingGroup.orElse(id);
    }
}