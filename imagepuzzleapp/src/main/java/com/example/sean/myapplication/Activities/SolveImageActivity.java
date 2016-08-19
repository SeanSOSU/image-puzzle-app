//TODO: add custom listeners?
//TODO: add ability to send puzzles to friends
//TODO: add sounds
//TODO: add dimensions option
//TODO: add camera functionality
//TODO: change hint to replace first index with correct piece

package com.example.sean.myapplication.Activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewTreeObserver;
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
        
        //Retrieve image uri and retrieve the image
        Intent intent = getIntent();
        imageUri = Uri.parse(intent.getStringExtra(PickImageActivity.EXTRA_URI));
        try {
            originalImage = getBitmapFromUri(this, imageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            messageBox(this, "Error", e.getMessage());
        }
        //Retrieve dimension size from intent
        tableWidth = intent.getIntExtra(PickImageActivity.EXTRA_DIMENSION, 3);
        tableHeight = intent.getIntExtra(PickImageActivity.EXTRA_DIMENSION, 3);

        //create the image pieces and shuffle them in place
        imagePuzzle = new ImagePuzzle();
        imagePuzzle.init(tableWidth, tableHeight, originalImage);



        createTableBody(table);
        setPuzzlePieces();

        //set table height parameters after layout
        table.post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < tableHeight; i++){
                    TableRow row = (TableRow) table.getChildAt(i);
                    row.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT, 0, 1.0f));
                }
            }
        });

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
            TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);

            for(int j = 0; j < tableWidth; j++) {
                ImageView tableCell = (ImageView) getLayoutInflater().inflate(
                        R.layout.table_cell, null);
                tableCell.setTag(index);
                tableCell.setOnLongClickListener(new imageOnLongClickListener());
                tableCell.setOnDragListener(new ImageDragEventListener());
                tableRow.addView(tableCell, new TableRow.LayoutParams(0,
                        TableRow.LayoutParams.MATCH_PARENT, 1.0f));
                index++;
            }
            table.addView(tableRow);
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
                    view.setBackgroundResource(R.color.image_border_selected);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    view.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
                    view.setBackgroundResource(R.color.image_border_normal);
                    return true;

                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    int i = (int) view.getTag();
                    int j = Integer.parseInt(item.getText().toString());
                    swapImages(i, j);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    view.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
                    view.setBackgroundResource(R.color.image_border_normal);
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
    ** Swaps the position of bitmap images in the views of the table and in the logic
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
        imagePuzzle.shuffle();
    }

    /*
    ** onClick listener for hint button.
    ** Highlights a piece to be dragged and dropped to its correct position
     */
    public void showHintButton(View view) {
        if(imagePuzzle.isPuzzleSolved()) {
            return;
        }

        //find first piece not in correct position
        int wrongIndex = 0;
        while(imagePuzzle.inCorrectPosition(wrongIndex)) {
            wrongIndex++;
        }
        int correctIndex = imagePuzzle.getOriginalIndexOf(wrongIndex);

        ImageView correctView = (ImageView) table.findViewWithTag(correctIndex);
        ImageView wrongView = (ImageView) table.findViewWithTag(wrongIndex);
        correctView.setPadding(VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE,
                VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE);
        wrongView.setPadding(VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE,
                VIEW_SELECTED_SIZE, VIEW_SELECTED_SIZE);
        correctView.setBackgroundResource(R.color.correct_position);
        wrongView.setBackgroundResource(R.color.wrong_position);
    }

    /*
    ** Sets the bitmap images for each table cell.
     */
    private void setPuzzlePieces() {
        for(int i = 0; i < tableWidth * tableHeight; i++) {
            ImageView imageView = (ImageView) table.findViewWithTag(i);
            imageView.setImageBitmap(imagePuzzle.getImageAt(i));
            imageView.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
            imageView.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));

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
