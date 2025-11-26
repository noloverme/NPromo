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
    private static FileConfiguration messagesConfig;

    public static void reload() {
        File messagesFile = new File(NPromo.getInstance().getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            NPromo.getInstance().saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String color(String message) {
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

    public static void sendMessage(CommandSender sender, String path, String... replacements) {
        if (messagesConfig == null) {
            sender.sendMessage(ChatColor.RED + "Messages configuration is not loaded.");
            return;
        }
        String message = messagesConfig.getString(path, "&cMessage not found: " + path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(color(messagesConfig.getString("prefix", "") + message));
    }
}
