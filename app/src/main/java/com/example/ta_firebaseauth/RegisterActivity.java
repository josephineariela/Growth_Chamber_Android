package com.example.ta_firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    //Initialize variables
    private Button buttonSignUp;
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhoneNumber, editTextInstance;
    private TextView textViewSignIn;
    private ProgressBar progressBar;

    //Initialize Firebase Authentication
    private FirebaseAuth mAuth;
    DatabaseReference reff;

    SaveRegInformation reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Authentification
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database
        reff = FirebaseDatabase.getInstance().getReference().child("DatasetUser");
        reg = new SaveRegInformation();

        //FirebaseUser user = mAuth.getCurrentUser();

        findID();
        setOnClick();

    }

    //HomeActivity will open if user account is not NULL
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
    }

    private void registerUser(){
        final String name = editTextName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repass = editTextConfirmPassword.getText().toString().trim();
        final String instance = editTextInstance.getText().toString().trim();
        //final String occupation = editTextOccupation.getText().toString().trim();
        final String phone = editTextPhoneNumber.getText().toString().trim();

        //To make sure every category is filled
        if (name.isEmpty()){
            //email is empty
            //Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            //stop the function for executing further
            editTextName.setError("Please enter your full name");
            editTextName.requestFocus();
            return;
        }
        if (email.isEmpty()){
            //email is empty
            //Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            //stop the function for executing further
            editTextEmail.setError("Please enter your email");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid Email!");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            //password is empty
            //Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            //stop the function for executing further
            editTextPassword.setError("Please enter your password");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length()<6){
            editTextPassword.setError("Min. password length is 6 characters!");
            editTextPassword.requestFocus();
            return;
        }
        if (!(repass.equals(password))){
            editTextConfirmPassword.setError("Password confirmation doesn't match Password");
            editTextConfirmPassword.requestFocus();
            return;
        }
        if (phone.isEmpty()){
            //password is empty
            //Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            //stop the function for executing further
            editTextPhoneNumber.setError("Please enter your phone number");
            editTextPhoneNumber.requestFocus();
            return;
        }
        if (instance.isEmpty()){
            editTextInstance.setError("Instance is Required!");
            editTextInstance.requestFocus();
            return;
        }
        /*
        if (occupation.isEmpty()){
            editTextOccupation.setError("Occupation is Required!");
            editTextOccupation.requestFocus();
            return;
        }
        */

        //if validation are ok
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"User Registered Successful", Toast.LENGTH_LONG).show();
                            reg.setName(name);
                            reg.setEmail(email);
                            reg.setInstance(instance);
                            //reg.setOccupation(occupation);
                            reg.setPhone(phone);
                            reg.setStatus("Member");
                            reff.push().setValue(reg);
                            startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                                                        /*
                            //Saving name, email, and phone number information in Firebase Database
                            final SaveRegInformation saveRegInformation = new SaveRegInformation(name, email, phoneNumber);
                            FirebaseDatabase.getInstance().getReference("UserRegInfo")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(saveRegInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Send message register succeed
                                        Toast.makeText(RegisterActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();

                                    } else {
                                        //Send message fail to registered
                                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            saveRegInformation.setName(name);
                            saveRegInformation.setEmail(email);
                            saveRegInformation.setPhone(phoneNumber);
                            //will open login activity
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            */

                        } else {

                            // If sign in fails, display a message to the user.
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "You already signed in with this account!", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

    }

    private void findID(){
        //Syncing XML variables
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextConfirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        //editTextOccupation = (EditText) findViewById(R.id.editTextOccupation);
        editTextInstance = (EditText) findViewById(R.id.editTextInstance);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);
    }

    private void setOnClick(){
        //Initialize button to do a function / open an activity
        buttonSignUp.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
    }

    //Function when button or textView is clicked
    @Override
    public void onClick(View v) {
        if (v == buttonSignUp){
            registerUser();
        }

        if (v == textViewSignIn){
            //will open login activity
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
