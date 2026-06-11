package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.core.config.DamageNexusSettings;
import net.minecraft.world.entity.Entity;

public final class CombatTraceFactory {

    private CombatTraceFactory() {
    }

    public static CombatTrace create(
            long damageId,
            Entity attacker,
            Entity victim
    ) {
        if (!DamageNexusSettings.summaryTraceEnabled()) {
            return NoOpCombatTrace.INSTANCE;
        }

        return new Slf4jCombatTrace(damageId, attacker, victim);
    }
}

