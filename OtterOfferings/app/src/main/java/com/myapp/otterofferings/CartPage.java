package com.myapp.otterofferings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartPage extends AppCompatActivity {

    private Context contextApp;
    private ViewGroup mainLayout;
    private final int NUMBER_OF_CHILDREN_AFTER_LIST_ITEMS = 3;

    public static void addToCart(Context c, String storeID, String itemID) {
        FirebaseHandler.STORES_REF.child(storeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Store store = snapshot.getValue(Store.class);
                if (store == null) return;

                // Not allowing adding items from multiple stores
                // If cart has items, check against index 0 in LinkedHashMap -- if same store, allow add
                LinkedHashMap<Item, Integer> currentCart = FirebaseHandler.getCurrentCart();
                if (currentCart.isEmpty()
                        || currentCart.keySet().iterator().next().getStoreID().equals(storeID)) {
                    Item itemToAdd = store.getItem(itemID);

                    boolean itemExists = false;
                    if (!currentCart.isEmpty()) {
                        for (Item cartItem : currentCart.keySet()) {
                            if (cartItem.getItemID().equals(itemID)) {
                                currentCart.replace(cartItem, currentCart.get(cartItem) + 1);
                                itemExists = true;
                                break;
                            }
                        }
                    } if (!itemExists) currentCart.put(itemToAdd, 1);

                    Toast.makeText(c, "Added item to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("Error adding to cart: Items from multiple stores");
                    Toast.makeText(c, "Only one store per order!", Toast.LENGTH_SHORT).show();
                } System.out.println("Done ADDING! Current cart: " + currentCart.entrySet());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public static void removeFromCart(String storeID, String itemID) {
        FirebaseHandler.STORES_REF.child(storeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Store store = snapshot.getValue(Store.class);
                if (store == null) return;

                LinkedHashMap<Item, Integer> currentCart = FirebaseHandler.getCurrentCart();

                if (!currentCart.isEmpty()) {
                    for (Item cartItem : currentCart.keySet()) {
                        if (cartItem.getItemID().equals(itemID)) {
                            if (currentCart.get(cartItem) > 1) currentCart.replace(cartItem, currentCart.get(cartItem) - 1);
                            else currentCart.remove(cartItem);

                            System.out.println("Done REMOVING! Current cart: " + currentCart.entrySet());
                            return;
                        }
                    }
                } System.out.println("I CAN'T REMOVE THIS!!!!!!!!!!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public static double getCartPrice(LinkedHashMap<Item, Integer> cart) {
        if (cart.isEmpty()) return 0.00;
        double total = 0.00;
        for (Item i : cart.keySet()) {
            total += (i.getItemPrice() * cart.get(i));
        } return Double.parseDouble(String.format("%.2f", total));
    }


    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                System.out.println("Image failed to fetch from URL");
                throw new RuntimeException();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    private ViewGroup getCartListItem(String itemName, String storeName, String storeAddress, double itemPrice, int itemQuantity) {
        ViewGroup listItem = (ViewGroup) LayoutInflater.from(contextApp).inflate(R.layout.cart_list_view, null);
        TextView nameField = (TextView) listItem.getChildAt(1);
        TextView storeField = (TextView) listItem.getChildAt(3);
        TextView locationField = (TextView) listItem.getChildAt(4);
        TextView priceField = (TextView) listItem.getChildAt(5);
        TextView quantityField = (TextView) listItem.getChildAt(6);

        nameField.setText(itemName);
        storeField.setText(storeName);
        locationField.setText(storeAddress);
        priceField.setText("RM " + String.format("%.2f", itemPrice));
        quantityField.setText(Integer.toString(itemQuantity));

        storeField.setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery(storeName);
            startActivity(new Intent(CartPage.this, SearchResultsPage.class));
            finish();
        });

        locationField.setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery(storeAddress);
            finish();
        });

        return listItem;
    }

    private void addNewListItem(String itemName, String storeName, String storeAddress, double itemPrice, int itemQuantity, String imageURL) {
        ViewGroup newListItem = getCartListItem(itemName, storeName, storeAddress, itemPrice, itemQuantity);
        ImageView itemImage = (ImageView) newListItem.getChildAt(0);
        new CartPage.DownloadImageFromInternet(itemImage).execute(imageURL);

        mainLayout.addView(newListItem, mainLayout.getChildCount() - NUMBER_OF_CHILDREN_AFTER_LIST_ITEMS);
    }

    private void getCartView() {
        FirebaseHandler.STORES_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Store store = snapshot.child(
                        FirebaseHandler.getCurrentCart().keySet().iterator().next().getStoreID()
                ).getValue(Store.class);

                ((TextView) findViewById(R.id.cartStoreName)).setText(store.getStoreName());

                for (Map.Entry<Item, Integer> i : FirebaseHandler.getCurrentCart().entrySet()) {
                    Item item = i.getKey();
                    int quantity = i.getValue();

                    addNewListItem(item.getItemName(), store.getStoreName(), store.getStoreAddress(), item.getItemPrice(), quantity, item.getItemImageURL());
                }

                ((TextView) findViewById(R.id.subtotal_text)).setText(
                        "RM " + String.format("%.2f", getCartPrice(FirebaseHandler.getCurrentCart()))
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public void clearCartHandler(View v) {
        FirebaseHandler.clearCart();
        Toast.makeText(this, "Cart has been emptied!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(CartPage.this, HomePage.class));
        finish();
    }

    public void addMoreItemsHandler(View v) {
        SearchResultsPage.setCurrentSearchQuery("");
        startActivity(new Intent(CartPage.this, SearchResultsPage.class));
        finish();
    }
    public void checkOutHandler(View v) {
        (findViewById(R.id.checkout_button)).setEnabled(false);
        (findViewById(R.id.clear_cart_button)).setEnabled(false);
        (findViewById(R.id.add_more_button)).setEnabled(false);

        startActivity(new Intent(CartPage.this, CheckoutPage.class));

        (findViewById(R.id.checkout_button)).setEnabled(true);
        (findViewById(R.id.clear_cart_button)).setEnabled(true);
        (findViewById(R.id.add_more_button)).setEnabled(true);
    }

    public void backButtonHandler(View v) {
        finish();
    }

    private void showEmptyCartDisplay() {
        findViewById(R.id.cartStoreName).setVisibility(View.GONE);
        findViewById(R.id.subtotal_label).setVisibility(View.GONE);
        findViewById(R.id.subtotal_text).setVisibility(View.GONE);
        findViewById(R.id.footer_button_menu).setVisibility(View.GONE);

        findViewById(R.id.emptyCartContainer).setVisibility(View.VISIBLE);
    }

    private void initCart() {
        if (!FirebaseHandler.getCurrentCart().isEmpty()) getCartView();
        else showEmptyCartDisplay();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        contextApp = this;
        mainLayout = findViewById(R.id.home_mainLayout);
        initCart();
    }

    protected void onRestart() {
        super.onRestart();
        if (FirebaseHandler.getCurrentCart().isEmpty()) finish();
    }
}