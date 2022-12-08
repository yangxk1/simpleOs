package myos.manager.device;

import java.util.concurrent.ArrayBlockingQueue;

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
