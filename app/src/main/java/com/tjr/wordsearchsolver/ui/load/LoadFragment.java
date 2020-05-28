package com.tjr.wordsearchsolver.ui.load;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.DataStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.tjr.wordsearchsolver.data.RequestCodes.LOAD_WORDSEARCH_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCodes.LOAD_WORDS_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCodes.TAKE_WORDSEARCH_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCodes.TAKE_WORDS_PHOTO;

public class LoadFragment extends Fragment implements View.OnClickListener {

    private final Logger logger = LoggerFactory.getLogger(LoadFragment.class);

    private Button loadWordsearchCameraButton;
    private Button loadWordsearchPhotoButton;
    private Button loadWordsCameraButton;
    private Button loadWordsPhotoButton;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_load, container, false);

        // Set button listeners
        loadWordsearchCameraButton = root.findViewById(R.id.button_load_wordsearch_camera);
        loadWordsearchCameraButton.setOnClickListener(this);

        loadWordsearchPhotoButton = root.findViewById(R.id.button_load_wordsearch_photo);
        loadWordsearchPhotoButton.setOnClickListener(this);

        loadWordsCameraButton = root.findViewById(R.id.button_load_words_camera);
        loadWordsCameraButton.setOnClickListener(this);

        loadWordsPhotoButton = root.findViewById(R.id.button_load_words_photo);
        loadWordsPhotoButton.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_load_wordsearch_camera:
                takePicture(TAKE_WORDSEARCH_PHOTO);
                break;
            case R.id.button_load_wordsearch_photo:
                selectPicture(LOAD_WORDSEARCH_PHOTO);
                break;
            case R.id.button_load_words_camera:
                takePicture(TAKE_WORDS_PHOTO);
                break;
            case R.id.button_load_words_photo:
                selectPicture(LOAD_WORDS_PHOTO);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == LOAD_WORDSEARCH_PHOTO || requestCode == LOAD_WORDS_PHOTO) {
                Uri selectedImageURI = data.getData();
                if (requestCode == LOAD_WORDSEARCH_PHOTO && checkBitmapIsntEmpty(selectedImageURI)) {
                    DataStore.getDataStore().setWordsearchImage(selectedImageURI);
                    loadWordsearchPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                    loadWordsearchCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_camera, null), null, null, null);
                } else {
                    DataStore.getDataStore().setSearchWordsImage(selectedImageURI);
                    loadWordsPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                    loadWordsCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_camera, null), null, null, null);
                }
            } else if (requestCode == TAKE_WORDSEARCH_PHOTO || requestCode == TAKE_WORDS_PHOTO) {
                if (requestCode == TAKE_WORDSEARCH_PHOTO && checkBitmapIsntEmpty(DataStore.getDataStore().getWordsearchImage())) {
                    loadWordsearchCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                    loadWordsearchPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo, null), null, null, null);
                } else if (checkBitmapIsntEmpty(DataStore.getDataStore().getSearchWordsImage())) {
                    loadWordsCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                    loadWordsPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo, null), null, null, null);
                }
            }
        }
    }

    private void selectPicture(int requestCode) {
        Intent intent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), requestCode);
    }

    private void takePicture(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Make sure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(requestCode);
            } catch (IOException e) {
                logger.error("Failed to create error", e);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireActivity().getApplicationContext(), "com.example.android.fileprovider", photoFile);

                if (requestCode == TAKE_WORDSEARCH_PHOTO)
                    DataStore.getDataStore().setWordsearchImage(photoURI);
                else if (requestCode == TAKE_WORDS_PHOTO)
                    DataStore.getDataStore().setSearchWordsImage(photoURI);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    private File createImageFile(int requestCode) throws IOException {
        String imageFileName = "Image_";

        if (requestCode == TAKE_WORDSEARCH_PHOTO)
            imageFileName += "Wordsearch_";
        else if (requestCode == TAKE_WORDS_PHOTO)
            imageFileName += "Words_";

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private boolean checkBitmapIsntEmpty(Uri uri) {
        Bitmap bitmap = null;
        Bitmap emptyBitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
            emptyBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        } catch (IOException e) {
            logger.error("Error checking if bitmaps are empty", e);
        }

        return bitmap != null && !bitmap.sameAs(emptyBitmap);
    }
}