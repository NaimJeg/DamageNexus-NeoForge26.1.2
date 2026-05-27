package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

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

    public MutableComponent identifierName(Identifier id) {
        return Component.literal(identifierNamePlain(id));
    }

    public String identifierNamePlain(Identifier id) {
        if (id == null) {
            return "Unknown";
        }

        if ("minecraft".equals(id.getNamespace())
                || "damagenexus".equals(id.getNamespace())) {
            return humanize(id.getPath());
        }

        return id.getNamespace() + ":" + humanize(id.getPath());
    }

    public String rawId(Identifier id) {
        return id != null ? id.toString() : "unknown";
    }

    public String tagNamePlain(TagKey<?> tag) {
        return tag != null
                ? "#" + tag.location()
                : "#unknown";
    }

    public MutableComponent effectName(Identifier id) {
        if (id == null) {
            return Component.literal("Unknown Effect");
        }

        return Component.translatableWithFallback(
                "effect." + id.getNamespace() + "." + id.getPath(),
                identifierNamePlain(id)
        );
    }

    public String effectNamePlain(Identifier id) {
        return identifierNamePlain(id);
    }

    public String bucketNamePlain(Identifier id) {
        return identifierNamePlain(id);
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