package com.elksa.ddsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ProgressBar pbProgress;
    private TextView txtProgress;
    private Group grpProgress;
    private String moduleImages;

    private SplitInstallManager manager;
    private SplitInstallStateUpdatedListener listener = new SplitInstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(SplitInstallSessionState state) {

            switch (state.status()) {
                case SplitInstallSessionStatus.DOWNLOADING:
                    displayLoadingState(state, "Downloading " + moduleImages);
                    break;
                case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                    try {
                        startIntentSender(state.resolutionIntent().getIntentSender(), null, 0, 0, 0);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Error requestig user confirmation: " + e.toString());
                    }
                    break;
                case SplitInstallSessionStatus.INSTALLED:
                    launchModule(moduleImages);
                    break;
                case SplitInstallSessionStatus.INSTALLING:
                    displayLoadingState(state, "Installing " + moduleImages);
                    break;
                case SplitInstallSessionStatus.FAILED:
                    Toast.makeText(MainActivity.this, "Error: " + state.errorCode() +
                            " for module " + state.moduleNames().get(0), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moduleImages = "images";

        manager = SplitInstallManagerFactory.create(this);

        findViewById(R.id.btn_pictures).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAndLaunchModule(moduleImages);
            }
        });

        findViewById(R.id.btn_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this,
                        "Requesting uninstall of dynamic modules, this will take place some time in the next 24 hours",
                        Toast.LENGTH_LONG).show();

                manager.deferredUninstall(new ArrayList<>(manager.getInstalledModules())).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // TODO Log information somewhere, maybe Firebase Analytics.
                    }
                });
            }
        });

        pbProgress = findViewById(R.id.pb_progress);
        txtProgress = findViewById(R.id.txt_progress);
        grpProgress = findViewById(R.id.grp_progress);
    }

    @Override
    protected void onPause() {
        manager.registerListener(listener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        manager.unregisterListener(listener);
        super.onResume();
    }

    private void loadAndLaunchModule(final String moduleName) {

        if (manager.getInstalledModules().contains(moduleName)) {
            updateProgressMessage("Already installed");
            launchModule(moduleName);
        } else {
            // Module is not installed, so load and install it.
            updateProgressMessage("Starting install for " + moduleName);
            SplitInstallRequest request = SplitInstallRequest.newBuilder().addModule(moduleName).build();
            manager.startInstall(request)
                    .addOnCompleteListener(new OnCompleteListener<Integer>() {
                        @Override
                        public void onComplete(Task<Integer> task) {
                            Toast.makeText(MainActivity.this, "Module " + moduleName +
                                    " installed", Toast.LENGTH_SHORT).show();
                            launchModule(moduleName);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(MainActivity.this, "Loading module " + moduleName,
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(MainActivity.this, "Error downloading module " +
                                    moduleName, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void launchModule(String moduleName) {
        String packageName = "com.elksa.images";
        //String packageName = getPackageName();
        startActivity(new Intent().setClassName(getPackageName(), packageName + "." +
                getLandingActivityForModule(moduleName)));
    }

    private void updateProgressMessage(String message) {
        if (grpProgress.getVisibility() != View.VISIBLE) {
            grpProgress.setVisibility(View.VISIBLE);
            txtProgress.setText(message);
        }
    }
    private String getLandingActivityForModule(String moduleName) {
        // TODO Add the rest of the modules
        return moduleName.equals(moduleImages) ? "MainActivity" : null;
    }

    private void displayLoadingState(SplitInstallSessionState state, String message) {

        grpProgress.setVisibility(View.VISIBLE);

        pbProgress.setMax(Integer.parseInt(String.valueOf(state.totalBytesToDownload())));
        pbProgress.setProgress(Integer.parseInt(String.valueOf(state.bytesDownloaded())));

        updateProgressMessage(message);
    }
}
