package io.github.naimjeg.damagenexus.api.rule.affix;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleValidator;

import java.util.ArrayList;
import java.util.List;

public final class DamageAffixValidator {

    private DamageAffixValidator() {}

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

            if (affix.rules().isEmpty()) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage affix ignored. source={} affix={} reason=no_rules",
                        source,
                        affix.id()
                );
                continue;
            }

            List<DamageRuleDefinition> validRules =
                    DamageRuleValidator.filterValid(
                            affix.rules(),
                            source + "/affix/" + affix.id()
                    );

            if (validRules.isEmpty()) {
                DamageNexus.LOGGER.warn(
                        "[DamageNexus] Invalid damage affix ignored. source={} affix={} reason=no_valid_rules",
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
                    validRules,
                    affix.stacking(),
                    affix.stackingGroup()
            ));
        }

        return List.copyOf(result);
    }
}