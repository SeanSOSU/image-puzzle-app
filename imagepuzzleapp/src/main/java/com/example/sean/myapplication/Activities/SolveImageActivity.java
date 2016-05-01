//TODO: add custom listeners?
//TODO: add "hint" functionality
//TODO: add ability to send puzzles to friends
//TODO: add sounds
//TODO: add save function

package com.example.sean.myapplication.Activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sean.myapplication.Models.ImagePuzzle;
import com.example.sean.myapplication.R;
import com.example.sean.myapplication.Util.Util;

import static com.example.sean.myapplication.Util.Util.getBitmapFromUri;
import static com.example.sean.myapplication.Util.Util.messageBox;

public class SolveImageActivity extends AppCompatActivity {
    //default table size set to be 3x3
    private static int tableWidth = 3, tableHeight = 3;

    //border size for table cells
    private final static int BORDER_SIZE = 2;
    private final static int VIEW_SELECTED_SIZE = 10;

    //image URI received from main activity
    private Uri imageUri;
    private Bitmap originalImage;
    private ImagePuzzle imagePuzzle;
    private boolean dragEnabled;

    //views
    TableLayout table;
    TextView puzzleStatusView;

    //animations
    ObjectAnimator textFadeIn;
    ObjectAnimator imageFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        
        dragEnabled = true;
        table = (TableLayout) findViewById(R.id.shuffledImageTable);
        puzzleStatusView = (TextView) findViewById(R.id.puzzleStatus);
        
        //Retrieve image uri and get retrieve the image
        Intent intent = getIntent();
        imageUri = Uri.parse(intent.getStringExtra(PickImageActivity.EXTRA_URI));
        try {
            originalImage = getBitmapFromUri(imageUri, this);
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            messageBox("Error", e.getMessage(), this);
        }

        //create the image pieces and shuffle them in place
        imagePuzzle = new ImagePuzzle();
        imagePuzzle.init(tableWidth, tableHeight, originalImage);
        createTableBody(table);
        setPuzzlePieces();

