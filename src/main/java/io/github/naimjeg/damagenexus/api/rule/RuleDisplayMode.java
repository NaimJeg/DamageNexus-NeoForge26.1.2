package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum RuleDisplayMode {
    /**
     * Rule executes but is not shown in normal item tooltip.
     */
    HIDDEN,

    /**
     * Rule may be shown directly as a simple standalone effect.
     */
    SIMPLE,

    /**
     * Rule belongs to an affix. The affix controls normal display.
     */
    AFFIX_MEMBER;

    public static final Codec<RuleDisplayMode> CODEC =
            Codec.STRING.xmap(
                    name -> RuleDisplayMode.valueOf(name.toUpperCase(Locale.ROOT)),
                    mode -> mode.name().toLowerCase(Locale.ROOT)
            );
}