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

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import net.milkbowl.localshops.LocalShops;
import net.milkbowl.localshops.objects.Item;
import net.milkbowl.localshops.objects.MsgType;
import net.milkbowl.localshops.objects.GlobalShop;
import net.milkbowl.localshops.objects.PermType;
import net.milkbowl.localshops.objects.PlayerData;
import net.milkbowl.localshops.objects.Shop;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandShopDestroy extends Command {

    public CommandShopDestroy(LocalShops plugin, String commandLabel, CommandSender sender, String command, boolean isGlobal) {
        super(plugin, commandLabel, sender, command, isGlobal);
    }

    public CommandShopDestroy(LocalShops plugin, String commandLabel, CommandSender sender, String[] command, boolean isGlobal) {
        super(plugin, commandLabel, sender, command, isGlobal);
    }

    public boolean process() {
        if (!(sender instanceof Player) || !canUseCommand(PermType.DESTROY)) {
            sender.sendMessage(plugin.getResourceManager().getString(MsgType.GEN_USER_ACCESS_DENIED));
            return false;
        }

        /*
         * Available formats: /shop destroy; /shop delete; /shop del
         * 
         */

        Player player = (Player) sender;
        String playerName = player.getName();

        // get the shop the player is currently in
        if (plugin.getPlayerData().get(playerName).shopList.size() == 1 && !isGlobal) {
            UUID shopUuid = plugin.getPlayerData().get(playerName).shopList.get(0);
            Shop shop = plugin.getShopManager().getLocalShop(shopUuid);

            if (!shop.getOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(PermType.ADMIN_LOCAL)) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner to destroy it.");
                return false;
            }

            Iterator<PlayerData> it = plugin.getPlayerData().values().iterator();
            while (it.hasNext()) {
                PlayerData p = it.next();
                if (p.shopList.contains(shop.getUuid())) {
                    Player thisPlayer = plugin.getServer().getPlayer(p.playerName);
                    p.removePlayerFromShop(thisPlayer, shop.getUuid());
                    thisPlayer.sendMessage(plugin.getResourceManager().getChatPrefix() + " " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " has been destroyed");
                }
            }

            Collection<Item> shopItems = shop.getItems();

            if (plugin.getShopManager().deleteShop(shop)) {
                // return items to player (if a player)
                if (sender instanceof Player) {
                    for (Item item : shopItems) {
                        givePlayerItem(item, shop.getItem(item).getStock());
                    }
                }
            } else {
                // error message :(
                sender.sendMessage("Could not return shop inventory!");
            }

        } else if (isGlobal) {
            GlobalShop shop = plugin.getShopManager().getGlobalShop(player.getWorld());

            // Check if shop exists
            if (shop == null) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be inside a shop to use /" + commandLabel + " destroy");
                return true;
            }

            // Check if permissions
            if (!shop.getOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(PermType.ADMIN_GLOBAL) && !shop.getManagers().contains(player.getName())) {
                player.sendMessage(ChatColor.DARK_AQUA + "You must be the shop owner or manager to destroy a global shop");
                return false;
            }

            // Delete Shop
            if (plugin.getShopManager().deleteShop(shop)) {
                player.sendMessage(plugin.getResourceManager().getChatPrefix() + " " + ChatColor.WHITE + shop.getName() + ChatColor.DARK_AQUA + " has been destroyed");
            } else {
                player.sendMessage(plugin.getResourceManager().getChatPrefix() + " " + " Error while attempting to destroy shop: " + ChatColor.WHITE + shop.getName());
            }
        }

        return true;
    }
}
