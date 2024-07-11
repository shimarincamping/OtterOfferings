package com.myapp.otterofferings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.Random;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LoginPage extends AppCompatActivity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Button loginButton;
    Button goToRegisterButton;
    TextView errorBox;

    public static String sanitiseEmail(String email) {
        return email.replace(".", "");
    }

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static String getTokenJSON(String email, String token) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(new LoginToken(email, token));
    }

    private void goToHomeScreen() {
        Intent i = new Intent(LoginPage.this, HomePage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void disableButtons() {
        loginButton.setEnabled(false);
        goToRegisterButton.setEnabled(false);
    }

    private void enableButtons() {
        loginButton.setEnabled(true);
        goToRegisterButton.setEnabled(true);
    }

    public static void writeLoginToken(File file, String data) {
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

    private void loginUser(String email, String passwordAttempt) {
        disableButtons();
        errorBox.setText(getResources().getString(R.string.null_string));

        FirebaseHandler.USERS_REF.child(sanitiseEmail(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userToLogin = snapshot.getValue(User.class);

                if (userToLogin != null
                        && BCrypt.verifyer().verify(passwordAttempt.toCharArray(), userToLogin.getHashedPassword()).verified) {

                    FirebaseHandler.setCurrentUser(userToLogin);
                    System.out.println("Login was successful; CURRENT USER: " + FirebaseHandler.getCurrentUser().getUserID());

                    String newToken = Integer.toString((new Random()).nextInt(10000000));
                    FirebaseHandler.getCurrentUser().setLoginToken(newToken);
                    FirebaseHandler.USERS_REF.child(FirebaseHandler.getCurrentUser().getUserID()).child("loginToken").setValue(newToken);

                    try {
                        String tokenJSON = getTokenJSON(FirebaseHandler.getCurrentUser().getUserID(), hashPassword(newToken));
                        savePrivately(tokenJSON);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    enableButtons();
                    goToHomeScreen();
                } else {
                    System.out.println("Login was unsuccessful: Incorrect credentials");
                    errorBox.setText(getResources().getString(R.string.wrong_credentials));
                    enableButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
                enableButtons();
            }
        });
    }

    public void checkLoginToken() throws IOException {

        File tokensFolder = getExternalFilesDir("OtterOfferings");
        File tokensFile = new File(tokensFolder, "otter-logintoken.json");

        if (!tokensFile.exists()) {
            tokensFile.createNewFile();
            savePrivately(getTokenJSON("", ""));
        }

        LoginToken loginToken = OBJECT_MAPPER.readValue(tokensFile, LoginToken.class);
        if (!(loginToken.getLoginEmail().isEmpty() && loginToken.getLoginToken().isEmpty())) {
            FirebaseHandler.USERS_REF.child(sanitiseEmail(loginToken.getLoginEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userToLogin = snapshot.getValue(User.class);

                    if (userToLogin != null
                            && BCrypt.verifyer().verify(userToLogin.getLoginToken().toCharArray(), loginToken.getLoginToken()).verified) {

                        FirebaseHandler.setCurrentUser(userToLogin);
                        System.out.println("Login was successful; CURRENT USER: " + FirebaseHandler.getCurrentUser().getUserID());

                        goToHomeScreen();
                    } else {
                        System.out.println("Token could not be verified, possible tampering or writing malfunction.");
                        enableButtons();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println("The read failed: " + error.getCode());
                    enableButtons();
                }
            });
        } else {
            System.out.println("No login token found. Enabling login page.");
            enableButtons();
        }
    }

    public void loginHandler(View v) {
        TextView email = findViewById(R.id.login_email);
        TextView password = findViewById(R.id.login_password);
        String inputEmail = email.getText().toString();
        String inputPassword = password.getText().toString();

        if (!inputEmail.isEmpty() && !inputPassword.isEmpty()) loginUser(inputEmail, inputPassword);
        else errorBox.setText(getResources().getString(R.string.empty_box));
    }

    public void goToRegisterHandler(View v) {
        startActivity(new Intent(LoginPage.this, RegisterPage.class));
    }
    private void validateLoginToken() {
        try {
            checkLoginToken();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton = findViewById(R.id.login_button);
        goToRegisterButton = findViewById(R.id.go_to_register_button);
        errorBox = findViewById(R.id.login_errorBox);
        disableButtons();
        validateLoginToken();
    }
}