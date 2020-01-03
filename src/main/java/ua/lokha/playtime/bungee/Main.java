package ua.lokha.playtime.bungee;

import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import ua.lokha.playtime.Common;
import ua.lokha.playtime.Config;
import ua.lokha.playtime.Dao;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Plugin {
    private static Main instance;

    @Getter
    private Config customConfig;

    @Getter
    private Set<String> servers;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Common.setLogger(this.getLogger());
        this.reloadCustomConfig();

        PluginManager manager = BungeeCord.getInstance().getPluginManager();
        manager.registerCommand(this, new AdminPlayTimeCommand());
        manager.registerCommand(this, new PlayTimeCommand());

        BungeeCord.getInstance().getPluginManager().registerListener(this, new Events());
    }

    public void reloadCustomConfig() {
        this.getLogger().info("Reload config...");
        customConfig = new Config(new File(this.getDataFolder(), "config.yml"));

        Message.getMessages().forEach(message -> message.init(customConfig));

        servers = new HashSet<>(customConfig.getOrSet("servers-list", Collections.emptyList()));

        this.getLogger().info("Reinit database connection...");
        Dao.getInstance().stop();
        try {
            Dao.getInstance().init(customConfig, this.getClass().getClassLoader());

            Dao.getInstance().createServerColumns(servers);
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
