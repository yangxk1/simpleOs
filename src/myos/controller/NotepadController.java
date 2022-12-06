package myos.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import myos.Software;
import myos.manager.filesys.FileOperator;
import myos.manager.filesys.OpenedFile;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * 记事本控制器
 *
 * @author WTDYang
 * @date 2022/12/06
 */
public class NotepadController implements Initializable{
    @FXML
    private MenuItem saveMenuItem;
//    @FXML
//    private MenuItem closeMenuItem;
    @FXML
    private TextArea content;
    private OpenedFile openedFile;
    public NotepadController( ){
        this.openedFile=openedFile;
    }
    public void saveFile() throws Exception {
        FileOperator fileOperator= Software.fileOperator;
        String text=content.getText();
        fileOperator.write(openedFile,text.getBytes(),text.length());
    }
    public void closeFile() throws Exception {
        FileOperator fileOperator= Software.fileOperator;
        fileOperator.close(openedFile);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setOpenedFile(OpenedFile openedFile) {
        this.openedFile = openedFile;
    }
    public void setText(String text){
        content.setText(text);
    }
}
