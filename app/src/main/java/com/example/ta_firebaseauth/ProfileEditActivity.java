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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.widget.Toast.makeText;

public class ProfileEditActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView imageViewProfilePic, imageViewSave, imageViewProfile;
    private TextView textViewUserName, textViewStatus;
    private EditText editTextUserName, editTextUserPhone, editTextUserInstance, editTextUserEmail, editTextUserPassword;
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    Uri pickedProfilePic;
    public String name, phone, instance, status, email, password;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private DatabaseReference reff;
    //private EditText editTextAddres, editTextAge;

    SaveRegInformation reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        mAuth = FirebaseAuth.getInstance();

        reff = FirebaseDatabase.getInstance().getReference("DatasetUser");
        user = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        reg = new SaveRegInformation();

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
                    password = "" + ds.child("password").getValue();

                    if (status.equals("admin")){
                        textViewStatus.setText("Administrator");
                    }
                    else{
                        textViewStatus.setText("Member");
                    }
                    textViewUserName.setText(name);

                    editTextUserName.setText(name);
                    editTextUserPhone.setText(phone);
                    editTextUserEmail.setText(email);
                    //editTextUserOccu.setText(occupation);
                    editTextUserInstance.setText(instance);
                    editTextUserPassword.setText(password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile(){
        reff = FirebaseDatabase.getInstance().getReference("DatasetUser");
        user = mAuth.getCurrentUser();
        makeText(this, "Your profile has successfully updated!", Toast.LENGTH_SHORT).show();

        //get new data from editText
        name = editTextUserName.getText().toString().trim();
        email = editTextUserEmail.getText().toString().trim();
        instance = editTextUserInstance.getText().toString().trim();
        phone = editTextUserPhone.getText().toString().trim();
        password = editTextUserPassword.getText().toString().trim();

        user.updateEmail(email);
        user.updatePassword(password);
        reg = new SaveRegInformation(name, email, password, phone, instance);
        reff.setValue(reg);

        //publish new data user to database / saveRegInformation class
        reg.setName(name);
        reg.setEmail(email);
        reg.setInstance(instance);
        reg.setPassword(password);
        //reg.setOccupation(occupation);
        reg.setPhone(phone);
        reg.setStatus("Member");
        reff.push().setValue(reg);
    }


    //not done
    private void uploadProfilePicture(){
        if (Build.VERSION.SDK_INT >= 21){
            checkAndRequestPermission();
        }else {
            openGallery();
        }
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileEditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                makeText(this, "Please accept request permission!", Toast.LENGTH_SHORT).show();
            }else {
                ActivityCompat.requestPermissions(ProfileEditActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null){
            pickedProfilePic = data.getData();
            imageViewProfilePic.setImageURI(pickedProfilePic);
        }
    }

    private void findID(){
        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);
        imageViewProfilePic = (ImageView) findViewById(R.id.imageViewProfilePic);
        imageViewProfile = (ImageView) findViewById(R.id.imageViewProfile);
        textViewUserName = (TextView) findViewById(R.id.textViewUserName);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextUserEmail = (EditText) findViewById(R.id.editTextUserEmail);
        editTextUserPhone = (EditText) findViewById(R.id.editTextUserPhone);
        //editTextUserOccu = (EditText) findViewById(R.id.editTextUserOccu);
        editTextUserInstance = (EditText) findViewById(R.id.editTextUserInstance);
        editTextUserPassword = (EditText) findViewById(R.id.editTextUserPassword);
    }

    private void setOnClick(){
        imageViewSave.setOnClickListener(this);
        imageViewProfilePic.setOnClickListener(this);
        imageViewProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == imageViewSave){
            updateProfile();
        }
        if (v == imageViewProfilePic){
            uploadProfilePicture();
        }
        if (v == imageViewProfile){
            //will open profile activity
            startActivity(new Intent(this, ProfileActivity.class));
        }
    }

}
