package org.chimeramc.launcher.ui.activities;

import android.os.Bundle;

import org.chimeramc.launcher.R;

public class ControllerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        setupNavBar();
     }

    private void setupNavBar() {

        setActiveNavTab(R.id.nav_tab_controller);
        findViewById(R.id.nav_tab_controller).setOnClickListener(v -> {});
     }
}