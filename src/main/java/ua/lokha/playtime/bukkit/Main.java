package ua.lokha.playtime.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

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
        this.getLogger().info(this.getName() + " enabled");
    }

    public static Main getInstance() {
        return instance;
    }
}
