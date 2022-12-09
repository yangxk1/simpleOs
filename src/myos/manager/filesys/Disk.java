package myos.manager.filesys;

import myos.constant.OsConstant;

import java.io.*;

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
        //| 引导块 | 超级块 | i节点位图 | 盘块位图 | i节点 | 数据块 |
        //+---------------------------------------------------+
        //|       |       |         |         |       |       |
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
                    //前三个盘块不可用
                    bytes[0] = -1;
                    bytes[1] = -1;
                    bytes[2] = -1;
                }
                //写入根目录
                if (i == 2) {
                    //根目录名为root
                    bytes[0] = 'r';
                    bytes[1] = 'o';
                    bytes[2] = 'o';
                    bytes[3] = 't';
                    bytes[4] = 0;
                    //目录属性
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
}
