package control;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainFX extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{

        try
        {
            AnchorPane root = FXMLLoader.load(getClass().getResource("../view/view.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Genetic Timetable");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { launch(args); }

}
