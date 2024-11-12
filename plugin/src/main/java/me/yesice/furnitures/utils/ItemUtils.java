package me.yesice.furnitures.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;

public class ItemUtils {

    public static ItemStack getGlass() {
        return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static ItemStack getPreviousPage() {
        return ItemBuilder.from(Material.GLASS_PANE)
                .name(text("§7ᴘᴀɢɪɴᴀ ᴘʀᴇᴄᴇᴅᴇɴᴛᴇ"))
                .model(48)
                .build();
    }

    public static ItemStack getNextPage() {
        return ItemBuilder.from(Material.GLASS_PANE)
                .name(text("§7ᴘᴀɢɪɴᴀ ѕᴜᴄᴄᴇѕѕɪᴠᴀ"))
                .model(49)
                .build();
    }

    public static ItemStack getClose() {
        return ItemBuilder.from(Material.GLASS_PANE)
                .name(text("§cᴄʜɪᴜᴅɪ"))
                .model(52)
                .build();
    }

    public static ItemStack getHome() {
        return ItemBuilder.from(Material.GLASS_PANE)
                .name(text("§aᴛᴏʀɴᴀ ᴀʟʟᴀ ʜᴏᴍᴇ"))
                .model(65)
                .build();
    }

    public static void dropItem(Location location, ItemStack item) {
        World world = location.getWorld();

        if (world != null)
            world.dropItemNaturally(location, item);
    }
}
