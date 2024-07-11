package com.myapp.otterofferings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class HomePage extends AppCompatActivity {

    private Context contextApp;

    private final String PROMO_BANNER = "https://i.imgur.com/xb5Ngzp.png";

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

    private ViewGroup getOfferingListItem(String storeID, String itemID, String itemName, String storeName, String storeAddress, double itemPrice) {
        ViewGroup listItem = (ViewGroup) LayoutInflater.from(contextApp).inflate(R.layout.item_list_view, null);
        TextView nameField = (TextView) listItem.getChildAt(1);
        TextView storeField = (TextView) listItem.getChildAt(3);
        TextView locationField = (TextView) listItem.getChildAt(4);
        TextView priceField = (TextView) listItem.getChildAt(5);
        Button addToCartButton = (Button) listItem.getChildAt(6);

        nameField.setText(itemName);
        storeField.setText(storeName);
        locationField.setText(storeAddress);
        priceField.setText("RM " + String.format("%.2f", itemPrice));
        addToCartButton.setTag(itemID);

        storeField.setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery(storeName);
            startActivity(new Intent(HomePage.this, SearchResultsPage.class));
        });

        locationField.setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery(storeAddress);
            startActivity(new Intent(HomePage.this, SearchResultsPage.class));
        });

        addToCartButton.setOnClickListener(v -> {
            CartPage.addToCart(this, storeID, itemID);
        });

        return listItem;
    }

    private void addNewListItem(String storeID, String itemID, String itemName, String storeName, String storeAddress, double itemPrice, String imageURL) {
        ViewGroup newListItem = getOfferingListItem(storeID, itemID, itemName, storeName, storeAddress, itemPrice);
        ImageView itemImage = (ImageView) newListItem.getChildAt(0);
        new HomePage.DownloadImageFromInternet(itemImage).execute(imageURL);

        ViewGroup mainLayout = findViewById(R.id.home_mainLayout);
        mainLayout.addView(newListItem);
    }

    private ArrayList<Item> getRandomWithoutDuplicate(ArrayList<DataSnapshot> input, int howMany) {
        ArrayList<Item> newList = new ArrayList<>();

        Collections.shuffle(input);

        for (DataSnapshot i : input) {
            Store store = i.getValue(Store.class);

            if (store.getStoreItems() != null) {
                int randomItemFromStore = (new Random()).nextInt(store.getStoreItems().size());

                if (!newList.contains(store.getItem(randomItemFromStore)))
                    newList.add(store.getItem(randomItemFromStore));
                if (newList.size() >= howMany) break;
            }
        }

        return newList;
    }

    private void fetchRecommendedOfferings() {
        FirebaseHandler.STORES_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<DataSnapshot> stores = new ArrayList<>();
                snapshot.getChildren().forEach(stores::add);

                ArrayList<Item> itemsToDisplay = getRandomWithoutDuplicate(stores, 5);
                for (Item i : itemsToDisplay) {
                    Store itemStore = snapshot.child(i.getStoreID()).getValue(Store.class);

                    addNewListItem(itemStore.getStoreID(), i.getItemID(), i.getItemName(), itemStore.getStoreName(), itemStore.getStoreAddress(), i.getItemPrice(), i.getItemImageURL());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    private void initSearchBar() {
        EditText searchBar = findViewById(R.id.searchBar);

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                SearchResultsPage.setCurrentSearchQuery(searchBar.getText().toString());
                startActivity(new Intent(HomePage.this, SearchResultsPage.class));
                handled = true;
            }
            return handled;
        });
    }

    private void initAddress() {
        try {
            if (!FirebaseHandler.getCurrentUser().getSavedAddress().isEmpty())
                ((TextView) findViewById(R.id.home_addressBox)).setText(FirebaseHandler.getCurrentUser().getSavedAddress());
        } catch (NullPointerException e) {
            ((TextView) findViewById(R.id.home_addressBox)).setText(getResources().getString(R.string.no_address_found));
        }
    }

    private void initUI() {
        new HomePage.DownloadImageFromInternet(findViewById(R.id.dealsContainer)).execute(PROMO_BANNER);
        fetchRecommendedOfferings();
        initSearchBar();
        initAddress();

        findViewById(R.id.home_cartButton).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, CartPage.class));
        });

        findViewById(R.id.shopButton).setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery("");
            startActivity(new Intent(HomePage.this, SearchResultsPage.class));
        });

        findViewById(R.id.activityButton).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, MyActivityPage.class));
        });

        findViewById(R.id.profileButton).setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, ProfilePage.class));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        contextApp = this;
        initUI();
        // addNewListItem("Test", "Test", "Test", "Test", "Test", "https://steamuserimages-a.akamaihd.net/ugc/5863198105381781422/D3E6731CDC24B66CD89C12EC4FD9A99F4B0E05DA/?imw=512&&ima=fit&impolicy=Letterbox&imcolor=%23000000&letterbox=false");
        // addNewListItem("Test2", "Test2", "Test2", "Test2", "Test2","https://s1.zerochan.net/Yoru.no.Kurage.wa.Oyogenai.600.4171166.jpg");
        // addNewListItem("Test3", "Test3", "Test3", "Test3", "Test3", "https://i.pinimg.com/736x/eb/73/71/eb7371c15a8ca772874cba3c0f33dea8.jpg");
    }

    protected void onRestart() {
        super.onRestart();
        initAddress();
    }
}