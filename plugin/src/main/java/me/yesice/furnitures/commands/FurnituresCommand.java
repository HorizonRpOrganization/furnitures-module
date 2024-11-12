package me.yesice.furnitures.commands;

import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.gui.FurnituresGui;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FurnituresCommand implements CommandExecutor {

    //
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().severe("Errore: Non sei un player.");
            return true;
        }

        if (args.length == 0) {
            new FurnituresGui().openCategories(player);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("categories")) {
            List<File> categories = Furnitures.getInstance().getDirectoryManager().getCategories();
            if (categories.isEmpty()) {
                Utils.sendMessage(player, "&cNon ci sono categorie.");
                return true;
            }

            Utils.sendMessage(player, "&r");
            Utils.sendMessage(player, " &6" + Utils.toSmallText("lista categorie:"));
            for (File category : categories) {
                Utils.sendMessage(player, "  &8• &e" + category.getName());
            }
            Utils.sendMessage(player, "&r");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("furnitures")) {
            String categoryName = args[1];

            Optional<File> optionalCategory = Furnitures.getInstance().getDirectoryManager().getCategory(categoryName);
            if (optionalCategory.isEmpty()) {
                Utils.sendMessage(player, "&cCategoria non trovata.");
                return true;
            }

            FileConfiguration config = Furnitures.getInstance().getDirectoryManager().getFurnituresConfig(categoryName);
            if (config == null) {
                Utils.sendMessage(player, "&cConfig non trovato.");
                return true;
            }

            ConfigurationSection furnituresSection = config.getConfigurationSection("furnitures");
            if (furnituresSection == null) return true;

            Set<String> keys = furnituresSection.getKeys(false);
            if (keys.isEmpty()) return true;

            Utils.sendMessage(player, "&r");
            Utils.sendMessage(player, " &6" + Utils.toSmallText("lista furnitures") + " &7(#" + Utils.toSmallText(categoryName) + ")");
            for (String key : keys) {
                ConfigurationSection furnitureSection = furnituresSection.getConfigurationSection(key);
                if (furnitureSection == null) continue;

                Utils.sendMessage(player, "  &8• &e" + key);
            }
            Utils.sendMessage(player, "&r");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("search")) {
            String key = args[1];
            List<Furniture> furnitures = Furnitures.getInstance().getFurnituresManager().searchFurniture(key.toLowerCase());

            new FurnituresGui().openSearch(player, key, furnitures);
        } else {
            sendHelpMessage(player, label);
        }

        return true;
    }

    private void sendHelpMessage(Player player, String label) {
        Utils.sendMessage(player, "&r");
        Utils.sendMessage(player, " &c&l" + Utils.toSmallText("aiuto comandi") + " &7(/" + Utils.toSmallText(label) + ")");
        Utils.sendMessage(player, "  &8• &e/" + label + " categories");
        Utils.sendMessage(player, "  &8• &e/" + label + " furnitures <category>");
        Utils.sendMessage(player, "  &8• &e/" + label + " search <key>");
        Utils.sendMessage(player, "&r");
    }
}
