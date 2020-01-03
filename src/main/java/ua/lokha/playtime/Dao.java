package ua.lokha.playtime;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
                        "    `place` INT NOT NULL," +
                        "    PRIMARY KEY (`username`)" +
                        ")" +
                        "COLLATE='utf8_general_ci';")) {
            statement.execute();
        }
    }

    @SneakyThrows
    public void createServerColumns(List<String> servers) {
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
                        "ADD COLUMN `" + server + "` INT NOT NULL DEFAULT '0' AFTER `place`;")) {
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
}
