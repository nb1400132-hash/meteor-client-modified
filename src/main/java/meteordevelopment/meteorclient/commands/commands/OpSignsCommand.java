package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class OpSignsCommand extends Command {

    public OpSignsCommand() {
        super("opsigns", "Spam signs with op commands. Requires OP.", "os", "signspam");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("opall")
            .executes(context -> {
                spawnOpSigns(50, "op @a");
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    int count = IntegerArgumentType.getInteger(context, "count");
                    spawnOpSigns(count, "op @a");
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("deopall")
            .executes(context -> {
                spawnOpSigns(50, "deop @a");
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    int count = IntegerArgumentType.getInteger(context, "count");
                    spawnOpSigns(count, "deop @a");
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("banall")
            .executes(context -> {
                spawnOpSigns(50, "ban @a HACKED");
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    int count = IntegerArgumentType.getInteger(context, "count");
                    spawnOpSigns(count, "ban @a HACKED");
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("kickall")
            .executes(context -> {
                spawnOpSigns(50, "kick @a GET OUT");
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    int count = IntegerArgumentType.getInteger(context, "count");
                    spawnOpSigns(count, "kick @a GET OUT");
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("gamemode")
            .executes(context -> {
                spawnOpSigns(50, "gamemode creative @a");
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    int count = IntegerArgumentType.getInteger(context, "count");
                    spawnOpSigns(count, "gamemode creative @a");
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("custom")
            .then(argument("command", StringArgumentType.greedyString())
                .executes(context -> {
                    String cmd = StringArgumentType.getString(context, "command");
                    spawnOpSigns(50, cmd);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.executes(context -> {
            spawnOpSigns(100, "op @a");
            info("Spawned 100 OP signs around you!");
            return SINGLE_SUCCESS;
        });
    }

    private void spawnOpSigns(int count, String command) {
        if (mc.player == null) return;
        
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 20 - 10);
            int y = (int) (Math.random() * 5);
            int z = (int) (Math.random() * 20 - 10);
            sendCommand("setblock ~" + x + " ~" + y + " ~" + z + " oak_sign[rotation=" + (i % 16) + "]{front_text:{messages:['\"\"','\"[CLICK TO EXECUTE]\"','{\"text\":\"" + command + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/" + command + "\"}}','\"\"']}}");
        }
        info("Spawned " + count + " signs with command: " + command);
    }

    private void sendCommand(String cmd) {
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand(cmd);
        }
    }
}
