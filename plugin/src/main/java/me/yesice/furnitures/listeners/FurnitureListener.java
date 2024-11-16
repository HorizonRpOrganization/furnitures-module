package me.yesice.furnitures.listeners;

import com.sk89q.worldguard.protection.flags.Flags;
import me.yesice.furnitures.Furnitures;
import me.yesice.furnitures.api.events.FurnitureBreakEvent;
import me.yesice.furnitures.api.events.FurnitureInteractEvent;
import me.yesice.furnitures.api.events.FurniturePlaceEvent;
import me.yesice.furnitures.api.objects.Furniture;
import me.yesice.furnitures.utils.*;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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

    private void placeFurniture(Player player, Block b, Block block, BlockFace blockFace, double offsetX,
                                double offsetY, double offsetZ, ItemStack item, Furniture furniture, Rotation rotation) {
        String type = furniture.type();

        if (type.equalsIgnoreCase("SMALL_ARMOR_STAND") || type.equalsIgnoreCase("ARMOR_STAND")) {
            boolean isSmall = type.equalsIgnoreCase("SMALL_ARMOR_STAND");
            spawnArmorStand(player, b, offsetX, offsetY, offsetZ, item, furniture, isSmall);
        } else if (type.equalsIgnoreCase("ITEM_FRAME")) {
            spawnItemFrame(player, block, blockFace, item, furniture, rotation);
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

    private void spawnItemFrame(Player player, Block block, BlockFace blockFace, ItemStack item, Furniture furniture, Rotation rotation) {
        Location location = block.getLocation();
        Location frameLocation = ItemFrameUtil.calculateFrameLocation(location, blockFace);

        if (!ItemFrameUtil.thereIsItemFrame(frameLocation)) {
            ItemFrame itemFrame = ItemFrameUtil.placeInvisibleItemFrame(frameLocation, blockFace, false);

            FurniturePlaceEvent furniturePlaceEvent = new FurniturePlaceEvent(player, itemFrame, furniture);
            Bukkit.getServer().getPluginManager().callEvent(furniturePlaceEvent);
            if (furniturePlaceEvent.isCancelled()) {
                itemFrame.remove();
                return;
            }

            itemFrame.setItem(item);
            itemFrame.getPersistentDataContainer()
                    .set(new NamespacedKey(Furnitures.getInstance(), "furniture"), PersistentDataType.STRING, furniture.id());
            itemFrame.getPersistentDataContainer()
                    .set(new NamespacedKey(Furnitures.getInstance(), "category"), PersistentDataType.STRING, furniture.category());
            itemFrame.setRotation(rotation);
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

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(category, id);
        if (optionalFurniture.isEmpty()) return;

        Furniture furniture = optionalFurniture.get();

        event.setCancelled(true);

        if (RegionUtils.testRegionFlag(player, block.getLocation(), Flags.BLOCK_PLACE)
                || player.hasPermission("furnitures.place.bypass")) {
            Block b = block.getRelative(0, 1, 0);

            double offsetX = furniture.offsetX() * blockFace.getModX();
            double offsetY = furniture.offsetY() * blockFace.getModY();
            double offsetZ = furniture.offsetZ() * blockFace.getModZ();

            Rotation rotation = Rotation.NONE;
            if (blockFace == BlockFace.UP) {
                // floor
                if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnFloor(furniture)) return;
                rotation = ItemFrameUtil.getItemFrameRotation(player.getLocation().getYaw());
            } else if (blockFace == BlockFace.DOWN) {
                // ceiling
                if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnCeiling(furniture)) return;
            } else {
                // wall
                if (!Furnitures.getInstance().getFurnituresManager().isPlaceableOnWalls(furniture)) return;
            }

            ItemStack clone = item.clone();
            clone.setAmount(1);

            ItemMeta cloneMeta = clone.getItemMeta();
            cloneMeta.displayName(Component.text("§r"));
            clone.setItemMeta(cloneMeta);

            placeFurniture(player, b, block, blockFace, offsetX, offsetY, offsetZ, clone, furniture, rotation);

            if (player.getGameMode() != GameMode.CREATIVE)
                item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemFrameFurnitureDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");

        if (!itemFrame.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
        if (!itemFrame.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

        String id = itemFrame.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
        String categoryName = itemFrame.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
        if (optionalFurniture.isEmpty()) return;

        event.setCancelled(true);

        if (RegionUtils.testRegionFlag(player, itemFrame.getLocation(), Flags.BLOCK_BREAK) || player.hasPermission("furnitures.break.bypass")) {
            ItemStack item = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(categoryName, id);

            Bukkit.getScheduler().runTask(Furnitures.getInstance(), itemFrame::remove);

            if (!item.getType().isAir()) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.getInventory().addItem(item);
                } else {
                    ItemUtils.dropItem(itemFrame.getLocation(), item);
                }
            }
        }
    }

    @EventHandler
    public void onItemFrameRotation(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) return;

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");

        if (!itemFrame.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
        if (!itemFrame.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

        String id = itemFrame.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
        String categoryName = itemFrame.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
        if (optionalFurniture.isEmpty()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandFurnitureBreak(EntityDamageByEntityEvent event) {
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

        ItemStack item = Furnitures.getInstance().getFurnituresManager().getFurnitureItem(categoryName, id);

        event.setCancelled(true);

        if (RegionUtils.testRegionFlag(player, armorStand.getLocation(), Flags.BLOCK_BREAK)
                || player.hasPermission("furnitures.break.bypass")) {
            FurnitureBreakEvent furnitureBreakEvent = new FurnitureBreakEvent(player, armorStand, furniture);
            Bukkit.getServer().getPluginManager().callEvent(furnitureBreakEvent);
            if (furnitureBreakEvent.isCancelled()) return;

            armorStand.remove();
            for (Entity e : armorStand.getWorld().getEntities()) {
                NamespacedKey namespacedKey = new NamespacedKey(Furnitures.getInstance(), "furniture-seat-" + e.getEntityId());
                if (!e.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) continue;

                e.remove();
            }
            if (item != null) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.getInventory().addItem(item);
                } else {
                    ItemUtils.dropItem(armorStand.getLocation(), item);
                }
            }
        }
    }


    @EventHandler
    public void onArmorStandFurnitureBreak2(EntityDamageByEntityEvent event) {
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

        if (RegionUtils.testRegionFlag(player, armorStand.getLocation(), Flags.BLOCK_BREAK)
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
                ItemUtils.dropItem(armorStand.getLocation(), helmet);
            }
        }
    }

    @EventHandler
    public void onFurnitureInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        NamespacedKey key1 = new NamespacedKey(Furnitures.getInstance(), "furniture");
        NamespacedKey key2 = new NamespacedKey(Furnitures.getInstance(), "category");
        if (!entity.getPersistentDataContainer().has(key1, PersistentDataType.STRING)) return;
        if (!entity.getPersistentDataContainer().has(key2, PersistentDataType.STRING)) return;

        String id = entity.getPersistentDataContainer().get(key1, PersistentDataType.STRING);
        String categoryName = entity.getPersistentDataContainer().get(key2, PersistentDataType.STRING);

        Optional<Furniture> optionalFurniture = Furnitures.getInstance().getFurnituresManager().getFurniture(categoryName, id);
        if (optionalFurniture.isEmpty()) return;

        Furniture furniture = optionalFurniture.get();
        FurnitureInteractEvent interactEvent = new FurnitureInteractEvent(player, entity, furniture);
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
