package com.damienetstevan.firegallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.damienetstevan.firegallery.models.ImageModel;

import java.util.ArrayList;

/**
 * Recycler View adapter for Images
 */
public final class RecyclerImageAdapter extends RecyclerView.Adapter<RecyclerImageAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<ImageModel> images;

    public RecyclerImageAdapter(final Context context, final ArrayList<ImageModel> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public RecyclerImageAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        // initialize view holder (single image container)
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.single_image_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerImageAdapter.ViewHolder holder, final int position) {
        // load image at position from url into its corresponding view holder
        Glide.with(context)
                .load(images.get(position).getImageUrl())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
