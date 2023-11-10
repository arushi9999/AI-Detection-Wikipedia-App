package com.example.finalproj;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Interpreter.Options;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
    Context ctx;
    List<Image> imgList;

    private int imageWidth = 224;//224
    private int imageHeight = 224;//224
    private int channels = 3;
    private int modelInputSize = 4* imageWidth * imageHeight * channels;
    private ByteBuffer inputBuffer;

    public ImageAdapter(Context context, List<Image> imageList) {
        this.ctx = context;
        this.imgList = imageList;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.imageloader,parent,false);
        return new ImageHolder(layout);
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        Image image = imgList.get(position);
        Picasso.get().load(image.getImageUrl()).into(holder.igView);
        holder.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase fDatabase = FirebaseDatabase.getInstance();
                System.out.println("Hello:" + image.getImageUrl());
                Picasso.get()
                        .load(image.getImageUrl())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                // The image is loaded successfully as a Bitmap
                                //imageView.setImageBitmap(bitmap);
                                classifyImage(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                // Handle any errors while loading the image
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // Optional: You can set a placeholder image while the image is being loaded
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public void classifyImage(Bitmap bitmap){
        // Load the MobileNet model
        try {
            Interpreter.Options options = new Options();
            Interpreter interpreter = new Interpreter(loadModelFile("mobilenet_v2_1.0_224_1_default_1.tflite"), options);
            // Assuming MobileNet has NUM_CLASSES classes
            preprocessImage(interpreter,bitmap);
       } catch (IOException e) {
            e.printStackTrace();
        }
   }

    private MappedByteBuffer loadModelFile(String fileName) throws IOException {
        AssetFileDescriptor fileDescriptor = ctx.getAssets().openFd(fileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void preprocessImage(Interpreter interpreter,Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, true);

        inputBuffer = ByteBuffer.allocateDirect(modelInputSize);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[imageWidth * imageHeight];
        resizedBitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        int pixel = 0;
        for (int i = 0; i < imageWidth; ++i) {
            for (int j = 0; j < imageHeight; ++j) {
                final int val = pixels[pixel++];
                inputBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);  // Red component
                inputBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);   // Green component
                inputBuffer.putFloat((val & 0xFF) / 255.0f);          // Blue component
            }
        }
        float[][] outputScores = new float[1][1001];
        interpreter.run(inputBuffer, outputScores);
        // Get the predicted class and confidence
        float[] scores = outputScores[0];
        int maxIndex = 0;
        for (int i = 1; i < 1001; i++) {
            if (scores[i] > scores[maxIndex]) {
                maxIndex = i;
            }
        }
        // You can use the maxIndex and scores[maxIndex] for further processing or display
        Log.d("Classification", "Predicted class: " + maxIndex);
        Log.d("Classification", "Confidence: " + scores[maxIndex]);

        // Load the mapping file
        String resultlabel="unidentified";
        Map<Integer, String> classMap = new HashMap<>();
        try {
            InputStream inputStream = ctx.getAssets().open("labesl.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int counter =0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                //int classIndex = Integer.parseInt(parts[0]);
                String className = parts[0];
                classMap.put(counter, className);
                counter++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultlabel = classMap.get(maxIndex);
        Log.d("Classification", "Predicted className: " + classMap.get(maxIndex));
        showAlert(ctx,"Classification Result:",resultlabel);
    }

    private void showAlert(Context context, String title, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        TextView txtMessage = dialogView.findViewById(R.id.text_message);
        txtMessage.setText("Retrieved from Wikipedia:" + "\n\n" +  callWiki(message));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setView(dialogView)
                .setMessage(Html.fromHtml("<b>" + message+ "</b>"))
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do something when the positive button is clicked
                        dialog.dismiss();
                    }
                });

        builder.create();
        builder.show();
    }

    private  String  callWiki(String classifiedItem){

        String apiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=&explaintext=&titles=" + classifiedItem;
        String itemDetails ="";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response using Gson
                Gson gson = new Gson();
                WikipediaApiResponse apiResponse = gson.fromJson(response.toString(), WikipediaApiResponse.class);

                // Access the desired data from the parsed response
                String pageExtract = apiResponse.getQueryResult().getPages().values().iterator().next().getExtract();
                System.out.println(pageExtract);
                itemDetails = pageExtract;
            } else {
                System.out.println("API request failed. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception: ");
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("callwiki Exception: ");
        }
        return itemDetails;
    }
}
