package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;

public class MainWindowController implements Initializable {
    TempDB tempDB;
    private String[] attributes;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private TableView table;
    @FXML
    private Button refreshButton;


    public MainWindowController(TempDB tempDB) {
        this.tempDB = tempDB;
        attributes = new String[]{
                "secid",
                "regnumber",
                "name",
                "emitent_title",
                "tradedate",
                "numtrades",
                "open",
                "close",
        };
    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
/*        for (int i = 0; i < attributes.length; i++) {
            TableColumn<Map, String> column = new TableColumn<>(attributes[i].toUpperCase());
            column.setCellValueFactory(new MapValueFactory(attributes[i].toLowerCase()));
            Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactoryForMap = new Callback<TableColumn<Map, String>,
                    TableCell<Map, String>>() {
                @Override
                public TableCell call(TableColumn p) {
                    return new TextFieldTableCell(new StringConverter() {
                        @Override
                        public String toString(Object t) {
                            return t.toString();
                        }

                        @Override
                        public Object fromString(String string) {
                            return string;
                        }
                    });
                }
            };
            table.getColumns().add(column);
        }*/

        for (int i = 0; i < attributes.length; i++) {
            TableColumn<Map, String> column = new TableColumn<>(attributes[i].toUpperCase());
            column.setCellValueFactory(new MapValueFactory(attributes[i].toLowerCase()));
            Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactoryForMap = new Callback<TableColumn<Map, String>,
                    TableCell<Map, String>>() {
                @Override
                public TableCell call(TableColumn p) {
                    return new TextFieldTableCell(new StringConverter() {
                        @Override
                        public String toString(Object t) {
                            return t.toString();
                        }

                        @Override
                        public Object fromString(String string) {
                            return string;
                        }
                    });
                }
            };
            table.getColumns().add(column);
        }

        ObservableList<Map> items = FXCollections.observableArrayList();
        for(TempDB.HistoryEntry he : tempDB.getHistories()){
            Map<String, Object> m = he.getAllAttributes();
            m.putAll(he.getSecurity().getInfo());
            m.put("HistoryEntry", he);
            items.add(m);
        }
        table.setItems(items);
    }

    @FXML
    public void refresh() {
    }
}
