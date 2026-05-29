package io.github.naimjeg.damagenexus.bridge.vanilla;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public final class VanillaBridgeLogger {

    private static final Logger LOGGER = LogUtils.getLogger();

    private VanillaBridgeLogger() {}

    public static void logSnapshot(
            VanillaDamageCapture.OffensiveSnapshot snapshot
    ) {
        if (!ModConfig.isDebugMode() || snapshot == null) {
            return;
        }

        VanillaDamageCapture.PreEventDelta delta = snapshot.preEventDelta();

        if (snapshot.hasEnchantDelta()) {
            LOGGER.info(
                    "[DN-Bridge] offensive_enchant attacker={} victim={} source={} weapon={} preEnchant={} postEnchant={} enchantDelta={} eventOriginal={}",
                    entityName(snapshot.attacker()),
                    entityName(snapshot.victim()),
                    sourceId(snapshot.source()),
                    snapshot.weapon().getHoverName().getString(),
                    snapshot.preEnchantDamage(),
                    snapshot.postEnchantDamage(),
                    snapshot.enchantDelta(),
                    snapshot.eventOriginalDamage()
            );
        }

        if (snapshot.preEventDelta().kind() != PreEventDeltaKind.NONE) {
            LOGGER.info(
                    "[DN-Bridge] pre_event kind={} reason={} source_id={} postEnchant={} eventOriginal={} ratio={} delta={}",
                    delta.kind(),
                    delta.reason(),
                    sourceId(snapshot.source()),
                    delta.postEnchantDamage(),
                    delta.eventOriginalDamage(),
                    delta.ratio(),
                    delta.delta()
            );
        }

        if (delta.kind() == PreEventDeltaKind.UNKNOWN) {
            LOGGER.warn(
                    "[DN-Bridge][UNKNOWN_PRE_EVENT_DELTA] attacker={} victim={} source={} weapon={} postEnchant={} eventOriginal={} ratio={} delta={} reason={}",
                    entityName(snapshot.attacker()),
                    entityName(snapshot.victim()),
                    sourceId(snapshot.source()),
                    snapshot.weapon().getHoverName().getString(),
                    delta.postEnchantDamage(),
                    delta.eventOriginalDamage(),
                    delta.ratio(),
                    delta.delta(),
                    delta.reason()
            );
        }

        if (snapshot.hasProjectileCriticalBonus()) {
            LOGGER.info(
                    "[DN-Bridge] projectile_crit_bonus attacker={} victim={} source={} weapon={} bonus={} eventOriginal={}",
                    entityName(snapshot.attacker()),
                    entityName(snapshot.victim()),
                    sourceId(snapshot.source()),
                    snapshot.weapon().getHoverName().getString(),
                    snapshot.projectileCriticalBonus(),
                    snapshot.eventOriginalDamage()
            );
        }
    }

    private static String entityName(Entity entity) {
        return entity != null
                ? entity.getName().getString()
                : "null";
    }

    private static String sourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }
}