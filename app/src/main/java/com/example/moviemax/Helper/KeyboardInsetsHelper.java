package com.example.moviemax.Helper;

import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class KeyboardInsetsHelper {

    // Applies keyboard (IME) and system bar insets to the given view
    public static void applyKeyboardInsets(View rootView) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Adjust padding dynamically depending on keyboard visibility
            int bottomPadding = Math.max(imeInsets.bottom, systemInsets.bottom);

            v.setPadding(
                    systemInsets.left,
                    systemInsets.top,
                    systemInsets.right,
                    bottomPadding
            );

            return WindowInsetsCompat.CONSUMED;
        });
    }
}
