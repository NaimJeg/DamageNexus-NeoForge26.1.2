package io.github.naimjeg.damagenexus.config;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.diagnostics.logging.DamageNexusLifecycleLog;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class DamageNexusConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        DeveloperConfigSpec.define(BUILDER);
        DiagnosticsConfigSpec.define(BUILDER);
        TooltipConfigSpec.define(BUILDER);
        CombatFormulaConfigSpec.define(BUILDER);
        VanillaCompatibilityConfigSpec.define(BUILDER);
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile DamageNexusConfigValues CURRENT =
            DamageNexusConfigValues.defaults();

    private DamageNexusConfig() {}

    public static DamageNexusConfigValues current() {
        return CURRENT;
    }

    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bakeConfig();
        }
    }

    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bakeConfig();
        }
    }

    public static void bakeConfig() {
        DamageNexusConfigValues values = new DamageNexusConfigValues(
                DeveloperConfigSpec.bake(),
                DiagnosticsConfigSpec.bake(),
                TooltipConfigSpec.bake(),
                CombatFormulaConfigSpec.bake(),
                VanillaCompatibilityConfigSpec.bake()
        );

        CURRENT = values;
        ModConfig.syncLegacyValues(values);

        DamageNexusLifecycleLog.configBaked(
                values.diagnostics().debugMode(),
                values.developer().enableTestCommands(),
                values.diagnostics().postDamageDiagnosticsEnabled(),
                values.developer().strictProcessorErrors(),
                values.developer().strictRuleErrors(),
                values.vanillaCompatibility().mode(),
                values.formulas().asymptoticKValue(),
                values.formulas().resistanceKValue(),
                values.formulas().ratingPerProtScore()
        );
    }
}