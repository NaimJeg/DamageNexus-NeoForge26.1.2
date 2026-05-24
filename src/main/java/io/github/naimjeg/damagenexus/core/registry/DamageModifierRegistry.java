package io.github.naimjeg.damagenexus.core.registry;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.Identifier;

public final class DamageModifierRegistry {
    private static final Object2IntOpenHashMap<Identifier> PRE_IDS =
            new Object2IntOpenHashMap<>();

    private static boolean frozen = false;

    static {
        PRE_IDS.defaultReturnValue(-1);
    }

    private DamageModifierRegistry() {}

    public static int registerPreModifier(Identifier id) {
        if (frozen) {
            throw new IllegalStateException(
                    "Cannot register damage modifier after registry is frozen: " + id
            );
        }

        int existing = PRE_IDS.getInt(id);
        if (existing >= 0) {
            return existing;
        }

        int next = PRE_IDS.size();
        PRE_IDS.put(id, next);
        return next;
    }

    public static int getPreModifierId(Identifier id) {
        int result = PRE_IDS.getInt(id);
        if (result < 0) {
            throw new IllegalArgumentException("Unknown pre modifier id: " + id);
        }
        return result;
    }

    public static int preModifierCount() {
        return PRE_IDS.size();
    }

    public static void freeze() {
        if (frozen) return;

        if (PRE_IDS.isEmpty()) {
            throw new IllegalStateException(
                    "DamageModifierRegistry cannot be frozen before any modifier id is registered."
            );
        }

        frozen = true;
    }

    public static void requireFrozen() {
        if (!frozen) {
            throw new IllegalStateException(
                    "DamageModifierRegistry is not frozen yet. Did you forget to call DamageNexusModifierIds.register() and DamageModifierRegistry.freeze()?"
            );
        }
    }

    public static boolean isFrozen() {
        return frozen;
    }
}