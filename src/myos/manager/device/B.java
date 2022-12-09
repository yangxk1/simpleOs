package myos.manager.device;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * b设备
 *
 * @author WTDYang
 * @date 2022/12/09
 */
public class B extends Device{
    public B(int count) {
        super(count);
        this.setName("B");
        this.deviceQueue = new ArrayBlockingQueue<>(count);
        for (int i = 1; i <= count; i++) {
            addDevice(i+"号"+name);
        }
    }
}
