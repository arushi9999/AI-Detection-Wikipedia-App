package com.example.finalproj;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.finalproj.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    Button selectButton;
    Button uploadButton;
    Button showUploadsButton;
    ImageView image;
    ActivityMainBinding binding;
    Uri imageuri;
    StorageReference storageRef;
    ProgressDialog progressDialog;

    boolean allow=false;

    static final int REQ_CODE=123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        selectButton=findViewById(R.id.selectButton);
        uploadButton=findViewById(R.id.uploadButton);
        showUploadsButton=findViewById(R.id.showuploadsButton);
        image=findViewById(R.id.imageView);
        image.setImageResource(R.drawable.uploadd);

        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uploadButton.setClickable(false);// can't upload first

        binding.selectButton.setOnClickListener(new View.OnClickListener() { //selectButton
            @Override
            public void onClick(View view) {
                selectImage();
                allow=true;
                uploadButton.setClickable(true);
            }
        });
            binding.uploadButton.setOnClickListener(new View.OnClickListener() { //uploadButton
                @Override
                public void onClick(View view) {
                    if(allow) {
                        uploadImage();
                        image.setImageResource(R.drawable.uploadd);
                    }else{
                        Toast.makeText(MainActivity.this,"First select an image to upload",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            binding.showuploadsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent= new Intent(MainActivity.this,MainActivity2.class);
                    startActivity(intent);
                }
            });


    }//onCreate
    private void selectImage(){
        Intent i= new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,18);
    }

    private void uploadImage(){
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Uploading file...");
        progressDialog.show();


        SimpleDateFormat formatter= new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now=new Date();
        String fileName= formatter.format(now);
        storageRef= FirebaseStorage.getInstance().getReference("images/"+fileName);

        storageRef.putFile(imageuri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //binding.imageView.setImageURI(null);
                        binding.imageView.setImageResource(R.drawable.uploadd);
                        Toast.makeText(MainActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(progressDialog.isShowing()){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,"Failed to Upload",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==18 && data.getData()!=null){
            imageuri=data.getData();
            binding.imageView.setImageURI(imageuri);
        }

    }

}