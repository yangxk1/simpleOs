package myos;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import myos.controller.MainController;


/**
 * 主要
 *
 * @author WTDYang
 * @date 2022/12/06
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader=new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/os.fxml"));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        MainController mainController= fxmlLoader.getController();
        System.out.println(mainController);
        Software os= Software.getInstance();
        os.setMainController(mainController);
        mainController.setOs(os);
        primaryStage.setTitle("杨潇康的垃圾操作系统");
        primaryStage.setScene(new Scene(root,1100,750));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                mainController.closeOs();
                System.exit(0);
            }
        });

        primaryStage.show();
    }

    /**
     * 主启动类
     *
     * @param args arg
     * @throws Exception 异常
     */
    public static void main(String[] args) throws Exception {

               launch(args);

    }
}
