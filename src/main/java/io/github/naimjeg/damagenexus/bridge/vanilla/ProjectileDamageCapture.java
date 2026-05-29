package io.github.naimjeg.damagenexus.bridge.vanilla;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ProjectileDamageCapture {

    private static final ThreadLocal<ProjectileFrame> CURRENT =
            new ThreadLocal<>();

    private ProjectileDamageCapture() {}

    public static void capture(
            Entity projectile,
            Entity victim,
            DamageSource source,
            @Nullable ItemStack weapon,
            float preEnchantDamage,
            float postEnchantDamage,
            int preCritDamage,
            int postCritDamage,
            boolean critical
    ) {
        if (!Float.isFinite(preEnchantDamage)
                || !Float.isFinite(postEnchantDamage)) {
            return;
        }

        if (preCritDamage < 0 || postCritDamage < 0) {
            return;
        }

        CURRENT.set(new ProjectileFrame(
                projectile,
                victim,
                source,
                weapon == null ? ItemStack.EMPTY : weapon.copy(),
                Math.max(0.0f, preEnchantDamage),
                Math.max(0.0f, postEnchantDamage),
                Math.max(0, preCritDamage),
                Math.max(0, postCritDamage),
                critical,
                Math.max(0, postCritDamage - preCritDamage)
        ));

        if (ModConfig.isDebugMode()) {
            DamageNexus.LOGGER.info(
                    "[DN-VanillaCapture] projectile_damage weapon={} preEnchant={} postEnchant={} preCrit={} postCrit={} critical={} critBonus={} source={}",
                    weapon == null || weapon.isEmpty()
                            ? "<empty>"
                            : weapon.getHoverName().getString(),
                    preEnchantDamage,
                    postEnchantDamage,
                    preCritDamage,
                    postCritDamage,
                    critical,
                    Math.max(0, postCritDamage - preCritDamage),
                    source.type().msgId()
            );
        }
    }

    public static @Nullable ProjectileFrame peekFor(
            DamageSource source,
            Entity victim
    ) {
        ProjectileFrame frame = CURRENT.get();

        if (frame == null) {
            return null;
        }

        if (frame.victim() != victim) {
            return null;
        }

        if (frame.source() != source) {
            return null;
        }

        return frame;
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record ProjectileFrame(
            Entity projectile,
            Entity victim,
            DamageSource source,
            ItemStack weapon,
            float preEnchantDamage,
            float postEnchantDamage,
            int preCritDamage,
            int postCritDamage,
            boolean critical,
            int criticalBonus
    ) {}
}