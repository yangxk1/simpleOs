package myos.constant;

/**
 * 操作系统常量
 *
 * @author WTDYang
 * @date 2022/12/05
 */
public class OsConstant {
    /**
     * 磁盘文件
     */
    public  static final String DISK_FILE ="resources/disk.dat";
    /**
     * 物理块大小
     */
    public static final int DISK_BLOCK_SIZE = 64;
    /**
     * 物理块数量
     */
    public static final int DISK_BLOCK_QUNTITY = 128;
    /**
     * 内存
     * 用户面积大小
     */
    public static final int USER_AREA_SIZE=512;
    /**
     * PCB数量
     */
    public static final int PCB_COUNT=10;
    /**
     * 最大进程数
     */
    public static final int PROCESS_MAX=10;

}
