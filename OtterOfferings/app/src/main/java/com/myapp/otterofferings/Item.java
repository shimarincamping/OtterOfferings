package com.myapp.otterofferings;

public class Item {
    private String itemID;
    private String storeID;
    private String itemName;
    private double itemPrice;
    private String itemExpirationDate;
    private int itemStock;
    private String itemImageURL;

    public Item() {

    }

    public Item(String storeID, String itemID, String itemName, double itemPrice, String itemExpirationDate, int itemStock, String itemImageURL) {
        this.storeID = storeID;
        this.itemID = storeID + "-" + itemID;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemExpirationDate = itemExpirationDate;
        this.itemStock = itemStock;
        this.itemImageURL = itemImageURL;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemExpirationDate() {
        return itemExpirationDate;
    }

    public void setItemExpirationDate(String itemExpirationDate) {
        this.itemExpirationDate = itemExpirationDate;
    }

    public String getItemImageURL() {
        return itemImageURL;
    }

    public void setItemImageURL(String itemImageURL) {
        this.itemImageURL = itemImageURL;
    }

    public int getItemStock() {
        return itemStock;
    }

    public void setItemStock(int itemStock) {
        this.itemStock = itemStock;
    }

    public void increaseItemStock() {
        itemStock++;
    }

    public void increaseItemStock(int i) {
        itemStock += i;
    }

    public void decreaseItemStock() {
        itemStock--;
    }

    public void decreaseItemStock(int i) {
        itemStock -= i;
    }
}
