package com.noloverme.npromo.managers;

import com.noloverme.npromo.NPromo;
import com.noloverme.npromo.data.Database;
import com.noloverme.npromo.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class CodeManager {

    private final NPromo plugin;
    private final Database database;

    public CodeManager(NPromo plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    public boolean codeExistsInConfig(String code) {
        return plugin.getCodes().isConfigurationSection(code);
    }

    public void activateCode(Player player, String code) {
        if (!codeExistsInConfig(code)) {
            ChatUtil.sendMessage(player, "invalid-code");
            return;
        }

        ConfigurationSection codeSection = plugin.getCodes().getConfigurationSection(code);
        assert codeSection != null;

        int limit = codeSection.getInt("limit", -1);
        if (limit != -1 && database.getCodeActivations(code) >= limit) {
            ChatUtil.sendMessage(player, "code-limit-reached");
            return;
        }

        String expiration = codeSection.getString("expires");
        if (expiration != null && !expiration.isEmpty()) {

        }

        String mode = plugin.getConfig().getString("mode", "UNLIMITED");
        if ("UNLIMITED".equalsIgnoreCase(mode)) {
            if (database.hasActivatedCode(player.getUniqueId(), code)) {
                ChatUtil.sendMessage(player, "code-already-activated");
                return;
            }
        } else if ("MEDIA".equalsIgnoreCase(mode)) {
            if (database.hasActivatedAnyCode(player.getUniqueId())) {
                ChatUtil.sendMessage(player, "media-code-already-activated");
                return;
            }
            if (plugin.getConfig().getBoolean("media-settings.check-ip", true)) {
                if (database.hasIpActivatedAnyCode(player.getAddress().getAddress().getHostAddress())) {
                    ChatUtil.sendMessage(player, "media-code-already-activated");
                    return;
                }
            }
        }

        database.activateCode(player.getUniqueId(), player.getAddress().getAddress().getHostAddress(), code);

        String successMessage = codeSection.getString("successfully");
        if (successMessage != null && !successMessage.isEmpty()) {
            player.sendMessage(ChatUtil.color(successMessage.replace("%player%", player.getName())));
        } else {
            ChatUtil.sendMessage(player, "code-activated", "{code}", code);
        }

        List<String> commands = codeSection.getStringList("cmds");
        for (String cmd : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }
    }
}
