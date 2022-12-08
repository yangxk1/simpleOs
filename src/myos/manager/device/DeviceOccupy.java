package myos.manager.device;

import myos.manager.process.PCB;
import myos.utils.DelayItem;

import java.util.concurrent.TimeUnit;


/**
 * 设备占用
 *
 * @author WTDYang
 * @date 2022/12/06
 */
public class DeviceOccupy extends DelayItem<PCB> {
    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备昵称
     */
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public DeviceOccupy(PCB obj, long workTime, TimeUnit timeUnit) {
        super(obj, workTime, timeUnit);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
