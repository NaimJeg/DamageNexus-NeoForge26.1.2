package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.util.IdentifierText;
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

    public String signedNumber(float value) {
        if (value > 0.0f) {
            return "+" + number(value);
        }

        return number(value);
    }

    public String signedPercent(float value) {
        if (value > 0.0f) {
            return "+" + percent(value) + "%";
        }

        return percent(value) + "%";
    }

    public String percentWithSymbol(float value) {
        return percent(value) + "%";
    }

    public String phaseNamePlain(Enum<?> phase) {
        return humanize(phase.name().toLowerCase(java.util.Locale.ROOT));
    }

    public String enumNamePlain(Enum<?> value) {
        return humanize(value.name().toLowerCase(java.util.Locale.ROOT));
    }

    public String channelNamePlain(DamageChannel channel) {
        return channelNamePlain(channel.id());
    }

    public Component channelName(Identifier id) {
        return Component.translatableWithFallback(
                "damage_channel." + idToLangPath(id),
                humanize(identifierPath(id))
        );
    }

    public String channelNamePlain(Identifier id) {
        return humanize(identifierPath(id));
    }

    public Component damageTypeName(Identifier id) {
        return Component.translatableWithFallback(
                "damage_type." + idToLangPath(id),
                humanize(identifierPath(id))
        );
    }

    public String damageTypeNamePlain(Identifier id) {
        return humanize(identifierPath(id));
    }

    public Component damageTypeTagName(String tagId) {
        return Component.translatableWithFallback(
                "damage_type_tag." + tagId.replace(':', '.'),
                "#" + tagId
        );
    }

    private static String idToLangPath(Identifier id) {
        return id.toString().replace(':', '.');
    }

    private static String identifierPath(Identifier id) {
        String raw = id.toString();
        int colon = raw.indexOf(':');

        if (colon < 0 || colon + 1 >= raw.length()) {
            return raw;
        }

        return raw.substring(colon + 1);
    }


    public String rawId(Identifier id) {
        return id.toString();
    }

    public String tagNamePlain(TagKey<?> tag) {
        return "#" + tag.location();
    }

    public MutableComponent effectName(Identifier effectId) {
        return Component.translatableWithFallback(
                "effect." + IdentifierText.namespace(effectId) + "." + IdentifierText.path(effectId),
                humanize(IdentifierText.path(effectId))
        );
    }

    public MutableComponent entityTypeName(Identifier entityTypeId) {
        return Component.translatableWithFallback(
                "entity." + IdentifierText.namespace(entityTypeId) + "." + IdentifierText.path(entityTypeId),
                humanize(IdentifierText.path(entityTypeId))
        );
    }

    public String entityTypeNamePlain(Identifier entityTypeId) {
        return humanize(IdentifierText.path(entityTypeId));
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