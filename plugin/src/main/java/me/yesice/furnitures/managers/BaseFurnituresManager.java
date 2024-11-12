package me.yesice.furnitures.managers;

import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.FurnituresManager;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseFurnituresManager implements FurnituresManager {

    @Override
    public Optional<Furniture> getFurniture(String category, String furniture) {
        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(category);
        if (config == null) return Optional.empty();

        ConfigurationSection furnitureSection = config.getConfigurationSection("furnitures." + furniture);
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
                furniture,
                material,
                modelData,
                displayName,
                lore,
                category,
                seats,
                offsetX,
                offsetY,
                offsetZ,
                backwards
        ));
    }

    @Override
    public List<Furniture> getFurnituresOfCategory(String category) {
        List<Furniture> furnitures = new ArrayList<>();

        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(category);
        if (config == null) return furnitures;

        ConfigurationSection furnituresSection = config.getConfigurationSection("furnitures");
        if (furnituresSection == null) return furnitures;

        for (String key : furnituresSection.getKeys(false)) {
            Optional<Furniture> optionalFurniture = getFurniture(category, key);
            optionalFurniture.ifPresent(furnitures::add);
        }

        return furnitures;
    }

    public ItemStack getFurnitureItem(String category, String furniture) {
        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(category);
        if (config == null) return null;

        ConfigurationSection furnitureSection = config.getConfigurationSection("furnitures." + furniture);
        if (furnitureSection == null) return null;

        ConfigurationSection itemSection = furnitureSection.getConfigurationSection("item");
        if (itemSection == null || !itemSection.contains("material")) return null;

        Material material;
        try {
            material = Material.valueOf(itemSection.getString("material"));
        } catch (IllegalArgumentException e) {
            return null; // Materiale non valido
        }

        int modelData = itemSection.getInt("model-data");
        String displayName = itemSection.getString("display-name", "Unknown");
        List<String> lore = itemSection.getStringList("lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(Utils.color(displayName)));
            meta.setCustomModelData(modelData);
            meta.lore(Utils.colorList(lore));
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture);
            meta.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, category);
            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean hasSmallArmorStand(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(furniture.category());
        return config != null && config.getBoolean("furnitures." + furniture.id() + ".settings.small", false);
    }

    public boolean isPlaceableOnFloor(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(furniture.category());
        return config != null && config.getBoolean("furnitures." + furniture.id() + ".settings.floor", false);
    }

    public boolean isPlaceableOnWalls(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(furniture.category());
        return config != null && config.getBoolean("furnitures." + furniture.id() + ".settings.walls", false);
    }

    public boolean isPlaceableOnCeiling(Furniture furniture) {
        FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(furniture.category());
        return config != null && config.getBoolean("furnitures." + furniture.id() + ".settings.ceiling", false);
    }

    public List<Furniture> searchFurniture(String searchKey) {
        searchKey = searchKey.toLowerCase(); // Metti in minuscolo per evitare problemi di case sensitivity
        return searchFurnitureFromKey(searchKey).isEmpty() ? searchFurnitureFromDisplayName(searchKey) : searchFurnitureFromKey(searchKey);
    }

    private List<Furniture> getFurnitures() {
        List<File> categories = Furnitures.getInstance().getDirectoryManager().getCategories();
        List<Furniture> furnitures = new ArrayList<>();

        for (File categoryFile : categories) {
            String categoryName = categoryFile.getName();
            furnitures.addAll(getFurnituresOfCategory(categoryName));
        }

        return furnitures;
    }

    private List<Furniture> searchFurnitureFromKey(String searchKey) {
        List<Furniture> furnitures = new ArrayList<>();

        for (Furniture furniture : getFurnitures()) {
            FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(furniture.category());

            ConfigurationSection furnituresSection = config.getConfigurationSection("furnitures");
            if (furnituresSection == null) continue;

            for (String key : furnituresSection.getKeys(false)) {
                if (key.toLowerCase().contains(searchKey)) {
                    furnitures.addAll(getFurnitures(key));
                }
            }
        }

        return furnitures;
    }

    public List<Furniture> getFurnitures(String id) {
        List<Furniture> furnituresList = new ArrayList<>();

        List<Furniture> furnitures = getFurnitures();
        for (Furniture furniture : furnitures) {
            if (!furniture.id().equals(id)) continue;

            furnituresList.add(furniture);
        }

        return furnituresList;
    }

    private List<Furniture> searchFurnitureFromDisplayName(String searchKey) {
        List<Furniture> furnitures = new ArrayList<>();

        for (Furniture furniture : getFurnitures()) {
            FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(furniture.category());

            ConfigurationSection furnituresSection = config.getConfigurationSection("furnitures");
            if (furnituresSection == null) return furnitures;

            for (String key : furnituresSection.getKeys(false)) {
                List<Furniture> furnituresList = getFurnitures(key);
                for (Furniture furniture1 : furnituresList) {
                    if (furniture1.displayName().toLowerCase().contains(searchKey)) {
                        furnitures.add(furniture1);
                    }
                }
            }
        }

        return furnitures;
    }
}
