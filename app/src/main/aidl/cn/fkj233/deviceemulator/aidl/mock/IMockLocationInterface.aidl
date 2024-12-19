// IMockLocationInterface.aidl
package cn.fkj233.deviceemulator.aidl.mock;

import android.location.Location;

interface IMockLocationInterface {

    void setMockStatus(in boolean status);
    boolean getMockStatus();

    void setMockLocation(in Location location);
    Location getMockLocation();
}