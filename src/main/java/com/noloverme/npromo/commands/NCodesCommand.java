package com.noloverme.npromo.commands;

import com.noloverme.npromo.NPromo;
import com.noloverme.npromo.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class NCodesCommand implements CommandExecutor, TabCompleter {

    private final NPromo plugin;

    public NCodesCommand(NPromo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ncodes.admin")) {
            ChatUtil.sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            ChatUtil.sendMessage(sender, "usage", "{usage}", "/ncodes <reload|check|statspromo>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reload();
                ChatUtil.sendMessage(sender, "reload");
                break;
            case "check":
                if (args.length != 2) {
                    ChatUtil.sendMessage(sender, "usage", "{usage}", "/ncodes check <player>");
                    return true;
                }
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                if (!player.hasPlayedBefore()) {
                    ChatUtil.sendMessage(sender, "player-not-found");
                    return true;
                }
                List<String> codes = plugin.getDatabase().getActivatedCodes(player.getUniqueId());
                if (codes.isEmpty()) {
                    ChatUtil.sendMessage(sender, "check.no-codes", "{player}", player.getName());
                } else {
                    ChatUtil.sendMessage(sender, "check.header", "{player}", player.getName());
                    codes.forEach(code -> ChatUtil.sendMessage(sender, "check.line", "{code}", code));
                }
                break;
            case "statspromo":
                if (args.length != 2) {
                    ChatUtil.sendMessage(sender, "usage", "{usage}", "/ncodes statspromo <code>");
                    return true;
                }
                String code = args[1];
                if (!plugin.getCodes().isConfigurationSection(code)) {
                    ChatUtil.sendMessage(sender, "invalid-code");
                    return true;
                }
                int activations = plugin.getDatabase().getCodeActivations(code);
                ChatUtil.sendMessage(sender, "stats.header", "{code}", code);
                ChatUtil.sendMessage(sender, "stats.activations", "{count}", String.valueOf(activations));
                break;
            default:
                ChatUtil.sendMessage(sender, "usage", "{usage}", "/ncodes <reload|check|statspromo>");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("check");
            completions.add("statspromo");
            return completions;
        }
        return null;
    }
}
