package ua.lokha.playtime.bungee;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import ua.lokha.playtime.Config;
import ua.lokha.playtime.MessageUtils;

import java.util.Arrays;
import java.util.List;

public enum Message {
    HELP("§e===============[PlayTime]===============\n" +
            "§3/playtime §7- посмотреть свою статистику\n" +
            "§3/playtime [ник] §7- посмотреть статистику игрока"),
    HEADER("&7Статистика игрока &e{name}. \n &7Всего наиграно &e{total_playtime}&7 &8(&7{place}&8) \n \n &7Детальная статистика:"),
    HOURS("часов"),
    MINUTES("минут"),
    PER_SERVER("&7- &e{server} &8(&7{playtime}&8)"),
    ;


    @Getter
    private static List<Message> messages = Arrays.asList(values());

    @Getter
    private String defaultValue;
    private String value;

    Message(String defaultValue) {
        this.defaultValue = this.value = MessageUtils.colored(StringUtils.stripToEmpty(defaultValue));
    }

    public void init(Config config) {
        this.value = MessageUtils.colored(config.getOrSet("locale." + this.name(), defaultValue));
    }

    public String get(String... replaced) {
        return MessageUtils.replaced(this.value, replaced);
    }

    public String get() {
        return this.value;
    }
}
