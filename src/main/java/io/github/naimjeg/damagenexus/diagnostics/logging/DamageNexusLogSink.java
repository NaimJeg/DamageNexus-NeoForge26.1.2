package io.github.naimjeg.damagenexus.diagnostics.logging;

import io.github.naimjeg.damagenexus.config.DamageNexusConfig;
import io.github.naimjeg.damagenexus.config.DiagnosticsSettings;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public final class DamageNexusLogSink {

    private DamageNexusLogSink() {
    }

    public static void info(
            Logger logger,
            Entity attacker,
            Entity victim,
            String template,
            Object... args
    ) {
        info(
                DamageNexusLogKind.TRACE_DETAIL,
                logger,
                attacker,
                victim,
                template,
                args
        );
    }

    public static void info(
            DamageNexusLogKind kind,
            Logger logger,
            Entity attacker,
            Entity victim,
            String template,
            Object... args
    ) {
        DamageNexusLogKind effectiveKind = effectiveKind(kind);

        boolean logToServer = shouldLogInfoToServer(effectiveKind);
        boolean mayForwardToClient =
                DamageNexusConfig.current()
                        .diagnostics()
                        .shouldForwardDebugLogsToClient();

        if (!logToServer && !mayForwardToClient) {
            return;
        }

        if (logToServer) {
            logger.info(template, args);
        }

        if (mayForwardToClient) {
            ClientDebugLogForwarder.forward(
                    attacker,
                    victim,
                    render(template, args),
                    effectiveKind
            );
        }
    }

    public static void warn(
            Logger logger,
            Entity attacker,
            Entity victim,
            String template,
            Object... args
    ) {
        warn(
                DamageNexusLogKind.WARNING,
                logger,
                attacker,
                victim,
                template,
                args
        );
    }

    public static void warn(
            DamageNexusLogKind kind,
            Logger logger,
            Entity attacker,
            Entity victim,
            String template,
            Object... args
    ) {
        DamageNexusLogKind effectiveKind = kind == null
                ? DamageNexusLogKind.WARNING
                : kind;

        logger.warn(template, args);

        if (DamageNexusConfig.current()
                .diagnostics()
                .shouldForwardDebugLogsToClient()) {
            ClientDebugLogForwarder.forward(
                    attacker,
                    victim,
                    "WARN " + render(template, args),
                    effectiveKind
            );
        }
    }

    private static boolean shouldLogInfoToServer(DamageNexusLogKind kind) {
        if (kind == DamageNexusLogKind.WARNING
                || kind == DamageNexusLogKind.LIFECYCLE) {
            return true;
        }

        DiagnosticsSettings diagnostics =
                DamageNexusConfig.current().diagnostics();

        if (!diagnostics.debugMode()
                && !diagnostics.postDamageDiagnosticsEnabled()) {
            return false;
        }

        return switch (diagnostics.serverLogVerbosity()) {
            case WARNINGS_ONLY -> false;

            case SUMMARY -> kind == DamageNexusLogKind.TRACE_SUMMARY
                    || kind == DamageNexusLogKind.COMPATIBILITY;

            case FULL -> true;
        };
    }

    private static DamageNexusLogKind effectiveKind(DamageNexusLogKind kind) {
        return kind == null ? DamageNexusLogKind.TRACE_DETAIL : kind;
    }

    private static String render(String template, Object... args) {
        if (template == null) {
            return "";
        }

        if (args == null || args.length == 0) {
            return template;
        }

        StringBuilder builder = new StringBuilder(template.length() + args.length * 8);
        int argIndex = 0;
        int cursor = 0;

        while (cursor < template.length()) {
            int placeholder = template.indexOf("{}", cursor);

            if (placeholder < 0) {
                builder.append(template, cursor, template.length());
                break;
            }

            builder.append(template, cursor, placeholder);

            if (argIndex < args.length) {
                builder.append(args[argIndex++]);
            } else {
                builder.append("{}");
            }

            cursor = placeholder + 2;
        }

        while (argIndex < args.length) {
            Object trailing = args[argIndex++];

            if (trailing instanceof Throwable) {
                continue;
            }

            builder.append(' ').append(trailing);
        }

        return builder.toString();
    }
}

