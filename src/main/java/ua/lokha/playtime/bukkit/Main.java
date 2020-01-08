package ua.lokha.playtime.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ua.lokha.playtime.Common;
import ua.lokha.playtime.Config;
import ua.lokha.playtime.Try;

import java.io.File;

/**
 * Главный класс плагина
 */
public class Main extends JavaPlugin {

    private static Main instance;

    @Getter
    private Config customConfig;

    private BukkitTask task;
    private int afkThreshold;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Common.setLogger(this.getLogger());
        this.reloadCustomConfig();

        this.getCommand("adminplaytime").setExecutor(new AdminPlayTimeCommand());

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "playtime:playtime");

        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }

    public void reloadCustomConfig() {
        this.getLogger().info("Reload config...");
        customConfig = new Config(new File(this.getDataFolder(), "config.yml"));
        customConfig.setDescription("/adminplaytime reload - перезагрузить этот конфиг, перезагружаются все параметры (право command.adminplaytime)");

        if (task != null) {
            Try.ignore(task::cancel);
            task = null;
        }
        afkThreshold = customConfig.getOrSetNumber("afk-threshold-seconds", 60).intValue();
        task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            long current = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                Try.ignore(() -> {
                    Metadata metadata = Metadata.get(player);
                    if (!metadata.isAfk()) {
                        if (current - metadata.getLastMove() > (afkThreshold * 1000)) {
                            metadata.setAfk(true);
                        }
                    }
                });
            }
        }, 0, Math.max(1, afkThreshold / 2));

        this.getLogger().info("Plugin successfully reload.");
    }

    public static Main getInstance() {
        return instance;
    }
}
