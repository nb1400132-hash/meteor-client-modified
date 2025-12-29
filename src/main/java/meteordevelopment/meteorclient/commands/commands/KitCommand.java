package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.Resource;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class KitCommand extends Command {
    public static final Path KITS_DIR = Paths.get(MeteorClient.FOLDER.toString(), "kits");

    public KitCommand() {
        super("kit", "Opens a custom kit GUI.");
        try {
            if (!Files.exists(KITS_DIR)) {
                Files.createDirectories(KITS_DIR);
            }
            kitAddDefault(0, "fair", "fair.json", Items.LIGHT_BLUE_SHULKER_BOX);
            kitAddDefault(1, "legit", "legit.json", Items.RED_SHULKER_BOX);
            kitAddDefault(2, "unfair", "unfairpvp.json", Items.BLUE_SHULKER_BOX);
            kitAddDefault(3, "grief", "grief.json", Items.YELLOW_SHULKER_BOX);
        } catch (IOException e) {
            MeteorClient.LOG.error("Failed to create kits folder", e);
        }
    }

    private void kitAddDefault(int slotIndex, String kitName, String internalFile, net.minecraft.item.Item displayItem) {
        Path destination = KITS_DIR.resolve("slot" + (slotIndex + 1) + ".nbt");

        if (!Files.exists(destination)) {
            Identifier resourceId = Identifier.of("meteor-client", "kits/" + internalFile);
            String nbtString = loadNbtFromResource(resourceId);
            
            if (nbtString != null) {
                try {
                    NbtCompound nbt = NbtHelper.fromNbtProviderString(nbtString);
                    
                    if (!nbt.contains("components")) {
                        NbtCompound wrapper = new NbtCompound();
                        wrapper.put("components", nbt);
                        nbt = wrapper;
                    }
                    
                    nbt.putString("item", net.minecraft.registry.Registries.ITEM.getId(displayItem).toString());
                    if (!nbt.contains("count")) {
                        nbt.putInt("count", 1);
                    }

                    try (OutputStream os = Files.newOutputStream(destination)) {
                        NbtIo.writeCompressed(nbt, os);
                    }
                    MeteorClient.LOG.info("Extracted default kit " + kitName + " to slot " + (slotIndex + 1));
                } catch (Exception e) {
                    MeteorClient.LOG.error("Failed to write default kit: " + kitName, e);
                }
            }
        }
    }

    private String loadNbtFromResource(Identifier resourceId) {
        if (mc.getResourceManager() == null) return null;
        Optional<Resource> resourceOptional = mc.getResourceManager().getResource(resourceId);

        if (resourceOptional.isPresent()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceOptional.get().getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player == null || !mc.player.getAbilities().creativeMode) {
                error("You must be in Creative mode to use this.");
                return SINGLE_SUCCESS;
            }
            
            mc.send(() -> mc.setScreen(new KitScreen(mc.player.getInventory())));
            return SINGLE_SUCCESS;
        });
    }

    private static class KitScreen extends GenericContainerScreen {
        private int currentPage = 0;
        private final int MAX_PAGES = 300;
        private final int KITS_PER_PAGE = 45;

        public KitScreen(net.minecraft.entity.player.PlayerInventory playerInventory) {
            super(new KitScreenHandler(playerInventory), playerInventory, Text.literal("Kit Selector"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            super.render(context, mouseX, mouseY, delta);
            this.drawMouseoverTooltip(context, mouseX, mouseY);
        }

        private void reloadPage() {
            if (this.handler instanceof KitScreenHandler kitHandler) {
                kitHandler.loadKits(currentPage);
            }
        }

        @Override
        protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
            KitScreenHandler handler = (KitScreenHandler) this.handler;
            Inventory kitInv = handler.getInventory();
            MinecraftClient client = MinecraftClient.getInstance();

            // Navigation and Button Protection
            if (slot != null && slot.inventory == kitInv) {
                int index = slot.getIndex();
                
                // Previous Page (Slot 45)
                if (index == 45) {
                    if (currentPage > 0) {
                        currentPage--;
                        reloadPage();
                    }
                    return; // Cancel interaction
                }
                
                // Next Page (Slot 53)
                if (index == 53) {
                    if (currentPage < MAX_PAGES - 1) {
                        currentPage++;
                        reloadPage();
                    }
                    return; // Cancel interaction
                }
                
                // Protect the entire 6th row (45-53) from modification
                if (index >= 45 && index <= 53) {
                    return;
                }
            }

            if (slot == null) {
                if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.PICKUP_ALL) {
                    ItemStack cursor = handler.getCursorStack();
                    if (!cursor.isEmpty()) {
                        client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(-1, cursor));
                        handler.setCursorStack(ItemStack.EMPTY);
                    }
                }
                if (actionType == SlotActionType.QUICK_CRAFT) {
                    onDragEnd(button, handler, kitInv, client);
                }
                return;
            }

            if (actionType == SlotActionType.PICKUP) {
                ItemStack cursorStack = handler.getCursorStack();
                ItemStack slotStack = slot.getStack();
                boolean slotChanged = false;

                if (button == 0) {
                    if (cursorStack.isEmpty()) {
                        if (!slotStack.isEmpty()) {
                            handler.setCursorStack(slotStack);
                            slot.setStack(ItemStack.EMPTY);
                            slotChanged = true;
                        }
                    } else {
                        if (slotStack.isEmpty()) {
                            slot.setStack(cursorStack);
                            handler.setCursorStack(ItemStack.EMPTY);
                            slotChanged = true;
                        } else if (ItemStack.areItemsAndComponentsEqual(cursorStack, slotStack)) {
                            int available = slotStack.getMaxCount() - slotStack.getCount();
                            int toMove = Math.min(cursorStack.getCount(), available);
                            if (toMove > 0) {
                                slotStack.increment(toMove);
                                cursorStack.decrement(toMove);
                                slotChanged = true;
                            }
                        } else {
                            handler.setCursorStack(slotStack);
                            slot.setStack(cursorStack);
                            slotChanged = true;
                        }
                    }
                } else if (button == 1) {
                    if (cursorStack.isEmpty()) {
                        if (!slotStack.isEmpty()) {
                            int amount = (int) Math.ceil(slotStack.getCount() / 2.0);
                            ItemStack newCursor = slotStack.copy();
                            newCursor.setCount(amount);
                            handler.setCursorStack(newCursor);
                            slotStack.decrement(amount);
                            slotChanged = true;
                        }
                    } else {
                        if (slotStack.isEmpty()) {
                            ItemStack newSlot = cursorStack.copy();
                            newSlot.setCount(1);
                            slot.setStack(newSlot);
                            cursorStack.decrement(1);
                            slotChanged = true;
                        } else if (ItemStack.areItemsAndComponentsEqual(cursorStack, slotStack)) {
                            if (slotStack.getCount() < slotStack.getMaxCount()) {
                                slotStack.increment(1);
                                cursorStack.decrement(1);
                                slotChanged = true;
                            }
                        } else {
                            handler.setCursorStack(slotStack);
                            slot.setStack(cursorStack);
                            slotChanged = true;
                        }
                    }
                }

                if (slotChanged) {
                    if (slot.inventory == kitInv) {
                        int index = slot.getIndex();
                        // Only save if it's in the kit area (0-44)
                        if (index < KITS_PER_PAGE) {
                            int absoluteIndex = (currentPage * KITS_PER_PAGE) + index;
                            saveSlot(absoluteIndex, slot.getStack());
                        }
                    } else {
                        int creativeSlot = getCreativeSlotId(slot);
                        if (creativeSlot != -1) {
                            client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(creativeSlot, slot.getStack()));
                        }
                    }
                }
            } else if (actionType == SlotActionType.QUICK_MOVE) {
                if (slot.inventory == kitInv) {
                    ItemStack toMove = slot.getStack().copy();
                    if (!toMove.isEmpty()) {
                        List<ItemStack> beforeStacks = new ArrayList<>();
                        for (int i = 54; i < 90; i++) {
                            beforeStacks.add(handler.getSlot(i).getStack().copy());
                        }

                        if (handler.insertItemPublic(toMove, 54, 90, true)) {
                            slot.setStack(ItemStack.EMPTY);
                            
                            int index = slot.getIndex();
                            if (index < KITS_PER_PAGE) {
                                saveSlot((currentPage * KITS_PER_PAGE) + index, ItemStack.EMPTY);
                            }

                            for (int i = 54; i < 90; i++) {
                                ItemStack after = handler.getSlot(i).getStack();
                                if (!ItemStack.areEqual(beforeStacks.get(i - 54), after)) {
                                    int creativeSlot = getCreativeSlotId(handler.getSlot(i));
                                    if (creativeSlot != -1) {
                                        client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(creativeSlot, after));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ItemStack toMove = slot.getStack().copy();
                    if (!toMove.isEmpty()) {
                        Slot sourceSlot = slot;
                        
                        // RESTRICTED: Only try to insert into first 45 slots (0 to KITS_PER_PAGE - 1)
                        // Do not attempt to insert into button slots (45-53)
                        for (int i = 0; i < KITS_PER_PAGE; i++) {
                            ItemStack target = kitInv.getStack(i);
                            if (target.isEmpty()) {
                                kitInv.setStack(i, toMove);
                                sourceSlot.setStack(ItemStack.EMPTY); 
                                
                                saveSlot((currentPage * KITS_PER_PAGE) + i, toMove);
                                
                                int creativeSlot = getCreativeSlotId(sourceSlot);
                                if (creativeSlot != -1) {
                                    client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(creativeSlot, ItemStack.EMPTY));
                                }
                                break;
                            }
                        }
                    }
                }
            } else if (actionType == SlotActionType.CLONE && slot.hasStack()) {
                ItemStack stack = slot.getStack().copy();
                stack.setCount(stack.getMaxCount());
                handler.setCursorStack(stack);
            }
        }

        private void onDragEnd(int button, KitScreenHandler handler, Inventory kitInv, MinecraftClient client) {
            ItemStack cursorStack = handler.getCursorStack();
            if (cursorStack.isEmpty()) return;

            if (button != 2 && button != 6) return;

            Set<Slot> dragSlots = this.cursorDragSlots;
            if (dragSlots.isEmpty()) return;

            int count = cursorStack.getCount();
            int numSlots = dragSlots.size();

            if (button == 2) {
                int perSlot = count / numSlots;
                int remainder = count % numSlots;
                
                if (perSlot == 0 && remainder == 0) return;

                for (Slot s : dragSlots) {
                    if (s.canInsert(cursorStack)) {
                        // Prevent dragging into button area
                        if (s.inventory == kitInv && s.getIndex() >= KITS_PER_PAGE) continue;

                        ItemStack toPlace = cursorStack.copy();
                        toPlace.setCount(perSlot);
                        
                        ItemStack current = s.getStack();
                        if (current.isEmpty()) {
                            s.setStack(toPlace);
                        } else if (ItemStack.areItemsAndComponentsEqual(current, toPlace)) {
                            current.increment(perSlot);
                        }
                        
                        if (s.inventory == kitInv) {
                            int index = s.getIndex();
                            if (index < KITS_PER_PAGE) {
                                int absoluteIndex = (currentPage * KITS_PER_PAGE) + index;
                                saveSlot(absoluteIndex, s.getStack());
                            }
                        } else {
                            int creativeSlot = getCreativeSlotId(s);
                            if (creativeSlot != -1) {
                                client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(creativeSlot, s.getStack()));
                            }
                        }
                    }
                }
                cursorStack.setCount(remainder);
            } else {
                for (Slot s : dragSlots) {
                    if (count <= 0) break;
                    if (s.canInsert(cursorStack)) {
                        // Prevent dragging into button area
                        if (s.inventory == kitInv && s.getIndex() >= KITS_PER_PAGE) continue;

                        ItemStack current = s.getStack();
                        if (current.isEmpty()) {
                            ItemStack toPlace = cursorStack.copy();
                            toPlace.setCount(1);
                            s.setStack(toPlace);
                            count--;
                        } else if (ItemStack.areItemsAndComponentsEqual(current, cursorStack)) {
                            if (current.getCount() < current.getMaxCount()) {
                                current.increment(1);
                                count--;
                            }
                        }
                        
                        if (s.inventory == kitInv) {
                            int index = s.getIndex();
                            if (index < KITS_PER_PAGE) {
                                int absoluteIndex = (currentPage * KITS_PER_PAGE) + index;
                                saveSlot(absoluteIndex, s.getStack());
                            }
                        } else {
                            int creativeSlot = getCreativeSlotId(s);
                            if (creativeSlot != -1) {
                                client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(creativeSlot, s.getStack()));
                            }
                        }
                    }
                }
                cursorStack.setCount(count);
            }
            handler.setCursorStack(cursorStack);
        }

        private int getCreativeSlotId(Slot slot) {
            if (!(slot.inventory instanceof net.minecraft.entity.player.PlayerInventory)) return -1;
            int index = slot.getIndex();
            if (index >= 0 && index < 9) return 36 + index; 
            if (index >= 9 && index < 36) return index;     
            if (index >= 36 && index < 40) return 5 + (index - 36); 
            if (index == 40) return 45; 
            return -1;
        }
    }

    private static class KitScreenHandler extends GenericContainerScreenHandler {
        private final int KITS_PER_PAGE = 45;

        public KitScreenHandler(net.minecraft.entity.player.PlayerInventory playerInventory) {
            super(ScreenHandlerType.GENERIC_9X6, 0, playerInventory, new SimpleInventory(54), 6);
            loadKits(0);
        }

        public boolean insertItemPublic(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
            return this.insertItem(stack, startIndex, endIndex, fromLast);
        }

        public void loadKits(int page) {
            Inventory inv = this.getInventory();
            inv.clear();

            for (int i = 0; i < KITS_PER_PAGE; i++) {
                int fileIndex = (page * KITS_PER_PAGE) + i + 1;
                
                Path binaryFile = KITS_DIR.resolve("slot" + fileIndex + ".nbt");
                
                try {
                    NbtCompound nbt = null;

                    if (Files.exists(binaryFile)) {
                        try (InputStream is = Files.newInputStream(binaryFile)) {
                            nbt = NbtIo.readCompressed(is, NbtSizeTracker.ofUnlimitedBytes());
                        }
                    }
                    
                    if (nbt != null) {
                        String itemId = nbt.getString("item").orElse("minecraft:air");
                        
                        if (!itemId.equals("minecraft:air")) {
                             net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(itemId));
                             ItemStack stack = new ItemStack(item);
                                 
                             if (nbt.contains("components")) {
                                 var ops = RegistryOps.of(NbtOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager());
                                 var result = ComponentMap.CODEC.parse(ops, nbt.get("components"));
                                 result.result().ifPresent(stack::applyComponentsFrom);
                             }
                                 
                             stack.setCount(nbt.getInt("count").orElse(1));
                             inv.setStack(i, stack);
                        }
                    }

                } catch (Exception e) {
                    MeteorClient.LOG.error("Failed to load kit slot " + fileIndex, e);
                }
            }

            // Original Buttons (Arrow, Paper, Arrow)
            ItemStack prev = new ItemStack(Items.ARROW);
            prev.set(DataComponentTypes.ITEM_NAME, Text.literal("Previous Page").styled(style -> style.withColor(net.minecraft.util.Formatting.RED)));
            
            ItemStack next = new ItemStack(Items.ARROW);
            next.set(DataComponentTypes.ITEM_NAME, Text.literal("Next Page").styled(style -> style.withColor(net.minecraft.util.Formatting.GREEN)));
            
            ItemStack filler = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            filler.set(DataComponentTypes.ITEM_NAME, Text.empty());
            
            ItemStack info = new ItemStack(Items.PAPER);
            info.set(DataComponentTypes.ITEM_NAME, Text.literal("Page " + (page + 1) + "/300").styled(style -> style.withColor(net.minecraft.util.Formatting.YELLOW)));

            // Slot 45: Prev
            if (page > 0) inv.setStack(45, prev);
            else inv.setStack(45, filler);

            // Slots 46-52: Filler
            for (int i = 46; i <= 52; i++) {
                inv.setStack(i, filler);
            }
            
            // Slot 49: Info (Overwrite filler)
            inv.setStack(49, info);

            // Slot 53: Next
            if (page < 299) inv.setStack(53, next);
            else inv.setStack(53, filler);
        }

        @Override
        public boolean canUse(net.minecraft.entity.player.PlayerEntity player) {
            return true;
        }
    }

    private static void saveSlot(int absoluteIndex, ItemStack stack) {
        Path file = KITS_DIR.resolve("slot" + (absoluteIndex + 1) + ".nbt");
        try {
            if (stack.isEmpty()) {
                Files.deleteIfExists(file);
                return;
            }

            NbtCompound root = new NbtCompound();
            root.putString("item", net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString());
            root.putInt("count", stack.getCount());

            var ops = RegistryOps.of(NbtOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager());
            var result = ComponentMap.CODEC.encodeStart(ops, stack.getComponents());
            result.result().ifPresent(elem -> root.put("components", elem));

            try (OutputStream os = Files.newOutputStream(file)) {
                NbtIo.writeCompressed(root, os);
            }

        } catch (Exception e) {
            MeteorClient.LOG.error("Failed to save kit slot " + (absoluteIndex + 1), e);
        }
    }
}