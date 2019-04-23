package com.wuzhexiaolu.campusui.geocomponent;

import com.supermap.realspace.Camera;
import com.supermap.realspace.Feature3D;
import com.supermap.realspace.LookAt;

class LandFeature {
    private Feature3D feature3D;
    private Camera camera;
    private LookAt lookAt;

    public LandFeature(Feature3D feature3D, Camera camera) {
        this.feature3D = feature3D;
        this.camera = camera;
    }

    public LandFeature(Feature3D feature3D, Camera camera, LookAt lookAt) {
        this(feature3D, camera);
        this.lookAt = lookAt;
    }

    public Feature3D getFeature3D() {
        return feature3D;
    }

    public Camera getCamera() {
        return camera;
    }

    public LookAt getLookAt() {
        return lookAt;
    }
}

