package au.com.tyo.common.tv;

import android.view.View;

/**
 * Created by monfee on 14/3/17.
 */

public interface FocusHighlightHandler {

    /**
     * Called when an item gains or loses focus.
     * @hide
     *
     * @param view The view whose focus is changing.
     * @param hasFocus True if focus is gained; false otherwise.
     */
    void onItemFocused(View view, boolean hasFocus);

    /**
     * Called when the view is being created.
     */
    void onInitializeView(View view);

}