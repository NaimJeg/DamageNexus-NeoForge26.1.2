package io.github.naimjeg.damagenexus.core.trace;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public record DamageNexusTransaction(
        long damageId,
        LivingEntity attacker,
        LivingEntity victim,
        DamageSource source,

        float eventOriginalAmount,
        float initialBaseAmount,
        float offensiveTotal,
        float finalEventAmount,

        float eventAmountBeforeSet,
        float eventAmountAfterSet,

        float victimHealthBefore,
        float victimAbsorptionBefore,
        int victimInvulnerableTimeBefore,
        long gameTime
) {}