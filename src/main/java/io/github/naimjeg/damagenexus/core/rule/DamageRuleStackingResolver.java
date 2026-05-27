package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DamageRuleStackingResolver {

    private DamageRuleStackingResolver() {}

    public static DamageRuleStackingResult resolve(List<RuntimeDamageRule> input) {
        if (input.isEmpty()) {
            return new DamageRuleStackingResult(input, List.of());
        }

        if (input.size() == 1) {
            return new DamageRuleStackingResult(input, List.of());
        }

        if (allStack(input)) {
            return new DamageRuleStackingResult(input, List.of());
        }

        List<RuntimeDamageRule> stacked = new ArrayList<>(input.size());
        List<StackingTrace> traces = null;

        Map<StackingKey, RuntimeDamageRule> uniqueSource = null;
        Map<StackingKey, RuntimeDamageRule> highestValue = null;
        Map<StackingKey, RuntimeDamageRule> lowestValue = null;
        Map<StackingKey, RuntimeDamageRule> replace = null;

        for (RuntimeDamageRule runtimeRule : input) {
            DamageRuleDefinition rule = runtimeRule.definition();

            switch (rule.stacking()) {
                case STACK -> stacked.add(runtimeRule);

                case UNIQUE_SOURCE -> {
                    if (uniqueSource == null) {
                        uniqueSource = new LinkedHashMap<>();
                    }

                    traces = mergeWithTrace(
                            uniqueSource,
                            keyOf(runtimeRule),
                            runtimeRule,
                            traces,
                            DamageRuleStacking.UNIQUE_SOURCE,
                            DamageRuleStackingResolver::chooseHigherPriority
                    );
                }

                case HIGHEST_VALUE -> {
                    if (highestValue == null) {
                        highestValue = new LinkedHashMap<>();
                    }

                    traces = mergeWithTrace(
                            highestValue,
                            keyOf(runtimeRule),
                            runtimeRule,
                            traces,
                            DamageRuleStacking.HIGHEST_VALUE,
                            DamageRuleStackingResolver::chooseHigherValue
                    );
                }

                case LOWEST_VALUE -> {
                    if (lowestValue == null) {
                        lowestValue = new LinkedHashMap<>();
                    }

                    traces = mergeWithTrace(
                            lowestValue,
                            keyOf(runtimeRule),
                            runtimeRule,
                            traces,
                            DamageRuleStacking.LOWEST_VALUE,
                            DamageRuleStackingResolver::chooseLowestValue
                    );
                }

                case REPLACE -> {
                    if (replace == null) {
                        replace = new LinkedHashMap<>();
                    }

                    traces = mergeWithTrace(
                            replace,
                            keyOf(runtimeRule),
                            runtimeRule,
                            traces,
                            DamageRuleStacking.REPLACE,
                            DamageRuleStackingResolver::chooseReplacement
                    );
                }
            }
        }

        if (uniqueSource != null) {
            stacked.addAll(uniqueSource.values());
        }

        if (highestValue != null) {
            stacked.addAll(highestValue.values());
        }

        if (lowestValue != null) {
            stacked.addAll(lowestValue.values());
        }

        if (replace != null) {
            stacked.addAll(replace.values());
        }

        return new DamageRuleStackingResult(
                stacked,
                traces != null ? traces : List.of()
        );
    }

    private static boolean allStack(List<RuntimeDamageRule> input) {
        for (RuntimeDamageRule runtimeRule : input) {
            if (runtimeRule.definition().stacking() != DamageRuleStacking.STACK) {
                return false;
            }
        }

        return true;
    }

    @FunctionalInterface
    private interface RuleChooser {
        RuntimeDamageRule choose(RuntimeDamageRule existing, RuntimeDamageRule candidate);
    }

    private static List<StackingTrace> mergeWithTrace(
            Map<StackingKey, RuntimeDamageRule> map,
            StackingKey key,
            RuntimeDamageRule candidate,
            List<StackingTrace> traces,
            DamageRuleStacking policy,
            RuleChooser chooser
    ) {
        RuntimeDamageRule existing = map.get(key);

        if (existing == null) {
            map.put(key, candidate);
            return traces;
        }

        RuntimeDamageRule chosen = chooser.choose(existing, candidate);
        RuntimeDamageRule dropped = chosen == existing ? candidate : existing;

        map.put(key, chosen);

        if (traces == null) {
            traces = new ArrayList<>(2);
        }

        DamageRuleDefinition chosenDefinition = chosen.definition();
        DamageRuleDefinition droppedDefinition = dropped.definition();

        traces.add(new StackingTrace(
                candidate.definition().phase(),
                chosenDefinition.id(),
                droppedDefinition.id(),
                policy,
                stackingValue(chosenDefinition),
                stackingValue(droppedDefinition)
        ));

        return traces;
    }

    private static RuntimeDamageRule chooseHigherPriority(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        if (candidate.definition().priority() > existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseReplacement(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        if (candidate.definition().priority() >= existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseHigherValue(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        float existingValue = stackingValue(existing.definition());
        float candidateValue = stackingValue(candidate.definition());

        int valueCompare = Float.compare(candidateValue, existingValue);

        if (valueCompare > 0) {
            return candidate;
        }

        if (valueCompare == 0
                && candidate.definition().priority() > existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseLowestValue(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        float existingValue = stackingValue(existing.definition());
        float candidateValue = stackingValue(candidate.definition());

        int valueCompare = Float.compare(candidateValue, existingValue);

        if (valueCompare < 0) {
            return candidate;
        }

        if (valueCompare == 0
                && candidate.definition().priority() > existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static StackingKey keyOf(RuntimeDamageRule runtimeRule) {
        DamageRuleDefinition rule = runtimeRule.definition();

        return new StackingKey(
                rule.stackingKey(),
                rule.phase(),
                rule.role()
        );
    }

    private static float stackingValue(DamageRuleDefinition rule) {
        float result = 0.0f;
        boolean hasValue = false;

        for (DamageRuleOperation operation : rule.operations()) {
            float value = operation.stackingValue();

            if (!hasValue || value > result) {
                result = value;
                hasValue = true;
            }
        }

        return hasValue ? result : 0.0f;
    }

    private record StackingKey(
            Identifier group,
            DamagePhase phase,
            DamageRuleRole role
    ) {}
}