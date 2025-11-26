package com.noloverme.npromo;

import com.noloverme.npromo.commands.NCodeCommand;
import com.noloverme.npromo.commands.NCodesCommand;
import com.noloverme.npromo.data.Database;
import com.noloverme.npromo.data.H2Database;
import com.noloverme.npromo.data.MySQLDatabase;
import com.noloverme.npromo.hooks.PlaceholderAPIHook;
import com.noloverme.npromo.managers.CodeManager;
import com.noloverme.npromo.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class NPromo extends JavaPlugin {

    private static NPromo instance;
    private CodeManager codeManager;
    private Database database;
    private FileConfiguration codesConfig;

    @Override
    public void onEnable() {
        instance = this;

        if (!isPaper() || !isCompatibleVersion()) {
            getLogger().severe("Плагин не был инициализирован из-за возможной несовместимости версий. Пожалуйста, используйте ядро Paper или PurPur и версию 1.16+");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("codes.yml", false);
        reloadCodes();
        ChatUtil.reload();

        try {
            setupDatabase();
        } catch (SQLException e) {
            getLogger().severe("Не удалось подключиться к базе данных! Отключение плагина.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        codeManager = new CodeManager(this, database);

        PluginCommand ncodeCommand = getCommand("ncode");
        if (ncodeCommand != null) {
            ncodeCommand.setExecutor(new NCodeCommand(this));
        }

        PluginCommand ncodesCommand = getCommand("ncodes");
        if (ncodesCommand != null) {
            ncodesCommand.setExecutor(new NCodesCommand(this));
            ncodesCommand.setTabCompleter(new NCodesCommand(this));
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        getLogger().info("NPromo has been enabled!");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        getLogger().info("NPromo has been disabled!");
    }

    private boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isCompatibleVersion() {
        String version = Bukkit.getServer().getBukkitVersion();
        return version.matches("1\\.(1[6-9]|[2-9][0-9])(\\..*)?");
    }

    private void setupDatabase() throws SQLException {
        String storageType = getConfig().getString("storage.type", "H2");
        if (storageType.equalsIgnoreCase("MYSQL")) {
            database = new MySQLDatabase(this);
        } else {
            database = new H2Database(this);
        }
        database.init();
    }

    public void reload() {
        reloadConfig();
        saveResource("messages.yml", false);
        ChatUtil.reload();
        reloadCodes();
        if (database != null) {
            database.close();
        }
        try {
            setupDatabase();
            codeManager = new CodeManager(this, database);
        } catch (SQLException e) {
            getLogger().severe("Не удалось переподключиться к базе данных во время перезагрузки!");
            e.printStackTrace();
        }
    }

    public void reloadCodes() {
        File codesFile = new File(getDataFolder(), "codes.yml");
        if (!codesFile.exists()) {
            saveResource("codes.yml", false);
        }
        codesConfig = YamlConfiguration.loadConfiguration(codesFile);
    }

    public static NPromo getInstance() {
        return instance;
    }

    public CodeManager getCodeManager() {
        return codeManager;
    }

    public Database getDatabase() {
        return database;
    }

    public FileConfiguration getCodes() {
        return codesConfig;
    }
}
