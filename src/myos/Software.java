package myos;

import myos.constant.OsConstant;
import myos.controller.MainController;
import myos.manager.device.DeviceManager;
import myos.manager.filesys.FileOperator;
import myos.manager.memory.Memory;
import myos.manager.process.CPU;
import myos.manager.process.Clock;
import myos.manager.process.ProcessCreator;
import myos.utils.ThreadPoolUtil;

import java.io.*;

import static myos.constant.OsConstant.DISK_BLOCK_QUNTITY;
import static myos.constant.OsConstant.DISK_BLOCK_SIZE;

@SuppressWarnings("all")
public class Software {
    /**
     * 磁盘
     */
    public static RandomAccessFile disk;
    /**
     * 文件操作
     */
    public static FileOperator fileOperator;
    /**
     * 过程创造者
     */
    public static ProcessCreator processCreator;
    /**
     * cpu
     */
    public static CPU cpu;
    /**
     * 内存
     */
    public static Memory memory;
    /**
     * 时钟
     */
    public static Clock clock;
    /**
     * 开机状态
     */
    public static volatile boolean launched;
    /**
     * 界面控制类
     */
    public  MainController mainController;
    public static volatile Software instance;
    static {
        try {
            DeviceManager.initDisk();
//            RandomAccessFile支持"随机访问"的方式，程序可以直接跳转到文件的任意地方来读写数据。
            disk = new RandomAccessFile(OsConstant.DISK_FILE, "rw");
            memory = new Memory();
            cpu = new CPU();
            clock = new Clock();
            processCreator = new ProcessCreator();
            fileOperator = new FileOperator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Software() throws Exception {     }
    public static Software getInstance() throws Exception {
        if (instance == null) {
            // 加锁 对类进行加锁，效果是一旦发现实例对象未初始化，那么立刻锁住对象，保证初始化未完成之前的线程只能有序进行操作。
            synchronized (Software.class) {
                    /* 为什么这里也需要进行判断？ 在上一行的代码进行加锁保证了不同线程对此处的有序进行，如果不判断null，那么放进来
                    的线程将会依次对实例进行初始化，因此判断一个null，就可以保证只有第一个进来的线程可以进行初始化。*/
                if (instance == null) {
                    instance = new Software();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化系统
     */
    public void init() throws Exception {
        cpu.init();
        memory.init();
        clock.init();
        fileOperator.init();
    }

    /**
     * 初始化模拟磁盘
     */
   static void initDisk() {
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

    //启动系统
    public void start() throws Exception {
        init();
        ThreadPoolUtil instance = ThreadPoolUtil.getInstance();
        instance.execute(clock);
        instance.execute(cpu);


    }


    /**
     * 关闭系统资源
     */
    public void close() {
        launched = false;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        fileOperator.setMainController(mainController);
    }

    public MainController getMainController() {
        return mainController;
    }
}
