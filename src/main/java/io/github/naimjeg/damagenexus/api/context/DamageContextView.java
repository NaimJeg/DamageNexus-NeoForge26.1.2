package io.github.naimjeg.damagenexus.api.context;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface DamageContextView {

    boolean isManaged();

    LivingEntity attacker();

    LivingEntity victim();

    DamageSource source();

    long damageId();

    DamageChannel getInitialChannel();

    /**
     * Returns whether the current damage packet contains positive damage in the
     * given channel at this point of the pipeline.
     */
    boolean hasActiveDamageInChannel(DamageChannel channel);

    DamagePhase currentPhase();

    boolean isCritical();

    boolean isDamageCancelled();
}