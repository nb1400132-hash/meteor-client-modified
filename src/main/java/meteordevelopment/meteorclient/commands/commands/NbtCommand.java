package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DataResult;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;

public class NbtCommand extends Command {

    private static boolean devMode = false;

    private static final DynamicCommandExceptionType MALFORMED_ITEM_EXCEPTION =
        new DynamicCommandExceptionType(
            error -> Text.stringifiedTranslatable("arguments.item.malformed", error)
        );

    public NbtCommand() {
        super("nbt", "Modifies NBT data for an item.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        builder.then(literal("copy").executes(context -> {
            if (mc.player == null) return SINGLE_SUCCESS;
            ItemStack stack = mc.player.getInventory().getSelectedStack();

            if (stack.isEmpty()) {
                safeError("Hold an item.");
                return SINGLE_SUCCESS;
            }

            var registryOps = RegistryOps.of(NbtOps.INSTANCE, mc.world.getRegistryManager());
            var result = ItemStack.CODEC.encode(stack, registryOps, new NbtCompound());

            if (result.result().isEmpty()) {
                String msg = "Failed to encode item NBT.";
                safeError(msg);
                copyErrorToClipboard(msg);
                return SINGLE_SUCCESS;
            }

            NbtCompound nbt = (NbtCompound) result.result().get();

            copyNbtToClipboard(nbt.toString());
            safeInfo("NBT data copied to clipboard.");

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add")
            .then(argument("tag", StringArgumentType.word())
                .executes(context -> {
                    String tag = StringArgumentType.getString(context, "tag");
                    ItemStack stack = mc.player.getInventory().getSelectedStack();

                    if (!validBasic(stack)) return 0;

                    try {
                        NbtCompound componentsNbt = new NbtCompound();

                        switch (tag) {
                            case "unbreakable" -> componentsNbt.put("minecraft:unbreakable", new NbtCompound());
                            case "consumable" -> {
                                NbtCompound consumable = new NbtCompound();
                                consumable.put("on_consume_effects", new NbtList());
                                componentsNbt.put("minecraft:consumable", consumable);
                            }
                            default -> throw new CommandSyntaxException(null, Text.literal("Support for tag '" + tag + "' not added yet."));
                        }

                        var registryOps = RegistryOps.of(NbtOps.INSTANCE, mc.world.getRegistryManager());
                        DataResult<ComponentMap> parseResult =
                            ComponentMap.CODEC.parse(registryOps, componentsNbt);

                        ComponentMap newComponents = parseResult.getOrThrow(errorMsg ->
                            new CommandSyntaxException(
                                null, Text.literal("Failed to parse components NBT: " + errorMsg)
                            )
                        );

                        DataResult<Unit> validation =
                            ItemStack.validateComponents(newComponents);
                        validation.getOrThrow(MALFORMED_ITEM_EXCEPTION::create);

                        stack.applyComponentsFrom(newComponents);
                        setStack(stack);

                        safeInfo("Added tag: " + tag);

                    } catch (Throwable e) {
                        String msg = "Failed to add tag: " + e.getMessage();
                        safeError(msg);
                        copyErrorToClipboard(msg);
                        return 0;
                    }

                    return 1;
                })
            )
        );

        builder.then(literal("setitemmodel")
            .then(argument("model", StringArgumentType.word())
                .executes(context -> {
                    String model = StringArgumentType.getString(context, "model");
                    ItemStack stack = mc.player.getInventory().getSelectedStack();

                    if (!validBasic(stack)) return SINGLE_SUCCESS;

                    try {
                        NbtCompound componentsNbt = new NbtCompound();

                        if (!"null".equalsIgnoreCase(model)) {
                            componentsNbt.putString("minecraft:item_model", "minecraft:" + model);
                        }

                        var registryOps = RegistryOps.of(NbtOps.INSTANCE, mc.world.getRegistryManager());
                        DataResult<ComponentMap> parseResult =
                            ComponentMap.CODEC.parse(registryOps, componentsNbt);

                        ComponentMap parsedComponents = parseResult.getOrThrow(
                            errorMsg ->
                                new CommandSyntaxException(
                                    null,
                                    Text.literal("Failed to parse components NBT: " + errorMsg)
                                )
                        );

                        ItemStack.validateComponents(parsedComponents)
                            .getOrThrow(MALFORMED_ITEM_EXCEPTION::create);

                        stack.applyComponentsFrom(parsedComponents);
                        setStack(stack);

                        safeInfo("Item model set to: " + model);

                    } catch (Throwable e) {
                        String msg = "Failed to apply item model: " + e.getMessage();
                        safeError(msg);
                        copyErrorToClipboard(msg);
                    }

                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("count")
            .then(argument("count", IntegerArgumentType.integer(1, 9999))
                .executes(context -> {
                    ItemStack stack = mc.player.getInventory().getSelectedStack();

                    if (validBasic(stack)) {
                        int count = IntegerArgumentType.getInteger(context, "count");
                        stack.setCount(count);

                        NbtCompound components = new NbtCompound();
                        components.put("minecraft:max_stack_size", NbtInt.of(count));

                        var registryOps = RegistryOps.of(NbtOps.INSTANCE, mc.world.getRegistryManager());
                        ComponentMap compMap =
                            ComponentMap.CODEC.parse(registryOps, components)
                                .getOrThrow(msg -> new CommandSyntaxException(null, Text.literal(msg)));

                        stack.applyComponentsFrom(compMap);
                        setStack(stack);

                        safeInfo("Count and max_stack_size set to " + count + ".");
                    }

                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("load").executes(context -> {
            if (!validCreative()) return SINGLE_SUCCESS;
            NbtCompound root = parseClipboard();
            if (root == null) return SINGLE_SUCCESS;

            try {
                var registryOps = RegistryOps.of(NbtOps.INSTANCE, mc.world.getRegistryManager());
                var result = ItemStack.CODEC.parse(registryOps, root);

                if (result.result().isPresent()) {
                    ItemStack newStack = result.result().get();
                    setStack(newStack);
                    safeInfo("Item loaded and replaced.");
                } else {
                    if (root.contains("id")) {
                        String id = root.getString("id").orElse("minecraft:air");
                        net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(id));
                        
                        ItemStack manualStack = new ItemStack(item);
                        manualStack.setCount(root.getInt("count").orElse(1));
                        
                        if (root.contains("components")) {
                            var compResult = ComponentMap.CODEC.parse(registryOps, root.get("components"));
                            compResult.result().ifPresent(manualStack::applyComponentsFrom);
                        }
                        
                        setStack(manualStack);
                        safeInfo("Item loaded (Lenient Mode).");
                    } else {
                        String error = result.error().map(e -> e.message()).orElse("Missing 'id'");
                        safeError("Cannot create item: " + error);
                    }
                }
            } catch (Throwable e) {
                String msg = "Failed to load NBT: " + e.getMessage();
                safeError(msg);
                copyErrorToClipboard(msg);
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("paste").executes(context -> {
            ItemStack stack = mc.player.getInventory().getSelectedStack();
            if (!validBasic(stack)) return SINGLE_SUCCESS;
            
            NbtCompound root = parseClipboard();
            if (root == null) return SINGLE_SUCCESS;

            try {
                if (!root.contains("components")) {
                    safeError("'components' tag missing.");
                    return SINGLE_SUCCESS;
                }

                var registryOps = RegistryOps.of(NbtOps.INSTANCE, mc.world.getRegistryManager());
                var result = ComponentMap.CODEC.parse(registryOps, root.get("components"));
                
                if (result.result().isPresent()) {
                        ComponentMap newComponents = result.result().get();
                        try {
                            ItemStack.validateComponents(newComponents).getOrThrow(MALFORMED_ITEM_EXCEPTION::create);
                            stack.applyComponentsFrom(newComponents);
                            setStack(stack);
                            safeInfo("NBT pasted onto held item.");
                        } catch (Exception e) {
                            safeError("Invalid components: " + e.getMessage());
                        }
                } else {
                    String error = result.error().map(e -> e.message()).orElse("Unknown parse error");
                    safeError("Failed to parse components: " + error);
                }
            } catch (Throwable e) {
                String msg = "Failed to paste NBT: " + e.getMessage();
                safeError(msg);
                copyErrorToClipboard(msg);
            }
            return SINGLE_SUCCESS;
        }));
    }

    private NbtCompound parseClipboard() {
        String clipboardContent = mc.keyboard.getClipboard();
        if (clipboardContent == null || clipboardContent.isEmpty()) {
            String msg = "Clipboard is empty.";
            safeError(msg);
            return null;
        }

        NbtCompound root = null;
        
        try {
            String cleanContent = clipboardContent.replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(cleanContent);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(decoded)) {
                root = NbtIo.readCompressed(bais, NbtSizeTracker.ofUnlimitedBytes());
            }
        } catch (Exception ignored) {}

        if (root == null) {
            try {
                String sanitized = clipboardContent.replaceAll("(?<=[\\s\\:\\[\\,])(-?Infinityd)(?=[\\s\\,\\}\\]])", "\"$1\"");
                root = NbtHelper.fromNbtProviderString(sanitized);
            } catch (Exception e) {
                safeError("Failed to parse NBT string: " + e.getMessage());
                return null;
            }
        }

        fixInfinity(root);

        if (root.contains("item") && !root.contains("id")) {
            root.putString("id", root.getString("item").orElse(""));
        }
        
        return root;
    }

    private NbtElement fixInfinity(NbtElement element) {
        if (element instanceof NbtCompound compound) {
            for (String key : new ArrayList<>(compound.getKeys())) {
                compound.put(key, fixInfinity(compound.get(key)));
            }
            return compound;
        } else if (element instanceof NbtList list) {
            for (int i = 0; i < list.size(); i++) {
                list.set(i, fixInfinity(list.get(i)));
            }
            return list;
        } else if (element instanceof NbtString str) {
            String val = str.toString(); 
            
            if ("Infinityd".equals(val) || "\"Infinityd\"".equals(val)) {
                return NbtDouble.of(Double.POSITIVE_INFINITY);
            }
            if ("-Infinityd".equals(val) || "\"-Infinityd\"".equals(val)) {
                return NbtDouble.of(Double.NEGATIVE_INFINITY);
            }
        }
        return element;
    }

    private void setStack(ItemStack stack) {
        if (mc.player != null && mc.player.getAbilities().creativeMode) {
            mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
            mc.player.networkHandler.sendPacket(
                new CreativeInventoryActionC2SPacket(
                    36 + mc.player.getInventory().getSelectedSlot(), stack
                )
            );
        }
    }

    private boolean validCreative() {
        if (mc.player == null) {
            safeError("Player unavailable.");
            return false;
        }
        if (!mc.player.getAbilities().creativeMode) {
            safeError("Creative only.");
            return false;
        }
        return true;
    }

    private boolean validBasic(ItemStack stack) {
        if (!validCreative()) return false;
        if (stack == null || stack.isEmpty()) {
            safeError("Hold an item.");
            return false;
        }
        return true;
    }

    private void safeInfo(String msg) {
        try { info(msg); } catch (Throwable ignored) {}
    }

    private void safeError(String msg) {
        try { error(msg); } catch (Throwable ignored) {}
    }

    private void copyNbtToClipboard(String text) {
        try {
            mc.keyboard.setClipboard(text);
        } catch (Throwable ignored) {}
    }

    private void copyErrorToClipboard(String text) {
        if (!devMode) return;
        try {
            mc.keyboard.setClipboard(text);
        } catch (Throwable ignored) {}
    }
}