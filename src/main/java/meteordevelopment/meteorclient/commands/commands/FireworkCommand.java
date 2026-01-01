package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FireworkCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));
    private static final Random random = new Random();

    public FireworkCommand() {
        super("firework", "Create custom fireworks. Creative only.", "fw", "fireworkbuilder");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("random")
            .executes(context -> {
                giveRandomFirework(3, 1);
                return SINGLE_SUCCESS;
            })
            .then(argument("explosions", IntegerArgumentType.integer(1, 256))
                .executes(context -> {
                    int explosions = IntegerArgumentType.getInteger(context, "explosions");
                    giveRandomFirework(3, explosions);
                    return SINGLE_SUCCESS;
                })
                .then(argument("flight", IntegerArgumentType.integer(1, 127))
                    .executes(context -> {
                        int explosions = IntegerArgumentType.getInteger(context, "explosions");
                        int flight = IntegerArgumentType.getInteger(context, "flight");
                        giveRandomFirework(flight, explosions);
                        return SINGLE_SUCCESS;
                    })
                )
            )
        );

        builder.then(literal("nuke")
            .executes(context -> {
                giveNukeFirework(127);
                return SINGLE_SUCCESS;
            })
            .then(argument("explosions", IntegerArgumentType.integer(1, 256))
                .executes(context -> {
                    int explosions = IntegerArgumentType.getInteger(context, "explosions");
                    giveNukeFirework(explosions);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("boost")
            .executes(context -> {
                giveBoostFirework(3);
                return SINGLE_SUCCESS;
            })
            .then(argument("flight", IntegerArgumentType.integer(1, 127))
                .executes(context -> {
                    int flight = IntegerArgumentType.getInteger(context, "flight");
                    giveBoostFirework(flight);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("presets")
            .then(literal("rainbow").executes(context -> {
                giveRainbowFirework();
                info("Created rainbow firework");
                return SINGLE_SUCCESS;
            }))
            .then(literal("creeper").executes(context -> {
                giveCreeperFirework();
                info("Created creeper firework");
                return SINGLE_SUCCESS;
            }))
            .then(literal("star").executes(context -> {
                giveStarFirework();
                info("Created star firework");
                return SINGLE_SUCCESS;
            }))
            .then(literal("burst").executes(context -> {
                giveBurstFirework();
                info("Created burst firework");
                return SINGLE_SUCCESS;
            }))
            .then(literal("lagmachine").executes(context -> {
                giveLagFirework();
                info("Created lag machine firework (use carefully!)");
                return SINGLE_SUCCESS;
            }))
        );

        builder.then(literal("flight")
            .then(argument("duration", IntegerArgumentType.integer(1, 127))
                .executes(context -> {
                    int duration = IntegerArgumentType.getInteger(context, "duration");
                    giveBoostFirework(duration);
                    info("Created flight " + duration + " firework");
                    return SINGLE_SUCCESS;
                })
            )
        );
    }

    private void giveRandomFirework(int flight, int explosionCount) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        for (int i = 0; i < explosionCount; i++) {
            explosions.add(createRandomExplosion());
        }

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(flight, explosions));
        giveItem(firework);
        info("Created random firework (flight:" + flight + ", explosions:" + explosionCount + ")");
    }

    private void giveNukeFirework(int explosionCount) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        for (int i = 0; i < explosionCount; i++) {
            List<Integer> colors = new ArrayList<>();
            colors.add(0xFF0000);
            colors.add(0xFF5500);
            colors.add(0xFFAA00);

            List<Integer> fadeColors = new ArrayList<>();
            fadeColors.add(0x000000);

            explosions.add(new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.LARGE_BALL,
                IntList.of(colors.stream().mapToInt(Integer::intValue).toArray()),
                IntList.of(fadeColors.stream().mapToInt(Integer::intValue).toArray()),
                true,
                true
            ));
        }

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(127, explosions));
        giveItem(firework);
        info("Created nuke firework with " + explosionCount + " explosions");
    }

    private void giveBoostFirework(int flight) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(flight, List.of()));
        giveItem(firework);
        info("Created boost firework (flight:" + flight + ")");
    }

    private void giveRainbowFirework() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        int[] rainbowColors = {0xFF0000, 0xFF7F00, 0xFFFF00, 0x00FF00, 0x0000FF, 0x4B0082, 0x9400D3};

        for (int color : rainbowColors) {
            explosions.add(new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.LARGE_BALL,
                IntList.of(color),
                IntList.of(0xFFFFFF),
                true,
                true
            ));
        }

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(3, explosions));
        giveItem(firework);
    }

    private void giveCreeperFirework() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        explosions.add(new FireworkExplosionComponent(
            FireworkExplosionComponent.Type.CREEPER,
            IntList.of(0x00FF00),
            IntList.of(0x000000),
            true,
            false
        ));

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(2, explosions));
        giveItem(firework);
    }

    private void giveStarFirework() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        explosions.add(new FireworkExplosionComponent(
            FireworkExplosionComponent.Type.STAR,
            IntList.of(0xFFFF00, 0xFFFFFF),
            IntList.of(0xFF0000),
            true,
            true
        ));

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(3, explosions));
        giveItem(firework);
    }

    private void giveBurstFirework() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        explosions.add(new FireworkExplosionComponent(
            FireworkExplosionComponent.Type.BURST,
            IntList.of(0x00FFFF, 0xFF00FF, 0xFFFF00),
            IntList.of(0xFFFFFF),
            true,
            true
        ));

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(2, explosions));
        giveItem(firework);
    }

    private void giveLagFirework() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        for (int i = 0; i < 256; i++) {
            int[] colors = new int[64];
            int[] fadeColors = new int[64];
            for (int j = 0; j < 64; j++) {
                colors[j] = random.nextInt(0xFFFFFF);
                fadeColors[j] = random.nextInt(0xFFFFFF);
            }

            explosions.add(new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.values()[random.nextInt(FireworkExplosionComponent.Type.values().length)],
                IntList.of(colors),
                IntList.of(fadeColors),
                true,
                true
            ));
        }

        firework.set(DataComponentTypes.FIREWORKS, new FireworksComponent(127, explosions));
        giveItem(firework);
    }

    private FireworkExplosionComponent createRandomExplosion() {
        FireworkExplosionComponent.Type[] types = FireworkExplosionComponent.Type.values();
        FireworkExplosionComponent.Type type = types[random.nextInt(types.length)];

        int colorCount = random.nextInt(5) + 1;
        int[] colors = new int[colorCount];
        for (int i = 0; i < colorCount; i++) {
            DyeColor[] dyeColors = DyeColor.values();
            colors[i] = dyeColors[random.nextInt(dyeColors.length)].getFireworkColor();
        }

        int fadeCount = random.nextInt(3) + 1;
        int[] fadeColors = new int[fadeCount];
        for (int i = 0; i < fadeCount; i++) {
            DyeColor[] dyeColors = DyeColor.values();
            fadeColors[i] = dyeColors[random.nextInt(dyeColors.length)].getFireworkColor();
        }

        return new FireworkExplosionComponent(
            type,
            IntList.of(colors),
            IntList.of(fadeColors),
            random.nextBoolean(),
            random.nextBoolean()
        );
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
            36 + mc.player.getInventory().getSelectedSlot(), stack));
    }
}
