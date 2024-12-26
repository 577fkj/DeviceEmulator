// IDeviceEmulatorInterface.aidl
package cn.fkj233.deviceemulator.aidl;

import cn.fkj233.deviceemulator.aidl.mock.IMockLocationInterface;
import cn.fkj233.deviceemulator.aidl.mock.IMockDeviceInfoInterface;

interface IDeviceEmulatorInterface {

    IMockDeviceInfoInterface getMockDeviceInfo();

    IMockLocationInterface getMockLocation();

    int getVersion();

}