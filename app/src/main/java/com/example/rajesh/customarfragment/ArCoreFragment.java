package com.example.rajesh.customarfragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class ArCoreFragment extends ArFragment {



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Config getSessionConfiguration(Session session) {
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        getArSceneView().setupSession(session);
        return config;

    }
}
