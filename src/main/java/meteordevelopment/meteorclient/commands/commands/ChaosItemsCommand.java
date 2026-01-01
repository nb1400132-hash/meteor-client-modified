package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChaosItemsCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));

    public ChaosItemsCommand() {
        super("chaositems", "Gives chaos/griefing items. Creative only.", "ci", "grief");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("deathsword").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveDeathSword();
            info("Gave Death Sword - instant kill on hit");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("cursepotion").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveCursePotion();
            info("Gave Curse Potion - all negative effects max level");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("instakillbow").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveInstakillBow();
            info("Gave Instakill Bow - one shot anything");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("crashegg").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveCrashEgg();
            info("Gave Crash Egg - spawns recursive entities");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("lagtnt").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveLagTnt();
            info("Gave Lag TNT - causes massive lag when ignited");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("knockbackstick").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveKnockbackStick();
            info("Gave Knockback Stick - yeets players to the moon");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("voidtrident").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveVoidTrident();
            info("Gave Void Trident - sends targets to the void");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("32kweapon").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            give32kWeapon();
            info("Gave 32k Weapon - classic sharpness 32767");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("lightningrod").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveLightningRod();
            info("Gave Lightning Rod Trident - channeling on every hit");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("all").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            int slot = 0;
            slot = giveItemToSlot(slot, createDeathSword());
            slot = giveItemToSlot(slot, createCursePotion());
            slot = giveItemToSlot(slot, createInstakillBow());
            slot = giveItemToSlot(slot, createKnockbackStick());
            slot = giveItemToSlot(slot, createVoidTrident());
            slot = giveItemToSlot(slot, create32kWeapon());
            slot = giveItemToSlot(slot, createLightningRod());
            info("Gave all chaos items!");
            return SINGLE_SUCCESS;
        }));
    }

    private void giveDeathSword() {
        giveItem(createDeathSword());
    }

    private ItemStack createDeathSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 2147483647);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 1000);
        addAttribute(entries, EntityAttributes.ATTACK_KNOCKBACK, 100);
        addAttribute(entries, EntityAttributes.ENTITY_INTERACTION_RANGE, 50);
        sword.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        sword.set(DataComponentTypes.ITEM_NAME, Text.literal("DEATH SWORD").styled(s -> s.withColor(Formatting.DARK_RED).withBold(true).withObfuscated(false)));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return sword;
    }

    private void giveCursePotion() {
        giveItem(createCursePotion());
    }

    private ItemStack createCursePotion() {
        ItemStack potion = new ItemStack(Items.SPLASH_POTION);
        List<StatusEffectInstance> effects = new ArrayList<>();
        effects.add(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 125));
        effects.add(new StatusEffectInstance(StatusEffects.WITHER, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.POISON, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.SLOWNESS, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.BLINDNESS, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.HUNGER, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.WEAKNESS, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.NAUSEA, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.LEVITATION, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.GLOWING, 999999, 125));
        effects.add(new StatusEffectInstance(StatusEffects.DARKNESS, 999999, 125));
        potion.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(0x000000), effects, Optional.empty()));
        potion.set(DataComponentTypes.ITEM_NAME, Text.literal("CURSE POTION").styled(s -> s.withColor(Formatting.DARK_PURPLE).withBold(true)));
        return potion;
    }

    private void giveInstakillBow() {
        giveItem(createInstakillBow());
    }

    private ItemStack createInstakillBow() {
        ItemStack bow = new ItemStack(Items.BOW);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 2147483647);
        bow.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        bow.set(DataComponentTypes.ITEM_NAME, Text.literal("INSTAKILL BOW").styled(s -> s.withColor(Formatting.RED).withBold(true)));
        bow.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return bow;
    }

    private void giveCrashEgg() {
        ItemStack egg = new ItemStack(Items.BAT_SPAWN_EGG, 64);
        egg.set(DataComponentTypes.ITEM_NAME, Text.literal("CRASH EGG").styled(s -> s.withColor(Formatting.DARK_RED).withBold(true)));
        giveItem(egg);
    }

    private void giveLagTnt() {
        ItemStack tnt = new ItemStack(Items.TNT, 64);
        tnt.set(DataComponentTypes.ITEM_NAME, Text.literal("LAG TNT").styled(s -> s.withColor(Formatting.RED).withBold(true)));
        giveItem(tnt);
    }

    private void giveKnockbackStick() {
        giveItem(createKnockbackStick());
    }

    private ItemStack createKnockbackStick() {
        ItemStack stick = new ItemStack(Items.STICK);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_KNOCKBACK, 2147483647);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 1000);
        addAttribute(entries, EntityAttributes.ENTITY_INTERACTION_RANGE, 50);
        stick.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        stick.set(DataComponentTypes.ITEM_NAME, Text.literal("YEET STICK").styled(s -> s.withColor(Formatting.AQUA).withBold(true)));
        stick.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stick;
    }

    private void giveVoidTrident() {
        giveItem(createVoidTrident());
    }

    private ItemStack createVoidTrident() {
        ItemStack trident = new ItemStack(Items.TRIDENT);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 2147483647);
        addAttribute(entries, EntityAttributes.ATTACK_KNOCKBACK, 2147483647);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 1000);
        trident.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        trident.set(DataComponentTypes.ITEM_NAME, Text.literal("VOID TRIDENT").styled(s -> s.withColor(Formatting.DARK_PURPLE).withBold(true)));
        trident.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return trident;
    }

    private void give32kWeapon() {
        giveItem(create32kWeapon());
    }

    private ItemStack create32kWeapon() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 32767);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 100);
        sword.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        sword.set(DataComponentTypes.ITEM_NAME, Text.literal("32K SWORD").styled(s -> s.withColor(Formatting.GOLD).withBold(true)));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return sword;
    }

    private void giveLightningRod() {
        giveItem(createLightningRod());
    }

    private ItemStack createLightningRod() {
        ItemStack trident = new ItemStack(Items.TRIDENT);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>();
        addAttribute(entries, EntityAttributes.ATTACK_DAMAGE, 1000);
        addAttribute(entries, EntityAttributes.ATTACK_SPEED, 100);
        trident.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(entries));
        trident.set(DataComponentTypes.ITEM_NAME, Text.literal("LIGHTNING ROD").styled(s -> s.withColor(Formatting.YELLOW).withBold(true)));
        trident.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return trident;
    }

    private void addAttribute(List<AttributeModifiersComponent.Entry> entries, RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, double value) {
        Identifier id = Identifier.of("meteor", "chaos_" + System.nanoTime());
        EntityAttributeModifier modifier = new EntityAttributeModifier(id, value, EntityAttributeModifier.Operation.ADD_VALUE);
        entries.add(new AttributeModifiersComponent.Entry(attribute, modifier, AttributeModifierSlot.ANY));
    }

    private int giveItemToSlot(int slot, ItemStack stack) {
        if (slot >= 36) return slot;
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, stack));
        return slot + 1;
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + mc.player.getInventory().getSelectedSlot(), stack));
    }
}
