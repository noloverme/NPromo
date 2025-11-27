package com.noloverme.npromo.managers;

import com.noloverme.npromo.NPromo;
import com.noloverme.npromo.data.Database;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
        ConfigurationSection codeSection = plugin.getCodes().getConfigurationSection(code);
        if (codeSection == null) {
            plugin.getChatUtil().sendMessage(player, "invalid-code");
            return;
        }

        hasReachedActivationLimit(code, codeSection).thenAccept(limitReached -> {
            if (limitReached) {
                plugin.getChatUtil().sendMessage(player, "code-limit-reached");
                return;
            }

            canActivate(player, code).thenAccept(canActivate -> {
                if (canActivate) {
                    executeActivation(player, code, codeSection);
                }
            });
        });
    }

    private CompletableFuture<Boolean> hasReachedActivationLimit(String code, ConfigurationSection codeSection) {
        int limit = codeSection.getInt("limit", -1);
        if (limit == -1) {
            return CompletableFuture.completedFuture(false);
        }
        return database.getCodeActivations(code).thenApply(activations -> activations >= limit);
    }

    private CompletableFuture<Boolean> canActivate(Player player, String code) {
        String mode = plugin.getConfig().getString("mode", "UNLIMITED");
        switch (mode.toUpperCase()) {
            case "UNLIMITED":
                return hasActivatedCode(player, code, "code-already-activated");
            case "MEDIA":
                return canActivateMediaCode(player);
            default:
                return CompletableFuture.completedFuture(true);
        }
    }

    private CompletableFuture<Boolean> hasActivatedCode(Player player, String code, String messageKey) {
        return database.hasActivatedCode(player.getUniqueId(), code).thenApply(hasActivated -> {
            if (hasActivated) {
                plugin.getChatUtil().sendMessage(player, messageKey);
                return false;
            }
            return true;
        });
    }

    private CompletableFuture<Boolean> canActivateMediaCode(Player player) {
        return database.hasActivatedAnyCode(player.getUniqueId()).thenCompose(hasActivated -> {
            if (hasActivated) {
                plugin.getChatUtil().sendMessage(player, "media-code-already-activated");
                return CompletableFuture.completedFuture(false);
            }

            if (plugin.getConfig().getBoolean("media-settings.check-ip", true)) {
                return hasIpActivatedAnyCode(player, "media-code-already-activated");
            }

            return CompletableFuture.completedFuture(true);
        });
    }

    private CompletableFuture<Boolean> hasIpActivatedAnyCode(Player player, String messageKey) {
        String ipAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        return database.hasIpActivatedAnyCode(ipAddress).thenApply(ipHasActivated -> {
            if (ipHasActivated) {
                plugin.getChatUtil().sendMessage(player, messageKey);
                return false;
            }
            return true;
        });
    }

    private void executeActivation(Player player, String code, ConfigurationSection codeSection) {
        String ipAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        database.activateCode(player.getUniqueId(), ipAddress, code).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendSuccessMessage(player, code, codeSection);
                executeCommands(player, codeSection);
            });
        });
    }

    private void sendSuccessMessage(Player player, String code, ConfigurationSection codeSection) {
        String successMessage = codeSection.getString("successfully");
        if (successMessage != null && !successMessage.isEmpty()) {
            player.sendMessage(plugin.getChatUtil().color(successMessage.replace("%player%", player.getName())));
        } else {
            plugin.getChatUtil().sendMessage(player, "code-activated", "{code}", code);
        }
    }

    private void executeCommands(Player player, ConfigurationSection codeSection) {
        List<String> commands = codeSection.getStringList("cmds");
        for (String cmd : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }
    }
}
