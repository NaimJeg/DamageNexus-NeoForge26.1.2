package io.github.naimjeg.damagenexus.diagnostics.logging;

import java.util.LinkedHashMap;
import java.util.Map;

final class CompatibilityDiagnosticRateLimiter {

    private static final int MAX_KEYS = 128;
    private static final long WINDOW_MILLIS = 5_000L;
    private static final int MAX_PER_WINDOW = 5;

    private static final Map<String, Entry> ENTRIES =
            new LinkedHashMap<>(16, 0.75f, true);

    private CompatibilityDiagnosticRateLimiter() {
    }

    static synchronized Decision check(String key) {
        long now = System.currentTimeMillis();
        String safeKey = key == null || key.isBlank() ? "unknown" : key;

        Entry entry = ENTRIES.get(safeKey);

        if (entry == null) {
            evictIfNeeded();
            ENTRIES.put(safeKey, new Entry(now, 1, 0));
            return new Decision(true, 0);
        }

        if (now - entry.windowStartMillis >= WINDOW_MILLIS) {
            int suppressed = entry.suppressed;
            entry.windowStartMillis = now;
            entry.emitted = 1;
            entry.suppressed = 0;
            return new Decision(true, suppressed);
        }

        if (entry.emitted < MAX_PER_WINDOW) {
            entry.emitted++;
            return new Decision(true, 0);
        }

        entry.suppressed++;
        return new Decision(false, 0);
    }

    private static void evictIfNeeded() {
        if (ENTRIES.size() < MAX_KEYS) {
            return;
        }

        var iterator = ENTRIES.keySet().iterator();

        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    static final class Decision {
        private final boolean allowed;
        private final int suppressed;

        private Decision(boolean allowed, int suppressed) {
            this.allowed = allowed;
            this.suppressed = suppressed;
        }

        boolean allowed() {
            return allowed;
        }

        int suppressed() {
            return suppressed;
        }

        String suffix() {
            return suppressed > 0
                    ? " suppressed_in_previous_window=" + suppressed
                    : "";
        }
    }

    private static final class Entry {
        private long windowStartMillis;
        private int emitted;
        private int suppressed;

        private Entry(
                long windowStartMillis,
                int emitted,
                int suppressed
        ) {
            this.windowStartMillis = windowStartMillis;
            this.emitted = emitted;
            this.suppressed = suppressed;
        }
    }
}
