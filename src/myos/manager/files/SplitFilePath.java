package myos.manager.files;


/**
 * 文件路径
 *
 * @author WTDYang
 * @date 2022/12/08
 */
public class SplitFilePath {
    private String path;
    private String fileName;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * 将文件路径与文件名分割开来
     *
     * @param filePath
     * @return
     */
    public static SplitFilePath splitPathAndFileName(String filePath) {
        int fileNameStartIndex = filePath.lastIndexOf('/');
        //单独的文件名
        String fileName = filePath.substring(fileNameStartIndex + 1);
        //提取路径
        String path = filePath.substring(0, fileNameStartIndex);
        SplitFilePath splitFilePath = new SplitFilePath();
        splitFilePath.setFileName(fileName);
        splitFilePath.setPath(path);
        return splitFilePath;
    }
}
