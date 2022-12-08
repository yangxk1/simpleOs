package myos.manager.process;

import java.util.Random;

/**
 * Created by lindanpeng on 2017/12/24.
 */
public class PCB implements Comparable{
    /**
     * 就绪
     */
    public static final String STATUS_WAIT="WAIT";
    /**
     * 运行状态
     */
    public static final String STATUS_RUN="RUN";
    /**
     * 阻塞
     */
    public static final String STATUS_BLOCK="BLOCK";
    /**
     * 闲逛
     */
    public static final String STATUS_HANG_OUT="HANG_OUT";
    /**
     * 等待IO
     */
    public static final int EVENT_WAIT_DEVICE=0;
    /**
     * 阻塞IO
     */
    public static final int EVENT_USING_DEVICE=1;
    /**
     * 事件记录
     */
    public static final int EVENT_NOTING=2;
    /**
     * id生成
     */
    private static  int idGenerator =0;
    /**
     * pid
     */
    private int PID;
    /**
     * 状态
     */
    private String status;
    /**
     * 优先级
     */
    private int priority;
    //时间片余量
    private int counter;
    //IP寄存器
    private int IP;
    //寄存器数据
    private int AX;
    private int BX;
    private int CX;
    private int DX;
    //指向进程的程序和数据在内存中的首地址
    private int memStart;
    //指向进程的程序和数据在内存中的尾地址
    private int memEnd;
    //事件
    private int event;
    /**
     * 指定优先级
     *
     * @param priority 优先级
     */
    public PCB(int priority){
        idGenerator++;
        PID=idGenerator;
        this.priority= priority;
        counter = priority;
    }
    /**
     * 优先级随机
     */
    public PCB(){
        this(new Random().nextInt(64));
    }


    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }


    public int getAX() {
        return AX;
    }

    public void setAX(int AX) {
        this.AX = AX;
    }

    public int getMemStart() {
        return memStart;
    }

    public void setMemStart(int memStart) {
        this.memStart = memStart;
    }

    public int getMemEnd() {
        return memEnd;
    }

    public void setMemEnd(int memEnd) {
        this.memEnd = memEnd;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }
    public int getIP() {
        return IP;
    }

    public void setIP(int IP) {
        this.IP = IP;
    }

    public int getBX() {
        return BX;
    }

    public void setBX(int BX) {
        this.BX = BX;
    }

    public int getCX() {
        return CX;
    }

    public void setCX(int CX) {
        this.CX = CX;
    }

    public int getDX() {
        return DX;
    }

    public void setDX(int DX) {
        this.DX = DX;
    }

    @Override
    public int compareTo(Object o) {
        PCB pcb = (PCB)o;
        return this.counter - pcb.counter;
    }


}
