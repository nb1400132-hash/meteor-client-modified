package meteordevelopment.meteorclient.systems.modules.peter;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class TrueSight extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> allEntities = sgGeneral.add(new BoolSetting.Builder()
        .name("all-entities")
        .description("Shows all invisible entities, including items and mobs.")
        .defaultValue(true)
        .build()
    );

    public TrueSight() {
        super(Categories.Peter, "true-sight", "Forces invisible entities to render with their skins/textures.");
    }

    public boolean shouldOverride(Entity entity) {
        if (!isActive()) return false;
        return allEntities.get() || entity instanceof PlayerEntity;
    }
}