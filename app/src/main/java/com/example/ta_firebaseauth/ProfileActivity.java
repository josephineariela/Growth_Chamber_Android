package com.example.ta_firebaseauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.viewmodel.RequestCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import static okhttp3.internal.Internal.instance;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView imageViewHome, imageViewLogout, imageViewProfilePic, imageViewProfile, imageViewHistory;
    private TextView textViewUserName, textViewStatus, textViewUserName2, textViewUserEmail, textViewUserPhone, textViewUserInstance;
    //private EditText editTextUserName, editTextUserPhone, editTextUserInstance, editTextUserEmail;
    private Button buttonEdit;
    public String name, phone, occupation, instance, status, email;


    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private DatabaseReference reff;
    //private EditText editTextAddres, editTextAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        reff = FirebaseDatabase.getInstance().getReference("DatasetUser");
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseUser user = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        findID();
        showProfile();
        setOnClick();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    // function to access user data from database & display on interface
    private void showProfile(){
        Query query = reff.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    //get data
                    name = "" + ds.child("name").getValue();
                    phone = "" + ds.child("phone").getValue();
                    email = "" + ds.child("email").getValue();
                    //occupation = "" + ds.child("occupation").getValue();
                    instance = "" + ds.child("instance").getValue();
                    status = "" + ds.child("status").getValue();

                    // show the profile information
                    textViewUserName.setText(name);
                    if (status.equals("admin")){
                        textViewStatus.setText("Administrator");
                    }
                    else{
                        textViewStatus.setText("Member");
                    }

                    textViewUserName2.setText(name);
                    textViewUserEmail.setText(email);
                    textViewUserPhone.setText(phone);
                    textViewUserInstance.setText(instance);
                    /*
                    editTextUserName.setText(name);
                    editTextUserPhone.setText(phone);
                    editTextUserEmail.setText(email);
                    //editTextUserOccu.setText(occupation);
                    editTextUserInstance.setText(instance);
                    */
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void findID(){
        imageViewProfile = (ImageView) findViewById(R.id.imageViewProfile);
        imageViewHome = (ImageView) findViewById(R.id.imageViewHome);
        imageViewLogout = (ImageView) findViewById(R.id.imageViewLogout);
        imageViewProfilePic = (ImageView) findViewById(R.id.imageViewProfilePic);
        imageViewHistory = (ImageView) findViewById(R.id.imageViewHistory);
        textViewUserName = (TextView) findViewById(R.id.textViewUserName);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewUserName2 = (TextView) findViewById(R.id.textViewUserName2);
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserPhone = (TextView) findViewById(R.id.textViewUserPhone);
        textViewUserInstance = (TextView) findViewById(R.id.textViewUserInstance);
        buttonEdit = (Button) findViewById(R.id.buttonEdit);
        //editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        //editTextUserEmail = (EditText) findViewById(R.id.editTextUserEmail);
        //editTextUserPhone = (EditText) findViewById(R.id.editTextUserPhone);
        //editTextUserOccu = (EditText) findViewById(R.id.editTextUserOccu);
        //editTextUserInstance = (EditText) findViewById(R.id.editTextUserInstance);
    }

    private void setOnClick(){
        imageViewLogout.setOnClickListener(this);
        imageViewHome.setOnClickListener(this);
        imageViewProfile.setOnClickListener(this);
        imageViewHistory.setOnClickListener(this);
        imageViewProfilePic.setOnClickListener(this);
        buttonEdit.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == imageViewLogout){
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (v == imageViewHome){
            startActivity(new Intent(this, HomeActivity.class));
        }
        if (v == imageViewProfile){
            startActivity(new Intent(this, ProfileActivity.class));
        }
        if (v == imageViewHistory){
            startActivity(new Intent(this, HistoryActivity.class));
        }
        if (v == buttonEdit){
            startActivity(new Intent(this, ProfileEditActivity.class));
        }
    }

}
