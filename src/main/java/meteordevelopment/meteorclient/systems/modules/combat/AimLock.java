package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class AimLock extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgSmoothing = settings.createGroup("Smoothing");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to lock onto targets.")
        .defaultValue(6.0)
        .min(1)
        .sliderRange(1, 15)
        .build()
    );

    private final Setting<Double> fov = sgGeneral.add(new DoubleSetting.Builder()
        .name("fov")
        .description("Field of view to acquire targets.")
        .defaultValue(180.0)
        .min(1)
        .sliderRange(1, 360)
        .build()
    );

    private final Setting<Target> bodyTarget = sgGeneral.add(new EnumSetting.Builder<Target>()
        .name("body-target")
        .description("Which part of the body to aim at.")
        .defaultValue(Target.Head)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only lock aim while holding attack.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> throughWalls = sgGeneral.add(new BoolSetting.Builder()
        .name("through-walls")
        .description("Lock onto targets through walls.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> wallsRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("Range to lock through walls.")
        .defaultValue(3.0)
        .min(1)
        .sliderRange(1, 10)
        .visible(() -> !throughWalls.get())
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to target.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to select the target.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Don't lock onto friends.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreNaked = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-naked")
        .description("Don't lock onto players with no armor.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> smooth = sgSmoothing.add(new BoolSetting.Builder()
        .name("smooth-aim")
        .description("Smoothly rotate to target instead of snapping.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> smoothSpeed = sgSmoothing.add(new DoubleSetting.Builder()
        .name("smooth-speed")
        .description("How fast to rotate to target. Higher = faster.")
        .defaultValue(5.0)
        .min(0.1)
        .sliderRange(0.1, 20.0)
        .visible(smooth::get)
        .build()
    );

    private final Setting<Double> randomization = sgSmoothing.add(new DoubleSetting.Builder()
        .name("randomization")
        .description("Add random jitter to aim for more human-like movement.")
        .defaultValue(0.0)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );

    private final Setting<Boolean> prediction = sgSmoothing.add(new BoolSetting.Builder()
        .name("prediction")
        .description("Predict target movement and aim ahead.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> predictionStrength = sgSmoothing.add(new DoubleSetting.Builder()
        .name("prediction-strength")
        .description("How much to predict ahead.")
        .defaultValue(2.0)
        .min(0)
        .sliderRange(0, 10)
        .visible(prediction::get)
        .build()
    );

    private Entity target;
    private Vec3d lastTargetPos;
    private float targetYaw, targetPitch;

    public AimLock() {
        super(Categories.Combat, "aim-lock", "Automatically aim at targets with smooth rotation.");
    }

    @Override
    public void onActivate() {
        target = null;
        lastTargetPos = null;
    }

    @Override
    public void onDeactivate() {
        target = null;
        lastTargetPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (onlyOnClick.get() && !mc.options.attackKey.isPressed()) {
            target = null;
            return;
        }

        target = TargetUtils.get(this::isValidTarget, priority.get());

        if (target == null) {
            lastTargetPos = null;
            return;
        }

        Vec3d targetPos = getTargetPos(target);

        if (prediction.get() && lastTargetPos != null) {
            Vec3d velocity = targetPos.subtract(lastTargetPos);
            targetPos = targetPos.add(velocity.multiply(predictionStrength.get()));
        }
        lastTargetPos = getTargetPos(target);

        if (randomization.get() > 0) {
            double randX = (Math.random() - 0.5) * 2 * randomization.get() * 0.01;
            double randY = (Math.random() - 0.5) * 2 * randomization.get() * 0.01;
            double randZ = (Math.random() - 0.5) * 2 * randomization.get() * 0.01;
            targetPos = targetPos.add(randX, randY, randZ);
        }

        targetYaw = (float) Rotations.getYaw(targetPos);
        targetPitch = (float) Rotations.getPitch(targetPos);

        if (smooth.get()) {
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();

            float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
            float pitchDiff = targetPitch - currentPitch;

            float speed = (float) (smoothSpeed.get() * 0.1);

            float newYaw = currentYaw + yawDiff * speed;
            float newPitch = currentPitch + pitchDiff * speed;

            newPitch = MathHelper.clamp(newPitch, -90f, 90f);

            mc.player.setYaw(newYaw);
            mc.player.setPitch(newPitch);
        } else {
            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player) return false;
        if (entity == mc.getCameraEntity()) return false;
        if (!(entity instanceof LivingEntity living) || living.isDead() || !entity.isAlive()) return false;
        if (!entities.get().contains(entity.getType())) return false;
        if (!PlayerUtils.isWithin(entity, range.get())) return false;

        if (!throughWalls.get() && !PlayerUtils.canSeeEntity(entity)) {
            if (!PlayerUtils.isWithin(entity, wallsRange.get())) return false;
        }

        double angleTo = getAngleTo(entity);
        if (angleTo > fov.get() / 2.0) return false;

        if (entity instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return false;
            if (ignoreFriends.get() && Friends.get().isFriend(player)) return false;
            if (ignoreNaked.get() && !hasArmor(player)) return false;
        }

        return true;
    }

    private Vec3d getTargetPos(Entity entity) {
        Vec3d pos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        return switch (bodyTarget.get()) {
            case Head -> pos.add(0, entity.getEyeHeight(entity.getPose()), 0);
            case Body -> pos.add(0, entity.getHeight() / 2, 0);
            case Feet -> pos;
        };
    }

    private double getAngleTo(Entity entity) {
        Vec3d playerDir = mc.player.getRotationVec(1.0f);
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3d toEntity = entityPos.subtract(playerPos).normalize();
        return Math.toDegrees(Math.acos(playerDir.dotProduct(toEntity)));
    }

    private boolean hasArmor(PlayerEntity player) {
        if (!player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) return true;
        if (!player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) return true;
        if (!player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) return true;
        if (!player.getEquippedStack(EquipmentSlot.FEET).isEmpty()) return true;
        return false;
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public String getInfoString() {
        if (target != null && target instanceof PlayerEntity player) {
            return player.getGameProfile().name();
        }
        return target != null ? target.getType().getName().getString() : null;
    }
}
