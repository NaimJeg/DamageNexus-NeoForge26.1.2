package io.github.naimjeg.damagenexus.api.rule.affix;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryValidator;

import java.util.ArrayList;
import java.util.List;

public final class DamageAffixValidator {

    private DamageAffixValidator() {
    }

    public static List<DamageAffixDefinition> filterValid(
            List<DamageAffixDefinition> affixes,
            String source
    ) {
        if (affixes == null || affixes.isEmpty()) {
            return List.of();
        }

        List<DamageAffixDefinition> result = new ArrayList<>();

        for (DamageAffixDefinition affix : affixes) {
            if (affix == null) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage affix ignored. source={} reason=null_affix",
                        source
                );
                continue;
            }

            if (affix.entries().isEmpty()) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage affix ignored. source={} affix={} reason=no_entries",
                        source,
                        affix.id()
                );
                continue;
            }

            List<DamageEntryDefinition> validEntries =
                    DamageEntryValidator.filterValid(
                            affix.entries(),
                            source + "/affix/" + affix.id()
                    );

            if (validEntries.isEmpty()) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage affix ignored. source={} affix={} reason=no_valid_entries",
                        source,
                        affix.id()
                );
                continue;
            }

            result.add(new DamageAffixDefinition(
                    affix.id(),
                    affix.display(),
                    affix.slot(),
                    affix.rarity(),
                    validEntries,
                    affix.stacking(),
                    affix.stackingGroup()
            ));
        }

        return List.copyOf(result);
    }
}
