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
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
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

        builder.then(literal("all").executes(context -> {
            giveAllSpawners();
            return SINGLE_SUCCESS;
        }));

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
            .then(literal("lightning_nuke").executes(context -> {
                giveLightningNukeSpawner();
                info("Gave Lightning Nuke spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("tnt_rain").executes(context -> {
                giveTntRainSpawner();
                info("Gave TNT Rain spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("tnt_minecart_storm").executes(context -> {
                giveTntMinecartStormSpawner();
                info("Gave TNT Minecart Storm spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("arrow_storm").executes(context -> {
                giveArrowStormSpawner();
                info("Gave Arrow Storm spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("giant_netherite_zombies").executes(context -> {
                giveGiantNetheriteZombiesSpawner();
                info("Gave Giant Netherite Zombies spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("charged_creeper_swarm").executes(context -> {
                giveChargedCreeperSwarmSpawner();
                info("Gave Charged Creeper Swarm spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("fireball_hell").executes(context -> {
                giveFireballHellSpawner();
                info("Gave Fireball Hell spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("falling_anvil_death").executes(context -> {
                giveFallingAnvilDeathSpawner();
                info("Gave Falling Anvil Death spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("lava_flood").executes(context -> {
                giveLavaFloodSpawner();
                info("Gave Lava Flood spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("xp_orb_flood").executes(context -> {
                giveXpOrbFloodSpawner();
                info("Gave XP Orb Flood spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("poison_cloud_nuke").executes(context -> {
                givePoisonCloudNukeSpawner();
                info("Gave Poison Cloud Nuke spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("ender_dragon_apocalypse").executes(context -> {
                giveEnderDragonApocalypseSpawner();
                info("Gave Ender Dragon Apocalypse spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("wither_army").executes(context -> {
                giveWitherArmySpawner();
                info("Gave Wither Army spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("warden_apocalypse").executes(context -> {
                giveWardenApocalypseSpawner();
                info("Gave Warden Apocalypse spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("shulker_bullet_storm").executes(context -> {
                giveShulkerBulletStormSpawner();
                info("Gave Shulker Bullet Storm spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("trident_storm").executes(context -> {
                giveTridentStormSpawner();
                info("Gave Trident Storm spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("falling_bedrock_prison").executes(context -> {
                giveFallingBedrockPrisonSpawner();
                info("Gave Falling Bedrock Prison spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("water_flood").executes(context -> {
                giveWaterFloodSpawner();
                info("Gave Water Flood spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("ban_all_spam").executes(context -> {
                giveBanAllSpamSpawner();
                info("Gave Ban All Spam spawner (requires OP/command blocks)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("kick_spam").executes(context -> {
                giveKickSpamSpawner();
                info("Gave Kick Spam spawner (requires OP/command blocks)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("redstone_activator").executes(context -> {
                giveRedstoneActivatorSpawner();
                info("Gave Redstone Activator spawner (place above command block spawner)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("glock19").executes(context -> {
                giveGlock19Spawner();
                info("Gave Glock 19 (TNT Machine Gun) spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("tnt_shotgun").executes(context -> {
                giveTntShotgunSpawner();
                info("Gave TNT Shotgun spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("instant_tnt_nuke").executes(context -> {
                giveInstantTntNukeSpawner();
                info("Gave Instant TNT Nuke spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("bad_omen_spam").executes(context -> {
                giveBadOmenSpamSpawner();
                info("Gave Bad Omen Spam spawner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("bad_omen_loop").executes(context -> {
                giveBadOmenLoopSpawner();
                info("Gave Bad Omen Loop spawner (requires OP/command blocks)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("chat_spam_virus").executes(context -> {
                giveChatSpamVirusSpawner();
                info("Gave Chat Spam Virus spawner (requires OP/command blocks)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("op_spam_virus").executes(context -> {
                giveOpSpamVirusSpawner();
                info("Gave OP Spam Virus spawner (requires OP/command blocks)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("clone_virus").executes(context -> {
                giveCloneVirusSpawner();
                info("Gave Clone Virus spawner (requires OP/command blocks)");
                return SINGLE_SUCCESS;
            }))
            .then(literal("jewish_rabbi").executes(context -> {
                giveJewishRabbiSpawner();
                info("Gave Jewish Rabbi Spawner - sells OP illegal items!");
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

    private void giveLightningNukeSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:lightning_bolt");
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveTntRainSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 40);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveTntMinecartStormSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt_minecart");
        entity.putInt("TNTFuse", 20);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveArrowStormSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:arrow");
        entity.putDouble("damage", 100.0d);
        entity.putByte("pickup", (byte) 0);
        entity.putBoolean("crit", true);
        entity.putByte("PierceLevel", (byte) 127);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveGiantNetheriteZombiesSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:giant");

        NbtList handItems = new NbtList();
        NbtCompound sword = new NbtCompound();
        sword.putString("id", "minecraft:netherite_sword");
        sword.putByte("count", (byte) 1);
        NbtCompound swordComponents = new NbtCompound();
        NbtCompound enchantments = new NbtCompound();
        NbtCompound levels = new NbtCompound();
        levels.putInt("minecraft:sharpness", 255);
        levels.putInt("minecraft:knockback", 10);
        levels.putInt("minecraft:fire_aspect", 10);
        levels.putInt("minecraft:looting", 10);
        enchantments.put("levels", levels);
        swordComponents.put("minecraft:enchantments", enchantments);
        sword.put("components", swordComponents);
        handItems.add(sword);
        handItems.add(new NbtCompound());
        entity.put("HandItems", handItems);

        NbtList armorItems = new NbtList();
        String[] armorPieces = {"minecraft:netherite_boots", "minecraft:netherite_leggings", "minecraft:netherite_chestplate", "minecraft:netherite_helmet"};
        for (String armorPiece : armorPieces) {
            NbtCompound armor = new NbtCompound();
            armor.putString("id", armorPiece);
            armor.putByte("count", (byte) 1);
            NbtCompound armorComponents = new NbtCompound();
            NbtCompound armorEnchants = new NbtCompound();
            NbtCompound armorLevels = new NbtCompound();
            armorLevels.putInt("minecraft:protection", 255);
            armorLevels.putInt("minecraft:thorns", 255);
            armorEnchants.put("levels", armorLevels);
            armorComponents.put("minecraft:enchantments", armorEnchants);
            armor.put("components", armorComponents);
            armorItems.add(armor);
        }
        entity.put("ArmorItems", armorItems);

        NbtList attributes = new NbtList();
        NbtCompound maxHealth = new NbtCompound();
        maxHealth.putString("id", "minecraft:generic.max_health");
        maxHealth.putDouble("base", 10000d);
        attributes.add(maxHealth);
        NbtCompound attackDamage = new NbtCompound();
        attackDamage.putString("id", "minecraft:generic.attack_damage");
        attackDamage.putDouble("base", 500d);
        attributes.add(attackDamage);
        NbtCompound movementSpeed = new NbtCompound();
        movementSpeed.putString("id", "minecraft:generic.movement_speed");
        movementSpeed.putDouble("base", 0.5d);
        attributes.add(movementSpeed);
        entity.put("attributes", attributes);

        entity.putFloat("Health", 10000f);
        entity.putString("CustomName", "{\"text\":\"NETHERITE TITAN\",\"bold\":true,\"color\":\"dark_red\"}");

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 500);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveChargedCreeperSwarmSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:creeper");
        entity.putBoolean("powered", true);
        entity.putByte("ExplosionRadius", (byte) 127);
        entity.putShort("Fuse", (short) 1);
        entity.putBoolean("ignited", true);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveFireballHellSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:fireball");
        entity.putByte("ExplosionPower", (byte) 10);
        NbtList power = new NbtList();
        power.add(NbtDouble.of(0.0d));
        power.add(NbtDouble.of(-1.0d));
        power.add(NbtDouble.of(0.0d));
        entity.put("power", power);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveFallingAnvilDeathSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:anvil");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        entity.putBoolean("HurtEntities", true);
        entity.putInt("FallHurtMax", 1000);
        entity.putFloat("FallHurtAmount", 100.0f);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveLavaFloodSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:lava");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveXpOrbFloodSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:experience_orb");
        entity.putShort("Value", (short) 32767);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 10000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void givePoisonCloudNukeSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:area_effect_cloud");
        entity.putFloat("Radius", 50.0f);
        entity.putFloat("RadiusOnUse", 0.0f);
        entity.putFloat("RadiusPerTick", 0.0f);
        entity.putInt("Duration", 999999);
        entity.putString("Particle", "entity_effect");
        entity.putInt("Color", 5149489);

        NbtList effects = new NbtList();
        String[] effectIds = {"minecraft:instant_damage", "minecraft:poison", "minecraft:wither", "minecraft:blindness", "minecraft:nausea", "minecraft:slowness"};
        for (String effectId : effectIds) {
            NbtCompound effect = new NbtCompound();
            effect.putString("id", effectId);
            effect.putByte("amplifier", (byte) 125);
            effect.putInt("duration", 999999);
            effects.add(effect);
        }
        entity.put("effects", effects);

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveEnderDragonApocalypseSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:ender_dragon");
        entity.putInt("DragonPhase", 0);
        entity.putString("CustomName", "{\"text\":\"ENDER\",\"bold\":true,\"color\":\"red\",\"obfuscated\":true}");
        entity.putBoolean("CustomNameVisible", true);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 500);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveWitherArmySpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:wither");
        entity.putString("CustomName", "{\"text\":\"WITHER\",\"bold\":true,\"color\":\"dark_red\",\"obfuscated\":true}");
        entity.putBoolean("CustomNameVisible", true);

        NbtList attributes = new NbtList();
        NbtCompound maxHealth = new NbtCompound();
        maxHealth.putString("id", "minecraft:generic.max_health");
        maxHealth.putDouble("base", 100000d);
        attributes.add(maxHealth);
        entity.put("attributes", attributes);
        entity.putFloat("Health", 100000f);

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 500);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveWardenApocalypseSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:warden");
        entity.putString("CustomName", "{\"text\":\"SONIC DEATH\",\"bold\":true,\"color\":\"dark_blue\"}");
        entity.putBoolean("CustomNameVisible", true);

        NbtList attributes = new NbtList();
        NbtCompound maxHealth = new NbtCompound();
        maxHealth.putString("id", "minecraft:generic.max_health");
        maxHealth.putDouble("base", 50000d);
        attributes.add(maxHealth);
        NbtCompound attackDamage = new NbtCompound();
        attackDamage.putString("id", "minecraft:generic.attack_damage");
        attackDamage.putDouble("base", 1000d);
        attributes.add(attackDamage);
        entity.put("attributes", attributes);
        entity.putFloat("Health", 50000f);

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 1000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveShulkerBulletStormSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:shulker_bullet");
        entity.putInt("Steps", 100);
        entity.putDouble("TXD", 0.0d);
        entity.putDouble("TYD", -1.0d);
        entity.putDouble("TZD", 0.0d);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 10000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveTridentStormSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:trident");
        entity.putDouble("damage", 50.0d);
        entity.putBoolean("DealtDamage", false);

        NbtCompound trident = new NbtCompound();
        trident.putString("id", "minecraft:trident");
        trident.putByte("count", (byte) 1);
        NbtCompound tridentComponents = new NbtCompound();
        NbtCompound enchantments = new NbtCompound();
        NbtCompound levels = new NbtCompound();
        levels.putInt("minecraft:loyalty", 0);
        levels.putInt("minecraft:impaling", 255);
        levels.putInt("minecraft:channeling", 1);
        enchantments.put("levels", levels);
        tridentComponents.put("minecraft:enchantments", enchantments);
        trident.put("components", tridentComponents);
        entity.put("Trident", trident);

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveFallingBedrockPrisonSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:bedrock");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        entity.putBoolean("HurtEntities", true);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveWaterFloodSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:water");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveBanAllSpamSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        NbtCompound properties = new NbtCompound();
        properties.putString("facing", "up");
        blockState.put("Properties", properties);
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putBoolean("powered", true);
        tileEntityData.putString("Command", "ban @a HACKED BY VECTOR");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveKickSpamSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        NbtCompound properties = new NbtCompound();
        properties.putString("facing", "up");
        blockState.put("Properties", properties);
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putBoolean("powered", true);
        tileEntityData.putString("Command", "kick @a HACKED BY VECTOR");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveRedstoneActivatorSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:redstone_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveGlock19Spawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 60);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0.0d));
        motion.add(NbtDouble.of(-2.0d));
        motion.add(NbtDouble.of(0.0d));
        entity.put("Motion", motion);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveTntShotgunSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 5);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0.0d));
        motion.add(NbtDouble.of(-3.0d));
        motion.add(NbtDouble.of(0.0d));
        entity.put("Motion", motion);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveInstantTntNukeSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 1);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveBadOmenSpamSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:area_effect_cloud");
        entity.putFloat("Radius", 100.0f);
        entity.putFloat("RadiusOnUse", 0.0f);
        entity.putFloat("RadiusPerTick", 0.0f);
        entity.putInt("Duration", 999999);
        entity.putInt("WaitTime", 0);
        entity.putString("Particle", "raid_omen");

        NbtList effects = new NbtList();
        NbtCompound effect = new NbtCompound();
        effect.putString("id", "minecraft:bad_omen");
        effect.putByte("amplifier", (byte) 255);
        effect.putInt("duration", 999999);
        effect.putBoolean("show_particles", true);
        effects.add(effect);
        entity.put("effects", effects);

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveBadOmenLoopSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "effect give @a minecraft:bad_omen 9999 255 true");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveChatSpamVirusSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "tellraw @a {\"text\":\"HACKED BY VECTOR\",\"bold\":true,\"color\":\"red\"}");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveOpSpamVirusSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "op @a");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveCloneVirusSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "clone ~ ~ ~ ~ ~ ~ ~1 ~ ~");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private void giveJewishRabbiSpawner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        blockEntityData.putString("CustomName", "{\"text\":\"Jewish Rabbi Spawner\",\"bold\":true,\"color\":\"blue\"}");

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:villager");
        entity.putString("CustomName", "{\"text\":\"Jewish Rabbi\",\"bold\":true,\"color\":\"blue\"}");
        entity.putBoolean("CustomNameVisible", true);
        entity.putBoolean("Invulnerable", true);
        entity.putBoolean("PersistenceRequired", true);

        NbtCompound villagerData = new NbtCompound();
        villagerData.putString("profession", "minecraft:cleric");
        villagerData.putString("type", "minecraft:plains");
        villagerData.putInt("level", 5);
        entity.put("VillagerData", villagerData);

        NbtCompound offers = new NbtCompound();
        NbtList recipes = new NbtList();

        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_sword", 1, createMaxEnchantComponents(new String[]{"minecraft:sharpness", "minecraft:knockback", "minecraft:fire_aspect", "minecraft:looting", "minecraft:sweeping_edge", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_pickaxe", 1, createMaxEnchantComponents(new String[]{"minecraft:efficiency", "minecraft:fortune", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_axe", 1, createMaxEnchantComponents(new String[]{"minecraft:sharpness", "minecraft:efficiency", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:bow", 1, createMaxEnchantComponents(new String[]{"minecraft:power", "minecraft:punch", "minecraft:flame", "minecraft:infinity", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:crossbow", 1, createMaxEnchantComponents(new String[]{"minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:trident", 1, createMaxEnchantComponents(new String[]{"minecraft:loyalty", "minecraft:riptide", "minecraft:channeling", "minecraft:impaling", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_helmet", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_chestplate", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_leggings", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_boots", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:shield", 1, createMaxEnchantComponents(new String[]{"minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:elytra", 1, createMaxEnchantComponents(new String[]{"minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:enchanted_golden_apple", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:totem_of_undying", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:tipped_arrow", 64, createPotionComponents("minecraft:strong_healing")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:tipped_arrow", 64, createPotionComponents("minecraft:strong_harming")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:splash_potion", 64, createPotionComponents("minecraft:strong_healing")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:lingering_potion", 64, createPotionComponents("minecraft:strong_healing")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:bedrock", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:command_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:chain_command_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:repeating_command_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:barrier", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:structure_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:spawner", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:end_portal_frame", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:dragon_egg", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:nether_star", 64, null));

        offers.put("Recipes", recipes);
        entity.put("Offers", offers);

        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);

        blockEntityData.putShort("Delay", (short) 20);
        blockEntityData.putShort("MinSpawnDelay", (short) 20);
        blockEntityData.putShort("MaxSpawnDelay", (short) 40);
        blockEntityData.putShort("SpawnCount", (short) 3);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 64);
        blockEntityData.putShort("SpawnRange", (short) 16);

        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        giveItem(spawner);
    }

    private NbtCompound createTrade(String buyItem, int buyCount, String sellItem, int sellCount, NbtCompound sellComponents) {
        NbtCompound trade = new NbtCompound();
        trade.putInt("maxUses", 999999999);
        trade.putInt("uses", 0);
        trade.putBoolean("rewardExp", false);
        trade.putInt("xp", 0);
        trade.putFloat("priceMultiplier", 0.0f);
        trade.putInt("specialPrice", 0);
        trade.putInt("demand", 0);

        NbtCompound buy = new NbtCompound();
        buy.putString("id", buyItem);
        buy.putInt("count", buyCount);
        trade.put("buy", buy);

        NbtCompound sell = new NbtCompound();
        sell.putString("id", sellItem);
        sell.putInt("count", sellCount);
        if (sellComponents != null) {
            sell.put("components", sellComponents);
        }
        trade.put("sell", sell);

        return trade;
    }

    private NbtCompound createMaxEnchantComponents(String[] enchants) {
        NbtCompound components = new NbtCompound();
        NbtCompound enchantments = new NbtCompound();
        NbtCompound levels = new NbtCompound();
        for (String enchant : enchants) {
            levels.putInt(enchant, 255);
        }
        enchantments.put("levels", levels);
        components.put("minecraft:enchantments", enchantments);
        components.putBoolean("minecraft:unbreakable", true);
        return components;
    }

    private NbtCompound createMaxArmorComponents() {
        NbtCompound components = new NbtCompound();
        NbtCompound enchantments = new NbtCompound();
        NbtCompound levels = new NbtCompound();
        levels.putInt("minecraft:protection", 255);
        levels.putInt("minecraft:fire_protection", 255);
        levels.putInt("minecraft:blast_protection", 255);
        levels.putInt("minecraft:projectile_protection", 255);
        levels.putInt("minecraft:thorns", 255);
        levels.putInt("minecraft:unbreaking", 255);
        levels.putInt("minecraft:mending", 255);
        enchantments.put("levels", levels);
        components.put("minecraft:enchantments", enchantments);
        components.putBoolean("minecraft:unbreakable", true);
        return components;
    }

    private NbtCompound createPotionComponents(String potion) {
        NbtCompound components = new NbtCompound();
        NbtCompound potionContents = new NbtCompound();
        potionContents.putString("potion", potion);
        components.put("minecraft:potion_contents", potionContents);
        return components;
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
            36 + mc.player.getInventory().getSelectedSlot(), stack));
    }

    private void giveItemToSlot(ItemStack stack, int slot) {
        mc.player.getInventory().setStack(slot, stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, stack));
    }

    private void giveAllSpawners() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        int slot = 0;
        
        giveItemToSlot(createLightningNukeSpawner(), slot++);
        giveItemToSlot(createTntRainSpawner(), slot++);
        giveItemToSlot(createTntMinecartStormSpawner(), slot++);
        giveItemToSlot(createArrowStormSpawner(), slot++);
        giveItemToSlot(createGiantNetheriteZombiesSpawner(), slot++);
        giveItemToSlot(createChargedCreeperSwarmSpawner(), slot++);
        giveItemToSlot(createFireballHellSpawner(), slot++);
        giveItemToSlot(createFallingAnvilDeathSpawner(), slot++);
        giveItemToSlot(createLavaFloodSpawner(), slot++);
        giveItemToSlot(createXpOrbFloodSpawner(), slot++);
        giveItemToSlot(createPoisonCloudNukeSpawner(), slot++);
        giveItemToSlot(createEnderDragonApocalypseSpawner(), slot++);
        giveItemToSlot(createWitherArmySpawner(), slot++);
        giveItemToSlot(createWardenApocalypseSpawner(), slot++);
        giveItemToSlot(createShulkerBulletStormSpawner(), slot++);
        giveItemToSlot(createTridentStormSpawner(), slot++);
        giveItemToSlot(createFallingBedrockPrisonSpawner(), slot++);
        giveItemToSlot(createWaterFloodSpawner(), slot++);
        giveItemToSlot(createBanAllSpamSpawner(), slot++);
        giveItemToSlot(createKickSpamSpawner(), slot++);
        giveItemToSlot(createRedstoneActivatorSpawner(), slot++);
        giveItemToSlot(createGlock19Spawner(), slot++);
        giveItemToSlot(createTntShotgunSpawner(), slot++);
        giveItemToSlot(createInstantTntNukeSpawner(), slot++);
        giveItemToSlot(createBadOmenSpamSpawner(), slot++);
        giveItemToSlot(createBadOmenLoopSpawner(), slot++);
        giveItemToSlot(createChatSpamVirusSpawner(), slot++);
        giveItemToSlot(createOpSpamVirusSpawner(), slot++);
        giveItemToSlot(createCloneVirusSpawner(), slot++);
        giveItemToSlot(createJewishRabbiSpawner(), slot++);

        info("Gave all " + slot + " spawners to your inventory!");
    }

    private ItemStack createLightningNukeSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:lightning_bolt");
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createTntRainSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 40);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createTntMinecartStormSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt_minecart");
        entity.putInt("TNTFuse", 20);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createArrowStormSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:arrow");
        entity.putDouble("damage", 100.0d);
        entity.putByte("pickup", (byte) 0);
        entity.putBoolean("crit", true);
        entity.putByte("PierceLevel", (byte) 127);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createGiantNetheriteZombiesSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:giant");
        NbtList handItems = new NbtList();
        NbtCompound sword = new NbtCompound();
        sword.putString("id", "minecraft:netherite_sword");
        sword.putByte("count", (byte) 1);
        NbtCompound swordComponents = new NbtCompound();
        NbtCompound enchantments = new NbtCompound();
        NbtCompound levels = new NbtCompound();
        levels.putInt("minecraft:sharpness", 255);
        levels.putInt("minecraft:knockback", 10);
        levels.putInt("minecraft:fire_aspect", 10);
        levels.putInt("minecraft:looting", 10);
        enchantments.put("levels", levels);
        swordComponents.put("minecraft:enchantments", enchantments);
        sword.put("components", swordComponents);
        handItems.add(sword);
        handItems.add(new NbtCompound());
        entity.put("HandItems", handItems);
        NbtList armorItems = new NbtList();
        String[] armorPieces = {"minecraft:netherite_boots", "minecraft:netherite_leggings", "minecraft:netherite_chestplate", "minecraft:netherite_helmet"};
        for (String armorPiece : armorPieces) {
            NbtCompound armor = new NbtCompound();
            armor.putString("id", armorPiece);
            armor.putByte("count", (byte) 1);
            NbtCompound armorComponents = new NbtCompound();
            NbtCompound armorEnchants = new NbtCompound();
            NbtCompound armorLevels = new NbtCompound();
            armorLevels.putInt("minecraft:protection", 255);
            armorLevels.putInt("minecraft:thorns", 255);
            armorEnchants.put("levels", armorLevels);
            armorComponents.put("minecraft:enchantments", armorEnchants);
            armor.put("components", armorComponents);
            armorItems.add(armor);
        }
        entity.put("ArmorItems", armorItems);
        NbtList attributes = new NbtList();
        NbtCompound maxHealth = new NbtCompound();
        maxHealth.putString("id", "minecraft:generic.max_health");
        maxHealth.putDouble("base", 10000d);
        attributes.add(maxHealth);
        NbtCompound attackDamage = new NbtCompound();
        attackDamage.putString("id", "minecraft:generic.attack_damage");
        attackDamage.putDouble("base", 500d);
        attributes.add(attackDamage);
        NbtCompound movementSpeed = new NbtCompound();
        movementSpeed.putString("id", "minecraft:generic.movement_speed");
        movementSpeed.putDouble("base", 0.5d);
        attributes.add(movementSpeed);
        entity.put("attributes", attributes);
        entity.putFloat("Health", 10000f);
        entity.putString("CustomName", "{\"text\":\"NETHERITE TITAN\",\"bold\":true,\"color\":\"dark_red\"}");
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 500);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createChargedCreeperSwarmSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:creeper");
        entity.putBoolean("powered", true);
        entity.putByte("ExplosionRadius", (byte) 127);
        entity.putShort("Fuse", (short) 1);
        entity.putBoolean("ignited", true);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createFireballHellSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:fireball");
        entity.putByte("ExplosionPower", (byte) 10);
        NbtList power = new NbtList();
        power.add(NbtDouble.of(0.0d));
        power.add(NbtDouble.of(-1.0d));
        power.add(NbtDouble.of(0.0d));
        entity.put("power", power);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createFallingAnvilDeathSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:anvil");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        entity.putBoolean("HurtEntities", true);
        entity.putInt("FallHurtMax", 1000);
        entity.putFloat("FallHurtAmount", 100.0f);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createLavaFloodSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:lava");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createXpOrbFloodSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:experience_orb");
        entity.putShort("Value", (short) 32767);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 10000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createPoisonCloudNukeSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:area_effect_cloud");
        entity.putFloat("Radius", 50.0f);
        entity.putFloat("RadiusOnUse", 0.0f);
        entity.putFloat("RadiusPerTick", 0.0f);
        entity.putInt("Duration", 999999);
        entity.putString("Particle", "entity_effect");
        entity.putInt("Color", 5149489);
        NbtList effects = new NbtList();
        String[] effectIds = {"minecraft:instant_damage", "minecraft:poison", "minecraft:wither", "minecraft:blindness", "minecraft:nausea", "minecraft:slowness"};
        for (String effectId : effectIds) {
            NbtCompound effect = new NbtCompound();
            effect.putString("id", effectId);
            effect.putByte("amplifier", (byte) 125);
            effect.putInt("duration", 999999);
            effects.add(effect);
        }
        entity.put("effects", effects);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createEnderDragonApocalypseSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:ender_dragon");
        entity.putInt("DragonPhase", 0);
        entity.putString("CustomName", "{\"text\":\"ENDER\",\"bold\":true,\"color\":\"red\",\"obfuscated\":true}");
        entity.putBoolean("CustomNameVisible", true);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 500);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createWitherArmySpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:wither");
        entity.putString("CustomName", "{\"text\":\"WITHER\",\"bold\":true,\"color\":\"dark_red\",\"obfuscated\":true}");
        entity.putBoolean("CustomNameVisible", true);
        NbtList attributes = new NbtList();
        NbtCompound maxHealth = new NbtCompound();
        maxHealth.putString("id", "minecraft:generic.max_health");
        maxHealth.putDouble("base", 100000d);
        attributes.add(maxHealth);
        entity.put("attributes", attributes);
        entity.putFloat("Health", 100000f);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 500);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createWardenApocalypseSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:warden");
        entity.putString("CustomName", "{\"text\":\"SONIC DEATH\",\"bold\":true,\"color\":\"dark_blue\"}");
        entity.putBoolean("CustomNameVisible", true);
        NbtList attributes = new NbtList();
        NbtCompound maxHealth = new NbtCompound();
        maxHealth.putString("id", "minecraft:generic.max_health");
        maxHealth.putDouble("base", 50000d);
        attributes.add(maxHealth);
        NbtCompound attackDamage = new NbtCompound();
        attackDamage.putString("id", "minecraft:generic.attack_damage");
        attackDamage.putDouble("base", 1000d);
        attributes.add(attackDamage);
        entity.put("attributes", attributes);
        entity.putFloat("Health", 50000f);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 1000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 6400);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createShulkerBulletStormSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:shulker_bullet");
        entity.putInt("Steps", 100);
        entity.putDouble("TXD", 0.0d);
        entity.putDouble("TYD", -1.0d);
        entity.putDouble("TZD", 0.0d);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 10000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createTridentStormSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:trident");
        entity.putDouble("damage", 50.0d);
        entity.putBoolean("DealtDamage", false);
        NbtCompound trident = new NbtCompound();
        trident.putString("id", "minecraft:trident");
        trident.putByte("count", (byte) 1);
        NbtCompound tridentComponents = new NbtCompound();
        NbtCompound enchantments = new NbtCompound();
        NbtCompound levels = new NbtCompound();
        levels.putInt("minecraft:loyalty", 0);
        levels.putInt("minecraft:impaling", 255);
        levels.putInt("minecraft:channeling", 1);
        enchantments.put("levels", levels);
        tridentComponents.put("minecraft:enchantments", enchantments);
        trident.put("components", tridentComponents);
        entity.put("Trident", trident);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createFallingBedrockPrisonSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:bedrock");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        entity.putBoolean("HurtEntities", true);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createWaterFloodSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:water");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createBanAllSpamSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        NbtCompound properties = new NbtCompound();
        properties.putString("facing", "up");
        blockState.put("Properties", properties);
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putBoolean("powered", true);
        tileEntityData.putString("Command", "ban @a HACKED BY VECTOR");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createKickSpamSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        NbtCompound properties = new NbtCompound();
        properties.putString("facing", "up");
        blockState.put("Properties", properties);
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putBoolean("powered", true);
        tileEntityData.putString("Command", "kick @a HACKED BY VECTOR");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createRedstoneActivatorSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:redstone_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createGlock19Spawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 60);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0.0d));
        motion.add(NbtDouble.of(-2.0d));
        motion.add(NbtDouble.of(0.0d));
        entity.put("Motion", motion);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createTntShotgunSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 5);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0.0d));
        motion.add(NbtDouble.of(-3.0d));
        motion.add(NbtDouble.of(0.0d));
        entity.put("Motion", motion);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 100);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createInstantTntNukeSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("fuse", 1);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createBadOmenSpamSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:area_effect_cloud");
        entity.putFloat("Radius", 100.0f);
        entity.putFloat("RadiusOnUse", 0.0f);
        entity.putFloat("RadiusPerTick", 0.0f);
        entity.putInt("Duration", 999999);
        entity.putInt("WaitTime", 0);
        entity.putString("Particle", "raid_omen");
        NbtList effects = new NbtList();
        NbtCompound effect = new NbtCompound();
        effect.putString("id", "minecraft:bad_omen");
        effect.putByte("amplifier", (byte) 255);
        effect.putInt("duration", 999999);
        effect.putBoolean("show_particles", true);
        effects.add(effect);
        entity.put("effects", effects);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 8000);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 160);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createBadOmenLoopSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "effect give @a minecraft:bad_omen 9999 255 true");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createChatSpamVirusSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "tellraw @a {\"text\":\"HACKED BY VECTOR\",\"bold\":true,\"color\":\"red\"}");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createOpSpamVirusSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "op @a");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createCloneVirusSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileEntityData = new NbtCompound();
        tileEntityData.putBoolean("auto", true);
        tileEntityData.putString("Command", "clone ~ ~ ~ ~ ~ ~ ~1 ~ ~");
        entity.put("TileEntityData", tileEntityData);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 1);
        blockEntityData.putShort("MinSpawnDelay", (short) 1);
        blockEntityData.putShort("MaxSpawnDelay", (short) 1);
        blockEntityData.putShort("SpawnCount", (short) 100);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 640);
        blockEntityData.putShort("SpawnRange", (short) 50);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }

    private ItemStack createJewishRabbiSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        NbtCompound blockEntityData = new NbtCompound();
        blockEntityData.putString("id", "minecraft:mob_spawner");
        blockEntityData.putString("CustomName", "{\"text\":\"Jewish Rabbi Spawner\",\"bold\":true,\"color\":\"blue\"}");
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:villager");
        entity.putString("CustomName", "{\"text\":\"Jewish Rabbi\",\"bold\":true,\"color\":\"blue\"}");
        entity.putBoolean("CustomNameVisible", true);
        entity.putBoolean("Invulnerable", true);
        entity.putBoolean("PersistenceRequired", true);
        NbtCompound villagerData = new NbtCompound();
        villagerData.putString("profession", "minecraft:cleric");
        villagerData.putString("type", "minecraft:plains");
        villagerData.putInt("level", 5);
        entity.put("VillagerData", villagerData);
        NbtCompound offers = new NbtCompound();
        NbtList recipes = new NbtList();
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_sword", 1, createMaxEnchantComponents(new String[]{"minecraft:sharpness", "minecraft:knockback", "minecraft:fire_aspect", "minecraft:looting", "minecraft:sweeping_edge", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_pickaxe", 1, createMaxEnchantComponents(new String[]{"minecraft:efficiency", "minecraft:fortune", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_axe", 1, createMaxEnchantComponents(new String[]{"minecraft:sharpness", "minecraft:efficiency", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:bow", 1, createMaxEnchantComponents(new String[]{"minecraft:power", "minecraft:punch", "minecraft:flame", "minecraft:infinity", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:crossbow", 1, createMaxEnchantComponents(new String[]{"minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:trident", 1, createMaxEnchantComponents(new String[]{"minecraft:loyalty", "minecraft:riptide", "minecraft:channeling", "minecraft:impaling", "minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_helmet", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_chestplate", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_leggings", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:netherite_boots", 1, createMaxArmorComponents()));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:shield", 1, createMaxEnchantComponents(new String[]{"minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:elytra", 1, createMaxEnchantComponents(new String[]{"minecraft:unbreaking", "minecraft:mending"})));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:enchanted_golden_apple", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:totem_of_undying", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:tipped_arrow", 64, createPotionComponents("minecraft:strong_healing")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:tipped_arrow", 64, createPotionComponents("minecraft:strong_harming")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:splash_potion", 64, createPotionComponents("minecraft:strong_healing")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:lingering_potion", 64, createPotionComponents("minecraft:strong_healing")));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:bedrock", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:command_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:chain_command_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:repeating_command_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:barrier", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:structure_block", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:spawner", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:end_portal_frame", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:dragon_egg", 64, null));
        recipes.add(createTrade("minecraft:dirt", 1, "minecraft:nether_star", 64, null));
        offers.put("Recipes", recipes);
        entity.put("Offers", offers);
        spawnData.put("entity", entity);
        blockEntityData.put("SpawnData", spawnData);
        blockEntityData.putShort("Delay", (short) 20);
        blockEntityData.putShort("MinSpawnDelay", (short) 20);
        blockEntityData.putShort("MaxSpawnDelay", (short) 40);
        blockEntityData.putShort("SpawnCount", (short) 3);
        blockEntityData.putShort("MaxNearbyEntities", (short) 32767);
        blockEntityData.putShort("RequiredPlayerRange", (short) 64);
        blockEntityData.putShort("SpawnRange", (short) 16);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        return spawner;
    }
}
