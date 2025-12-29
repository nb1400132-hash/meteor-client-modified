package meteordevelopment.meteorclient.systems.modules.peter;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.Vec3d;

public class Superman extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgFallDamage;

    private final Setting<Double> speed;
    private final Setting<Boolean> preventFallDamage;
    private final Setting<Boolean> alwaysPreventFallDamage;

    public Superman() {
        super(Categories.Peter, "superman", "Fly like Superman without the Elytra pose.");

        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgFallDamage = this.settings.createGroup("Fall Damage");

        this.speed = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .description("Your flying speed.")
            .defaultValue(1.0)
            .min(0.1)
            .max(35.0)
            .sliderMin(0.1)
            .sliderMax(35.0)
            .build());

        this.preventFallDamage = this.sgFallDamage.add(new BoolSetting.Builder()
            .name("prevent-fall-damage")
            .description("Prevents fall damage when using Superman.")
            .defaultValue(true)
            .build());

        this.alwaysPreventFallDamage = this.sgFallDamage.add(new BoolSetting.Builder()
            .name("always-prevent")
            .description("Always prevents fall damage.")
            .defaultValue(true)
            .visible(preventFallDamage::get)
            .build());
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.setVelocity(0, 0, 0);
        }
    }

    @EventHandler
    private void onTick(final TickEvent.Pre event) {
        if (mc.player == null || !mc.player.isAlive()) {
            return;
        }

        // Calculate movement direction based on pressed keys and look direction
        Vec3d moveInput = Vec3d.ZERO;
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        // Forward (W)
        if (mc.options.forwardKey.isPressed()) {
            moveInput = moveInput.add(Vec3d.fromPolar(pitch, yaw));
        }
        // Backward (S)
        if (mc.options.backKey.isPressed()) {
            moveInput = moveInput.add(Vec3d.fromPolar(pitch, yaw).negate());
        }
        // Left (A)
        if (mc.options.leftKey.isPressed()) {
            moveInput = moveInput.add(Vec3d.fromPolar(0, yaw - 90));
        }
        // Right (D)
        if (mc.options.rightKey.isPressed()) {
            moveInput = moveInput.add(Vec3d.fromPolar(0, yaw + 90));
        }

        // Stop movement if no keys are pressed
        if (moveInput.equals(Vec3d.ZERO)) {
            mc.player.setVelocity(0.0, 0.0, 0.0);
            return;
        }

        // Apply speed
        Vec3d velocity = moveInput.normalize().multiply(this.speed.get());
        mc.player.setVelocity(velocity.x, velocity.y, velocity.z);
    }

    @EventHandler
    private void onSendPacket(final PacketEvent.Send event) {
        if (!this.preventFallDamage.get()) {
            return;
        }

        if (!(event.packet instanceof PlayerMoveC2SPacket)) {
            return;
        }

        if (((IPlayerMoveC2SPacket)event.packet).meteor$getTag() == 1337) {
            return;
        }

        // Spoof onGround to prevent fall damage (standard NoFall)
        if (this.alwaysPreventFallDamage.get() || (mc.player != null && mc.player.getVelocity().y < -0.5)) {
            ((PlayerMoveC2SPacketAccessor)event.packet).meteor$setOnGround(true);
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f", this.speed.get());
    }
}