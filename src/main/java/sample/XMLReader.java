package sample;

import com.sun.istack.internal.NotNull;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;

/**
 * Класс осуществляет чтение XML-файла и преобразование его данных в виде объектов внутреннего класса Record.
 * Каждый объект класса XMLReader представляет один файл. В поле DATA_ID заносится имя типа данных, котоыре хранит файл:
 * "securities", если в XML файле хранятся данные о ценных бумагах; "history", если в файле хранятся записи о ходе торгов.
 */

public class XMLReader {
    private static String filePath;
    private DATA_ID dataId;
    private HashMap<String, String> types;
    private LinkedList<Record> records;
    private XMLStreamReader stream;

    /**
     * Создание объекта XMLReader по ссылке на файл
     *
     * @param filePath путь к файлу
     * @throws FileNotFoundException файл не найден
     * @throws XMLStreamException    прочитать файл не удаётся
     */
    public XMLReader(@NotNull String filePath) throws FileNotFoundException, XMLStreamException, ParseException {
        XMLReader.filePath = filePath;
        records = new LinkedList<>();
        stream = getStream();
        types = new HashMap<>();

        while (stream.hasNext()) {
            if (stream.next() == XMLEvent.START_ELEMENT) {
                if (dataId == null) parseDataId();
                parseTypes();
                parseRows();
            }
        }
        stream.close();
    }

    /**
     * Создание объекта XMLReader по передаваемому объекту java.io.File
     *
     * @param file объект java.io.File
     * @throws XMLStreamException    прочитать файл не удаётся
     * @throws FileNotFoundException файл не найден
     */
    public XMLReader(@NotNull File file) throws XMLStreamException, FileNotFoundException, ParseException {
        dataId = null;
        XMLReader.filePath = file.getAbsolutePath();
        records = new LinkedList();
        types = new HashMap<>();
        stream = getStream();

        while (stream.hasNext()) {
            if (stream.next() == XMLEvent.START_ELEMENT) {
                if (dataId == null) parseDataId();
                parseTypes();
                parseRows();
            }
        }
        stream.close();
    }

    private static XMLStreamReader getStream() throws FileNotFoundException, XMLStreamException {
        return XMLInputFactory.newInstance().createXMLStreamReader(filePath, new FileInputStream(filePath));
    }

    private void parseTypes() {
        if ("column".equals(stream.getLocalName())) {
            String name = "";
            String type = "";
            for (int i = 0; i < stream.getAttributeCount(); i++) {
                if ("name".equalsIgnoreCase(stream.getAttributeName(i).toString())) {
                    name = stream.getAttributeValue(i).trim().toLowerCase();
                } else if ("type".equalsIgnoreCase(stream.getAttributeName(i).toString())) {
                    type = stream.getAttributeValue(i);
                }
            }
            switch (name) {
                case "INDEX":
                case "TOTAL":
                case "PAGESIZE":
                    break;
                default:
                    types.put(name, type);
            }
        }
    }

    /**
     * Возвращает все записи Record полученные из файла
     *
     * @return LinkedList всех записей Records полученых из файла
     */

    public LinkedList<Record> getRecords() {
        return records;
    }

    /**
     * Возвращает DATA_ID представляющий тип данных, котоыре хранятся в файле.
     *
     * @return значение dataId
     */
    public DATA_ID getDataId() {
        return dataId;
    }

    // определяет, какого типа данные содержит файл - информация о ценных бумагах или записи о ходе торгов
    private void parseDataId() throws XMLStreamException {
        if ("data".equals(stream.getLocalName())) {
            String id = stream.getAttributeValue(0);
            switch (id) {
                case "history":
                    dataId = DATA_ID.HISTORY;
                    break;
                case "securities":
                    dataId = DATA_ID.SECURITIES;
                    break;
                default:
                    throw new XMLStreamException();
            }
        }
    }

    // извлекает из файла и обрабатывает только строки обозначеные тегом <row />, представляющие полезную информацию.
    // Технические данные файла, также записаные в этих тегах, игнорируются
    private void parseRows() throws ParseException {
        if ("row".equals(stream.getLocalName())) {
            if (stream.getAttributeName(0).toString().equalsIgnoreCase("index")
                    || stream.getAttributeName(0).toString().equalsIgnoreCase("pagesize")
                    || stream.getAttributeName(0).toString().equalsIgnoreCase("total")) return;

            // для каждой строки <row />, содержащей полезную информацию, создаётся объект внутреннего класса Record,
            // в который заносятся все атрибуты и их значения, содержащиеся в XML-строке
            Record record = new Record();
            String attName = "";
            Object attValue;
            for (int i = 0; i < stream.getAttributeCount(); i++) {
                // имя атрибута сверяется с types, и создаётся нужный объект для данных
                // (Date для даты, Integer или Double для цифр и т.д.)
                attName = stream.getAttributeName(i).toString().toLowerCase();
                if (!stream.getAttributeValue(i).equals("")) {
                        // переменной для исходных данных для последующей обработки
                        String inputValue = stream.getAttributeValue(i);
                        // в соответствии с types для атрибута задаётся свой объект для данных
                        switch (types.get(attName)) {
                            case "int32":
                                attValue = Integer.parseInt(inputValue);
                                break;
                            case "date":
                                //TODO  неправильно форматирует дату
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                attValue = sdf.parse(inputValue);
                                break;
                            case "double":
                                attValue = Double.parseDouble(inputValue);
                                break;
                            default:
                                attValue = inputValue;
                        }
                } else {
                    attValue = stream.getAttributeValue(i);
                }
                record.putAttribute(attName, attValue);
            }

            switch (attName) {
                case "index":
                case "pagesize":
                case "total":
                    break;
                default:
                    records.add(record);
            }
        }
    }

    public enum DATA_ID {
        SECURITIES,
        HISTORY
    }

    /**
     * Внутренний класс хранит, обрабатывает и представляет данные, извлечённые из одной строки XML-файла.
     */

    public class Record {

        private LinkedHashMap<String, Object> attributes;

        public Record() {
            attributes = new LinkedHashMap<>();
        }

        public void putAttribute(String attName, Object attValue) {
            attributes.put(attName.toLowerCase(), attValue);

        }

        public Object getAttributeValue(String attName) {
            if (attributes.containsKey(attName.toLowerCase())) {
                return attributes.get(attName.toLowerCase());
            }
            if (attributes.containsKey(attName.toUpperCase())) {
                return attributes.get(attName.toLowerCase());
            } else return null;
        }


        public LinkedHashMap<String, Object> getAllAttributes() {
            return attributes;
        }

        public void changeAttribute(String attName, String newValue) {
            if (attributes.containsKey(attName.toLowerCase())) {
                attributes.put(attName.toLowerCase(), newValue);
            }
            if (attributes.containsKey(attName.toUpperCase())) {
                attributes.put(attName.toLowerCase(), newValue);
            }
            //TODO: добавить исключение
            else System.out.println("Атрибут не найден");
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            for (String s : attributes.keySet()) {
                if (attributes.get(s).equals("")) continue;
                sb.append(s);
                sb.append("\t");
                sb.append(attributes.get(s));
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
