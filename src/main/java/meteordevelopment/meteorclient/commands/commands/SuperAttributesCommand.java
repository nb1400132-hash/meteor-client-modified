package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SuperAttributesCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));
    private static final SimpleCommandExceptionType NOT_HOLDING_ITEM = new SimpleCommandExceptionType(Text.literal("You must hold an item."));

    public SuperAttributesCommand() {
        super("superattributes", "Max out item attributes. Creative only.", "sa", "godattributes", "maxattributes");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("god").executes(context -> {
            ItemStack stack = getStack();
            applyGodAttributes(stack);
            sync();
            info("Applied GOD attributes to item");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("max").executes(context -> {
            ItemStack stack = getStack();
            applyMaxAttributes(stack);
            sync();
            info("Applied MAX attributes (2147483647) to item");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("damage")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.ATTACK_DAMAGE, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set attack damage to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("speed")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.ATTACK_SPEED, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set attack speed to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("health")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.MAX_HEALTH, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set max health bonus to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("armor")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.ARMOR, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set armor to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("toughness")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.ARMOR_TOUGHNESS, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set armor toughness to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("knockback")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.ATTACK_KNOCKBACK, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set knockback to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("movespeed")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 100))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.MOVEMENT_SPEED, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set movement speed bonus to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("reach")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 2147483647))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.ENTITY_INTERACTION_RANGE, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    addAttribute(stack, EntityAttributes.BLOCK_INTERACTION_RANGE, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set reach to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("knockbackres")
            .then(argument("value", DoubleArgumentType.doubleArg(0, 1))
                .executes(context -> {
                    ItemStack stack = getStack();
                    double value = DoubleArgumentType.getDouble(context, "value");
                    addAttribute(stack, EntityAttributes.KNOCKBACK_RESISTANCE, value, EntityAttributeModifier.Operation.ADD_VALUE);
                    sync();
                    info("Set knockback resistance to " + value);
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("clear").executes(context -> {
            ItemStack stack = getStack();
            stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            sync();
            info("Cleared all attributes");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("oneshot").executes(context -> {
            ItemStack stack = getStack();
            applyOneShotAttributes(stack);
            sync();
            info("Applied one-shot kill attributes");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("tank").executes(context -> {
            ItemStack stack = getStack();
            applyTankAttributes(stack);
            sync();
            info("Applied tank/unkillable attributes");
            return SINGLE_SUCCESS;
        }));
    }

    private void applyGodAttributes(ItemStack stack) throws CommandSyntaxException {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        
        addEntry(entries, EntityAttributes.ATTACK_DAMAGE, 1000, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ATTACK_SPEED, 100, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ATTACK_KNOCKBACK, 100, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.MAX_HEALTH, 1000, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ARMOR, 1000, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ARMOR_TOUGHNESS, 1000, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.MOVEMENT_SPEED, 0.5, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ENTITY_INTERACTION_RANGE, 50, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.BLOCK_INTERACTION_RANGE, 50, EntityAttributeModifier.Operation.ADD_VALUE);

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries, true));
        giveItem(stack);
    }

    private void applyMaxAttributes(ItemStack stack) throws CommandSyntaxException {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        
        addEntry(entries, EntityAttributes.ATTACK_DAMAGE, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ATTACK_SPEED, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ATTACK_KNOCKBACK, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.MAX_HEALTH, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ARMOR, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ARMOR_TOUGHNESS, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.MOVEMENT_SPEED, 10, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ENTITY_INTERACTION_RANGE, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.BLOCK_INTERACTION_RANGE, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries, true));
        giveItem(stack);
    }

    private void applyOneShotAttributes(ItemStack stack) throws CommandSyntaxException {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        
        addEntry(entries, EntityAttributes.ATTACK_DAMAGE, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ATTACK_SPEED, 1000, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ATTACK_KNOCKBACK, 1000, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ENTITY_INTERACTION_RANGE, 100, EntityAttributeModifier.Operation.ADD_VALUE);

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries, true));
        giveItem(stack);
    }

    private void applyTankAttributes(ItemStack stack) throws CommandSyntaxException {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        
        addEntry(entries, EntityAttributes.MAX_HEALTH, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ARMOR, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.ARMOR_TOUGHNESS, 2147483647, EntityAttributeModifier.Operation.ADD_VALUE);
        addEntry(entries, EntityAttributes.KNOCKBACK_RESISTANCE, 1, EntityAttributeModifier.Operation.ADD_VALUE);

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries, true));
        giveItem(stack);
    }

    private void addAttribute(ItemStack stack, RegistryEntry<EntityAttribute> attribute, double value, EntityAttributeModifier.Operation operation) throws CommandSyntaxException {
        AttributeModifiersComponent existing = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>(existing.modifiers());
        
        addEntry(entries, attribute, value, operation);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries, true));
        giveItem(stack);
    }

    private void addEntry(List<AttributeModifiersComponent.Entry> entries, RegistryEntry<EntityAttribute> attribute, double value, EntityAttributeModifier.Operation operation) {
        Identifier id = Identifier.of("meteor", "super_" + attribute.getIdAsString().replace(":", "_") + "_" + System.nanoTime());
        EntityAttributeModifier modifier = new EntityAttributeModifier(id, value, operation);
        entries.add(new AttributeModifiersComponent.Entry(attribute, modifier, AttributeModifierSlot.ANY));
    }

    private ItemStack getStack() throws CommandSyntaxException {
        if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) stack = mc.player.getOffHandStack();
        if (stack.isEmpty()) throw NOT_HOLDING_ITEM.create();
        return stack;
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
            36 + mc.player.getInventory().getSelectedSlot(), stack));
    }

    private void sync() {
        mc.setScreen(new InventoryScreen(mc.player));
        mc.setScreen(null);
    }
}
