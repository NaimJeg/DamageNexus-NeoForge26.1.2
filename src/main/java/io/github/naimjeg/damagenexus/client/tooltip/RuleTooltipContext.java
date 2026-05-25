package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;

public final class RuleTooltipContext {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    public String number(float value) {
        return FORMAT.format(value);
    }

    public String percent(float value) {
        return FORMAT.format(value * 100.0f);
    }

    public MutableComponent channelName(DamageChannel channel) {
        return Component.translatableWithFallback(
                "channel." + channel.id().getNamespace() + "." + channel.id().getPath(),
                humanize(channel.id().getPath())
        );
    }

    public String channelNamePlain(DamageChannel channel) {
        return humanize(channel.id().getPath());
    }

    private static String humanize(String path) {
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(part.charAt(0)));

            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        return builder.toString();
    }
}