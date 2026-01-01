package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class SummonChaosCommand extends Command {

    public SummonChaosCommand() {
        super("summonchaos", "Summon chaos entities via commands. Creative/OP only.", "sc", "chaos");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("witherstorm")
            .executes(context -> {
                summonWitherStorm(10);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 100))
                .executes(context -> {
                    summonWitherStorm(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("giants")
            .executes(context -> {
                summonGiants(10);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 100))
                .executes(context -> {
                    summonGiants(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("dragons")
            .executes(context -> {
                summonDragons(5);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 50))
                .executes(context -> {
                    summonDragons(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("lightning")
            .executes(context -> {
                summonLightning(50);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    summonLightning(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("tnt")
            .executes(context -> {
                summonTnt(100);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 1000))
                .executes(context -> {
                    summonTnt(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("creepers")
            .executes(context -> {
                summonChargedCreepers(20);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 200))
                .executes(context -> {
                    summonChargedCreepers(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("wardens")
            .executes(context -> {
                summonWardens(5);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 50))
                .executes(context -> {
                    summonWardens(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("fireballs")
            .executes(context -> {
                summonFireballs(20);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 200))
                .executes(context -> {
                    summonFireballs(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("anvils")
            .executes(context -> {
                summonFallingAnvils(50);
                return SINGLE_SUCCESS;
            })
            .then(argument("count", IntegerArgumentType.integer(1, 500))
                .executes(context -> {
                    summonFallingAnvils(IntegerArgumentType.getInteger(context, "count"));
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("killall").executes(context -> {
            sendCommand("kill @e[type=!player]");
            info("Killed all non-player entities");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("apocalypse").executes(context -> {
            summonWitherStorm(5);
            summonDragons(3);
            summonWardens(10);
            summonChargedCreepers(50);
            summonLightning(100);
            info("APOCALYPSE UNLEASHED!");
            return SINGLE_SUCCESS;
        }));
    }

    private void summonWitherStorm(int count) {
        for (int i = 0; i < count; i++) {
            sendCommand("summon minecraft:wither ~ ~" + (5 + i * 3) + " ~ {CustomName:'\"WITHER STORM\"',CustomNameVisible:1b,Attributes:[{Name:\"generic.max_health\",Base:100000}],Health:100000f}");
        }
        info("Summoned " + count + " super withers!");
    }

    private void summonGiants(int count) {
        for (int i = 0; i < count; i++) {
            sendCommand("summon minecraft:giant ~ ~2 ~" + (i * 5) + " {CustomName:'\"TITAN\"',Attributes:[{Name:\"generic.max_health\",Base:10000},{Name:\"generic.attack_damage\",Base:500}],Health:10000f}");
        }
        info("Summoned " + count + " giants!");
    }

    private void summonDragons(int count) {
        for (int i = 0; i < count; i++) {
            sendCommand("summon minecraft:ender_dragon ~ ~" + (20 + i * 10) + " ~ {DragonPhase:0}");
        }
        info("Summoned " + count + " ender dragons!");
    }

    private void summonLightning(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 40 - 20);
            int z = (int) (Math.random() * 40 - 20);
            sendCommand("summon minecraft:lightning_bolt ~" + x + " ~ ~" + z);
        }
        info("Summoned " + count + " lightning bolts!");
    }

    private void summonTnt(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 20 - 10);
            int y = (int) (Math.random() * 10 + 5);
            int z = (int) (Math.random() * 20 - 10);
            sendCommand("summon minecraft:tnt ~" + x + " ~" + y + " ~" + z + " {fuse:80}");
        }
        info("Summoned " + count + " TNT!");
    }

    private void summonChargedCreepers(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 30 - 15);
            int z = (int) (Math.random() * 30 - 15);
            sendCommand("summon minecraft:creeper ~" + x + " ~ ~" + z + " {powered:1b,ExplosionRadius:10,Fuse:20,ignited:1b}");
        }
        info("Summoned " + count + " charged creepers!");
    }

    private void summonWardens(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 20 - 10);
            int z = (int) (Math.random() * 20 - 10);
            sendCommand("summon minecraft:warden ~" + x + " ~ ~" + z + " {Attributes:[{Name:\"generic.max_health\",Base:50000},{Name:\"generic.attack_damage\",Base:1000}],Health:50000f}");
        }
        info("Summoned " + count + " super wardens!");
    }

    private void summonFireballs(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 40 - 20);
            int z = (int) (Math.random() * 40 - 20);
            sendCommand("summon minecraft:fireball ~" + x + " ~20 ~" + z + " {ExplosionPower:10,power:[0.0,-0.5,0.0]}");
        }
        info("Summoned " + count + " fireballs!");
    }

    private void summonFallingAnvils(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * 30 - 15);
            int z = (int) (Math.random() * 30 - 15);
            sendCommand("summon minecraft:falling_block ~" + x + " ~30 ~" + z + " {BlockState:{Name:\"minecraft:anvil\"},Time:1,HurtEntities:1b,FallHurtMax:1000,FallHurtAmount:100}");
        }
        info("Summoned " + count + " falling anvils!");
    }

    private void sendCommand(String cmd) {
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand(cmd);
        }
    }
}
