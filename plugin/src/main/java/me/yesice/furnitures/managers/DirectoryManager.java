package me.yesice.furnitures.managers;

import me.yesice.furnitures.Furnitures;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DirectoryManager {

    private final String BASE_PATH = Furnitures.getInstance().getDataFolder() + "/furnitures";

    private final List<File> categories = new ArrayList<>();

    public void loadCategories() {
        File baseDirectory = new File(BASE_PATH);
        if (!baseDirectory.isDirectory()) return;

        File[] files = baseDirectory.listFiles();
        if (files == null) return;

        for (File category : files) {
            if (!category.isDirectory()) continue;
            categories.add(category);
        }
    }

    public Optional<File> getCategory(String name) {
        File file = new File(BASE_PATH + "/" + name);
        if (!file.exists()) return Optional.empty();

        return Optional.of(file);
    }

    public FileConfiguration getFurnituresConfig(String categoryName) {
        Optional<File> optionalCategory = getCategory(categoryName);
        if (optionalCategory.isEmpty()) return null;

        File furnituresFile = new File(BASE_PATH + "/" + categoryName + "/furnitures.yml");
        if (!furnituresFile.exists()) {
            try {
                furnituresFile.createNewFile();

                FileConfiguration config = YamlConfiguration.loadConfiguration(furnituresFile);

                // SECTIONS
                ConfigurationSection furnituresSection = config.createSection("furnitures");
                ConfigurationSection exampleFurnitureSection = furnituresSection.createSection("example");
                ConfigurationSection settingsSection = exampleFurnitureSection.createSection("settings");
                ConfigurationSection itemSection = exampleFurnitureSection.createSection("item");

                settingsSection.set("type", "SMALL_ARMOR_STAND # ARMOR_STAND, SMALL_ARMOR_STAND, ITEM_FRAME");
                settingsSection.set("offset", "0.0#0.0#0.0");
                settingsSection.set("backwards", true);
                settingsSection.set("floor", true);
                settingsSection.set("walls", false);
                settingsSection.set("ceiling", false);

                itemSection.set("material", "STICK");
                itemSection.set("model-data", 0);
                itemSection.set("display-name", "&7Furniture");
                itemSection.set("lore", new ArrayList<>());

                exampleFurnitureSection.set("seats", new ArrayList<>());

                saveConfig(config, furnituresFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(furnituresFile);
    }

    public Optional<File> getFurnituresFile(String category) {
        File file = new File(BASE_PATH + "/" + category + "/furnitures.yml");
        if (!file.exists()) return Optional.empty();

        return Optional.of(file);
    }

    public void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<File> getCategories() {
        return categories;
    }
}
