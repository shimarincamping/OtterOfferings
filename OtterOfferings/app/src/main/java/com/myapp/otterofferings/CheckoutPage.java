package com.myapp.otterofferings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CheckoutPage extends AppCompatActivity {

    ViewGroup pickupButton;
    ViewGroup deliveryButton;
    TextView addressView;
    TextView orderSubtotal;
    TextView deliveryFee;
    TextView orderGrandTotal;
    Button placeOrderButton;

    private void checkoutCart(int minutesToReceive) {
        Order newOrder = new Order(
                FirebaseHandler.getCurrentUser().getUserID(),
                FirebaseHandler.getCurrentCart().keySet().iterator().next().getStoreID(),
                FirebaseHandler.getCurrentCart(),
                CartPage.getCartPrice(FirebaseHandler.getCurrentCart()),
                minutesToReceive
        );
        FirebaseHandler.ORDERS_REF.child(newOrder.getOrderID()).setValue(newOrder);
        OrderThankYouPage.setOrderToDisplay(newOrder);

        startActivity(new Intent(CheckoutPage.this, OrderThankYouPage.class));
        finish();
    }

    public void backButtonHandler(View v) {
        finish();
    }

    public void editAddressHandler(View v) {
        startActivity(new Intent(CheckoutPage.this, Maps.class));
    }

    public void paymentMethodHandler(View v) {
        startActivity(new Intent(CheckoutPage.this, PaymentMethodPage.class));
    }

    public void placeOrder(View v) {
        if (addressView.getText().toString().equals(getResources().getString(R.string.no_address_found)))
            Toast.makeText(this, "Please set an address first!", Toast.LENGTH_LONG).show();
        else if (!PaymentMethodPage.isPaymentSelected())
            Toast.makeText(this, "Please choose a payment method first!", Toast.LENGTH_LONG).show();
        else {
            placeOrderButton.setEnabled(false);
            checkoutCart(15);
            FirebaseHandler.clearCart();
            PaymentMethodPage.resetPaymentSelected();
        }
    }

    private void initUI() {
        pickupButton.setOnClickListener(v -> {
            pickupButton.setBackground(getResources().getDrawable(R.drawable.choose_delivery_pickup_option_bg));
            deliveryButton.setBackground(null);
        });

        deliveryButton.setOnClickListener(v -> {
            deliveryButton.setBackground(getResources().getDrawable(R.drawable.choose_delivery_pickup_option_bg));
            pickupButton.setBackground(null);
        });

        try {
            if (!FirebaseHandler.getCurrentUser().getSavedAddress().isEmpty())
                addressView.setText(FirebaseHandler.getCurrentUser().getSavedAddress());
        } catch (NullPointerException e) {
            addressView.setText(getResources().getString(R.string.no_address_found));
        }

        double cartPrice = CartPage.getCartPrice(FirebaseHandler.getCurrentCart());
        double deliveryFeePrice = FirebaseHandler.getDeliveryFee();

        orderSubtotal.setText("RM " + String.format("%.2f", cartPrice));
        deliveryFee.setText("RM " + String.format("%.2f", deliveryFeePrice));
        orderGrandTotal.setText("RM " + String.format("%.2f", cartPrice + deliveryFeePrice));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_page);

        pickupButton = findViewById(R.id.pickup_option);
        deliveryButton = findViewById(R.id.delivery_option);
        addressView = findViewById(R.id.tv_address);
        orderSubtotal = findViewById(R.id.subtotal_text);
        deliveryFee = findViewById(R.id.delivery_fee_text);
        orderGrandTotal = findViewById(R.id.grand_total_text);
        placeOrderButton = findViewById(R.id.place_order_button);

        initUI();
    }}
