package org.chimeramc.launcher.ui.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.chimeramc.launcher.R;
import org.chimeramc.launcher.core.content.ContentImporter;
import org.chimeramc.launcher.core.content.ResourcePackItem;
import org.chimeramc.launcher.core.content.ResourcePackManager;
import org.chimeramc.launcher.core.versions.GameVersion;
import org.chimeramc.launcher.core.versions.VersionManager;
import org.chimeramc.launcher.ui.adapter.SkinsAdapter;
import org.chimeramc.launcher.ui.animation.DynamicAnim;
import org.chimeramc.launcher.util.LauncherStorage;
import org.chimeramc.launcher.util.PersonalizationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkinsActivity extends BaseActivity {

    private static final String PREFS_NAME = "skins_state";
    private static final String KEY_APPLIED_TYPE = "applied_type";
    private static final String KEY_APPLIED_NAME = "applied_name";

    private RecyclerView recycler;
    private SkinsAdapter adapter;
    private View loadingOverlay;
    private View emptyView;
    private VersionManager versionManager;
    private ActivityResultLauncher<String> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skins);
        setupNavBar();

        PersonalizationManager pm = new PersonalizationManager(this);
        applyCompactPadding(pm);
        View root = findViewById(android.R.id.content);
        if (root != null) {
            pm.applyAccentToView(root, this);
        }

        recycler = findViewById(R.id.skins_recycler);
        loadingOverlay = findViewById(R.id.skins_loading_overlay);
        emptyView = findViewById(R.id.skins_empty);
        Button importButton = findViewById(R.id.skins_import_button);
        Button emptyImportButton = findViewById(R.id.skins_empty_import_button);
        importButton.setOnClickListener(v -> startImport());
        emptyImportButton.setOnClickListener(v -> startImport());

        versionManager = VersionManager.get(this);
        adapter = new SkinsAdapter();
        adapter.setOnSkinActionListener(this::applySkinPack);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importSkinFile(uri);
                    }
                }
        );

        loadSkins();
    }

    private void applyCompactPadding(PersonalizationManager pm) {
        View root = findViewById(android.R.id.content);
        if (root instanceof android.view.ViewGroup vg) {
            int pad = (int) ((pm.isCompactMode() ? 8 : 16) * getResources().getDisplayMetrics().density);
            vg.setPadding(0, pad, 0, pad);
        }
    }

    private void startImport() {
        try {
            importLauncher.launch("application/zip");
        } catch (Exception e) {
            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void importSkinFile(Uri uri) {
        Toast.makeText(this, R.string.skins_loading, Toast.LENGTH_SHORT).show();
        GameVersion version = versionManager.getSelectedVersion();
        String profileId = version != null ? version.getStorageProfileId() : LauncherStorage.INSTALLED_MINECRAFT_PROFILE_ID;
        File gameDataDir = LauncherStorage.getProfileGameDataDir(this, profileId, true);
        File resDir = new File(gameDataDir, "resource_packs");
        File behDir = new File(gameDataDir, "behavior_packs");
        File skinDir = new File(gameDataDir, "skin_packs");
        List<Uri> uris = new ArrayList<>();
        uris.add(uri);
        new ContentImporter(this).importContent(uris, resDir, behDir, skinDir, null,
                new ContentImporter.ImportCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(SkinsActivity.this, message, Toast.LENGTH_LONG).show();
                            loadSkins();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(SkinsActivity.this, error, Toast.LENGTH_LONG).show();
                            loadSkins();
                        });
                    }

                    @Override
                    public void onProgress(int progress) {
                    }
                });
    }

    private void loadSkins() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        }
        new Thread(() -> {
            List<ResourcePackItem> packs = readSkinPacks();
            String applied = readAppliedPackName();
            runOnUiThread(() -> {
                appliedPackName = applied;
                adapter.updateSkinPacks(packs, applied, true);
                boolean hasPacks = !packs.isEmpty();
                if (emptyView != null) emptyView.setVisibility(hasPacks ? View.GONE : View.VISIBLE);
                if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                recycler.setVisibility(hasPacks ? View.VISIBLE : View.GONE);
                recycler.post(() -> DynamicAnim.staggerRecyclerChildren(recycler));
            });
        }).start();
    }

    private List<ResourcePackItem> readSkinPacks() {
        GameVersion version = versionManager.getSelectedVersion();
        if (version == null) return new ArrayList<>();
        ResourcePackManager manager = new ResourcePackManager(this);
        manager.setCurrentVersion(version);
        List<ResourcePackItem> packs = manager.getSkinPacks();
        return packs != null ? packs : new ArrayList<>();
    }

    private void applySkinPack(ResourcePackItem pack) {
        GameVersion version = versionManager.getSelectedVersion();
        if (version == null) {
            Toast.makeText(this, R.string.skins_no_version, Toast.LENGTH_LONG).show();
            return;
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_APPLIED_TYPE, version.isInstalled ? "installed" : "custom")
                .putString(KEY_APPLIED_NAME, pack.getPackName())
                .apply();
        Toast.makeText(this, getString(R.string.skins_applied, pack.getPackName()), Toast.LENGTH_SHORT).show();
        loadSkins();
    }

    private String readAppliedPackName() {
        GameVersion version = versionManager.getSelectedVersion();
        if (version == null) return null;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String type = prefs.getString(KEY_APPLIED_TYPE, "");
        String name = prefs.getString(KEY_APPLIED_NAME, null);
        if (name == null || !type.equals(version.isInstalled ? "installed" : "custom")) return null;
        return name;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSkins();
    }

    private void setupNavBar() {
        setActiveNavTab(R.id.nav_tab_skins);
        findViewById(R.id.nav_tab_skins).setOnClickListener(v -> {
        });
    }
}
