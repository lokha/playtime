package ua.lokha.playtime;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("SqlNoDataSourceInspection")
public class Dao {

    @Getter
    private static Dao instance = new Dao();

    @Getter
    private MysqlDataSource dataSource;
    private Connection connection;

    @Getter
    private ExecutorService service;
    private String tableName;

    public void init(Config config, ClassLoader classLoader) {
        this.stop();
        String host = config.getOrSet("database-settings.host", "localhost");
        String database = config.getOrSet("database-settings.database", "local_db");
        int port = config.getOrSetNumber("database-settings.port", 3306).intValue();
        dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        dataSource.setUser(config.getOrSet("database-settings.username", "admin"));
        dataSource.setPassword(config.getOrSet("database-settings.password", ""));

        tableName = config.getOrSet("database-settings.table-name", "playtime_manager");
        this.initTable(tableName);

        service = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("PlayTime Database Thread %d").build());
    }

    @SneakyThrows
    private void initTable(String tableName) {
        Connection connection = this.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                        "    `username` VARCHAR(16) NOT NULL," +
                        "    PRIMARY KEY (`username`)" +
                        ")" +
                        "COLLATE='utf8_general_ci';")) {
            statement.execute();
        }
    }

    @SneakyThrows
    public void createServerColumns(Collection<String> servers) {
        Connection connection = this.getConnection();
        List<String> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SHOW COLUMNS FROM " + tableName);
             ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                columns.add(set.getString("Field"));
            }
        }

        for (String server : servers) {
            if (!columns.contains(server)) {
                Common.getLogger().info("Создаем колонку для сервера " + server);
                try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + tableName + "`" +
                        "ADD COLUMN `" + server + "` INT NOT NULL DEFAULT '0' AFTER `username`;")) {
                    statement.execute();
                }
                try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + tableName + "`" +
                        "ADD INDEX `" + server + "` (`" + server + "`);")) {
                    statement.execute();
                }
            }
        }
    }

    public void stop() {
        if (service != null) {
            Common.getLogger().info("Shutdown sql executor service...");
            Try.ignore(service::shutdown);
            service = null;
        }

        if (connection != null) {
            Common.getLogger().info("Shutdown database connection service...");
            Try.ignore(connection::close);
            connection = null;
        }
        dataSource = null;
    }

    @SneakyThrows
    private synchronized Connection getConnection() {
        if (connection == null || connection.isClosed()) {
            connection = dataSource.getConnection();
        }
        return connection;
    }

    @SneakyThrows
    public void update(String name, Set<String> servers, Map<String, Integer> addOnlineSeconds) {
        if (addOnlineSeconds.isEmpty()) {
            return;
        }

        Connection connection = this.getConnection();
        Map<String, Integer> onlineSeconds = null;
        try (PreparedStatement statement = connection.prepareStatement("select " + String.join(", ", servers) + " from " + tableName + " " +
                "where `username` = ?")) {
            statement.setString(1, name);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    onlineSeconds = new HashMap<>(MapUtils.calculateExpectedSize(servers.size()));
                    for (String server : servers) {
                        onlineSeconds.put(server, set.getInt(server));
                    }
                }
            }
        }

        int sumSeconds = 0;
        if (onlineSeconds != null) {
            for (String server : servers) {
                sumSeconds += onlineSeconds.getOrDefault(server, 0) + addOnlineSeconds.getOrDefault(server, 0);
            }
        } else {
            for (String server : servers) {
                sumSeconds += addOnlineSeconds.getOrDefault(server, 0);
            }
        }

        if (onlineSeconds != null) {
            StringBuilder values = new StringBuilder();
            addOnlineSeconds.forEach((serverName, seconds) -> {
                if (values.length() > 0) {
                    values.append(", ");
                }
                values.append("`").append(serverName).append("` = `").append(serverName).append("` + ").append(seconds);
            });
            String sql = "UPDATE " + tableName + " SET " + values.toString() + " WHERE `username` = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        } else {
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO `" + tableName + "` (`username`");
            for (String serverName : servers) {
                sqlBuilder.append(", `").append(serverName).append("`");
            }
            sqlBuilder.append(") VALUES (?");
            for (String serverName : servers) {
                sqlBuilder.append(", ").append(addOnlineSeconds.getOrDefault(serverName, 0));
            }
            sqlBuilder.append(")");
            String sql = sqlBuilder.toString();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.execute();
            }
        }
    }

    @SneakyThrows
    public UserInfo getInfo(String username, Set<String> servers) {
        UserInfo info = new UserInfo();
        info.setUsername(username);

        Connection connection = this.getConnection();
        Map<String, Integer> onlineSeconds = new HashMap<>(MapUtils.calculateExpectedSize(servers.size()));
        try (PreparedStatement statement = connection.prepareStatement("select " + String.join(", ", servers) + " from " + tableName + " " +
                "where `username` = ?")) {
            statement.setString(1, username);
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    for (String server : servers) {
                        onlineSeconds.put(server, 0);
                    }
                } else {
                    for (String server : servers) {
                        onlineSeconds.put(server, set.getInt(server));
                    }
                }
            }
        }
        info.setOnlineSeconds(onlineSeconds);

        int sumSeconds = info.calcSumSeconds();

        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) as count_up FROM " + tableName + " WHERE (" +
                servers.stream()
                        .map(serverName -> "`" + serverName + "`")
                        .collect(Collectors.joining(" + "))
                + ") > ?")) {
            statement.setInt(1, sumSeconds);
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    throw new RuntimeException("rows empty");
                }

                info.setPlace(set.getInt("count_up") + 1);
            }
        }

        return info;
    }

    public static void async(Consumer<Dao> consumer) {
        ExecutorService service = instance.getService();
        if (service == null) {
            Common.getLogger().warning("Соединение с базой не установлено, настройте данные от базы и перезагрузите плагин /adminplaytime reload");
        } else {
            service.submit(() -> {
                try {
                    consumer.accept(instance);
                } catch (Exception e) {
                    Common.getLogger().severe("SQL error " + consumer);
                    e.printStackTrace();
                }
            });
        }
    }

    @Data
    public static class UserInfo {
        private String username;
        private int place;
        private Map<String, Integer> onlineSeconds;

        public int calcSumSeconds() {
            return onlineSeconds.values().stream().mapToInt(Integer::intValue).sum();
        }
    }
}
