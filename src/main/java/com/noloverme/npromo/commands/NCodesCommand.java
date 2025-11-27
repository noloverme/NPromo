package com.noloverme.npromo.commands;

import com.noloverme.npromo.NPromo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NCodesCommand implements CommandExecutor, TabCompleter {

    private final NPromo plugin;

    public NCodesCommand(NPromo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ncodes.admin")) {
            this.plugin.getChatUtil().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "check":
                handleCheck(sender, args);
                break;
            case "statspromo":
                handleStatsPromo(sender, args);
                break;
            default:
                sendUsage(sender);
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        this.plugin.reloadPlugin();
        this.plugin.getChatUtil().sendMessage(sender, "reload");
    }

    private void handleCheck(CommandSender sender, String[] args) {
        if (args.length != 2) {
            this.plugin.getChatUtil().sendMessage(sender, "usage", "{usage}", "/ncodes check <player>");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        if (!player.hasPlayedBefore()) {
            this.plugin.getChatUtil().sendMessage(sender, "player-not-found");
            return;
        }

        this.plugin.getDatabase().getActivatedCodes(player.getUniqueId()).thenAccept(codes -> {
            if (codes.isEmpty()) {
                this.plugin.getChatUtil().sendMessage(sender, "check.no-codes", "{player}", player.getName());
            } else {
                this.plugin.getChatUtil().sendMessage(sender, "check.header", "{player}", player.getName());
                codes.forEach(code -> this.plugin.getChatUtil().sendMessage(sender, "check.line", "{code}", code));
            }
        });
    }

    private void handleStatsPromo(CommandSender sender, String[] args) {
        if (args.length != 2) {
            this.plugin.getChatUtil().sendMessage(sender, "usage", "{usage}", "/ncodes statspromo <code>");
            return;
        }

        String code = args[1];
        if (!this.plugin.getCodeManager().codeExistsInConfig(code)) {
            this.plugin.getChatUtil().sendMessage(sender, "invalid-code");
            return;
        }

        this.plugin.getDatabase().getCodeActivations(code).thenAccept(activations -> {
            this.plugin.getChatUtil().sendMessage(sender, "stats.header", "{code}", code);
            this.plugin.getChatUtil().sendMessage(sender, "stats.activations", "{count}", String.valueOf(activations));
        });
    }

    private void sendUsage(CommandSender sender) {
        this.plugin.getChatUtil().sendMessage(sender, "usage", "{usage}", "/ncodes <reload|check|statspromo>");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "check", "statspromo").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check")) {
                return null; // Player names
            }
            if (args[0].equalsIgnoreCase("statspromo")) {
                return new ArrayList<>(this.plugin.getCodes().getKeys(false));
            }
        }
        return new ArrayList<>();
    }
}
