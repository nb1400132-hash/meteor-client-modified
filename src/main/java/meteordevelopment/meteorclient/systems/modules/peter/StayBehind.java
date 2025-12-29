package meteordevelopment.meteorclient.systems.modules.peter;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Comparator;

public class StayBehind extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range").defaultValue(15).min(1).sliderMax(50).build()
    );

    private final Setting<Double> behindDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("behind-distance").defaultValue(1.5).min(0.5).sliderMax(5).build()
    );

    private final Setting<Double> movementSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed").defaultValue(0.5).min(0.1).sliderMax(2).build()
    );

    private final Setting<Boolean> vertical = sgGeneral.add(new BoolSetting.Builder()
        .name("vertical-axis")
        .description("Includes vertical pitch when calculating the position behind the player.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> antiWall = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-wall")
        .description("Prevents you from backing into/through walls.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate").defaultValue(true).build()
    );

    private PlayerEntity currentTarget = null;
    private Vec3d desiredPositionBehindTarget = null;

    public StayBehind() {
        super(Categories.Peter, "stay-behind", "Stays behind a player while preventing wall clipping.");
    }

    @Override
    public void onActivate() {
        currentTarget = null;
        desiredPositionBehindTarget = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (currentTarget == null || !currentTarget.isAlive() || mc.player.distanceTo(currentTarget) > targetRange.get()) {
            findTarget();
            if (currentTarget == null) {
                desiredPositionBehindTarget = null;
                return;
            }
        }

        calculatePosition();
    }

    private void findTarget() {
        this.currentTarget = mc.world.getPlayers().stream()
            .filter(player -> player != mc.player && player.isAlive() && mc.player.distanceTo(player) <= targetRange.get())
            .min(Comparator.comparingDouble(player -> mc.player.distanceTo(player)))
            .orElse(null);
    }

    private void calculatePosition() {
        if (currentTarget == null) return;

        Vec3d lookVec;
        if (vertical.get()) {
            lookVec = Vec3d.fromPolar(currentTarget.getPitch(), currentTarget.getYaw());
        } else {
            lookVec = Vec3d.fromPolar(0, currentTarget.getYaw());
        }

        // FIX: Replaced getPos() with manual coordinate retrieval
        Vec3d targetEyePos = new Vec3d(currentTarget.getX(), currentTarget.getY() + currentTarget.getEyeHeight(currentTarget.getPose()), currentTarget.getZ());
        Vec3d rawBehindPos = targetEyePos.subtract(lookVec.multiply(behindDistance.get()));

        if (antiWall.get()) {
            BlockHitResult hit = mc.world.raycast(new RaycastContext(
                targetEyePos,
                rawBehindPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
            ));

            if (hit.getType() == HitResult.Type.BLOCK) {
                desiredPositionBehindTarget = hit.getPos().add(lookVec.multiply(0.2)); 
            } else {
                desiredPositionBehindTarget = rawBehindPos;
            }
        } else {
            desiredPositionBehindTarget = rawBehindPos;
        }

        if (rotate.get()) {
            mc.player.setYaw(currentTarget.getYaw());
            if (vertical.get()) mc.player.setPitch(currentTarget.getPitch());
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null || currentTarget == null || desiredPositionBehindTarget == null) return;
        if (event.type != MovementType.SELF) return;

        // FIX: Replaced getPos() with manual coordinate retrieval
        Vec3d playerEyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        Vec3d diff = desiredPositionBehindTarget.subtract(playerEyePos);
        
        double distSq = diff.lengthSquared();
        if (distSq < 0.001) {
            ((IVec3d) event.movement).meteor$set(0, 0, 0);
            return;
        }

        double speed = Math.min(movementSpeed.get(), Math.sqrt(distSq));
        Vec3d velocity = diff.normalize().multiply(speed);

        ((IVec3d) event.movement).meteor$set(velocity.x, velocity.y, velocity.z);
    }
}