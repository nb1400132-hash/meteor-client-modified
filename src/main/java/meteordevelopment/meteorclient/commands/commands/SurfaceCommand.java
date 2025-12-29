package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SurfaceCommand extends Command {
    private static final int MAX_SEARCH_RANGE = 256;
    private static final int REQUIRED_AIR_BLOCKS_ABOVE_SURFACE = 2; // Reduced to 2 as 3 is often too strict for caves

    public SurfaceCommand() {
        super("surface", "Teleports you to the first solid block above using VClip logic.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ClientPlayerEntity player = mc.player;
            if (player == null) return 0;
            
            World world = mc.world;
            if (world == null) return 0;

            BlockPos playerPos = player.getBlockPos();
            BlockPos targetSurfaceBlock = null;

            int worldTopY = world.getDimension().logicalHeight();

            // 1. Find the surface block
            BlockPos.Mutable currentSearchPos = new BlockPos.Mutable();
            for (int yOffset = 1; yOffset <= MAX_SEARCH_RANGE; yOffset++) {
                int potentialSurfaceY = playerPos.getY() + yOffset;
                
                if (potentialSurfaceY >= (worldTopY - REQUIRED_AIR_BLOCKS_ABOVE_SURFACE)) break;

                currentSearchPos.set(playerPos.getX(), potentialSurfaceY, playerPos.getZ());
                BlockState blockState = world.getBlockState(currentSearchPos);

                // If block is solid
                if (!blockState.getCollisionShape(world, currentSearchPos).isEmpty()) {
                    boolean hasEnoughClearance = true;
                    
                    // Check for air above it
                    for (int i = 1; i <= REQUIRED_AIR_BLOCKS_ABOVE_SURFACE; i++) {
                        BlockPos clearancePos = currentSearchPos.up(i);
                        if (!world.getBlockState(clearancePos).getCollisionShape(world, clearancePos).isEmpty()) {
                            hasEnoughClearance = false;
                            break;
                        }
                    }

                    if (hasEnoughClearance) {
                        targetSurfaceBlock = currentSearchPos.toImmutable();
                        break;
                    }
                }
            }

            if (targetSurfaceBlock != null) {
                double targetX = targetSurfaceBlock.getX() + 0.5;
                double targetY = targetSurfaceBlock.getY() + 1.0; // Stand ON the block
                double targetZ = targetSurfaceBlock.getZ() + 0.5;

                // 2. Calculate VClip distance
                double currentY = player.getY();
                double distance = targetY - currentY;

                // 3. Apply VClip Packet Logic
                // Paper/Spigot allows ~10 blocks per move packet
                int packetsRequired = (int) Math.ceil(Math.abs(distance / 10));

                if (packetsRequired > 20) {
                    // Limit to prevent instant kicks for massive jumps (>200 blocks)
                    packetsRequired = 1;
                }

                if (player.hasVehicle()) {
                    // Vehicle Logic
                    for (int i = 0; i < (packetsRequired - 1); i++) {
                        player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(player.getVehicle()));
                    }
                    player.getVehicle().setPosition(targetX, targetY, targetZ);
                    player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(player.getVehicle()));
                } else {
                    // Player Logic
                    // Send 'packetsRequired' empty move packets to simulate travel time
                    for (int i = 0; i < (packetsRequired - 1); i++) {
                        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, player.horizontalCollision));
                    }
                    // Send final teleport packet
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY, targetZ, true, player.horizontalCollision));
                    player.setPosition(targetX, targetY, targetZ);
                }

                ChatUtils.info("VClip to surface: %.1f blocks up.", distance);
            } else {
                ChatUtils.error("No surface found within %d blocks.", MAX_SEARCH_RANGE);
            }

            return SINGLE_SUCCESS;
        });
    }
}