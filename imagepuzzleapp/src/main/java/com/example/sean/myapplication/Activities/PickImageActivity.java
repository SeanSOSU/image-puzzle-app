package com.example.sean.myapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.sean.myapplication.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.sean.myapplication.Util.Util.messageBox;

public class PickImageActivity extends AppCompatActivity {
    public final static String EXTRA_URI = "com.example.sean.myapplication.URI";
    public final static String EXTRA_DIMENSION ="dimensionSize";
    private static int  RESULT_LOAD_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public final static int MEDIA_TYPE_IMAGE = 1;
    public final static int MEDIA_TYPE_VIDEO = 2;
    private static int puzzleDimension = 0;

    private Uri photoUri;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        loadDefaultImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Load image wrapper to ask for permissions if necessary */
    public void loadImageFromGalleryWrapper(View view) {
        //if permission has not been granted yet, ask for permissions
        if (ActivityCompat.checkSelfPermission(PickImageActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PickImageActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        //otherwise can go ahead with load from gallery
        } else {
            loadImageFromGallery();
        }
    }

    /**
    * Called when user clicks the displayed image. Displays image on MainImageView.
    */
    public void loadImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    loadImageFromGallery();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(PickImageActivity.this, "READ_EXTERNAL_STORAGE Denied",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMAGE_ACTIVITY_REQUEST_CODE
                    && resultCode == RESULT_OK && data != null) {
                imageUri = data.getData();
                Bitmap imageBitmap = getBitmapFromUri(imageUri);

                ImageView imageView = (ImageView) findViewById(R.id.MainImageView);
                imageView.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(this, "You haven't picked an image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                messageBox(this, "Error", e.getMessage());
        }
    }

    /**
     * Loads placeholder image until another is selected
     */
    public void loadDefaultImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.placeholder, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        ImageView imageView = (ImageView) findViewById(R.id.MainImageView);
        imageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                R.drawable.placeholder, 300, 300));
    }

    public static Bitmap decodeSampledBitmapFromResource(
            Resources res, int resId, int reqWidth, int reqHeight) {
        //Decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        //Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            while((halfHeight/inSampleSize) > reqHeight
                    && (halfWidth/inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /*
    ** Sets puzzle dimension size from radio button click
     */
    public void onPuzzleDimensionClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radio_3:
                if(checked)
                    puzzleDimension = 3;
                break;
            case R.id.radio_4:
                if(checked)
                    puzzleDimension = 4;
                break;
            case R.id.radio_5:
                if(checked)
                    puzzleDimension = 5;
                break;
            default:
                puzzleDimension = 0;
                break;
        }
    }

    /*
    ** Opens SolveImageActivity and sends the bitmap Uri
     */
    public void shuffleImageButton(View view) {
        //If imageUri is null, then an image has not been selected yet.
        if(imageUri == null) {
            Toast.makeText(this, "You have not selected an image.", Toast.LENGTH_LONG).show();
            return;
        } else if(puzzleDimension == 0) {
            Toast.makeText(this, "You have not selected a dimension size.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent shuffleIntent = new Intent(this, SolveImageActivity.class);
        shuffleIntent.putExtra(EXTRA_URI, imageUri.toString());
        shuffleIntent.putExtra(EXTRA_DIMENSION, puzzleDimension);
        startActivity(shuffleIntent);
    }


    /*
    public void takePhotoButton() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri photoUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }*/

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}
