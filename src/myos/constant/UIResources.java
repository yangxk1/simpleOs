package myos.constant;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * 文件扩展名
 *
 * @author WTDYang
 * @date 2022/12/05
 */
public class UIResources {
//    public static final Node directoryIcon;
//    public static final Node fileIcon;
//    static {
//            directoryIcon=
//            fileIcon=
//    }
    public static Node getDirectoryIcon(){
     return    new ImageView(new Image(UIResources.class.getResourceAsStream("/ui/directory.png")));
    }
    public static Node getFileIcon(){
      return   new ImageView(new Image(UIResources.class.getResourceAsStream("/ui/file.png")));
    }
    public static Node getProgramIcon(){
        return new ImageView(new Image(UIResources.class.getResourceAsStream("/ui/program.png")));
    }
}
