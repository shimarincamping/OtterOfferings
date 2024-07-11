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

public class SearchResultsPage extends AppCompatActivity {

    private Context contextApp;

    private static String currentSearchQuery = "";
    private final int NUMBER_OF_NON_RESULT_CHILDREN = 2;
    private EditText searchBar;
    private ViewGroup mainLayout;

    public static String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    public static void setCurrentSearchQuery(String newSearchQuery) {
        currentSearchQuery = newSearchQuery;
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
            setCurrentSearchQuery(storeName);
            this.recreate();
        });

        locationField.setOnClickListener(v -> {
            setCurrentSearchQuery(storeAddress);
            this.recreate();
        });

        addToCartButton.setOnClickListener(v -> {
            CartPage.addToCart(this, storeID, itemID);
        });

        return listItem;
    }

    private void addNewListItem(String storeID, String itemID, String itemName, String storeName, String storeAddress, double itemPrice, String imageURL) {
        ViewGroup newListItem = getOfferingListItem(storeID, itemID, itemName, storeName, storeAddress, itemPrice);
        ImageView itemImage = (ImageView) newListItem.getChildAt(0);
        new SearchResultsPage.DownloadImageFromInternet(itemImage).execute(imageURL);

        mainLayout.addView(newListItem);
    }

    private boolean isLooselyEqual(String data, String query) {
        return (data.toLowerCase().replaceAll("[^a-zA-Z\\d:]","").contains(query.toLowerCase().replaceAll("[^a-zA-Z\\d:]","")));
    }

    private void addAllItemsInStoreToList(Store store) {
        for (Item i : store.getStoreItems()) {
            addNewListItem(i.getStoreID(), i.getItemID(), i.getItemName(), store.getStoreName(), store.getStoreAddress(), i.getItemPrice(), i.getItemImageURL());
        }
    }

    private void addItemsMatchingQueryFromStoreToList(Store store) {
        for (Item i : store.getStoreItems()) {
            if (isLooselyEqual(i.getItemName(), currentSearchQuery))
                addNewListItem(i.getStoreID(), i.getItemID(), i.getItemName(), store.getStoreName(), store.getStoreAddress(), i.getItemPrice(), i.getItemImageURL());
        }
    }

    private void getSearchResults() {
        FirebaseHandler.STORES_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<DataSnapshot> stores = new ArrayList<>();
                snapshot.getChildren().forEach(stores::add);

                for (DataSnapshot i : stores) {
                    Store store = i.getValue(Store.class);
                    if (isLooselyEqual(store.getStoreName(), currentSearchQuery) || isLooselyEqual(store.getStoreAddress(), currentSearchQuery)) addAllItemsInStoreToList(store);
                    else addItemsMatchingQueryFromStoreToList(store);
                }

                int searchResults = mainLayout.getChildCount() - NUMBER_OF_NON_RESULT_CHILDREN;
                String searchText = (currentSearchQuery.isEmpty()) ? ("All available offerings:")
                        : (searchResults > 1) ? (searchResults + " search results:")
                        : (searchResults > 0) ? ("1 search result:")
                        : ("No results found...");

                ((TextView) findViewById(R.id.searchResultTitle)).setText(searchText);
                searchBar.setText(getCurrentSearchQuery());
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
                setCurrentSearchQuery(searchBar.getText().toString());
                this.recreate();
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
        getSearchResults();
        initSearchBar();
        initAddress();

        findViewById(R.id.home_cartButton).setOnClickListener(v -> {
            startActivity(new Intent(SearchResultsPage.this, CartPage.class));
        });

        findViewById(R.id.discoverButton).setOnClickListener(v -> {
            startActivity(new Intent(SearchResultsPage.this, HomePage.class));
        });

        findViewById(R.id.shopButton).setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery("");
            this.recreate();
        });

        findViewById(R.id.activityButton).setOnClickListener(v -> {
            startActivity(new Intent(SearchResultsPage.this, MyActivityPage.class));
        });

        findViewById(R.id.profileButton).setOnClickListener(v -> {
            startActivity(new Intent(SearchResultsPage.this, ProfilePage.class));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_results_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        contextApp = this;
        searchBar = findViewById(R.id.searchBar);
        mainLayout = findViewById(R.id.home_mainLayout);

        initUI();
    }

    protected void onRestart() {
        super.onRestart();
        initAddress();
    }
}