package cn.fkj233.deviceemulator.common;

import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface;

public class Constants {
    public static final String DESCRIPTOR = IDeviceEmulatorInterface.class.getName();
    public static final int TRANSACTION = '_' << 24 | 'F' << 16 | 'E' << 8 | 'M';
    public static final int ACTION_GET_BINDER = 1;
}
