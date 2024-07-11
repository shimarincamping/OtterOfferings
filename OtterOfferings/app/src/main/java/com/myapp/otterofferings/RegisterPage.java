package com.myapp.otterofferings;

import static com.myapp.otterofferings.LoginPage.sanitiseEmail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class RegisterPage extends AppCompatActivity {

    TextView errorBox;
    Button registerButton;
    Button goToLoginButton;

    private void disableButtons() {
        registerButton.setEnabled(false);
        goToLoginButton.setEnabled(false);
    }

    private void enableButtons() {
        registerButton.setEnabled(true);
        goToLoginButton.setEnabled(true);
    }

    private void goToLoginPage() {
        startActivity(new Intent(RegisterPage.this, LoginPage.class));
        finish();
    }

    private void registerUser(String fullName, String email, String password) {
        FirebaseHandler.USERS_REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(sanitiseEmail(email)).exists()) {
                    FirebaseHandler.USERS_REF.child(sanitiseEmail(email)).setValue(new User(fullName, email, password));
                    Toast.makeText(RegisterPage.this, "Success! Please log in again", Toast.LENGTH_LONG).show();
                    System.out.println("Successfully registered new user: " + email);
                    enableButtons();
                    goToLoginPage();
                } else {
                    System.out.println("Error creating new user: User already exists");
                    errorBox.setText(getResources().getString(R.string.user_already_exists));
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

    public void registerHandler(View v) {
        disableButtons();

        TextView fullName = findViewById(R.id.register_fullName);
        TextView email = findViewById(R.id.register_email);
        TextView password = findViewById(R.id.register_password);
        TextView confirmPassword = findViewById(R.id.register_confirmPassword);

        String inputFullName = fullName.getText().toString();
        String inputEmail = email.getText().toString();
        String inputPassword = password.getText().toString();
        String inputConfirmPassword = confirmPassword.getText().toString();

        if (!inputFullName.isEmpty() && !inputEmail.isEmpty() && !inputPassword.isEmpty() && !inputConfirmPassword.isEmpty()) {
            if (inputPassword.equals(inputConfirmPassword)) {
                registerUser(inputFullName, inputEmail, inputPassword);
            } else {
                errorBox.setText(getResources().getString(R.string.password_not_matching));
                enableButtons();
            }
        } else {
            errorBox.setText(getResources().getString(R.string.empty_box));
            enableButtons();
        }
    }

    public void goToLoginHandler(View v) {
        goToLoginPage();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        errorBox = findViewById(R.id.register_errorBox);
        registerButton = findViewById(R.id.register_button);
        goToLoginButton = findViewById(R.id.go_to_login_button);

        enableButtons();
    }
}