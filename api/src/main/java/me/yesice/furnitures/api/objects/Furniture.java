package me.yesice.furnitures.api.objects;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;

public record Furniture(String id, Material material, int modelData, String displayName, List<String> lore,
                        String category, List<Vector> seats, double offsetX, double offsetY,
                        double offsetZ, boolean backwards, String type) {
}
