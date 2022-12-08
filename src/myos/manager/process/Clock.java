package myos.manager.process;


import myos.Software;

/**
 * 时钟
 * 系统时钟
 *
 * @author WTDYang
 * @date 2022/12/03
 */
public class Clock implements Runnable {

    /**
     * 时间片长度
     */
    private static final long TIMESLICE_LENGTH=6;

    /**
     * 时间片单元（1000ms）
     */
    public static final long TIMESLICE_UNIT=1000;
    /**
     * 系统时钟
     */
    private  long systemTime;
    /**
     * 开始时间
     */
    private long beginTime;
    /**
     * 当前进程剩下的运行时间
     */
    private long restTime;
    /**
     * cpu
     */
    private CPU cpu;
    public Clock(){
        this.cpu= Software.cpu;
        init();
    }

    /**
     * 初始化时钟
     */
    public void init(){
        beginTime = System.currentTimeMillis();
        systemTime=0;
        restTime=TIMESLICE_LENGTH;
    }
    @Override
    public void run() {
        while(Software.launched) {
            try {
                Thread.sleep(TIMESLICE_UNIT);
                systemTime+=TIMESLICE_UNIT/1000;
                restTime=(restTime+TIMESLICE_LENGTH-TIMESLICE_UNIT/1000)%TIMESLICE_LENGTH;
                //时间耗尽
                if (restTime==0){
                    CPU.lock.lock();
                    cpu.toReady();
                    cpu.dispatch();
                    CPU.lock.unlock();
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public long getSystemTime() {
        return  systemTime;
    }

    public long getRestTime() {
        return restTime;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }
}
