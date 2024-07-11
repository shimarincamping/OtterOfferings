package com.myapp.otterofferings;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyActivityPage extends AppCompatActivity {

    Context contextApp;
    ViewGroup ordersContainer;
    Button pastOrdersButton;
    Button currentOrdersButton;

    private ViewGroup generateOrderItem(String storeName, String storeAddress, String dateOrdered, double orderPrice) {
        ViewGroup pastOrderItem = (ViewGroup) LayoutInflater.from(contextApp).inflate(R.layout.activity_list_view_completed, null);

        TextView storeNameField = (TextView) pastOrderItem.getChildAt(1);
        TextView storeAddressField = (TextView) pastOrderItem.getChildAt(2);
        TextView dateOrderedField = (TextView) pastOrderItem.getChildAt(3);
        TextView orderTotalField = (TextView) pastOrderItem.getChildAt(4);

        storeNameField.setText(storeName);
        storeAddressField.setText(storeAddress);
        dateOrderedField.setText(dateOrdered.substring(0, 16));
        orderTotalField.setText("RM " + String.format("%.2f", orderPrice));

        return pastOrderItem;
    }

    private ViewGroup generateOrderItem(String storeName, String storeAddress, String dateOrdered, double orderPrice, String orderID) {
        ViewGroup currentOrderItem = (ViewGroup) LayoutInflater.from(contextApp).inflate(R.layout.activity_list_view_current, null);

        TextView storeNameField = (TextView) currentOrderItem.getChildAt(1);
        TextView storeAddressField = (TextView) currentOrderItem.getChildAt(2);
        TextView dateOrderedField = (TextView) currentOrderItem.getChildAt(3);
        TextView orderTotalField = (TextView) currentOrderItem.getChildAt(4);
        Button orderReceivedButton = (Button) currentOrderItem.getChildAt(5);

        storeNameField.setText(storeName);
        storeAddressField.setText(storeAddress);
        dateOrderedField.setText(dateOrdered.substring(0, 16));
        orderTotalField.setText("RM " + String.format("%.2f", orderPrice));

        orderReceivedButton.setOnClickListener(v -> {
            FirebaseHandler.ORDERS_REF.child(orderID).child("orderStatus").setValue("Completed");
            Toast.makeText(this, "Order complete! Enjoy!", Toast.LENGTH_LONG).show();
            this.recreate();
        });

        return currentOrderItem;
    }

    private void addOrderViewToList(Order order, Store store, boolean pastOrder) {
        ViewGroup orderView;

        if (pastOrder)
            orderView = generateOrderItem(store.getStoreName(), store.getStoreAddress(), order.getDateTimeOrdered(), order.getOrderTotal());
        else
            orderView = generateOrderItem(store.getStoreName(), store.getStoreAddress(), order.getDateTimeOrdered(), order.getOrderTotal(), order.getOrderID());

        ordersContainer.addView(orderView, 0);

        View space = new View(this);
        space.setLayoutParams(new ViewGroup.LayoutParams(100, 35));

        ordersContainer.addView(space, 0);
    }

    private void getPastOrders() {
        FirebaseHandler.MAIN_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<DataSnapshot> orders = new ArrayList<>();
                snapshot.child("orders").getChildren().forEach(orders::add);

                for (DataSnapshot i : orders) {
                    Order order = i.getValue(Order.class);
                    if (order.getUserID().equals(FirebaseHandler.getCurrentUser().getUserID())) {
                        if (order.getOrderTotal() > 0 && order.getOrderStatus().equals("Completed")) {
                            Store store = snapshot.child("stores").child(order.getStoreID()).getValue(Store.class);
                            addOrderViewToList(order, store, true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    private void getCurrentOrders() {
        FirebaseHandler.MAIN_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<DataSnapshot> orders = new ArrayList<>();
                snapshot.child("orders").getChildren().forEach(orders::add);

                for (DataSnapshot i : orders) {
                    Order order = i.getValue(Order.class);
                    if (order.getUserID().equals(FirebaseHandler.getCurrentUser().getUserID())) {
                        if (order.getOrderTotal() > 0 && !order.getOrderStatus().equals("Completed")) {
                            Store store = snapshot.child("stores").child(order.getStoreID()).getValue(Store.class);
                            addOrderViewToList(order, store, false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }
    private void initUI() {
        pastOrdersButton.setOnClickListener(v -> {
            pastOrdersButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.main_text)));
            pastOrdersButton.setTextColor(getResources().getColor(R.color.white));

            currentOrdersButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            currentOrdersButton.setTextColor(getResources().getColor(R.color.main_text));

            ordersContainer.removeAllViews();
            getPastOrders();
        });

        currentOrdersButton.setOnClickListener(v -> {
            currentOrdersButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.main_text)));
            currentOrdersButton.setTextColor(getResources().getColor(R.color.white));

            pastOrdersButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            pastOrdersButton.setTextColor(getResources().getColor(R.color.main_text));

            ordersContainer.removeAllViews();
            getCurrentOrders();
        });

        findViewById(R.id.discoverButton).setOnClickListener(v -> {
            startActivity(new Intent(MyActivityPage.this, HomePage.class));
        });

        findViewById(R.id.shopButton).setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery("");
            startActivity(new Intent(MyActivityPage.this, SearchResultsPage.class));
        });

        findViewById(R.id.profileButton).setOnClickListener(v -> {
            startActivity(new Intent(MyActivityPage.this, ProfilePage.class));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        contextApp = this;
        ordersContainer = findViewById(R.id.ordersContainer);
        pastOrdersButton = findViewById(R.id.activity_past_orders);
        currentOrdersButton = findViewById(R.id.activity_current_orders);

        initUI();
        getPastOrders();
    }
}