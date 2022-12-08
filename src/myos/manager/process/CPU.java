package myos.manager.process;

import myos.Software;
import myos.manager.device.DeviceManager;
import myos.manager.device.DeviceRequest;
import myos.manager.memory.Memory;
import myos.manager.memory.SubArea;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * cpu
 *
 * @author WTDYang
 * @date 2022/12/04
 */
@SuppressWarnings("all")
public class CPU implements Runnable {
    static ReentrantLock lock = new ReentrantLock();
    /**
     * 指令寄存器
     */
    private int IR;
    private int AX; //0
    private int BX; //1
    private int CX; //2
    private int DX; //3
    /**
     * 程序计数器
     */
    private int IP;

    private int nextIR;
    /**
     * 操作码
     */
    private int OP;
    /**
     * 操作数
     */
    private int DR;
    /**
     * 设备状态寄存器
     */
    private int SR;
    private String result="CLK";
    private String process="";

    private static final int NOP = 0x0000;


    private Memory memory;
    private DeviceManager deviceManager;
    public CPU() {
        this.memory = Software.memory;
        deviceManager=new DeviceManager(this);
    }

    /**
     * 初始化CPU
     */
    public void init(){
        IR=0;
        AX=0;
        BX=0;
        CX=0;
        DX=0;
        IP=0;
        deviceManager.init();
    }

    /**
     * 取指令
     * 取指
     */
    public void fetchInstruction() {
        if (memory.getRunningPCB()==memory.getHangOutPCB()){
            IR=NOP;//NOP不执行
        }else{
            byte[] userArea = memory.getUserArea();
            IR = userArea[IP];
            IP++;
        }
    }

    /**
     * 译码
     */
    public void identifyInstruction() {
        //移位
        OP = (IR>>4)&0x0f;
        DR = (IR>>2)&0x03;
        SR = IR & 0x03;

        if(OP == 5)
        {
            byte[] userArea = memory.getUserArea();
            nextIR = userArea[IP];
            IP++;
        }
    }

