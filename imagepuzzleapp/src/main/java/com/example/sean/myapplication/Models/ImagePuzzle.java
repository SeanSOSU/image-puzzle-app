package com.example.sean.myapplication.Models;

import android.graphics.Bitmap;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sean on 4/20/2016.
 */
public class ImagePuzzle {
    //dimensions of the puzzle board
    private int width;
    private int height;

    private int correctPositions;

    //a list of
    private List<PuzzlePiece> pieceList;

    public ImagePuzzle() {
        width = 0;
        height = 0;
        correctPositions = 0;
        pieceList = new ArrayList<>();
    }

    /* Initializes the class attributes and creates a shuffled bitmap piece list from the sent
    ** bitmap
     */
    public void init(int width, int height, Bitmap bitmap) {
        this.width = width;
        this.height = height;
        createPuzzlePieces(bitmap);
        shuffle();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<PuzzlePiece> getPieceList() { return pieceList; }
    public int getCorrectPositions() { return correctPositions; }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }

    /*
    ** Creates the puzzle pieces from the original bitmap to store in an arraylist.
    ** The pieces are split evenly to match the dimensions of the board and are initially put into
    ** the list in order going left to right and top to bottom in the image.
     */
    public void createPuzzlePieces(Bitmap originalImage) {
        //used for delta
        int pieceWidth = originalImage.getWidth()/width;
        int pieceHeight = originalImage.getHeight()/height;

        int index = 0;
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                Bitmap imagePiece = Bitmap.createBitmap(originalImage, j * pieceWidth,
                        i * pieceHeight, pieceWidth, pieceHeight);
                pieceList.add(new PuzzlePiece(imagePiece, index));
                index++;
            }
        }
    }

    /*
    ** Checks to see if the list is in its original order at creation
     */
    public boolean isPuzzleSolved() {
        //if number of correct positions is equal to the amount of elements, return true
        return (correctPositions == width * height);
    }

    /*
    ** Swaps two elements in the arraylist in place and update correct positions
     */
    public void swap(int i, int j) {
        if(i == j) {
            return;
        }
        //if already in correction position, decrement
        if(pieceList.get(i).getOriginalIndex() == i) {
            correctPositions--;
        } else if(pieceList.get(i).getOriginalIndex() == j) {
            correctPositions++;
        }
        if(pieceList.get(j).getOriginalIndex() == j) {
            correctPositions--;
        } else if(pieceList.get(j).getOriginalIndex() == i) {
            correctPositions++;
        }

        Collections.swap(pieceList, i, j);
    }

    //helper functions
    public Bitmap getImageAt(int i) {
        return pieceList.get(i).getBitmap();
    }

    public int getOriginalIndexOf(int i) {
        return pieceList.get(i).getOriginalIndex();
    }

    /*
    ** Shuffles the ordering of puzzle pieces
     */
    public void shuffle() {
        Collections.shuffle(pieceList);
        initCorrectPositions();
    }

    /*
    ** Iterates through the array and initializes the number of pieces in the correct position
     */
    private void initCorrectPositions() {
        correctPositions = 0;

        for(int i = 0; i < pieceList.size(); i++) {
            if(i == pieceList.get(i).getOriginalIndex()) {
                correctPositions++;
            }
        }
    }

    public boolean inCorrectPosition(int index) {
        try {
            return (pieceList.get(index).getOriginalIndex() == index);
        } catch(ArrayIndexOutOfBoundsException e) {
            throw e;
        }
    }
}
