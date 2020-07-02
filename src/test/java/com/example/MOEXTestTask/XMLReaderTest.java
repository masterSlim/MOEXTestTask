package com.example.MOEXTestTask;

import java.io.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sample.XMLReader;

import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.*;

public class XMLReaderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    List<File> historyFiles;
    List<File> securityFiles;

    @Before
    public void init() {
        historyFiles = Arrays.asList(
                new File("test\\data\\history_1.xml"),
                new File("test\\data\\history_2.xml")
        );
        securityFiles = Arrays.asList(
                new File("test\\data\\securities_1.xml"),
                new File("test\\data\\securities_2.xml")
        );
    }

    @Test
    public void getRecords() throws XMLStreamException, FileNotFoundException, ParseException {

        for (File f : historyFiles) {
            XMLReader reader = new XMLReader(f);
            int i = reader.getRecords().size();
            assertEquals(100, i);
        }
        for (File f : securityFiles) {
            XMLReader reader = new XMLReader(f);
            int i = reader.getRecords().size();
            assertEquals(100, i);
        }
    }

    @Test
    public void test () throws FileNotFoundException, XMLStreamException, ParseException {
    thrown.expect(FileNotFoundException.class);
    File f = new File("");
    XMLReader reader = new XMLReader(f);
    }

    @Test
    public void testNullPointerException() throws FileNotFoundException, XMLStreamException, ParseException {
        thrown.expect(NullPointerException.class);
        File f = null;
        XMLReader reader = new XMLReader(f);
    }

    @Test
    public void testHistoryDataId() throws XMLStreamException, FileNotFoundException, ParseException {
        for (File f : historyFiles) {
            XMLReader reader = new XMLReader(f);
            XMLReader.DATA_ID dataId = reader.getDataId();
            assertEquals(XMLReader.DATA_ID.HISTORY, dataId);
        }
    }
    @Test
    public void testSecurityDataId() throws XMLStreamException, FileNotFoundException, ParseException {
        for (File f : securityFiles) {
            XMLReader reader = new XMLReader(f);
            XMLReader.DATA_ID dataId = reader.getDataId();
            assertEquals(XMLReader.DATA_ID.SECURITIES, dataId);
        }
    }
}