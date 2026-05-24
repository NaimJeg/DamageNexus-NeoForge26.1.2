package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import net.minecraft.resources.Identifier;

import java.util.*;

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
        id = Objects.requireNonNull(id, "Damage rule id must not be null");
        role = Objects.requireNonNull(role, "Damage rule role must not be null");
        phase = Objects.requireNonNull(phase, "Damage rule phase must not be null");
        display = Objects.requireNonNull(display, "Damage rule display must not be null");
        stacking = Objects.requireNonNull(stacking, "Damage rule stacking policy must not be null");

        stackingGroup = stackingGroup != null
                ? stackingGroup
                : Optional.empty();

        traceLabel = traceLabel != null
                ? traceLabel
                : Optional.empty();

        conditions = copyNonNullElements(
                conditions,
                "conditions",
                "Damage rule condition must not be null"
        );

        operations = copyNonNullElements(
                operations,
                "operations",
                "Damage rule operation must not be null"
        );
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

    private static <T> List<T> copyNonNullElements(
            List<T> input,
            String listName,
            String elementMessage
    ) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }

        List<T> copy = new ArrayList<>(input.size());

        for (T element : input) {
            copy.add(Objects.requireNonNull(
                    element,
                    elementMessage + " in " + listName
            ));
        }

        return List.copyOf(copy);
    }
}