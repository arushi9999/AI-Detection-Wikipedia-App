package com.example.finalproj;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class MainActivity2 extends AppCompatActivity {
    RecyclerView recyclerView;
    FirebaseStorage fStore;
    FirebaseDatabase fDatabase;
    DatabaseReference dbRef;
    ImageAdapter adapter;
    List<Image> imgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main2);

        recyclerView =findViewById(R.id.recyclerViews);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imgList = new ArrayList<Image>();

        adapter = new ImageAdapter(MainActivity2.this,imgList);
        recyclerView.setAdapter(adapter);
        // Retrieve and display images
        retrieveImages();

    }

    private void retrieveImages() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Specify the path of the directory containing the images in Firebase Storage
        String directoryPath = "images";

        StorageReference directoryRef = storageRef.child(directoryPath);

        directoryRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        // Iterate through the list of items (images) in the directory

                        for (StorageReference item : listResult.getItems()) {
                            // Get the download URL of each image and add it to the list
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Image image = new Image();
                                    image.setKey(uri.getQueryParameter("key"));
                                    image.setStorageKey(uri.getQueryParameter("storage"));
                                    image.setImageUrl(uri.toString());
                                    imgList.add(image);
                                    adapter.notifyDataSetChanged();
                                    System.out.println(uri.toString() + ":" + uri.getQueryParameter("key") + ":" + uri.getQueryParameter("storage"));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception exception) {
                                    // Handle any errors
                                    Toast.makeText(MainActivity2.this, "Failed to retrieve image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        // Handle any errors
                        Toast.makeText(MainActivity2.this, "Failed to retrieve images: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}