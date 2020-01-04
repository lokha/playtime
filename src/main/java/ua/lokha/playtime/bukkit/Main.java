package ua.lokha.playtime.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ua.lokha.playtime.Common;
import ua.lokha.playtime.Try;

/**
 * Главный класс плагина
 */
public class Main extends JavaPlugin {

    private static Main instance;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Common.setLogger(this.getLogger());

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "playtime");

        Bukkit.getPluginManager().registerEvents(new Events(), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long current = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                Try.ignore(() -> {
                    Metadata metadata = Metadata.get(player);
                    if (!metadata.isAfk()) {
                        if (current - metadata.getLastMove() > 30_000) {
                            metadata.setAfk(true);
                        }
                    }
                });
            }
        }, 0, 20 * 10);
    }

    public static Main getInstance() {
        return instance;
    }
}
