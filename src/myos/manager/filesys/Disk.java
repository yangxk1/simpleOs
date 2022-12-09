package myos.manager.filesys;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import myos.constant.OsConstant;
import myos.controller.MainController;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;
import static myos.constant.OsConstant.DISK_BLOCK_QUNTITY;
import static myos.constant.OsConstant.DISK_BLOCK_SIZE;

/**
 * 磁盘
 *
 * @author WTDYang
 * @date 2022/12/08
 */
@SuppressWarnings("all")
public class Disk {

    /**
     * 磁盘文件
     */
    RandomAccessFile disk;
    public Disk(RandomAccessFile disk){
        this.disk = disk;
    }

    /**
     * 随机寻址
     *
     * @param pos pos
     * @throws IOException ioexception
     */
    public void seek(long pos) throws IOException {
        disk.seek(pos);
    }

    /**
     * 写字节
     *
     * @param v v
     * @throws IOException ioexception
     */
    public final void writeByte(int v )throws java.io.IOException{
        disk.writeByte(v);
    }

    /**
     * 写入数据
     *
     * @param b   the data
     * @param off the start offset in the data
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    public void write(byte[] b, int off, int len ) throws java.io.IOException {
        disk.write(b,off,len);
    }
    public void write(int b )throws java.io.IOException{
        disk.write(b);
    }
    /**
     * 读取字节
     *
     * @return byte 读取到的字节
     * @throws IOException ioexception
     */
    public final byte readByte()throws java.io.IOException{
        return disk.readByte();
    }

    /**
     * 读
     *
     * @param b   the buffer into which the data is read
     * @param off the start offset in array b at which the data is written
     * @param len the maximum number of bytes read
     * @return int the total number of bytes read into the buffer, or -1 if there is no more data because the end of the file has been reached.
     * @throws IOException ioexception
     */
    public int read(byte[] b,int off,int len )throws java.io.IOException  {
        return disk.read(b,off,len);
    }



    public RandomAccessFile getDisk() {
        return disk;
    }

    public void setDisk(RandomAccessFile disk) {
        this.disk = disk;
    }

