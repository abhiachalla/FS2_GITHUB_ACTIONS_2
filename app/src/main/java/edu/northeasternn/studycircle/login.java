package edu.northeasternn.studycircle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.commons.validator.routines.EmailValidator;

public class login extends AppCompatActivity {

    private EditText email, password;
    private ProgressBar progressBar;

    private TextView registerAccount;
    private Button loginBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        View activityLayout = findViewById(android.R.id.content);

        auth = FirebaseAuth.getInstance();

        EmailValidator validator = EmailValidator.getInstance();
        email = findViewById(R.id.email);
        progressBar = findViewById(R.id.progressBar);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        registerAccount = findViewById(R.id.registerLink);
        registerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this, MainActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String emailText = email.getText().toString();
                final String pwd = password.getText().toString();

                if (!validator.isValid(emailText)) {
                    Snackbar.make(activityLayout, "Invalid email address", Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if (TextUtils.isEmpty(pwd)) {
                    Snackbar.make(activityLayout, "Password cannot be empty", Snackbar.LENGTH_LONG).show();;
                    return;
                } else if(pwd.length() < 8){
                    Snackbar.make(activityLayout, "Password cannot less than 8 characters long", Snackbar.LENGTH_LONG).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                auth.signInWithEmailAndPassword(emailText, pwd)
                        .addOnSuccessListener(login.this, new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(login.this, MainActivity.class));
                            }
                        })
                        .addOnFailureListener(login.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Snackbar.make(activityLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                return;
                            }
                        });
            }
        });


    }
}