package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

public enum AffixEntityTarget {
    ATTACKER,
    VICTIM;

    public static final Codec<AffixEntityTarget> CODEC =
            Codec.STRING.xmap(
                    name -> AffixEntityTarget.valueOf(name.toUpperCase(Locale.ROOT)),
                    target -> target.name().toLowerCase(Locale.ROOT)
            );

    public LivingEntity resolve(DamageNexusContext ctx) {
        return switch (this) {
            case ATTACKER -> ctx.attacker;
            case VICTIM -> ctx.victim;
        };
    }
}