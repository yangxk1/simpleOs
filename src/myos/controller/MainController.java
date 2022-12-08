package myos.controller;

import com.sun.deploy.util.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import myos.Software;
import myos.constant.UIResources;
import myos.manager.device.DeviceOccupy;
import myos.manager.device.DeviceRequest;
import myos.manager.filesys.Catalog;
import myos.manager.filesys.OpenedFile;
import myos.manager.process.CPU;
import myos.manager.process.PCB;
import myos.manager.memory.SubArea;
import myos.manager.process.Clock;
import myos.utils.ThreadPoolUtil;
import myos.vo.DeviceVo;
import myos.vo.MyTreeItem;
import myos.vo.PCBVo;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * UI总控
 *
 * @author WTDYang
 * @date 2022/12/07
 */
@SuppressWarnings("all")
public class MainController implements Initializable {
    @FXML
    private GridPane fatView;
    @FXML
    private Button osSwitchBtn;
    @FXML
    private TreeView<Catalog> catalogTreeView;
    @FXML
    private Text systemTimeTxt;
    @FXML
    private Text timesliceTxt;
    @FXML
    private TextArea cmdView;
    @FXML
    private TextArea processRunningView;
    @FXML
    private TextArea processResultView;
    @FXML
    private TableView<PCBVo> pcbQueueView;
    @FXML
    private TableColumn pidCol;
    @FXML
    private TableColumn statusCol;
    @FXML
    private TableColumn priorityCol;
    @FXML
    private HBox userAreaView;
    @FXML
    private TableView<DeviceVo> waitingDeviceQueueView;
    @FXML
    private TableColumn waitingDeviceNameCol;
    @FXML
    private TableColumn waitingDevicePIDCol;
    @FXML
    private TableView<DeviceVo> usingDeviceQueueView;
    @FXML
    private TableColumn usingDeviceNameCol;
    @FXML
    private TableColumn usingDevicePIDCol;
    private Software os;

    /**
     * 位置
     */
    private String location = "root";

    /**
     * 初始化组件
     *
     * @throws Exception
     */
    public void initComponent() throws Exception {
        processRunningView.setText("");
        processResultView.setText("");
        ThreadPoolUtil.getInstance().execute(()->{
            cmdView.setText("\n\n\n\n\n           LOADING MY OS  .");
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                cmdView.setText("\n\n\n\n\n           LOADING MY OS  . .");
                TimeUnit.MILLISECONDS.sleep(500);
                cmdView.setText("\n\n\n\n\n           LOADING MY OS  . . .");
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cmdView.setText(location+">");
        });
        //初始化进程队列视图
        initPcbQueueView();
        //初始化目录树
        initCatalogTree();
        //初始化磁盘分配表视图
        updateFatView();
        initUsingDeviceQueueView();
        initWaingDeviceQueueView();

    }

