package com.example.cb_f;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;


public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextPassword, editTextemail, phoneNo, firstName, aadhar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private ProgressBar progressBar;
    private View signup_b;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        findViewById(R.id.signupButton).setOnClickListener(this);
        editTextemail = findViewById(R.id.emailBox);
        editTextPassword = findViewById(R.id.passBox);
        aadhar = findViewById(R.id.aadhar);
        mAuth = FirebaseAuth.getInstance();
        firstName = findViewById(R.id.firstName);
        phoneNo = findViewById(R.id.phoneNo);
        progressBar = findViewById(R.id.progress_bar);
        signup_b = findViewById(R.id.signupButton);
        logo = findViewById(R.id.logo);
        //cbox = findViewById(R.id.checkBox2);

    }

    public void registerUser() {
        final String email = editTextemail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String mFirstName = firstName.getText().toString().trim();
        final String mPhone = phoneNo.getText().toString().trim();
        final String aadhar_no = aadhar.getText().toString().trim();


        if (email.isEmpty()) {
            editTextemail.setError("Email is required");
            editTextemail.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextemail.setError("Please enter a valid Email");
            editTextemail.requestFocus();

            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();

            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Please enter atleast 6 character password");
            editTextPassword.requestFocus();

            return;
        }
        if (phoneNo.length() < 10) {
            phoneNo.setError("Please enter valid Phone Number");
            phoneNo.requestFocus();

            return;
        }

        if (firstName.getText() == null) {
            firstName.setError("Please enter valid First Name");
            firstName.requestFocus();

            return;
        }

        if (aadhar.getText() == null || aadhar.getText().length() != 12) {
            aadhar.setError("Please enter valid Aadhar Number");
            aadhar.requestFocus();

            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String Id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final user user1 = new user(mPhone, mFirstName, email, aadhar_no, Id);
                    updatedisplayname(mFirstName);
                    userRef.child(Id)
                            .setValue(user1)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendVerificationEmail();
                                    Log.d("Reg", "onSuccess: Databse updated");
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    finish();
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Reg", "onFailure: " + e.getMessage());
                                }
                            });
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "User already exist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);

            }
        });

    }

    private void sendVerificationEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        // Re-enable button
                        /*findViewById(R.id.verify_email_button).setEnabled(true);*/

                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this,
                                    "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("Verification", "sendEmailVerification", task.getException());
                            Toast.makeText(SignupActivity.this,
                                    "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updatedisplayname(String mFirstName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mFirstName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("ProfileUpdate", "User profile updated.");
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.signupButton):
                registerUser();
                break;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignupActivity.this, Pair.create(signup_b, "signupTransition"));//, Pair.create((View)logo,"logoTransition"));
        startActivity(intent, options.toBundle());
    }


}
