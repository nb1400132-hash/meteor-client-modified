package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

public class SpawnerCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));

    public SpawnerCommand() {
        super("spawner", "Create custom mob spawners. Creative only.", "spawnergive", "sg");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("entity", StringArgumentType.word())
            .executes(context -> {
                String entity = StringArgumentType.getString(context, "entity");
                giveSpawner(entity, 200, 4, 4, 16);
                return SINGLE_SUCCESS;
            })
            .then(argument("delay", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    String entity = StringArgumentType.getString(context, "entity");
                    int delay = IntegerArgumentType.getInteger(context, "delay");
                    giveSpawner(entity, delay, 4, 4, 16);
                    return SINGLE_SUCCESS;
                })
                .then(argument("count", IntegerArgumentType.integer(1, 32767))
                    .executes(context -> {
                        String entity = StringArgumentType.getString(context, "entity");
                        int delay = IntegerArgumentType.getInteger(context, "delay");
                        int count = IntegerArgumentType.getInteger(context, "count");
                        giveSpawner(entity, delay, count, count, 16);
                        return SINGLE_SUCCESS;
                    })
                    .then(argument("range", IntegerArgumentType.integer(1, 64))
                        .executes(context -> {
                            String entity = StringArgumentType.getString(context, "entity");
                            int delay = IntegerArgumentType.getInteger(context, "delay");
                            int count = IntegerArgumentType.getInteger(context, "count");
                            int range = IntegerArgumentType.getInteger(context, "range");
                            giveSpawner(entity, delay, count, count, range);
                            return SINGLE_SUCCESS;
                        })
                    )
                )
            )
        );

        builder.then(literal("presets")
            .then(literal("wither").executes(context -> {
                giveSpawner("wither", 20, 10, 10, 32);
                info("Gave Wither spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("tnt").executes(context -> {
                giveSpawner("tnt", 1, 100, 100, 16);
                info("Gave TNT spawner (use carefully!)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("creeper").executes(context -> {
                giveSpawner("creeper", 10, 20, 20, 32);
                info("Gave Creeper spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("lightning").executes(context -> {
                giveSpawner("lightning_bolt", 1, 50, 50, 32);
                info("Gave Lightning spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("ender_dragon").executes(context -> {
                giveSpawner("ender_dragon", 100, 1, 1, 16);
                info("Gave Ender Dragon spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("giant").executes(context -> {
                giveSpawner("giant", 50, 5, 5, 32);
                info("Gave Giant spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("killer_bunny").executes(context -> {
                giveKillerBunnySpawner();
                info("Gave Killer Bunny spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("charged_creeper").executes(context -> {
                giveChargedCreeperSpawner();
                info("Gave Charged Creeper spawner");
                return SINGLE_SUCCESS;
            }))
        );
    }

    private void giveSpawner(String entityId, int delay, int minCount, int maxCount, int range) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);

        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", entityId.contains(":") ? entityId : "minecraft:" + entityId);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) delay);
        blockEntityData.putShort("MinSpawnDelay", (short) Math.max(1, delay / 2));
        blockEntityData.putShort("MaxSpawnDelay", (short) delay);
        blockEntityData.putShort("SpawnCount", (short) maxCount);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) range);
        blockEntityData.putShort("SpawnRange", (short) Math.min(range, 16));

        NbtList spawnPotentials = new NbtList();
        NbtCompound potential = new NbtCompound();
        potential.putInt("weight", 1);
        NbtCompound potentialData = new NbtCompound();
        NbtCompound potentialEntity = new NbtCompound();
        potentialEntity.putString("id", entityId.contains(":") ? entityId : "minecraft:" + entityId);
        potentialData.put("entity", potentialEntity);
        potential.put("data", potentialData);
        spawnPotentials.add(potential);
        blockEntityData.put("SpawnPotentials", spawnPotentials);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));

        giveItem(spawner);
        info("Gave spawner: " + entityId + " (delay:" + delay + ", count:" + maxCount + ", range:" + range + ")");
    }

    private void giveKillerBunnySpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);

        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:rabbit");
        entity.putInt("RabbitType", 99);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 20);
        blockEntityData.putShort("MinSpawnDelay", (short) 10);
        blockEntityData.putShort("MaxSpawnDelay", (short) 20);
        blockEntityData.putShort("SpawnCount", (short) 10);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 32);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveChargedCreeperSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);

        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:creeper");
        entity.putBoolean("powered", true);
        entity.putShort("ExplosionRadius", (short) 10);
        entity.putShort("Fuse", (short) 10);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 20);
        blockEntityData.putShort("MinSpawnDelay", (short) 10);
        blockEntityData.putShort("MaxSpawnDelay", (short) 20);
        blockEntityData.putShort("SpawnCount", (short) 5);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 32);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
            36 + mc.player.getInventory().getSelectedSlot(), stack));
    }
}
