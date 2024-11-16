package me.yesice.furnitures.utils;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class ItemFrameUtil {

    public static ItemFrame placeInvisibleItemFrame(Location location, BlockFace blockFace, boolean fixed) {
        ItemFrame itemFrame = location.getWorld().spawn(location, ItemFrame.class, frame -> {
            frame.setVisible(false);
            frame.setFacingDirection(blockFace);
        });

        itemFrame.setFixed(fixed); // Lock item rotation


        return itemFrame;
    }

    public static Location calculateFrameLocation(Location blockLocation, BlockFace blockFace) {
        Vector offset = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());

        return blockLocation.clone().add(offset);
    }

    public static boolean thereIsItemFrame(Location location) {
        List<Entity> entities = location.getWorld().getEntities();
        for (Entity entity : entities) {
            if (!(entity instanceof ItemFrame itemFrame)) continue;
            if (!Utils.isSameBlockLocation(itemFrame.getLocation(), location)) continue;

            return true;
        }

        return false;
    }

    public static Rotation getItemFrameRotation(float yaw) {
        // Normalizziamo il valore di yaw (da 0 a 360 gradi)
        yaw = (yaw % 360 + 360) % 360;

        // Arrotondiamo il yaw al multiplo di 45 più vicino (così da avere rotazioni stabili)
        yaw = Math.round(yaw / 45) * 45;

        // Suddividiamo il cerchio in 8 sezioni di 45 gradi ciascuna
        if (yaw == 360 || yaw == 0) {
            return Rotation.NONE; // Sud (0 gradi)
        } else if (yaw == 45) {
            return Rotation.CLOCKWISE_45; // Sud-Ovest (45 gradi)
        } else if (yaw == 90) {
            return Rotation.CLOCKWISE; // Ovest (90 gradi)
        } else if (yaw == 135) {
            return Rotation.CLOCKWISE_135; // Nord-Ovest (135 gradi)
        } else if (yaw == 180) {
            return Rotation.FLIPPED; // Nord (180 gradi)
        } else if (yaw == 225) {
            return Rotation.FLIPPED_45; // Nord-Est (225 gradi)
        } else if (yaw == 270) {
            return Rotation.COUNTER_CLOCKWISE; // Est (270 gradi)
        } else if (yaw == 315) {
            return Rotation.COUNTER_CLOCKWISE_45; // Sud-Est (315 gradi)
        } else {
            return Rotation.NONE; // Valore di fallback (Sud)
        }
    }


    public static Rotation getRandomRotation() {
        Rotation[] rotations = Rotation.values();
        Random random = new Random();
        return rotations[random.nextInt(rotations.length)];
    }
}
