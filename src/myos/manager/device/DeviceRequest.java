package myos.manager.device;

import myos.manager.process.PCB;


/**
 * 设备请求
 *
 * @author WTDYang
 * @date 2022/12/06
 */
public class DeviceRequest {
    /**
     * 请求pcb
     */
    private PCB pcb;
    /**
     * 占用时间，以毫秒为单位
     */
    private long workTime;
    /**
     * 设备名称
     */
    private String deviceName;
    public PCB getPcb() {
        return pcb;
    }

    public void setPcb(PCB pcb) {
        this.pcb = pcb;
    }

    public long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(long workTime) {
        this.workTime = workTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
