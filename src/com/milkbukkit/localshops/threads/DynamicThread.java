package com.milkbukkit.localshops.threads;

import java.util.Random;
import java.util.logging.Logger;

import com.milkbukkit.localshops.Config;
import com.milkbukkit.localshops.InventoryItem;
import com.milkbukkit.localshops.LocalShops;
import com.milkbukkit.localshops.Shop;

/*
 * Represents the Dynamic Shop Task which is run in a thread
 * 
 * @author sleaker
 * @params plugin
 * 
 */
public class DynamicThread extends Thread {

    private boolean run = true;
    private LocalShops plugin = null;
    private static Random randNum = new Random();

    // Logging
    private static final Logger log = Logger.getLogger("Minecraft");

    public DynamicThread(LocalShops plugin) {
        this.plugin = plugin;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public void run() {
        while (run) {
            // Do our Sleep stuff!

            int interval = Config.getDynamicInterval();
            int dynamicChance = Config.getDynamicChance();
            int maxPriceChange = Config.getDynamicMaxPriceChange();
            int minPriceChange = Config.getDynamicMinPriceChange();

            for (int i = 0; i < interval; i++) {
                try {
                    if (!run) {
                        return;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.info("Could not sleep!");
                }
            }

            for (Shop shop : plugin.getShopManager().getAllShops()) {
                if (shop.isDynamicPrices()) {
                    // Log to let us know scheduler is working.
                    log.info("[LocalShops] - Adjusting prices for shop: " + shop.getName());

                    int totDynItems = shop.numDynamicItems();
                    // Ignore this shop if we have no items set to Dynamic
                    if (totDynItems == 0) {
                        log.info("[LocalShops] - Shop " + shop.getName() + " is set to dynamic but has no dynamic items");
                        continue;
                    }

                    for (InventoryItem item : shop.getItems()) {

                        if (item.isDynamic()) {
                            float itemChance = randNum.nextFloat();
                            if (item.isNormalized() && itemChance >= (100 - dynamicChance) / 100) {
                                /*
                                 * Reduce price if: dynamicChance + half the
                                 * value to 100 is greater than random var
                                 * 
                                 * example: 75 + (100-75)/2 = 88
                                 */
                                if (itemChance < dynamicChance + ((100 - dynamicChance) / 2)) {
                                    // Get change percentage as expressed in
                                    // decimal
                                    int change = 1 - ((randGen(maxPriceChange - minPriceChange) + minPriceChange) / 100);
                                    item.setDynBuyPrice(item.getBuyPrice() * change);
                                    item.setDynSellPrice(item.getSellPrice() * change);
                                }
                                // Increase price
                                else {
                                    // Get change percentage as expressed in
                                    // decimal
                                    int change = 1 + ((randGen(maxPriceChange - minPriceChange) + minPriceChange) / 100);
                                    item.setDynBuyPrice(item.getBuyPrice() * change);
                                    item.setDynSellPrice(item.getSellPrice() * change);
                                }
                            } else if (itemChance >= (100 - dynamicChance) / 100) {
                                // Reset prices to normal
                                item.setDynBuyPrice(item.getBuyPrice());
                                item.setDynSellPrice(item.getSellPrice());
                            }
                        }
                    }
                }
            }

        }
    }

    /*
     * Generate a random number between 1 and max and return the result
     * 
     * @params int max - maximum number possible
     * 
     * @returns int Result - generated random number between 1 and max
     */
    private int randGen(int max) {
        int result = randNum.nextInt(max) + 1;
        return result;
    }

}