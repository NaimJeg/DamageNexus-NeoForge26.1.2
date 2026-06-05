package io.github.naimjeg.damagenexus.command.test;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.display.DisplayText;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDisplay;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntrySlot;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryStacking;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TestItemFactory {

    private TestItemFactory() {
    }

    public static ItemStack physicalScalingSword() {
        return withRules(
                named(
                        new ItemStack(Items.IRON_SWORD),
                        "搂a[DN-Test] +25% Physical"
                ),
                List.of(TestRuleFactory.physicalScaling25())
        );
    }

    public static ItemStack flatFireSword() {
        return withRules(
                named(
                        new ItemStack(Items.DIAMOND_SWORD),
                        "搂c[DN-Test] +4 Fire Damage"
                ),
                List.of(TestRuleFactory.flatFire4())
        );
    }

    public static ItemStack convertGainOpsItem() {
        return withRules(
                named(
                        new ItemStack(Items.GOLDEN_SWORD),
                        "搂b[DN-Test] Ops / Convert + Gain"
                ),
                List.of(
                        TestRuleFactory.convertPhysicalToFire(),
                        TestRuleFactory.gainLightningFromPhysical()
                )
        );
    }

    public static ItemStack defensiveOpsItem() {
        return withRules(
                named(
                        new ItemStack(Items.SHIELD),
                        "搂9[DN-Test] Ops / Defensive Mitigation"
                ),
                List.of(
                        TestRuleFactory.temporaryFireResistance(),
                        TestRuleFactory.physicalMitigation20()
                )
        );
    }

    public static ItemStack finalOverrideOpsItem() {
        return withRules(
                named(
                        new ItemStack(Items.STICK),
                        "搂c[DN-Test] Ops / Final Override 7"
                ),
                List.of(TestRuleFactory.overrideFinalDamage7())
        );
    }

    public static ItemStack multiplierOpsItem() {
        return withRules(
                named(
                        new ItemStack(Items.DIAMOND_SWORD),
                        "搂d[DN-Test] Ops / Global + Post Multipliers"
                ),
                List.of(
                        TestRuleFactory.globalPreMultiplier15(),
                        TestRuleFactory.firePostMultiplierNegative10()
                )
        );
    }

    public static ItemStack arrows64() {
        return named(
                new ItemStack(Items.ARROW, 64),
                "搂7[DN-Test] Arrows"
        );
    }

    public static ItemStack powerBow(ServerLevel level) {
        return enchantedItem(
                level,
                Items.BOW,
                "搂b[DN-Test] Power V Bow",
                Enchantments.POWER,
                5
        );
    }

    public static ItemStack ruleBow() {
        return withRules(
                named(
                        new ItemStack(Items.BOW),
                        "搂c[DN-Test] Projectile Rule Bow / +3 Fire"
                ),
                List.of(TestRuleFactory.projectileFire3())
        );
    }

    public static ItemStack plainCrossbow() {
        return named(
                new ItemStack(Items.CROSSBOW),
                "搂7[DN-Test] Plain Crossbow"
        );
    }

    public static ItemStack piercingCrossbow(ServerLevel level) {
        return enchantedItem(
                level,
                Items.CROSSBOW,
                "搂b[DN-Test] Piercing IV Crossbow",
                Enchantments.PIERCING,
                4
        );
    }

    public static ItemStack ruleCrossbow() {
        return withRules(
                named(
                        new ItemStack(Items.CROSSBOW),
                        "搂c[DN-Test] Projectile Rule Crossbow / +3 Fire"
                ),
                List.of(TestRuleFactory.projectileFire3())
        );
    }

    public static ItemStack plainTrident() {
        return named(
                new ItemStack(Items.TRIDENT),
                "搂7[DN-Test] Plain Trident"
        );
    }

    public static ItemStack impalingTrident(ServerLevel level) {
        return enchantedItem(
                level,
                Items.TRIDENT,
                "搂b[DN-Test] Impaling V Trident",
                Enchantments.IMPALING,
                5
        );
    }

    public static ItemStack ruleTrident() {
        return withRules(
                named(
                        new ItemStack(Items.TRIDENT),
                        "搂3[DN-Test] Projectile Rule Trident / +3 Kinetic"
                ),
                List.of(TestRuleFactory.projectileKinetic3())
        );
    }

    public static ItemStack plainIronSword() {
        return named(
                new ItemStack(Items.IRON_SWORD),
                "搂7[DN-Test] Plain Iron Sword"
        );
    }

    public static ItemStack plainDiamondSword() {
        return named(
                new ItemStack(Items.DIAMOND_SWORD),
                "搂7[DN-Test] Plain Diamond Sword"
        );
    }

    public static ItemStack sharpnessSword(ServerLevel level) {
        return enchantedItem(
                level,
                Items.IRON_SWORD,
                "搂b[DN-Test] Sharpness V",
                Enchantments.SHARPNESS,
                5
        );
    }

    public static ItemStack smiteSword(ServerLevel level) {
        return enchantedItem(
                level,
                Items.IRON_SWORD,
                "搂2[DN-Test] Smite V",
                Enchantments.SMITE,
                5
        );
    }

    public static ItemStack baneSword(ServerLevel level) {
        return enchantedItem(
                level,
                Items.IRON_SWORD,
                "搂6[DN-Test] Bane V",
                Enchantments.BANE_OF_ARTHROPODS,
                5
        );
    }

    public static ItemStack critDamageSword() {
        return withRules(
                named(
                        new ItemStack(Items.IRON_SWORD),
                        "搂d[DN-Test] +20% Crit Damage"
                ),
                List.of(TestRuleFactory.critPhysicalPreMultiplier20())
        );
    }

    public static ItemStack blazingEdgeSword() {
        return withAffixes(
                named(
                        new ItemStack(Items.IRON_SWORD),
                        "搂c[DN-Test] Affix: Blazing Edge"
                ),
                List.of(TestRuleFactory.blazingEdgeAffix())
        );
    }

    public static ItemStack entryFireSword() {
        return withEntries(
                named(
                        new ItemStack(Items.IRON_SWORD),
                        "搂c[DN-Test] Entry: Fire Edge"
                ),
                List.of(new DamageEntryDefinition(
                        Identifier.fromNamespaceAndPath(
                                DamageNexus.MODID,
                                "test_entry_fire_edge"
                        ),
                        new DamageEntryDisplay(
                                DisplayText.literal("Fire Edge"),
                                List.of(DisplayText.literal("+4 Fire Damage")),
                                Optional.empty(),
                                true
                        ),
                        DamageEntrySlot.WEAPON,
                        List.of(TestRuleFactory.flatFire4()),
                        DamageEntryStacking.STACK,
                        Optional.empty()
                ))
        );
    }

    private static ItemStack withEntries(
            ItemStack stack,
            List<DamageEntryDefinition> entries
    ) {
        stack.set(
                ModDataComponents.DAMAGE_ENTRIES.get(),
                List.copyOf(entries)
        );

        return stack;
    }

    private static ItemStack withAffixes(
            ItemStack stack,
            List<DamageAffixDefinition> affixes
    ) {
        stack.set(
                ModDataComponents.DAMAGE_AFFIXES.get(),
                List.copyOf(affixes)
        );

        return stack;
    }

    public static ItemStack enchantedItem(
            ServerLevel level,
            Item item,
            String name,
            ResourceKey<Enchantment> enchantmentKey,
            int levelValue
    ) {
        ItemStack stack = named(new ItemStack(item), name);

        Holder<Enchantment> enchantment =
                level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .get(enchantmentKey)
                        .orElse(null);

        if (enchantment == null) {
            return stack;
        }

        ItemEnchantments.Mutable mutableEnchantments =
                new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        mutableEnchantments.set(enchantment, levelValue);

        stack.set(
                DataComponents.ENCHANTMENTS,
                mutableEnchantments.toImmutable()
        );

        return stack;
    }

    private static ItemStack withRules(
            ItemStack stack,
            List<DamageRuleDefinition> rules
    ) {
        if (rules == null || rules.isEmpty()) {
            return stack;
        }

        List<DamageEntryDefinition> entries = rules.stream()
                .map(TestItemFactory::entryFromRule)
                .toList();

        stack.set(
                ModDataComponents.DAMAGE_ENTRIES.get(),
                entries
        );

        return stack;
    }

    private static DamageEntryDefinition entryFromRule(
            DamageRuleDefinition rule
    ) {
        Identifier entryId = Identifier.fromNamespaceAndPath(
                DamageNexus.MODID,
                "test_entry_" + sanitizePath(rule.id().getPath())
        );

        return new DamageEntryDefinition(
                entryId,
                new DamageEntryDisplay(
                        DisplayText.literal(rule.id().getPath()),
                        List.of(DisplayText.literal("Legacy test rule entry")),
                        Optional.empty(),
                        true
                ),
                DamageEntrySlot.WEAPON,
                List.of(rule),
                DamageEntryStacking.STACK,
                Optional.empty()
        );
    }

    private static String sanitizePath(String path) {
        if (path == null || path.isBlank()) {
            return "unknown";
        }

        return path
                .replace(':', '_')
                .replace('/', '_')
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT);
    }

    private static ItemStack named(
            ItemStack stack,
            String name
    ) {
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        return stack;
    }
}
