package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.stream.XMLStreamException;

import javafx.event.*;

import java.io.*;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    private static TempDB tempDB;
    Alert alert;

    public static void main(String[] args) {
        launch();

    }

    @Override
    public void start(Stage primaryStage) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("E:\\Java\\JavaProjects\\untitled\\MOEXTestTask\\test\\data"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file", "*.xml"));
        tempDB = new TempDB();
        List<File> files;
        try {
            files = fc.showOpenMultipleDialog(primaryStage);
            for (File f : files) {
                XMLReader r = new XMLReader(f);
                tempDB.add(r);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
            MainWindowController controller = new MainWindowController(tempDB);
            loader.setController(controller);
            try {
                Parent root = loader.load();
                primaryStage.setTitle("MOEX security data");
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
            } catch (IOException e) {
                alert = showAlert(e, "Невозможно загрузить контроллер");
            }
        } catch (NullPointerException e) {
            alert = showAlert(e, "Файл XML не выбран");
        } catch (FileNotFoundException e) {
            alert = showAlert(e, "Файл не найден");
        } catch (XMLStreamException | ParseException e) {
            alert = showAlert(e, "Невозможно прочитать файл");
        }
    }

    private Alert showAlert(Exception e, String message) {
        Alert alert;
        if (e.getClass() == NullPointerException.class ||
                e.getClass() == FileNotFoundException.class) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            System.err.println(message);
            alert.setHeaderText(message);
            alert.setContentText("Выбрать другой файл?");
            ButtonType choose = new ButtonType("Выбрать");
            ButtonType cancel = new ButtonType("Отмена");
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(choose, cancel);
            Optional<ButtonType> option = alert.showAndWait();

            ButtonType buttonType = option.get();

            if (choose.equals(buttonType)) {
                start(new Stage());
                alert.close();

            } else if (cancel.equals(buttonType)) {
                System.exit(0);
            }
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            System.err.println(message);
            alert.setHeaderText(message);
            alert.setContentText(e.fillInStackTrace().toString());
            alert.showAndWait();
            System.exit(0);
        }
        return alert;
    }
}
