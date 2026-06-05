package io.github.naimjeg.damagenexus.api.rule.entry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleValidator;

import java.util.ArrayList;
import java.util.List;

public final class DamageEntryValidator {

    private DamageEntryValidator() {
    }

    public static List<DamageEntryDefinition> filterValid(
            List<DamageEntryDefinition> entries,
            String source
    ) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        List<DamageEntryDefinition> result = new ArrayList<>();

        for (DamageEntryDefinition entry : entries) {
            if (entry == null) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage entry ignored. source={} reason=null_entry",
                        source
                );
                continue;
            }

            if (entry.rules().isEmpty()) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage entry ignored. source={} entry={} reason=no_rules",
                        source,
                        entry.id()
                );
                continue;
            }

            List<DamageRuleDefinition> validRules =
                    DamageRuleValidator.filterValid(
                            entry.rules(),
                            source + "/entry/" + entry.id()
                    );

            if (validRules.isEmpty()) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage entry ignored. source={} entry={} reason=no_valid_rules",
                        source,
                        entry.id()
                );
                continue;
            }

            result.add(new DamageEntryDefinition(
                    entry.id(),
                    entry.display(),
                    entry.slot(),
                    validRules,
                    entry.stacking(),
                    entry.stackingGroup()
            ));
        }

        return List.copyOf(result);
    }
}
