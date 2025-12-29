package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.command.CommandSource.suggestMatching;

public class HuntCommand extends Command {
    private PlayerEntity target = null;
    private boolean isHunting = false;
    private final double SPEED = 5.0;

    public HuntCommand() {
        super("hunt", "Continually chases a player using Superman logic.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Explicit literal for .hunt stop
        builder.then(literal("stop").executes(context -> {
            stopHunting();
            return SINGLE_SUCCESS;
        }));

        // Argument with autocomplete using the ServerCommand's profile.name() logic
        builder.then(argument("player", StringArgumentType.word())
            .suggests((context, builder1) -> suggestMatching(
                mc.getNetworkHandler().getPlayerList().stream()
                    .map(entry -> entry.getProfile().name()), // FIX: .getName() -> .name()
                builder1
            ))
            .executes(context -> {
                String name = StringArgumentType.getString(context, "player");

                target = null;
                if (mc.world != null) {
                    for (PlayerEntity entity : mc.world.getPlayers()) {
                        // FIX: .getEntityName() -> .getName().getString() 
                        if (entity.getName().getString().equalsIgnoreCase(name)) {
                            target = entity;
                            break;
                        }
                    }
                }

                // Self-hunt protection
                if (target != null && target == mc.player) {
                    ChatUtils.error("You cannot hunt yourself.");
                    return 0;
                }

                if (target == null) {
                    ChatUtils.error("Player not found.");
                    return 0;
                }

                if (!isHunting) {
                    isHunting = true;
                    MeteorClient.EVENT_BUS.subscribe(this);
                    ChatUtils.info("Hunting (highlight)%s(default)...", target.getName().getString());
                }

                return SINGLE_SUCCESS;
            })
        );
    }

    private void stopHunting() {
        if (isHunting) {
            isHunting = false;
            target = null;
            MeteorClient.EVENT_BUS.unsubscribe(this);
            if (mc.player != null) mc.player.setVelocity(0, 0, 0);
            ChatUtils.info("Hunt stopped.");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!isHunting || target == null || mc.player == null) return;

        if (!target.isAlive() || target.isRemoved()) {
            ChatUtils.error("Target lost. Stopping hunt.");
            stopHunting();
            return;
        }

        // Logic from ServerCommand: Manual Vec3d construction to bypass mapping errors
        Vec3d targetPos = new Vec3d(target.getX(), target.getY() + 1.0, target.getZ());
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        Vec3d dir = targetPos.subtract(playerPos).normalize();

        // Superman Velocity
        mc.player.setVelocity(dir.multiply(SPEED));

        // NoFall Packet logic
        PlayerMoveC2SPacket.Full forcedPacket = new PlayerMoveC2SPacket.Full(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ(),
            mc.player.getYaw(),
            mc.player.getPitch(),
            true, 
            mc.player.horizontalCollision
        );
        
        ((IPlayerMoveC2SPacket) forcedPacket).meteor$setTag(1337);
        mc.getNetworkHandler().sendPacket(forcedPacket);
    }
}   