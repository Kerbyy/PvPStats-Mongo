package me.indite.stats;

import lombok.Getter;
import me.indite.stats.backend.MongoBackend;
import me.indite.stats.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Stats extends JavaPlugin {

    @Getter private static Stats instance;
    @Getter private MongoBackend mongoBackend;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.mongoBackend = new MongoBackend();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        mongoBackend.close();
        instance = null;
    }
}
