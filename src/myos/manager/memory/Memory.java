package myos.manager.memory;

import myos.constant.OsConstant;
import myos.manager.process.PCB;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * 内存
 *
 * @author WTDYang
 * @date 2022/12/07
 */
@SuppressWarnings("all")
public class Memory {
    /**
     * 内存分配表(虚拟内存)
     */
    private List<SubArea> subAreas;
    /*    //空闲进程控制块
    private Queue<PCB> freePCB;*/
    //就绪进程控制块
    private Queue<PCB> waitPCB;
    /**
     * 阻塞进程控制块
     */
    private Queue<PCB> blockPCB;
    /**
     * 运行进程
     */
    private  PCB runningPCB;
    /**
     * 闲逛进程
     */
    private PCB hangOutPCB;
    /**
     * 用户区内存(物理内存)
     */
    private byte[] userArea;
    public Memory() {
        subAreas =Collections.synchronizedList(new LinkedList<>());
        waitPCB = new PriorityBlockingQueue<>();
        blockPCB = new PriorityBlockingQueue<>();
        //闲逛进程的优先值最低
        hangOutPCB=new PCB(0);
        //初始化内存
        userArea = new byte[OsConstant.USER_AREA_SIZE];
    }
    public void init(){
        Arrays.fill(userArea,(byte) 0);
        waitPCB.removeAll(waitPCB);
        blockPCB.removeAll(blockPCB);
        hangOutPCB.setStatus(PCB.STATUS_RUN);
        runningPCB=hangOutPCB;
        subAreas.removeAll(subAreas);
        SubArea subArea = new SubArea();
        subArea.setSize(OsConstant.USER_AREA_SIZE);
        subArea.setStartAdd(0);
        subArea.setStatus(SubArea.STATUS_FREE);
        subAreas.add(subArea);
    }
    public List<SubArea> getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(List<SubArea> subAreas) {
        this.subAreas = subAreas;
    }


    public Queue<PCB> getWaitPCB() {
        return waitPCB;
    }

    public void setWaitPCB(Queue<PCB> waitPCB) {
        this.waitPCB = waitPCB;
    }

    public Queue<PCB> getBlockPCB() {
        return blockPCB;
    }

    public void setBlockPCB(Queue<PCB> blockPCB) {
        this.blockPCB = blockPCB;
    }

    public byte[] getUserArea() {
        return userArea;
    }

    public void setUserArea(byte[] userArea) {
        this.userArea = userArea;
    }

    public PCB getRunningPCB() {
        return runningPCB;
    }

    public void setRunningPCB(PCB runningPCB) {
        this.runningPCB = runningPCB;
    }

    public PCB getHangOutPCB() {
        return hangOutPCB;
    }

    public List<PCB> getAllPCB() {
        List<PCB> allPCB=new ArrayList<>(10);
        allPCB.add(runningPCB);
        allPCB.addAll(blockPCB);
        allPCB.addAll(waitPCB);
        return allPCB;
    }

    /**
     * 申请内存
     * 首次适配法 第一个空闲位置
     * @param newPCB  申请程序的PCB
     * @param program 程序段
     * @param data    数据段
     * @return {@link SubArea}
     * @throws Exception 异常
     */
    public SubArea requestMemory(PCB newPCB, byte[] program, byte[] data) throws Exception {
        SubArea subArea = null;

        //遍历内存分配表
        ListIterator<SubArea> it=this.getSubAreas().listIterator();
        while(it.hasNext()){
            SubArea s = it.next();
            if (s.getStatus() == SubArea.STATUS_FREE
                    && s.getSize() >= program.length) {
                subArea = s;
                break;
            }
        }
        if (subArea==null) {
            throw new Exception("内存不足");
        }
        //如果区域过大，分出一块新的空闲区成两块
        if (subArea.getSize() > program.length){
            //新的空闲区域
            SubArea newSubArea=new SubArea();
            //FREE
            newSubArea.setStatus(SubArea.STATUS_FREE);
            //内存块多出来的数量
            int newSubAreaSize=subArea.getSize()-program.length;
            newSubArea.setSize(newSubAreaSize);
            //新内存块的起始地址为原来的起始地址+本次程序占用的大小
            newSubArea.setStartAdd(subArea.getStartAdd()+program.length);
            it.add(newSubArea);
        }

        //将占用内存置为忙碌
        subArea.setSize(program.length);
        subArea.setTaskNo(newPCB.getPID());
        subArea.setStatus(SubArea.STATUS_BUSY);
        return subArea;
    }

    /**
     * 回收内存
     * 寻找空闲的上下邻
     * 合并空闲的上下邻
     * @param pcb 撤销程序的PCB
     */
    public void ReclaimMemory(PCB pcb){
        SubArea subArea=null;
        List<SubArea> subAreas=this.getSubAreas();
        //寻找内存位置
        for (SubArea s:subAreas){
            if (s.getTaskNo()==pcb.getPID()){
                subArea=s;
                break;
            }
        }
        //释放内存
        subArea.setStatus(SubArea.STATUS_FREE);
        int index=subAreas.indexOf(subArea);

        //如果不是第一个，判断上一个分区是否为空闲
        if (index>0){
            SubArea preSubArea=subAreas.get(index-1);
            if(preSubArea.getStatus()==SubArea.STATUS_FREE) {
                mergeMemory(subArea,preSubArea);
            }
        }
        //如果不是最后一个，判断下一个分区是否空闲
        if (index<subAreas.size()-1){
            SubArea nextSubArea=subAreas.get(index+1);
            if (nextSubArea.getStatus()==SubArea.STATUS_FREE) {
                mergeMemory(subArea,nextSubArea);
            }
        }
    }

    /**
     * 合并内存
     *
     * @param thisArea     占用区域
     * @param nextSubArea  相邻区域
     */
    private void mergeMemory(SubArea thisArea,SubArea nextSubArea){
        //把内存都给下一个
        nextSubArea.setSize(nextSubArea.getSize() + thisArea.getSize());
        nextSubArea.setStartAdd(thisArea.getStartAdd());
        //把自己删除掉
        subAreas.remove(thisArea);
    }


}
