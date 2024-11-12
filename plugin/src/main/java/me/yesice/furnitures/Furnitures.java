package me.yesice.furnitures;

import me.yesice.furnitures.api.FurnituresAPI;
import me.yesice.furnitures.commands.FurnituresCommand;
import me.yesice.furnitures.listeners.FurnitureListener;
import me.yesice.furnitures.listeners.InventoryListener;
import me.yesice.furnitures.managers.BaseFurnituresManager;
import me.yesice.furnitures.managers.DirectoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Furnitures extends JavaPlugin implements FurnituresAPI {

    private static Furnitures instance;
    private DirectoryManager directoryManager;
    private BaseFurnituresManager baseFurnituresManager;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        createFurnituresFolder();

        instance = this;
        directoryManager = new DirectoryManager();
        baseFurnituresManager = new BaseFurnituresManager();

        // LOAD ALL CATEGORIES
        directoryManager.loadCategories();

        getServer().getPluginManager().registerEvents(new FurnitureListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        getCommand("furnitures").setExecutor(new FurnituresCommand());

        Bukkit.getServicesManager().register(FurnituresAPI.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        directoryManager.getCategories().clear();
    }

    private void createFurnituresFolder() {
        String path = getDataFolder().getAbsolutePath() + "/furnitures";
        File folder = new File(path);
        if (!folder.exists()) folder.mkdirs();
    }

    public static Furnitures getInstance() {
        return instance;
    }

    public DirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public BaseFurnituresManager getFurnituresManager() {
        return baseFurnituresManager;
    }
}
