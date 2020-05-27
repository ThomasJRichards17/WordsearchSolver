package com.tjr.wordsearchsolver.ui.load;

import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.tjr.wordsearchsolver.R;
import com.tjr.wordsearchsolver.data.DataStore;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.tjr.wordsearchsolver.data.RequestCodes.LOAD_WORDSEARCH_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCodes.LOAD_WORDS_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCodes.TAKE_WORDSEARCH_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCodes.TAKE_WORDS_PHOTO;

public class LoadFragment extends Fragment implements View.OnClickListener {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_load, container, false);

        // Set button listeners
        Button loadWordsearchCameraButton = root.findViewById(R.id.button_load_wordsearch_camera);
        loadWordsearchCameraButton.setOnClickListener(this);

        Button loadWordsearchPhotoButton = root.findViewById(R.id.button_load_wordsearch_photo);
        loadWordsearchPhotoButton.setOnClickListener(this);

        Button loadWordsCameraButton = root.findViewById(R.id.button_load_words_camera);
        loadWordsCameraButton.setOnClickListener(this);

        Button loadWordsPhotoButton = root.findViewById(R.id.button_load_words_photo);
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
                if (requestCode == LOAD_WORDSEARCH_PHOTO)
                    DataStore.getDataStore().setWordsearchImage(selectedImageURI);
                else
                    DataStore.getDataStore().setSearchWordsImage(selectedImageURI);
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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(requestCode);
            } catch (IOException ex) {

            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.android.fileprovider", photoFile);

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

        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}