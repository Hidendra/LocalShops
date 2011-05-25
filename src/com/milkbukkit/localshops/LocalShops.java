package com.milkbukkit.localshops;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.milkbukkit.localshops.commands.ShopCommandExecutor;
import com.milkbukkit.localshops.modules.economy.EconomyManager;
import com.milkbukkit.localshops.modules.permission.PermissionManager;
import com.milkbukkit.localshops.threads.NotificationThread;
import com.milkbukkit.localshops.threads.ReportThread;
import com.milkbukkit.localshops.threads.DynamicThread;

/**
 * Local Shops Plugin
 * 
 * @author Jonbas
 */
public class LocalShops extends JavaPlugin {
    // Listeners & Objects
    public ShopsPlayerListener playerListener = new ShopsPlayerListener(this);
    public ShopsBlockListener blockListener = new ShopsBlockListener(this);
    public ShopsEntityListener entityListener = new ShopsEntityListener(this);
    private ShopManager shopManager = new ShopManager(this);
    public PluginDescriptionFile pdfFile = null;
    public DynamicThread shopSchedulerThread = null;
    protected ReportThread reportThread = null;
    protected NotificationThread notificationThread = null;
    private EconomyManager econManager = null;
    private PermissionManager permManager = null;
    public boolean dynamicScheduled = false;

    // Logging
    private final Logger log = Logger.getLogger("Minecraft");

    // Constants
    public static final String CHAT_PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.DARK_AQUA + "] ";
    static File shopsDir;
    static File folderDir;
    static List<World> foundWorlds;

    private static ItemData itemList = new ItemData();
    private Map<String, PlayerData> playerData; // synchronized player hash
    
    public LocalShops() {
        Config.load();
    }

    public void onEnable() {
        pdfFile = getDescription();
        setPlayerData(Collections.synchronizedMap(new HashMap<String, PlayerData>()));

        // add all the online users to the data trees
        for (Player player : this.getServer().getOnlinePlayers()) {
            getPlayerData().put(player.getName(), new PlayerData(this, player.getName()));
        }

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        
        
        // Register Commands
        CommandExecutor cmdExec = new ShopCommandExecutor(this);
        getCommand("lshop").setExecutor(cmdExec);
        getCommand("lsadmin").setExecutor(cmdExec);
        getCommand("gshop").setExecutor(cmdExec);
        getCommand("buy").setExecutor(cmdExec);
        getCommand("sell").setExecutor(cmdExec);
        getCommand("gbuy").setExecutor(cmdExec);
        getCommand("gsell").setExecutor(cmdExec);

        // setup the file IO
        folderDir = new File(Config.getDirPath());
        folderDir.mkdir();
        shopsDir = new File(Config.getDirShopsActivePath());
        shopsDir.mkdir();

        foundWorlds = getServer().getWorlds();
        // read the shops into memory
        getShopManager().loadShops(shopsDir);
        
        //Make sure our global shop definitions are valid in case someone monkeyed with the config
        Config.verifyGlobalShops(getShopManager());
        
        //Check if we should run the scheduler
		if (dynamicScheduled)
            enableShedule();
            
        // update the console that we've started
        log.info(String.format("[%s] %s", pdfFile.getName(), "Loaded with " + getShopManager().getNumShops() + " shop(s)"));
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is enabled: " + Config.getSrvUuid().toString()));

        // check which shops players are inside
        for (Player player : this.getServer().getOnlinePlayers()) {
            playerListener.checkPlayerPosition(player);
        }
        
        // Start reporting thread
        if(Config.getSrvReport()) {
            reportThread = new ReportThread(this);
            reportThread.start();
        }
        
        // Start Notification thread
        if (Config.getShopTransactionNotice()) {
            notificationThread = new NotificationThread(this);
            notificationThread.start();
        }
        
        setEconManager(new EconomyManager(this));
        if(!getEconManager().loadEconomies()) {
            // No valid economies, display error message and disables
            log.warning(String.format("[%s] FATAL: No economic plugins found, please refer to the documentation.", pdfFile.getName()));
            getPluginLoader().disablePlugin(this);
        }
        
        setPermManager(new PermissionManager(this));
        if(!getPermManager().load()) {
            // no valid permissions, display error message and disables
            log.warning(String.format("[%s] FATAL: No permission plugins found, please refer to the documentation.", pdfFile.getName()));
            getPluginLoader().disablePlugin(this);
        }
    }

    public void onDisable() {
        // Save all shops
        getShopManager().saveAllShops();
        
        // Save config file
        Config.save();
        
        // Stop Reporting thread
        if(Config.getSrvReport() && reportThread != null && reportThread.isAlive()) {
            try {
                Config.setSrvReport(false);
                reportThread.join(2000);
            } catch (InterruptedException e) {
                // hmm, thread didn't die
                log.warning(String.format("[%s] %s", pdfFile.getName(), "ReportThread did not exit"));
            }
        }
         //Disable the scheduler
        if ( shopSchedulerThread != null && shopSchedulerThread.isAlive()) {
            try {
                shopSchedulerThread.setRun(false);
                shopSchedulerThread.join(2000);
            } catch (InterruptedException e) {
                // hmm, thread didn't die
                log.warning(String.format("[%s] %s", pdfFile.getName(), "dynamicThread did not exit"));
            }
        }
        
        // Stop notification thread
        if(Config.getShopTransactionNotice() && notificationThread != null && notificationThread.isAlive()) {
            try {
                Config.setShopTransactionNotice(false);
                notificationThread.join(2000);
            } catch (InterruptedException e) {
                // hmm, thread didn't die
                log.warning(String.format("[%s] %s", pdfFile.getName(), "NotificationThread did not exit"));
            }
        }
        
        // update the console that we've stopped
        log.info(String.format("[%s] %s", pdfFile.getName(), "Version " + pdfFile.getVersion() + " is disabled!"));
    }

    public void setShopData(ShopManager shopData) {
        this.shopManager = shopData;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public void setPlayerData(Map<String, PlayerData> playerData) {
        this.playerData = playerData;
    }

    public Map<String, PlayerData> getPlayerData() {
        return playerData;
    }

    public static void setItemList(ItemData itemList) {
        LocalShops.itemList = itemList;
    }

    public static ItemData getItemList() {
        return itemList;
    }

    public void setEconManager(EconomyManager econManager) {
        this.econManager = econManager;
    }

    public EconomyManager getEconManager() {
        return econManager;
    }

    public void setPermManager(PermissionManager permManager) {
        this.permManager = permManager;
    }

    public PermissionManager getPermManager() {
        return permManager;
    }
        /**
     * Checks if the task is set to be scheduled
     * Does not check if it is already running or not.
     * 
     * @return dynamicScheduled 
     */
    public boolean isDynamicScheduled() {
        return dynamicScheduled;
    }
    /**
     * Tags the scheduler as enabled or disabled.
     * 
     * @param dynamicScheduled variable to determine if scheduler is set to run
     */
    public void setDynamicScheduler(boolean dynamicScheduled) {
        this.dynamicScheduled = dynamicScheduled;
    }
        /**
     * Attempts to Enable the dynamic price adjusting schedule
     * Will do nothing if the Thread has already been initialized
     * 
     */
    public void enableShedule() {
        //Check to make sure we don't already have a scheduler running.
        if ( dynamicScheduled && shopSchedulerThread == null) {
            shopSchedulerThread = new DynamicThread(this);
            shopSchedulerThread.start();
        } 

    }
    
}
