package com.cineworm.util;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class Picker {

    private PickerObserver observer;

    private Picker picker;

    private Picker() {
    }

    public Picker(AppCompatActivity activity) {
        observer = new PickerObserver(activity);
        activity.getLifecycle().addObserver(observer);
        if (picker == null) {
            picker = new Picker();
        }
    }

    public Picker(Fragment fragment) {
        observer = new PickerObserver(fragment);
        fragment.getLifecycle().addObserver(observer);
        if (picker == null) {
            picker = new Picker();
        }
    }

    public void pickImage(PickerListener pickerListener) {
        observer.pickImage(pickerListener);
    }

    public void pickAll(PickerListener pickerListener) {
        observer.pickAll(pickerListener);
    }

    public void pickVideo(PickerListener pickerListener) {
        observer.pickVideo(pickerListener);
    }

    public void pickAudio(PickerListener pickerListener) {
        observer.pickAudio(pickerListener);
    }

    public void pickPdf(PickerListener pickerListener) {
        observer.pickPdf(pickerListener);
    }

    public void pickEpub(PickerListener pickerListener) {
        observer.pickEpub(pickerListener);
    }

    public void captureImage(PickerListener pickerListener) {
        observer.captureImage(pickerListener);
    }

}
