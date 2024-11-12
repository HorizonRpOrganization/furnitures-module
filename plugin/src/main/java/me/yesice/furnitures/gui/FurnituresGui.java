package me.yesice.furnitures.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class FurnituresGui {

    public void openCategories(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Categorie Furnitures"))
                .rows(4)
                .pageSize(27)
                .create();

        gui.setItem(27, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(28, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(29, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(30, ItemBuilder.from(ItemUtils.getPreviousPage()).asGuiItem(event -> gui.previous()));
        gui.setItem(31, ItemBuilder.from(ItemUtils.getClose()).asGuiItem(event -> gui.close(player)));
        gui.setItem(32, ItemBuilder.from(ItemUtils.getNextPage()).asGuiItem(event -> gui.next()));
        gui.setItem(33, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(34, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(35, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());

        List<File> categories = Furnitures.getInstance().getDirectoryManager().getCategories();
        for (File category : categories) {
            List<Furniture> furnitures = Furnitures.getInstance().getFurnituresManager().getFurnituresOfCategory(category.getName());

            ItemStack item = new ItemStack(Material.CONDUIT);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aCategoria #" + category.getName());
            meta.setLore(List.of("§f", "§fFurniture disponibili: §7" + furnitures.size(), "§f", "§eᴄʟɪᴄᴄᴀ ᴘᴇʀ ᴀᴘʀɪʀᴇ ʟᴀ ᴄᴀᴛᴇɢᴏʀɪᴀ"));
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, category.getName());
            item.setItemMeta(meta);

            gui.addItem(ItemBuilder.from(item).asGuiItem());
        }

        gui.open(player);
    }

    public void openFurnitures(Player player, String category) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Categoria #" + category))
                .rows(4)
                .pageSize(27)
                .create();

        gui.setItem(27, ItemBuilder.from(ItemUtils.getHome()).asGuiItem(event -> openCategories(player)));
        gui.setItem(28, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(29, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(30, ItemBuilder.from(ItemUtils.getPreviousPage()).asGuiItem(event -> gui.previous()));
        gui.setItem(31, ItemBuilder.from(ItemUtils.getClose()).asGuiItem(event -> gui.close(player)));
        gui.setItem(32, ItemBuilder.from(ItemUtils.getNextPage()).asGuiItem(event -> gui.next()));
        gui.setItem(33, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(34, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(35, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());

        List<Furniture> furnitures = Furnitures.getInstance().getFurnituresManager().getFurnituresOfCategory(category);
        for (Furniture furniture : furnitures) {
            ItemStack item = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(category, furniture.id());
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            meta.setLore(List.of("§f", "§fModel Data: §7" + meta.getCustomModelData(), "§fID: §7" + furniture.id(), "§f", "§eᴄʟɪᴄᴄᴀ ᴘᴇʀ ᴏᴛᴛᴇɴᴇʀᴇ"));

            /*
            for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
                meta.getPersistentDataContainer().remove(key);
            }
             */

            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());

            item.setItemMeta(meta);

            gui.addItem(ItemBuilder.from(item).asGuiItem());
        }

        gui.open(player);
    }

    public void openSearch(Player player, String key, List<Furniture> furnitures) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Risultati Ricerca: " + key))
                .rows(4)
                .pageSize(27)
                .create();

        gui.setItem(27, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(28, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(29, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(30, ItemBuilder.from(ItemUtils.getPreviousPage()).asGuiItem(event -> gui.previous()));
        gui.setItem(31, ItemBuilder.from(ItemUtils.getClose()).asGuiItem(event -> gui.close(player)));
        gui.setItem(32, ItemBuilder.from(ItemUtils.getNextPage()).asGuiItem(event -> gui.next()));
        gui.setItem(33, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(34, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());
        gui.setItem(35, ItemBuilder.from(ItemUtils.getGlass()).asGuiItem());

        for (Furniture furniture : furnitures) {
            ItemStack item = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(furniture.category(), furniture.id());
            ItemMeta meta = item.getItemMeta();
            meta.setLore(List.of("§f", "§fModel Data: §7" + meta.getCustomModelData(), "§fID: §7" + furniture.id(), "§f", "§eᴄʟɪᴄᴄᴀ ᴘᴇʀ ᴏᴛᴛᴇɴᴇʀᴇ"));
            for (NamespacedKey namespacedKey : meta.getPersistentDataContainer().getKeys()) {
                meta.getPersistentDataContainer().remove(namespacedKey);
            }
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
            item.setItemMeta(meta);

            gui.addItem(ItemBuilder.from(item).asGuiItem());
        }

        gui.open(player);
    }
}
