package com.myapp.otterofferings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProfilePage extends AppCompatActivity {

    EditText fullName;
    EditText phoneNumber;
    EditText profileLink;
    EditText email;
    EditText password;

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

    private void savePrivately(String data) {
        File folder = getExternalFilesDir("OtterOfferings");
        File file = new File(folder, "otter-logintoken.json");
        LoginPage.writeLoginToken(file, data);
    }

    public void logoutUser() throws IOException {
        savePrivately(LoginPage.getTokenJSON("", ""));
        FirebaseHandler.setCurrentUser(null);
    }

    public void logoutHandler(View v) throws IOException {
        logoutUser();
        Intent i = new Intent(ProfilePage.this, LoginPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public void showUnableToChangeText(View v) {
        (findViewById(R.id.unableToChangeTextField)).setVisibility(View.VISIBLE);
    }

    private void getCurrentUserInformation() {
        User user = FirebaseHandler.getCurrentUser();
        fullName.setText(user.getFullName());
        phoneNumber.setText(user.getPhoneNumber());
        profileLink.setText(user.getProfileImageURL());
        email.setText(user.getEmail());
        password.setText("YOUR PASSWORD");

        new ProfilePage.DownloadImageFromInternet(findViewById(R.id.profileImg)).execute(user.getProfileImageURL());
    }

    public void saveUserInformation(View v) {
        (findViewById(R.id.save_profile_button)).setEnabled(false);
        ((TextView) findViewById(R.id.register_errorBox)).setText(getResources().getString(R.string.null_string));

        String inputFullName = fullName.getText().toString();
        String inputPhoneNumber = phoneNumber.getText().toString();
        String inputProfileLink = profileLink.getText().toString();

        if (!inputFullName.isEmpty() && !inputPhoneNumber.isEmpty() && !inputProfileLink.isEmpty()) {
            User newUser = FirebaseHandler.getCurrentUser();
            newUser.setFullName(inputFullName);
            newUser.setPhoneNumber(inputPhoneNumber);
            newUser.setProfileImageURL(inputProfileLink);

            FirebaseHandler.setCurrentUser(newUser);
            FirebaseHandler.USERS_REF.child(newUser.getUserID()).setValue(newUser);
            Toast.makeText(this, "Details have been saved!", Toast.LENGTH_SHORT).show();

            getCurrentUserInformation();
        } else {
            ((TextView) findViewById(R.id.register_errorBox)).setText(getResources().getString(R.string.empty_box));
        }

        (findViewById(R.id.save_profile_button)).setEnabled(true);
    }

    public void manageAddressHandler(View v) {
        startActivity(new Intent(ProfilePage.this, Maps.class));
    }

    private void initUI() {
        getCurrentUserInformation();

        findViewById(R.id.discoverButton).setOnClickListener(v -> {
            startActivity(new Intent(ProfilePage.this, HomePage.class));
        });

        findViewById(R.id.shopButton).setOnClickListener(v -> {
            SearchResultsPage.setCurrentSearchQuery("");
            startActivity(new Intent(ProfilePage.this, HomePage.class));
        });

        findViewById(R.id.activityButton).setOnClickListener(v -> {
            startActivity(new Intent(ProfilePage.this, MyActivityPage.class));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullName = findViewById(R.id.editFullName);
        phoneNumber = findViewById(R.id.editPhoneNumber);
        profileLink = findViewById(R.id.editProfileImageURL);
        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.editPassword);

        initUI();
    }
}