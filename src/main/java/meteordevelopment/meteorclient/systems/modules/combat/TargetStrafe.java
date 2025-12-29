package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TargetStrafe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMovement = settings.createGroup("Movement");
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to find targets.")
        .defaultValue(5.0)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("strafe-distance")
        .description("The distance to maintain from the target while strafing.")
        .defaultValue(2.5)
        .min(0.5)
        .sliderRange(0.5, 6)
        .build()
    );

    private final Setting<Boolean> autoDirection = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-direction")
        .description("Automatically switches strafe direction when hitting a wall.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyWhileAttacking = sgGeneral.add(new BoolSetting.Builder()
        .name("only-while-attacking")
        .description("Only strafe while KillAura is active and attacking.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> speed = sgMovement.add(new DoubleSetting.Builder()
        .name("speed")
        .description("The strafe speed multiplier.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderRange(0.1, 3.0)
        .build()
    );

    private final Setting<Boolean> jump = sgMovement.add(new BoolSetting.Builder()
        .name("auto-jump")
        .description("Automatically jump while strafing.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> jumpDelay = sgMovement.add(new IntSetting.Builder()
        .name("jump-delay")
        .description("Delay between jumps in ticks.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 40)
        .visible(jump::get)
        .build()
    );

    private final Setting<Boolean> sprint = sgMovement.add(new BoolSetting.Builder()
        .name("sprint")
        .description("Automatically sprint while strafing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> strafeInAir = sgMovement.add(new BoolSetting.Builder()
        .name("strafe-in-air")
        .description("Continue strafing while in the air.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to select the target.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Boolean> players = sgTargeting.add(new BoolSetting.Builder()
        .name("players")
        .description("Target players.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> mobs = sgTargeting.add(new BoolSetting.Builder()
        .name("mobs")
        .description("Target mobs.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Don't strafe around friends.")
        .defaultValue(true)
        .build()
    );

    private Entity target;
    private int direction = 1;
    private int jumpTimer = 0;
    private int switchTimer = 0;

    public TargetStrafe() {
        super(Categories.Combat, "target-strafe", "Strafe around your target while attacking for harder hits.");
    }

    @Override
    public void onActivate() {
        target = null;
        direction = 1;
        jumpTimer = 0;
        switchTimer = 0;
    }

    @Override
    public void onDeactivate() {
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (onlyWhileAttacking.get()) {
            KillAura killAura = Modules.get().get(KillAura.class);
            if (killAura == null || !killAura.isActive() || !killAura.attacking) {
                target = null;
                return;
            }
            target = killAura.getTarget();
        } else {
            target = TargetUtils.get(entity -> {
                if (entity == mc.player) return false;
                if (!(entity instanceof LivingEntity living) || living.isDead()) return false;
                if (!PlayerUtils.isWithin(entity, range.get())) return false;

                if (entity instanceof PlayerEntity player) {
                    if (!players.get()) return false;
                    if (ignoreFriends.get() && Friends.get().isFriend(player)) return false;
                    if (player.isCreative() || player.isSpectator()) return false;
                }

                if (!(entity instanceof PlayerEntity) && !mobs.get()) return false;

                return true;
            }, priority.get());
        }

        if (target == null) return;

        if (!mc.player.isOnGround() && !strafeInAir.get()) return;

        if (autoDirection.get() && switchTimer <= 0) {
            if (mc.player.horizontalCollision) {
                direction *= -1;
                switchTimer = 10;
            }
        }
        if (switchTimer > 0) switchTimer--;

        strafe();

        if (jump.get() && mc.player.isOnGround()) {
            if (jumpTimer <= 0) {
                mc.player.jump();
                jumpTimer = jumpDelay.get();
            }
        }
        if (jumpTimer > 0) jumpTimer--;

        if (sprint.get() && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }

    private void strafe() {
        double targetX = target.getX();
        double targetZ = target.getZ();
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        double diffX = playerX - targetX;
        double diffZ = playerZ - targetZ;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        double strafeAngle = Math.atan2(diffZ, diffX) + (Math.PI / 2.0 * direction);

        double targetDist = distance.get();
        double moveX = targetX + Math.cos(strafeAngle) * targetDist - playerX;
        double moveZ = targetZ + Math.sin(strafeAngle) * targetDist - playerZ;

        double moveDist = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (moveDist > 0) {
            moveX /= moveDist;
            moveZ /= moveDist;
        }

        double baseSpeed = 0.2873 * speed.get();

        if (dist < targetDist * 0.8) {
            moveX = -diffX / dist;
            moveZ = -diffZ / dist;
        } else if (dist > targetDist * 1.2) {
            double toTarget = Math.atan2(diffZ, diffX) + Math.PI;
            moveX = Math.cos(toTarget);
            moveZ = Math.sin(toTarget);
        }

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(
            moveX * baseSpeed,
            velocity.y,
            moveZ * baseSpeed
        );

        float yaw = (float) Math.toDegrees(Math.atan2(-diffZ, -diffX)) - 90f;
        mc.player.setYaw(yaw);
        mc.player.bodyYaw = yaw;
        mc.player.headYaw = yaw;
    }

    public void switchDirection() {
        direction *= -1;
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public String getInfoString() {
        if (target != null) {
            if (target instanceof PlayerEntity player) {
                return player.getGameProfile().getName();
            }
            return target.getType().getName().getString();
        }
        return null;
    }
}
