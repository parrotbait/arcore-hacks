package com.google.ar.core;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.ar.core.exceptions.InternalException;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoCameraMetadata;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoImage;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.google.atap.tangoservice.experimental.TangoPlaneData;
import android.support.annotation.NonNull;
import com.google.ar.core.exceptions.InternalException;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.Tango.TangoUpdateCallback;
import com.google.atap.tangoservice.experimental.TangoPlaneData;

import java.util.ArrayList;
import java.util.List;

import java.util.List;

/**
 * Created by eddielong on 14/12/2017.
 */

public class TangoWrapperCustom implements ITango {

    Tango mTango;
    List<TangoCoordinateFramePair> mFramePair = null;
    ArrayList<TangoUpdateCallback> mCallbacks = new ArrayList<>();

    public TangoWrapperCustom() {
        }

    TangoUpdateCallback mCallback = new TangoUpdateCallback() {
        public void onPoseAvailable(TangoPoseData pose) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onPoseAvailable(pose);
            }
        }

        public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onXyzIjAvailable(xyzIj);
            }
        }

        public void onFrameAvailable(int cameraId) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onFrameAvailable(cameraId);
            }
        }

        public void onImageAvailable(TangoImage image, TangoCameraMetadata metadata, int cameraId) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onImageAvailable(image, metadata, cameraId);
            }
        }

        public void onTangoEvent(TangoEvent event) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onTangoEvent(event);
            }
        }

        public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onPointCloudAvailable(pointCloud);
            }
        }

        public void onOnlineCalibrationStatus(int status) {
            for (TangoUpdateCallback callback : mCallbacks) {
                callback.onOnlineCalibrationStatus(status);
            }
        }
    };
    public void createInstance(Context context, Runnable runOnTangoReady) {
        if(this.mTango != null) {
            throw new InternalException("TangoWrapper already has an instance");
        } else {
            this.mTango = new Tango(context, runOnTangoReady);
        }
    }

    public void destroyInstance() {
        this.mTango = null;
    }

    public boolean hasInstance() {
        return this.mTango != null;
    }

    public void connectListener(List<TangoCoordinateFramePair> framePairs, Tango.TangoUpdateCallback listener) {

        mCallbacks.add(listener);
        if (mFramePair == null) {
            mFramePair = framePairs;
            this.mTango.connectListener(mFramePair, mCallback);
        }
    }

    public void connectOnImageAvailable(int cameraId) {
        this.mTango.connectOnImageAvailable(cameraId);
    }

    public void connectOnTextureAvailable(int cameraId) {
        this.mTango.connectOnTextureAvailable(cameraId);
    }

    public double updateTextureExternalOes(int cameraId, int textureId) {
        return this.mTango.updateTextureExternalOes(cameraId, textureId);
    }

    public void disconnectCamera(int cameraId) {
        this.mTango.disconnectCamera(cameraId);
    }

    public void disconnect() {
        this.mTango.disconnect();
    }

    public TangoConfig getConfig(int configType) {
        return this.mTango.getConfig(configType);
    }

    public void connect(TangoConfig config) {
        this.mTango.connect(config);
    }

    @NonNull
    public TangoPoseData getPoseAtTime(double timestamp, TangoCoordinateFramePair coordinateFramePair) {
        return this.mTango.getPoseAtTime(timestamp, coordinateFramePair);
    }

    @NonNull
    public TangoCameraIntrinsics getCameraIntrinsics(int cameraId) {
        return this.mTango.getCameraIntrinsics(cameraId);
    }

    public void setRuntimeConfig(TangoConfig tangoConfig) {
        this.mTango.setRuntimeConfig(tangoConfig);
    }

    public List<TangoPlaneData> getPlanes() {
            return this.mTango.experimentalGetPlanes();
        }

}
