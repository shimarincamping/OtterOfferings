package com.myapp.otterofferings;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentMethodPage extends AppCompatActivity {

    private static boolean paymentSelected = false;

    public static boolean isPaymentSelected() {
        return paymentSelected;
    }

    public static void resetPaymentSelected() {
        paymentSelected = false;
    }

    public void cashHandler(View v) {
        findViewById(R.id.tng_selected).setVisibility(View.GONE);
        findViewById(R.id.cash_selected).setVisibility(View.VISIBLE);

        paymentSelected = true;
    }

    public void tngHandler(View v) {
        findViewById(R.id.cash_selected).setVisibility(View.GONE);
        findViewById(R.id.tng_selected).setVisibility(View.VISIBLE);

        paymentSelected = true;
    }

    public void backButtonHandler(View v) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_method_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}