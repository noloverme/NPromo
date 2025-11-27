package com.noloverme.npromo.utils;

import com.noloverme.npromo.NPromo;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final NPromo plugin;
    private FileConfiguration messagesConfig;
    private String prefix;

    public ChatUtil(NPromo plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        this.prefix = color(this.messagesConfig.getString("prefix", ""));
    }

    public String color(String message) {
        if (message == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + group).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public void sendMessage(CommandSender sender, String path, String... replacements) {
        if (this.messagesConfig == null) {
            sender.sendMessage(ChatColor.RED + "Messages configuration is not loaded.");
            return;
        }
        String message = this.messagesConfig.getString(path, "&cMessage not found: " + path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(this.prefix + color(message));
    }
}
