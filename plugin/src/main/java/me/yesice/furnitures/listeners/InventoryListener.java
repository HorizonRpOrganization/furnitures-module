package me.yesice.furnitures.listeners;

import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.events.FurnitureGetEvent;
import me.yesice.furnitures.gui.FurnituresGui;
import me.yesice.furnitures.api.objects.Furniture;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getView().getTitle().equals("Categorie Furnitures")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(Furnitures.getInstance(), "category");
            if (!meta.getPersistentDataContainer().has(key)) return;

            String category = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            new FurnituresGui().openFurnitures(player, category);
        } else if (event.getView().getTitle().startsWith("Categoria #") || event.getView().getTitle().startsWith("Risultati Ricerca: ")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            ItemMeta meta = item.getItemMeta();
            NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
            NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");
            if (!meta.getPersistentDataContainer().has(key1)) return;
            if (!meta.getPersistentDataContainer().has(key2)) return;

            String categoryName = meta.getPersistentDataContainer().get(key2, PersistentDataType.STRING);
            String furnitureName = meta.getPersistentDataContainer().get(key1, PersistentDataType.STRING);

            Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, furnitureName);
            if (optionalFurniture.isEmpty()) return;

            Furniture furniture = optionalFurniture.get();
            ItemStack stack = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(categoryName, furnitureName);

            FurnitureGetEvent furnitureGetEvent = new FurnitureGetEvent(player, stack, furniture);
            Bukkit.getServer().getPluginManager().callEvent(furnitureGetEvent);
            if (furnitureGetEvent.isCancelled()) return;

            player.getInventory().addItem(stack);
        }
    }
}
