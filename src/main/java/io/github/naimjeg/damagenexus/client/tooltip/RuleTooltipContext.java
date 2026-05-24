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
        return phase.name();
    }

    public String enumNamePlain(Enum<?> value) {
        return value.name();
    }

    public String channelNamePlain(DamageChannel channel) {
        return channel.id().toString();
    }

    public Component channelName(Identifier id) {
        return Component.translatable("damage_channel." + idToLangPath(id));
    }

    public String channelNamePlain(Identifier id) {
        return id.toString();
    }

    public Component damageTypeName(Identifier id) {
        return Component.translatable("damage_type." + idToLangPath(id));
    }

    public String damageTypeNamePlain(Identifier id) {
        return id.toString();
    }

    public Component damageTypeTagName(String tagId) {
        return Component.translatable("damage_type_tag." + tagId.replace(':', '.'));
    }

    private static String idToLangPath(Identifier id) {
        return id.toString().replace(':', '.');
    }

    public String rawId(Identifier id) {
        return id.toString();
    }

    public String tagNamePlain(TagKey<?> tag) {
        return "#" + tag.location();
    }

    public MutableComponent effectName(Identifier effectId) {
        return Component.translatable(
                "effect." + IdentifierText.namespace(effectId) + "." + IdentifierText.path(effectId)
        );
    }

    public MutableComponent entityTypeName(Identifier entityTypeId) {
        return Component.translatable(
                "entity." + IdentifierText.namespace(entityTypeId) + "." + IdentifierText.path(entityTypeId)
        );
    }

    public String entityTypeNamePlain(Identifier entityTypeId) {
        return entityTypeId.toString();
    }
}
