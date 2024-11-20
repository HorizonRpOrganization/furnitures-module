package me.yesice.furnitures.listeners;

import me.yesice.furnitures.api.events.FurniturePlaceEvent;
import me.yesice.furnitures.api.objects.Furniture;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FurnituresListener implements Listener {

    @EventHandler
    public void onWashingMachinePlace(FurniturePlaceEvent event) {
        Player player = event.getPlayer();
        Furniture furniture = event.getFurniture();
        if (!furniture.id().equalsIgnoreCase("washing-machine")) return;

        player.sendMessage(Component.text("§aHai piazzato una lavatrice!"));
    }
}
