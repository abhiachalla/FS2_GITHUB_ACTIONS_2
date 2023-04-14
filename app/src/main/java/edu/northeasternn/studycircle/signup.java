package edu.northeasternn.studycircle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.Date;

import edu.northeasternn.studycircle.model.User;

public class signup extends AppCompatActivity {


    private TextInputEditText firstName, password, repassword,email,
            lastname, location;
    private TextView signupHeader;
    private Button registerBtn;
    private ProgressBar progressBar;


    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        EmailValidator validator = EmailValidator.getInstance();

        registerBtn = findViewById(R.id.registerBtn);
        View activityLayout = findViewById(android.R.id.content);
        firstName = findViewById(R.id.regFirstName);
        password = findViewById(R.id.regPasssword);
        repassword = findViewById(R.id.regRePassword);
        lastname = findViewById(R.id.regLastName);
        location = findViewById(R.id.regLocation);
        email = findViewById(R.id.regEmail);
        progressBar = findViewById(R.id.regProgressBar);
        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String usernameTxt = email.getText().toString().trim().toUpperCase();
                String passwordTxt = password.getText().toString().trim();
                String retype_password = repassword.getText().toString().trim();
                final String firstNameTxt = firstName.getText().toString().trim();
                final String lastNameTxt = lastname.getText().toString().trim();
                final String locationTxt = location.getText().toString().trim();

                if (TextUtils.isEmpty(firstNameTxt)) {
                    Snackbar.make(activityLayout, "Invalid email address", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (TextUtils.isEmpty(lastNameTxt)) {
                    Snackbar.make(activityLayout, "Last Name cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (TextUtils.isEmpty(usernameTxt)) {
                    Snackbar.make(activityLayout, "Email Cannot be Empty", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (TextUtils.isEmpty(locationTxt)) {
                    Snackbar.make(activityLayout, "Location cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (!validator.isValid(usernameTxt)){
                    Snackbar.make(activityLayout, "Invalid Email", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (usernameTxt.contains(" ")) {
                    Snackbar.make(activityLayout, "Email cannot contain space", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (TextUtils.isEmpty(passwordTxt)) {
                    Snackbar.make(activityLayout, "Password cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (firstNameTxt.length() > 25) {
                    Snackbar.make(activityLayout, "First name contains too many characters. Maximum 25 characters allowed", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (lastNameTxt.length() > 25) {
                    Snackbar.make(activityLayout, "Last name contains too many characters. Maximum 25 characters allowed", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (usernameTxt.length() > 40) {
                    Snackbar.make(activityLayout, "Email contains too many characters. Maximum 40 characters allowed", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (locationTxt.length() > 25) {
                    Snackbar.make(activityLayout, "Location contains too many characters. Maximum 40 characters allowed", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (passwordTxt.length() < 6) {
                    Snackbar.make(activityLayout, "Password contains too many characters. Maximum 15 characters allowed", Snackbar.LENGTH_LONG).show();
                    return;
                } else if (!passwordTxt.equals(retype_password)) {
                    Snackbar.make(activityLayout, "Passwords do not match.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(usernameTxt, passwordTxt)
                        .addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Snackbar.make(activityLayout, "Error: Account Creation Failed", Snackbar.LENGTH_LONG).show();
                                }
                                else {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    WriteBatch batch = db.batch();
                                    DocumentReference currentUser = db.collection("users").document(usernameTxt);
                                    batch.set(currentUser,
                                            new User(
                                                    firstNameTxt.substring(0, 1).toUpperCase() + firstNameTxt.substring(1),
                                                    lastNameTxt.substring(0, 1).toUpperCase() + lastNameTxt.substring(1),
                                                    usernameTxt,
                                                    usernameTxt,
                                                    locationTxt.substring(0, 1).toUpperCase() + locationTxt.substring(1),
                                                    String.valueOf(new Timestamp(new Date()).getSeconds())
                                            )
                                    );

                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            Snackbar.make(activityLayout, "Account Creation Successful", Snackbar.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    startActivity(new Intent(signup.this, MainActivity.class));
                                                }
                                            }, 5000);
                                            //startActivity(new Intent(signup.this, MainActivity.class));
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(activityLayout, "User Profile Creation Failed", Snackbar.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        });
            }
        });

    }
}