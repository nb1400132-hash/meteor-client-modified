package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeathBookCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode."));

    public DeathBookCommand() {
        super("deathbook", "Gives a death/crash book. Creative only.", "db", "crashbook");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("lag").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveLagBook();
            info("Gave LAG book - opening causes client lag");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("crash").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveCrashBook();
            info("Gave CRASH book - may crash weak clients");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("spam").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveSpamBook();
            info("Gave SPAM book - fills chat with garbage");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("obfuscated").executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveObfuscatedBook();
            info("Gave OBFUSCATED book - seizure warning");
            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            if (!mc.player.isCreative()) throw NOT_IN_CREATIVE.create();
            giveCrashBook();
            info("Gave CRASH book");
            return SINGLE_SUCCESS;
        });
    }

    private void giveLagBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        String longText = "§k" + "A".repeat(32000);
        for (int i = 0; i < 100; i++) {
            pages.add(RawFilteredPair.of(Text.literal(longText)));
        }
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
            RawFilteredPair.of("LAG BOOK"),
            "VECTOR",
            0,
            pages,
            true
        ));
        book.set(DataComponentTypes.ITEM_NAME, Text.literal("LAG BOOK").styled(s -> s.withColor(Formatting.DARK_RED).withBold(true).withObfuscated(true)));
        giveItem(book);
    }

    private void giveCrashBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        StringBuilder massiveText = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            massiveText.append("§k§l§m§n§o");
        }
        String crashText = massiveText.toString();
        for (int i = 0; i < 100; i++) {
            pages.add(RawFilteredPair.of(Text.literal(crashText)));
        }
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
            RawFilteredPair.of("CRASH BOOK"),
            "VECTOR",
            0,
            pages,
            true
        ));
        book.set(DataComponentTypes.ITEM_NAME, Text.literal("CRASH BOOK").styled(s -> s.withColor(Formatting.DARK_RED).withBold(true)));
        giveItem(book);
    }

    private void giveSpamBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        String spamText = "GET REKT BY VECTOR ".repeat(500);
        for (int i = 0; i < 100; i++) {
            pages.add(RawFilteredPair.of(Text.literal(spamText)));
        }
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
            RawFilteredPair.of("SPAM BOOK"),
            "VECTOR",
            0,
            pages,
            true
        ));
        book.set(DataComponentTypes.ITEM_NAME, Text.literal("SPAM BOOK").styled(s -> s.withColor(Formatting.RED).withBold(true)));
        giveItem(book);
    }

    private void giveObfuscatedBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        String obfText = "§k§4§l" + "DEATH".repeat(1000);
        for (int i = 0; i < 100; i++) {
            pages.add(RawFilteredPair.of(Text.literal(obfText)));
        }
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
            RawFilteredPair.of("§k§4DEATH"),
            "§k§4VECTOR",
            0,
            pages,
            true
        ));
        book.set(DataComponentTypes.ITEM_NAME, Text.literal("SEIZURE BOOK").styled(s -> s.withColor(Formatting.DARK_RED).withBold(true).withObfuscated(true)));
        giveItem(book);
    }

    private void giveItem(ItemStack stack) {
        mc.player.getInventory().setStack(mc.player.getInventory().getSelectedSlot(), stack);
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + mc.player.getInventory().getSelectedSlot(), stack));
    }
}
