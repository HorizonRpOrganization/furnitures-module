package me.yesice.furnitures.utils;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class RegionUtils {

    public static void sendRegionMessage(String region, Component text) {
        for (Player regionPlayer : getRegionPlayers(region)) {
            regionPlayer.sendMessage(text);
        }
    }

    public static List<Player> getRegionPlayers(String region) {
        List<Player> players = Lists.newArrayList();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (getRegions(onlinePlayer).getRegions().stream().anyMatch((protectedRegion) -> protectedRegion.getId().equals(region))) {
                players.add(onlinePlayer);
            }
        }

        return players;
    }

    public static boolean isInRegion(Player player, String region) {
        return isInRegion(player.getLocation(), region);
    }

    public static boolean isInRegion(Location location, String region) {
        return getRegions(location).getRegions().stream().anyMatch((protectedRegion) -> protectedRegion.getId().equals(region));
    }

    public static Optional<ProtectedRegion> getRegionByName(String regionId, World world) {
        return Optional.ofNullable(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)).getRegion(regionId));
    }

    public static ApplicableRegionSet getRegions(Player player) {
        return getRegions(player.getLocation());
    }

    public static ApplicableRegionSet getRegions(Location location) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(location));
    }

    public static List<ProtectedRegion> getRegionsByFlag(World world, StateFlag stateFlag) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)).getRegions().values().stream().filter((protectedRegion) -> protectedRegion.getFlag(stateFlag) == StateFlag.State.ALLOW).toList();
    }

    public static boolean testRegionFlag(Player player, Location location, StateFlag stateFlag) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), stateFlag);
    }
}
