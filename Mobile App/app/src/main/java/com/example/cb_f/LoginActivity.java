package com.example.cb_f;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.cb_f.R.*;

public class LoginActivity<Button> extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText mEmail,mPassword;
    private ProgressBar progressBar;
    private TextView forgotPassword;
    private View signup_b;
    //TextView error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_login);
        FirebaseApp.initializeApp(this);

        Bundle extras = getIntent().getExtras();
        {
            if(extras!=null){
                FirebaseDatabase.getInstance().getReference("Users").child(extras.getString("UID")).setValue(null);
            }
        }

        findViewById(id.loginButton).setOnClickListener(this);
        findViewById(id.signUp).setOnClickListener(this);
        progressBar = findViewById(id.spin_kit);
        //error = findViewById(R.id.error);
        mEmail = findViewById(id.emailBox);
        mPassword = findViewById(id.passBox);
        mAuth = FirebaseAuth.getInstance();
        forgotPassword = findViewById(id.forgotpassword);
        forgotPassword.setOnClickListener(this);
        signup_b = findViewById(id.signUp);

    }

    public void userRegister(){
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if(email.isEmpty()){
            mEmail.setError("Email is required");
            mEmail.requestFocus();

            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Please enter a valid Email");
            mEmail.requestFocus();

            return;
        }

        if(password.isEmpty()){
            mPassword.setError("Password is required");
            mPassword.requestFocus();

            return;
        }

        if(password.length()<6){
            mPassword.setError("Please enter atleast 6 character password");
            mPassword.requestFocus();

            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    finish();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case(id.signUp):

                finish();
                Intent intent = new Intent(this, SignupActivity.class);
                //getWindow().getSharedElementEnterTransition().setDuration(500);
                startActivity(intent);
                break;
            case(id.loginButton):
                hideKeyboard(LoginActivity.this);
                progressBar.setVisibility(View.VISIBLE);
                userRegister();
                break;
            case (id.forgotpassword):
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                alertDialog.setTitle("Reset Password");
                alertDialog.setMessage("An email with link to reset password will be sent to your registered email.");
                final EditText input = new EditText(LoginActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input); // uncomment this line
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String email = input.getText().toString();
                        if(email.isEmpty()){
                            input.setError("Email is required!");
                            input.setFocusable(true);
                            return;
                        }
                        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                            input.setError("Please enter a valid email");
                            input.setFocusable(true);
                            return;
                        }
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(LoginActivity.this, "An email to reset password has been sent to "+ email, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                task.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
                alertDialog.show();
        }
    }

    @Override
    public void onEnterAnimationComplete() {
        signup_b.setTransitionName(null);
        super.onEnterAnimationComplete();
    }
    public static void hideKeyboard(Activity activity){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view == null){
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

}
