package com.example.mediabuttonsminimal;

import android.util.Log;
import android.util.Pair;

import java.util.LinkedList;
import java.util.Locale;

public class MediaButtonLogger extends LinkedList<Pair<Long, String>> {
    private final int maxSize;

    public MediaButtonLogger(int size) {
        this.maxSize = size;
    }

    /**
     * Override the add method to limit the size
     */
    public boolean add(String tag, String entry) {
        Log.d(tag, entry);
        boolean added = super.add(new Pair<>(System.currentTimeMillis(), entry));
        // if the size is greater than the max size, remove the oldest element (first in the list)
        while (added && size() > maxSize) {
            super.remove();
        }
        return added;
    }
}