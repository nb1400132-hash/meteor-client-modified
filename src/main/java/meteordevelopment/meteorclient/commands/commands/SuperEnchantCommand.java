package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.RegistryEntryReferenceArgumentType;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class SuperEnchantCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));
    private static final SimpleCommandExceptionType NOT_HOLDING_ITEM = new SimpleCommandExceptionType(Text.literal("You must hold an item."));

    public SuperEnchantCommand() {
        super("superenchant", "Apply enchantments at any level (up to 32767). Creative only.", "se", "godenchant");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
            .then(argument("enchantment", RegistryEntryReferenceArgumentType.enchantment())
                .then(argument("level", IntegerArgumentType.integer(1, 32767))
                    .executes(context -> {
                        ItemStack stack = getStack();
                        RegistryEntry.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
                        int level = IntegerArgumentType.getInteger(context, "level");
                        Utils.addEnchantment(stack, enchantment, level);
                        sync();
                        info("Added " + enchantment.registryKey().getValue().getPath() + " " + level);
                        return SINGLE_SUCCESS;
                    })
                )
            )
        );

        builder.then(literal("god").executes(context -> {
            ItemStack stack = getStack();
            applyGodEnchants(stack, 255);
            sync();
            info("Applied GOD enchantments (level 255)");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("max").executes(context -> {
            ItemStack stack = getStack();
            applyGodEnchants(stack, 32767);
            sync();
            info("Applied MAX enchantments (level 32767)");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("custom")
            .then(argument("level", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    ItemStack stack = getStack();
                    int level = IntegerArgumentType.getInteger(context, "level");
                    applyGodEnchants(stack, level);
                    sync();
                    info("Applied all enchantments at level " + level);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("sharp")
            .then(argument("level", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    ItemStack stack = getStack();
                    int level = IntegerArgumentType.getInteger(context, "level");
                    applyEnchantById(stack, "sharpness", level);
                    sync();
                    info("Applied Sharpness " + level);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("prot")
            .then(argument("level", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    ItemStack stack = getStack();
                    int level = IntegerArgumentType.getInteger(context, "level");
                    applyEnchantById(stack, "protection", level);
                    sync();
                    info("Applied Protection " + level);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("kb")
            .then(argument("level", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    ItemStack stack = getStack();
                    int level = IntegerArgumentType.getInteger(context, "level");
                    applyEnchantById(stack, "knockback", level);
                    sync();
                    info("Applied Knockback " + level);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("fire")
            .then(argument("level", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    ItemStack stack = getStack();
                    int level = IntegerArgumentType.getInteger(context, "level");
                    applyEnchantById(stack, "fire_aspect", level);
                    sync();
                    info("Applied Fire Aspect " + level);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("looting")
            .then(argument("level", IntegerArgumentType.integer(1, 32767))
                .executes(context -> {
                    ItemStack stack = getStack();
                    int level = IntegerArgumentType.getInteger(context, "level");
                    applyEnchantById(stack, "looting", level);
                    sync();
                    info("Applied Looting " + level);
                    return SINGLE_SUCCESS;
                })
            )
        );
    }

    private void applyGodEnchants(ItemStack stack, int level) {
        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
            registry.streamEntries().forEach(enchantment -> {
                String id = enchantment.registryKey().getValue().toString();
                if (!id.contains("curse") && !id.contains("binding")) {
                    Utils.addEnchantment(stack, enchantment, level);
                }
            });
        });
    }

    private void applyEnchantById(ItemStack stack, String enchantId, int level) {
        mc.getNetworkHandler().getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
            registry.streamEntries().forEach(enchantment -> {
                if (enchantment.registryKey().getValue().getPath().equals(enchantId)) {
                    Utils.addEnchantment(stack, enchantment, level);
                }
            });
        });
    }

    private ItemStack getStack() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) stack = mc.player.getOffHandStack();
        if (stack.isEmpty()) throw NOT_HOLDING_ITEM.create();
        return stack;
    }

    private void sync() {
        mc.setScreen(new InventoryScreen(mc.player));
        mc.setScreen(null);
    }
}
