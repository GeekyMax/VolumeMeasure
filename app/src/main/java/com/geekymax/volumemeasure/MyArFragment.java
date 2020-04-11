package com.geekymax.volumemeasure;

import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.geekymax.volumemeasure.entity.OnSceneUpdateListener;
import com.google.ar.core.Config;
import com.google.ar.core.Session;

import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Implements AR Required ArFragment. Does not require additional permissions and uses the default
 * configuration for ARCore.
 */
public class MyArFragment extends BaseArFragment {
    private static final String TAG = "StandardArFragment";

    private OnSceneUpdateListener onUpdateListener;

    @Override
    public boolean isArRequired() {
        return true;
    }

    @Override
    public String[] getAdditionalPermissions() {
        return new String[0];
    }

    @Override
    protected void handleSessionException(UnavailableException sessionException) {

        String message;
        if (sessionException instanceof UnavailableArcoreNotInstalledException) {
            message = "请安装ARCore";
        } else if (sessionException instanceof UnavailableApkTooOldException) {
            message = "请更新ARCore";
        } else if (sessionException instanceof UnavailableSdkTooOldException) {
            message = "请更新app";
        } else if (sessionException instanceof UnavailableDeviceNotCompatibleException) {
            message = "当前设备部支持AR";
        } else {
            message = "未能创建AR会话,请查看机型适配,arcore版本与系统版本";
        }
        Log.e(TAG, "Error: " + message, sessionException);
        Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        return new Config(session);
    }


    @Override
    protected Set<Session.Feature> getSessionFeatures() {
        return Collections.emptySet();
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        if (onUpdateListener != null) {
            onUpdateListener.onUpdate(this.getArSceneView());
        }
    }

    /**
     * 设置OnUpdate回调
     *
     * @param onUpdateListener OnUpdateListener
     */
    public void setOnUpdateListener(OnSceneUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment" + this.getArSceneView().getSession());

    }
}
