package sample;

import com.sun.istack.internal.NotNull;

import java.util.*;

public class TempDB {
    private List<HistoryEntry> histories;
    private Map<String, SecurityPaper> securities;
    private Comparator<HistoryEntry> comparator;
    private long idCounter;

    /**
     * Класс содержит, обрабатывает и предоставляет данные о ценных бумагах (securities) и записях истории торгов
     * (histories). Данные о ценной бумаге представлены объектом внутреннего класса SecurityPaper. Даные о записи истории
     * торгов представлены объектом внутреннего класса HistoryEntry, который в качестве аргумента конструктора принмиает
     * объект SecurityPaper представляющий торгуемую бумагу. Таким образом, создание HistoryEntry возможно только
     * при наличии соответствующей SecurityPaper. Ключевым уникальным полем обоих объектов является SECID.
     */

    public TempDB() {
        histories = new LinkedList<>();
        securities = new HashMap<>();
        comparator = Comparator.comparing(obj -> obj.getSecid());
        idCounter = 0;
    }

    /**
     * Добавление записей в локальную базу данных. Принимает в качестве аргумента один или несколько объектов XMLReader.
     */

    public void add(XMLReader... readers) {
        // обход циклом всех аргументов метода
        for (XMLReader reader : readers) {
            // если объект reader представляет список ценных бумаг, каждая бумага добавляется в переменную securities
            if (reader.getDataId() == XMLReader.DATA_ID.SECURITIES) {
                for (XMLReader.Record r : reader.getRecords()) {
                    String secid = (String) r.getAttributeValue("secid");
                    SecurityPaper sp = new SecurityPaper(secid, r.getAllAttributes());
                    securities.put(secid, sp);
                }
            }
            // если объект reader представляет историю торгов, то для каждой записи проверяется наличие ценной
            // бумаги в securities; создаётся объект HistoryEntry, за которой объект этой бумаги закрепляется
            // и добавляется в переменную securities
            if (reader.getDataId() == XMLReader.DATA_ID.HISTORY) {
                for (XMLReader.Record r : reader.getRecords()) {
                    String secid = (String) r.getAttributeValue("secid");
                    if (securities.containsKey(secid)) {
                        HistoryEntry he = new HistoryEntry(securities.get(secid), r.getAllAttributes(), idCounter++);
                        histories.add(he);
                    }
                }
            }
        }
        // сортировка по secid в алфавитном порядке
        histories.sort(comparator);
    }

    /**
     * Добавление данных о ценной бумаге в локальную базу данных. Принимает в качестве аргумента один
     * или несколько объектов SecurityPaper.
     */

    public void add(SecurityPaper... securityPapers) {
        for (SecurityPaper sp : securityPapers) {
            String secid = sp.getSecid();
            // ключом для ценной бумаги является её уникальный secid, значением  - объект SecurityPaper
            securities.put(secid, sp);
        }
    }

    /**
     * Добавление записей о истории торгов в локальную базу данных. Принимает в качестве аргумента один
     * или несколько объектов HistoryEntry.
     */

    public void add(HistoryEntry... historyEntries) {
        for (HistoryEntry he : historyEntries) {
            // новая запись истории торгов добавляется в histories только в том случае, если есть данные о торгующейся
            // ценной бумаге в securities (проверяется secid, являющийся уникальным для каждой бумаги)
            if (securities.containsKey(he.getSecid())) {
                histories.add(he);
            }
        }
    }

    /**
     * Возвращает список HashMap-объектов, каждый из которых представляющих данные из одного HistoryEntry
     * с одним или несколькими атрибутами, переданными в качестве аргумента.
     */

    public LinkedList<HashMap<String, Object>> getPreparedList(String... attributes) {
        LinkedList<HashMap<String, Object>> result = new LinkedList<>();
        for (HistoryEntry he : histories) {
            result.add(he.getAttributes(attributes));
        }
        return result;
    }

