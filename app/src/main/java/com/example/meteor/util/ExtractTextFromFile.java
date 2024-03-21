package com.example.meteor.util;

import android.content.ContentResolver;
import android.net.Uri;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class ExtractTextFromFile {
    public static final String docx_mimetype = "application/vnd.openxmlformats-officedocument" +
            ".wordprocessingml.document";
    public static final String txt_mimetype = "text/plain";
    public static final String doc_mimetype = "application/msword";
    private static ContentResolver contentResolver;
    private static Map<String, TextExtractorStrategy> strategyMap;
    private static volatile ExtractTextFromFile instance; // 使用volatile确保多线程环境下的可见性


    private ExtractTextFromFile() {

        strategyMap = new HashMap<>();
        strategyMap.put(txt_mimetype, new TxtTextExtractor());
        strategyMap.put(docx_mimetype, new DocxTextExtractor());

    }

    public static ExtractTextFromFile getInstance(ContentResolver resolver) {
        if (contentResolver == null) {
            synchronized (ExtractTextFromFile.class) {
                if (contentResolver == null) {
                    contentResolver = resolver;
                    instance = new ExtractTextFromFile();
                }
            }
        }
        return instance;
    }

    public String getText(Uri fileUri) {
        String mimeType = contentResolver.getType(fileUri);
        TextExtractorStrategy strategy = strategyMap.get(mimeType);

        if (strategy != null) {
            try (InputStream inputStream = contentResolver.openInputStream(fileUri)) {
                return strategy.extractText(inputStream);
            } catch (IOException | InvalidFormatException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "Unsupported file type";
        }
    }

}
