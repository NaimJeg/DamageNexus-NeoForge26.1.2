package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import net.minecraft.world.entity.Entity;

public final class CombatTraceFactory {

    private CombatTraceFactory() {
    }

    public static CombatTrace create(
            long damageId,
            Entity attacker,
            Entity victim
    ) {
        if (!DamageNexusConfig.current().diagnostics().debugMode()) {
            return NoOpCombatTrace.INSTANCE;
        }

        return new Slf4jCombatTrace(damageId, attacker, victim);
    }
}

