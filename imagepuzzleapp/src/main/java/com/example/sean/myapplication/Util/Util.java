package com.example.sean.myapplication.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;

import java.io.FileDescriptor;
import java.io.IOException;

import static java.security.AccessController.getContext;

/**
 * Created by Sean on 4/11/2016.
 */
public final class Util {
    private Util() {}
    public final static String BACKGROUND_COLOR = "#000000";
    public final static String DRAG_EVENT_STARTED_COLOR = "#1a66ff";
    public final static String VIEW_SELECTED_FOR_DROP_COLOR = "#4ce600";

    /**
    * Displays a message in a message box
     */
    public static void messageBox(String method, String message, Activity activity) {
        Log.d("EXCEPTION: " + method, message);

        AlertDialog.Builder messageBox = new AlertDialog.Builder(activity);
        messageBox.setTitle(method);
        messageBox.setMessage(message);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }

    /**
     * Retrieves bitmap from Uri and returns it
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context context) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
