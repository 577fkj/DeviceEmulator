// IOnMockLocationListenerlInterface.aidl
package cn.fkj233.deviceemulator.aidl.mock;

interface IOnMockLocationListenerlInterface {
    void onRequestLocation(String packageName, int uid, boolean isSystem);
}