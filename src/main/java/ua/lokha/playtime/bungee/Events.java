package ua.lokha.playtime.bungee;

import lombok.var;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import ua.lokha.playtime.Dao;
import ua.lokha.playtime.MapUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Events implements Listener {

    @EventHandler
    public void on(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Metadata metadata = Metadata.get(event.getPlayer());

        metadata.updateCurrentServer();

        Server server = player.getServer();
        if (server != null) {
            String serverName = server.getInfo().getName();
            if (Main.getInstance().getServers().contains(serverName)) {
                metadata.setCurrentServer(serverName);
            } else {
                metadata.setCurrentServer(null);
            }
        }
    }

    @EventHandler
    public void on(PlayerDisconnectEvent event) {
        Metadata metadata = Metadata.get(event.getPlayer());
        updateTimeDb(metadata);
    }

    @EventHandler
    public void on(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer) {
            if (event.getTag().equals("playtime:playtime")) {
                Metadata metadata = Metadata.get((ProxiedPlayer) event.getReceiver());
                String message = new String(event.getData(), StandardCharsets.UTF_8);
                String[] data = message.split("☭");
                if (data[0].equals("afk")) {
                    boolean afk = Boolean.parseBoolean(data[1]);
                    metadata.setAfk(afk);
                }
            }
        }
    }

    public static void updateTimeDb(Metadata metadata) {
        Dao.async(dao -> {
            synchronized (metadata) {
                metadata.updateCurrentServer();
                var changes = metadata.getOnlineUpdate().values();
                Map<String, Integer> update = new HashMap<>(MapUtils.calculateExpectedSize(changes.size()));
                for (var change : changes) {
                    if (Main.getInstance().getServers().contains(change.getServerName()) && change.getSeconds() > 0) {
                        update.put(change.getServerName(), change.getSeconds());
                    }
                    change.setSeconds(0);
                }

                if (!update.isEmpty()) {
                    dao.update(metadata.getPlayer().getName(), Main.getInstance().getServers(), update);
                }
            }
        });
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onMax(PlayerDisconnectEvent event) {
        Metadata.getPlayers().remove(event.getPlayer());
    }
}
