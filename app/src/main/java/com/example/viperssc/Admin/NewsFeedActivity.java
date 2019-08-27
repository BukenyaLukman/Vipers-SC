package com.example.viperssc.Admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.viperssc.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NewsFeedActivity extends AppCompatActivity {

    private EditText ImageTitle,ImageDetails;
    private Button ChooseFile,UploadInfoBtn;
    private ImageView images;
    private static final int GalleryPick = 1;

    private String InputTitle,InputDetails,downloadImageUrl;
    private Uri imageUrl;
    private ProgressDialog loadingBar;

    private String SaveCurrentTime,SaveCurrentDate;
    private StorageReference PicturesStore;
    private DatabaseReference PicturesRef;

    private String PictureKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);

        PicturesRef = FirebaseDatabase.getInstance().getReference().child("NewsFeed");
        PicturesStore = FirebaseStorage.getInstance().getReference().child("News Feed");

        InitializeFields();

        ChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        UploadInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateUploads();
            }
        });
    }

    private void ValidateUploads() {
        InputTitle = ImageTitle.getText().toString();
        InputDetails = ImageDetails.getText().toString();

        if (imageUrl == null){
            Toast.makeText(this, "Please choose Image", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(InputTitle)){
            Toast.makeText(this, "Please write Picture Title", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(InputDetails)){
            Toast.makeText(this, "Please write Details and articles", Toast.LENGTH_SHORT).show();
        }else{
            StoreInformation();
        }
    }

    private void StoreInformation() {
        loadingBar.setTitle("Uploading...");
        loadingBar.setMessage("Please Wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(DateFormat.getDateInstance().format(DateFormat.MEDIUM));
        SaveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        SaveCurrentTime = currentTime.format(calendar.getTime());

        PictureKey = SaveCurrentDate + SaveCurrentTime;

        final StorageReference filePath = PicturesStore.child(imageUrl.getLastPathSegment() + PictureKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(imageUrl);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(NewsFeedActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(NewsFeedActivity.this, "Image Upload Successfully", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();

                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();


                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadImageUrl = task.getResult().toString();
                            Toast.makeText(NewsFeedActivity.this, "Image Url obtained successfully...", Toast.LENGTH_SHORT).show();

                            savePictureInfoToDatabase();

                        }
                    }
                });
            }
        });

    }
    private void savePictureInfoToDatabase(){
        HashMap<String,Object> pictureMap = new HashMap<>();
        pictureMap.put("pid",PictureKey);
        pictureMap.put("date",SaveCurrentDate);
        pictureMap.put("time",SaveCurrentTime);
        pictureMap.put("description",InputDetails);
        pictureMap.put("title",InputTitle);
        pictureMap.put("image",downloadImageUrl);


        PicturesRef.child(PictureKey).updateChildren(pictureMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            loadingBar.dismiss();
                            Toast.makeText(NewsFeedActivity.this, "Picture is added successfully", Toast.LENGTH_SHORT).show();

                        }else{
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(NewsFeedActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            imageUrl = data.getData();
            images.setImageURI(imageUrl);
        }
    }

    private void InitializeFields() {
        ImageTitle = findViewById(R.id.image_title);
        ImageDetails = findViewById(R.id.image_details);
        ChooseFile = findViewById(R.id.btn_admin_choose_file);
        UploadInfoBtn = findViewById(R.id.send_info_btn);
        images = findViewById(R.id.ImageView);
        loadingBar = new ProgressDialog(this);

    }
}

