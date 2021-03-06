/**
 * 
 * Copyright 2011 MilkBowl (https://github.com/MilkBowl)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package net.milkbowl.localshops.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.localshops.LocalShops;
import net.milkbowl.localshops.objects.MsgType;
import net.milkbowl.localshops.util.GenericFunctions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommandExecutor implements CommandExecutor {

    private final LocalShops plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    public static final Map<String, CommandTypeInfo> commandTypeMap = new HashMap<String, CommandTypeInfo>();

    static {
        commandTypeMap.put("admin", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandAdminSet.class, false, false, false));
        commandTypeMap.put("add", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopAdd.class, true, true, false));
        commandTypeMap.put("addloc", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopLink.class, true, true, true));
        commandTypeMap.put("browse", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopBrowse.class, true, true, false));
        commandTypeMap.put("bro", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopBrowse.class, true, true, false));
        commandTypeMap.put("buy", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopBuy.class, true, true, false));
        commandTypeMap.put("create", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopCreate.class, true, true, true));
        commandTypeMap.put("debug", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopDebug.class, true, true, false));
        commandTypeMap.put("del", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopDestroy.class, true, true, true));
        commandTypeMap.put("delete", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopDestroy.class, true, true, true));
        commandTypeMap.put("destroy", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopDestroy.class, true, true, true));
        commandTypeMap.put("find", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopFind.class, true, false, false));
        commandTypeMap.put("help", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopHelp.class, true, true, false));
        commandTypeMap.put("info", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopInfo.class, true, true, false));
        commandTypeMap.put("link", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopLink.class, true, true, true));
        commandTypeMap.put("list", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopList.class, true, true, false));
        commandTypeMap.put("move", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopMove.class, true, false, true));
        commandTypeMap.put("remove", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopRemove.class, true, true, false));
        commandTypeMap.put("removeloc", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopUnlink.class, false, true, true));
        commandTypeMap.put("search", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopSearch.class, true, true, false));
        commandTypeMap.put("searchall", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopMultiSearch.class, true, true, false));
        commandTypeMap.put("select", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopSelect.class, true, false, false));
        commandTypeMap.put("sell", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopSell.class, true, true, false));
        commandTypeMap.put("set", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopSet.class, true, true, false));
        commandTypeMap.put("unlink", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopUnlink.class, false, true, true));
        commandTypeMap.put("version", new CommandTypeInfo(net.milkbowl.localshops.commands.CommandShopVersion.class, true, true, false));
    }

    public ShopCommandExecutor(LocalShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String type = null;
        boolean global = false;
        String user = "CONSOLE";

        if (sender instanceof Player) {
            user = ((Player) sender).getName();
        }

        String cmdString = null;
        if (commandLabel.equalsIgnoreCase("lsadmin")) {
            cmdString = GenericFunctions.join(args, " ");
            type = "admin";
        } else if (commandLabel.equalsIgnoreCase("buy")) {
            cmdString = "buy " + GenericFunctions.join(args, " ");
            type = "buy";
        } else if (commandLabel.equalsIgnoreCase("sell")) {
            cmdString = "sell " + GenericFunctions.join(args, " ");
            type = "sell";
        } else if (commandLabel.equalsIgnoreCase("gbuy")) {
            cmdString = "buy " + GenericFunctions.join(args, " ");
            type = "buy";
            global = true;
        } else if (commandLabel.equalsIgnoreCase("gsell")) {
            cmdString = "sell" + GenericFunctions.join(args, " ");
            type = "sell";
            global = true;
        } else {
            if (args.length > 0) {
                cmdString = GenericFunctions.join(args, " ");
                type = args[0].toLowerCase();
            } else if (command.getName().equalsIgnoreCase("gshop")) {
                return (new CommandShopHelp(plugin, commandLabel, sender, args, true)).process();
            } else {
                return (new CommandShopHelp(plugin, commandLabel, sender, args, false)).process();
            }

            if (commandLabel.equalsIgnoreCase("gshop")) {
                global = true;
            }
        }

        CommandTypeInfo cInfo = commandTypeMap.get(type);
        if (cInfo == null) {
            commandTypeMap.get("help").getCommandInstance(plugin, commandLabel, sender, cmdString, global).process();
            return true;
        } else {
            net.milkbowl.localshops.commands.Command cmd = cInfo.getCommandInstance(plugin, commandLabel, sender, cmdString, global);
            if (cmd != null) {
                boolean cVal = cmd.process();
                if (cVal && cInfo.checkPlayerPositions) {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        plugin.checkPlayerPosition(player);
                    }
                }

                if (global) {
                    log.info(plugin.getResourceManager().getString(MsgType.CMD_ISSUED_GLOBAL, new String[]{"%NAME%", "%COMMAND%"}, new Object[]{user, cmdString}));
                } else {
                    log.info(plugin.getResourceManager().getString(MsgType.CMD_ISSUED_LOCAL, new String[]{"%NAME%", "%COMMAND%"}, new Object[]{user, cmdString}));
                }

                return cVal;
            } else {
                return false;
            }
        }
    }
}
