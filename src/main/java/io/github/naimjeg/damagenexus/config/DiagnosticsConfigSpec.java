package io.github.naimjeg.damagenexus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DiagnosticsConfigSpec {
    public static ModConfigSpec.EnumValue<DiagnosticDomain> DIAGNOSTIC_MODE;
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

        DIAGNOSTIC_MODE = builder
                .comment(
                        "Primary DamageNexus diagnostics mode.",
                        "OFF: no transaction diagnostics or trace output; normal warnings may still be logged.",
                        "COMPATIBILITY: emit compatibility diagnostics for vanilla/other-mod interaction checks without normal trace spam.",
                        "SUMMARY: emit compact transaction summaries plus compatibility diagnostics.",
                        "FULL_TRACE: emit verbose processor, rule, mutation, contribution, and bucket-level trace details.",
                        "Default: OFF"
                )
                .defineEnum("mode", DiagnosticDomain.OFF);

        DEBUG_MODE = builder
                .comment(
                        "Deprecated compatibility alias for older configs.",
                        "Prefer diagnostics.mode.",
                        "When true, DamageNexus derives at least SUMMARY diagnostics from the legacy verbosity settings.",
                        "Default: false"
                )
                .define("debugMode", false);

        ENABLE_POST_DAMAGE_DIAGNOSTICS = builder
                .comment(
                        "Deprecated compatibility alias for older configs.",
                        "Prefer diagnostics.mode=COMPATIBILITY when checking vanilla or other-mod post-damage interactions.",
                        "When true, transaction tracking and compatibility diagnostics are enabled without full trace detail.",
                        "Server output still follows legacy serverLogVerbosity unless diagnostics.mode is set.",
                        "Default: false"
                )
                .define("postDamageDiagnostics", false);

        SERVER_DEBUG_LOG_VERBOSITY = builder
                .comment(
                        "Deprecated legacy verbosity hint used with debugMode=true.",
                        "Prefer diagnostics.mode for the main diagnostics level.",
                        "WARNINGS_ONLY: only suspicious/warning lines.",
                        "SUMMARY: derive SUMMARY diagnostics from debugMode=true.",
                        "FULL: derive FULL_TRACE diagnostics from debugMode=true.",
                        "Default: WARNINGS_ONLY"
                )
                .defineEnum(
                        "serverLogVerbosity",
                        ServerDebugLogVerbosity.WARNINGS_ONLY
                );

        builder.push("clientForwarding");

        CLIENT_DEBUG_LOG_FORWARD_MODE = builder
                .comment(
                        "Forward selected DamageNexus diagnostics to clients as system chat messages.",
                        "Requires diagnostics.mode or legacy aliases to enable the selected diagnostic kind.",
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
                        "Caps which enabled DamageNexus diagnostics may be forwarded to clients.",
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
                        "Prevents chat/network spam when client forwarding is enabled.",
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
                DIAGNOSTIC_MODE.get(),
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
