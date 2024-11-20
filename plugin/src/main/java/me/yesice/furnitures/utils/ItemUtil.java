package me.yesice.furnitures.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;

public class ItemUtil {

    public static GuiItem getGlass() {
        return ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(text("§r"))
                .asGuiItem();
    }

    public static GuiItem getPreviousPage(PaginatedGui gui) {
        return ItemBuilder.from(Material.PAPER)
                .name(text("§7" + Util.toSmallText("pagina precedente")))
                .model(12)
                .asGuiItem(event -> gui.previous());
    }

    public static GuiItem getNextPage(PaginatedGui gui) {
        return ItemBuilder.from(Material.PAPER)
                .name(text("§7" + Util.toSmallText("pagina successiva")))
                .model(13)
                .asGuiItem(event -> gui.next());
    }

    public static GuiItem getClose(BaseGui gui, Player player) {
        return ItemBuilder.from(Material.PAPER)
                .name(text("§c" + Util.toSmallText("chiudi")))
                .model(18)
                .asGuiItem(event -> gui.close(player));
    }

    public static ItemStack getBack() {
        return ItemBuilder.from(Material.PAPER)
                .name(text("§c" + Util.toSmallText("torna indietro")))
                .model(45)
                .build();
    }

    public static void dropItem(Location location, ItemStack item) {
        World world = location.getWorld();

        if (world != null)
            world.dropItemNaturally(location, item);
    }
}
