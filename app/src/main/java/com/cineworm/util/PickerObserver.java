package com.cineworm.util;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig;
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig;
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig;
import com.nareshchocha.filepickerlibrary.models.PickMediaType;
import com.nareshchocha.filepickerlibrary.ui.FilePicker;
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const;

import java.io.File;
import java.util.ArrayList;

public class PickerObserver implements DefaultLifecycleObserver {
    private final ActivityResultRegistry registry;
    private final Activity activity;
    private PickerListener pickerListener;

    ActivityResultCallback<ActivityResult> pickerResultIntent = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                if (result != null && result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String path = data.getStringExtra(Const.BundleExtras.FILE_PATH);
                        File file = new File(path);
                        Log.e("listData", "" + path);
                        pickerListener.onPicked(file);
                    }
                }
            } catch (Exception e) {
                Log.e("FILE_RESULT", "" + e);
            }
        }
    };

    private ActivityResultLauncher<Intent> pickerLauncher;

    public PickerObserver(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        this.registry = activity.getActivityResultRegistry();
    }

    public PickerObserver(@NonNull Fragment fr) {
        this.activity = fr.requireActivity();
        this.registry = fr.requireActivity().getActivityResultRegistry();
    }


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        //  DefaultLifecycleObserver.super.onCreate(owner);
        pickerLauncher = registry.register("imagePick", owner, new ActivityResultContracts.StartActivityForResult(), pickerResultIntent);
    }

    public void pickImage(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        pickerLauncher.launch(new FilePicker.Builder(activity).pickMediaBuild(new PickMediaConfig(null, null, null, null, PickMediaType.ImageOnly, null, null, null, null)));
    }

    public void pickVideo(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        pickerLauncher.launch(new FilePicker.Builder(activity).pickMediaBuild(new PickMediaConfig(null, null, null, null, PickMediaType.VideoOnly, null, null, null, null)));
    }

    public void pickPdf(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        ArrayList<String> mMimeTypesList = new ArrayList<>();
        mMimeTypesList.add("application/pdf");
        pickerLauncher.launch(new FilePicker.Builder(activity).pickDocumentFileBuild(new DocumentFilePickerConfig(null, null, null, null, mMimeTypesList, null, null, null, null)));
    }

    public void pickAudio(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        ArrayList<String> mMimeTypesList = new ArrayList<>();
        mMimeTypesList.add("audio/*");
        pickerLauncher.launch(new FilePicker.Builder(activity).pickDocumentFileBuild(new DocumentFilePickerConfig(null, null, null, null, mMimeTypesList, null, null, null, null)));
    }

    public void pickEpub(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        ArrayList<String> mMimeTypesList = new ArrayList<>();
        mMimeTypesList.add("application/epub+zip");
        pickerLauncher.launch(new FilePicker.Builder(activity).pickDocumentFileBuild(new DocumentFilePickerConfig(null, null, null, null, mMimeTypesList, null, null, null, null)));
    }

    public void pickAll(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        ArrayList<String> mMimeTypesList = new ArrayList<>();
        mMimeTypesList.add("*/*");
        pickerLauncher.launch(new FilePicker.Builder(activity).pickDocumentFileBuild(new DocumentFilePickerConfig(null, null, null, null, mMimeTypesList, null, null, null, null)));
    }

    public void captureImage(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        pickerLauncher.launch(new FilePicker.Builder(activity).imageCaptureBuild(new ImageCaptureConfig(null, null, null, null, null, null, null, null)));
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        //  DefaultLifecycleObserver.super.onStart(owner);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onStop(owner);
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onPause(owner);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onDestroy(owner);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onResume(owner);
    }
}
