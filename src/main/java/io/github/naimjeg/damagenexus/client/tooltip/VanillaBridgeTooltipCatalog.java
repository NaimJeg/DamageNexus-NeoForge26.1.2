package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.enums.DamageApplicationBucket;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.builtin.rule.condition.TargetEntityTypeTagCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.AddBaseDamageOperation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

public final class VanillaBridgeTooltipCatalog {

    private static final Identifier SHARPNESS =
            minecraft("sharpness");

    private static final Identifier SMITE =
            minecraft("smite");

    private static final Identifier BANE_OF_ARTHROPODS =
            minecraft("bane_of_arthropods");

    private static final TagKey<EntityType<?>> SENSITIVE_TO_SMITE =
            entityTypeTag("sensitive_to_smite");

    private static final TagKey<EntityType<?>> SENSITIVE_TO_BANE_OF_ARTHROPODS =
            entityTypeTag("sensitive_to_bane_of_arthropods");

    private VanillaBridgeTooltipCatalog() {}

    public static Optional<VanillaBridgeTooltipSpec> create(
            Identifier enchantmentId,
            int level
    ) {
        if (SHARPNESS.equals(enchantmentId)) {
            return Optional.of(sharpness(enchantmentId, level));
        }

        if (SMITE.equals(enchantmentId)) {
            return Optional.of(smite(enchantmentId, level));
        }

        if (BANE_OF_ARTHROPODS.equals(enchantmentId)) {
            return Optional.of(baneOfArthropods(enchantmentId, level));
        }

        return Optional.empty();
    }

    private static VanillaBridgeTooltipSpec sharpness(
            Identifier source,
            int level
    ) {
        float value = sharpnessValue(level);

        return meleeBase(
                source,
                level,
                value,
                List.of()
        );
    }

    private static VanillaBridgeTooltipSpec smite(
            Identifier source,
            int level
    ) {
        float value = 2.5f * level;

        return meleeBase(
                source,
                level,
                value,
                List.of(new TargetEntityTypeTagCondition(SENSITIVE_TO_SMITE))
        );
    }

    private static VanillaBridgeTooltipSpec baneOfArthropods(
            Identifier source,
            int level
    ) {
        float value = 2.5f * level;

        return meleeBase(
                source,
                level,
                value,
                List.of(new TargetEntityTypeTagCondition(
                        SENSITIVE_TO_BANE_OF_ARTHROPODS
                ))
        );
    }

    private static VanillaBridgeTooltipSpec meleeBase(
            Identifier source,
            int level,
            float value,
            List<io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition> conditions
    ) {
        return new VanillaBridgeTooltipSpec(
                source,
                enchantmentName(source, level),
                List.of(new AddBaseDamageOperation(
                        DamageChannel.PHYSICAL_ID,
                        DamageApplicationBucket.VANILLA_MELEE_ENCHANTMENT,
                        value
                )),
                conditions
        );
    }

    private static float sharpnessValue(int level) {
        if (level <= 0) {
            return 0.0f;
        }

        return 0.5f * level + 0.5f;
    }

    private static Component enchantmentName(
            Identifier source,
            int level
    ) {
        return Component.translatable(
                        "enchantment."
                                + source.getNamespace()
                                + "."
                                + source.getPath()
                )
                .append(" ")
                .append(Component.translatable("enchantment.level." + level));
    }

    private static Identifier minecraft(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    private static TagKey<EntityType<?>> entityTypeTag(String path) {
        return TagKey.create(
                Registries.ENTITY_TYPE,
                minecraft(path)
        );
    }
}