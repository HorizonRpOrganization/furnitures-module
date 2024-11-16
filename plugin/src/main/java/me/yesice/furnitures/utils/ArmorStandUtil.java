package me.yesice.furnitures.utils;

import org.bukkit.entity.Player;

public class ArmorStandUtil {

    public static float getArmorStandRotationInGrid(Player player) {
        float yaw = player.getLocation().getYaw();

        yaw = (yaw % 360 + 360) % 360;

        yaw = Math.round(yaw / 45) * 45;

        return yaw;
    }
}
