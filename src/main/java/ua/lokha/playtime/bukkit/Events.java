package ua.lokha.playtime.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    @EventHandler
    public void on(PlayerMoveEvent event) {
        Metadata.get(event.getPlayer()).setLastMove(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerQuitEvent event) {
        Metadata.getPlayers().remove(event.getPlayer());
    }
}
