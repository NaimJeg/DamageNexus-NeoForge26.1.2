package io.github.naimjeg.damagenexus.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public class DamageNexusCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("damagenexus")
                        .then(
                                Commands.literal("zombie_test")
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            ServerLevel level = source.getLevel();
                                            Vec3 pos = source.getPosition();

                                            spawnZombie(
                                                    level,
                                                    pos.add(2, 0, 0),
                                                    "§7[DN-铁套测试僵尸]§r",
                                                    Items.IRON_HELMET,
                                                    Items.IRON_CHESTPLATE,
                                                    Items.IRON_LEGGINGS,
                                                    Items.IRON_BOOTS,
                                                    false
                                            );

                                            spawnZombie(
                                                    level,
                                                    pos.add(0, 0, 2),
                                                    "§b[DN-钻石测试僵尸]§r",
                                                    Items.DIAMOND_HELMET,
                                                    Items.DIAMOND_CHESTPLATE,
                                                    Items.DIAMOND_LEGGINGS,
                                                    Items.DIAMOND_BOOTS,
                                                    false
                                            );

                                            spawnZombie(
                                                    level,
                                                    pos.add(-2, 0, 0),
                                                    "§4[DN-下界合金保护IV测试僵尸]§r",
                                                    Items.NETHERITE_HELMET,
                                                    Items.NETHERITE_CHESTPLATE,
                                                    Items.NETHERITE_LEGGINGS,
                                                    Items.NETHERITE_BOOTS,
                                                    true
                                            );

                                            spawnZombie(
                                                    level,
                                                    pos.add(0, 0, -2),
                                                    "§9[DN-钻石保护IV测试僵尸]§r",
                                                    Items.DIAMOND_HELMET,
                                                    Items.DIAMOND_CHESTPLATE,
                                                    Items.DIAMOND_LEGGINGS,
                                                    Items.DIAMOND_BOOTS,
                                                    true
                                            );

                                            source.sendSuccess(
                                                    () -> Component.literal("§a[DamageNexus] zombie_test 测试僵尸已生成。"),
                                                    true
                                            );

                                            return 1;
                                        })
                        )
        );
    }

    private static void spawnZombie(
            ServerLevel level,
            Vec3 pos,
            String name,
            Item helmet,
            Item chest,
            Item legs,
            Item boots,
            boolean protectionIv
    ) {
        Zombie zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        if (zombie == null) {
            return;
        }

        zombie.setPos(pos.x, pos.y, pos.z);
        zombie.setYRot(0.0F);

        zombie.setCustomName(Component.literal(name));
        zombie.setCustomNameVisible(true);

        zombie.setPersistenceRequired();
        zombie.setNoAi(true);

        Holder<Enchantment> protection = null;

        if (protectionIv) {
            var enchantmentRegistry =
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

            protection = enchantmentRegistry
                    .get(Enchantments.PROTECTION)
                    .orElse(null);
        }

        zombie.setItemSlot(
                EquipmentSlot.HEAD,
                createArmorStack(helmet, protection)
        );

        zombie.setItemSlot(
                EquipmentSlot.CHEST,
                createArmorStack(chest, protection)
        );

        zombie.setItemSlot(
                EquipmentSlot.LEGS,
                createArmorStack(legs, protection)
        );

        zombie.setItemSlot(
                EquipmentSlot.FEET,
                createArmorStack(boots, protection)
        );

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            zombie.setDropChance(slot, 0.0F);
        }

        level.addFreshEntity(zombie);
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
}