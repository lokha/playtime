package ua.lokha.playtime.bungee;

import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import ua.lokha.playtime.Common;
import ua.lokha.playtime.Config;
import ua.lokha.playtime.Dao;
import ua.lokha.playtime.Try;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Main extends Plugin {
    private static Main instance;

    @Getter
    private Config customConfig;

    @Getter
    private Set<String> servers;
    private ScheduledTask syncDb;

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

        BungeeCord.getInstance().registerChannel("playtime");

        BungeeCord.getInstance().getPluginManager().registerListener(this, new Events());
    }

    public void reloadCustomConfig() {
        this.getLogger().info("Reload config...");
        customConfig = new Config(new File(this.getDataFolder(), "config.yml"));
        customConfig.setDescription("/adminplaytime reload - перезагрузить этот конфиг, перезагружаются все параметры (право command.adminplaytime)");

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

        if (syncDb != null) {
            this.getLogger().info("Останавливаем таймер обновления времени.");
            Try.ignore(syncDb::cancel);
            syncDb = null;
        }
        int syncInterval = customConfig.getOrSetNumber("sync-interval-seconds", 60).intValue();
        this.getLogger().info("Запускаем таймер обновления времени каждые " + syncInterval + " секунд.");
        syncDb = BungeeCord.getInstance().getScheduler().schedule(this, () -> {
            for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
                Events.updateTimeDb(Metadata.get(player));
            }
        }, 0, syncInterval, TimeUnit.SECONDS);

        this.getLogger().info("Plugin successfully reload.");
    }

    public static Main getInstance() {
        return instance;
    }
}
