package osmo.devweekhack2016.com.interfaces;

import dji.sdk.Camera.DJICamera;
import dji.sdk.base.DJIBaseProduct;

/**
 * Created by Michael Yoon Huh on 2/13/2016.
 */
public interface OnDeviceConnected {
    void deviceConnected(DJIBaseProduct product, DJICamera Camera);
}
