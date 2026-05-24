package io.github.naimjeg.damagenexus.event.neoforge;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = DamageNexus.MODID)
public final class VanillaCritHandler {

    private static final ThreadLocal<PendingVanillaCrit> PENDING_CRIT =
            new ThreadLocal<>();

    private VanillaCritHandler() {}

    @SubscribeEvent
    public static void onVanillaCriticalHit(CriticalHitEvent event) {
        Player attacker = event.getEntity();
        Entity target = event.getTarget();

        if (event.isVanillaCritical()
                && !attacker.level().isClientSide()) {
            PENDING_CRIT.set(new PendingVanillaCrit(
                    attacker.getId(),
                    target.getId(),
                    attacker.level().getGameTime(),
                    true
            ));
        } else {
            clear();
        }

        /*
         * DN captures vanilla critical state here, then rebuilds the critical
         * bonus inside the DamageNexus pipeline.
         *
         * This prevents vanilla's critical multiplier from being applied before
         * DN reconstructs the offensive transaction.
         */
        event.setDamageMultiplier(1.0f);
    }

    public static boolean consumePendingVanillaCrit(
            @Nullable LivingEntity attacker,
            LivingEntity victim,
            DamageSource source
    ) {
        PendingVanillaCrit pending = PENDING_CRIT.get();

        if (pending == null) {
            return false;
        }

        if (!pending.matches(attacker, victim, source)) {
            return false;
        }

        clear();
        return true;
    }

    public static void clear() {
        PENDING_CRIT.remove();
    }

    public record PendingVanillaCrit(
            int attackerId,
            int targetId,
            long gameTime,
            boolean vanillaCritical
    ) {
        private boolean matches(
                @Nullable LivingEntity attacker,
                LivingEntity victim,
                DamageSource source
        ) {
            if (!vanillaCritical) {
                return false;
            }

            if (attacker == null) {
                return false;
            }

            if (attackerId >= 0 && attacker.getId() != attackerId) {
                return false;
            }

            if (victim.getId() != targetId) {
                return false;
            }

            if (gameTime >= 0L && attacker.level().getGameTime() != gameTime) {
                return false;
            }

            if (source.getEntity() != attacker) {
                return false;
            }

            if (source.getDirectEntity() != attacker) {
                return false;
            }

            return isVanillaPlayerAttackSource(source);
        }

        private static boolean isVanillaPlayerAttackSource(DamageSource source) {
            String msgId = source.type().msgId();

            return "player".equals(msgId)
                    || "player_attack".equals(msgId);
        }
    }
}