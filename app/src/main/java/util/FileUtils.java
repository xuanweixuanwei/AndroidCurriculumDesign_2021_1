package util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileUtils {

    public  String getFileNameAndExtension(ContentResolver contentResolver, Uri uri) {
        String fileName = "";
//         contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);

        // 查询文件名
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }

        // 获取文件扩展名
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String fileExtension = fileName.substring(dotIndex + 1);
            return fileExtension;
        } else {
            return "error";
        }
    }
}