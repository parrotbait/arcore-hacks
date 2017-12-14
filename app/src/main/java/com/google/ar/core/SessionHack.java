package com.google.ar.core;

import com.google.atap.tangoservice.TangoCameraIntrinsics;

/**
 * Created by eddielong on 14/12/2017.
 */

public class SessionHack {
    Session mSession = null;
    public SessionHack(Session s) {
        mSession = s;
    }

    public TangoCameraIntrinsics cameraIntrinsics(int displayRotation) {
        return mSession.cameraStateProvider.getRotatedColorCameraIntrinsics(displayRotation);
    }


}
