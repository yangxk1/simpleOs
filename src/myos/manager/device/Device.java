package myos.manager.device;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备
 * @author WTDYang
 * @date 2022/12/06
 */
public class Device {
    /**
     * 空闲位
     */
    public static final int STATUS_FREE=0;
    /**
     * 占用位
     */
    public static final int STATUS_BUSY=1;
    //设备状态
    private int status;
    //占用时间
    private int timeout;
    //设备名称
    protected String name = "未知设备";



    /**
     * 设备队列
     */
    protected ArrayBlockingQueue<String> deviceQueue;

    public String getDeviceNiceName() throws InterruptedException {
        return deviceQueue.take();
    }

    public void addDevice(String niceName) {
        this.deviceQueue.add(niceName);
    }
    /**
     * 数
     */
    private  volatile AtomicInteger count;
    public Device(int count){
        this.count=new AtomicInteger(count);
    }
    public int decreaseCount(){
        return count.getAndDecrement();
    }
    public int getCount() {
        return count.intValue();
    }
    public void increaseCount(){
        count.getAndIncrement();
    }
    public void setCount(int count) {
        this.count.set(count);
    }
    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }



    public void setName(String name) {
        this.name = name;
    }


}
