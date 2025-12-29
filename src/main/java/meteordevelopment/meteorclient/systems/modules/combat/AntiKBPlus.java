package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.EntityVelocityUpdateS2CPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

public class AntiKBPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgModes = settings.createGroup("Modes");
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("How to handle knockback.")
        .defaultValue(Mode.Cancel)
        .build()
    );

    private final Setting<Double> horizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal")
        .description("Horizontal knockback multiplier.")
        .defaultValue(0.0)
        .sliderRange(-1.0, 1.0)
        .visible(() -> mode.get() == Mode.Modify)
        .build()
    );

    private final Setting<Double> vertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical")
        .description("Vertical knockback multiplier.")
        .defaultValue(0.0)
        .sliderRange(-1.0, 1.0)
        .visible(() -> mode.get() == Mode.Modify)
        .build()
    );

    private final Setting<Boolean> knockback = sgModes.add(new BoolSetting.Builder()
        .name("knockback")
        .description("Prevent knockback from attacks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> explosions = sgModes.add(new BoolSetting.Builder()
        .name("explosions")
        .description("Prevent knockback from explosions.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> fishing = sgModes.add(new BoolSetting.Builder()
        .name("fishing-rods")
        .description("Prevent knockback from fishing rods.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> water = sgModes.add(new BoolSetting.Builder()
        .name("water-push")
        .description("Prevent being pushed by water.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> entityPush = sgModes.add(new BoolSetting.Builder()
        .name("entity-push")
        .description("Prevent being pushed by entities.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> blockPush = sgModes.add(new BoolSetting.Builder()
        .name("block-push")
        .description("Prevent being pushed out of blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> grim = sgAdvanced.add(new BoolSetting.Builder()
        .name("grim-bypass")
        .description("Attempt to bypass Grim anticheat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> grimDelay = sgAdvanced.add(new IntSetting.Builder()
        .name("grim-delay")
        .description("Ticks to wait before resetting velocity for Grim bypass.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 10)
        .visible(grim::get)
        .build()
    );

    private final Setting<Boolean> onlyInCombat = sgAdvanced.add(new BoolSetting.Builder()
        .name("only-in-combat")
        .description("Only prevent knockback when in combat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> combatTimeout = sgAdvanced.add(new IntSetting.Builder()
        .name("combat-timeout")
        .description("Ticks until no longer considered in combat.")
        .defaultValue(40)
        .min(1)
        .sliderRange(1, 100)
        .visible(onlyInCombat::get)
        .build()
    );

    private int lastHitTicks = 0;
    private int grimTicks = 0;
    private Vec3d savedVelocity = null;

    public AntiKBPlus() {
        super(Categories.Combat, "anti-kb-plus", "Advanced knockback prevention with multiple modes and bypass options.");
    }

    @Override
    public void onActivate() {
        lastHitTicks = combatTimeout.get() + 1;
        grimTicks = 0;
        savedVelocity = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        lastHitTicks++;

        if (grim.get() && savedVelocity != null && grimTicks > 0) {
            grimTicks--;
            if (grimTicks == 0) {
                ((IVec3d) mc.player.getVelocity()).meteor$set(savedVelocity.x, savedVelocity.y, savedVelocity.z);
                savedVelocity = null;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (onlyInCombat.get() && lastHitTicks > combatTimeout.get()) return;

        if (event.packet instanceof EntityVelocityUpdateS2CPacket packet && knockback.get()) {
            if (packet.getEntityId() != mc.player.getId()) return;

            if (mode.get() == Mode.Cancel) {
                event.cancel();
            } else if (mode.get() == Mode.Modify) {
                Vec3d currentVel = mc.player.getVelocity();
                Vec3d packetVel = packet.getVelocity();

                double newX = currentVel.x + (packetVel.getX() - currentVel.x) * horizontal.get();
                double newY = currentVel.y + (packetVel.getY() - currentVel.y) * vertical.get();
                double newZ = currentVel.z + (packetVel.getZ() - currentVel.z) * horizontal.get();

                if (grim.get()) {
                    savedVelocity = new Vec3d(newX, newY, newZ);
                    grimTicks = grimDelay.get();
                } else {
                    ((EntityVelocityUpdateS2CPacketAccessor) packet).meteor$setVelocity(new Vec3d(newX, newY, newZ));
                }
            } else if (mode.get() == Mode.Reverse) {
                Vec3d packetVel = packet.getVelocity();
                ((EntityVelocityUpdateS2CPacketAccessor) packet).meteor$setVelocity(
                    new Vec3d(-packetVel.getX() * 0.5, packetVel.getY() * 0.1, -packetVel.getZ() * 0.5)
                );
            }
        }

        if (event.packet instanceof ExplosionS2CPacket && explosions.get()) {
            if (mode.get() == Mode.Cancel) {
                event.cancel();
            }
        }

        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 2) {
                lastHitTicks = 0;
            }
        }
    }

    public boolean cancelFishing() {
        if (!isActive() || !fishing.get()) return false;
        if (onlyInCombat.get() && lastHitTicks > combatTimeout.get()) return false;
        return true;
    }

    public boolean cancelWater() {
        if (!isActive() || !water.get()) return false;
        if (onlyInCombat.get() && lastHitTicks > combatTimeout.get()) return false;
        return true;
    }

    public double getEntityPushMultiplier() {
        if (!isActive() || !entityPush.get()) return 1.0;
        if (onlyInCombat.get() && lastHitTicks > combatTimeout.get()) return 1.0;
        return 0.0;
    }

    public boolean cancelBlockPush() {
        if (!isActive() || !blockPush.get()) return false;
        if (onlyInCombat.get() && lastHitTicks > combatTimeout.get()) return false;
        return true;
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }

    public enum Mode {
        Cancel,
        Modify,
        Reverse
    }
}