    public static void init() {
        File file = new File(OsConstant.DISK_FILE);
        //+---------------------------------------------------+
        //|      位图    |   root  |          可用分区          |
        //+---------------------------------------------------+
        //|   2块128字节 |    1块   |           125块           |
        //+---------------------------------------------------+
        FileOutputStream fout = null;
        //判断模拟磁盘是否已经创建
        if (file.exists()){
            System.out.println("模拟磁盘已存在，无需重新创建");
            return;
        }
        try {
            fout = new FileOutputStream(file);
            byte[] bytes;
            for (int i = 0; i < DISK_BLOCK_QUNTITY; i++) {
                bytes = new byte[DISK_BLOCK_SIZE];
                //写入初始文件分配表
                if (i == 0) {
                    //第一、二个盘块存储位图， 第三个盘块存储root文件
                    bytes[0] = -1;
                    bytes[1] = -1;
                    bytes[2] = -1;
                }
                //第三个盘块开始写入root根目录
                if (i == 2) {
                    //根目录名为root
                    bytes[0] = 'r';
                    bytes[1] = 'o';
                    bytes[2] = 'o';
                    bytes[3] = 't';
                    bytes[4] = 0;
                    //目录属性
                    //可执文件为00010000
                    //目录文件为00001000
                    bytes[5] = Byte.parseByte("00001000", 2);
                    //起始盘号
                    bytes[6] = -1;
                    //保留一字节未使用
                    bytes[7] = 0;
                }
                fout.write(bytes);
            }
        } catch (FileNotFoundException e) {
            System.out.println("磁盘文件异常");
            e.printStackTrace();
            exit(0);
        } catch (IOException e) {
            System.out.println("磁盘IO异常");
            e.printStackTrace();
            exit(0);
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
    }

    /**
     * 获取磁盘使用情况位图
     *
     * @return {@link byte[]}
     * @throws IOException ioexception
     */
    public byte[] getBitMaps() throws IOException {
        //物理地址置0
        disk.seek(0);
        byte[] buffer = new byte[OsConstant.DISK_BLOCK_QUNTITY];
        //直接扫描全部全部物理块
        disk.read(buffer, 0, buffer.length);
        return buffer;
    }
    /**
     * 读取目录项
     *
     * @param blockPos
     * @return
     * @throws IOException
     */
    public Catalog readCatalog(int blockPos) {
        Catalog catalog = null;
        try {

            disk.seek(blockPos * OsConstant.DISK_BLOCK_SIZE);
            byte[] buffer = new byte[8];
            disk.read(buffer, 0, buffer.length);
            catalog = new Catalog(buffer);
            catalog.setCatalogBlock(blockPos);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return catalog;
    }
    /**
     * 写入目录
     *
     * @param catalog
     * @throws IOException
     */
    public void writeCatalog(Catalog catalog) throws IOException {
        disk.seek(catalog.getCatalogBlock() * OsConstant.DISK_BLOCK_SIZE);
        disk.write(catalog.getBytes(), 0, catalog.getBytes().length);
    }

    /**
     * 格式化硬盘（强制）
     * @throws Exception
     */
    public void format() throws Exception {
        Catalog root=readCatalog(2);
        for (Catalog catalog:root.list()){
            if(!catalog.isDirectory()) {
                delete(root,catalog);
            } else {
                rmdir(root,catalog);
            }
        }
    }
    /**
     * 删除目录硬件实现
     * 递归删除
     * @param parent 要删除目录的父目录
     * @param catalog 要删除的目录
     */
    public void rmdir(Catalog parent,Catalog catalog) throws Exception {
        //如果是文件或空文件夹，则直接删除
        if (!catalog.isDirectory()||catalog.isBlank()){
            delete(parent,catalog);
            return;
        }
        //先删除所有子目录
        for (Catalog c:catalog.list()){
            rmdir(catalog,c);
        }
        catalog.setBlank(true);
        //再删除本目录
        rmdir(parent,catalog);
    }

    /**
     * 删除文件硬件实现
     *
     * @param parent 父
     * @param c      c
     * @throws Exception 异常
     */
    public void delete(Catalog parent, Catalog c) throws Exception {
        if (parent.getStartBlock() == c.getCatalogBlock()) {
            parent.setStartBlock(getNextBlock(c.getCatalogBlock()));
            writeCatalog(parent);
        } else {
            int  nextBlock = parent.getStartBlock();
            int  pre = nextBlock;
            while (nextBlock != c.getCatalogBlock()) {
                pre = nextBlock;
                nextBlock = getNextBlock(pre);
            }
            setNextBlock(pre, getNextBlock(c.getCatalogBlock()));
        }
        //删除目录项
        setNextBlock(c.getCatalogBlock(), 0);
        System.out.println("删除文件"+c.getName()+"成功");
    }

    /**
     * 获取分配表指向的下一个磁盘块号
     *
     * @param i 当前位置
     * @return int
     * @throws IOException ioexception
     */
    public int getNextBlock(int i) throws IOException {
        disk.seek(i);
        return disk.readByte();
    }
    /**
     * 修改文件分配表指向的下一个磁盘块
     *
     * @param i
     * @param nextBlock
     * @throws IOException
     */
    public void setNextBlock(int i, int nextBlock) throws IOException {
        disk.seek(i);
        disk.writeByte(nextBlock);
    }

    /**
     * 找到目录内容所在的最后一个磁盘块
     *
     * @param i 我
     * @return int
     * @throws IOException ioexception
     */
    public int getLastBlock(int i) throws IOException {
        int nextBlock = getNextBlock(i);
        if (nextBlock != -1) {
            return getLastBlock(nextBlock);
        }
        return i;
    }
    /**
     * 查找第一个可用磁盘块
     *
     * @return
     */
    public int firstFreeBlock() throws IOException {
        int nextBlock;
        for (int i = 3; i < OsConstant.DISK_BLOCK_QUNTITY; i++) {
            //0表示可用
            nextBlock = getNextBlock(i);
            if (nextBlock == 0) {
                return i;
            }
        }
        return -1;
    }
    /**
     * 从开始磁盘块查找，判断是否存在同名目录
     *
     * @param fileName
     * @param startBlock
     * @return
     * @throws IOException
     */
    public boolean existsFile(String fileName, int startBlock) throws IOException {
        int nextBlock = startBlock;
        while (nextBlock != -1) {
            Catalog catalog = readCatalog(nextBlock);
            if (catalog.getName().equals(fileName)) {
                return true;
            }
            nextBlock = getNextBlock(nextBlock);
        }
        return false;
    }

    /**
     * 查找文件所在磁盘块号
     *
     * @param filePath
     * @param startBlockPos 从哪一个盘块开始搜索根目录
     */
    public int getCatalogBlock(String filePath, int startBlockPos) throws Exception {
        int index = filePath.indexOf('/');
        String rootName, sonPath;
        //该路径最上层的目录名
        if (index != -1) {
            rootName = filePath.substring(0, index);
            //子路径
            sonPath = filePath.substring(index + 1);
        } else {
            rootName = filePath;
            sonPath = "";
        }
        byte nextBlock = (byte) startBlockPos;
        do {
            Catalog catalog = readCatalog(nextBlock);
            if (catalog.getName().equals(rootName)) {
                //找到最下层了就返回
                if ("".equals(sonPath)) {
                    return nextBlock;
                }
                return getCatalogBlock(sonPath, catalog.getStartBlock());
            }
            disk.seek(nextBlock);
            nextBlock = disk.readByte();
        } while (nextBlock != -1);

        throw new Exception("未找到文件夹" + rootName);
    }
    public  byte[] read(OpenedFile openedFile, int length) throws Exception {
        int readByte = 0;
        //1.文件内容不够长
        //2.遇到结束符
        //3.跨越磁盘块
        byte[] buffer = new byte[1024];
        Pointer p = openedFile.getReadPointer();
        byte temp;
        while (p.getBlockNo() != -1 && readByte != length) {
            //读完一个磁盘块
            if (p.getAddress() == OsConstant.DISK_BLOCK_SIZE) {
                p.setBlockNo(getNextBlock(p.getBlockNo()));
                p.setAddress(0);
            }
            disk.seek(p.getBlockNo() * OsConstant.DISK_BLOCK_SIZE + p.getAddress());
            temp =disk.readByte();
            //遇到结束符停止读取
            if (temp == '#') {
                break;
            }
            buffer[readByte] = temp;
            p.setAddress(p.getAddress() + 1);
            readByte++;
        }
        byte[] content = Arrays.copyOf(buffer, readByte);
        return content;
    }

}
