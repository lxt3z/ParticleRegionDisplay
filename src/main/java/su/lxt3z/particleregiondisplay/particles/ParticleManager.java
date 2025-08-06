package su.lxt3z.particleregiondisplay.particles;

import su.lxt3z.particleregiondisplay.Main;

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sk89q.worldedit.bukkit.BukkitAdapter;

public class ParticleManager {

    private final Main plugin;
    private final Map<UUID, BukkitRunnable> playerParticleTasks = new HashMap<>();
    private final Map<UUID, Boolean> particleToggles = new HashMap<>();
    private final double particleHeight;
    private final double particleDistance;
    private final int particleRange;
    private final double boundaryThreshold;
    private final long updateInterval;
    private final Particle.DustOptions safeParticleOptions;
    private final Particle.DustOptions pvpParticleOptions;

    public ParticleManager(Main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.particleHeight = config.getDouble("particle.height", 1.0);
        this.particleDistance = config.getDouble("particle.distance", 0.5);
        this.particleRange = config.getInt("particle.range", 20);
        this.boundaryThreshold = config.getDouble("particle.boundary-threshold", 1.0);
        this.updateInterval = config.getLong("particle.update-interval", 10L);
        this.safeParticleOptions = new Particle.DustOptions(
                org.bukkit.Color.fromRGB(
                        config.getInt("particle.safe-color.red", 0),
                        config.getInt("particle.safe-color.green", 255),
                        config.getInt("particle.safe-color.blue", 0)
                ), 1.0f);
        this.pvpParticleOptions = new Particle.DustOptions(
                org.bukkit.Color.fromRGB(
                        config.getInt("particle.pvp-color.red", 255),
                        config.getInt("particle.pvp-color.green", 0),
                        config.getInt("particle.pvp-color.blue", 0)
                ), 1.0f);
    }

    public void startParticleTask(Player player) {
        UUID playerId = player.getUniqueId();
        stopParticleTask(player);
        particleToggles.putIfAbsent(playerId, true);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (particleToggles.getOrDefault(playerId, true)) {
                    displayRegionParticles(player);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, updateInterval);
        playerParticleTasks.put(playerId, task);
    }

    public void stopParticleTask(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable task = playerParticleTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void stopAllTasks() {
        playerParticleTasks.values().forEach(BukkitRunnable::cancel);
        playerParticleTasks.clear();
    }

    public void updateParticleTask(Player player) {
        if (!playerParticleTasks.containsKey(player.getUniqueId())) {
            startParticleTask(player);
        }
    }

    public boolean toggleParticles(Player player) {
        UUID playerId = player.getUniqueId();
        boolean newState = !particleToggles.getOrDefault(playerId, true);
        particleToggles.put(playerId, newState);
        return newState;
    }

    private void displayRegionParticles(Player player) {
        Location playerLocation = player.getLocation();
        // Convert Bukkit World to WorldEdit World
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
        // Using Vector3 for WorldGuard 7.0.9
        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(world).getApplicableRegions(Vector3.at(
                        playerLocation.getX(),
                        playerLocation.getY(),
                        playerLocation.getZ()
                ).toBlockPoint());

        for (ProtectedRegion region : regions) {
            boolean isPvpAllowed = region.getFlag(Flags.PVP) == StateFlag.State.ALLOW;
            Particle.DustOptions dustOptions = isPvpAllowed ? pvpParticleOptions : safeParticleOptions;

            for (Location boundaryPoint : getRegionBoundaryPoints(region, playerLocation)) {
                if (boundaryPoint.distance(playerLocation) <= particleRange) {
                    player.spawnParticle(Particle.REDSTONE, boundaryPoint, 1, dustOptions);
                }
            }
        }
    }

    private Iterable<Location> getRegionBoundaryPoints(ProtectedRegion region, Location playerLocation) {
        Vector3 min = region.getMinimumPoint().toVector3();
        Vector3 max = region.getMaximumPoint().toVector3();
        // Use getX(), getY(), getZ() for WorldGuard 7.0.9 compatibility
        Location minLoc = new Location(playerLocation.getWorld(), min.getX(), min.getY(), min.getZ());
        Location maxLoc = new Location(playerLocation.getWorld(), max.getX(), max.getY(), max.getZ());

        java.util.List<Location> boundaryPoints = new ArrayList<>();
        double y = playerLocation.getY() + particleHeight;

        // North boundary (z = minLoc.getZ())
        for (double x = minLoc.getX(); x <= maxLoc.getX(); x += particleDistance) {
            boundaryPoints.add(new Location(playerLocation.getWorld(), x, y, minLoc.getZ()));
        }

        // South boundary (z = maxLoc.getZ())
        for (double x = minLoc.getX(); x <= maxLoc.getX(); x += particleDistance) {
            boundaryPoints.add(new Location(playerLocation.getWorld(), x, y, maxLoc.getZ()));
        }

        // West boundary (x = minLoc.getX())
        for (double z = minLoc.getZ(); z <= maxLoc.getZ(); z += particleDistance) {
            boundaryPoints.add(new Location(playerLocation.getWorld(), minLoc.getX(), y, z));
        }

        // East boundary (x = maxLoc.getX())
        for (double z = minLoc.getZ(); z <= maxLoc.getZ(); z += particleDistance) {
            boundaryPoints.add(new Location(playerLocation.getWorld(), maxLoc.getX(), y, z));
        }

        return boundaryPoints;
    }

    private boolean isNearBoundary(double value, double min, double max, double threshold) {
        return Math.abs(value - min) <= threshold || Math.abs(value - max) <= threshold;
    }

}
