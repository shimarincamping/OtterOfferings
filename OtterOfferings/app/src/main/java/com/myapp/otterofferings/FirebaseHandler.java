package com.myapp.otterofferings;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedHashMap;

public class FirebaseHandler {

    public final static FirebaseDatabase DB = FirebaseDatabase.getInstance();
    public final static DatabaseReference MAIN_REF = DB.getReference();
    public final static DatabaseReference ORDERS_REF = MAIN_REF.child("orders");
    public final static DatabaseReference STORES_REF = MAIN_REF.child("stores");
    public final static DatabaseReference USERS_REF = MAIN_REF.child("users");
    private static User currentUser;
    private static LinkedHashMap<Item, Integer> currentCart = new LinkedHashMap<>();


    // Getter and setter
    public static User getCurrentUser() {
        return currentUser;
    }
    public static void setCurrentUser(User newUser) {
        currentUser = newUser;
    }

    public static LinkedHashMap<Item, Integer> getCurrentCart() {
        return currentCart;
    }
    public static void clearCart() {
        currentCart = new LinkedHashMap<Item, Integer>();
    }

    public static double getDeliveryFee() {
        return 3.00;
    }
}
