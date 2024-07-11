package com.myapp.otterofferings;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Order {

    // int order ID? = len(orders)
    private String orderID;
    private String userID;
    private String storeID;
    private HashMap<String, Integer> orderItems;
    private double orderTotal;
    private double deliveryFee;
    private String dateTimeOrdered;
    private String dateTimeToReceive;
    private String orderStatus;

    public Order() {

    }
    @SuppressLint("SimpleDateFormat")
    public Order(String userID, String storeID, LinkedHashMap<Item, Integer> orderItems, double orderTotal, int minutesToReceive) {
        this.userID = userID;
        this.storeID = storeID;
        this.orderItems = sanitiseOrderItems(orderItems);
        this.orderTotal = orderTotal;
        this.deliveryFee = FirebaseHandler.getDeliveryFee();

        Date currentTime = new Date();
        this.dateTimeOrdered = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(currentTime);
        this.dateTimeToReceive = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date(currentTime.getTime() + (minutesToReceive * 60000L)));
        this.orderStatus = "Ordered";
        this.orderID = userID + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(currentTime);
    }

    private HashMap<String, Integer> sanitiseOrderItems(LinkedHashMap<Item, Integer> orderItems) {
        HashMap<String, Integer> sanitisedOrderItems = new LinkedHashMap<>();
        for (Item i : orderItems.keySet()) {
            sanitisedOrderItems.put(i.getItemID(), orderItems.get(i));
        } return sanitisedOrderItems;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public HashMap<String, Integer> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(HashMap<String, Integer> orderItems) {
        this.orderItems = orderItems;
    }

    public double getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(double orderTotal) {
        this.orderTotal = orderTotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getDateTimeOrdered() {
        return dateTimeOrdered;
    }

    public void setDateTimeOrdered(String dateTimeOrdered) {
        this.dateTimeOrdered = dateTimeOrdered;
    }

    public String getDateTimeToReceive() {
        return dateTimeToReceive;
    }

    public void setDateTimeToReceive(String dateTimeToReceive) {
        this.dateTimeToReceive = dateTimeToReceive;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}


