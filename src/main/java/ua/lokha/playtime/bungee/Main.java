package ua.lokha.playtime.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {
    private static Main instance;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getLogger().info(this.getDescription().getName() + " enabled");
    }

    public static Main getInstance() {
        return instance;
    }
}
