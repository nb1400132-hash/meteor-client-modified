package meteordevelopment.meteorclient.systems.modules.peter;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;

import java.util.UUID;

public class GamemodeAlerts extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> creative = sgGeneral.add(new BoolSetting.Builder().name("creative").defaultValue(true).build());
    private final Setting<Boolean> spectator = sgGeneral.add(new BoolSetting.Builder().name("spectator").defaultValue(true).build());
    private final Setting<Boolean> survival = sgGeneral.add(new BoolSetting.Builder().name("survival").defaultValue(false).build());

    public GamemodeAlerts() {
        super(Categories.Peter, "gamemode-alerts", "Alerts when players change gamemodes.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof PlayerListS2CPacket packet)) return;
        if (!packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_GAME_MODE)) return;

        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            GameMode mode = entry.gameMode();
            if (mode == null) continue;

            String name = getPlayerName(entry.profileId());
            if (name == null || (mc.player != null && name.equals(mc.player.getGameProfile().name()))) continue;

            if (mode == GameMode.CREATIVE && creative.get()) {
                info("(highlight)%s(default) is now in (yellow)Creative(default)!", name);
            } 
            else if (mode == GameMode.SPECTATOR && spectator.get()) {
                info("(highlight)%s(default) is now in (gray)Spectator(default)!", name);
            } 
            else if (mode == GameMode.SURVIVAL && survival.get()) {
                info("(highlight)%s(default) returned to (green)Survival(default).", name);
            }
        }
    }

    private String getPlayerName(UUID uuid) {
        if (mc.getNetworkHandler() == null) return null;
        var entry = mc.getNetworkHandler().getPlayerListEntry(uuid);
        return (entry != null) ? entry.getProfile().name() : null;
    }
}