        //set fade in animations
        textFadeIn =  (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.fade_in);
        textFadeIn.setTarget(puzzleStatusView);
        imageFadeIn =  (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.fade_in);
        imageFadeIn.setTarget(table);
    }

    /*
    ** Creates a table with tableWidth x tableHeight dimensions.
    ** Sets an index tag relative to its position in the table. The index goes from
    ** 0 - (tableWidth * tableHeight - 1) left to right then top to bottom.
     */
    public void createTableBody(TableLayout table) {
        int index = 0;
        for(int i = 0; i < tableHeight; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, 0, 1));
            for(int j = 0; j < tableWidth; j++) {
                ImageView view = new ImageView(this);
                view.setAdjustViewBounds(true);
                view.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
                view.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));
                view.setTag(index);
                view.setOnLongClickListener(new imageOnLongClickListener());
                view.setOnDragListener(new ImageDragEventListener());
                row.addView(view, new TableRow.LayoutParams(0,
                        TableRow.LayoutParams.MATCH_PARENT, 1));
                index++;
            }
            table.addView(row);
        }
    }

    /*
    ** OnLongClickListener for each view cell in the table.
     */
    private final class imageOnLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            ClipData.Item item = new ClipData.Item(view.getTag().toString());
            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData dragData = new ClipData(view.getTag().toString(), mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

            view.startDrag(dragData, shadowBuilder, view, 0);
            view.setPadding(VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE,
                    VIEW_SELECTED_SIZE);
            view.setBackgroundColor(android.graphics.Color.WHITE);
            return true;
        }
    }

    /*
    ** DragEventListener for each view cell in the table.
     */
    private class ImageDragEventListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            ImageView view = (ImageView) v;
            final int action = event.getAction();

            switch(action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    view.setPadding(VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE,
                            VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE);
                    view.setBackgroundColor(Color.parseColor(Util.VIEW_SELECTED_FOR_DROP_COLOR));
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    view.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
                    view.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));
                    return true;

                case DragEvent.ACTION_DROP:
                    view.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
                    view.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    int i = (int) view.getTag();
                    int j = Integer.parseInt(item.getText().toString());
                    swapImages(i, j);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    view.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));
                    if(imagePuzzle.isPuzzleSolved()) {
                        view.setOnLongClickListener(null);
                        dragEnabled = false;
                        doPuzzleSolvedAnim();
                    }
                    return true;

                default:
                    break;
            }

            return false;
        }
    }

    /*
    ** Swaps the position of bitmap images in the views of the table
     */
    private void swapImages(int i, int j) {
        if(i == j) {
            return;
        }

        ImageView viewI = (ImageView) table.findViewWithTag(i);
        ImageView viewJ = (ImageView) table.findViewWithTag(j);
        viewI.setImageBitmap(imagePuzzle.getImageAt(j));
        viewJ.setImageBitmap(imagePuzzle.getImageAt(i));
        imagePuzzle.swap(i, j);
    }

    /*
    ** onClick listener for reshuffle button.
    ** Reshuffles the image and starts the animation
     */
    public void reShuffleButton(View view) {
        if(imagePuzzle.isPuzzleSolved()) {
            doRestartAnim();
        } else {
            doReshuffleAnim();
        }
        imagePuzzle.reShuffle();
    }

    /*
    ** Sets the bitmap images for each table cell.
     */
    private void setPuzzlePieces() {
        for(int i = 0; i < tableWidth * tableHeight; i++) {
            ImageView imageView = (ImageView) table.findViewWithTag(i);
            imageView.setImageBitmap(imagePuzzle.getImageAt(i));

            //re-enable drag listener if disabled
            if (!dragEnabled) {
                imageView.setOnLongClickListener(new imageOnLongClickListener());
            }
        }
    }

    /*
    ** Animation when puzzle is solved
     */
    public void doPuzzleSolvedAnim() {
        //text fades out, writes "Solved!", fades back in
        Animator textFadeOut =  AnimatorInflater.loadAnimator(this, R.animator.fade_out);
        textFadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                puzzleStatusView.setText(R.string.solved);
                textFadeIn.start();
            }
        });
        textFadeOut.setTarget(puzzleStatusView);

        //image fades out, removes borders, fades back in
        Animator imageFadeOut = AnimatorInflater.loadAnimator(this, R.animator.fade_out);
        imageFadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for(int i = 0; i < tableHeight * tableWidth; i++) {
                    ImageView view = (ImageView) table.findViewWithTag(i);
                    view.setPadding(0, 0, 0, 0);
                }
                imageFadeIn.start();
            }
        });
        imageFadeOut.setTarget(table);

        textFadeOut.start();
        imageFadeOut.start();
    }

    /*
    ** Animation to reshuffle the image when the puzzle is not solved
     */
    public void doReshuffleAnim() {
        Animator imageFadeOut = AnimatorInflater.loadAnimator(this, R.animator.fade_out);
        imageFadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setPuzzlePieces();
                imageFadeIn.start();
            }
        });
        imageFadeOut.setTarget(table);
        imageFadeOut.start();
    }

    /*
    ** Animation to reshuffle the image after the puzzle is solved
     */
    public void doRestartAnim() {
        //text fades out, writes "Solve the Image", fades back in
        Animator textFadeOut =  AnimatorInflater.loadAnimator(this, R.animator.fade_out);
        textFadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                puzzleStatusView.setText(R.string.solve_the_image);
                textFadeIn.start();
            }
        });
        textFadeOut.setTarget(puzzleStatusView);

        //image fades out, adds borders back in, fades in
        Animator imageFadeOut = AnimatorInflater.loadAnimator(this, R.animator.fade_out);
        imageFadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setPuzzlePieces();
                for (int i = 0; i < tableHeight * tableWidth; i++) {
                    ImageView view = (ImageView) table.findViewWithTag(i);
                    view.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
                }
                imageFadeIn.start();
            }
        });
        imageFadeOut.setTarget(table);

        textFadeOut.start();
        imageFadeOut.start();
    }
}
