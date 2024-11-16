package me.yesice.furnitures.api.events;

import me.yesice.furnitures.api.objects.Furniture;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FurnitureInteractEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Entity holder;
    private final Furniture furniture;

    public FurnitureInteractEvent(Player player, Entity holder, Furniture furniture) {
        this.player = player;
        this.holder = holder;
        this.furniture = furniture;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getHolder() {
        return holder;
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
