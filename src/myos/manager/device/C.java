package myos.manager.device;

import java.util.concurrent.ArrayBlockingQueue;

public class C extends Device {
    public C(int count) {
        super(count);
        this.setName("C");
        this.deviceQueue = new ArrayBlockingQueue<>(count);
        for (int i = 1; i <= count; i++) {
            addDevice(i+"号"+name);
        }
    }
}
