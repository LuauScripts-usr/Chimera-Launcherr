package org.chimeramc.launcher.ui.activities;

import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.chimeramc.launcher.R;
import org.chimeramc.launcher.launcher.controller.ControllerCatalog;
import org.chimeramc.launcher.launcher.controller.ControllerIllustration;
import org.chimeramc.launcher.launcher.controller.ControllerManager;
import org.chimeramc.launcher.launcher.controller.ControllerType;

import java.util.HashSet;
import java.util.Set;

public class ControllerActivity extends BaseActivity {
    private static final ControllerType[] MANUAL_CYCLE = {ControllerType.XBOX, ControllerType.DS4, ControllerType.DUAL_SENSE};

    private ControllerManager controllerManager;
    private ControllerType selectedType;
    private int manualIndex =  0;
    private TextView statusText;
    private TextView typeText;
    private TextView hintText;
    private Button nextButton;

    private FrameLayout illustrationHolder;
    private ControllerIllustration illustration;
    private ControllerType shownType;
    private final Set<String> activeHighlights = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        setupNavBar();

        statusText = findViewById(R.id.controller_status_text);
        typeText = findViewById(R.id.controller_type_text);
        hintText = findViewById(R.id.controller_hint_text);
        nextButton = findViewById(R.id.controller_next_btn);
        nextButton.setOnClickListener(v -> onNextClicked());
        illustrationHolder = findViewById(R.id.controller_illustration_holder);
        controllerManager = new ControllerManager(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        controllerManager.addListener(listener);
        controllerManager.register();
        render();
    }

    @Override
    protected void onPause(){
        super.onPause();
        controllerManager.unregister();
        controllerManager.removeListener(listener);
        if(illustration != null) illustration.clearAll();
        super.onPause();
    }

    private final ControllerManager.Listener listener = type -> onControllerChanged(type);

    private void onControllerChanged(ControllerType type){
        selectedType = type;
        manualIndex =  0;
        render();
    }

    private void onNextClicked(){
        manualIndex = (manualIndex + 1) % MANUAL_CYCLE.length;
        selectedType = null;
        render();
    }

    private void render(){
        ControllerType type = selectedType != null ? selectedType : MANUAL_CYCLE[manualIndex];
        if(selectedType != null){
            typeText.setText(type.getDisplayName());
            statusText.setText(R.string.controller_status_detected);
            hintText.setText(R.string.controller_connected_hint);
            nextButton.setVisibility(View.GONE);
        }else{
            typeText.setText(type.getDisplayName());
            statusText.setText(R.string.controller_status_none) ;
            hintText.setText(R.string.controller_manual_hint) ;
            nextButton.setVisibility(View.VISIBLE);
        }
        showIllustration(type);
    }

    private void showIllustration(ControllerType type){
        if(illustration != null && type == shownType) return;
        shownType = type;
        illustrationHolder.removeAllViews();
        ControllerCatalog.Spec spec = ControllerCatalog.specFor(type);
        illustration = new ControllerIllustration(this, illustrationHolder, spec.baseRes);
        for (ControllerIllustration.Region r : spec.regions) illustration.addRegion(r);
        for (ControllerCatalog.Label l : spec.labels) illustration.addLabel(l.text, l.cx, l.cy, l.above);
        illustration.clearAll();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        if(shownType != null && illustration != null){
            String tag = tagForKey(event.getKeyCode(), shownType);
            if(tag != null){
                boolean down = event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0;
                boolean up = event.getAction() == KeyEvent.ACTION_UP;
                if(down){
                    activeHighlights.add(tag);
                    illustration.highlight(tag);
                }else if(up){
                    activeHighlights.remove(tag);
                    illustration.clear(tag);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent e){
        if(shownType != null && illustration != null){
            int src = e.getSource();
            if((src & InputDevice.SOURCE_JOYSTICK) != 0){
                float x = e.getAxisValue(MotionEvent.AXIS_X);
                float y = e.getAxisValue(MotionEvent.AXIS_Y);
                float z = e.getAxisValue(MotionEvent.AXIS_Z);
                float rz = e.getAxisValue(MotionEvent.AXIS_RZ);
                boolean lMove = Math.abs(x) > 0.15f || Math.abs(y) > 0.15f;
                boolean rMove = Math.abs(z) > 0.15f || Math.abs(rz) > 0.15f;
                float lt = e.getAxisValue(MotionEvent.AXIS_LTRIGGER);
                float rt = e.getAxisValue(MotionEvent.AXIS_RTRIGGER);
                float hatX = e.getAxisValue(MotionEvent.AXIS_HAT_X);
                float hatY = e.getAxisValue(MotionEvent.AXIS_HAT_Y);
                boolean dpadActive = Math.abs(hatX) > 0.5f || Math.abs(hatY) > 0.5f;
                if(lMove || (shownType == ControllerType.XBOX && dpadActive)){
                    illustration.highlight((shownType == ControllerType.XBOX && dpadActive) ? "DPAD" : "LS");
                }else{
                    illustration.clear("LS");
                }
                if(rMove){
                    illustration.highlight("RS");
                }else{
                    illustration.clear("RS");
                }
                if(lt > 0.5f){
                    illustration.highlight("LT");
                }else{
                    illustration.clear("LT");
                }
                if(rt > 0.5f){
                    illustration.highlight("RT");
                }else{
                    illustration.clear("RT");
                }
            }
        }
        return super.onGenericMotionEvent(e);
    }

    private String tagForKey(int keyCode, ControllerType type){
        switch (keyCode){
            case KeyEvent.KEYCODE_BUTTON_A: return "A";
            case KeyEvent.KEYCODE_BUTTON_B: return "B";
            case KeyEvent.KEYCODE_BUTTON_X: return "X";
            case KeyEvent.KEYCODE_BUTTON_Y: return "Y";
            case KeyEvent.KEYCODE_BUTTON_L1: return "LB";
            case KeyEvent.KEYCODE_BUTTON_R1: return "RB";
            case KeyEvent.KEYCODE_BUTTON_L2: return "LT";
            case KeyEvent.KEYCODE_BUTTON_R2: return "RT";
            case KeyEvent.KEYCODE_BUTTON_THUMBL: return "LS";
            case KeyEvent.KEYCODE_BUTTON_THUMBR: return "RS";
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: return "DPAD";
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                if(type == ControllerType.DUAL_SENSE) return "CREATE";
                if(type == ControllerType.DS4) return "SHARE";
                return "VIEW";
            case KeyEvent.KEYCODE_BUTTON_START:
                if(type == ControllerType.XBOX) return "MENU";
                return "OPTIONS";
            case KeyEvent.KEYCODE_BUTTON_MODE:
                if(type == ControllerType.XBOX) return "XBOX";
                return "PS";
            default: return null;
        }
    }

    private void setupNavBar() {
        setActiveNavTab(R.id.nav_tab_controller);
        findViewById(R.id.nav_tab_controller).setOnClickListener(v -> {});
    }
}
