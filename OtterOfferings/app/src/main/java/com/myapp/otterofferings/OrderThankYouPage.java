package com.myapp.otterofferings;

import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
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

public class OrderThankYouPage extends AppCompatActivity {

    private static Order orderToDisplay;

    public static void setOrderToDisplay(Order newOrder) {
        orderToDisplay = newOrder;
    }

    private String reformatDate(String date) {
        String[] dateTokens = date.split(" ")[1].split(":");
        return (dateTokens[0] + ":" + dateTokens[1]);
    }

    private void markOrderAsReceived() {
        FirebaseHandler.ORDERS_REF.child(orderToDisplay.getOrderID())
                .child("orderStatus").setValue("Completed");
    }

    public void contactButtonHandler(View v) {
        findViewById(R.id.contactField).setVisibility(View.VISIBLE);
    }

    public void orderReceivedHandler(View v) {
        markOrderAsReceived();
        Toast.makeText(this, "Order complete! Enjoy!", Toast.LENGTH_LONG).show();
        finish();
    }

    public void backButtonHandler(View v) {
        finish();
    }

    private void initUI() {
        ((TextView) findViewById(R.id.orderIDField)).setText(orderToDisplay.getOrderID());
        ((TextView) findViewById(R.id.orderTimeField)).setText(reformatDate(orderToDisplay.getDateTimeToReceive()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_thank_you_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUI();
    }
}