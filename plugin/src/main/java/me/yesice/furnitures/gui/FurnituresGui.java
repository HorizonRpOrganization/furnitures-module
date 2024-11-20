package me.yesice.furnitures.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.events.FurnitureGetEvent;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class FurnituresGui {

    public void openCategories(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Categorie Furnitures"))
                .rows(4)
                .pageSize(27)
                .disableAllInteractions()
                .create();

        int[] indexes = new int[]{27, 28, 29, 33, 34, 35};

        for (int index : indexes)
            gui.setItem(index, ItemUtil.getGlass());

        gui.setItem(30, ItemUtil.getPreviousPage(gui));
        gui.setItem(31, ItemUtil.getClose(gui, player));
        gui.setItem(32, ItemUtil.getNextPage(gui));

        List<String> categories = Furnitures.getInstance().getFurnituresManager().getCategories();
        for (String category : categories) {
            List<Furniture> furnitures = Furnitures.getInstance().getFurnituresManager().getFurnituresOfCategory(category);

            ItemStack item = new ItemStack(Material.CONDUIT);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(text("§aCategoria #" + category));
            meta.lore(List.of(
                    text("§r"),
                    text("§fFurniture disponibili: §7" + furnitures.size()),
                    text("§r"),
                    text("§eᴄʟɪᴄᴄᴀ ᴘᴇʀ ᴀᴘʀɪʀᴇ ʟᴀ ᴄᴀᴛᴇɢᴏʀɪᴀ")
            ));
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, category);
            item.setItemMeta(meta);

            gui.addItem(ItemBuilder.from(item).asGuiItem(event -> {
                openFurnitures(player, category);
            }));
        }

        gui.open(player);
    }

    public void openFurnitures(Player player, String category) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Categoria #" + category))
                .rows(4)
                .pageSize(27)
                .disableAllInteractions()
                .create();

        int[] indexes = new int[]{28, 29, 33, 34, 35};

        for (int index : indexes)
            gui.setItem(index, ItemUtil.getGlass());

        gui.setItem(27, ItemBuilder.from(ItemUtil.getBack()).asGuiItem(event -> openCategories(player)));
        gui.setItem(30, ItemUtil.getPreviousPage(gui));
        gui.setItem(31, ItemUtil.getClose(gui, player));
        gui.setItem(32, ItemUtil.getNextPage(gui));

        List<Furniture> furnitures = Furnitures.getInstance().getFurnituresManager().getFurnituresOfCategory(category);
        for (Furniture furniture : furnitures) {
            ItemStack item = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(furniture);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            meta.lore(List.of(
                    text("§r"),
                    text("§fModel Data: §7" + meta.getCustomModelData()),
                    text("§fID: §7" + furniture.id()),
                    text("§r"),
                    text("§eᴄʟɪᴄᴄᴀ ᴘᴇʀ ᴏᴛᴛᴇɴᴇʀᴇ")
            ));

            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());

            item.setItemMeta(meta);

            gui.addItem(ItemBuilder.from(item).asGuiItem(event -> {
                ItemStack stack = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(furniture);

                FurnitureGetEvent furnitureGetEvent = new FurnitureGetEvent(player, stack, furniture);
                Bukkit.getServer().getPluginManager().callEvent(furnitureGetEvent);
                if (furnitureGetEvent.isCancelled()) return;

                player.getInventory().addItem(stack);
            }));
        }

        gui.open(player);
    }

    public void openSearch(Player player, String key, List<Furniture> furnitures) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Risultati Ricerca: " + key))
                .rows(4)
                .pageSize(27)
                .disableAllInteractions()
                .create();

        int[] indexes = new int[]{27, 28, 29, 33, 34, 35};

        for (int index : indexes)
            gui.setItem(index, ItemUtil.getGlass());

        gui.setItem(30, ItemUtil.getPreviousPage(gui));
        gui.setItem(31, ItemUtil.getClose(gui, player));
        gui.setItem(32, ItemUtil.getNextPage(gui));

        for (Furniture furniture : furnitures) {
            ItemStack item = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(furniture);
            ItemMeta meta = item.getItemMeta();
            meta.lore(List.of(
                    text("§f"),
                    text("§fModel Data: §7" + meta.getCustomModelData()),
                    text("§fID: §7" + furniture.id()),
                    text("§f"),
                    text("§eᴄʟɪᴄᴄᴀ ᴘᴇʀ ᴏᴛᴛᴇɴᴇʀᴇ"))
            );

            for (NamespacedKey namespacedKey : meta.getPersistentDataContainer().getKeys()) {
                meta.getPersistentDataContainer().remove(namespacedKey);
            }

            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
            item.setItemMeta(meta);

            gui.addItem(ItemBuilder.from(item).asGuiItem(event -> {
                ItemStack stack = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(furniture);

                FurnitureGetEvent furnitureGetEvent = new FurnitureGetEvent(player, stack, furniture);
                Bukkit.getServer().getPluginManager().callEvent(furnitureGetEvent);
                if (furnitureGetEvent.isCancelled()) return;

                player.getInventory().addItem(stack);
            }));
        }

        gui.open(player);
    }
}
