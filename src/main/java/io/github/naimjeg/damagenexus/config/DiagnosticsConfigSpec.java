package io.github.naimjeg.damagenexus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DiagnosticsConfigSpec {
    public static ModConfigSpec.BooleanValue DEBUG_MODE;
    public static ModConfigSpec.BooleanValue ENABLE_POST_DAMAGE_DIAGNOSTICS;
    public static ModConfigSpec.EnumValue<ServerDebugLogVerbosity> SERVER_DEBUG_LOG_VERBOSITY;

    public static ModConfigSpec.EnumValue<ClientDebugLogForwardMode> CLIENT_DEBUG_LOG_FORWARD_MODE;
    public static ModConfigSpec.IntValue CLIENT_DEBUG_LOG_FORWARD_MAX_LINES_PER_TICK;
    public static ModConfigSpec.EnumValue<ClientDebugLogForwardVerbosity> CLIENT_DEBUG_LOG_FORWARD_VERBOSITY;
    public static ModConfigSpec.BooleanValue CLIENT_DEBUG_LOG_FORWARD_REQUIRE_RECEIVER_OPT_IN;

    private DiagnosticsConfigSpec() {
    }

    static void define(ModConfigSpec.Builder builder) {
        builder.push("diagnostics");

        DEBUG_MODE = builder
                .comment(
                        "Enable detailed combat transaction logging.",
                        "Set to true only for debugging.",
                        "This can output a large amount of logs during combat.",
                        "Default: false"
                )
                .define("debugMode", false);

        ENABLE_POST_DAMAGE_DIAGNOSTICS = builder
                .comment(
                        "Enable post-damage transaction diagnostics without enabling all debug logs.",
                        "This records and checks final health / absorption deltas after LivingDamageEvent.Post.",
                        "debugMode=true also enables these diagnostics.",
                        "Default: false"
                )
                .define("postDamageDiagnostics", false);

        SERVER_DEBUG_LOG_VERBOSITY = builder
                .comment(
                        "Controls which DamageNexus debug lines are written to the server log.",
                        "WARNINGS_ONLY: only suspicious/warning lines.",
                        "SUMMARY: warnings plus compact transaction summaries and compatibility diagnostics.",
                        "FULL: all trace lines, including processor/rule/phase spam.",
                        "Default: WARNINGS_ONLY"
                )
                .defineEnum(
                        "serverLogVerbosity",
                        ServerDebugLogVerbosity.WARNINGS_ONLY
                );

        builder.push("clientForwarding");

        CLIENT_DEBUG_LOG_FORWARD_MODE = builder
                .comment(
                        "Forward DamageNexus debug log lines to clients as system chat messages.",
                        "Requires debugMode=true.",
                        "OFF: disabled.",
                        "INVOLVED_PLAYERS: only attacker/victim players involved in the damage event.",
                        "OPS: online players with permission level 2+.",
                        "ALL_PLAYERS: every online player. Use only on private debug servers.",
                        "Default: OFF"
                )
                .defineEnum(
                        "mode",
                        ClientDebugLogForwardMode.OFF
                );

        CLIENT_DEBUG_LOG_FORWARD_VERBOSITY = builder
                .comment(
                        "Controls which DamageNexus debug lines may be forwarded to clients.",
                        "WARNINGS_ONLY: only suspicious/warning lines.",
                        "SUMMARY: warnings plus transaction summaries and compatibility diagnostics.",
                        "FULL: all trace lines, including processor/rule/phase spam.",
                        "Default: WARNINGS_ONLY"
                )
                .defineEnum(
                        "verbosity",
                        ClientDebugLogForwardVerbosity.WARNINGS_ONLY
                );

        CLIENT_DEBUG_LOG_FORWARD_MAX_LINES_PER_TICK = builder
                .comment(
                        "Maximum DamageNexus debug lines forwarded to clients per server tick.",
                        "Prevents chat/network spam when debugMode is enabled.",
                        "Default: 20"
                )
                .defineInRange(
                        "maxLinesPerTick",
                        20,
                        1,
                        200
                );

        CLIENT_DEBUG_LOG_FORWARD_REQUIRE_RECEIVER_OPT_IN = builder
                .comment(
                        "If true, players must opt in before receiving forwarded DamageNexus debug chat.",
                        "Recommended true for public test servers.",
                        "Default: true"
                )
                .define(
                        "requireReceiverOptIn",
                        true
                );

        builder.pop();
        builder.pop();
    }

    static DiagnosticsSettings bake() {
        return new DiagnosticsSettings(
                DEBUG_MODE.get(),
                ENABLE_POST_DAMAGE_DIAGNOSTICS.get(),
                SERVER_DEBUG_LOG_VERBOSITY.get(),
                CLIENT_DEBUG_LOG_FORWARD_MODE.get(),
                CLIENT_DEBUG_LOG_FORWARD_VERBOSITY.get(),
                CLIENT_DEBUG_LOG_FORWARD_MAX_LINES_PER_TICK.get(),
                CLIENT_DEBUG_LOG_FORWARD_REQUIRE_RECEIVER_OPT_IN.get()
        );
    }
}
