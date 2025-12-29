package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.block.entity.BannerPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BannerCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));
    private static final Random random = new Random();

    private static final String[] PATTERN_NAMES = {
        "base", "stripe_bottom", "stripe_top", "stripe_left", "stripe_right",
        "stripe_center", "stripe_middle", "stripe_downright", "stripe_downleft",
        "small_stripes", "cross", "straight_cross", "diagonal_left", "diagonal_right",
        "diagonal_up_left", "diagonal_up_right", "half_vertical", "half_vertical_right",
        "half_horizontal", "half_horizontal_bottom", "square_bottom_left", "square_bottom_right",
        "square_top_left", "square_top_right", "triangle_bottom", "triangle_top",
        "triangles_bottom", "triangles_top", "circle", "rhombus", "border",
        "curly_border", "bricks", "gradient", "gradient_up", "creeper", "skull",
        "flower", "mojang", "globe", "piglin", "flow", "guster"
    };

    public BannerCommand() {
        super("banner", "Create custom banners with patterns. Creative only.", "bannercreate", "bc");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("random")
            .executes(context -> {
                giveRandomBanner(6);
                return SINGLE_SUCCESS;
            })
            .then(argument("layers", IntegerArgumentType.integer(1, 16))
                .executes(context -> {
                    int layers = IntegerArgumentType.getInteger(context, "layers");
                    giveRandomBanner(layers);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("maxlayers").executes(context -> {
            giveRandomBanner(16);
            info("Created banner with 16 layers");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("illegal")
            .executes(context -> {
                giveIllegalBanner(64);
                return SINGLE_SUCCESS;
            })
            .then(argument("layers", IntegerArgumentType.integer(1, 1000))
                .executes(context -> {
                    int layers = IntegerArgumentType.getInteger(context, "layers");
                    giveIllegalBanner(layers);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("presets")
            .then(literal("rainbow").executes(context -> {
                giveRainbowBanner();
                info("Created rainbow banner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("creeper").executes(context -> {
                giveCreeperBanner();
                info("Created creeper face banner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("skull").executes(context -> {
                giveSkullBanner();
                info("Created skull banner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("gradient").executes(context -> {
                giveGradientBanner();
                info("Created gradient banner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("earth").executes(context -> {
                giveEarthBanner();
                info("Created earth/globe banner");
                return SINGLE_SUCCESS;
            }))
            .then(literal("ww2").executes(context -> {
                giveWW2Banner();
                info("Created WW2 banner for trolling");
                return SINGLE_SUCCESS;
            }))
        );

        builder.then(literal("solid")
            .then(argument("color", StringArgumentType.word())
                .executes(context -> {
                    String color = StringArgumentType.getString(context, "color");
                    giveSolidBanner(color);
                    return SINGLE_SUCCESS;
                })
            )
        );
    }

    private void giveRandomBanner(int layers) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        DyeColor[] colors = DyeColor.values();
        ItemStack banner = new ItemStack(Items.WHITE_BANNER);

        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            for (int i = 0; i < layers; i++) {
                String patternName = PATTERN_NAMES[random.nextInt(PATTERN_NAMES.length)];
                DyeColor color = colors[random.nextInt(colors.length)];

                registry.streamEntries().forEach(pattern -> {
                    if (pattern.registryKey().getValue().getPath().equals(patternName)) {
                        patternLayers.add(new BannerPatternsComponent.Layer(pattern, color));
                    }
                });
            }
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
        info("Created random banner with " + layers + " layers");
    }

    private void giveIllegalBanner(int layers) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        DyeColor[] colors = DyeColor.values();
        ItemStack banner = new ItemStack(Items.BLACK_BANNER);

        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            List<RegistryEntry.Reference<BannerPattern>> allPatterns = registry.streamEntries().toList();
            for (int i = 0; i < layers; i++) {
                RegistryEntry.Reference<BannerPattern> pattern = allPatterns.get(random.nextInt(allPatterns.size()));
                DyeColor color = colors[random.nextInt(colors.length)];
                patternLayers.add(new BannerPatternsComponent.Layer(pattern, color));
            }
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
        info("Created illegal banner with " + layers + " layers");
    }

    private void giveRainbowBanner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack banner = new ItemStack(Items.WHITE_BANNER);
        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            registry.streamEntries().forEach(pattern -> {
                if (pattern.registryKey().getValue().getPath().equals("stripe_top")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.RED));
                } else if (pattern.registryKey().getValue().getPath().equals("stripe_center")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.ORANGE));
                } else if (pattern.registryKey().getValue().getPath().equals("stripe_middle")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.YELLOW));
                } else if (pattern.registryKey().getValue().getPath().equals("stripe_bottom")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.LIME));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                if (pattern.registryKey().getValue().getPath().equals("gradient")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLUE));
                }
            });
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
    }

    private void giveCreeperBanner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack banner = new ItemStack(Items.LIME_BANNER);
        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            registry.streamEntries().forEach(pattern -> {
                if (pattern.registryKey().getValue().getPath().equals("creeper")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLACK));
                }
            });
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
    }

    private void giveSkullBanner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack banner = new ItemStack(Items.BLACK_BANNER);
        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            registry.streamEntries().forEach(pattern -> {
                if (pattern.registryKey().getValue().getPath().equals("skull")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.WHITE));
                }
            });
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
    }

    private void giveGradientBanner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack banner = new ItemStack(Items.BLACK_BANNER);
        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("gradient")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.PURPLE));
                } else if (name.equals("gradient_up")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.MAGENTA));
                }
            });
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
    }

    private void giveEarthBanner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack banner = new ItemStack(Items.BLUE_BANNER);
        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            registry.streamEntries().forEach(pattern -> {
                if (pattern.registryKey().getValue().getPath().equals("globe")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.GREEN));
                }
            });
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
    }

    private void giveWW2Banner() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        ItemStack banner = new ItemStack(Items.RED_BANNER);
        List<BannerPatternsComponent.Layer> patternLayers = new ArrayList<>();

        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).ifPresent(registry -> {
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("circle")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.WHITE));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("stripe_center")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLACK));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("stripe_middle")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLACK));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("square_top_left")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLACK));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("square_bottom_right")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLACK));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("stripe_top")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.RED));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("stripe_bottom")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.RED));
                }
            });
            registry.streamEntries().forEach(pattern -> {
                String name = pattern.registryKey().getValue().getPath();
                if (name.equals("border")) {
                    patternLayers.add(new BannerPatternsComponent.Layer(pattern, DyeColor.BLACK));
                }
            });
        });

        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(patternLayers));
        giveItem(banner);
    }

    private void giveSolidBanner(String colorName) throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();

        DyeColor color;
        try {
            color = DyeColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            error("Invalid color: " + colorName);
            return;
        }

        ItemStack banner = new ItemStack(getBannerByColor(color));
        giveItem(banner);
        info("Created " + colorName + " banner");
    }

    private net.minecraft.item.Item getBannerByColor(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_BANNER;
            case ORANGE -> Items.ORANGE_BANNER;
            case MAGENTA -> Items.MAGENTA_BANNER;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_BANNER;
            case YELLOW -> Items.YELLOW_BANNER;
            case LIME -> Items.LIME_BANNER;
            case PINK -> Items.PINK_BANNER;
            case GRAY -> Items.GRAY_BANNER;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_BANNER;
            case CYAN -> Items.CYAN_BANNER;
            case PURPLE -> Items.PURPLE_BANNER;
            case BLUE -> Items.BLUE_BANNER;
            case BROWN -> Items.BROWN_BANNER;
            case GREEN -> Items.GREEN_BANNER;
            case RED -> Items.RED_BANNER;
            case BLACK -> Items.BLACK_BANNER;
        };
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
            36 + mc.player.getInventory().getSelectedSlot(), stack));
    }
}
