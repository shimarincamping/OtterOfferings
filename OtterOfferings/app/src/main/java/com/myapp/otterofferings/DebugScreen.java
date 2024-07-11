package com.myapp.otterofferings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Random;

import at.favre.lib.crypto.bcrypt.BCrypt;

// THIS IS THE TEST ACTIVITY!!!! THIS ISN'T THE REAL LOGIN PAGE

public class DebugScreen extends AppCompatActivity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String sanitiseEmail(String email) {
        return email.replace(".", "");
    }

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    private static String getTokenJSON(String email, String token) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(new LoginToken(email, token));
    }

    private void writeLoginToken(File file, String data) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void savePrivately(String data) {
        File folder = getExternalFilesDir("OtterOfferings");
        File file = new File(folder, "otter-logintoken.json");
        writeLoginToken(file, data);
    }

    private void registerUser(String fullName, String email, String password) {
        FirebaseHandler.USERS_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(sanitiseEmail(email)).exists()) {
                    FirebaseHandler.USERS_REF.child(sanitiseEmail(email)).setValue(new User(fullName, email, password));
                } else System.out.println("USER ALREADY EXISTS!!!!!!!!!!!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    private void loginUser(String email, String passwordAttempt) {
        FirebaseHandler.USERS_REF.child(sanitiseEmail(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userToLogin = snapshot.getValue(User.class);

                if (userToLogin != null
                        && BCrypt.verifyer().verify(passwordAttempt.toCharArray(), userToLogin.getHashedPassword()).verified) {

                    FirebaseHandler.setCurrentUser(userToLogin);
                    System.out.println("Success!!!! LOGIN!!! GOOOD!!!! CURRENT USER:" + FirebaseHandler.getCurrentUser().getUserID());

                    String newToken = Integer.toString((new Random()).nextInt(10000000));
                    FirebaseHandler.USERS_REF.child(FirebaseHandler.getCurrentUser().getUserID()).child("loginToken").setValue(newToken);

                    try {
                        String tokenJSON = getTokenJSON(FirebaseHandler.getCurrentUser().getUserID(), hashPassword(newToken));
                        savePrivately(tokenJSON);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // Allow LOGIN - change Screen to Home
                } else System.out.println("FAIL!!!!!!! BAD!!!!!!!!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public void logoutUser() throws IOException {
        savePrivately(getTokenJSON("", ""));
        FirebaseHandler.setCurrentUser(null);
    }

    public void checkLoginToken() throws IOException {
        LoginToken loginToken = OBJECT_MAPPER.readValue(new File(getExternalFilesDir("OtterOfferings"), "otter-logintoken.json"), LoginToken.class);
        if (!(loginToken.getLoginEmail().isEmpty() && loginToken.getLoginToken().isEmpty())) {
            FirebaseHandler.USERS_REF.child(sanitiseEmail(loginToken.getLoginEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userToLogin = snapshot.getValue(User.class);

                    if (userToLogin != null
                            && BCrypt.verifyer().verify(userToLogin.getLoginToken().toCharArray(), loginToken.getLoginToken()).verified) {

                        FirebaseHandler.setCurrentUser(userToLogin);
                        System.out.println("Success!!!! LOGIN!!! GOOOD!!!! CURRENT USER:" + FirebaseHandler.getCurrentUser().getUserID());
                        // Allow LOGIN - change Screen to Home
                    } else System.out.println("FAIL!!!!!!! BAD!!!!!!!!");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println("The read failed: " + error.getCode());
                }
            });
        } else System.out.println("owo? deres nothing here");
    }

    public void registerHandler(View v) {
        registerUser("Sunny", "sunny@gmail.com", "123");
    }

    public void loginHandler(View v) {
        loginUser("sunny@gmail.com", "123");
    }

    public void logoutHandler(View v) throws IOException {
        logoutUser();
        Intent i = new Intent(DebugScreen.this, LoginPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void validateLoginToken() {
        try {
            checkLoginToken();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }


    // ADDITIONAL FUNCTIONS - MEANT FOR OTHER SCREENS

    public void createNewStore(String storeID, String storeName, String storeAddress, String storeLocation, String openTime, String closeTime) {
        FirebaseHandler.STORES_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(storeID).exists()) {
                    FirebaseHandler.STORES_REF.child(storeID).setValue(new Store(storeID, storeName, storeAddress, storeLocation, openTime, closeTime));
                } else System.out.println("STORE ALREADY EXISTS!!!!!!!!!!!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public void addItemToStore(Item i) {
        FirebaseHandler.STORES_REF.child(i.getStoreID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Store store = snapshot.getValue(Store.class);
                if (store == null) return;

                if (store.getStoreItems() != null) {
                    for (Item storeItem : store.getStoreItems()) {
                        if (storeItem.getItemID().equals(i.getItemID())) {
                            System.out.println("ITEM ID ALREADY EXISTS!!!!!!!!!!!");
                            return;
                        }
                    }
                } store.addItem(i);
                FirebaseHandler.STORES_REF.child(i.getStoreID()).setValue(store);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public void addToCart(String storeID, String itemID) {
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
                } else System.out.println("CANT ADD ITEMS FROM DIFFERENT STORESS!!!");

                System.out.println("Done ADDING! Current cart: " + currentCart.entrySet());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });
    }

    public void removeFromCart(String storeID, String itemID) {
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

    private double getCartPrice(LinkedHashMap<Item, Integer> cart) {
        if (cart.isEmpty()) return 0.00;
        double total = 0.00;
        for (Item i : cart.keySet()) {
            total += (i.getItemPrice() * cart.get(i));
        } return total;
    }
    public void checkoutCart(int minutesToReceive) {
        Order newOrder = new Order(
                FirebaseHandler.getCurrentUser().getUserID(),
                FirebaseHandler.getCurrentCart().keySet().iterator().next().getStoreID(),
                FirebaseHandler.getCurrentCart(),
                getCartPrice(FirebaseHandler.getCurrentCart()),
                minutesToReceive
        );

        FirebaseHandler.ORDERS_REF.child(newOrder.getOrderID()).setValue(newOrder);
    }



    public void addToCartOne(View v) {
        addToCart("DFM-01", "DFM-01-01");
    }

    public void addToCartTwo(View v) {
        addToCart("DFM-01", "DFM-01-02");
    }

    public void removeFromCartOne(View v) {
        removeFromCart("DFM-01", "DFM-01-01");
    }

    public void removeFromCartTwo(View v) {
        removeFromCart("DFM-01", "DFM-01-02");
    }

    public void checkoutCartHandler(View v) {
        checkoutCart(15);
    }

    // END OF ADDITIONAL FUNCTIONS

    // IMAGE TEST
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
                System.out.println("SOMETHING WENT WRONG!!!! IN IMAGE!!");
                throw new RuntimeException();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    // END OF IMAGE TEST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.debug_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check for token? If valid, log in automatically
        validateLoginToken();

        new DownloadImageFromInternet(findViewById(R.id.ivTest)).execute("https://static01.nyt.com/images/2023/07/21/multimedia/21baguettesrex-hbkc/21baguettesrex-hbkc-superJumbo.jpg");

        /*
        createNewStore("SLA-01",
                "Salad Atelier",
                "Sunway Pyramid",
                "N/A",
                "8:00",
                "17:00"
        );


        addItemToStore(new Item("FMM-01",
                        "05",
                        "Matcha Roll Cake",
                        2.99,
                        "15/07/2024",
                        5,
                        "https://familymart.com.my/img/freshfood/bakery/ff-bakery01.jpg")
        );
        */
    }
}