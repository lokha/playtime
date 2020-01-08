package ua.lokha.playtime.bukkit;

import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Metadata {
    @Getter
    private static Map<Player, Metadata> players = new ConcurrentHashMap<>();

    private final Player player;

    private long lastMove = System.currentTimeMillis();
    private boolean afk = false;

    public Metadata(Player player) {
        this.player = player;
    }

    public static Metadata get(Player player) {
        return players.computeIfAbsent(player, Metadata::new);
    }

    public void setAfk(boolean afk) {
        if (afk) {
            Main.getInstance().getLogger().info("Игрок " + player.getName() + " ушел в AFK.");
        } else {
            Main.getInstance().getLogger().info("Игрок " + player.getName() + " вышел из AFK.");
        }
        this.afk = afk;
        player.sendPluginMessage(Main.getInstance(), "playtime:playtime", ("afk☭" + afk).getBytes(StandardCharsets.UTF_8));
    }

    public void setLastMove(long lastMove) {
        this.lastMove = lastMove;

        if (afk) {
            this.setAfk(false);
        }
    }
}
