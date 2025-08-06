package su.lxt3z.particleregiondisplay.listeners;

import su.lxt3z.particleregiondisplay.particles.ParticleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ParticleManager particleManager;

    public PlayerListener(ParticleManager particleManager) {
        this.particleManager = particleManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        particleManager.startParticleTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        particleManager.stopParticleTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        particleManager.updateParticleTask(event.getPlayer());
    }
}