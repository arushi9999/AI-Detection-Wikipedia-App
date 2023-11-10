package com.example.finalproj;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class ImageHolder extends RecyclerView.ViewHolder {
    ImageView igView;
    Button button2;

    public ImageHolder(View itemView) {
        super(itemView);
        igView = itemView.findViewById(R.id.imgLoader);
        button2 = itemView.findViewById(R.id.button2);

    }
}
