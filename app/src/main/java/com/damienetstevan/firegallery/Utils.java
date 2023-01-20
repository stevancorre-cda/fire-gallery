package com.damienetstevan.firegallery;

import android.content.Context;
import android.widget.Toast;

public final class Utils {
    private Utils() {
    }

    public static void makeLongToast(final Context context, final String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void makeLongErrorToast(final Context context, final String text) {
        Toast.makeText(context, "ERREUR: " + text, Toast.LENGTH_LONG).show();
    }
}
