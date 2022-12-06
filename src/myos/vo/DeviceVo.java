package myos.vo;

import javafx.beans.property.SimpleStringProperty;

/**
 * 设备签证官
 * @author WTDYang
 * @date 2022/12/04
 */
public class DeviceVo {
    private SimpleStringProperty deviceName;
    private SimpleStringProperty PID;
    public DeviceVo(String deviceName,int PID){
        this.deviceName=new SimpleStringProperty(deviceName);
        this.PID=new SimpleStringProperty(PID+"");
    }
    public String getDeviceName() {
        return deviceName.get();
    }

    public SimpleStringProperty deviceNameProperty() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName.set(deviceName);
    }

    public String getPID() {
        return PID.get();
    }

    public SimpleStringProperty PIDProperty() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID.set(PID);
    }
}
