package ua.lokha.playtime.bungee;

import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import ua.lokha.playtime.MapUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Metadata {
    @Getter
    private static Map<ProxiedPlayer, Metadata> players = new ConcurrentHashMap<>();

    private final ProxiedPlayer player;
    private String currentServer;
    private Map<String, OnlineInfo> onlineUpdate = new HashMap<>(MapUtils.calculateExpectedSize(Main.getInstance().getServers().size()));
    private boolean afk = false;

    public Metadata(ProxiedPlayer player) {
        this.player = player;
    }

    public static Metadata get(ProxiedPlayer player) {
        return players.computeIfAbsent(player, Metadata::new);
    }

    public void updateCurrentServer() {
        if (currentServer != null) {
            OnlineInfo onlineInfo = onlineUpdate.computeIfAbsent(currentServer, OnlineInfo::new);
            LocalDateTime now = LocalDateTime.now();
            if (!afk) {
                int between = (int) ChronoUnit.SECONDS.between(onlineInfo.getLastUpdate(), now);
                onlineInfo.setSeconds(onlineInfo.getSeconds() + between);
            }
            onlineInfo.setLastUpdate(now);
        }
    }

    public void setCurrentServer(String currentServer) {
        this.afk = false;
        this.currentServer = currentServer;
        if (currentServer != null) {
            OnlineInfo onlineInfo = onlineUpdate.computeIfAbsent(currentServer, OnlineInfo::new);
            onlineInfo.setLastUpdate(LocalDateTime.now());
        }
    }

    public void setAfk(boolean afk) {
        if (afk == this.afk) {
            return;
        }
        if (afk) {
            Main.getInstance().getLogger().info("Игрок " + player.getName() + " ушел в AFK.");
        } else {
            Main.getInstance().getLogger().info("Игрок " + player.getName() + " вышел из AFK.");
        }
        this.updateCurrentServer();
        this.afk = afk;
    }

    @Data
    public static class OnlineInfo {
        private String serverName;
        private int seconds = 0;
        private LocalDateTime lastUpdate = LocalDateTime.now();

        public OnlineInfo(String serverName) {
            this.serverName = serverName;
        }
    }
}
