package myos.manager.filesys;

import myos.constant.OsConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
}
