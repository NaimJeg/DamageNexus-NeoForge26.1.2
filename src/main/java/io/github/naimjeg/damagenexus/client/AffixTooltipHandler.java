package io.github.naimjeg.damagenexus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.api.affix.AffixEntry;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.api.affix.condition.*;
import io.github.naimjeg.damagenexus.api.affix.effect.*;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(
        modid = DamageNexus.MODID,
        value = Dist.CLIENT
)
public final class AffixTooltipHandler {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private AffixTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        List<AffixEntry> affixes =
                event.getItemStack().getOrDefault(
                        ModDataComponents.ITEM_AFFIXES.get(),
                        List.of()
                );

        if (affixes.isEmpty()) {
            return;
        }

        List<AffixEntry> normalAffixes = new ArrayList<>();
        List<AffixEntry> conditionalAffixes = new ArrayList<>();

        for (AffixEntry affix : affixes) {
            if (isUnconditionalAffix(affix)) {
                normalAffixes.add(affix);
            } else {
                conditionalAffixes.add(affix);
            }
        }

        /*
         * 1. 默认直接显示普通词条。
         */
        for (AffixEntry affix : normalAffixes) {
            for (AffixEffect effect : affix.effects()) {
                MutableComponent line = formatNormalEffect(effect);

                if (line != null) {
                    event.getToolTip().add(
                            line.withStyle(ChatFormatting.DARK_GREEN)
                    );
                }
            }
        }

        /*
         * 2. 条件词条按 affix group 折叠。
         */
        Map<Identifier, List<AffixEntry>> conditionalGroups =
                groupAffixes(conditionalAffixes);

        if (conditionalGroups.isEmpty()) {
            return;
        }

        if (!isShiftDown()) {
            for (Identifier groupId : conditionalGroups.keySet()) {
                event.getToolTip().add(
                        Component.literal("[Shift] ")
                                .append(formatAffixGroupName(groupId))
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }

            return;
        }

        /*
         * 3. Shift 展开条件词条。
         */
        event.getToolTip().add(Component.empty());

        for (Map.Entry<Identifier, List<AffixEntry>> group : conditionalGroups.entrySet()) {
            event.getToolTip().add(
                    formatAffixGroupName(group.getKey())
                            .withStyle(ChatFormatting.AQUA)
            );

            List<AffixCondition> conditions =
                    collectConditions(group.getValue());

            for (AffixCondition condition : conditions) {
                event.getToolTip().add(
                        Component.literal("  ")
                                .append(formatCondition(condition))
                                .withStyle(ChatFormatting.GRAY)
                );
            }

            for (AffixEntry affix : group.getValue()) {
                for (AffixEffect effect : affix.effects()) {
                    MutableComponent detail = formatDetailEffect(effect);

                    if (detail != null) {
                        event.getToolTip().add(
                                Component.literal("  ")
                                        .append(detail)
                                        .withStyle(ChatFormatting.DARK_GREEN)
                        );
                    }
                }
            }
        }
    }

    private static Map<Identifier, List<AffixEntry>> groupAffixes(List<AffixEntry> affixes) {
        Map<Identifier, List<AffixEntry>> groups = new LinkedHashMap<>();

        for (AffixEntry affix : affixes) {
            Identifier groupId = normalizeAffixGroupId(affix.id());

            groups.computeIfAbsent(groupId, ignored -> new ArrayList<>())
                    .add(affix);
        }

        return groups;
    }

    private static Identifier normalizeAffixGroupId(Identifier id) {
        String path = id.getPath();

        path = stripSuffix(path, "_base");
        path = stripSuffix(path, "_multi");
        path = stripSuffix(path, "_pre");
        path = stripSuffix(path, "_post");
        path = stripSuffix(path, "_global");
        path = stripSuffix(path, "_final");

        return Identifier.fromNamespaceAndPath(id.getNamespace(), path);
    }

    private static String stripSuffix(String value, String suffix) {
        if (value.endsWith(suffix)) {
            return value.substring(0, value.length() - suffix.length());
        }

        return value;
    }

    private static List<AffixCondition> collectConditions(List<AffixEntry> entries) {
        List<AffixCondition> result = new ArrayList<>();

        for (AffixEntry entry : entries) {
            for (AffixCondition condition : entry.conditions()) {
                if (!containsConditionOfSameType(result, condition)) {
                    result.add(condition);
                }
            }
        }

        return result;
    }

    private static boolean containsConditionOfSameType(
            List<AffixCondition> existing,
            AffixCondition target
    ) {
        for (AffixCondition condition : existing) {
            if (condition.type().equals(target.type())) {
                return true;
            }
        }

        return false;
    }

