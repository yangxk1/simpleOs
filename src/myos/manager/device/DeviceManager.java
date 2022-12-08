package myos.manager.device;

import myos.Software;
import myos.constant.OsConstant;
import myos.manager.process.Clock;
import myos.manager.process.CPU;
import myos.utils.ThreadPoolUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import static myos.constant.OsConstant.DISK_BLOCK_QUNTITY;
import static myos.constant.OsConstant.DISK_BLOCK_SIZE;

/**
 * Created by lindanpeng on 2017/12/28.
 */
public class DeviceManager{
    private CPU cpu;
    private A a;
    private B b;
    private C c;
    /**
     * 使用中的设备
     */
    private DelayQueue<DeviceOccupy> usingDevices;
    /**
     * 等待使用设备的进程队列
     */
    private BlockingQueue<DeviceRequest> waitForDevice;

    public DeviceManager(CPU cpu){
        //A设备1个
        a= new A(1);
        //B设备2个
        b= new B(2);
        //C设备3个
        c= new C(3);
        usingDevices =new DelayQueue<>();
        waitForDevice=new ArrayBlockingQueue<>(20);
        this.cpu=cpu;
    }
    public static void initDisk() {
        File file = new File(OsConstant.DISK_FILE);
        FileOutputStream fout = null;
        //判断模拟磁盘是否已经创建
        if (!file.exists()) {
            try {
                fout = new FileOutputStream(file);
                byte[] bytes;
                for (int i = 0; i < DISK_BLOCK_QUNTITY; i++) {
                    bytes = new byte[DISK_BLOCK_SIZE];
                    //写入初始文件分配表
                    if (i == 0) {
                        //前三个盘块不可用
                        bytes[0] = -1;
                        bytes[1] = -1;
                        bytes[2] = -1;
                    }
                    //写入根目录
                    if (i == 2) {

                        bytes[0] = 'r';//根目录名为rt
                        bytes[1] = 'o';
                        bytes[2] = 'o';
                        bytes[3] = 't';
                        bytes[4] = 0;
                        bytes[5] = Byte.parseByte("00001000", 2);//目录属性
                        bytes[6] = -1;//起始盘号
                        bytes[7] = 0;//保留一字节未使用
                    }
                    fout.write(bytes);
                }
            } catch (FileNotFoundException e) {
                java.lang.System.out.println("打开/新建磁盘文件失败！");
                e.printStackTrace();
                java.lang.System.exit(0);
            } catch (IOException e) {
                java.lang.System.out.println("写入文件时发生错误");
                e.printStackTrace();
                java.lang.System.exit(0);
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        java.lang.System.out.println("关闭文件流时发生错误");
                        e.printStackTrace();
                    }
                }
            }

        } else {
            System.out.println("模拟磁盘已存在，无需重新创建");
        }

    }
    public void  init(){
        //释放设备线程
        ThreadPoolUtil.getInstance().execute(()-> {
            while(Software.launched){
                try {
                    DeviceOccupy deviceOccupy = usingDevices.take();
                    System.out.println(deviceOccupy.getNickName()+"设备使用完毕");
                    deviceDone(deviceOccupy);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //处理设备申请请求线程
        ThreadPoolUtil.getInstance().execute(()-> {
            while(Software.launched){
                try {
                    DeviceRequest deviceRequest=waitForDevice.take();
                    DeviceOccupy deviceOccupy=new DeviceOccupy(deviceRequest.getPcb(),deviceRequest.getWorkTime(), TimeUnit.MILLISECONDS);
                    deviceOccupy.setDeviceName(deviceRequest.getDeviceName());
                    switch (deviceRequest.getDeviceName()){
                        case "A":
                            //如果有设备空闲就使用设备
                            synchronized (A.class){
                                chooseDevice(a,deviceRequest,deviceOccupy);
                            }
                            break;
                        case "B":
                            synchronized (B.class) {
                                //如果有B设备空闲就使用设备
                                chooseDevice(b,deviceRequest,deviceOccupy);
                            }
                            break;
                        case "C":
                            synchronized (C.class) {
                                //如果有C设备空闲就使用设备
                                chooseDevice(c,deviceRequest,deviceOccupy);
                            }
                            break;
                        default:
                            System.out.println("未知设备");
                    }
                    Thread.sleep(Clock.TIMESLICE_UNIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * 请求使用设备
     * @param
     */
    public void requestDevice(DeviceRequest deviceRequest){
        try {
            System.out.println("进程"+deviceRequest.getPcb().getPID()+"请求使用设备"+deviceRequest.getDeviceName());
            waitForDevice.put(deviceRequest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void chooseDevice(Device device, DeviceRequest deviceRequest, DeviceOccupy deviceOccupy) throws InterruptedException {

            //如果有设备空闲就使用设备
            if (device.getCount()>0){
                //可用设备减1
                device.decreaseCount();
                deviceOccupy.setNickName(device.getDeviceNiceName());
                System.out.println("设备"+deviceOccupy.getNickName()+"可用,分配给进程"+deviceRequest.getPcb().getPID());
                usingDevices.put(deviceOccupy);
            }else {
                //否则将设备请求重新放到请求队列中
                System.out.println("无可用"+deviceRequest.getDeviceName()+"设备，等待分配");
                waitForDevice.put(deviceRequest);
            }
    }
    /**
     * 设备使用结束，释放资源，请求中断
     */
    private void deviceDone(DeviceOccupy deviceOccupy){
        //释放资源
        switch (deviceOccupy.getDeviceName()){
            case "A":a.increaseCount();a.addDevice(deviceOccupy.getNickName());break;
            case "B":b.increaseCount();b.addDevice(deviceOccupy.getNickName());break;
            case "C":c.increaseCount();c.addDevice(deviceOccupy.getNickName());break;
            default:break;
        }

       //将进程从阻塞队列中移到就绪队列
      cpu.awake(deviceOccupy.getObj());

    }

    public DelayQueue<DeviceOccupy> getUsingDevices() {
        return usingDevices;
    }

    public BlockingQueue<DeviceRequest> getWaitForDevice() {
        return waitForDevice;
    }
}
