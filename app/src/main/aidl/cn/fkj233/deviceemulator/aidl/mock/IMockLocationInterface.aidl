// IMockLocationInterface.aidl
package cn.fkj233.deviceemulator.aidl.mock;

import android.location.Location;
import cn.fkj233.deviceemulator.aidl.mock.IOnMockLocationListenerlInterface;

interface IMockLocationInterface {

    void registerMockLocationListener(in IOnMockLocationListenerlInterface listener);

    void setMockStatus(in boolean status);
    boolean getMockStatus();

    void setMockLocation(in Location location);
    Location getMockLocation();

    void setLocationUpdateInterval(in long interval);
    long getLocationUpdateInterval();
}