package io.github.naimjeg.damagenexus.diagnostics.logging;

public record CombatTraceContext(
        long damageId,
        String attackerName,
        String victimName,
        String sourceId,
        String initialChannel,
        float eventOriginalAmount,
        float initialBaseAmount
) {
    public String prefix() {
        return "[DN#" + damageId + "]";
    }
}

