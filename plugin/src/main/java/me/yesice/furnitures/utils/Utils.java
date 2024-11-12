package me.yesice.furnitures.utils;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;

public class Utils {

    public static String color(String message) {
        String hexPattern = "(&#([0-9a-fA-F]{6}))";
        Pattern compiledPattern = Pattern.compile(hexPattern);
        Matcher matcher = compiledPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(2);
            String replacement = ChatColor.COLOR_CHAR + "x";
            for (char c : hexCode.toCharArray()) {
                replacement += ChatColor.COLOR_CHAR + "" + c;
            }
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<Component> colorList(List<String> list) {
        List<Component> components = new ArrayList<>();
        list.forEach(string -> components.add(Component.text(color(string))));
        return components;
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(text(color(message)));
    }

    public static String toSmallText(String text) {
        text = text.replace("a", "ᴀ");
        text = text.replace("b", "ʙ");
        text = text.replace("c", "ᴄ");
        text = text.replace("d", "ᴅ");
        text = text.replace("e", "ᴇ");
        text = text.replace("f", "ꜰ");
        text = text.replace("g", "ɢ");
        text = text.replace("h", "ʜ");
        text = text.replace("i", "ɪ");
        text = text.replace("j", "ᴊ");
        text = text.replace("k", "ᴋ");
        text = text.replace("l", "ʟ");
        text = text.replace("m", "ᴍ");
        text = text.replace("n", "ɴ");
        text = text.replace("o", "ᴏ");
        text = text.replace("p", "ᴘ");
        text = text.replace("q", "ǫ");
        text = text.replace("r", "ʀ");
        text = text.replace("s", "ꜱ");
        text = text.replace("t", "ᴛ");
        text = text.replace("u", "ᴜ");
        text = text.replace("v", "ᴠ");
        text = text.replace("w", "ᴡ");
        text = text.replace("x", "x");
        text = text.replace("y", "ʏ");
        text = text.replace("z", "ᴢ");
        text = text.replace("A", "ᴀ");
        text = text.replace("B", "ʙ");
        text = text.replace("C", "ᴄ");
        text = text.replace("D", "ᴅ");
        text = text.replace("E", "ᴇ");
        text = text.replace("F", "ꜰ");
        text = text.replace("G", "ɢ");
        text = text.replace("H", "ʜ");
        text = text.replace("I", "ɪ");
        text = text.replace("J", "ᴊ");
        text = text.replace("K", "ᴋ");
        text = text.replace("L", "ʟ");
        text = text.replace("M", "ᴍ");
        text = text.replace("N", "ɴ");
        text = text.replace("O", "ᴏ");
        text = text.replace("P", "ᴘ");
        text = text.replace("Q", "ǫ");
        text = text.replace("R", "ʀ");
        text = text.replace("S", "ꜱ");
        text = text.replace("T", "ᴛ");
        text = text.replace("U", "ᴜ");
        text = text.replace("V", "ᴠ");
        text = text.replace("W", "ᴡ");
        text = text.replace("X", "x");
        text = text.replace("Y", "ʏ");
        text = text.replace("Z", "ᴢ");

        return text;
    }

    public static String getCardinalDirection(Entity e) {
        double rotation = (e.getLocation().getYaw() - 90.0F) % 360.0F;

        if (rotation < 0.0D)
            rotation += 360.0D;

        if (0.0D <= rotation && rotation < 22.5D)
            return "W";
        if (22.5D <= rotation && rotation < 67.5D)
            return "NW";
        if (67.5D <= rotation && rotation < 112.5D)
            return "N";
        if (112.5D <= rotation && rotation < 157.5D)
            return "NE";
        if (157.5D <= rotation && rotation < 202.5D)
            return "E";
        if (202.5D <= rotation && rotation < 247.5D)
            return "SE";
        if (247.5D <= rotation && rotation < 292.5D)
            return "S";
        if (292.5D <= rotation && rotation < 337.5D)
            return "SW";
        if (337.5D <= rotation && rotation < 360.0D)
            return "W";

        return null;
    }
}
