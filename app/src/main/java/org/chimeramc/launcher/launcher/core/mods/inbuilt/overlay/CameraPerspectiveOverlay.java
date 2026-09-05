package org.chimeramc.launcher.core.mods.inbuilt.overlay;

import android.app.Activity;
import android.view.KeyEvent;
import android.widget.ImageButton;

import org.chimeramc.launcher.R;
import org.chimeramc.launcher.core.mods.inbuilt.model.ModIds;

public class CameraPerspectiveOverlay extends BaseOverlayButton {
    public CameraPerspectiveOverlay(Activity activity) {
        super(activity);
    }

    @Override
    protected String getModId() {
        return ModIds.CAMERA_PERSPECTIVE;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_camera;
    }

    @Override
    protected void onButtonPressStart() {
        setPressedIcon(true);
    }

    @Override
    protected void onButtonPressEnd() {
        setPressedIcon(false);
    }

    private void setPressedIcon(boolean pressed) {
        if (overlayView instanceof ImageButton) {
            ((ImageButton) overlayView).setImageResource(
                    pressed ? R.drawable.ic_camera_pressed : R.drawable.ic_camera);
        }
    }

    @Override
    protected void onButtonClick() {
        sendKey(KeyEvent.KEYCODE_F5);
    }
}
