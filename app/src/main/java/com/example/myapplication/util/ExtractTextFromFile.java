package com.example.myapplication.util;

import static com.example.myapplication.util.FileUtils.getFileExtension;

import android.content.ContentResolver;
import android.net.Uri;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;

public class ExtractTextFromFile{
    public static final String docx_mimetype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String txt_mimetype = "text/plain";
    public static final String doc_mimetype = "application/msword";
    public static final String docx = "docx";
    public static final String txt = "txt";
    public static final String doc = "doc";
    public static final String error = "errorMimeType";
    private ContentResolver contentResolver;

    public ExtractTextFromFile(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public String getFileMimetype(Uri fileUri){
        String mimeType =  contentResolver.getType(fileUri);


        switch (mimeType) {
            case docx_mimetype:
                return docx;
            case txt_mimetype:
                return txt;
            case doc_mimetype:
                return doc;
            default:
                return mimeType;
        }
    }



/*    public String readFromDocxFile(Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            XWPFDocument document = new XWPFDocument(inputStream);
            StringBuilder stringBuilder = new StringBuilder();



            for (XWPFParagraph paragraph : document.getParagraphs()) {
                stringBuilder.append(paragraph.getText()).append("\n");
            }

            inputStream.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }


    }*/

    public String readFromTxtFile(Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            reader.close();
            inputStream.close();
            return stringBuilder.toString();
        } catch (IOException e) {


            e.printStackTrace();
            return "";
        }
    }

    public String readFromDocxFile(Uri uri) {

        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            XWPFWordExtractor extractor = new XWPFWordExtractor(OPCPackage.open(inputStream));
            String  fileContent = extractor.getText();
            inputStream.close();
            return fileContent;
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            return "";
        }
//        return "";
    }

    public String getText(Uri fileUri){
        String mimeType =  getFileMimetype(fileUri);

        switch (mimeType) {
            case docx:
                return readFromDocxFile(fileUri);
            case txt:
                return readFromTxtFile(fileUri);
            case doc:
                return doc;
            default:
                return getFileExtension(contentResolver,fileUri);
        }
    }
}
