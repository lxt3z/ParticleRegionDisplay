package su.lxt3z.particleregiondisplay;

import su.lxt3z.particleregiondisplay.commands.ParticleToggleCommand;
import su.lxt3z.particleregiondisplay.listeners.PlayerListener;
import su.lxt3z.particleregiondisplay.particles.ParticleManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private ParticleManager particleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        particleManager = new ParticleManager(this, config);

        getServer().getPluginManager().registerEvents(new PlayerListener(particleManager), this);
        Objects.requireNonNull(getCommand("toggleparticles")).setExecutor(new ParticleToggleCommand(particleManager));
        getLogger().info("RegionParticleDisplay has been enabled!");
    }

    @Override
    public void onDisable() {
        particleManager.stopAllTasks();
        getLogger().info("RegionParticleDisplay has been disabled!");
    }
}