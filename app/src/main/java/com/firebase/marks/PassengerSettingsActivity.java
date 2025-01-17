package com.firebase.marks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PassengerSettingsActivity extends AppCompatActivity {

    private EditText nameField, phoneField;
    private Button confirmButton, backButton;
    private FirebaseAuth auth;
    private DatabaseReference passengerDatabase;
    private String userID, userName, userPhone, userProfileImageUrl;
    private ImageView profileImage;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_settings);

        nameField = findViewById(R.id.name);
        phoneField = findViewById(R.id.phoneNumber);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        passengerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(userID);
        getUserInfo();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

    }

    private void getUserInfo() {
        passengerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        userName = map.get("name").toString();
                        nameField.setText(userName);
                    }
                    if (map.get("phone") != null) {
                        userPhone = map.get("phone").toString();
                        phoneField.setText(userPhone);
                    }
                    if (map.get("profileImageUrl") != null) {
                        userProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide
                                .with(getApplication())
                                .load(userProfileImageUrl)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInfo() {
        userName = nameField.getText().toString();
        userPhone = phoneField.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("name", userName);
        userInfo.put("phone", userPhone);
        passengerDatabase.updateChildren(userInfo);
        if (resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_Images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    Task<Uri> downloadTaskUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    while (!downloadTaskUrl.isSuccessful());
                    Uri downloadUrl = downloadTaskUrl.getResult();
                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    passengerDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        } else {
            finish();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }
}