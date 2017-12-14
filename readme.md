# ARCore-Hacks

ARCore looks very nice but is missing some really obvious functionality in comparison with ARKit on iOS

* Access to the raw camera data
* Access to the camera intrinsics

Using reflection I've done a few small(ish) hacks to get into the internal Tango objects and get the sort of information I need. I don't know why Google didn't just do this in the first place, it is pretty trivial for me to add these so the maintainers of the library could do pretty easily themselves I'm sure.

WARNING: These are hacks, they are dangerous and should be used with extra caution. The use of reflection to access internal types means that if/when the library, interfaces or implementation changes this app will become non-functional

Works on Google Pixel running 8.0 on 14/12/2017.

## Hacks in detail

* Added TangoWrapperCustom.java. This allows me to image callbacks. Being at the same package level allows me to get to ITango which is what the mTango implements in the Session class.
* SessionHack.java. This is designed to be at the same level and provide public access to the TangoCameraIntrinsics class
* Use reflection to update mTango to TangoWrapperCustom

```
mSession = new Session(/*context=*/this);
mSessionHack = new SessionHack(mSession);

Field[] fields = mSession.getClass().getDeclaredFields();
for (Field field : fields) {
    Log.d(TAG, field.getType().toString());
    if (field.getType().toString().contains("ITango")) {
        field.setAccessible(true);
        try {
            mTango = new TangoWrapperCustom();
            field.set(mSession, mTango);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new RuntimeException("Unable to get tango object");
        }
        break;
    }
}
```
* Add listener to tango object. Looking at the decompiled Session object I can see it only adds listeners after the Tango object has correctly initialised. This happens in the resume of the Session object. So I put in a delay with an arbitrary value (5ms) to add my own listener:

```
mTangoBinder.postDelayed(new Runnable() {
    @Override
    public void run() {
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList();
        framePairs.add(new TangoCoordinateFramePair(1, 2));
        Tango.TangoUpdateCallback callback = new Tango.TangoUpdateCallback() {
            public void onPoseAvailable(TangoPoseData pose) {


            }

            public void onPointCloudAvailable(TangoPointCloudData pointCloud) {

            }

            public void onTangoEvent(TangoEvent event) {

            }

            public void onFrameAvailable(int cameraId) {


            }

            public void onImageAvailable(TangoImage image, TangoCameraMetadata metadata, int cameraId) {
                Log.d(TAG, String.format("dimensions: %dx%d format: %d", image.width, image.height, image.format));
                Log.d(TAG, String.format("planes: %d", image.numPlanes));

                if (image.format == TangoImage.YCRCB_420_SP) {
                    ByteBuffer luminance = image.planeData[0];
                    ByteBuffer uPlane = image.planeData[1];
                    ByteBuffer vPlane = image.planeData[2];
                    Log.d(TAG, String.format("stride 0: %d stride 1: %d stride 2: %d", image.planePixelStride[0], image.planePixelStride[1], image.planePixelStride[2]));
                    Log.d(TAG, String.format("row stride 0: %d row stride 1: %d row stride 2: %d", image.planeRowStride[0], image.planeRowStride[1], image.planeRowStride[2]));
                    Log.d(TAG, String.format("plane size 0: %d plane size 1: %d plane size 2: %d", image.planeSize[0], image.planeSize[1], image.planeSize[2]));

                    final int screenOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                    TangoCameraIntrinsics intrinsics = mSessionHack.cameraIntrinsics(screenOrientation);
                    Log.d(TAG, String.format("Focal length: %fx%f, principle point %fx%f image size %dx%d", intrinsics.fx, intrinsics.fy, intrinsics.cx, intrinsics.cy, intrinsics.width, intrinsics.height));
                }
            }
        };
        mTango.connectListener(framePairs, callback);
    }
}, 5);
```

I don't know if this delay is actually really needed or not, can play around with it here.
Now in onImageAvailable I see we are getting back an image with the format YCRCB_420_SP and I can pull out all the other bits and pieces we need.

## Risks

Obviously because ARCore is not bundled in your app, you're out of control. Pretty much all problems can be caught at least and dealt with in a reasonable 'graceful' way, i.e. this app is running an unsupported version of ARCore. But this is not ideal.
* ITango can change. We can validate all the methods we expect and fail otherwise.
* Session may not hold onto the ITango interface any more.
* The TangoWrapper may start to include other logic which will be wiped out by my TangoWrapperCustom.

