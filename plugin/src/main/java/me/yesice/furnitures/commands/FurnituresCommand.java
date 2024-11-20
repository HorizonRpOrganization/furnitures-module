package me.yesice.furnitures.commands;

import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.constants.Permissions;
import me.yesice.furnitures.gui.FurnituresGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class FurnituresCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNon sei un player.");
            return true;
        }

        if (!player.hasPermission(Permissions.FURNITURES.permission())) {
            player.sendMessage(text("§cNon hai il permesso per eseguire questo comando!"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Furnitures.getInstance().reloadConfig();
            player.sendMessage(text("§aConfig ricaricato correttamente."));
        } else {
            new FurnituresGui().openCategories(player);
        }

        return true;
    }
}
