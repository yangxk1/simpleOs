package myos.manager.process;


import myos.Software;

/**
 * 系统时钟
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
    //系统时钟
    private  long systemTime;
    /**
     * 开始时间
     */
    private long beginTime;
    //当前进程剩下的运行时间
    private long restTime;
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
                //时间片到了
                if (restTime==0){
              //      System.out.println("时间片用完了");
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
//    public String getSystemTime() {
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(beginTime+systemTime));
//    }

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
