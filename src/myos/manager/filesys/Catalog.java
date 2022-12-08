package myos.manager.filesys;

import myos.Software;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录FCB
 *
 * @author WTDYang
 * @date 2022/12/07
 */
@SuppressWarnings("all")
public class Catalog {
    private static final int NAME_LENGTH = 5;
    private static final int PROPERTY_LOCATION = 5;
    private static final int START_BLOCK_LOCATION = 6;
    private static final int FILE_LENGTH_LOCATION = 7;
    private static final int EXE_BITE = 4;
    private static final int DIR_BITE = 3;

    /**
     * 文件名
     */
    private String name;
    /**
     * 目录项占用空间
     */
    private byte[] bytes;
    /**
     * 文件属性
     */
    private int property;
    /**
     * 文件内容起始盘号
     */
    private int startBlock;
    /**
     * 文件长度，单位为盘块，如果是目录则为0
     */
    private int fileLength;
    /**
     * 是否可执行
     */
    public boolean executable;
    /**
     * 是否是目录
     */
    private boolean isDirectory;
    /**
     * 目录所在磁盘块号
     */
    private int catalogBlock;
    /**
     * 是否为空
     */
    private boolean isBlank;


    /**
     * 目录
     *     //+---------------------------------+
     *     //| 文件名 | 属性 | 起始盘号 | 文件长度 |
     *     //+---------------------------------+
     *     //|   5   |  1  |    1    |    1    |
     *     //+---------------------------------+
     * @param bytes 硬件读取到的字节流
     */
    public Catalog(byte[] bytes){
        this.bytes=bytes;
        //文件名(0-4)
        this.name=new String(bytes,0,NAME_LENGTH);
        //文件属性(5)
        setProperty(bytes[PROPERTY_LOCATION]);
        //可执文件为00010000
        //目录文件为00001000
        if (property >> EXE_BITE == 1){
            executable=true;
        }else if (property >> DIR_BITE == 1){
            isDirectory=true;
        }
        //起始盘号(6)
        this.startBlock=bytes[START_BLOCK_LOCATION];
        //其实地址为-1,说明存储的数据为空
        isBlank= startBlock == -1;
        //文件长度(7)
        this.fileLength=bytes[FILE_LENGTH_LOCATION];

    }

    /**
     * 文件系统调用新建文件
     *     //+---------------------------------+
     *     //| 文件名 | 属性 | 起始盘号 | 文件长度 |
     *     //+---------------------------------+
     *     //|   5   |  1  |    1    |    1    |
     *     //+---------------------------------+
     * @param fileName 文件名称
     * @param property 属性
     * @throws Exception 异常
     */
    public Catalog(String fileName,int property) throws Exception {
        //分配空间
        this.bytes=new byte[8];
        //新建文件为空
        this.isBlank=true;
        //起始地址为-1
        this.setStartBlock(-1);
        //文件长度为0
        this.setFileLength(0);
        //设置文件名
        this.setName(fileName);
        //文件属性
        this.setProperty(property);
        //是否为目录
        isDirectory = property >> DIR_BITE == 1;
    }

    public List<Catalog> list() throws IOException {
        List<Catalog> catalogs=new ArrayList<>();
        Catalog catalog= Software.fileOperator.readCatalog(catalogBlock);
        int nextBlock=catalog.getStartBlock();
        while(nextBlock!=-1){
            Catalog c= Software.fileOperator.readCatalog(nextBlock);
            catalogs.add(c);
            nextBlock= Software.fileOperator.getNextBlock(nextBlock);
        }
        return  catalogs;
    }

    public String getName() {
        return name.trim();
    }

    public void setBlank(boolean blank) {
        isBlank = blank;
    }
    public void setName(String name) throws Exception {
        this.name = name;
        byte[] nameBytes=name.getBytes();
        if (nameBytes.length>NAME_LENGTH) {
            throw new Exception("文件名过长！");
        }
        System.arraycopy(nameBytes, 0, bytes, 0, nameBytes.length);
    }

    /**
     * 获取属性
     *
     * @return int
     */
    public int getProperty() {
        return property;
    }

    /**
     * 设置属性
     *
     * @param property 财产
     */
    public void setProperty(int property) {
        this.property = property;
        bytes[5]=(byte)property;
        if (property>>4==1){
            executable=true;
        }else{
            executable=false;
        }
         if (property>>3==1){
            isDirectory=true;
        }else {
             isDirectory=false;
         }
    }

    public int getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(int startBlock) {
        this.startBlock = startBlock;
        bytes[6]=(byte)startBlock;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
        bytes[7]=(byte) fileLength;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getCatalogBlock() {
        return catalogBlock;
    }

    public void setCatalogBlock(int catalogBlock) {
        this.catalogBlock = catalogBlock;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public boolean isExecutable() {
        return executable;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Catalog catalog = (Catalog) o;

        if (catalogBlock != catalog.catalogBlock) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + startBlock;
        return result;
    }

}
