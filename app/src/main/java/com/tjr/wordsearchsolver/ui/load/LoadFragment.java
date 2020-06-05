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
import com.tjr.wordsearchsolver.processing.ImageProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static android.app.Activity.RESULT_OK;
import static com.tjr.wordsearchsolver.data.RequestCode.LOAD_WORDSEARCH_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCode.LOAD_WORDS_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCode.TAKE_WORDSEARCH_PHOTO;
import static com.tjr.wordsearchsolver.data.RequestCode.TAKE_WORDS_PHOTO;

public class LoadFragment extends Fragment implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger(LoadFragment.class);
    private ReentrantLock lock = new ReentrantLock();

    private DataStore dataStore;
    private ImageProcessor wordFinder;

    private Button loadWordsearchCameraButton;
    private Button loadWordsearchPhotoButton;
    private Button loadWordsCameraButton;
    private Button loadWordsPhotoButton;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_load, container, false);

        dataStore = DataStore.getDataStore();
        wordFinder = new ImageProcessor();

        // Set button listeners
        loadWordsearchCameraButton = root.findViewById(R.id.button_load_wordsearch_camera);
        loadWordsearchCameraButton.setOnClickListener(this);

        loadWordsearchPhotoButton = root.findViewById(R.id.button_load_wordsearch_photo);
        loadWordsearchPhotoButton.setOnClickListener(this);

        loadWordsCameraButton = root.findViewById(R.id.button_load_words_camera);
        loadWordsCameraButton.setOnClickListener(this);

        loadWordsPhotoButton = root.findViewById(R.id.button_load_words_photo);
        loadWordsPhotoButton.setOnClickListener(this);

        Executors.newSingleThreadExecutor().execute(this::loadStoredValues);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_load_wordsearch_camera:
                takePicture(TAKE_WORDSEARCH_PHOTO.code);
                break;
            case R.id.button_load_wordsearch_photo:
                selectPicture(LOAD_WORDSEARCH_PHOTO.code);
                break;
            case R.id.button_load_words_camera:
                takePicture(TAKE_WORDS_PHOTO.code);
                break;
            case R.id.button_load_words_photo:
                selectPicture(LOAD_WORDS_PHOTO.code);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == LOAD_WORDSEARCH_PHOTO.code || requestCode == LOAD_WORDS_PHOTO.code) {
                Uri selectedImageURI = data.getData();
                if (requestCode == LOAD_WORDSEARCH_PHOTO.code && checkBitmapIsntEmpty(selectedImageURI)) {
                    dataStore.setWordsearchImagePath(selectedImageURI);
                    dataStore.setWordsearchFromCamera(false);
                    getWordsFromBitmap(LOAD_WORDSEARCH_PHOTO.code);
                } else {
                    dataStore.setSearchWordsImagePath(selectedImageURI);
                    dataStore.setAreWordsFromCamera(false);
                    getWordsFromBitmap(LOAD_WORDS_PHOTO.code);
                }
            } else if (requestCode == TAKE_WORDSEARCH_PHOTO.code || requestCode == TAKE_WORDS_PHOTO.code) {
                if (requestCode == TAKE_WORDSEARCH_PHOTO.code && checkBitmapIsntEmpty(dataStore.getWordsearchImagePath())) {
                    dataStore.setWordsearchFromCamera(true);
                    getWordsFromBitmap(TAKE_WORDSEARCH_PHOTO.code);
                } else if (checkBitmapIsntEmpty(dataStore.getSearchWordsImagePath())) {
                    dataStore.setAreWordsFromCamera(true);
                    getWordsFromBitmap(TAKE_WORDS_PHOTO.code);
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
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = createImageFile(requestCode);
            Uri photoUri = FileProvider.getUriForFile(requireActivity().getApplicationContext(), "com.example.android.fileprovider", photoFile);

            if (requestCode == TAKE_WORDSEARCH_PHOTO.code)
                dataStore.setWordsearchImagePath(photoUri);
            else if (requestCode == TAKE_WORDS_PHOTO.code)
                dataStore.setSearchWordsImagePath(photoUri);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, requestCode);
        }
    }

    private File createImageFile(int requestCode) {
        String imageFileName = "Image_";

        if (requestCode == TAKE_WORDSEARCH_PHOTO.code)
            imageFileName += "Wordsearch.png";
        else if (requestCode == TAKE_WORDS_PHOTO.code)
            imageFileName += "Words.png";

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName);
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

    private void loadStoredValues() {
        if (dataStore.getIsWordsearchFromCamera())
            loadWordsearchCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
        else if (dataStore.getWordsearchImagePath() != null)
            loadWordsearchPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);

        if (dataStore.getAreWordsFromCamera())
            loadWordsCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
        else if (dataStore.getSearchWordsImagePath() != null)
            loadWordsPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
    }

    private void getWordsFromBitmap(int requestCode) {
        if (requestCode == LOAD_WORDS_PHOTO.code || requestCode == TAKE_WORDS_PHOTO.code) {
            Future<List<String>> wordsFuture = null;
            try {
                lock.lock();
                try {
                    wordsFuture = wordFinder.recogniseWords(MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), dataStore.getSearchWordsImagePath()));
                } finally {
                    lock.unlock();
                    if (wordsFuture != null) {
                        dataStore.setSearchWords(wordsFuture.get());
                        if (requestCode == LOAD_WORDS_PHOTO.code) {
                            loadWordsPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                            loadWordsCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_camera, null), null, null, null);
                        } else {
                            loadWordsCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                            loadWordsPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo, null), null, null, null);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error getting words from words photo", e);
            } catch (ExecutionException | InterruptedException e) {
                logger.error("Error setting search words", e);
            }
        } else if (requestCode == LOAD_WORDSEARCH_PHOTO.code || requestCode == TAKE_WORDSEARCH_PHOTO.code) {
            Future<List<List<Character>>> wordsearchFuture = null;
            try {
                lock.lock();
                try {
                    wordsearchFuture = wordFinder.recogniseWordsearch(MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), dataStore.getWordsearchImagePath()));
                } finally {
                    lock.unlock();
                    if (wordsearchFuture != null) {
                        dataStore.setWordsearchGrid(wordsearchFuture.get());
                        if (requestCode == LOAD_WORDSEARCH_PHOTO.code) {
                            loadWordsearchPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                            loadWordsearchCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_camera, null), null, null, null);
                        } else {
                            loadWordsearchCameraButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tick, null), null, null, null);
                            loadWordsearchPhotoButton.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo, null), null, null, null);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error getting wordsearch from wordsearch photo", e);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error setting wordsearch", e);
            }
        }
    }
}