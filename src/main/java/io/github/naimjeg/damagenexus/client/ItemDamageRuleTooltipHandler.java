package io.github.naimjeg.damagenexus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
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
public final class ItemDamageRuleTooltipHandler {

    private ItemDamageRuleTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        DamageNexusClientTooltips.register();

        List<DamageRuleDefinition> rules =
                event.getItemStack().getOrDefault(
                        ModDataComponents.ITEM_DAMAGE_RULES.get(),
                        List.of()
                );

        if (rules.isEmpty()) {
            return;
        }

        List<DamageRuleDefinition> normalRules = new ArrayList<>();
        List<DamageRuleDefinition> conditionalRules = new ArrayList<>();

        for (DamageRuleDefinition rule : rules) {
            if (isUnconditionalRule(rule)) {
                normalRules.add(rule);
            } else {
                conditionalRules.add(rule);
            }
        }

        /*
         * 1. 默认直接显示无条件规则。
         *    无条件规则 = 没有 conditions，或者只有 always condition。
         */
        appendNormalRules(event, normalRules);

        /*
         * 2. 条件规则折叠显示。
         */
        Map<Identifier, List<DamageRuleDefinition>> conditionalGroups =
                groupRules(conditionalRules);

        if (conditionalGroups.isEmpty()) {
            return;
        }

        if (!isShiftDown()) {
            appendCollapsedConditionalGroups(event, conditionalGroups);
            return;
        }

        /*
         * 3. Shift 展开条件规则。
         */
        appendExpandedConditionalGroups(event, conditionalGroups);
    }

    private static void appendNormalRules(
            ItemTooltipEvent event,
            List<DamageRuleDefinition> normalRules
    ) {
        for (DamageRuleDefinition rule : normalRules) {
            for (DamageRuleOperation operation : rule.operations()) {
                MutableComponent line =
                        RuleTooltipDescriptions.describeEffect(
                                operation,
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
            Map<Identifier, List<DamageRuleDefinition>> conditionalGroups
    ) {
        for (Map.Entry<Identifier, List<DamageRuleDefinition>> group : conditionalGroups.entrySet()) {
            event.getToolTip().add(
                    Component.literal("[Shift] ")
                            .append(formatRuleGroupName(group.getKey(), group.getValue()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    private static void appendExpandedConditionalGroups(
            ItemTooltipEvent event,
            Map<Identifier, List<DamageRuleDefinition>> conditionalGroups
    ) {
        event.getToolTip().add(Component.empty());

        for (Map.Entry<Identifier, List<DamageRuleDefinition>> group : conditionalGroups.entrySet()) {
            List<DamageRuleDefinition> groupedRules = group.getValue();

            event.getToolTip().add(
                    formatRuleGroupName(group.getKey(), groupedRules)
                            .withStyle(ChatFormatting.AQUA)
            );

            List<DamageRuleCondition> conditions =
                    collectConditions(groupedRules);

            for (DamageRuleCondition condition : conditions) {
                event.getToolTip().add(
                        Component.literal("  ")
                                .append(RuleTooltipDescriptions.describeCondition(
                                        condition,
                                        RuleTooltipMode.DETAIL
                                ))
                                .withStyle(ChatFormatting.GRAY)
                );
            }

            for (DamageRuleDefinition rule : groupedRules) {
                for (DamageRuleOperation operation : rule.operations()) {
                    event.getToolTip().add(
                            Component.literal("  ")
                                    .append(RuleTooltipDescriptions.describeEffect(
                                            operation,
                                            RuleTooltipMode.DETAIL
                                    ))
                                    .withStyle(ChatFormatting.DARK_GREEN)
                    );
                }
            }
        }
    }

    private static Map<Identifier, List<DamageRuleDefinition>> groupRules(
            List<DamageRuleDefinition> rules
    ) {
        Map<Identifier, List<DamageRuleDefinition>> groups = new LinkedHashMap<>();

        for (DamageRuleDefinition rule : rules) {
            Identifier groupId = normalizeRuleGroupId(rule.id());

            groups.computeIfAbsent(groupId, ignored -> new ArrayList<>())
                    .add(rule);
        }

        return groups;
    }

    private static Identifier normalizeRuleGroupId(Identifier id) {
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

    private static List<DamageRuleCondition> collectConditions(
            List<DamageRuleDefinition> rules
    ) {
        List<DamageRuleCondition> result = new ArrayList<>();

        for (DamageRuleDefinition rule : rules) {
            for (DamageRuleCondition condition : rule.conditions()) {
                if (!result.contains(condition)) {
                    result.add(condition);
                }
            }
        }

        return result;
    }

    private static boolean isUnconditionalRule(DamageRuleDefinition rule) {
        if (rule.conditions().isEmpty()) {
            return true;
        }

        for (DamageRuleCondition condition : rule.conditions()) {
            if (!(condition instanceof AlwaysCondition)) {
                return false;
            }
        }

        return true;
    }

    private static MutableComponent formatRuleGroupName(
            Identifier id,
            List<DamageRuleDefinition> rules
    ) {
        for (DamageRuleDefinition rule : rules) {
            if (rule.display().name().isPresent()) {
                String name = rule.display().name().get();

                if (!name.isBlank()) {
                    return Component.literal(name);
                }
            }
        }

        return Component.translatableWithFallback(
                "damage_rule." + id.getNamespace() + "." + id.getPath(),
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