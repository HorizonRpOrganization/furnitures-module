package me.yesice.furnitures.api.events;

import me.yesice.furnitures.api.objects.Furniture;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FurnitureInteractEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final ArmorStand armorStand;
    private final Furniture furniture;

    public FurnitureInteractEvent(Player player, ArmorStand armorStand, Furniture furniture) {
        this.player = player;
        this.armorStand = armorStand;
        this.furniture = furniture;
    }

    public Player getPlayer() {
        return player;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Furniture getFurniture() {
        return furniture;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
