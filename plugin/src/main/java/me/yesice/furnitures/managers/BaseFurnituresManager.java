package me.yesice.furnitures.managers;

import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.FurnituresManager;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BaseFurnituresManager implements FurnituresManager {

    @Override
    public Optional<Furniture> getFurniture(String id) {
        FileConfiguration config = Furnitures.getInstance().getConfig();

        ConfigurationSection furnitureSection = config.getConfigurationSection("furnitures." + id);
        if (furnitureSection == null) return Optional.empty();

        ConfigurationSection settingsSection = furnitureSection.getConfigurationSection("settings");
        if (settingsSection == null) return Optional.empty();

        ConfigurationSection itemSection = furnitureSection.getConfigurationSection("item");
        if (itemSection == null || !itemSection.contains("material")) return Optional.empty();

        Material material;
        try {
            material = Material.valueOf(itemSection.getString("material"));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        String type = settingsSection.getString("type");
        String category = settingsSection.getString("category");

        int modelData = itemSection.getInt("model-data");
        String displayName = itemSection.getString("display-name", "Unknown");
        List<String> lore = itemSection.getStringList("lore");

        String offset = settingsSection.getString("offset", "0.0#0.0#0.0");
        String[] offsetArgs = offset.split("#");
        if (offsetArgs.length != 3) return Optional.empty();

        double offsetX = Double.parseDouble(offsetArgs[0]);
        double offsetY = Double.parseDouble(offsetArgs[1]);
        double offsetZ = Double.parseDouble(offsetArgs[2]);

        boolean backwards = settingsSection.getBoolean("backwards", false);

        List<Vector> seats = new ArrayList<>();
        for (String line : furnitureSection.getStringList("seats")) {
            String[] args = line.split("#");
            if (args.length == 3) {
                seats.add(new Vector(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])));
            }
        }

        return Optional.of(new Furniture(
                id,
                material,
                modelData,
                displayName,
                lore,
                category,
                seats,
                offsetX,
                offsetY,
                offsetZ,
                backwards,
                type
        ));
    }

    @Override
    public List<Furniture> getFurnituresOfCategory(String category) {
        List<Furniture> furnitures = new ArrayList<>();

        FileConfiguration config = Furnitures.getInstance().getConfig();

        ConfigurationSection furnituresSection = config.getConfigurationSection("furnitures");
        if (furnituresSection == null) return furnitures;

        for (String key : furnituresSection.getKeys(false)) {
            Furniture furniture = getFurniture(key).orElse(null);
            if (furniture == null || !furniture.category().equals(category)) continue;

            furnitures.add(furniture);
        }

        return furnitures;
    }

    public List<String> getCategories() {
        FileConfiguration config = Furnitures.getInstance().getConfig();
        return new ArrayList<>(config.getStringList("categories"));
    }

    public ItemStack getFurnitureItem(String furniture) {
        FileConfiguration config = Furnitures.getInstance().getConfig();

        ConfigurationSection furnitureSection = config.getConfigurationSection("furnitures." + furniture);
        if (furnitureSection == null) return null;

        ConfigurationSection settingsSection = furnitureSection.getConfigurationSection("settings");
        if (settingsSection == null) return null;

        ConfigurationSection itemSection = furnitureSection.getConfigurationSection("item");
        if (itemSection == null) return null;

        Material material;
        try {
            material = Material.valueOf(itemSection.getString("material"));
        } catch (IllegalArgumentException e) {
            return null; // Materiale non valido
        }

        String category = settingsSection.getString("category");
        if (category == null) return null;

        int modelData = itemSection.getInt("model-data");
        String displayName = itemSection.getString("display-name", "Unknown");
        List<String> lore = itemSection.getStringList("lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(Util.color(displayName)));
            meta.setCustomModelData(modelData);
            meta.lore(Util.colorList(lore));
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture);
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, Objects.requireNonNull(category));
            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getFurnitureItem(Furniture furniture) {
        return getFurnitureItem(furniture.id());
    }

    public boolean isPlaceableOnFloor(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getConfig();
        return config.getBoolean("furnitures." + furniture.id() + ".settings.floor", false);
    }

    public boolean isPlaceableOnWalls(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getConfig();
        return config.getBoolean("furnitures." + furniture.id() + ".settings.walls", false);
    }

    public boolean isPlaceableOnCeiling(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getConfig();
        return config.getBoolean("furnitures." + furniture.id() + ".settings.ceiling", false);
    }
}
