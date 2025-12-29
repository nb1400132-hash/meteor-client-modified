/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.peter;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;

public class TPAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFlight = settings.createGroup("Flight Chase");

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Movement speed in blocks per second.")
        .defaultValue(5.6)
        .min(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("Orbit radius around target.")
        .defaultValue(2.0)
        .min(0.5)
        .sliderMax(5)
        .build()
    );

    private final Setting<Double> timerSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("timer")
        .description("Game tick multiplier.")
        .defaultValue(1.5)
        .min(1.0)
        .sliderMax(3)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Max distance to find targets.")
        .defaultValue(30)
        .min(5)
        .sliderMax(64)
        .build()
    );

    private final Setting<Boolean> faceTarget = sgGeneral.add(new BoolSetting.Builder()
        .name("face-target")
        .description("Always look at the target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoJump = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-jump")
        .description("Jump to follow target if they are higher.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnAttack = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-attack")
        .description("Pause movement when attacking for full damage.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> pauseTicks = sgGeneral.add(new IntSetting.Builder()
        .name("pause-ticks")
        .description("Ticks to pause after attacking.")
        .defaultValue(2)
        .min(1)
        .sliderMax(5)
        .visible(pauseOnAttack::get)
        .build()
    );

    private final Setting<Boolean> flightChase = sgFlight.add(new BoolSetting.Builder()
        .name("flight-chase")
        .description("Fly to chase airborne targets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> flightThreshold = sgFlight.add(new DoubleSetting.Builder()
        .name("flight-threshold")
        .description("Height difference to activate flight chase.")
        .defaultValue(3.0)
        .min(1)
        .sliderMax(10)
        .visible(flightChase::get)
        .build()
    );

    private final Setting<Double> verticalSpeed = sgFlight.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical flight speed.")
        .defaultValue(5.0)
        .min(1)
        .sliderMax(10)
        .visible(flightChase::get)
        .build()
    );

    private final Setting<Boolean> groundSpoof = sgFlight.add(new BoolSetting.Builder()
        .name("ground-spoof")
        .description("Spoof ground state while flying.")
        .defaultValue(true)
        .visible(flightChase::get)
        .build()
    );

    private PlayerEntity target = null;
    private double orbitAngle = 0;
    private int attackPauseTicks = 0;
    private boolean isFlying = false;

    public TPAura() {
        super(Categories.Peter, "tp-aura", "Orbit with jump and flight chase.");
    }

    @Override
    public void onActivate() {
        target = null;
        orbitAngle = 0;
        attackPauseTicks = 0;
        isFlying = false;
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(Timer.OFF);
        target = null;
        isFlying = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (attackPauseTicks > 0) attackPauseTicks--;

        findTarget();

        if (target == null || !target.isAlive()) {
            Modules.get().get(Timer.class).setOverride(Timer.OFF);
            isFlying = false;
            return;
        }

        Modules.get().get(Timer.class).setOverride(timerSpeed.get());

        if (faceTarget.get()) {
            lookAtTarget();
        }

        double yDiff = target.getY() - mc.player.getY();

        if (flightChase.get() && yDiff > flightThreshold.get()) {
            isFlying = true;
        } else if (mc.player.isOnGround()) {
            isFlying = false;
        }

        if (!isFlying && autoJump.get() && mc.player.isOnGround() && yDiff > 0.5) {
            mc.player.jump();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (pauseOnAttack.get() && event.packet instanceof IPlayerInteractEntityC2SPacket packet) {
            if (packet.meteor$getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
                attackPauseTicks = pauseTicks.get();
            }
        }

        if (groundSpoof.get() && isFlying && event.packet instanceof PlayerMoveC2SPacket) {
            ((PlayerMoveC2SPacketAccessor) event.packet).meteor$setOnGround(true);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF) return;
        if (mc.player == null || mc.world == null) return;
        if (target == null || !target.isAlive()) return;
        if (attackPauseTicks > 0) return;

        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();
        double playerX = mc.player.getX();
        double playerY = mc.player.getY();
        double playerZ = mc.player.getZ();

        double dx = targetX - playerX;
        double dz = targetZ - playerZ;
        double distToTarget = Math.sqrt(dx * dx + dz * dz);

        double bps = speed.get();
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            double amp = (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
            bps += bps * amp;
        }
        double moveSpeed = bps / 20.0;

        double moveX, moveZ, moveY = event.movement.y;

        if (distToTarget > radius.get() + 1.5) {
            double approachAngle = Math.atan2(dz, dx);
            moveX = Math.cos(approachAngle) * moveSpeed;
            moveZ = Math.sin(approachAngle) * moveSpeed;
        } else {
            double angularSpeed = moveSpeed / radius.get();
            orbitAngle += angularSpeed;

            double goalX = targetX + Math.cos(orbitAngle) * radius.get();
            double goalZ = targetZ + Math.sin(orbitAngle) * radius.get();

            moveX = goalX - playerX;
            moveZ = goalZ - playerZ;

            double moveDist = Math.sqrt(moveX * moveX + moveZ * moveZ);
            if (moveDist > moveSpeed) {
                moveX = (moveX / moveDist) * moveSpeed;
                moveZ = (moveZ / moveDist) * moveSpeed;
            }
        }

        if (isFlying && flightChase.get()) {
            double yDiff = targetY - playerY;
            double vSpeed = verticalSpeed.get() / 20.0;
            moveY = MathHelper.clamp(yDiff, -vSpeed, vSpeed);
            
            if (Math.abs(yDiff) < 0.5) {
                moveY = 0;
            }
        }

        double newX = playerX + moveX;
        double newZ = playerZ + moveZ;

        if (!isFlying && isCollidingAt(newX, playerY, newZ)) {
            if (!isCollidingAt(newX, playerY, playerZ)) {
                moveZ = 0;
            } else if (!isCollidingAt(playerX, playerY, newZ)) {
                moveX = 0;
            } else {
                moveX = 0;
                moveZ = 0;
            }
        }

        ((IVec3d) event.movement).meteor$set(moveX, moveY, moveZ);
    }

    private void findTarget() {
        target = mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .filter(p -> !p.isSpectator() && p.isAlive())
            .filter(p -> mc.player.distanceTo(p) <= range.get())
            .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
            .orElse(null);
    }

    private void lookAtTarget() {
        if (target == null) return;
        
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getEyeHeight(target.getPose()) / 2) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private boolean isCollidingAt(double x, double y, double z) {
        Box box = mc.player.getBoundingBox().offset(x - mc.player.getX(), 0, z - mc.player.getZ());
        return !mc.world.isSpaceEmpty(mc.player, box);
    }

    @Override
    public String getInfoString() {
        if (target == null) return null;
        return (isFlying ? "Flying " : "") + target.getGameProfile().name();
    }
}