package me.indite.stats.listeners;

import lombok.NonNull;
import me.indite.stats.profiles.Death;
import me.indite.stats.profiles.Kill;
import me.indite.stats.profiles.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPreLogin(final AsyncPlayerPreLoginEvent e) {
        new Profile(e.getUniqueId(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        final Profile profile = Profile.getByPlayer(e.getPlayer());
        profile.setName(e.getPlayer().getName());
        profile.save();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        this.handleDisconnect(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(PlayerKickEvent e) {
        this.handleDisconnect(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e) {
        final Profile deadProfile = Profile.getByPlayer(e.getEntity());
        final Profile killerProfile = Profile.getByPlayer(e.getEntity().getKiller());

        final Player killer = e.getEntity().getKiller();
        final Player dead = e.getEntity();


        if (killer != null) {
            Kill killObj = new Kill(killer.getUniqueId(), dead.getUniqueId(), System.currentTimeMillis());
            Death deathObj = new Death(killer.getUniqueId(), dead.getUniqueId(), System.currentTimeMillis());

            killerProfile.getKills().add(killObj);
            deadProfile.getDeaths().add(deathObj);
            deadProfile.save();
            killerProfile.save();
        } else {
            Death deathObj = new Death(null, dead.getUniqueId(), System.currentTimeMillis());
            deadProfile.getDeaths().add(deathObj);
            deadProfile.save();

        }
    }

    private void handleDisconnect(@NonNull final Player player) {
        final Profile profile = Profile.getByPlayer(player);
        profile.save();
        Profile.getProfiles().remove(profile.getUuid().toString());
    }
}
