package io.github.naimjeg.damagenexus.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDisplay;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleRole;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleStacking;
import io.github.naimjeg.damagenexus.builtin.rule.condition.AlwaysCondition;
import io.github.naimjeg.damagenexus.builtin.rule.condition.IsCriticalCondition;
import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import io.github.naimjeg.damagenexus.registry.ModAttributes;
import io.github.naimjeg.damagenexus.registry.ModDataComponents;
import io.github.naimjeg.damagenexus.registry.PreMultiplierBuckets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = DamageNexus.MODID)
public final class DamageNexusCommands {

    private static final String PREFIX = "§a[DamageNexus]§r ";

    private DamageNexusCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (!ModConfig.isDebugMode() && !ModConfig.areTestCommandsEnabled()) {
            return;
        }

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("damagenexus");

        if (ModConfig.areTestCommandsEnabled()) {
            root.then(testTree())
                    .then(itemTree());
        }

        if (ModConfig.isDebugMode()) {
            root.then(mobTree())
                    .then(effectTree())
                    .then(attributeTree())
                    .then(Commands.literal("cleanup")
                            .executes(ctx -> cleanup(ctx.getSource())));
        }

        dispatcher.register(root);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> testTree() {
        return Commands.literal("test")
                .then(Commands.literal("all")
                        .executes(ctx -> runAll(ctx.getSource())))

                .then(Commands.literal("targets")
                        .then(Commands.literal("all")
                                .executes(ctx -> spawnAllTargets(ctx.getSource())))
                        .then(Commands.literal("defense")
                                .executes(ctx -> spawnDefenseTargets(ctx.getSource())))
                        .then(Commands.literal("enchant")
                                .executes(ctx -> spawnEnchantTargets(ctx.getSource())))
                        .then(Commands.literal("effects")
                                .executes(ctx -> spawnEffectTargets(ctx.getSource())))
                        .then(Commands.literal("post")
                                .executes(ctx -> spawnInvulTargets(ctx.getSource()))))

                .then(Commands.literal("bridge")
                        .then(Commands.literal("all")
                                .executes(ctx -> spawnBridgeTargets(ctx.getSource())))
                        .then(Commands.literal("projectile")
                                .executes(ctx -> spawnProjectileTargets(ctx.getSource())))
                        .then(Commands.literal("mace")
                                .executes(ctx -> spawnMaceTargets(ctx.getSource())))
                        .then(Commands.literal("spear")
                                .executes(ctx -> spawnSpearTargets(ctx.getSource())))
                        .then(Commands.literal("mob_difficulty")
                                .executes(ctx -> spawnMobDifficultyTargets(ctx.getSource()))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> itemTree() {
        return Commands.literal("item")
                .then(Commands.literal("all")
                        .executes(ctx -> giveAllItems(ctx.getSource())))

                .then(Commands.literal("base")
                        .executes(ctx -> giveBaseKit(ctx.getSource())))

                .then(Commands.literal("enchant")
                        .executes(ctx -> giveEnchantKit(ctx.getSource())))

                .then(Commands.literal("crit")
                        .executes(ctx -> giveCritKit(ctx.getSource())))

                .then(Commands.literal("channel")
                        .executes(ctx -> giveChannelKit(ctx.getSource())))

                .then(Commands.literal("sharpness")
                        .executes(ctx -> giveSharpnessSword(ctx.getSource())))

                .then(Commands.literal("smite")
                        .executes(ctx -> giveSmiteSword(ctx.getSource())))

                .then(Commands.literal("bane")
                        .executes(ctx -> giveBaneSword(ctx.getSource())))

                .then(Commands.literal("physical_25")
                        .executes(ctx -> givePhysicalSword(ctx.getSource())))

                .then(Commands.literal("fire_flat")
                        .executes(ctx -> giveFireSword(ctx.getSource())))

                .then(Commands.literal("mace")
                        .executes(ctx -> giveNamedItem(ctx.getSource(), Items.MACE, "§6[DN-Test] Mace")))

                .then(Commands.literal("bow_power")
                        .executes(ctx -> givePowerBow(ctx.getSource())))

                .then(Commands.literal("tooltip")
                        .then(Commands.literal("all")
                                .executes(ctx -> giveTooltipKit(ctx.getSource())))
                        .then(Commands.literal("signs")
                                .executes(ctx -> giveTooltipSignsItem(ctx.getSource())))
                        .then(Commands.literal("detail")
                                .executes(ctx -> giveTooltipDetailItem(ctx.getSource())))
                        .then(Commands.literal("convert")
                                .executes(ctx -> giveTooltipConvertGainItem(ctx.getSource())))
                        .then(Commands.literal("defense")
                                .executes(ctx -> giveTooltipDefenseItem(ctx.getSource())))
                        .then(Commands.literal("override")
                                .executes(ctx -> giveTooltipOverrideItem(ctx.getSource()))))

        /*
         * 如果当前版本确实有 Items.SPEAR，再打开：
         *
         * .then(Commands.literal("spear")
         *         .executes(ctx -> giveNamedItem(ctx.getSource(), Items.SPEAR, "§b[DN-Test] Spear")))
         */
                .then(Commands.literal("spear")
                .executes(ctx -> giveNamedItem(ctx.getSource(), Items.WOODEN_SPEAR, "§b[DN-Test] Spear")));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> mobTree() {
        return Commands.literal("mob")
                .then(Commands.literal("baseline")
                        .executes(ctx -> spawnBaselineMob(ctx.getSource())))

                .then(Commands.literal("zombie")
                        .executes(ctx -> spawnSingleZombie(ctx.getSource())))

                .then(Commands.literal("cow")
                        .executes(ctx -> spawnSingleCow(ctx.getSource())))

                .then(Commands.literal("spider")
                        .executes(ctx -> spawnSingleSpider(ctx.getSource())))

                .then(Commands.literal("iron")
                        .executes(ctx -> spawnArmoredTarget(ctx.getSource(), ArmorSet.IRON, false, "§7[DN-Test] Iron Armor")))

                .then(Commands.literal("diamond")
                        .executes(ctx -> spawnArmoredTarget(ctx.getSource(), ArmorSet.DIAMOND, false, "§b[DN-Test] Diamond Armor")))

                .then(Commands.literal("netherite_prot")
                        .executes(ctx -> spawnArmoredTarget(ctx.getSource(), ArmorSet.NETHERITE, true, "§4[DN-Test] Netherite Prot IV")))

                .then(Commands.literal("low_hp")
                        .executes(ctx -> spawnLowHpTarget(ctx.getSource())))

                .then(Commands.literal("invul")
                        .executes(ctx -> spawnInvulTarget(ctx.getSource())));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> effectTree() {
        return Commands.literal("effect")
                .then(Commands.literal("self")
                        .then(Commands.literal("strength")
                                .executes(ctx -> addSelfStrength(ctx.getSource(), 0)))
                        .then(Commands.literal("strength2")
                                .executes(ctx -> addSelfStrength(ctx.getSource(), 1)))
                        .then(Commands.literal("weakness")
                                .executes(ctx -> addSelfWeakness(ctx.getSource(), 0)))
                        .then(Commands.literal("clear")
                                .executes(ctx -> clearSelfEffects(ctx.getSource()))))

                .then(Commands.literal("target")
                        .then(Commands.literal("resistance")
                                .executes(ctx -> addTargetResistance(ctx.getSource(), 0)))
                        .then(Commands.literal("resistance2")
                                .executes(ctx -> addTargetResistance(ctx.getSource(), 1)))
                        .then(Commands.literal("burning")
                                .executes(ctx -> igniteNearestTarget(ctx.getSource())))
                        .then(Commands.literal("clear")
                                .executes(ctx -> clearTargetEffects(ctx.getSource()))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> attributeTree() {
        return Commands.literal("attribute")
                .then(Commands.literal("self")
                        .then(Commands.literal("crit_0")
                                .executes(ctx -> setSelfAttribute(ctx.getSource(), ModAttributes.CRIT_CHANCE, 0.0D, "crit chance = 0")))
                        .then(Commands.literal("crit_100")
                                .executes(ctx -> setSelfAttribute(ctx.getSource(), ModAttributes.CRIT_CHANCE, 1.0D, "crit chance = 100%")))
                        .then(Commands.literal("crit_damage_20")
                                .executes(ctx -> setSelfAttribute(ctx.getSource(), ModAttributes.CRIT_DAMAGE_ADDITIVE, 0.20D, "crit damage additive = 20%"))))

                .then(Commands.literal("target")
                        .then(Commands.literal("armor_0")
                                .executes(ctx -> setTargetAttribute(ctx.getSource(), Attributes.ARMOR, 0.0D, "armor = 0")))
                        .then(Commands.literal("armor_20")
                                .executes(ctx -> setTargetAttribute(ctx.getSource(), Attributes.ARMOR, 20.0D, "armor = 20")))
                        .then(Commands.literal("toughness_0")
                                .executes(ctx -> setTargetAttribute(ctx.getSource(), Attributes.ARMOR_TOUGHNESS, 0.0D, "toughness = 0")))
                        .then(Commands.literal("toughness_12")
                                .executes(ctx -> setTargetAttribute(ctx.getSource(), Attributes.ARMOR_TOUGHNESS, 12.0D, "toughness = 12")))
                        .then(Commands.literal("fire_res_50")
                                .executes(ctx -> setTargetAttribute(ctx.getSource(), ModAttributes.RESISTANCE_FIRE, 50.0D, "fire resistance = 50")))
                        .then(Commands.literal("physical_res_50")
                                .executes(ctx -> setTargetAttribute(ctx.getSource(), ModAttributes.RESISTANCE_PHYSICAL, 50.0D, "physical resistance = 50"))));
    }

    private static int runAll(CommandSourceStack source) {
        spawnAllTargets(source);
        giveAllItems(source);

        return success(source, "完整测试场景已生成。");
    }

    private static int spawnAllTargets(CommandSourceStack source) {
        spawnDefenseTargets(source);
        spawnEnchantTargets(source);
        spawnEffectTargets(source);
        spawnInvulTargets(source);

        source.sendSuccess(
                () -> Component.literal(PREFIX + "全部测试目标已生成。"),
                true
        );

        return 1;
    }

    private static int spawnDefenseTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        spawnZombie(
                level,
                pos.add(2, 0, 0),
                "§7[DN-Test] No Armor",
                null,
                false,
                false
        );

        spawnZombie(
                level,
                pos.add(4, 0, 0),
                "§7[DN-Test] Iron Armor",
                ArmorSet.IRON,
                false,
                false
        );

        spawnZombie(
                level,
                pos.add(6, 0, 0),
                "§b[DN-Test] Diamond Armor",
                ArmorSet.DIAMOND,
                false,
                false
        );

        spawnZombie(
                level,
                pos.add(8, 0, 0),
                "§4[DN-Test] Netherite Prot IV",
                ArmorSet.NETHERITE,
                true,
                false
        );

        spawnZombie(
                level,
                pos.add(10, 0, 0),
                "§9[DN-Test] Resistance I",
                ArmorSet.NONE,
                false,
                true
        );

        return success(source, "defense targets generated.");
    }

    private static int spawnBaselineMob(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Cow cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
        if (cow == null) {
            return 0;
        }

        setupMob(cow, pos.add(2, 0, 0), "§7[DN-Test] Baseline / No Armor");
        sanitizeTestLiving(cow);
        level.addFreshEntity(cow);

        return success(source, "baseline mob generated.");
    }

    private static void sanitizeTestLiving(LivingEntity entity) {
        entity.removeAllEffects();
        entity.setRemainingFireTicks(0);
        entity.clearFire();

        entity.setAbsorptionAmount(0.0f);
        entity.invulnerableTime = 0;
        entity.hurtTime = 0;
        entity.hurtDuration = 0;

        entity.setHealth(entity.getMaxHealth());
    }

    private static void sanitizePlayer(ServerPlayer player) {
        player.removeEffect(MobEffects.STRENGTH);
        player.removeEffect(MobEffects.WEAKNESS);
        player.setRemainingFireTicks(0);
        player.clearFire();
    }

    private static int spawnSingleZombie(CommandSourceStack source) {
        spawnZombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "§2[DN-Test] Zombie",
                ArmorSet.NONE,
                false,
                false
        );

        return success(source, "zombie generated.");
    }

    private static int spawnSingleCow(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Cow cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
        if (cow == null) {
            return 0;
        }

        setupMob(cow, pos.add(2, 0, 0), "§a[DN-Test] Cow");
        level.addFreshEntity(cow);

        return success(source, "cow generated.");
    }

    private static int spawnSingleSpider(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Spider spider = EntityType.SPIDER.create(level, EntitySpawnReason.COMMAND);
        if (spider == null) {
            return 0;
        }

        setupMob(spider, pos.add(2, 0, 0), "§6[DN-Test] Spider");
        level.addFreshEntity(spider);

        return success(source, "spider generated.");
    }

    private static int spawnArmoredTarget(
            CommandSourceStack source,
            ArmorSet armorSet,
            boolean protectionIv,
            String name
    ) {
        spawnZombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                name,
                armorSet,
                protectionIv,
                false
        );

        return success(source, "armored target generated.");
    }

    private static int spawnLowHpTarget(CommandSourceStack source) {
        Zombie lowHp = spawnZombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "§c[DN-Test] Overkill Cap / 5 HP",
                ArmorSet.NONE,
                false,
                false
        );

        if (lowHp != null) {
            lowHp.setHealth(5.0f);
        }

        return success(source, "low HP target generated.");
    }

    private static int spawnInvulTarget(CommandSourceStack source) {
        Zombie target = spawnZombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "§e[DN-Test] Invul Delta / Fast Hit",
                ArmorSet.NONE,
                false,
                false
        );

        if (target != null) {
            target.invulnerableTime = 10;
        }

        return success(source, "invulnerability target generated.");
    }

    private static int spawnEnchantTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Zombie zombie = spawnZombie(
                level,
                pos.add(2, 0, 3),
                "§2[DN-Test] Undead Target / Smite",
                ArmorSet.NONE,
                false,
                false
        );

        Cow cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
        if (cow != null) {
            setupMob(cow, pos.add(4, 0, 3), "§a[DN-Test] Cow / Smite Negative");
            level.addFreshEntity(cow);
        }

        Spider spider = EntityType.SPIDER.create(level, EntitySpawnReason.COMMAND);
        if (spider != null) {
            setupMob(spider, pos.add(6, 0, 3), "§6[DN-Test] Spider / Bane");
            level.addFreshEntity(spider);
        }

        return success(source, "enchantment bridge targets generated.");
    }

    private static int spawnEffectTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Zombie normal = spawnZombie(
                level,
                pos.add(2, 0, 6),
                "§7[DN-Test] Effect Baseline",
                ArmorSet.NONE,
                false,
                false
        );

        Zombie resistance = spawnZombie(
                level,
                pos.add(4, 0, 6),
                "§9[DN-Test] Resistance I Target",
                ArmorSet.NONE,
                false,
                false
        );

        if (resistance != null) {
            resistance.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE,
                    20 * 60 * 10,
                    0,
                    false,
                    true
            ));
        }

