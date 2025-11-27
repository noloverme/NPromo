package com.noloverme.npromo.data;

import com.noloverme.npromo.NPromo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class H2Database implements Database {

    private final NPromo plugin;
    private final Executor asyncExecutor;
    private HikariDataSource dataSource;

    public H2Database(NPromo plugin) {
        this.plugin = plugin;
        this.asyncExecutor = runnable -> this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    @Override
    public void init() throws SQLException {
        File dbFile = new File(this.plugin.getDataFolder(), "data.db");
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:" + dbFile.getAbsolutePath());
        config.setUsername("sa");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);

        try (Connection connection = this.dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS activations (id INT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), ip VARCHAR(45), code VARCHAR(255))");
        }
    }

    @Override
    public void close() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
        }
    }

    private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.asyncExecutor);
    }

    private CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, this.asyncExecutor);
    }

    @Override
    public CompletableFuture<Void> activateCode(UUID uuid, String ip, String code) {
        return runAsync(() -> {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("INSERT INTO activations (uuid, ip, code) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, ip);
                ps.setString(3, code);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasActivatedCode(UUID uuid, String code) {
        return supplyAsync(() -> {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM activations WHERE uuid = ? AND code = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, code);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasActivatedAnyCode(UUID uuid) {
        return supplyAsync(() -> {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM activations WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasIpActivatedAnyCode(String ip) {
        return supplyAsync(() -> {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM activations WHERE ip = ?")) {
                ps.setString(1, ip);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getCodeActivations(String code) {
        return supplyAsync(() -> {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM activations WHERE code = ?")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        });
    }

    @Override
    public CompletableFuture<List<String>> getActivatedCodes(UUID uuid) {
        return supplyAsync(() -> {
            List<String> codes = new ArrayList<>();
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT code FROM activations WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        codes.add(rs.getString("code"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return codes;
        });
    }

    @Override
    public void importData(String fileName) {
        // Not implemented
    }

    @Override
    public void exportData(String fileName) {
        // Not implemented
    }
}
