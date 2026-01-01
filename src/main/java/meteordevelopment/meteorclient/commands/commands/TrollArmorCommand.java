package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TrollArmorCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));

    public TrollArmorCommand() {
        super("trollarmor", "Gives OP armor sets. Creative only.", "ta", "godarmor");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("god").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveGodArmor();
            info("Gave GOD armor set - immortality achieved");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("speed").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveSpeedArmor();
            info("Gave SPEED armor set - gotta go fast");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("tank").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveTankArmor();
            info("Gave TANK armor set - unkillable");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("vampire").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveVampireArmor();
            info("Gave VAMPIRE armor set - drain your enemies");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("berserker").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveBerserkerArmor();
            info("Gave BERSERKER armor set - pure damage");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("ninja").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveNinjaArmor();
            info("Gave NINJA armor set - fast and deadly");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("all").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveGodArmor();
            info("Gave GOD armor set");
            return SINGLE_SUCCESS;
        }));
    }

    private void giveGodArmor() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);

        applyGodAttributes(helmet, "GOD HELMET", Formatting.GOLD);
        applyGodAttributes(chestplate, "GOD CHESTPLATE", Formatting.GOLD);
        applyGodAttributes(leggings, "GOD LEGGINGS", Formatting.GOLD);
        applyGodAttributes(boots, "GOD BOOTS", Formatting.GOLD);

        giveArmorSet(helmet, chestplate, leggings, boots);
    }

    private void giveSpeedArmor() {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        int cyan = 0x00FFFF;
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cyan));
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cyan));
        leggings.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cyan));
        boots.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cyan));

        applySpeedAttributes(helmet, "SPEED HELMET", Formatting.AQUA);
        applySpeedAttributes(chestplate, "SPEED CHESTPLATE", Formatting.AQUA);
        applySpeedAttributes(leggings, "SPEED LEGGINGS", Formatting.AQUA);
        applySpeedAttributes(boots, "SPEED BOOTS", Formatting.AQUA);

        giveArmorSet(helmet, chestplate, leggings, boots);
    }

    private void giveTankArmor() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);

        applyTankAttributes(helmet, "TANK HELMET", Formatting.DARK_GRAY);
        applyTankAttributes(chestplate, "TANK CHESTPLATE", Formatting.DARK_GRAY);
        applyTankAttributes(leggings, "TANK LEGGINGS", Formatting.DARK_GRAY);
        applyTankAttributes(boots, "TANK BOOTS", Formatting.DARK_GRAY);

        giveArmorSet(helmet, chestplate, leggings, boots);
    }

    private void giveVampireArmor() {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        int red = 0x8B0000;
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(red));
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(red));
        leggings.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(red));
        boots.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(red));

        applyVampireAttributes(helmet, "VAMPIRE HELMET", Formatting.DARK_RED);
        applyVampireAttributes(chestplate, "VAMPIRE CHESTPLATE", Formatting.DARK_RED);
        applyVampireAttributes(leggings, "VAMPIRE LEGGINGS", Formatting.DARK_RED);
        applyVampireAttributes(boots, "VAMPIRE BOOTS", Formatting.DARK_RED);

        giveArmorSet(helmet, chestplate, leggings, boots);
    }

    private void giveBerserkerArmor() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);

        applyBerserkerAttributes(helmet, "BERSERKER HELMET", Formatting.RED);
        applyBerserkerAttributes(chestplate, "BERSERKER CHESTPLATE", Formatting.RED);
        applyBerserkerAttributes(leggings, "BERSERKER LEGGINGS", Formatting.RED);
        applyBerserkerAttributes(boots, "BERSERKER BOOTS", Formatting.RED);

        giveArmorSet(helmet, chestplate, leggings, boots);
    }

    private void giveNinjaArmor() {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        int black = 0x1a1a1a;
        helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(black));
        chestplate.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(black));
        leggings.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(black));
        boots.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(black));

        applyNinjaAttributes(helmet, "NINJA HELMET", Formatting.DARK_PURPLE);
        applyNinjaAttributes(chestplate, "NINJA CHESTPLATE", Formatting.DARK_PURPLE);
        applyNinjaAttributes(leggings, "NINJA LEGGINGS", Formatting.DARK_PURPLE);
        applyNinjaAttributes(boots, "NINJA BOOTS", Formatting.DARK_PURPLE);

        giveArmorSet(helmet, chestplate, leggings, boots);
    }

    private void applyGodAttributes(ItemStack stack, String name, Formatting color) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.MAX_HEALTH, 500);
        addAttribute(entries, EntityAttributes.ARMOR, 500);
        addAttribute(entries, EntityAttributes.ARMOR_TOUGHNESS, 500);
        addAttribute(entries, EntityAttributes.KNOCKBACK_RESISTANCE, 0.25);
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 100);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 25);
        addAttribute(entries, EntityAttributes.MOVEMENT_SPEED, 0.05);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private void applySpeedAttributes(ItemStack stack, String name, Formatting color) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.MOVEMENT_SPEED, 0.5);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 100);
        addAttribute(entries, EntityAttributes.ARMOR, 50);
        addAttribute(entries, EntityAttributes.MAX_HEALTH, 50);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private void applyTankAttributes(ItemStack stack, String name, Formatting color) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.MAX_HEALTH, 2000);
        addAttribute(entries, EntityAttributes.ARMOR, 1000);
        addAttribute(entries, EntityAttributes.ARMOR_TOUGHNESS, 1000);
        addAttribute(entries, EntityAttributes.KNOCKBACK_RESISTANCE, 0.25);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private void applyVampireAttributes(ItemStack stack, String name, Formatting color) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 200);
        addAttribute(entries, EntityAttributes.MAX_HEALTH, 200);
        addAttribute(entries, EntityAttributes.ARMOR, 100);
        addAttribute(entries, EntityAttributes.MOVEMENT_SPEED, 0.1);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private void applyBerserkerAttributes(ItemStack stack, String name, Formatting color) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 500);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 50);
        addAttribute(entries, EntityAttributes.ATTACK_KNOCKBACK, 50);
        addAttribute(entries, EntityAttributes.MOVEMENT_SPEED, 0.1);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private void applyNinjaAttributes(ItemStack stack, String name, Formatting color) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.MOVEMENT_SPEED, 0.3);
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 300);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 100);
        addAttribute(entries, EntityAttributes.ARMOR, 20);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(name).styled(s -> s.withColor(color).withBold(true)));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private void addAttribute(List<AttributeModifiersComponent.Entry> entries, RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, double value) {
        Identifier id = Identifier.of("meteor", "troll_" + System.nanoTime());
        EntityAttributeModifier modifier = new EntityAttributeModifier(id, value, EntityAttributeModifier.Operation.ADD_VALUE);
        entries.add(new AttributeModifiersComponent.Entry(attribute, modifier, AttributeModifierSlot.ANY));
    }

    private void giveArmorSet(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(5, helmet));
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(6, chestplate));
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(7, leggings));
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(8, boots));
    }
}
