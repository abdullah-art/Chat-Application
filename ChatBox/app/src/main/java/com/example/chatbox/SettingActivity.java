package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    private Button updateBtn;
    private EditText username,status;
    private ImageView profile_pic;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private DatabaseReference databaseReference;
    private static final int galleryPic=1;
    private StorageReference profileImagesStorageRef;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        firebaseAuth= FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();
        databaseReference=FirebaseDatabase.getInstance().getReference();
        profileImagesStorageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        InitializingFields();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               CropImage.activity()
                       .setAspectRatio(1,1)
                       .setCropShape(CropImageView.CropShape.OVAL)
                       .start(SettingActivity.this);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        retrievingUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                profile_pic.setImageURI(null);
                profile_pic.setImageURI(resultUri);
                imageUri=resultUri;
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void retrievingUserInfo() {
        databaseReference.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("about"))) || ((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")))){
                            String name=dataSnapshot.child("username").getValue().toString();
                            String about=dataSnapshot.child("about").getValue().toString();
                            String profileUrl=dataSnapshot.child("image").getValue().toString();
                            username.setText(name);
                            status.setText(about);
                            Uri uri=Uri.parse(profileUrl);
                            Picasso.get().load(uri).into(profile_pic);

                        }
                        else{
                            Toast.makeText(SettingActivity.this, "Please set your name!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void InitializingFields() {
        updateBtn=(Button)findViewById(R.id.update_button);
        username=(EditText)findViewById(R.id.username);
        status=(EditText)findViewById(R.id.about);
        profile_pic=(ImageView)findViewById(R.id.profile_image);
    }

    private void sendUserToMainActivity() {
        Intent authIntent=new Intent(SettingActivity.this,MainActivity.class);
        authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
        finish();
    }

    private void updateFields(String name,String about){
        final HashMap<String,Object> myMap=new HashMap<>();
        myMap.put("uid",currentUserId);
        myMap.put("username",name);
        myMap.put("about",about);
        databaseReference.child("Users").child(currentUserId).updateChildren(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    sendUserToMainActivity();
                    Toast.makeText(SettingActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();

                }
                else{
                    String error=task.getException().toString();
                    Toast.makeText(SettingActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSettings() {
      final String name=username.getText().toString();
      final String about=status.getText().toString();
      if(TextUtils.isEmpty(name))
      {
          Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
      }
      else if(TextUtils.isEmpty(about))
      {
          Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
      }
      else if(profile_pic.getDrawable()==null)
      {
          Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
      }
      else{
         if(imageUri !=null)
         {
             StorageReference filePath=profileImagesStorageRef.child(currentUserId + ".jpg");

             filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                     if(task.isSuccessful())
                     {
                         final String downloadUrl=task.getResult().getDownloadUrl().toString();

                         databaseReference.child("Users").child(currentUserId).child("image")
                                 .setValue(downloadUrl)
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful())
                                         {
                                             Toast.makeText(SettingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                             updateFields(name,about);
                                         }
                                     }
                                 });

                     }
                 }
             });
         }
         else{
            updateFields(name,about);
         }
      }
    }

}






