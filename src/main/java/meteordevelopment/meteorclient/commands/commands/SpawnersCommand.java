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
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpawnersCommand extends Command {

    public SpawnersCommand() {
        super("spawners", "Gives you all chaos spawners.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player == null || !mc.player.getAbilities().creativeMode) {
                error("You must be in Creative mode to use this.");
                return SINGLE_SUCCESS;
            }

            int slot = 0;
            slot = giveSpawner(slot, "LIGHTNING NUKE", Formatting.YELLOW, "minecraft:lightning_bolt", null, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveTntSpawner(slot, "TNT RAIN", Formatting.RED, 40, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveTntMinecartSpawner(slot, "TNT MINECART STORM", Formatting.DARK_RED, 20, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveArrowSpawner(slot, "ARROW STORM", Formatting.GRAY, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveGiantSpawner(slot);
            slot = giveChargedCreeperSpawner(slot, "CHARGED CREEPER SWARM", Formatting.AQUA, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveFireballSpawner(slot, "FIREBALL HELL", Formatting.GOLD, 10, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveFallingBlockSpawner(slot, "FALLING ANVIL DEATH", Formatting.DARK_GRAY, "minecraft:anvil", true, 1000, 100.0f, 1, 1, 1, 8000, 6400, 640, 160);
            slot = giveFallingBlockSpawner(slot, "LAVA FLOOD", Formatting.GOLD, "minecraft:lava", false, 0, 0, 1, 1, 1, 8000, 6400, 640, 50);
            slot = giveXpOrbSpawner(slot, "XP ORB FLOOD", Formatting.GREEN, 32767, 1, 1, 1, 10000, 32767, 640, 160);
            slot = givePoisonCloudSpawner(slot);
            slot = giveSpawner(slot, "ENDER DRAGON APOCALYPSE", Formatting.DARK_PURPLE, "minecraft:ender_dragon", null, 1, 1, 1, 500, 6400, 640, 160);
            slot = giveWitherSpawner(slot);
            slot = giveWardenSpawner(slot);
            slot = giveShulkerBulletSpawner(slot, "SHULKER BULLET STORM", Formatting.LIGHT_PURPLE, 1, 1, 1, 10000, 32767, 640, 160);
            slot = giveTridentSpawner(slot, "TRIDENT STORM", Formatting.AQUA, 1, 1, 1, 8000, 32767, 640, 160);
            slot = giveFallingBlockSpawner(slot, "FALLING BEDROCK PRISON", Formatting.DARK_GRAY, "minecraft:bedrock", true, 1000, 100.0f, 1, 1, 1, 8000, 32767, 640, 100);
            slot = giveFallingBlockSpawner(slot, "WATER FLOOD", Formatting.BLUE, "minecraft:water", false, 0, 0, 1, 1, 1, 8000, 32767, 640, 100);
            slot = giveCommandBlockSpawner(slot, "BAN ALL SPAM", Formatting.DARK_RED, "ban @a HACKED BY VECTOR", 1, 1, 1, 100, 32767, 640, 50);
            slot = giveCommandBlockSpawner(slot, "KICK SPAM", Formatting.RED, "kick @a HACKED BY VECTOR", 1, 1, 1, 100, 32767, 640, 50);
            slot = giveTntSpawner(slot, "GLOCK 19", Formatting.RED, 60, 1, 1, 1, 8000, 32767, 640, 160);
            slot = giveTntSpawner(slot, "TNT SHOTGUN", Formatting.DARK_RED, 5, 1, 1, 1, 8000, 32767, 640, 100);
            slot = giveTntSpawner(slot, "INSTANT TNT NUKE", Formatting.DARK_RED, 1, 1, 1, 1, 8000, 32767, 640, 160);
            slot = giveBadOmenSpawner(slot);
            slot = giveFallingBlockSpawner(slot, "REDSTONE ACTIVATOR", Formatting.RED, "minecraft:redstone_block", false, 0, 0, 1, 1, 1, 100, 32767, 640, 50);
            slot = giveCommandBlockSpawner(slot, "BAN BLOCK SPAWNER", Formatting.DARK_RED, "ban @a VECTOR OWNS YOU", 1, 1, 1, 100, 32767, 640, 50);
            slot = giveCommandBlockSpawner(slot, "OP ALL SPAWNER", Formatting.GREEN, "op @a", 1, 1, 1, 100, 32767, 640, 50);

            info("Gave " + slot + " chaos spawners to your inventory.");
            return SINGLE_SUCCESS;
        });
    }

    private int giveSpawner(int slot, String name, Formatting color, String entityId, NbtCompound extraEntityData, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound blockEntityData = createSpawnerNbt(entityId, extraEntityData, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange, name);
            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, blockEntityData));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed spawner " + name + ": " + e.getMessage());
            return slot;
        }
    }

    private NbtCompound createSpawnerNbt(String entityId, NbtCompound extraEntityData, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange, String customName) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", "minecraft:mob_spawner");
        if (customName != null) nbt.putString("CustomName", "{\"text\":\"" + customName + "\",\"bold\":true}");
        nbt.putShort("Delay", (short) delay);
        nbt.putShort("MinSpawnDelay", (short) minDelay);
        nbt.putShort("MaxSpawnDelay", (short) maxDelay);
        nbt.putShort("SpawnCount", (short) spawnCount);
        nbt.putShort("MaxNearbyEntities", (short) maxNearby);
        nbt.putShort("RequiredPlayerRange", (short) playerRange);
        nbt.putShort("SpawnRange", (short) spawnRange);

        NbtCompound spawnData = new NbtCompound();
        NbtCompound entity = new NbtCompound();
        entity.putString("id", entityId);
        if (extraEntityData != null) {
            for (String key : extraEntityData.getKeys()) {
                entity.put(key, extraEntityData.get(key));
            }
        }
        spawnData.put("entity", entity);
        nbt.put("SpawnData", spawnData);
        nbt.put("SpawnPotentials", new NbtList());
        return nbt;
    }

    private int giveTntSpawner(int slot, String name, Formatting color, int fuse, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putInt("fuse", fuse);
        return giveSpawner(slot, name, color, "minecraft:tnt", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int giveTntMinecartSpawner(int slot, String name, Formatting color, int fuse, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putInt("fuse", fuse);
        return giveSpawner(slot, name, color, "minecraft:tnt_minecart", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int giveArrowSpawner(int slot, String name, Formatting color, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putDouble("damage", 100.0);
        extra.putByte("pickup", (byte) 0);
        extra.putBoolean("crit", true);
        extra.putByte("PierceLevel", (byte) 127);
        return giveSpawner(slot, name, color, "minecraft:arrow", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int giveGiantSpawner(int slot) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putString("CustomName", "{\"text\":\"GIANT NETHERITE ZOMBIES\",\"bold\":true}");
            nbt.putShort("Delay", (short) 1);
            nbt.putShort("MinSpawnDelay", (short) 1);
            nbt.putShort("MaxSpawnDelay", (short) 1);
            nbt.putShort("SpawnCount", (short) 500);
            nbt.putShort("MaxNearbyEntities", (short) 6400);
            nbt.putShort("RequiredPlayerRange", (short) 640);
            nbt.putShort("SpawnRange", (short) 100);

            NbtCompound spawnData = new NbtCompound();
            NbtCompound entity = new NbtCompound();
            entity.putString("id", "minecraft:giant");
            entity.putString("CustomName", "{\"text\":\"NETHERITE TITAN\",\"bold\":true,\"color\":\"dark_red\"}");
            entity.putFloat("Health", 10000f);

            NbtList attributes = new NbtList();
            NbtCompound maxHealth = new NbtCompound();
            maxHealth.putString("id", "minecraft:generic.max_health");
            maxHealth.putDouble("base", 10000d);
            attributes.add(maxHealth);
            NbtCompound attackDamage = new NbtCompound();
            attackDamage.putString("id", "minecraft:generic.attack_damage");
            attackDamage.putDouble("base", 500d);
            attributes.add(attackDamage);
            NbtCompound speed = new NbtCompound();
            speed.putString("id", "minecraft:generic.movement_speed");
            speed.putDouble("base", 0.5d);
            attributes.add(speed);
            entity.put("attributes", attributes);

            NbtList handItems = new NbtList();
            NbtCompound sword = new NbtCompound();
            sword.putString("id", "minecraft:netherite_sword");
            sword.putInt("count", 1);
            handItems.add(sword);
            handItems.add(new NbtCompound());
            entity.put("HandItems", handItems);

            NbtList armorItems = new NbtList();
            String[] armorPieces = {"minecraft:netherite_boots", "minecraft:netherite_leggings", "minecraft:netherite_chestplate", "minecraft:netherite_helmet"};
            for (String piece : armorPieces) {
                NbtCompound armor = new NbtCompound();
                armor.putString("id", piece);
                armor.putInt("count", 1);
                armorItems.add(armor);
            }
            entity.put("ArmorItems", armorItems);

            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal("GIANT NETHERITE ZOMBIES").styled(s -> s.withColor(Formatting.DARK_PURPLE).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed giant spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveChargedCreeperSpawner(int slot, String name, Formatting color, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putBoolean("powered", true);
        extra.putByte("ExplosionRadius", (byte) 127);
        extra.putShort("Fuse", (short) 1);
        extra.putBoolean("ignited", true);
        return giveSpawner(slot, name, color, "minecraft:creeper", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int giveFireballSpawner(int slot, String name, Formatting color, int explosionPower, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) delay);
            nbt.putShort("MinSpawnDelay", (short) minDelay);
            nbt.putShort("MaxSpawnDelay", (short) maxDelay);
            nbt.putShort("SpawnCount", (short) spawnCount);
            nbt.putShort("MaxNearbyEntities", (short) maxNearby);
            nbt.putShort("RequiredPlayerRange", (short) playerRange);
            nbt.putShort("SpawnRange", (short) spawnRange);

            NbtCompound spawnData = new NbtCompound();
            NbtCompound entity = new NbtCompound();
            entity.putString("id", "minecraft:fireball");
            entity.putByte("ExplosionPower", (byte) explosionPower);
            NbtList power = new NbtList();
            power.add(NbtDouble.of(0.0));
            power.add(NbtDouble.of(-1.0));
            power.add(NbtDouble.of(0.0));
            entity.put("power", power);
            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed fireball spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveFallingBlockSpawner(int slot, String name, Formatting color, String blockId, boolean hurtEntities, int fallHurtMax, float fallHurtAmount, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) delay);
            nbt.putShort("MinSpawnDelay", (short) minDelay);
            nbt.putShort("MaxSpawnDelay", (short) maxDelay);
            nbt.putShort("SpawnCount", (short) spawnCount);
            nbt.putShort("MaxNearbyEntities", (short) maxNearby);
            nbt.putShort("RequiredPlayerRange", (short) playerRange);
            nbt.putShort("SpawnRange", (short) spawnRange);

            NbtCompound spawnData = new NbtCompound();
            NbtCompound entity = new NbtCompound();
            entity.putString("id", "minecraft:falling_block");
            NbtCompound blockState = new NbtCompound();
            blockState.putString("Name", blockId);
            entity.put("BlockState", blockState);
            entity.putInt("Time", 1);
            entity.putBoolean("DropItem", false);
            if (hurtEntities) {
                entity.putBoolean("HurtEntities", true);
                entity.putInt("FallHurtMax", fallHurtMax);
                entity.putFloat("FallHurtAmount", fallHurtAmount);
            }
            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed falling block spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveXpOrbSpawner(int slot, String name, Formatting color, int value, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putInt("Value", value);
        return giveSpawner(slot, name, color, "minecraft:experience_orb", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int givePoisonCloudSpawner(int slot) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) 1);
            nbt.putShort("MinSpawnDelay", (short) 1);
            nbt.putShort("MaxSpawnDelay", (short) 1);
            nbt.putShort("SpawnCount", (short) 8000);
            nbt.putShort("MaxNearbyEntities", (short) 32767);
            nbt.putShort("RequiredPlayerRange", (short) 640);
            nbt.putShort("SpawnRange", (short) 160);

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
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal("POISON CLOUD NUKE").styled(s -> s.withColor(Formatting.DARK_GREEN).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed poison cloud spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveWitherSpawner(int slot) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) 1);
            nbt.putShort("MinSpawnDelay", (short) 1);
            nbt.putShort("MaxSpawnDelay", (short) 1);
            nbt.putShort("SpawnCount", (short) 500);
            nbt.putShort("MaxNearbyEntities", (short) 6400);
            nbt.putShort("RequiredPlayerRange", (short) 640);
            nbt.putShort("SpawnRange", (short) 160);

            NbtCompound spawnData = new NbtCompound();
            NbtCompound entity = new NbtCompound();
            entity.putString("id", "minecraft:wither");
            entity.putString("CustomName", "{\"text\":\"DEATH\",\"bold\":true,\"color\":\"dark_red\",\"obfuscated\":true}");
            entity.putBoolean("CustomNameVisible", true);
            entity.putFloat("Health", 100000f);

            NbtList attributes = new NbtList();
            NbtCompound maxHealth = new NbtCompound();
            maxHealth.putString("id", "minecraft:generic.max_health");
            maxHealth.putDouble("base", 100000d);
            attributes.add(maxHealth);
            entity.put("attributes", attributes);

            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal("WITHER ARMY").styled(s -> s.withColor(Formatting.DARK_GRAY).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed wither spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveWardenSpawner(int slot) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) 1);
            nbt.putShort("MinSpawnDelay", (short) 1);
            nbt.putShort("MaxSpawnDelay", (short) 1);
            nbt.putShort("SpawnCount", (short) 1000);
            nbt.putShort("MaxNearbyEntities", (short) 6400);
            nbt.putShort("RequiredPlayerRange", (short) 640);
            nbt.putShort("SpawnRange", (short) 160);

            NbtCompound spawnData = new NbtCompound();
            NbtCompound entity = new NbtCompound();
            entity.putString("id", "minecraft:warden");
            entity.putString("CustomName", "{\"text\":\"SONIC DEATH\",\"bold\":true,\"color\":\"dark_blue\"}");
            entity.putBoolean("CustomNameVisible", true);
            entity.putFloat("Health", 50000f);

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

            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal("WARDEN APOCALYPSE").styled(s -> s.withColor(Formatting.DARK_AQUA).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed warden spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveShulkerBulletSpawner(int slot, String name, Formatting color, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putInt("Steps", 100);
        extra.putDouble("TXD", 0.0);
        extra.putDouble("TYD", -1.0);
        extra.putDouble("TZD", 0.0);
        return giveSpawner(slot, name, color, "minecraft:shulker_bullet", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int giveTridentSpawner(int slot, String name, Formatting color, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        NbtCompound extra = new NbtCompound();
        extra.putDouble("damage", 50.0);
        extra.putBoolean("DealtDamage", false);
        return giveSpawner(slot, name, color, "minecraft:trident", extra, delay, minDelay, maxDelay, spawnCount, maxNearby, playerRange, spawnRange);
    }

    private int giveBadOmenSpawner(int slot) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) 1);
            nbt.putShort("MinSpawnDelay", (short) 1);
            nbt.putShort("MaxSpawnDelay", (short) 1);
            nbt.putShort("SpawnCount", (short) 8000);
            nbt.putShort("MaxNearbyEntities", (short) 32767);
            nbt.putShort("RequiredPlayerRange", (short) 640);
            nbt.putShort("SpawnRange", (short) 160);

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
            effect.putByte("amplifier", (byte) 127);
            effect.putInt("duration", 999999);
            effect.putBoolean("show_particles", true);
            effects.add(effect);
            entity.put("effects", effects);

            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal("BAD OMEN SPAM").styled(s -> s.withColor(Formatting.DARK_GRAY).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed bad omen spawner: " + e.getMessage());
            return slot;
        }
    }

    private int giveCommandBlockSpawner(int slot, String name, Formatting color, String command, int delay, int minDelay, int maxDelay, int spawnCount, int maxNearby, int playerRange, int spawnRange) {
        if (slot >= 36) return slot;
        try {
            ItemStack spawner = new ItemStack(Items.SPAWNER, 64);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id", "minecraft:mob_spawner");
            nbt.putShort("Delay", (short) delay);
            nbt.putShort("MinSpawnDelay", (short) minDelay);
            nbt.putShort("MaxSpawnDelay", (short) maxDelay);
            nbt.putShort("SpawnCount", (short) spawnCount);
            nbt.putShort("MaxNearbyEntities", (short) maxNearby);
            nbt.putShort("RequiredPlayerRange", (short) playerRange);
            nbt.putShort("SpawnRange", (short) spawnRange);

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
            tileEntityData.putString("Command", command);
            entity.put("TileEntityData", tileEntityData);

            spawnData.put("entity", entity);
            nbt.put("SpawnData", spawnData);
            nbt.put("SpawnPotentials", new NbtList());

            spawner.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.MOB_SPAWNER, nbt));
            spawner.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, spawner));
            return slot + 1;
        } catch (Exception e) {
            error("Failed command block spawner: " + e.getMessage());
            return slot;
        }
    }
}
