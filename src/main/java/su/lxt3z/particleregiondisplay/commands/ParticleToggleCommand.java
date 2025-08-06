package su.lxt3z.particleregiondisplay.commands;

import su.lxt3z.particleregiondisplay.particles.ParticleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ParticleToggleCommand implements CommandExecutor {

    private final ParticleManager particleManager;

    public ParticleToggleCommand(ParticleManager particleManager) {
        this.particleManager = particleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        boolean newState = particleManager.toggleParticles(player);
        player.sendMessage("Region Particle " + (newState ? "enabled" : "disabled") + "!");
        return true;
    }
}