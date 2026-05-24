package io.github.naimjeg.damagenexus.core.pipeline;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.registry.ModModifiers;
import org.slf4j.Logger;

public class DamageNexusPipeline {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<DamagePhase, List<IDamageModifier>> PHASE_MODIFIERS = new EnumMap<>(DamagePhase.class);

    private static boolean isBuilt = false;

    private static void buildPipeline() {
        if (isBuilt) return;

        for (DamagePhase phase : DamagePhase.values()) {
            PHASE_MODIFIERS.put(phase, new ArrayList<>());
        }

        if (ModModifiers.REGISTRY != null) {
            for (IDamageModifier mod : ModModifiers.REGISTRY) {
                DamagePhase phase = mod.getPhase();

                List<IDamageModifier> list = PHASE_MODIFIERS.get(phase);

                if (list == null) {
                    LOGGER.error("Modifier {} returned invalid phase {}", mod.getClass().getName(), phase);
                    continue;
                }

                list.add(mod);
            }
        }

        for (List<IDamageModifier> list : PHASE_MODIFIERS.values()) {
            list.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        }

        isBuilt = true;

        if (ModConfig.isDebugMode()) {
            for (DamagePhase phase : DamagePhase.values()) {
                LOGGER.info("[DamageNexus] Pipeline phase {}:", phase);

                for (IDamageModifier modifier : PHASE_MODIFIERS.get(phase)) {
                    LOGGER.info(
                            "  - {} priority={}",
                            modifier.getClass().getSimpleName(),
                            modifier.getPriority()
                    );
                }
            }
        }
    }

    public static void clearCache() {
        isBuilt = false;
        PHASE_MODIFIERS.clear();
    }

    public static void execute(DamageNexusContext ctx) {
        if (!isBuilt) buildPipeline();
        if (!ctx.isManaged) return;


        runPhase(DamagePhase.BASE_MODIFICATION, ctx);
        runPhase(DamagePhase.TYPE_SCALING, ctx);
        runPhase(DamagePhase.CRITICAL_HIT, ctx);
        runPhase(DamagePhase.CONDITIONAL_MULTI, ctx);
        runPhase(DamagePhase.GLOBAL_ADJUSTMENT, ctx);

        ctx.finalizeOffensiveDamage();

        runPhase(DamagePhase.MITIGATION_SETUP, ctx);

        ctx.calculateDefensiveDamage();

        runPhase(DamagePhase.FINAL_OVERRIDE, ctx);

        ctx.applyIncomingDamageToEvent();
    }

    private static void runPhase(DamagePhase phase, DamageNexusContext ctx) {
        List<IDamageModifier> mods = PHASE_MODIFIERS.get(phase);
        if (mods == null || mods.isEmpty()) return;

        ctx.setCurrentProcessingPhase(phase);
        boolean phaseLogged = false;

        for (IDamageModifier mod : mods) {
            if (mod.canHandle(ctx)) {

                try {
                    if (!phaseLogged) {
                        ctx.debugger.logPhase(phase);
                        phaseLogged = true;
                    }

                    ctx.debugger.logModifier(mod.getClass().getSimpleName());

                    mod.apply(ctx);
                } catch (Exception e) {
                    LOGGER.error(
                            "[DamageNexus] Modifier {} crashed during phase {}",
                            mod.getClass().getName(),
                            phase,
                            e
                    );
                }
            }
        }
    }
}