package util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.loader.content.CursorLoader;

public class FileUtils {

    public static String getFileName (ContentResolver contentResolver, Uri uri){
        String fileName = "";
//         contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);

        // 查询文件名
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
            cursor.close();
            return fileName;
        }
        return "查找文件名失败";
    }

    public static String getFileExtension(ContentResolver contentResolver, Uri uri) {
        String fileName = getFileName(contentResolver,uri);

        // 获取文件扩展名
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String fileExtension = fileName.substring(dotIndex + 1);
            return fileExtension;
        } else {
            return "error";
        }
    }




    private static Context context;

    public static final String TAG = FileUtils.class.getSimpleName();
    public FileUtils(Context context) {
        this.context = context;
    }

    public static String getFilePathByUri(Uri uri) {
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }
        // 以/storage开头的也直接返回
        if (isOtherDocument(uri)) {
            return uri.getPath();
        }
        // 版本兼容的获取！
        String path = getFilePathByUri_BELOWAPI11(uri);
        if (path != null) {
            Log.d(TAG,"getFilePathByUri_BELOWAPI11获取到的路径为：" + path);
            return path;
        }
        path = getFilePathByUri_API11to18(uri);
        if (path != null) {
            Log.d(TAG,"getFilePathByUri_API11to18获取到的路径为：" + path);
            return path;
        }
        path = getFilePathByUri_API19(uri);
        Log.d(TAG,"getFilePathByUri_API19获取到的路径为：" + path);
        return path;
    }

    private static String getFilePathByUri_BELOWAPI11(Uri uri) {
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String path = null;
            String[] projection = new String[]{MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            return path;
        }
        return null;
    }

    private static String getFilePathByUri_API11to18(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        String result = null;
        CursorLoader cursorLoader = new CursorLoader(context, contentUri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
    }

    private static String getFilePathByUri_API19(Uri uri) {
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        if (split.length > 1) {
                            return Environment.getExternalStorageDirectory() + "/" + split[1];
                        } else {
                            return Environment.getExternalStorageDirectory() + "/";
                        }
                        // This is for checking SD Card
                    }
                } else if (isDownloadsDocument(uri)) {
                    //下载内容提供者时应当判断下载管理器是否被禁用
                    int stateCode = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
                    if (stateCode != 0 && stateCode != 1) {
                        return null;
                    }
                    String id = DocumentsContract.getDocumentId(uri);
                    // 如果出现这个RAW地址，我们则可以直接返回!
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    if (id.contains(":")) {
                        String[] tmp = id.split(":");
                        if (tmp.length > 1) {
                            id = tmp[1];
                        }
                    }
                    Uri contentUri = Uri.parse("content://downloads/public_downloads");
                    Log.d(TAG,"测试打印Uri: " + uri);
                    try {
                        contentUri = ContentUris.withAppendedId(contentUri, Long.parseLong(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String path = getDataColumn(contentUri, null, null);
                    if (path != null) return path;
                    // 兼容某些特殊情况下的文件管理器!
                    String fileName = getFileNameByUri(uri);
                    if (fileName != null) {
                        path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                        return path;
                    }
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(contentUri, selection, selectionArgs);
                }
            }
        }
        return null;
    }

    private static String getFileNameByUri(Uri uri) {
        String relativePath = getFileRelativePathByUri_API18(uri);
        if (relativePath == null) relativePath = "";
        final String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME
        };
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return relativePath + cursor.getString(index);
            }
        }
        return null;
    }

    private static String getFileRelativePathByUri_API18(Uri uri) {
        final String[] projection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projection = new String[]{
                    MediaStore.MediaColumns.RELATIVE_PATH
            };
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH);
                    return cursor.getString(index);
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isOtherDocument(Uri uri) {
        // 以/storage开头的也直接返回
        if (uri != null && uri.getPath() != null) {
            String path = uri.getPath();
            if (path.startsWith("/storage")) {
                return true;
            }
            if (path.startsWith("/external_files")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}