    /**
     * Возвращает список всех объектов HistoryEntry находящиеся в TempDB.
     *
     * @return
     */

    public List<HistoryEntry> getHistories() {
        return histories;
    }

    /**
     * Класс содержит, обрабатывает и предоставляет данные о конкретной ценной бумаге.
     */
    public class SecurityPaper {
        // уникальный id ценной бумаги состоящий из букв
        private final String secid;
        // карта аттрибутов (key) и их значений (value) для данной ценной бумаги
        private HashMap<String, Object> info;

        public SecurityPaper(@NotNull String secid, HashMap<String, Object> secInfo) {
            this.secid = secid;
            info = secInfo;
        }

        public String getSecid() {
            return secid;
        }

        /**
         * Возвращает HashMap всех аттрибутов (key) и их значений (value) для конкретной ценной бумаги
         * @return
         */
        public HashMap<String, Object> getInfo() {
            return info;
        }

        /**
         * Изменяет или добавляет атрибут (key) и значение этого атрибута (value) для конкретной ценой бумаги.
         *
         * @param info данные вида аттрибут (key) - значение аттрибута (value) коотрые необходимо внести или заменить
         */
        public void setInfo(HashMap<String, Object> info) {
            this.info.putAll(info);
        }

        /**
         * Формирует все данные о ценной бумаге в объект String предстваляющий XML-строку вида <row attribute="value" />.
         *
         * @return
         */
        public String getXMLRow() {
            StringBuilder row = new StringBuilder();
            row.append("<row");
            row.append(" ");
            for (String key : info.keySet()) {
                // заменяются ковычки-лапки на используемую в XML запись &quot
                String value = info.get(key).toString().replace("\"", "&quot");
                row.append(key + "=" + "\"" + value + "\"");
                row.append(" ");
            }
            row.append("/>");
            return row.toString();
        }
    }

    /**
     * Класс содержит, обрабатывает и предоставляет данные об одной конкретной записи в истории торгов.
     * Невозможно создать HistoryEntry, если бумаги, упоминающейся в ней (проверяется поле secid), нет в списке ценных бумаг
     * (переменная securities).
     */

    public class HistoryEntry {
        private final long id;
        private final String secid;
        private SecurityPaper security;
        private LinkedHashMap<String, Object> attributes;

        /**
         * Для создания объекта HistoryEntry необходим соответствующий объект SecurityPaper. СОответствие определяется
         * уникальным полем secid.
         *
         * @param security
         * @param attributes
         * @param id
         */

        public HistoryEntry(@NotNull SecurityPaper security, LinkedHashMap<String, Object> attributes, long id) {
            this.security = security;
            this.attributes = attributes;
            this.secid = this.security.getSecid();
            this.id = id;
        }

        public LinkedHashMap<String, Object> getAttributes(String... attribute) {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            for (String att : attribute) {
                if (security.getInfo().containsKey(att)) {
                    Object value = security.getInfo().get(att);
                    result.put(att, value);
                } else if (attributes.containsKey(att)) {
                    Object value = attributes.get(att);
                    result.put(att, value);
                }
            }
            return result;
        }

        /**
         * Формирует все данные о записи истории торгов в объект String предстваляющий XML-строку вида
         * <row attribute="value" />.
         */

        public String getXMLRow() {
            StringBuilder row = new StringBuilder();
            row.append("<row");
            row.append(" ");
            for (String key : attributes.keySet()) {
                String value = attributes.get(key).toString().replace("\"", "&quot");
                row.append(key + "=" + "\"" + value + "\"");
                row.append(" ");
            }
            row.append("/>");
            return row.toString();
        }

        public Map<String, Object> getAllAttributes() {
            return attributes;
        }

        public SecurityPaper getSecurity() {
            return security;
        }

        public String getSecid() {
            return secid;
        }

        public long getId() {
            return id;
        }
    }
}