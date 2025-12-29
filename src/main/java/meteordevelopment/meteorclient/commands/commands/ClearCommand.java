package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

public class ClearCommand extends Command {
    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode to use this."));

    public ClearCommand() {
        super("clear", "Clears your inventory.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();

            for (int i = 0; i < 46; i++) {
                mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(i, ItemStack.EMPTY));
            }

            mc.player.getInventory().clear();
            
            info("Inventory cleared.");
            return SINGLE_SUCCESS;
        });
    }
}