/**
 * Copyright 2011 Morgan Humes
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ or send
 * a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View,
 * California, 94041, USA.
 *
 */

package net.milkbowl.vault.modules.economy;

public class EconomyResponse {

    public static enum ResponseType {
        SUCCESS(1),
        FAILURE(2),
        NOT_IMPLEMENTED(3);
        
        private int id;
        ResponseType(int id) {
            this.id = id;
        }
        
        int getId() {
            return id;
        }
    }
    
    public final double amount;
    public final double balance;
    public final ResponseType type;
    public final String errorMessage;
    
    public EconomyResponse(double amount, double balance, ResponseType type, String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.errorMessage = errorMessage;
    }
    
    public boolean transactionSuccess() {
        switch (type) {
        case SUCCESS:
            return true;
        default:
            return false;
        }
    }
}