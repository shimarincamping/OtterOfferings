package com.myapp.otterofferings;

import java.util.ArrayList;

public class Store {

    private String storeID;
    private String storeName;
    private String storeAddress;
    private String storeLocation;
    private String openTime;
    private String closeTime;
    private ArrayList<Item> storeItems;


    public Store() {

    }

    public Store(String storeID, String storeName, String storeAddress, String storeLocation, String openTime, String closeTime) {
        this.storeID = storeID;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storeLocation = storeLocation;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.storeItems = new ArrayList<>();
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public ArrayList<Item> getStoreItems() {
        return storeItems;
    }

    public Item getItem(int itemIndex) {
        return storeItems.get(itemIndex);
    }
    public Item getItem(String itemID) {
        for (Item i : storeItems) {
            if (i.getItemID().equals(itemID)) return i;
        } return null;
    }

    public void addItem(Item i) {
        if (storeItems == null) storeItems = new ArrayList<>();
        storeItems.add(i);
    }

    public void addItem(ArrayList<Item> i) {
        storeItems.addAll(i);
    }

    public void removeItem(int itemIndex) {
        storeItems.remove(itemIndex);
    }

    public void removeItem(String itemID) {
        storeItems.remove(getItem(itemID));
    }

    public void clearItems() {
        storeItems.clear();
    }
}
