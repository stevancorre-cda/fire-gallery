package com.damienetstevan.firegallery.models;

public class ImageModel {
    private final String imageUrl;

    public ImageModel(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
