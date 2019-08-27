package com.example.viperssc.Admin;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.viperssc.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class VideosUploadActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Toolbar mToolbar;
    private VideoView VideoDisplay;
    private EditText VideoTitle,VideoDescription;
    private Spinner ContentProvider;
    private String Title,Description,Category,downloadUrl;
    private Button UploadVideosBtn,ChooseVideo;
    private Uri videoUrl;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ProgressBar mProgressBar;
    private String SaveCurrentDate,SaveCurrentTime,VideoKey;
    private static final int GalleryPick = 1;

    //private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_upload);

        mStorageRef = FirebaseStorage.getInstance().getReference().child("Uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("VideoUploads");


        mToolbar = findViewById(R.id.videos_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Videos Upload");


        InitializeFields();
        setTheSpinner();

        ChooseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        UploadVideosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadInfoToDatabase();
                UploadVideosBtn.setEnabled(false);
            }
        });

    }

    private void UploadInfoToDatabase() {
        Title = VideoTitle.getText().toString();
        Description = VideoDescription.getText().toString();
        Category = ContentProvider.getSelectedItem().toString();

        if(videoUrl == null){
            Toast.makeText(this, "Please select a video.", Toast.LENGTH_SHORT).show();
        }else  if (TextUtils.isEmpty(Title)){
            Toast.makeText(this, "Please write title of the Video", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Please write video description", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(Category)){
            Toast.makeText(this, "Please select category of the video", Toast.LENGTH_SHORT).show();
        }else{
            UploadData();
        }


    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void UploadData() {


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(DateFormat.getDateInstance().format(DateFormat.MEDIUM));
        SaveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        SaveCurrentTime = currentTime.format(calendar.getTime());

        VideoKey = SaveCurrentDate + SaveCurrentTime;
        final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "."+ getFileExtension(videoUrl));
        final UploadTask uploadTask = fileReference.putFile(videoUrl);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(VideosUploadActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                mProgressBar.setProgress((int) progress);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(0);
                    }
                },500);
                Toast.makeText(VideosUploadActivity.this, "Video Upload Successfully", Toast.LENGTH_LONG).show();



                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();

                        }

                        downloadUrl = fileReference.getDownloadUrl().toString();
                        return fileReference.getDownloadUrl();


                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().toString();
                            Toast.makeText(VideosUploadActivity.this, "Video Url obtained successfully...", Toast.LENGTH_SHORT).show();

                            saveVideoToDatabase();

                        }
                    }
                });
            }
        });
    }

    private void saveVideoToDatabase() {
        HashMap<String,Object> videoMap = new HashMap<>();
        videoMap.put("pid",VideoKey);
        videoMap.put("description",Description);
        videoMap.put("title",Title);
        videoMap.put("video",downloadUrl);
        videoMap.put("Category",Category);

      switch (Category){
          case "LIVE GAME":
              mDatabaseRef.child("LIVE GAME").child(VideoKey).updateChildren(videoMap)
                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){

                                  Toast.makeText(VideosUploadActivity.this, "Video is added successfully", Toast.LENGTH_SHORT).show();
                                  VideoTitle.setText("");
                                  VideoDescription.setText("");

                              }else{

                                  String message = task.getException().toString();
                                  Toast.makeText(VideosUploadActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                              }

                          }
                      });
              break;
          case "TRAINING SESSION":
              mDatabaseRef.child("TRAINING SESSION").child(VideoKey).updateChildren(videoMap)
                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){

                                  Toast.makeText(VideosUploadActivity.this, "Video is added successfully", Toast.LENGTH_SHORT).show();
                                  VideoTitle.setText("");
                                  VideoDescription.setText("");

                              }else{

                                  String message = task.getException().toString();
                                  Toast.makeText(VideosUploadActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                              }

                          }
                      });
              break;
          case "EVENTS":
              mDatabaseRef.child("EVENTS").child(VideoKey).updateChildren(videoMap)
                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){

                                  Toast.makeText(VideosUploadActivity.this, "Video is added successfully", Toast.LENGTH_SHORT).show();
                                  VideoTitle.setText("");
                                  VideoDescription.setText("");

                              }else{

                                  String message = task.getException().toString();
                                  Toast.makeText(VideosUploadActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                              }

                          }
                      });
              break;
          case "FANS":
              mDatabaseRef.child("FANS").child(VideoKey).updateChildren(videoMap)
                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){

                                  Toast.makeText(VideosUploadActivity.this, "Video is added successfully", Toast.LENGTH_SHORT).show();
                                  VideoTitle.setText("");
                                  VideoDescription.setText("");

                              }else{

                                  String message = task.getException().toString();
                                  Toast.makeText(VideosUploadActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                              }

                          }
                      });
              break;
      }

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("video/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null){
            videoUrl = data.getData();
            VideoDisplay.setVideoURI(videoUrl);
            VideoDisplay.setVideoPath(data.getData().toString());
            VideoDisplay.requestFocus();
            VideoDisplay.start();

        }
    }



    private void setTheSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.videos,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ContentProvider.setAdapter(adapter);
        ContentProvider.setOnItemSelectedListener(this);
    }

    private void InitializeFields() {
        VideoDisplay = findViewById(R.id.video_view_for_upload);
        VideoTitle = findViewById(R.id.video_title);
        VideoDescription = findViewById(R.id.video_description);
        ContentProvider = findViewById(R.id.spinner1);
        UploadVideosBtn = findViewById(R.id.admin_upload_videos);
        ChooseVideo = findViewById(R.id.admin_choose_videos);
        mProgressBar = findViewById(R.id.progress_bar);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
