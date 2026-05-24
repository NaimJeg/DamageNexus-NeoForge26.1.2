package io.github.naimjeg.damagenexus.api.rule;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DamageRuleValidator {

    public enum Policy {
        /**
         * Invalid rules are logged once and ignored by the caller.
         * Best for datapacks and item data where one bad rule should not crash the game.
         */
        WARN,

        /**
         * Invalid rules throw IllegalStateException immediately.
         * Best for builder / Java API registration.
         */
        REJECT
    }

    private static final Set<String> LOGGED_PROBLEMS =
            ConcurrentHashMap.newKeySet();

    private DamageRuleValidator() {}

    public static boolean validate(
            DamageRuleDefinition rule,
            String source,
            Policy policy
    ) {
        if (rule == null) {
            return problem(
                    source,
                    "<null>",
                    "rule is null",
                    policy
            );
        }

        String ruleId = safeRuleId(rule);

        if (rule.id() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule id is null",
                    policy
            );
        }

        if (rule.role() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule role is null",
                    policy
            );
        }

        if (rule.phase() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule phase is null",
                    policy
            );
        }

        if (rule.display() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule display is null",
                    policy
            );
        }

        if (rule.conditions() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule conditions list is null",
                    policy
            );
        }

        if (rule.operations() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule operations list is null",
                    policy
            );
        }

        if (rule.stacking() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule stacking policy is null",
                    policy
            );
        }

        if (rule.stackingGroup() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule stacking group optional is null",
                    policy
            );
        }

        if (rule.traceLabel() == null) {
            return problem(
                    source,
                    ruleId,
                    "rule trace label optional is null",
                    policy
            );
        }

        if (rule.operations().isEmpty()) {
            return problem(
                    source,
                    ruleId,
                    "rule has no operations",
                    policy
            );
        }

        boolean valid = true;

        for (DamageRuleCondition condition : rule.conditions()) {
            if (condition == null) {
                valid = false;
                problem(
                        source,
                        ruleId,
                        "rule contains null condition",
                        policy
                );
            }
        }

        for (DamageRuleOperation operation : rule.operations()) {
            if (operation == null) {
                valid = false;
                problem(
                        source,
                        ruleId,
                        "rule contains null operation",
                        policy
                );
                continue;
            }

            if (!operation.supportsPhase(rule.phase())) {
                valid = false;

                problem(
                        source,
                        ruleId,
                        "operation " + operation.type()
                                + " does not support rule phase " + rule.phase()
                                + "; supported=" + describeSupportedPhases(operation),
                        policy
                );
            }
        }

        return valid;
    }

    public static void requireValid(
            DamageRuleDefinition rule,
            String source
    ) {
        validate(rule, source, Policy.REJECT);
    }

    public static List<DamageRuleDefinition> filterValid(
            Collection<DamageRuleDefinition> rules,
            String source
    ) {
        if (rules == null || rules.isEmpty()) {
            return List.of();
        }

        List<DamageRuleDefinition> validRules =
                new ArrayList<>(rules.size());

        for (DamageRuleDefinition rule : rules) {
            if (validate(rule, source, Policy.WARN)) {
                validRules.add(rule);
            }
        }

        return List.copyOf(validRules);
    }

    static boolean problem(
            String source,
            String ruleId,
            String message,
            Policy policy
    ) {
        String safeSource = source == null ? "<unknown>" : source;
        String key = safeSource + "|" + ruleId + "|" + message;

        if (policy == Policy.REJECT) {
            throw new IllegalStateException(
                    "[DamageNexus] Invalid damage rule from "
                            + safeSource
                            + ": rule="
                            + ruleId
                            + " "
                            + message
            );
        }

        if (LOGGED_PROBLEMS.add(key)) {
            DamageNexus.LOGGER.warn(
                    "[DamageNexus] Invalid damage rule ignored. source={} rule={} reason={}",
                    safeSource,
                    ruleId,
                    message
            );
        }

        return false;
    }

    private static String safeRuleId(DamageRuleDefinition rule) {
        if (rule == null || rule.id() == null) {
            return "<null>";
        }

        return rule.id().toString();
    }

    private static String describeSupportedPhases(
            DamageRuleOperation operation
    ) {
        Set<DamagePhase> phases = operation.supportedPhases();

        if (phases == null || phases.isEmpty()) {
            return "any";
        }

        return phases.toString();
    }
}