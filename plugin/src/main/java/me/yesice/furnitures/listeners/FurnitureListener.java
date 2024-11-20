package me.yesice.furnitures.listeners;

import com.sk89q.worldguard.protection.flags.Flags;
import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.events.FurnitureBreakEvent;
import me.yesice.furnitures.api.events.FurnitureInteractEvent;
import me.yesice.furnitures.api.events.FurniturePlaceEvent;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.constants.Permissions;
import me.yesice.furnitures.utils.ArmorStandUtil;
import me.yesice.furnitures.utils.ItemUtil;
import me.yesice.furnitures.utils.RegionUtil;
import me.yesice.furnitures.utils.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FurnitureListener implements Listener {

    private void placeFurniture(Player player, Block b, double offsetX, double offsetY, double offsetZ, ItemStack item,
                                Furniture furniture) {
        String type = furniture.type();

        if (type.equalsIgnoreCase("SMALL_ARMOR_STAND") || type.equalsIgnoreCase("ARMOR_STAND")) {
            boolean isSmall = type.equalsIgnoreCase("SMALL_ARMOR_STAND");
            spawnArmorStand(player, b, offsetX, offsetY, offsetZ, item, furniture, isSmall);
        }
    }

    private void spawnArmorStand(Player player, Block b, double offsetX, double offsetY, double offsetZ, ItemStack clone,
                                 Furniture furniture, boolean isSmall) {
        float v1 = furniture.backwards() ? ArmorStandUtil.getArmorStandRotationInGrid(player) + 180 : ArmorStandUtil.getArmorStandRotationInGrid(player);
        ArmorStand armorStand = player.getWorld().spawn(b.getLocation().add(0.5 + offsetX, offsetY, 0.5 + offsetZ), ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setVisible(false);
            stand.setRotation(v1, stand.getLocation().getPitch());
            stand.setSmall(isSmall);
            stand.setGravity(false);
            stand.setCustomNameVisible(false);
            stand.setDisabledSlots(EquipmentSlot.HEAD);
            stand.getPersistentDataContainer()
                    .set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
            stand.getPersistentDataContainer()
                    .set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
        });

        Bukkit.getScheduler().runTaskLater(Furnitures.getInstance(), () -> {
            armorStand.getEquipment().setHelmet(clone);
        }, 2L);

        FurniturePlaceEvent furniturePlaceEvent = new FurniturePlaceEvent(player, armorStand, furniture);
        Bukkit.getServer().getPluginManager().callEvent(furniturePlaceEvent);
        if (furniturePlaceEvent.isCancelled()) {
            armorStand.remove();
            return;
        }

        if (!furniture.seats().isEmpty()) {
            for (Vector vector : furniture.seats()) {
                double seatOffsetX = vector.getX();
                double seatOffsetZ = vector.getZ();

                switch (Objects.requireNonNull(Util.getCardinalDirection(player))) {
                    case "N":
                        seatOffsetX = vector.getX(); // X rimane invariato
                        seatOffsetZ = -vector.getZ(); // Z negativo per Nord
                        break;
                    case "S":
                        seatOffsetX = vector.getX(); // X invariato
                        seatOffsetZ = vector.getZ();  // Z positivo per Sud
                        break;
                    case "W":
                        seatOffsetX = -vector.getZ(); // X negativo per Ovest
                        seatOffsetZ = vector.getX();  // Z invariato
                        break;
                    case "E":
                        seatOffsetX = vector.getZ();  // X positivo per Est
                        seatOffsetZ = vector.getX();  // Z invariato
                        break;
                }

                Vector direction = player.getLocation().getDirection().multiply(-1);

                Location loc = armorStand.getLocation().add(seatOffsetX, vector.getY(), seatOffsetZ);
                ArmorStand as = player.getWorld().spawn(loc, ArmorStand.class, stand -> {
                    Location headRotation = stand.getLocation().setDirection(direction);
                    stand.setInvisible(true);
                    stand.setVisible(false);
                    stand.setHeadPose(new EulerAngle(0, headRotation.getY(), 0));
                    stand.setRotation(player.getLocation().getYaw() + 180, player.getLocation().getPitch());
                    stand.setGravity(false);
                    stand.setCustomNameVisible(false);
                    stand.setDisabledSlots(EquipmentSlot.HEAD);
                    stand.getPersistentDataContainer()
                            .set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
                    stand.getPersistentDataContainer()
                            .set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
                });
                as.getPersistentDataContainer()
                        .set(new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + as.getEntityId()),
                                PersistentDataType.STRING, armorStand.getUniqueId().toString());
            }
        }
    }

    @EventHandler
    public void onFurniturePlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!block.getType().isSolid()) return;

        BlockFace blockFace = event.getBlockFace();

        ItemStack item = event.getItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey furnitureKey = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey categoryKey = new NamespacedKey(Furnitures.getInstance(), "category");
        if (!meta.getPersistentDataContainer().has(furnitureKey, PersistentDataType.STRING)) return;
        if (!meta.getPersistentDataContainer().has(categoryKey, PersistentDataType.STRING)) return;

        String id = meta.getPersistentDataContainer().get(furnitureKey, PersistentDataType.STRING);
        String category = meta.getPersistentDataContainer().get(categoryKey, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(id);
        if (optionalFurniture.isEmpty()) return;

        Furniture furniture = optionalFurniture.get();

        event.setCancelled(true);

        if (RegionUtil.testRegionFlag(player, block.getLocation(), Flags.BLOCK_PLACE)
                || player.hasPermission(Permissions.BYPASS_FURNITURE_PLACE.permission())) {
            Block b = block.getRelative(0, 1, 0);

            double offsetX = furniture.offsetX() * blockFace.getModX();
            double offsetY = furniture.offsetY() * blockFace.getModY();
            double offsetZ = furniture.offsetZ() * blockFace.getModZ();

            if (blockFace == BlockFace.UP) {
                // floor
                if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnFloor(furniture)) return;
            } else if (blockFace == BlockFace.DOWN) {
                // ceiling
                if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnCeiling(furniture)) return;
            } else {
                // wall
                if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnWalls(furniture)) return;
            }

            ItemStack clone = item.clone();
            clone.setAmount(1);

            placeFurniture(player, b, offsetX, offsetY, offsetZ, clone, furniture);

            if (player.getGameMode() != GameMode.CREATIVE)
                item.setAmount(item.getAmount() - 1);
        }
    }

    private void handleFurnitureBreak(Player player, ArmorStand armorStand, boolean isSeat) {
        if (!isSeat) {
            NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
            NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");
            if (!armorStand.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
            if (!armorStand.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

            String id = armorStand.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
            String categoryName = armorStand.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

            Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(id);
            if (optionalFurniture.isEmpty()) return;

            Furniture furniture = optionalFurniture.get();

            ItemStack helmet = armorStand.getEquipment().getHelmet();
            if (helmet == null) return;

            player.stopSound(Sound.ENTITY_ARMOR_STAND_HIT);

            if (RegionUtil.testRegionFlag(player, armorStand.getLocation(), Flags.BLOCK_BREAK)
                    || player.hasPermission(Permissions.BYPASS_FURNITURE_BREAK.permission())) {
                FurnitureBreakEvent furnitureBreakEvent = new FurnitureBreakEvent(player, armorStand, furniture);
                Bukkit.getServer().getPluginManager().callEvent(furnitureBreakEvent);
                if (furnitureBreakEvent.isCancelled()) return;

                for (Entity entity : armorStand.getWorld().getEntities()) {
                    NamespacedKey namespacedKey = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + entity.getEntityId());
                    if (!entity.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) continue;

                    entity.remove();
                }
                armorStand.remove();
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.getInventory().addItem(helmet);
                } else {
                    ItemUtil.dropItem(armorStand.getLocation(), helmet);
                }
            }
        } else {
            NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
            NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + armorStand.getEntityId());
            NamespacedKey key3 = new NamespacedKey(Furnitures.getInstance(), "category");
            if (!armorStand.getPersistentDataContainer().has(key1)) return;
            if (!armorStand.getPersistentDataContainer().has(key2)) return;
            if (!armorStand.getPersistentDataContainer().has(key3)) return;

            String categoryName = armorStand.getPersistentDataContainer().get(key3, PersistentDataType.STRING);
            String id = armorStand.getPersistentDataContainer().get(key1, PersistentDataType.STRING);

            Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(id);
            if (optionalFurniture.isEmpty()) return;

            String armorStandId = armorStand.getPersistentDataContainer().get(key2, PersistentDataType.STRING);
            if (armorStandId == null) return;

            Entity entity1 = Bukkit.getEntity(UUID.fromString(armorStandId));
            if (!(entity1 instanceof ArmorStand armorStand1)) return;

            Furniture furniture = optionalFurniture.get();

            ItemStack helmet = armorStand1.getEquipment().getHelmet();
            if (helmet == null) return;

            player.stopSound(Sound.ENTITY_ARMOR_STAND_HIT);

            if (RegionUtil.testRegionFlag(player, armorStand.getLocation(), Flags.BLOCK_BREAK)
                    || player.hasPermission("furnitures.break.bypass")) {
                FurnitureBreakEvent furnitureBreakEvent = new FurnitureBreakEvent(player, armorStand, furniture);
                Bukkit.getServer().getPluginManager().callEvent(furnitureBreakEvent);
                if (furnitureBreakEvent.isCancelled()) return;

                for (Entity entity : armorStand.getWorld().getEntities()) {
                    NamespacedKey namespacedKey = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + entity.getEntityId());
                    if (!entity.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) continue;

                    entity.remove();
                }
                armorStand1.remove();
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.getInventory().addItem(helmet);
                } else {
                    ItemUtil.dropItem(armorStand.getLocation(), helmet);
                }
            }
        }
    }

    @EventHandler
    public void onFurnitureBreak(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        event.setCancelled(true);

        handleFurnitureBreak(player, armorStand, false);
    }

    @EventHandler
    public void onFurnitureSeatBreak(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        event.setCancelled(true);

        handleFurnitureBreak(player, armorStand, true);
    }

    @EventHandler
    public void onFurnitureInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand armorStand)) return;

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");
        if (!armorStand.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
        if (!armorStand.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

        String id = armorStand.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
        String categoryName = armorStand.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(id);
        if (optionalFurniture.isEmpty()) return;

        Furniture furniture = optionalFurniture.get();
        FurnitureInteractEvent interactEvent = new FurnitureInteractEvent(player, armorStand, furniture);
        Bukkit.getServer().getPluginManager().callEvent(interactEvent);
    }

    @EventHandler
    public void onSeatInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity rightClicked = event.getRightClicked();
        if (!(rightClicked instanceof ArmorStand armorStand)) return;

        NamespacedKey key = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + armorStand.getEntityId());
        if (!armorStand.getPersistentDataContainer().has(key)) return;

        if (armorStand.getPassengers().isEmpty() && (armorStand.getLocation().distance(player.getLocation()) <= 2)) {
            armorStand.addPassenger(player);
        }
    }
}
