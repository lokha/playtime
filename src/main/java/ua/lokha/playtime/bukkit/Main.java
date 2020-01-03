package ua.lokha.playtime.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import ua.lokha.playtime.Common;

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
    }

    public static Main getInstance() {
        return instance;
    }
}