    public void loadDesk() throws Exception {
                    String[][] instruction = {{"mov ax,50", "inc ax", "mov bx,111", "dec bx", "mov cx,23", "! a 2", "end"},
                    {"mov ax,50", "mov dx,30", "! b 2", "dec bx", "inc dx", "mov ax 25", "end"},
                    {"mov ax,50", "! c 3", "mov bx,111", "! c 1", "mov cx,23", "! a 1", "end"},
                    {"mov ax,50", "inc ax", "! b 1", "! a 2", "mov cx,23", "inc cx", "inc ax", "! c 2", "end"},
                    {"mov bx,70", "inc bx", "mov bx,12", "dec bx", "! c 3", "inc bx", "mov cx,23", "! a 1", "dec cx", "end"},
                    {"mov ax,50", "! b 2", "mov bx,12", "! c 1", "mov cx,23", "! a 1", "mov ax,50", "inc ax", "mov bx,21", "dec bx", "mov cx,23", "! a 1", "end"},
                    {"mov ax,50", "inc ax", "! b 1", "! a 2", "mov cx,23", "inc ax", "mov bx,122", "dec bx", "mov cx,32", "! a 1", "end"},
                    {"mov ax,50", "inc ax", "! c 1", "mov cx,23", "! a 1", "mov bx,111", "dec bx", "mov cx,20", "! a 1", "end"},
                    {"mov ax,50", "inc ax", "mov bx,13", "dec bx", "mov cx,23", "! a 1", "inc ax", "mov bx,112", "dec bx", "mov cx,23", "! a 1", "end"},
                    {"mov ax,50", "inc ax", "mov bx,116", "inc ax", "mov bx,111", "dec bx", "mov cx,23", "! a 1", "dec bx", "mov cx,23", "! b 1", "end"}};
            os.fileOperator.create("root/exe1",8);
            for (int i = 0; i < instruction.length; i++) {
                String path = "root/exe1/" + String.valueOf(i) + ".e";
                os.fileOperator.create(path, 16);
                OpenedFile open = os.fileOperator.open(path, OpenedFile.OP_TYPE_WRITE);
                byte[] b = new CPU().getInstruction(instruction[i]);
                os.fileOperator.append(path, b, b.length);
                os.fileOperator.close(open);
            }
    }
    public void osSwitch() throws Exception {
        if (!Software.launched) {
            launchOS();
            cmdView.setEditable(true);
            osSwitchBtn.setStyle("-fx-background-color:#e34040");
            osSwitchBtn.setText("关机");
//            loadDesk();
        } else {
            closeOs();
            cmdView.setEditable(false);
            osSwitchBtn.setStyle("-fx-background-color:#0080ff");
            osSwitchBtn.setText("开机");
        }
    }

    /**
     * 启动系统
     */
    public void launchOS() throws Exception {
        Software.launched = true;
        os.start();
        initComponent();
        new Thread(new UpdateUIThread()).start();
    }

    /**
     * 关闭系统
     */
    public void closeOs() {
        Software.launched = false;
        os.close();

    }

    /**
     * 执行cmd
     *
     * @param event 事件
     * @throws Exception 异常
     */
    public void executeCMD(KeyEvent event) throws Exception {

        if (event.getCode() == KeyCode.ENTER) {
            if (cmdView.getText() == null || cmdView.getText().equals("")) {
                return;
            }
            String text = cmdView.getText();
            int index = text.lastIndexOf(">");
            if (index >= 0) {
                text = text.substring(index + 1);
                if ("".equals(text) || " ".equals(text)) {
                    return;
                }
            }
            String[] str = text.split("\\n");
            String s = str[str.length - 1];
            String[] instruction = s.trim().split("\\s+");
            if (instruction.length>1 &&!"cd".equals(instruction[0]) && instruction[1].indexOf("root") == -1) {
                instruction[1] = location + "/" + instruction[1];
            }
            try {
                if ("create".equals(instruction[0])) {
                    Software.fileOperator.create(instruction[1], 4);
                    cmdView.appendText("-> File created successfully\n");
                } else if ("delete".equals(instruction[0])) {
                    Software.fileOperator.delete(instruction[1]);
                    cmdView.appendText("-> File deleted successfully\n");
                } else if ("type".equals(instruction[0])) {
                    String content = Software.fileOperator.type(instruction[1]);
                    cmdView.appendText(content + "\n");
                } else if ("mkdir".equals(instruction[0])) {
                    Software.fileOperator.mkdir(instruction[1]);
                    cmdView.appendText("-> Directory created successfully\n");
                } else if ("rmdir".equals(instruction[0])) {
                    Software.fileOperator.rmdir(instruction[1]);
                    cmdView.appendText("-> Directory deleted successfully\n");
                } else if ("change".equals(instruction[0]) && instruction.length == 3) {
                    int newProperty = Integer.valueOf(instruction[2]).intValue();
                    Software.fileOperator.changeProperty(instruction[1], newProperty);
                    cmdView.appendText("-> Modified file attributes successfully \n");
                } else if ("run".equals(instruction[0])) {
                    Software.fileOperator.run(instruction[1]);
                    cmdView.appendText("-> Run file "+instruction[1]+" successfully\n");
                } else if ("open".equals(instruction[0])) {
                    OpenedFile openedFile = Software.fileOperator.open(instruction[1], OpenedFile.OP_TYPE_READ_WRITE);
                    String content = new String(Software.fileOperator.read(openedFile, -1));
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("/notepad.fxml"));
                    Parent parent = fxmlLoader.load();
                    NotepadController notepadController = fxmlLoader.getController();
                    notepadController.setOpenedFile(openedFile);
                    notepadController.setText(content);
                    Stage notePadStage = new Stage();
                    notePadStage.setScene(new Scene(parent, 600, 400));
                    notePadStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            try {
                                notepadController.closeFile();
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    });
                    notePadStage.show();
                } else if ("copy".equals(instruction[0])) {
                    Software.fileOperator.copy(instruction[1], instruction[2]);
                    cmdView.appendText("-> Files copied successfully\n");
                } else if ("format".equals(instruction[0])) {
                    Software.fileOperator.format();
                    cmdView.appendText("-> Format the hard disk successfully\n");
                } else if ("cd".equals(instruction[0])) {
                    if ("..".equals(instruction[1])) {
                        if (!"root".equals(location)) {
                            location = location.substring(0, location.lastIndexOf("/"));
                        }
                    } else {
                        Software.fileOperator.existsDir(instruction[1], location);
                        location = location + "/" + instruction[1];
                    }
                } else if("clear".equals(instruction[0])){
                    cmdView.setText("");
                }else {
                    cmdView.appendText("-> "+instruction[0]+"不是内部或外部命令\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                String[] exception = ex.toString().split(":");
                cmdView.appendText("-> " + exception[exception.length - 1].trim() + "\n");
            } finally {
                cmdView.appendText(location + ">");
            }
        }

    }