    private static MutableComponent formatNormalEffect(AffixEffect effect) {
        return switch (effect) {

            case AddBaseDamageEffect e ->
                    Component.literal("[+] ")
                            .append(Component.translatableWithFallback(
                                    "tooltip.damagenexus.normal.add_base",
                                    "+" + FORMAT.format(e.value()) + " " + channelNamePlain(e.channel()) + " Damage",
                                    FORMAT.format(e.value()),
                                    channelName(e.channel())
                            ));

            case AddChannelPreMultiplierEffect e ->
                    Component.literal("[x] ")
                            .append(Component.translatableWithFallback(
                                    "tooltip.damagenexus.normal.add_channel_pre",
                                    "+" + FORMAT.format(e.value() * 100.0f) + "% " + channelNamePlain(e.channel()) + " Damage",
                                    FORMAT.format(e.value() * 100.0f),
                                    channelName(e.channel())
                            ));

            case AddChannelPostMultiplierEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.normal.add_channel_post",
                            "+" + FORMAT.format(e.value() * 100.0f) + "% " + channelNamePlain(e.channel()) + " Damage [x]",
                            FORMAT.format(e.value() * 100.0f),
                            channelName(e.channel())
                    );

            case AddGlobalPostMultiplierEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.normal.add_global_post",
                            "+" + FORMAT.format(e.value() * 100.0f) + "% Damage [x]",
                            FORMAT.format(e.value() * 100.0f)
                    );


            case OverrideFinalDamageEffect ignored ->
                    null;

            default ->
                    null;
        };
    }

    private static MutableComponent formatDetailEffect(AffixEffect effect) {
        return switch (effect) {

            case AddBaseDamageEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.detail.add_base",
                            "+" + channelNamePlain(e.channel()) + " Damage",
                            channelName(e.channel())
                    );

            case AddChannelPreMultiplierEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.detail.add_channel_pre",
                            "+" + FORMAT.format(e.value() * 100.0f) + "% " + channelNamePlain(e.channel()) + " Damage",
                            FORMAT.format(e.value() * 100.0f),
                            channelName(e.channel())
                    );

            case AddChannelPostMultiplierEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.detail.add_channel_post",
                            "+" + FORMAT.format(e.value() * 100.0f) + "% " + channelNamePlain(e.channel()) + " Damage",
                            FORMAT.format(e.value() * 100.0f),
                            channelName(e.channel())
                    );

            case AddGlobalPostMultiplierEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.detail.add_global_post",
                            "+" + FORMAT.format(e.value() * 100.0f) + "% Damage",
                            FORMAT.format(e.value() * 100.0f)
                    );

            case OverrideFinalDamageEffect e ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.detail.override_final",
                            "Set final damage to " + FORMAT.format(e.value()),
                            FORMAT.format(e.value())
                    );

            default ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.detail.unknown",
                            "Unknown effect"
                    );
        };
    }

    private static MutableComponent formatCondition(AffixCondition condition) {
        return switch (condition) {
            case TargetOnFireCondition ignored ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.target_on_fire",
                            "When the enemy is burning:"
                    );

            case AttackerHealthBelowCondition c ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.attacker_health_below",
                            "When your health is below " + FORMAT.format(c.threshold() * 100.0f) + "%:",
                            FORMAT.format(c.threshold() * 100.0f)
                    );

            case TargetEntityTypeTagCondition c ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.target_entity_type_tag",
                            "Against matching targets:"
                    );

            case DamageSourceTagCondition c ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.damage_source_tag",
                            "When the damage source matches:"
                    );

            case EntityCounterAtLeastCondition c ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.entity_counter_at_least",
                            "When counter is at least " + c.value() + ":",
                            c.value()
                    );

            case AlwaysCondition ignored ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.always",
                            "Always:"
                    );

            default ->
                    Component.translatableWithFallback(
                            "tooltip.damagenexus.condition.unknown",
                            "When condition is met:"
                    );
        };
    }

    private static MutableComponent formatAffixGroupName(Identifier id) {
        return Component.translatableWithFallback(
                "affix." + id.getNamespace() + "." + id.getPath(),
                humanize(id.getPath())
        );
    }

    private static MutableComponent channelName(DamageChannel channel) {
        return Component.translatableWithFallback(
                "channel." + channel.id().getNamespace() + "." + channel.id().getPath(),
                humanize(channel.id().getPath())
        );
    }

    private static String channelNamePlain(DamageChannel channel) {
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

    private static boolean isUnconditionalAffix(AffixEntry affix) {
        if (affix.conditions().isEmpty()) {
            return true;
        }

        for (AffixCondition condition : affix.conditions()) {
            if (!(condition instanceof AlwaysCondition)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isShiftDown() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }

        Window window = minecraft.getWindow();

        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}