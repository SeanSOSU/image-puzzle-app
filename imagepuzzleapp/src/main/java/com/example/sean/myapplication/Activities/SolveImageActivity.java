//TODO: add animations to solved and reshuffle
//TODO: add "hint" functionality
//TODO: add ability to send puzzles to friends
//TODO: add sounds

package com.example.sean.myapplication.Activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
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
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sean.myapplication.Models.ImagePuzzle;
import com.example.sean.myapplication.Models.PuzzlePiece;
import com.example.sean.myapplication.R;
import com.example.sean.myapplication.Util.Util;

import java.util.ArrayList;
import java.util.List;

import static com.example.sean.myapplication.Util.Util.getBitmapFromUri;
import static com.example.sean.myapplication.Util.Util.messageBox;

public class SolveImageActivity extends AppCompatActivity {
    //default table size set to be 3x3
    private int tableWidth = 3, tableHeight = 3;
    private final static int BORDERSIZE = 2;

    //image URI received from main activity
    private  Uri imageUri;
    private Bitmap originalImage;
    private ImagePuzzle imagePuzzle;
    private boolean dragEnabled;
    private boolean puzzleSolved;

    TableLayout table;
    TextView puzzleStatusView;

    //aniamtions
    ObjectAnimator puzzleSolvedAnimation;
    ObjectAnimator textFadeIn;
    ObjectAnimator textFadeOut;
    ObjectAnimator imageFadeIn;
    ObjectAnimator imageFadeOut;

    public enum AnimationType {
        PUZZLESOLVED, RESTART, RESHUFFLE
    }

    AnimationType fadeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        dragEnabled = true;
        fadeType = AnimationType.RESHUFFLE;
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

        //set animations
        textFadeIn =  (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.fade_in);
        textFadeIn.setTarget(puzzleStatusView);
        textFadeOut =  (ObjectAnimator) AnimatorInflater.loadAnimator(
                this, R.animator.fade_out);
        textFadeOut.addListener(new textFadeAnimationListener());
        textFadeOut.setTarget(puzzleStatusView);

        imageFadeIn =  (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.fade_in);
        imageFadeIn.setTarget(table);
        imageFadeOut =  (ObjectAnimator) AnimatorInflater.loadAnimator(
                this, R.animator.fade_out);
        imageFadeOut.addListener(new imageFadeAnimationListener());
        imageFadeOut.setTarget(table);

        //start fade in animation upon load
        textFadeIn.start();
        imageFadeIn.start();
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
                view.setPadding(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE);
                view.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));
                view.setTag(index);
                view.setOnLongClickListener(new ImageOnLongClickListener());
                view.setOnDragListener(new ImageDragEventListener());
                view.setImageBitmap(imagePuzzle.getPieceList().get(index).getBitmap());
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
    private final class ImageOnLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            ClipData.Item item = new ClipData.Item((CharSequence)view.getTag().toString());
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
                    view.setBackgroundColor(Color.parseColor(Util.VIEW_SELECTED_FOR_DROP_COLOR));
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    view.setBackgroundColor(Color.parseColor(Util.BACKGROUND_COLOR));
                    return true;

                case DragEvent.ACTION_DROP:
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
                        fadeType = AnimationType.PUZZLESOLVED;
                        dragEnabled = false;
                        textFadeOut.start();
                        imageFadeOut.start();
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
    ** Starts the animation for reshuffle
     */
    public void reShuffleButton(View view) {
        if(fadeType == AnimationType.PUZZLESOLVED) {
            fadeType = AnimationType.RESTART;
            textFadeOut.start();
        }
        imageFadeOut.start();
    }

    /*
    ** Reshuffles the image and sets the bitmap images for each table cell.
     */
    private void reShuffle() {
        imagePuzzle.reShuffle();
        for(int i = 0; i < tableWidth * tableHeight; i++) {
            ImageView imageView = (ImageView) table.findViewWithTag(i);
            imageView.setImageBitmap(imagePuzzle.getImageAt(i));

            //re-enable drag listener if disabled
            if (!dragEnabled) {
                imageView.setOnLongClickListener(new ImageOnLongClickListener());
            }
        }
    }

    private class imageFadeAnimationListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationCancel(Animator animation) {}

        @Override
        public void onAnimationRepeat(Animator animation) {}

        @Override
        public void onAnimationStart(Animator animation) {}

        @Override
        public void onAnimationEnd(Animator animation) {
            if(fadeType == fadeType.PUZZLESOLVED) {
                //remove borders
                for(int i = 0; i < tableHeight * tableWidth; i++) {
                    ImageView view = (ImageView) table.findViewWithTag(i);
                    view.setPadding(0, 0, 0, 0);
                }
            } else {
                if (fadeType == fadeType.RESTART) {
                    //add borders
                    for (int i = 0; i < tableHeight * tableWidth; i++) {
                        ImageView view = (ImageView) table.findViewWithTag(i);
                        view.setPadding(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE);
                    }
                    fadeType = fadeType.RESHUFFLE;
                }
                //reshuffle image
                reShuffle();
            }
            imageFadeIn.start();
        }
    }

    private class textFadeAnimationListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationCancel(Animator animation) {}

        @Override
        public void onAnimationRepeat(Animator animation) {}

        @Override
        public void onAnimationStart(Animator animation) {}

        @Override
        public void onAnimationEnd(Animator animation) {
            if(fadeType == AnimationType.PUZZLESOLVED) {
                puzzleStatusView.setText(R.string.solved);

            } else if(fadeType == AnimationType.RESTART){
                puzzleStatusView.setText(R.string.solve_the_image);
            }
            textFadeIn.start();
        }
    }

}
