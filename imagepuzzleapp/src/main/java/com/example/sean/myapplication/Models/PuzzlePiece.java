package com.example.sean.myapplication.Models;

import android.graphics.Bitmap;

/**
 * Created by Sean on 4/19/2016.
 */
public class PuzzlePiece {
    //bitmap image
    private final Bitmap bitmap;

    //the piece's original index in the array
    private final int originalIndex;

    public PuzzlePiece(Bitmap bitmap, int originalIndex) {
        this.bitmap = bitmap;
        this.originalIndex = originalIndex;
    }

    public Bitmap getBitmap() { return bitmap; }

    public int getOriginalIndex() { return originalIndex; }
}