        Zombie resistance2 = spawnZombie(
                level,
                pos.add(6, 0, 6),
                "§9[DN-Test] Resistance II Target",
                ArmorSet.NONE,
                false,
                false
        );

        if (resistance2 != null) {
            resistance2.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE,
                    20 * 60 * 10,
                    1,
                    false,
                    true
            ));
        }

        return success(source, "mob effect targets generated.");
    }

    private static int spawnInvulTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Zombie fastHit = spawnZombie(
                level,
                pos.add(2, 0, 9),
                "§e[DN-Test] Invul Delta / Fast Hit",
                ArmorSet.NONE,
                false,
                false
        );

        if (fastHit != null) {
            fastHit.invulnerableTime = 10;
        }

        Zombie lowHp = spawnZombie(
                level,
                pos.add(4, 0, 9),
                "§c[DN-Test] Overkill Cap / 5 HP",
                ArmorSet.NONE,
                false,
                false
        );

        if (lowHp != null) {
            lowHp.setHealth(5.0f);
        }

        return success(source, "post classification targets generated.");
    }

    private static int giveAllItems(CommandSourceStack source) {
        giveBaseKit(source);
        giveEnchantKit(source);
        giveCritKit(source);
        giveChannelKit(source);

        return success(source, "all test items granted.");
    }
    private static int giveBaseKit(CommandSourceStack source) {
        give(source, named(new ItemStack(Items.IRON_SWORD), "§7[DN-Test] Plain Iron Sword"));
        give(source, named(new ItemStack(Items.DIAMOND_SWORD), "§7[DN-Test] Plain Diamond Sword"));

        return success(source, "base kit granted.");
    }

    private static int giveSharpnessSword(CommandSourceStack source) {
        give(source, enchantedSword(
                source.getLevel(),
                Items.IRON_SWORD,
                "§b[DN-Test] Sharpness V",
                Enchantments.SHARPNESS,
                5
        ));

        return success(source, "sharpness test sword granted.");
    }

    private static int giveSmiteSword(CommandSourceStack source) {
        give(source, enchantedSword(
                source.getLevel(),
                Items.IRON_SWORD,
                "§2[DN-Test] Smite V",
                Enchantments.SMITE,
                5
        ));

        return success(source, "smite test sword granted.");
    }

    private static int giveBaneSword(CommandSourceStack source) {
        give(source, enchantedSword(
                source.getLevel(),
                Items.IRON_SWORD,
                "§6[DN-Test] Bane V",
                Enchantments.BANE_OF_ARTHROPODS,
                5
        ));

        return success(source, "bane test sword granted.");
    }

    private static int givePhysicalSword(CommandSourceStack source) {
        ItemStack physical = named(
                new ItemStack(Items.IRON_SWORD),
                "§a[DN-Test] +25% Physical"
        );

        physical.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(testPhysicalScalingRule())
        );

        give(source, physical);

        return success(source, "physical scaling test sword granted.");
    }

    private static int giveFireSword(CommandSourceStack source) {
        ItemStack fire = named(
                new ItemStack(Items.DIAMOND_SWORD),
                "§c[DN-Test] +4 Fire Damage"
        );

        fire.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(testFlatFireRule())
        );

        give(source, fire);

        return success(source, "flat fire test sword granted.");
    }

    private static int givePowerBow(CommandSourceStack source) {
        give(source, enchantedItem(
                source.getLevel(),
                Items.BOW,
                "§b[DN-Test] Power V Bow",
                Enchantments.POWER,
                5
        ));

        give(source, named(new ItemStack(Items.ARROW, 64), "§7[DN-Test] Arrows"));

        return success(source, "power bow and arrows granted.");
    }

    private static int giveNamedItem(
            CommandSourceStack source,
            Item item,
            String name
    ) {
        give(source, named(new ItemStack(item), name));
        return success(source, "test item granted.");
    }

    private static int giveEnchantKit(CommandSourceStack source) {
        ServerLevel level = source.getLevel();

        give(source, enchantedSword(
                level,
                Items.IRON_SWORD,
                "§b[DN-Test] Sharpness V",
                Enchantments.SHARPNESS,
                5
        ));

        give(source, enchantedSword(
                level,
                Items.IRON_SWORD,
                "§2[DN-Test] Smite V",
                Enchantments.SMITE,
                5
        ));

        give(source, enchantedSword(
                level,
                Items.IRON_SWORD,
                "§6[DN-Test] Bane V",
                Enchantments.BANE_OF_ARTHROPODS,
                5
        ));

        return success(source, "enchantment kit granted.");
    }

    private static int giveCritKit(CommandSourceStack source) {
        ItemStack sword = named(
                new ItemStack(Items.IRON_SWORD),
                "§d[DN-Test] +20% Crit Damage"
        );

        sword.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(testCritDamageRule())
        );

        give(source, sword);

        return success(source, "crit kit granted.");
    }

    private static int giveTooltipKit(CommandSourceStack source) {
        giveTooltipSignsItem(source);
        giveTooltipDetailItem(source);
        giveTooltipConvertGainItem(source);
        giveTooltipDefenseItem(source);
        giveTooltipOverrideItem(source);

        return success(source, "tooltip test kit granted.");
    }

    private static int giveTooltipSignsItem(CommandSourceStack source) {
        ItemStack stack = named(
                new ItemStack(Items.IRON_SWORD),
                "§e[DN-Tooltip] Signed Values"
        );

        stack.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(
                        tooltipPositiveBaseRule(),
                        tooltipNegativeBaseRule(),
                        tooltipNegativeMultiplierRule()
                )
        );

        give(source, stack);
        return success(source, "tooltip signed-values item granted.");
    }

    private static int giveTooltipDetailItem(CommandSourceStack source) {
        ItemStack stack = named(
                new ItemStack(Items.DIAMOND_SWORD),
                "§d[DN-Tooltip] Detail / Stacking / Condition"
        );

        stack.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(
                        tooltipDetailRule(),
                        tooltipGlobalMultiplierRule(),
                        tooltipChannelPostRule()
                )
        );

        give(source, stack);
        return success(source, "tooltip detail item granted.");
    }

    private static int giveTooltipConvertGainItem(CommandSourceStack source) {
        ItemStack stack = named(
                new ItemStack(Items.GOLDEN_SWORD),
                "§b[DN-Tooltip] Convert + Gain Extra"
        );

        stack.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(
                        tooltipConvertRule(),
                        tooltipGainExtraRule()
                )
        );

        give(source, stack);
        return success(source, "tooltip convert/gain item granted.");
    }

    private static int giveTooltipDefenseItem(CommandSourceStack source) {
        ItemStack stack = named(
                new ItemStack(Items.SHIELD),
                "§9[DN-Tooltip] Defensive Ops"
        );

        stack.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(
                        tooltipTemporaryResistanceRule(),
                        tooltipChannelMitigationRule()
                )
        );

        give(source, stack);
        return success(source, "tooltip defensive item granted.");
    }

    private static int giveTooltipOverrideItem(CommandSourceStack source) {
        ItemStack stack = named(
                new ItemStack(Items.STICK),
                "§c[DN-Tooltip] Final Override"
        );

        stack.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(
                        tooltipOverrideFinalDamageRule()
                )
        );

        give(source, stack);
        return success(source, "tooltip override item granted.");
    }

    private static int giveChannelKit(CommandSourceStack source) {
        ItemStack physical = named(
                new ItemStack(Items.IRON_SWORD),
                "§a[DN-Test] +25% Physical"
        );

        physical.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(testPhysicalScalingRule())
        );

        give(source, physical);

        ItemStack fire = named(
                new ItemStack(Items.DIAMOND_SWORD),
                "§c[DN-Test] +4 Fire Damage"
        );

        fire.set(
                ModDataComponents.DAMAGE_RULES.get(),
                List.of(testFlatFireRule())
        );

        give(source, fire);

        return success(source, "channel kit granted.");
    }

    private static int addSelfStrength(CommandSourceStack source, int amplifier) {
        LivingEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH,
                20 * 60 * 10,
                amplifier,
                false,
                true
        ));

        return success(source, "strength applied to self.");
    }

    private static int addSelfWeakness(CommandSourceStack source, int amplifier) {
        LivingEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                20 * 60 * 10,
                amplifier,
                false,
                true
        ));

        return success(source, "weakness applied to self.");
    }

    private static int clearSelfEffects(CommandSourceStack source) {
        LivingEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        player.removeAllEffects();

        return success(source, "self effects cleared.");
    }

    private static int addTargetResistance(CommandSourceStack source, int amplifier) {
        LivingEntity target = nearestTestLiving(source);

        if (target == null) {
            return fail(source, "no nearby [DN-Test] living target found.");
        }

        target.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE,
                20 * 60 * 10,
                amplifier,
                false,
                true
        ));

        return success(source, "resistance applied to nearest test target.");
    }

    private static int igniteNearestTarget(CommandSourceStack source) {
        LivingEntity target = nearestTestLiving(source);

        if (target == null) {
            return fail(source, "no nearby [DN-Test] living target found.");
        }

        target.igniteForSeconds(30.0F);

        return success(source, "nearest test target ignited.");
    }

    private static int clearTargetEffects(CommandSourceStack source) {
        LivingEntity target = nearestTestLiving(source);

        if (target == null) {
            return fail(source, "no nearby [DN-Test] living target found.");
        }

        target.removeAllEffects();

        return success(source, "nearest test target effects cleared.");
    }

    private static int setSelfAttribute(
            CommandSourceStack source,
            Holder<Attribute> attribute,
            double value,
            String label
    ) {
        LivingEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        return setAttribute(source, player, attribute, value, label);
    }

    private static int setTargetAttribute(
            CommandSourceStack source,
            Holder<Attribute> attribute,
            double value,
            String label
    ) {
        LivingEntity target = nearestTestLiving(source);

        if (target == null) {
            return fail(source, "no nearby [DN-Test] living target found.");
        }

        return setAttribute(source, target, attribute, value, label);
    }

    private static int setAttribute(
            CommandSourceStack source,
            LivingEntity entity,
            Holder<Attribute> attribute,
            double value,
            String label
    ) {
        AttributeInstance instance = entity.getAttribute(attribute);

        if (instance == null) {
            return fail(source, "target does not have attribute: " + label);
        }

        instance.setBaseValue(value);

        return success(source, "attribute set: " + label);
    }

    private static int cleanup(CommandSourceStack source) {
        ServerLevel level = source.getLevel();

        level.getEntities().getAll().forEach(entity -> {
            if (entity == null) {
                return;
            }

            Component customName = entity.getCustomName();

            if (customName == null) {
                return;
            }

            String name = customName.getString();

            if (name.contains("[DN-Test]")) {
                entity.discard();
            }
        });

        Entity executor = source.getEntity();

        if (executor == source.getPlayer()) {
            sanitizePlayer(source.getPlayer());
        }

        return success(source, "test entities cleaned up.");
    }

    private static Zombie spawnZombie(
            ServerLevel level,
            Vec3 pos,
            String name,
            ArmorSet armorSet,
            boolean protectionIv,
            boolean resistance
    ) {
        Zombie zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);

        if (zombie == null) {
            return null;
        }

        setupMob(zombie, pos, name);

        if (armorSet != null && armorSet != ArmorSet.NONE) {
            equipArmor(level, zombie, armorSet, protectionIv);
        }

        if (resistance) {
            zombie.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE,
                    20 * 60 * 10,
                    0,
                    false,
                    true
            ));
        }

        level.addFreshEntity(zombie);

        return zombie;
    }

    private static void setupMob(
            Mob mob,
            Vec3 pos,
            String name
    ) {
        mob.setPos(pos.x, pos.y, pos.z);
        mob.setYRot(0.0F);
        mob.setCustomName(Component.literal(name));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.setNoAi(true);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0F);
        }
    }

    private static void equipArmor(
            ServerLevel level,
            LivingEntity entity,
            ArmorSet armorSet,
            boolean protectionIv
    ) {
        Holder<Enchantment> protection = null;

        if (protectionIv) {
            protection = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(Enchantments.PROTECTION)
                    .orElse(null);
        }

        entity.setItemSlot(
                EquipmentSlot.HEAD,
                createArmorStack(armorSet.helmet, protection)
        );

        entity.setItemSlot(
                EquipmentSlot.CHEST,
                createArmorStack(armorSet.chest, protection)
        );

        entity.setItemSlot(
                EquipmentSlot.LEGS,
                createArmorStack(armorSet.legs, protection)
        );

        entity.setItemSlot(
                EquipmentSlot.FEET,
                createArmorStack(armorSet.boots, protection)
        );
    }

    private static ItemStack createArmorStack(
            Item item,
            Holder<Enchantment> protection
    ) {
        ItemStack stack = new ItemStack(item);

        if (protection != null) {
            ItemEnchantments.Mutable mutableEnchantments =
                    new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            mutableEnchantments.set(protection, 4);

            stack.set(
                    DataComponents.ENCHANTMENTS,
                    mutableEnchantments.toImmutable()
            );
        }

        return stack;
    }

    private static int spawnBridgeTargets(CommandSourceStack source) {
        spawnProjectileTargets(source);
        spawnMaceTargets(source);
        spawnSpearTargets(source);
        spawnMobDifficultyTargets(source);

        return success(source, "bridge test targets generated.");
    }

    private static int spawnProjectileTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        spawnZombie(
                level,
                pos.add(2, 0, 12),
                "§b[DN-Test] Projectile Target",
                ArmorSet.NONE,
                false,
                false
        );

        spawnZombie(
                level,
                pos.add(4, 0, 12),
                "§b[DN-Test] Projectile Target / Armor",
                ArmorSet.IRON,
                false,
                false
        );

        return success(source, "projectile bridge targets generated.");
    }

    private static int spawnMaceTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        spawnZombie(
                level,
                pos.add(2, 0, 15),
                "§6[DN-Test] Mace Smash Target",
                ArmorSet.NONE,
                false,
                false
        );

        spawnZombie(
                level,
                pos.add(4, 0, 15),
                "§6[DN-Test] Mace Smash Target / Armor",
                ArmorSet.DIAMOND,
                false,
                false
        );

        return success(source, "mace bridge targets generated.");
    }

    private static int spawnSpearTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        spawnZombie(
                level,
                pos.add(2, 0, 18),
                "§3[DN-Test] Spear Target",
                ArmorSet.NONE,
                false,
                false
        );

        spawnZombie(
                level,
                pos.add(4, 0, 18),
                "§3[DN-Test] Spear Target / Armor",
                ArmorSet.IRON,
                false,
                false
        );

        return success(source, "spear bridge targets generated.");
    }

    private static int spawnMobDifficultyTargets(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Zombie attacker = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);

        if (attacker != null) {
            setupMob(attacker, pos.add(2, 0, 21), "§4[DN-Test] Mob Difficulty Attacker");
            attacker.setNoAi(false);
            level.addFreshEntity(attacker);
        }

        return success(source, "mob difficulty attacker generated.");
    }

    private static LivingEntity nearestTestLiving(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        AABB.ofSize(pos, 32.0D, 16.0D, 32.0D),
                        entity -> entity.getCustomName() != null
                                && entity.getCustomName().getString().contains("[DN-Test]")
                )
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(pos)))
                .orElse(null);
    }

    private static int fail(
            CommandSourceStack source,
            String message
    ) {
        source.sendFailure(
                Component.literal("§c[DamageNexus]§r " + message)
        );

        return 0;
    }

    private static ItemStack enchantedItem(
            ServerLevel level,
            Item item,
            String name,
            net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey,
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

    private static ItemStack enchantedSword(
            ServerLevel level,
            Item item,
            String name,
            net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey,
            int levelValue
    ) {
        return enchantedItem(
                level,
                item,
                name,
                enchantmentKey,
                levelValue
        );
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

    private static void give(
            CommandSourceStack source,
            ItemStack stack
    ) {
        if (source.getPlayer() == null) {
            return;
        }

        source.getPlayer().getInventory().add(stack);
    }

    private static int success(
            CommandSourceStack source,
            String message
    ) {
        source.sendSuccess(
                () -> Component.literal(PREFIX + message),
                true
        );

        return 1;
    }

    private static DamageRuleDefinition tooltipPositiveBaseRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_positive_fire_base"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.BASE_MODIFICATION,
                500,
                new DamageRuleDisplay(
                        Optional.of("Positive Fire Base"),
                        Optional.of("Adds Fire damage.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddBaseDamageOperation(
                        DamageChannel.FIRE_ID,
                        4.0f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip +4 Fire")
        );
    }

    private static DamageRuleDefinition tooltipNegativeBaseRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_negative_physical_base"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.BASE_MODIFICATION,
                501,
                new DamageRuleDisplay(
                        Optional.of("Negative Physical Base"),
                        Optional.of("Applies a negative Physical base adjustment.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddBaseDamageOperation(
                        DamageChannel.PHYSICAL_ID,
                        -1.0f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip -1 Physical")
        );
    }

    private static DamageRuleDefinition tooltipNegativeMultiplierRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_negative_fire_multiplier"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.TYPE_SCALING,
                500,
                new DamageRuleDisplay(
                        Optional.of("Negative Fire Multiplier"),
                        Optional.of("Applies a negative Fire damage multiplier.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddChannelPreMultiplierOperation(
                        DamageChannel.FIRE_ID,
                        Optional.empty(),
                        -0.20f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip -20% Fire")
        );
    }

    private static DamageRuleDefinition tooltipDetailRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_detail_crit_highest"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.CRITICAL_HIT,
                123,
                new DamageRuleDisplay(
                        Optional.of("Detail Metadata Rule"),
                        Optional.of("Adds critical Physical scaling with highest-value stacking.")
                ),
                List.of(new IsCriticalCondition()),
                List.of(new AddChannelPreMultiplierOperation(
                        DamageChannel.PHYSICAL_ID,
                        Optional.of(PreMultiplierBuckets.CRIT_DAMAGE_ID),
                        0.35f
                )),
                DamageRuleStacking.HIGHEST_VALUE,
                Optional.of(Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_crit_group")),
                Optional.of("Tooltip Detail Crit +35%")
        );
    }

    private static DamageRuleDefinition tooltipGlobalMultiplierRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_global_pre_15"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.GLOBAL_ADJUSTMENT,
                777,
                new DamageRuleDisplay(
                        Optional.of("Global Pre Multiplier"),
                        Optional.of("Adds a global pre-damage multiplier.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddGlobalPreMultiplierOperation(
                        Optional.empty(),
                        0.15f
                )),
                DamageRuleStacking.UNIQUE_SOURCE,
                Optional.of(Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_global_group")),
                Optional.of("Tooltip Global +15%")
        );
    }

    private static DamageRuleDefinition tooltipChannelPostRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_fire_post_negative"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.GLOBAL_ADJUSTMENT,
                778,
                new DamageRuleDisplay(
                        Optional.of("Negative Fire Post Multiplier"),
                        Optional.of("Applies a negative Fire post multiplier.")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddChannelPostMultiplierOperation(
                        DamageChannel.FIRE_ID,
                        -0.10f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip Fire Post -10%")
        );
    }

    private static DamageRuleDefinition tooltipConvertRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_convert_physical_to_fire"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.TYPE_SCALING,
                400,
                new DamageRuleDisplay(
                        Optional.of("Convert Physical to Fire"),
                        Optional.of("Converts 50% of current base Physical damage to Fire")
                ),
                List.of(new AlwaysCondition()),
                List.of(new ConvertDamageOperation(
                        DamageChannel.PHYSICAL_ID,
                        DamageChannel.FIRE_ID,
                        0.50f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip Convert 50% Physical to Fire")
        );
    }

    private static DamageRuleDefinition tooltipGainExtraRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_gain_lightning_from_physical"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.TYPE_SCALING,
                401,
                new DamageRuleDisplay(
                        Optional.of("Gain Lightning From Physical"),
                        Optional.of("Gains 25% of current base Physical damage as Lightning")
                ),
                List.of(new AlwaysCondition()),
                List.of(new GainExtraDamageOperation(
                        DamageChannel.PHYSICAL_ID,
                        DamageChannel.LIGHTNING_ID,
                        0.25f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip Gain 25% Physical as Lightning")
        );
    }

    private static DamageRuleDefinition tooltipTemporaryResistanceRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_temp_fire_resistance"),
                DamageRuleRole.DEFENSIVE,
                DamagePhase.MITIGATION_SETUP,
                500,
                new DamageRuleDisplay(
                        Optional.of("Temporary Fire Resistance"),
                        Optional.of("Adds temporary Fire resistance during mitigation setup")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddTemporaryResistanceOperation(
                        DamageChannel.FIRE_ID,
                        25.0f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip Temp Fire Resistance +25")
        );
    }

    private static DamageRuleDefinition tooltipChannelMitigationRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_physical_mitigation"),
                DamageRuleRole.DEFENSIVE,
                DamagePhase.MITIGATION_SETUP,
                501,
                new DamageRuleDisplay(
                        Optional.of("Physical Mitigation"),
                        Optional.of("Adds direct Physical mitigation")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddChannelMitigationOperation(
                        DamageChannel.PHYSICAL_ID,
                        0.20f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Tooltip Physical Mitigation +20%")
        );
    }

    private static DamageRuleDefinition tooltipOverrideFinalDamageRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_override_final_7"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.FINAL_OVERRIDE,
                999,
                new DamageRuleDisplay(
                        Optional.of("Override Final Damage"),
                        Optional.of("Sets final damage to 7")
                ),
                List.of(new AlwaysCondition()),
                List.of(new OverrideFinalDamageOperation(
                        7.0f
                )),
                DamageRuleStacking.REPLACE,
                Optional.of(Identifier.fromNamespaceAndPath(DamageNexus.MODID, "tooltip_override_group")),
                Optional.of("Tooltip Override Final 7")
        );
    }

    private static DamageRuleDefinition testCritDamageRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "test_crit_damage_20"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.CRITICAL_HIT,
                500,
                new DamageRuleDisplay(
                        Optional.of("Critical Damage"),
                        Optional.of("+20% critical damage")
                ),
                List.of(new IsCriticalCondition()),
                List.of(new AddChannelPreMultiplierOperation(
                        DamageChannel.PHYSICAL_ID,
                        Optional.of(PreMultiplierBuckets.CRIT_DAMAGE_ID),
                        0.20f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Critical Damage +20%")
        );
    }

    private static DamageRuleDefinition testPhysicalScalingRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "test_physical_scaling_25"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.TYPE_SCALING,
                500,
                new DamageRuleDisplay(
                        Optional.of("Physical Scaling"),
                        Optional.of("+25% physical damage")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddChannelPreMultiplierOperation(
                        DamageChannel.PHYSICAL_ID,
                        Optional.empty(),
                        0.25f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Physical Scaling +25%")
        );
    }

    private static DamageRuleDefinition testFlatFireRule() {
        return new DamageRuleDefinition(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "test_flat_fire_4"),
                DamageRuleRole.OFFENSIVE,
                DamagePhase.BASE_MODIFICATION,
                500,
                new DamageRuleDisplay(
                        Optional.of("Flat Fire"),
                        Optional.of("+4 fire damage")
                ),
                List.of(new AlwaysCondition()),
                List.of(new AddBaseDamageOperation(
                        DamageChannel.FIRE_ID,
                        4.0f
                )),
                DamageRuleStacking.STACK,
                Optional.empty(),
                Optional.of("Flat Fire +4")
        );
    }

    private enum ArmorSet {
        NONE(null, null, null, null),

        IRON(
                Items.IRON_HELMET,
                Items.IRON_CHESTPLATE,
                Items.IRON_LEGGINGS,
                Items.IRON_BOOTS
        ),

        DIAMOND(
                Items.DIAMOND_HELMET,
                Items.DIAMOND_CHESTPLATE,
                Items.DIAMOND_LEGGINGS,
                Items.DIAMOND_BOOTS
        ),

        NETHERITE(
                Items.NETHERITE_HELMET,
                Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS,
                Items.NETHERITE_BOOTS
        );

        final Item helmet;
        final Item chest;
        final Item legs;
        final Item boots;

        ArmorSet(
                Item helmet,
                Item chest,
                Item legs,
                Item boots
        ) {
            this.helmet = helmet;
            this.chest = chest;
            this.legs = legs;
            this.boots = boots;
        }
    }
}