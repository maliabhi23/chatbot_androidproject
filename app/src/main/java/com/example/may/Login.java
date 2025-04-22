package com.example.may;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Login extends AppCompatActivity {
    private EditText emailTextView, passwordTextView;
    private Button Btn;

    private ProgressBar progressbar;
    TextView forgetpass;
    Button register_btn,otpbtn;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // taking instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // initialising all views through id defined above
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        Btn = findViewById(R.id.login);
        progressbar = findViewById(R.id.progressBar);
        register_btn=findViewById(R.id.register_btn);
        // Set on Click Listener on Sign-in button
        forgetpass=findViewById(R.id.forgetpass);

//        otpbtn=findViewById(R.id.otpbtn);

//        otpbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i=new Intent(Login.this,Login_with_mobilenumber.class);
//                startActivity(i);
//
//            }
//        });
//


        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                loginUserAccount();
            }
        });

            forgetpass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendPasswordResetEmail();
                }
                private void sendPasswordResetEmail() {
                    // Show progress bar while sending the reset email
                    progressbar.setVisibility(View.VISIBLE);

                    String email = emailTextView.getText().toString();

                    // Validate the email input
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getApplicationContext(), "Please enter your email!", Toast.LENGTH_LONG).show();
                        progressbar.setVisibility(View.GONE);
                        return;
                    }

                    // Send password reset email through Firebase Auth
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Inform the user that the email was sent
                                    Toast.makeText(getApplicationContext(), "Password reset email sent!", Toast.LENGTH_LONG).show();
                                } else {
                                    // Inform the user of the failure
                                    Toast.makeText(getApplicationContext(), "Failed to send reset email!", Toast.LENGTH_LONG).show();
                                }
                                progressbar.setVisibility(View.GONE);
                            });
                }



            });
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Login.this,Register.class);
                startActivity(i);

            }
        });


    }

    private void loginUserAccount()
    {

        // show the visibility of progress bar to show loading
        progressbar.setVisibility(View.VISIBLE);

        // Take the value of two edit texts in Strings
        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();

        // validations for input email and password
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter email!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter password!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // signin existing user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(
                                    @NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                                    "Login successful!!",
                                                    Toast.LENGTH_LONG)
                                            .show();

                                    // hide the progress bar
                                    progressbar.setVisibility(View.GONE);

                                    // if sign-in is successful
                                    // intent to home activity
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    startActivity(intent);
                                }

                                else {

                                    // sign-in failed
                                    Toast.makeText(getApplicationContext(),
                                                    "Login failed!!",
                                                    Toast.LENGTH_LONG)
                                            .show();

                                    // hide the progress bar
                                    progressbar.setVisibility(View.GONE);
                                }
                            }
                        });
    }
}
