package me.yesice.furnitures;

import me.yesice.furnitures.api.FurnituresAPI;
import me.yesice.furnitures.commands.FurnituresCommand;
import me.yesice.furnitures.listeners.FurnitureListener;
import me.yesice.furnitures.listeners.FurnituresListener;
import me.yesice.furnitures.managers.BaseFurnituresManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Furnitures extends JavaPlugin implements FurnituresAPI {

    private static Furnitures instance;
    private BaseFurnituresManager baseFurnituresManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        baseFurnituresManager = new BaseFurnituresManager();

        getServer().getPluginManager().registerEvents(new FurnitureListener(), this);
        getServer().getPluginManager().registerEvents(new FurnituresListener(), this);

        getCommand("furnitures").setExecutor(new FurnituresCommand());

        Bukkit.getServicesManager().register(FurnituresAPI.class, this, this, ServicePriority.Normal);
    }

    public static Furnitures getInstance() {
        return instance;
    }

    public BaseFurnituresManager getFurnituresManager() {
        return baseFurnituresManager;
    }
}
