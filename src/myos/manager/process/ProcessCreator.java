package myos.manager.process;

import myos.Software;
import myos.constant.OsConstant;
import myos.manager.memory.Memory;
import myos.manager.memory.SubArea;

import java.util.ListIterator;

import static myos.manager.process.CPU.lock;


/**
 * 创建进程
 *
 * @author WTDYang
 * @date 2022/12/07
 */
public class ProcessCreator {
    private Memory memory;
    private CPU cpu;
    public ProcessCreator( ){
        this.memory= Software.memory;
        this.cpu= Software.cpu;
    }
    //TODO 内存管理

    /**
     * 创建
     * 为打开的可执行文件创建进程
     *
     * @param program 程序
     * @param data    数据
     * @throws Exception 异常
     */
    public void create(byte[] program,byte[] data) throws Exception {

//        查看是否超出运行进程的最大个数
        int pcbSize=memory.getAllPCB().size();
        if (pcbSize>=OsConstant.PROCESS_MAX) {
            throw  new Exception("当前运行的进程过多，请关闭其他程序后再试");
        }
//        申请内存
        SubArea subArea=null;
        //首次适配法 第一个空闲位置
        ListIterator<SubArea> it=memory.getSubAreas().listIterator();
        while(it.hasNext()){
            SubArea s=it.next();
            if (s.getStatus()==SubArea.STATUS_FREE&&s.getSize()>=program.length) {
                subArea = s;
                break;
            }
        }
        if (subArea==null) {
            throw new Exception("内存不足");
        }
        PCB newPCB=new PCB();

        //如果区域过大，分出一块新的空闲区成两块
       if (subArea.getSize()>program.length){
           int newSubAreaSize=subArea.getSize()-program.length;
           subArea.setSize(program.length);
           subArea.setTaskNo(newPCB.getPID());
           subArea.setStatus(SubArea.STATUS_BUSY);
           SubArea newSubArea=new SubArea();
           //新的空闲区域
           newSubArea.setStatus(SubArea.STATUS_FREE);
           newSubArea.setSize(newSubAreaSize);
           newSubArea.setStartAdd(subArea.getStartAdd()+subArea.getSize());
           it.add(newSubArea);
       }else {
           subArea.setSize(program.length);
           subArea.setTaskNo(newPCB.getPID());
           subArea.setStatus(SubArea.STATUS_BUSY);
       }

       //代码段写入内存
       byte[] userArea=memory.getUserArea();
       int lastAdd = subArea.getStartAdd()+subArea.getSize();
       for (int i=subArea.getStartAdd(),j=0;i<lastAdd;i++,j++){
           userArea[i]=program[j];
       }


        //初始化进程控制块
        newPCB.setMemStart(subArea.getStartAdd());
        newPCB.setMemEnd(program.length);
        newPCB.setCounter(subArea.getStartAdd());
        newPCB.setStatus(PCB.STATUS_WAIT);
        //申请完成
        memory.getWaitPCB().offer(newPCB);


        //判断当前是否有实际运行进程，没有的则申请进程调度
        if (memory.getRunningPCB()==null||memory.getRunningPCB()==memory.getHangOutPCB()) {
            lock.lock();
            cpu.toReady();
            cpu.dispatch();
            lock.unlock();
        }

    }

}
