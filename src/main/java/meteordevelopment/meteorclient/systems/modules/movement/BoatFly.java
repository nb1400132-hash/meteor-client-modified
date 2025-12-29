package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgAntiKick = settings.createGroup("Anti-Kick");

    private final Setting<Double> horizontalSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal flight speed.")
        .defaultValue(2.0)
        .min(0.1)
        .sliderRange(0.1, 10.0)
        .build()
    );

    private final Setting<Double> verticalSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical flight speed.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderRange(0.1, 5.0)
        .build()
    );

    private final Setting<Double> fallSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("fall-speed")
        .description("Fall speed when not pressing any key.")
        .defaultValue(0.0)
        .min(-1)
        .sliderRange(-1, 1)
        .build()
    );

    private final Setting<Boolean> accelerate = sgSpeed.add(new BoolSetting.Builder()
        .name("accelerate")
        .description("Gradually accelerate instead of instant speed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> acceleration = sgSpeed.add(new DoubleSetting.Builder()
        .name("acceleration")
        .description("Acceleration rate.")
        .defaultValue(0.1)
        .min(0.01)
        .sliderRange(0.01, 1.0)
        .visible(accelerate::get)
        .build()
    );

    private final Setting<Boolean> antiKick = sgAntiKick.add(new BoolSetting.Builder()
        .name("anti-kick")
        .description("Prevent being kicked for flying.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> antiKickInterval = sgAntiKick.add(new IntSetting.Builder()
        .name("anti-kick-interval")
        .description("Ticks between anti-kick packets.")
        .defaultValue(20)
        .min(1)
        .sliderRange(1, 40)
        .visible(antiKick::get)
        .build()
    );

    private final Setting<Double> antiKickDrop = sgAntiKick.add(new DoubleSetting.Builder()
        .name("anti-kick-drop")
        .description("How much to drop for anti-kick.")
        .defaultValue(0.04)
        .min(0.01)
        .sliderRange(0.01, 0.1)
        .visible(antiKick::get)
        .build()
    );

    private final Setting<Boolean> lockYaw = sgGeneral.add(new BoolSetting.Builder()
        .name("lock-yaw")
        .description("Lock the boat's yaw to your look direction.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> cancelGravity = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-gravity")
        .description("Cancel boat gravity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> cancelWaterDrag = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-water-drag")
        .description("Cancel boat water drag.")
        .defaultValue(true)
        .build()
    );

    private double currentSpeed = 0;
    private int tickCounter = 0;

    public BoatFly() {
        super(Categories.Movement, "boat-fly", "Fly using boats. Works on some servers.");
    }

    @Override
    public void onActivate() {
        currentSpeed = 0;
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;

        tickCounter++;

        if (lockYaw.get()) {
            boat.setYaw(mc.player.getYaw());
        }

        double velX = 0;
        double velY = fallSpeed.get();
        double velZ = 0;

        double targetSpeed = horizontalSpeed.get();
        if (accelerate.get()) {
            if (mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() ||
                mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed()) {
                currentSpeed = Math.min(currentSpeed + acceleration.get(), targetSpeed);
            } else {
                currentSpeed = Math.max(currentSpeed - acceleration.get() * 2, 0);
            }
            targetSpeed = currentSpeed;
        }

        float yaw = mc.player.getYaw();

        if (mc.options.forwardKey.isPressed()) {
            velX -= Math.sin(Math.toRadians(yaw)) * targetSpeed * 0.05;
            velZ += Math.cos(Math.toRadians(yaw)) * targetSpeed * 0.05;
        }
        if (mc.options.backKey.isPressed()) {
            velX += Math.sin(Math.toRadians(yaw)) * targetSpeed * 0.05;
            velZ -= Math.cos(Math.toRadians(yaw)) * targetSpeed * 0.05;
        }
        if (mc.options.leftKey.isPressed()) {
            velX += Math.cos(Math.toRadians(yaw)) * targetSpeed * 0.05;
            velZ += Math.sin(Math.toRadians(yaw)) * targetSpeed * 0.05;
        }
        if (mc.options.rightKey.isPressed()) {
            velX -= Math.cos(Math.toRadians(yaw)) * targetSpeed * 0.05;
            velZ -= Math.sin(Math.toRadians(yaw)) * targetSpeed * 0.05;
        }

        if (mc.options.jumpKey.isPressed()) {
            velY = verticalSpeed.get() * 0.05;
        }
        if (mc.options.sneakKey.isPressed()) {
            velY = -verticalSpeed.get() * 0.05;
        }

        if (antiKick.get() && tickCounter % antiKickInterval.get() == 0) {
            velY -= antiKickDrop.get();
        }

        boat.setVelocity(velX, velY, velZ);

        if (cancelGravity.get()) {
            boat.setNoGravity(true);
        }
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof BoatEntity)) return;

        if (cancelGravity.get()) {
            event.boat.setNoGravity(true);
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player == null) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle instanceof BoatEntity boat) {
            boat.setNoGravity(false);
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f", horizontalSpeed.get());
    }
}
