package io.github.naimjeg.damagenexus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.api.affix.AffixEntry;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.api.affix.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.client.tooltip.DamageNexusClientTooltips;
import io.github.naimjeg.damagenexus.client.tooltip.RuleTooltipDescriptions;
import io.github.naimjeg.damagenexus.client.tooltip.RuleTooltipMode;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(
        modid = DamageNexus.MODID,
        value = Dist.CLIENT
)
public final class AffixTooltipHandler {

    private AffixTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        DamageNexusClientTooltips.register();

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
         *    普通词条 = 没有条件，或者只有 always 条件。
         */
        appendNormalAffixes(event, normalAffixes);

        /*
         * 2. 条件词条折叠显示。
         */
        Map<Identifier, List<AffixEntry>> conditionalGroups =
                groupAffixes(conditionalAffixes);

        if (conditionalGroups.isEmpty()) {
            return;
        }

        if (!isShiftDown()) {
            appendCollapsedConditionalGroups(event, conditionalGroups);
            return;
        }

        /*
         * 3. Shift 展开条件词条。
         */
        appendExpandedConditionalGroups(event, conditionalGroups);
    }

    private static void appendNormalAffixes(
            ItemTooltipEvent event,
            List<AffixEntry> normalAffixes
    ) {
        for (AffixEntry affix : normalAffixes) {
            for (AffixEffect effect : affix.effects()) {
                MutableComponent line =
                        RuleTooltipDescriptions.describeEffect(
                                effect,
                                RuleTooltipMode.NORMAL
                        );

                event.getToolTip().add(
                        line.withStyle(ChatFormatting.DARK_GREEN)
                );
            }
        }
    }

    private static void appendCollapsedConditionalGroups(
            ItemTooltipEvent event,
            Map<Identifier, List<AffixEntry>> conditionalGroups
    ) {
        for (Identifier groupId : conditionalGroups.keySet()) {
            event.getToolTip().add(
                    Component.literal("[Shift] ")
                            .append(formatAffixGroupName(groupId))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    private static void appendExpandedConditionalGroups(
            ItemTooltipEvent event,
            Map<Identifier, List<AffixEntry>> conditionalGroups
    ) {
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
                                .append(RuleTooltipDescriptions.describeCondition(
                                        condition,
                                        RuleTooltipMode.DETAIL
                                ))
                                .withStyle(ChatFormatting.GRAY)
                );
            }

            for (AffixEntry affix : group.getValue()) {
                for (AffixEffect effect : affix.effects()) {
                    event.getToolTip().add(
                            Component.literal("  ")
                                    .append(RuleTooltipDescriptions.describeEffect(
                                            effect,
                                            RuleTooltipMode.DETAIL
                                    ))
                                    .withStyle(ChatFormatting.DARK_GREEN)
                    );
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
                if (!result.contains(condition)) {
                    result.add(condition);
                }
            }
        }

        return result;
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

    private static MutableComponent formatAffixGroupName(Identifier id) {
        return Component.translatableWithFallback(
                "affix." + id.getNamespace() + "." + id.getPath(),
                humanize(id.getPath())
        );
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