    /**
     * 执行指令
     */
    public void execute() {
        result ="CLK";  //无操作显示时钟闲逛
        process="";
        if(IR !=0)
        {
            result ="";
            switch (OP) {
                case 1:switch (DR){  //ADD 自增一
                    case 0:AX++;result +="INC AX";
                        process = "AX="+AX;
                        break;
                    case 1:BX++;result +="INC BX";
                        process = "BX="+BX;
                        break;
                    case 2:CX++;result +="INC CX";
                        process = "CX=" +CX;
                        break;
                    case 3:DX++;result +="INC DX";
                        process = "DX=" +DX;
                        break;
                    }
                break;
                case 2:switch (DR){ //DEC 自减一
                    case 0:AX--;result +="DEC AX";
                        process = "AX="+AX;
                        break;
                    case 1:BX--;result +="DEC BX";
                        process = "BX="+BX;
                        break;
                    case 2:CX--;result +="DEC CX";
                        process = "CX=" +CX;
                        break;
                    case 3:DX--;result +="DEC DX";
                        process = "DX=" +DX;
                        break;
                    }
                    break;
                case 3:              //! ? ?； 第一个？表示阻塞原因A,B(I/O申请），第二个？为一位数，表示阻塞时间（cpu循环次数）
                    String deviceName=null;
                    switch (DR){
                        case 0:deviceName="A";break;
                        case 1:deviceName="B";break;
                        case 2:deviceName="C";break;
                    }
                    result +="use device:"+DR+":"+deviceName+";Time:"+SR*10;
                    DeviceRequest deviceRequest=new DeviceRequest();
                    deviceRequest.setDeviceName(deviceName);
                    deviceRequest.setWorkTime(SR*10000);
                    deviceRequest.setPcb(memory.getRunningPCB());
                    deviceManager.requestDevice(deviceRequest);
                    //阻塞进程
                    block();
                    //重新调度
                    dispatch();
                    break;
                case 4:result += "END";//END 表示程序结束，其中包括文件路径名和x的值（软中断方式处理）
                        destroy();
                        dispatch();

                    break;
                case 5:switch (DR){ //MOV 给x赋值一位数
                    case 0:AX = nextIR;result +="MOV AX,"+nextIR;
                        process = "AX="+AX;
                        break;
                    case 1:BX = nextIR;result +="MOV BX,"+nextIR;
                        process = "BX="+BX;
                        break;
                    case 2:CX = nextIR;result +="MOV CX,"+nextIR;
                        process = "CX="+ CX;
                        break;
                    case 3:DX = nextIR;result +="MOV DX,"+nextIR;
                        process = "DX="+ DX;
                        break;
                }
                    break;
                default:
                    throw new RuntimeException("未知指令");
            }
        }

    }
    /**
     * 进程调度,将进程从就绪态恢复到运行态
     */
    public void dispatch() {
        PCB pcb1= memory.getRunningPCB();//当前运行的进程

        //调度算法
        PCB pcb2= null;
        try {
            pcb2 = schedule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        memory.setRunningPCB(pcb2);
            pcb2.setStatus(PCB.STATUS_RUN);
            //保存现场
            saveContext(pcb1);
            //恢复现场
            recoveryContext(pcb2);
            System.out.println("NEXT PID:"+pcb2.getPID());
    }

    /**
     * CPU调度算法
     * 优先级调度算法+时间片轮询
     * Linux0.11内核改造
     */
    PCB schedule() throws Exception {
        //取出队首，同时删除队首
        PCB next=memory.getWaitPCB().poll();
        if (next==null){
            //没有就绪进程了
            next=memory.getRunningPCB();
        }
        //如果第一个就绪进程是闲逛进程且还有其他的就绪进程
        if (next==memory.getHangOutPCB()&&memory.getWaitPCB().size()>0){
            //再次取头
            PCB pcb = memory.getWaitPCB().poll();
            //首至尾
            memory.getWaitPCB().offer(next);
            next = pcb;
        }
        //修改counter 让阻塞态的counter增加，也就是提高其优先级，当阻塞结束时就可以优先被调用
        if(next.getCounter() == 0){
            Queue<PCB> blockPCB = memory.getBlockPCB();
            blockPCB.forEach((item)->{
                item.setCounter(
                        item.getCounter() >> 1+ item.getPriority()
                );
            });
        }
        return next;
    }

    /**
     * 进程撤销
     */
    public void destroy(){
        PCB pcb=memory.getRunningPCB();
        System.out.println("进程"+pcb.getPID()+"运行结束,撤销进程");
        /*回收进程所占内存*/
        SubArea subArea=null;
        List<SubArea> subAreas=memory.getSubAreas();
        for (SubArea s:subAreas){
            if (s.getTaskNo()==pcb.getPID()){
                subArea=s;
                break;
            }
        }
        subArea.setStatus(SubArea.STATUS_FREE);
        int index=subAreas.indexOf(subArea);
        //如果不是第一个，判断上一个分区是否为空闲
        if (index>0){
            SubArea preSubArea=subAreas.get(index-1);
            if(preSubArea.getStatus()==SubArea.STATUS_FREE) {
                preSubArea.setSize(preSubArea.getSize() + subArea.getSize());
                subAreas.remove(subArea);
                subArea = preSubArea;
            }
        }
        //如果不是最后一个，判断下一个分区是否空闲
           if (index<subAreas.size()-1){
            SubArea nextSubArea=subAreas.get(index+1);
            if (nextSubArea.getStatus()==SubArea.STATUS_FREE) {
                nextSubArea.setSize(nextSubArea.getSize() + subArea.getSize());
                nextSubArea.setStartAdd(subArea.getStartAdd());
                subAreas.remove(subArea);
            }
        }


    }

    /**
     * 将运行进程转换为就绪态
     */
    public void toReady(){
        PCB pcb=memory.getRunningPCB();
//        System.out.println("进程"+pcb.getPID()+"被放入就绪队列");
        memory.getWaitPCB().offer(pcb);
        pcb.setStatus(PCB.STATUS_WAIT);
    }
    /**
     * 将运行进程转换为阻塞态
     */
    public void block(){
        PCB pcb=memory.getRunningPCB();
        //修改进程状态
        pcb.setStatus(PCB.STATUS_BLOCK);
        //将进程链入对应的阻塞队列，然后转向进程调度
        memory.getBlockPCB().add(pcb);
    }

    /**
     * 进程唤醒
     */
    public void awake(PCB pcb){

        System.out.println("唤醒进程"+pcb.getPID());
        //将进程从阻塞队列中调入到就绪队列
        lock.lock();
        try {
            pcb.setStatus(PCB.STATUS_WAIT);
            pcb.setEvent(PCB.EVENT_NOTING);
            memory.getBlockPCB().remove(pcb);
            memory.getWaitPCB().add(pcb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 保存上下文
     * @param pcb
     */
    private void  saveContext(PCB pcb){
        pcb.setIP(IP);
        pcb.setAX(this.AX);
        pcb.setBX(this.BX);
        pcb.setCX(this.CX);
        pcb.setDX(this.DX);
    }

    /**
     * 恢复现场
     */
    private void recoveryContext(PCB pcb){
        pcb.setStatus(PCB.STATUS_RUN);
        this.AX=pcb.getAX();
        this.BX=pcb.getBX();
        this.DX=pcb.getDX();
        this.CX=pcb.getCX();
        this.IP=pcb.getIP();
    }

    @Override
    public void run() {
        while (Software.launched) {
            try {
                Thread.sleep(Clock.TIMESLICE_UNIT);
            } catch (InterruptedException e) {
                return;
            }
            lock.lock();
            try {
                //取指
                fetchInstruction();
                //译码
                identifyInstruction();
                //执行
                execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 得到结果
     *
     * @return {@link String}
     */
    public String getResult()
    {
        String temp;
        lock.lock();
        try {
            temp=result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
        return temp;
    }
    public String getProcess(){
        if(!"".equals(process))
        return process+"\n";
        return "";
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }
    /**
     * 得到指令
     *
     * @param instruction 指令
     * @return {@link byte[]}
     */
    public byte[] getInstruction(String[] instruction) {
        ArrayList<Byte> ins = new ArrayList<>();
        for (int i = 0; i < instruction.length; i++) {
            String[] str = instruction[i].split("[\\s|,]");
            byte first;
            byte second = (byte) 0;
            if (str.length > 1) {
                if (str[1].contains("a")) {
                    second = 0;
                } else if (str[1].contains("b")) {
                    second = 4;
                } else if (str[1].contains("c")) {
                    second = 8;
                } else {
                    second = 12;
                }
            }
            if (str[0].contains("mov")) {
                first = (byte) 80;
                ins.add((byte) (first + second));
                ins.add(Byte.valueOf(str[2]));
            } else if (str[0].contains("inc")) {
                first = (byte) 16;
                ins.add((byte) (first + second));
            } else if (str[0].contains("dec")) {
                first = (byte) 32;
                ins.add((byte) (first + second));
            } else if (str[0].contains("!")) {
                first = (byte) 48;
                ins.add((byte) (first + second + Byte.valueOf(str[2])));
            } else if (str[0].contains("end")) {
                ins.add((byte) 64);
            }
        }
        byte[] instruct = new byte[ins.size()];
        for (int i = 0; i < instruct.length; i++) {
            instruct[i] = ins.get(i);
        }
        return instruct;
    }

}