    /**
     * 构建目录树
     */
    public void initCatalogTree() throws Exception {
        Catalog root = Software.fileOperator.readCatalog(2);

        TreeItem<Catalog> treeItem = new MyTreeItem(root);
        catalogTreeView.setRoot(treeItem);
        catalogTreeView.setCellFactory(new Callback<TreeView<Catalog>, TreeCell<Catalog>>() {
            public TreeCell<Catalog> call(TreeView<Catalog> param) {
                return new TreeCell<Catalog>() {

                    @Override
                    protected void updateItem(Catalog catalog, boolean empty) {
                        super.updateItem(catalog, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(catalog.getName());
                            if (catalog.isDirectory()) {
                                setGraphic(UIResources.getDirectoryIcon());
                            } else if (catalog.isExecutable()) {
                                setGraphic(UIResources.getProgramIcon());
                            } else {
                                setGraphic(UIResources.getFileIcon());
                            }
                        }
                    }
                };
            }
        });
        catalogTreeView.refresh();
    }

    /**
     * 初始化进程队列视图
     */
    public void initPcbQueueView() {
        pidCol.setCellValueFactory(new PropertyValueFactory<>("PID"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
    }

    public void initWaingDeviceQueueView() {
        waitingDeviceNameCol.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        waitingDevicePIDCol.setCellValueFactory(new PropertyValueFactory<>("PID"));
    }

    public void initUsingDeviceQueueView() {
        usingDeviceNameCol.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        usingDevicePIDCol.setCellValueFactory(new PropertyValueFactory<>("PID"));
    }

    /**
     * 添加树节点
     *
     * @param parent
     * @param newCatalog
     */
    public void addTreeItem(Catalog parent, Catalog newCatalog) {
        TreeItem<Catalog> root = catalogTreeView.getRoot();
        TreeItem<Catalog> parentTreeItem = findTreeItem(root, parent);
        parentTreeItem.getChildren().add(new TreeItem<>(newCatalog));
        catalogTreeView.refresh();
    }

    /**
     * 删除树节点
     *
     * @param catalog
     */
    public void removeTreeItem(Catalog catalog) {
        TreeItem<Catalog> root = catalogTreeView.getRoot();
        TreeItem<Catalog> treeItem = findTreeItem(root, catalog);
        //节点视图如果已经被加载
        if (treeItem != null) {
            treeItem.getParent().getChildren().remove(treeItem);
            catalogTreeView.refresh();
        }
    }

    /**
     * 更新树节点
     *
     * @param catalog
     */
    public void updateTreeItem(Catalog catalog) {
        TreeItem<Catalog> root = catalogTreeView.getRoot();
        TreeItem<Catalog> treeItem = findTreeItem(root, catalog);
        if (treeItem != null) {
            treeItem.setValue(catalog);
            catalogTreeView.refresh();
        }
    }

    /**
     * 从root节点开始查找节点
     *
     * @param catalog
     */
    public TreeItem<Catalog> findTreeItem(TreeItem<Catalog> root, Catalog catalog) {
        if (root.getValue().equals(catalog)) {
            return root;
        }

        if (root.isLeaf()) {
            return null;
        }
        for (TreeItem<Catalog> catalogTreeItem : root.getChildren()) {
            TreeItem t = findTreeItem(catalogTreeItem, catalog);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    /**
     * 更新磁盘使用情况
     */
    public void updateFatView() throws IOException {
        byte[] fat = Software.fileOperator.getFat();
        for (int i = 0; i < fat.length; i++) {
            Pane pane = (Pane) fatView.getChildren().get(i);
            if (fat[i] != 0) {
                pane.setStyle("-fx-background-color: #e34040;-fx-border-color: #ffffff");
            } else {
                pane.setStyle("-fx-background-color:#459aec;-fx-border-color: #ffffff");
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    /**
     * UI更新线程
     *
     * @author WTDYang
     * @date 2022/12/07
     */
    private class UpdateUIThread implements Runnable {
        @Override
        public void run() {
            while (Software.launched) {
                try {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            //更新等待设备进程队列视图
                            BlockingQueue<DeviceRequest> waitForDevices = Software.cpu.getDeviceManager().getWaitForDevice();
                            ObservableList<DeviceVo> deviceVos = FXCollections.observableArrayList();
                            for (DeviceRequest deviceRequest : waitForDevices) {
                                DeviceVo deviceVo = new DeviceVo(deviceRequest.getDeviceName(), deviceRequest.getPcb().getPID());
                                deviceVos.add(deviceVo);
                            }
                            waitingDeviceQueueView.setItems(deviceVos);
                            //更新使用设备进程队列视图
                            Queue<DeviceOccupy> usingDevices = Software.cpu.getDeviceManager().getUsingDevices();
                            ObservableList<DeviceVo> deviceVos2 = FXCollections.observableArrayList();
                            for (DeviceOccupy deviceOccupy : usingDevices) {
                                DeviceVo deviceVo = new DeviceVo(deviceOccupy.getNickName(), deviceOccupy.getObj().getPID());
                                deviceVos2.add(deviceVo);
                            }
                            usingDeviceQueueView.setItems(deviceVos2);
                            //更新进程执行过程视图2
                            MainController.this.processRunningView.appendText(Software.cpu.getResult() + "\n");
                            MainController.this.processResultView.appendText(Software.cpu.getProcess());
                            //更新系统时钟视图
                            MainController.this.systemTimeTxt.setText(Software.clock.getSystemTime() + "");
                            //更新时间片视图
                            MainController.this.timesliceTxt.setText(Software.clock.getRestTime() + "");
                            //更新进程队列视图
                            List<PCB> pcbs = Software.memory.getAllPCB();
                            List<PCBVo> pcbVos = new ArrayList<>(pcbs.size());
                            for (PCB pcb : pcbs) {
                                PCBVo pcbVo = new PCBVo(pcb);
                                pcbVos.add(pcbVo);
                            }
                            ObservableList<PCBVo> datas = FXCollections.observableList(pcbVos);
                            pcbQueueView.setItems(datas);
                            //更新用户区内存视图
                            userAreaView.getChildren().removeAll(userAreaView.getChildren());
                            List<SubArea> subAreas = Software.memory.getSubAreas();
                            for (SubArea subArea : subAreas) {
                                Pane pane = new Pane();
                                pane.setPrefHeight(40);
                                pane.setPrefWidth(subArea.getSize());
                                if (subArea.getStatus() == SubArea.STATUS_BUSY) {
                                    pane.setStyle("-fx-background-color: orangered;");
                                } else {
                                    pane.setStyle("-fx-background-color:#459aec;");
                                }

                                userAreaView.getChildren().add(pane);
                            }
                        }
                    });


                    Thread.sleep(Clock.TIMESLICE_UNIT);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public void setOs(Software os) {
        this.os = os;
    }
}
