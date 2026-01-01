package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SpawnersCommand extends Command {

    public SpawnersCommand() {
        super("spawners", "Gives chaos spawners. Use .spawners list for options.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            info("Spawners: lightning, tnt, tntminecart, arrow, giant, creeper, fireball, anvil, lava, xp, poison, dragon, wither, warden, shulker, trident, bedrock, water, ban, kick, glock, tntshotgun, instanttnt, badomen, redstone, banblock, op");
            info("Use: .spawners <name> or .spawners all");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("all").executes(context -> {
            if (!checkCreative()) return SINGLE_SUCCESS;
            giveAllSpawners();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("lightning").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveLightning(0); info("Gave lightning spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("tnt").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveTnt(0); info("Gave tnt spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("tntminecart").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveTntMinecart(0); info("Gave tntminecart spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("arrow").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveArrow(0); info("Gave arrow spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("giant").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveGiant(0); info("Gave giant spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("creeper").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveCreeper(0); info("Gave creeper spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("fireball").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveFireball(0); info("Gave fireball spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("anvil").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveAnvil(0); info("Gave anvil spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("lava").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveLava(0); info("Gave lava spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("xp").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveXp(0); info("Gave xp spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("poison").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; givePoison(0); info("Gave poison spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("dragon").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveDragon(0); info("Gave dragon spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("wither").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveWither(0); info("Gave wither spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("warden").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveWarden(0); info("Gave warden spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("shulker").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveShulker(0); info("Gave shulker spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("trident").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveTrident(0); info("Gave trident spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("bedrock").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveBedrock(0); info("Gave bedrock spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("water").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveWater(0); info("Gave water spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("ban").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveBan(0); info("Gave ban spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("kick").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveKick(0); info("Gave kick spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("glock").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveGlock(0); info("Gave glock spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("tntshotgun").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveTntShotgun(0); info("Gave tntshotgun spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("instanttnt").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveInstantTnt(0); info("Gave instanttnt spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("badomen").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveBadOmen(0); info("Gave badomen spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("redstone").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveRedstone(0); info("Gave redstone spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("banblock").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveBanBlock(0); info("Gave banblock spawner"); return SINGLE_SUCCESS; }));
        builder.then(literal("op").executes(ctx -> { if (!checkCreative()) return SINGLE_SUCCESS; giveOp(0); info("Gave op spawner"); return SINGLE_SUCCESS; }));

        builder.executes(context -> {
            info("Usage: .spawners <name> | .spawners all | .spawners list");
            return SINGLE_SUCCESS;
        });
    }

    private boolean checkCreative() {
        if (mc.player == null || !mc.player.getAbilities().creativeMode) {
            error("You must be in Creative mode.");
            return false;
        }
        return true;
    }

    private void giveAllSpawners() {
        int slot = 0;
        giveLightning(slot++);
        giveTnt(slot++);
        giveTntMinecart(slot++);
        giveArrow(slot++);
        giveGiant(slot++);
        giveCreeper(slot++);
        giveFireball(slot++);
        giveAnvil(slot++);
        giveLava(slot++);
        giveXp(slot++);
        givePoison(slot++);
        giveDragon(slot++);
        giveWither(slot++);
        giveWarden(slot++);
        giveShulker(slot++);
        giveTrident(slot++);
        giveBedrock(slot++);
        giveWater(slot++);
        giveBan(slot++);
        giveKick(slot++);
        giveGlock(slot++);
        giveTntShotgun(slot++);
        giveInstantTnt(slot++);
        giveBadOmen(slot++);
        giveRedstone(slot++);
        giveBanBlock(slot++);
        giveOp(slot++);
        info("Gave " + slot + " chaos spawners!");
    }

    private void giveSpawnerToSlot(ItemStack spawner, int slot) {
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
    }

    private NbtCompound createBaseBlockEntity(String customName, String color, int spawnCount, int range, int maxEntities) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", "minecraft:mob_spawner");
        nbt.putString("CustomName", "{\"text\":\"" + customName + "\",\"bold\":true,\"color\":\"" + color + "\"}");
        nbt.putShort("Delay", (short) 1);
        nbt.putShort("MinSpawnDelay", (short) 1);
        nbt.putShort("MaxSpawnDelay", (short) 1);
        nbt.putShort("SpawnCount", (short) spawnCount);
        nbt.putShort("MaxNearbyEntities", (short) maxEntities);
        nbt.putShort("RequiredPlayerRange", (short) 640);
        nbt.putShort("SpawnRange", (short) range);
        nbt.put("SpawnPotentials", new NbtList());
        return nbt;
    }

    private ItemStack createSpawner(NbtCompound blockEntityData, String itemName, String color) {
        ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
        spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
        spawner.set(DataComponentTypes.ITEM_NAME, Text.literal(itemName).styled(s -> s.withBold(true).withColor(getColor(color))));
        return spawner;
    }

    private int getColor(String color) {
        return switch (color) {
            case "yellow" -> 0xFFFF55;
            case "red" -> 0xFF5555;
            case "dark_red" -> 0xAA0000;
            case "gray" -> 0xAAAAAA;
            case "dark_purple" -> 0xAA00AA;
            case "aqua" -> 0x55FFFF;
            case "gold" -> 0xFFAA00;
            case "dark_gray" -> 0x555555;
            case "green" -> 0x55FF55;
            case "dark_green" -> 0x00AA00;
            case "dark_aqua" -> 0x00AAAA;
            case "light_purple" -> 0xFF55FF;
            case "black" -> 0x000000;
            case "blue" -> 0x5555FF;
            default -> 0xFFFFFF;
        };
    }

    private void giveLightning(int slot) {
        NbtCompound nbt = createBaseBlockEntity("LIGHTNING NUKE", "yellow", 8000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:lightning_bolt");
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "LIGHTNING NUKE", "yellow"), slot);
    }

    private void giveTnt(int slot) {
        NbtCompound nbt = createBaseBlockEntity("TNT RAIN", "red", 8000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("Fuse", 40);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "TNT RAIN", "red"), slot);
    }

    private void giveTntMinecart(int slot) {
        NbtCompound nbt = createBaseBlockEntity("TNT MINECART STORM", "dark_red", 8000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt_minecart");
        entity.putInt("TNTFuse", 20);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "TNT MINECART STORM", "dark_red"), slot);
    }

    private void giveArrow(int slot) {
        NbtCompound nbt = createBaseBlockEntity("ARROW STORM", "gray", 8000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:arrow");
        entity.putDouble("damage", 100.0);
        entity.putByte("pickup", (byte) 0);
        entity.putBoolean("crit", true);
        entity.putByte("PierceLevel", (byte) 127);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "ARROW STORM", "gray"), slot);
    }

    private void giveGiant(int slot) {
        NbtCompound nbt = createBaseBlockEntity("GIANT NETHERITE ZOMBIES", "dark_purple", 500, 100, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:giant");
        entity.putFloat("Health", 10000f);
        entity.putString("CustomName", "{\"text\":\"NETHERITE TITAN\",\"bold\":true,\"color\":\"dark_red\"}");

        NbtList attributes = new NbtList();
        NbtCompound health = new NbtCompound();
        health.putString("id", "minecraft:generic.max_health");
        health.putDouble("base", 10000);
        attributes.add(health);
        NbtCompound damage = new NbtCompound();
        damage.putString("id", "minecraft:generic.attack_damage");
        damage.putDouble("base", 500);
        attributes.add(damage);
        entity.put("attributes", attributes);

        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "GIANT NETHERITE ZOMBIES", "dark_purple"), slot);
    }

    private void giveCreeper(int slot) {
        NbtCompound nbt = createBaseBlockEntity("CHARGED CREEPER SWARM", "aqua", 8000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:creeper");
        entity.putBoolean("powered", true);
        entity.putByte("ExplosionRadius", (byte) 127);
        entity.putShort("Fuse", (short) 1);
        entity.putBoolean("ignited", true);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "CHARGED CREEPER SWARM", "aqua"), slot);
    }

    private void giveFireball(int slot) {
        NbtCompound nbt = createBaseBlockEntity("FIREBALL HELL", "gold", 8000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:fireball");
        entity.putByte("ExplosionPower", (byte) 10);
        NbtList power = new NbtList();
        power.add(NbtDouble.of(0.0));
        power.add(NbtDouble.of(-1.0));
        power.add(NbtDouble.of(0.0));
        entity.put("power", power);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "FIREBALL HELL", "gold"), slot);
    }

    private void giveAnvil(int slot) {
        NbtCompound nbt = createBaseBlockEntity("FALLING ANVIL DEATH", "dark_gray", 8000, 160, 6400);
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
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "FALLING ANVIL DEATH", "dark_gray"), slot);
    }

    private void giveLava(int slot) {
        NbtCompound nbt = createBaseBlockEntity("LAVA FLOOD", "gold", 8000, 50, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:lava");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "LAVA FLOOD", "gold"), slot);
    }

    private void giveXp(int slot) {
        NbtCompound nbt = createBaseBlockEntity("XP ORB FLOOD", "green", 10000, 160, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:experience_orb");
        entity.putShort("Value", (short) 32767);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "XP ORB FLOOD", "green"), slot);
    }

    private void givePoison(int slot) {
        NbtCompound nbt = createBaseBlockEntity("POISON CLOUD NUKE", "dark_green", 8000, 160, 64000);
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
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "POISON CLOUD NUKE", "dark_green"), slot);
    }

    private void giveDragon(int slot) {
        NbtCompound nbt = createBaseBlockEntity("ENDER DRAGON", "dark_purple", 500, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:ender_dragon");
        entity.putInt("DragonPhase", 0);
        entity.putString("CustomName", "{\"text\":\"DOOM\",\"bold\":true,\"color\":\"red\",\"obfuscated\":true}");
        entity.putBoolean("CustomNameVisible", true);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "ENDER DRAGON", "dark_purple"), slot);
    }

    private void giveWither(int slot) {
        NbtCompound nbt = createBaseBlockEntity("WITHER ARMY", "dark_gray", 500, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:wither");
        entity.putFloat("Health", 100000f);
        entity.putString("CustomName", "{\"text\":\"DEATH\",\"bold\":true,\"color\":\"dark_red\",\"obfuscated\":true}");
        entity.putBoolean("CustomNameVisible", true);
        NbtList attributes = new NbtList();
        NbtCompound health = new NbtCompound();
        health.putString("id", "minecraft:generic.max_health");
        health.putDouble("base", 100000);
        attributes.add(health);
        entity.put("attributes", attributes);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "WITHER ARMY", "dark_gray"), slot);
    }

    private void giveWarden(int slot) {
        NbtCompound nbt = createBaseBlockEntity("WARDEN APOCALYPSE", "dark_aqua", 1000, 160, 6400);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:warden");
        entity.putFloat("Health", 50000f);
        entity.putString("CustomName", "{\"text\":\"SONIC DEATH\",\"bold\":true,\"color\":\"dark_blue\"}");
        entity.putBoolean("CustomNameVisible", true);
        NbtList attributes = new NbtList();
        NbtCompound health = new NbtCompound();
        health.putString("id", "minecraft:generic.max_health");
        health.putDouble("base", 50000);
        attributes.add(health);
        NbtCompound damage = new NbtCompound();
        damage.putString("id", "minecraft:generic.attack_damage");
        damage.putDouble("base", 1000);
        attributes.add(damage);
        entity.put("attributes", attributes);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "WARDEN APOCALYPSE", "dark_aqua"), slot);
    }

    private void giveShulker(int slot) {
        NbtCompound nbt = createBaseBlockEntity("SHULKER BULLET STORM", "light_purple", 10000, 160, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:shulker_bullet");
        entity.putInt("Steps", 100);
        entity.putDouble("TXD", 0.0);
        entity.putDouble("TYD", -1.0);
        entity.putDouble("TZD", 0.0);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "SHULKER BULLET STORM", "light_purple"), slot);
    }

    private void giveTrident(int slot) {
        NbtCompound nbt = createBaseBlockEntity("TRIDENT STORM", "aqua", 8000, 160, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:trident");
        entity.putDouble("damage", 50.0);
        entity.putBoolean("DealtDamage", false);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "TRIDENT STORM", "aqua"), slot);
    }

    private void giveBedrock(int slot) {
        NbtCompound nbt = createBaseBlockEntity("FALLING BEDROCK PRISON", "black", 8000, 100, 64000);
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
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "FALLING BEDROCK PRISON", "black"), slot);
    }

    private void giveWater(int slot) {
        NbtCompound nbt = createBaseBlockEntity("WATER FLOOD", "blue", 8000, 100, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:water");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "WATER FLOOD", "blue"), slot);
    }

    private void giveBan(int slot) {
        NbtCompound nbt = createBaseBlockEntity("BAN ALL SPAM", "dark_red", 100, 50, 64000);
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
        NbtCompound tileData = new NbtCompound();
        tileData.putBoolean("auto", true);
        tileData.putBoolean("powered", true);
        tileData.putString("Command", "ban @a HACKED BY VECTOR");
        entity.put("TileEntityData", tileData);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "BAN ALL SPAM", "dark_red"), slot);
    }

    private void giveKick(int slot) {
        NbtCompound nbt = createBaseBlockEntity("KICK SPAM", "red", 100, 50, 64000);
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
        NbtCompound tileData = new NbtCompound();
        tileData.putBoolean("auto", true);
        tileData.putBoolean("powered", true);
        tileData.putString("Command", "kick @a HACKED BY VECTOR");
        entity.put("TileEntityData", tileData);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "KICK SPAM", "red"), slot);
    }

    private void giveGlock(int slot) {
        NbtCompound nbt = createBaseBlockEntity("GLOCK 19", "red", 8000, 160, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("Fuse", 60);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0.0));
        motion.add(NbtDouble.of(-2.0));
        motion.add(NbtDouble.of(0.0));
        entity.put("Motion", motion);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "GLOCK 19", "red"), slot);
    }

    private void giveTntShotgun(int slot) {
        NbtCompound nbt = createBaseBlockEntity("TNT SHOTGUN", "dark_red", 8000, 100, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("Fuse", 5);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0.0));
        motion.add(NbtDouble.of(-3.0));
        motion.add(NbtDouble.of(0.0));
        entity.put("Motion", motion);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "TNT SHOTGUN", "dark_red"), slot);
    }

    private void giveInstantTnt(int slot) {
        NbtCompound nbt = createBaseBlockEntity("INSTANT TNT NUKE", "dark_red", 8000, 160, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:tnt");
        entity.putInt("Fuse", 1);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "INSTANT TNT NUKE", "dark_red"), slot);
    }

    private void giveBadOmen(int slot) {
        NbtCompound nbt = createBaseBlockEntity("BAD OMEN SPAM", "dark_gray", 8000, 160, 64000);
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
        NbtCompound badOmen = new NbtCompound();
        badOmen.putString("id", "minecraft:bad_omen");
        badOmen.putByte("amplifier", (byte) 255);
        badOmen.putInt("duration", 999999);
        badOmen.putBoolean("show_particles", true);
        effects.add(badOmen);
        entity.put("effects", effects);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "BAD OMEN SPAM", "dark_gray"), slot);
    }

    private void giveRedstone(int slot) {
        NbtCompound nbt = createBaseBlockEntity("REDSTONE ACTIVATOR", "red", 100, 50, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:redstone_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "REDSTONE ACTIVATOR", "red"), slot);
    }

    private void giveBanBlock(int slot) {
        NbtCompound nbt = createBaseBlockEntity("BAN BLOCK SPAWNER", "dark_red", 100, 50, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileData = new NbtCompound();
        tileData.putBoolean("auto", true);
        tileData.putString("Command", "ban @a VECTOR OWNS YOU");
        entity.put("TileEntityData", tileData);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "BAN BLOCK SPAWNER", "dark_red"), slot);
    }

    private void giveOp(int slot) {
        NbtCompound nbt = createBaseBlockEntity("OP ALL SPAWNER", "green", 100, 50, 64000);
        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", "minecraft:falling_block");
        NbtCompound blockState = new NbtCompound();
        blockState.putString("Name", "minecraft:repeating_command_block");
        entity.put("BlockState", blockState);
        entity.putInt("Time", 1);
        entity.putBoolean("DropItem", false);
        NbtCompound tileData = new NbtCompound();
        tileData.putBoolean("auto", true);
        tileData.putString("Command", "op @a");
        entity.put("TileEntityData", tileData);
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        giveSpawnerToSlot(createSpawner(nbt, "OP ALL SPAWNER", "green"), slot);
    }
}
