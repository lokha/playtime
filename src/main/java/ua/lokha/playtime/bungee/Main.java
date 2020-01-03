package ua.lokha.playtime.bungee;

import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import ua.lokha.playtime.Common;
import ua.lokha.playtime.Config;
import ua.lokha.playtime.Dao;

import java.io.File;

public class Main extends Plugin {
    private static Main instance;

    @Getter
    private Config customConfig;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Common.setLogger(this.getLogger());
        this.reloadCustomConfig();

        PluginManager manager = BungeeCord.getInstance().getPluginManager();
        manager.registerCommand(this, new AdminPlayTimeCommand());
    }

    public void reloadCustomConfig() {
        this.getLogger().info("Reload config...");
        customConfig = new Config(new File(this.getDataFolder(), "config.yml"));

        this.getLogger().info("Reinit database connection...");
        Dao.getInstance().stop();
        try {
            Dao.getInstance().init(customConfig, this.getClass().getClassLoader());
        } catch (Exception e) {
            Common.getLogger().severe("Соединение с базой не установлено, настройте данные от базы и перезагрузите плагин /adminplaytime reload");
            e.printStackTrace();
        }

        this.getLogger().info("Plugin successfully reload.");
    }

    public static Main getInstance() {
        return instance;
    }
}
