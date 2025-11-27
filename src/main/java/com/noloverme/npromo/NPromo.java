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
import java.util.Objects;

public final class NPromo extends JavaPlugin {

    private CodeManager codeManager;
    private Database database;
    private FileConfiguration codesConfig;
    private ChatUtil chatUtil;

    @Override
    public void onEnable() {
        if (!isPaper() || !isCompatibleVersion()) {
            getLogger().severe("Plugin not initialized due to possible version incompatibility. Please use Paper or PurPur and version 1.16+");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("codes.yml", false);
        reloadCodes();

        this.chatUtil = new ChatUtil(this);
        this.chatUtil.reload();

        connectToDatabase();
    }

    @Override
    public void onDisable() {
        if (this.database != null) {
            this.database.close();
        }
        getLogger().info("NPromo has been disabled!");
    }

    private void connectToDatabase() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                setupDatabase();
                Bukkit.getScheduler().runTask(this, this::onDatabaseConnected);
            } catch (SQLException e) {
                getLogger().severe("Could not connect to the database! Disabling plugin.");
                e.printStackTrace();
                Bukkit.getScheduler().runTask(this, () -> getServer().getPluginManager().disablePlugin(this));
            }
        });
    }

    private void onDatabaseConnected() {
        this.codeManager = new CodeManager(this, this.database);
        registerCommands();
        registerHooks();
        getLogger().info("NPromo has been enabled!");
    }

    private void registerCommands() {
        PluginCommand ncodeCommand = getCommand("ncode");
        Objects.requireNonNull(ncodeCommand, "Command 'ncode' is not registered in plugin.yml");
        ncodeCommand.setExecutor(new NCodeCommand(this));

        PluginCommand ncodesCommand = getCommand("ncodes");
        Objects.requireNonNull(ncodesCommand, "Command 'ncodes' is not registered in plugin.yml");
        NCodesCommand commandExecutor = new NCodesCommand(this);
        ncodesCommand.setExecutor(commandExecutor);
        ncodesCommand.setTabCompleter(commandExecutor);
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook(this).register();
        }
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
        return Bukkit.getServer().getBukkitVersion().matches("1\\.(1[6-9]|[2-9][0-9])(\\..*)?");
    }

    private void setupDatabase() throws SQLException {
        String storageType = getConfig().getString("storage.type", "H2");
        if ("MYSQL".equalsIgnoreCase(storageType)) {
            this.database = new MySQLDatabase(this);
        } else {
            this.database = new H2Database(this);
        }
        this.database.init();
    }

    public void reloadPlugin() {
        reloadConfig();
        saveResource("messages.yml", false);
        this.chatUtil.reload();
        reloadCodes();

        if (this.database != null) {
            this.database.close();
        }

        connectToDatabase();
    }

    public void reloadCodes() {
        File codesFile = new File(getDataFolder(), "codes.yml");
        if (!codesFile.exists()) {
            saveResource("codes.yml", false);
        }
        this.codesConfig = YamlConfiguration.loadConfiguration(codesFile);
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

    public ChatUtil getChatUtil() {
        return chatUtil;
    }
}
