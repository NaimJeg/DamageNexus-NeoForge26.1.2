package io.github.naimjeg.damagenexus.core.registry;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.Identifier;

public final class PreMultiplierBucketRegistry {

    private static final Object2IntOpenHashMap<Identifier> BUCKET_IDS =
            new Object2IntOpenHashMap<>();

    private static boolean frozen = false;

    static {
        BUCKET_IDS.defaultReturnValue(-1);
    }

    private PreMultiplierBucketRegistry() {}

    public static synchronized int registerPreMultiplierBucket(Identifier id) {
        if (frozen) {
            throw new IllegalStateException(
                    "Cannot register pre multiplier bucket after registry is frozen: " + id
            );
        }

        int existing = BUCKET_IDS.getInt(id);

        if (existing >= 0) {
            return existing;
        }

        int next = BUCKET_IDS.size();
        BUCKET_IDS.put(id, next);
        return next;
    }

    public static int getPreMultiplierBucketId(Identifier id) {
        int result = BUCKET_IDS.getInt(id);

        if (result < 0) {
            throw new IllegalArgumentException(
                    "Unknown pre multiplier bucket id: " + id
            );
        }

        return result;
    }

    public static boolean containsPreMultiplierBucket(Identifier id) {
        return BUCKET_IDS.containsKey(id);
    }

    public static int bucketCount() {
        return BUCKET_IDS.size();
    }

    public static void freeze() {
        if (frozen) {
            return;
        }

        if (BUCKET_IDS.isEmpty()) {
            throw new IllegalStateException(
                    "PreMultiplierBucketRegistry cannot be frozen before any bucket is registered."
            );
        }

        frozen = true;
    }

    public static void requireFrozen() {
        if (!frozen) {
            throw new IllegalStateException(
                    "PreMultiplierBucketRegistry is not frozen yet. Did you forget to call PreMultiplierBuckets.register() and PreMultiplierBucketRegistry.freeze()?"
            );
        }
    }


    public static boolean isFrozen() {
        return frozen;
    }
}