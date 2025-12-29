/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.peter;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class ItemFountain extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRandom = settings.createGroup("Randomization");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("speed")
        .description("The delay between drops in ticks.")
        .defaultValue(0)
        .min(0)
        .sliderMax(50)
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("Amount of items to drop per cycle.")
        .defaultValue(1)
        .min(1)
        .sliderMax(5760)
        .build()
    );

    private final Setting<Boolean> randomNames = sgRandom.add(new BoolSetting.Builder()
        .name("random-names")
        .description("Give items random names.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> randomEnchants = sgRandom.add(new BoolSetting.Builder()
        .name("random-enchants")
        .description("Give items random enchantments.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> randomModels = sgRandom.add(new BoolSetting.Builder()
        .name("random-models")
        .description("Give items random visual models.")
        .defaultValue(false)
        .build()
    );

    private int timer;
    private final Random random = Random.create();
    private final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789☀☁☂☃☄★☆☇☈☉☊☋☌☍☎☏☐☑☒☓☔☕☖☗☘☙☚☛☜☝☞☟☠☡☢☣☤☥☦☧☨☩☪☫☬☭☮☯☰☱☲☳☴☵☶☷☸☹☺";

    public ItemFountain() {
        super(Categories.Peter, "item-fountain", "Generates and drops random items in Creative mode.");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (!mc.player.getAbilities().creativeMode) {
            error("You must be in Creative mode to use this.");
            toggle();
            return;
        }

        if (timer <= 0) {
            dropRandomItem();
            timer = delay.get();
        } else {
            timer--;
        }
    }

    private void dropRandomItem() {
        int itemsLeft = amount.get();
        var enchantmentRegistry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        while (itemsLeft > 0) {
            var randomEntry = Registries.ITEM.getRandom(random);
            if (randomEntry.isEmpty()) return;

            Item item = randomEntry.get().value();
            if (item == Items.AIR) continue;

            int stackSize = Math.min(itemsLeft, 64);
            itemsLeft -= stackSize;

            ItemStack stack = new ItemStack(item, stackSize);

            if (randomNames.get()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 10; i++) {
                    sb.append(chars.charAt(random.nextInt(chars.length())));
                }
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(sb.toString()));
            }

            if (randomEnchants.get()) {
                ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                
                int enchantCount = random.nextInt(5) + 1;
                for(int i = 0; i < enchantCount; i++) {
                    enchantmentRegistry.getRandom(random).ifPresent(entry -> {
                        builder.add(entry, random.nextInt(255) + 1);
                    });
                }
                stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
            }

            if (randomModels.get()) {
                Registries.ITEM.getRandom(random).ifPresent(entry -> {
                    Identifier id = Registries.ITEM.getId(entry.value());
                    stack.set(DataComponentTypes.ITEM_MODEL, id);
                });
            }

            int slot = 36 + mc.player.getInventory().getSelectedSlot();
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, stack));
            mc.player.dropSelectedItem(true);
        }
    }
}