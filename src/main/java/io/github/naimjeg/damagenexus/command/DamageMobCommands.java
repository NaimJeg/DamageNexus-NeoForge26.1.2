package io.github.naimjeg.damagenexus.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.naimjeg.damagenexus.command.test.TestMobFactory;
import io.github.naimjeg.damagenexus.command.test.TestMobFactory.ArmorSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

public final class DamageMobCommands {

    private DamageMobCommands() {
    }

    public static void register(
            LiteralArgumentBuilder<CommandSourceStack> root
    ) {
        root.then(Commands.literal("mob")
                .then(Commands.literal("baseline")
                        .executes(ctx -> spawnBaselineMob(ctx.getSource())))
                .then(Commands.literal("zombie")
                        .executes(ctx -> spawnSingleZombie(ctx.getSource())))
                .then(Commands.literal("cow")
                        .executes(ctx -> spawnSingleCow(ctx.getSource())))
                .then(Commands.literal("spider")
                        .executes(ctx -> spawnSingleSpider(ctx.getSource())))
                .then(Commands.literal("iron")
                        .executes(ctx -> spawnArmoredTarget(
                                ctx.getSource(),
                                ArmorSet.IRON,
                                false,
                                "搂7[DN-Test] Iron Armor"
                        )))
                .then(Commands.literal("diamond")
                        .executes(ctx -> spawnArmoredTarget(
                                ctx.getSource(),
                                ArmorSet.DIAMOND,
                                false,
                                "搂b[DN-Test] Diamond Armor"
                        )))
                .then(Commands.literal("netherite_prot")
                        .executes(ctx -> spawnArmoredTarget(
                                ctx.getSource(),
                                ArmorSet.NETHERITE,
                                true,
                                "搂4[DN-Test] Netherite Prot IV"
                        )))
                .then(Commands.literal("low_hp")
                        .executes(ctx -> spawnLowHpTarget(ctx.getSource())))
                .then(Commands.literal("invul")
                        .executes(ctx -> spawnInvulTarget(ctx.getSource()))));
    }

    private static int spawnBaselineMob(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        TestMobFactory.cow(
                level,
                pos.add(2, 0, 0),
                "搂7[DN-Test] Baseline / No Armor"
        );

        return CommandFeedback.success(
                source,
                "baseline mob generated."
        );
    }

    private static int spawnSingleZombie(CommandSourceStack source) {
        TestMobFactory.zombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "搂2[DN-Test] Zombie",
                ArmorSet.NONE,
                false,
                false
        );

        return CommandFeedback.success(
                source,
                "zombie generated."
        );
    }

    private static int spawnSingleCow(CommandSourceStack source) {
        TestMobFactory.cow(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "搂a[DN-Test] Cow"
        );

        return CommandFeedback.success(
                source,
                "cow generated."
        );
    }

    private static int spawnSingleSpider(CommandSourceStack source) {
        TestMobFactory.spider(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "搂6[DN-Test] Spider"
        );

        return CommandFeedback.success(
                source,
                "spider generated."
        );
    }

    private static int spawnArmoredTarget(
            CommandSourceStack source,
            ArmorSet armorSet,
            boolean protectionIv,
            String name
    ) {
        TestMobFactory.zombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                name,
                armorSet,
                protectionIv,
                false
        );

        return CommandFeedback.success(
                source,
                "armored target generated."
        );
    }

    private static int spawnLowHpTarget(CommandSourceStack source) {
        Zombie target = TestMobFactory.zombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "搂c[DN-Test] Overkill Cap / 5 HP",
                ArmorSet.NONE,
                false,
                false
        );

        if (target != null) {
            target.setHealth(5.0f);
        }

        return CommandFeedback.success(
                source,
                "low HP target generated."
        );
    }

    private static int spawnInvulTarget(CommandSourceStack source) {
        Zombie target = TestMobFactory.zombie(
                source.getLevel(),
                source.getPosition().add(2, 0, 0),
                "搂e[DN-Test] Invul Delta / Fast Hit",
                ArmorSet.NONE,
                false,
                false
        );

        if (target != null) {
            target.invulnerableTime = 10;
        }

        return CommandFeedback.success(
                source,
                "invulnerability target generated."
        );
    }
}
