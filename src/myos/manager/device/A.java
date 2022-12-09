package myos.manager.device;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * a设备
 *
 * @author WTDYang
 * @date 2022/12/09
 */
public class A extends Device{

    public A(int count) {
        super(count);
        this.setName("A");
        this.deviceQueue = new ArrayBlockingQueue<>(count);
        for (int i = 1; i <= count; i++) {
            addDevice(i+"号"+name);
        }

    }
}
