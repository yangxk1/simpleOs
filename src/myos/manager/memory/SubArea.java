package myos.manager.memory;


/**
 * 内存分区
 *
 * @author WTDYang
 * @date 2022/12/07
 */
public class SubArea {
    /**
     * 状态空闲
     */
    public  static final int STATUS_FREE=0;
    /**
     * 分区被使用
     */
    public  static final int STATUS_BUSY=1;
    /**
     * 段基址
     */
     private int startAdd;
    /**
     * 分区大小
     */
     private int size;
    /**
     * 分区状态
     */
    private int status;
    /**
     * 任务号
     */
    private int taskNo;

    public int getStartAdd() {
        return startAdd;
    }

    public void setStartAdd(int startAdd) {
        this.startAdd = startAdd;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(int taskNo) {
        this.taskNo = taskNo;
    }
}
