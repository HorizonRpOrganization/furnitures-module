package me.yesice.furnitures.listeners;

import com.sk89q.worldguard.protection.flags.Flags;
import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.events.FurnitureBreakEvent;
import me.yesice.furnitures.api.events.FurnitureInteractEvent;
import me.yesice.furnitures.api.events.FurniturePlaceEvent;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.ItemUtils;
import me.yesice.furnitures.utils.RegionUtils;
import me.yesice.furnitures.utils.Utils;
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

    @EventHandler
    public void onFurniturePlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            if (!block.getType().isSolid()) return;
            BlockFace blockFace = event.getBlockFace();

            ItemStack item = event.getItem();
            if (item == null) return;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
            NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");
            if (!meta.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
            if (!meta.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

            String id = meta.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
            String categoryName = meta.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

            Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
            if (optionalFurniture.isEmpty()) {
                System.out.println("Furniture is empty.");
                return;
            }

            Furniture furniture = optionalFurniture.get();

            event.setCancelled(true);

            if (RegionUtils.testRegionFlag(player, block.getLocation(), Flags.BLOCK_PLACE) || player.hasPermission("furnitures.place.bypass")) {
                Block b = block.getRelative(0, 1, 0);

                double offsetX = furniture.offsetX();
                double offsetY = furniture.offsetY();
                double offsetZ = furniture.offsetZ();

                if (blockFace == BlockFace.UP) {
                    // pavimento
                    if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnFloor(furniture)) return;
                } else if (blockFace == BlockFace.DOWN) {
                    // soffitto
                    if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnCeiling(furniture)) return;
                } else {
                    // muro
                    if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnWalls(furniture)) return;

                    switch (blockFace) {
                        case NORTH:
                            offsetX = furniture.offsetX(); // X rimane invariato
                            offsetY = furniture.offsetY(); // Y invariato
                            offsetZ = -furniture.offsetZ(); // Z negativo per Nord
                            break;
                        case SOUTH:
                            offsetX = furniture.offsetX(); // X invariato
                            offsetY = furniture.offsetY(); // Y invariato
                            offsetZ = furniture.offsetZ();  // Z positivo per Sud
                            break;
                        case WEST:
                            offsetX = -furniture.offsetZ(); // X negativo per Ovest
                            offsetY = furniture.offsetY();  // Y invariato
                            offsetZ = furniture.offsetX();  // Z invariato
                            break;
                        case EAST:
                            offsetX = furniture.offsetZ();  // X positivo per Est
                            offsetY = furniture.offsetY();  // Y invariato
                            offsetZ = furniture.offsetX();  // Z invariato
                            break;
                    }
                }

                ItemStack clone = item.clone();
                clone.setAmount(1);

                double y = furniture.backwards() ? Math.toRadians(player.getLocation().getYaw() + 180) : Math.toRadians(player.getLocation().getYaw());
                float v1 = furniture.backwards() ? player.getLocation().getYaw() + 180 : player.getLocation().getYaw();
                ArmorStand armorStand = player.getWorld().spawn(b.getLocation().add(0.5 + offsetX, offsetY, 0.5 + offsetZ), ArmorStand.class, stand -> {
                    stand.setInvisible(true);
                    stand.setVisible(false);
                    stand.setHeadPose(new EulerAngle(
                            0,
                            y,
                            0
                    ));
                    stand.setRotation(0, v1);
                    stand.setSmall(Furnitures.getInstance().getFurnituresManager().hasSmallArmorStand(furniture));
                    stand.setGravity(false);
                    stand.setCustomNameVisible(false);
                    stand.setDisabledSlots(EquipmentSlot.HEAD);
                    stand.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
                    stand.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
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
                item.setAmount(item.getAmount() - 1);

                if (!furniture.seats().isEmpty()) {
                    for (Vector vector : furniture.seats()) {
                        double seatOffsetX = vector.getX();
                        double seatOffsetZ = vector.getZ();

                        switch (Objects.requireNonNull(Utils.getCardinalDirection(player))) {
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
                            stand.setHeadPose(new EulerAngle(headRotation.getX(), headRotation.getY(), headRotation.getZ()));
                            stand.setRotation(player.getLocation().getYaw() + 180, player.getLocation().getPitch());
                            stand.setGravity(false);
                            stand.setCustomNameVisible(false);
                            stand.setDisabledSlots(EquipmentSlot.HEAD);
                            stand.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
                            stand.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
                        });
                        as.getPersistentDataContainer().set(new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + as.getEntityId()), PersistentDataType.STRING, armorStand.getUniqueId().toString());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFurnitureBreak(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");
        if (!armorStand.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
        if (!armorStand.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

        String id = armorStand.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
        String categoryName = armorStand.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
        if (optionalFurniture.isEmpty()) return;

        Furniture furniture = optionalFurniture.get();

        ItemStack helmet = armorStand.getEquipment().getHelmet();
        if (helmet == null) return;

        event.setCancelled(true);

        player.stopSound(Sound.ENTITY_ARMOR_STAND_HIT);

        if (RegionUtils.testRegionFlag(player, armorStand.getLocation(), Flags.BLOCK_BREAK) || player.hasPermission("furnitures.break.bypass")) {
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
                ItemUtils.dropItem(armorStand.getLocation(), helmet);
            }
        }
    }

    @EventHandler
    public void onFurnitureBreak2(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + armorStand.getEntityId());
        NamespacedKey key3 = new NamespacedKey(Furnitures.getInstance(), "category");
        if (!armorStand.getPersistentDataContainer().has(key1)) return;
        if (!armorStand.getPersistentDataContainer().has(key2)) return;
        if (!armorStand.getPersistentDataContainer().has(key3)) return;

        String categoryName = armorStand.getPersistentDataContainer().get(key3, PersistentDataType.STRING);
        String id = armorStand.getPersistentDataContainer().get(key1, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
        if (optionalFurniture.isEmpty()) return;

        String armorStandId = armorStand.getPersistentDataContainer().get(key2, PersistentDataType.STRING);
        if (armorStandId == null) return;

        Entity entity1 = Bukkit.getEntity(UUID.fromString(armorStandId));
        if (!(entity1 instanceof ArmorStand armorStand1)) return;

        Furniture furniture = optionalFurniture.get();

        ItemStack helmet = armorStand1.getEquipment().getHelmet();
        if (helmet == null) return;

        event.setCancelled(true);

        player.stopSound(Sound.ENTITY_ARMOR_STAND_HIT);

        if (RegionUtils.testRegionFlag(player, armorStand.getLocation(), Flags.BLOCK_BREAK) || player.hasPermission("furnitures.break.bypass")) {
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
                ItemUtils.dropItem(armorStand.getLocation(), helmet);
            }
        }
    }

    @EventHandler
    public void onFurnitureInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) return;

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + armorStand.getEntityId());
        NamespacedKey key3 = new NamespacedKey(Furnitures.getInstance(), "category");
        if (!armorStand.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
        if (armorStand.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;
        if (armorStand.getPersistentDataContainer().has(key3, PersistentDataType.STRING)) return;

        String id = armorStand.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
        String categoryName = armorStand.getPersistentDataContainer().get(key3, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
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
