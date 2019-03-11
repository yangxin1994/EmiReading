package com.emi.emireading.core.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.emi.emireading.R;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.entities.FileEntity;
import com.emi.emireading.entities.FileInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author :zhoujian
 * @description : 文件工具类
 * @company :翼迈科技
 * @date: 2017年7月16日下午 02:00
 * @Email: 971613168@qq.com
 */

public class FileUtil {
    private static String TAG = "FileUtil";
    private static final String DOT = ".";
    private static final int B = 1024;
    private static final int KB = 1048576;
    private static final int MB = 1073741824;

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return 删除成功返回true，否则返回false
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


    /**
     * 读取File中的内容
     *
     * @param file 请务必保证file文件已经存在
     * @return file中的内容
     */
    public static String getText(File file) {
        if (!file.exists()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }

        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return text.toString();
    }

    /**
     * 遍历获取Log文件夹下的所有crash文件
     *
     * @param logdir 从哪个文件夹下找起
     * @return 返回crash文件列表
     */
    public static ArrayList<File> getCrashList(File logdir) {
        ArrayList<File> crashFileList = new ArrayList<>();
        findFiles(logdir.getAbsolutePath(), crashFileList);
        return crashFileList;
    }


    /**
     * 将指定文件夹中满足要求的文件存储到list集合中
     *
     * @param f
     * @param list
     */

    /**
     * 递归查找文件
     *
     * @param baseDirName 查找的文件夹路径
     * @param fileList    查找到的文件集合
     */
    public static void findFiles(String baseDirName, List<File> fileList) {
        // 创建一个File对象
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            // 判断目录是否存在
            LogUtil.e(TAG, "文件查找失败：" + baseDirName + "不是一个目录！");
        }
        String tempName;
        //判断目录是否存在
        File tempFile;
        File[] files = baseDir.listFiles();
        for (File file : files) {
            tempFile = file;
            if (tempFile.isDirectory()) {
                findFiles(tempFile.getAbsolutePath(), fileList);
            } else if (tempFile.isFile()) {
                tempName = tempFile.getName();
                if (tempName.contains("Crash")) {
                    // 匹配成功，将文件名添加到结果集
                    fileList.add(tempFile.getAbsoluteFile());
                }
            }
        }
    }


    public static File createFile(File zipdir, File zipfile) {
        if (!zipdir.exists()) {
            boolean result = zipdir.mkdirs();
            LogUtil.d("TAG", "zipdir.mkdirs() = " + result);
        }
        if (!zipfile.exists()) {
            try {
                boolean result = zipfile.createNewFile();
                LogUtil.d("TAG", "zipdir.createNewFile() = " + result);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", e.getMessage());
            }
        }
        return zipfile;
    }


    public static ArrayList<FileEntity> getFilePathListFromDir(String path) {
        ArrayList<FileEntity> fileEntityList = new ArrayList<>();
        File file = new File(path);
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory()) {
                        FileEntity fileEntity = new FileEntity();
                        fileEntity.filePath = files[i].getPath();
                        fileEntity.fileName = files[i].getName();
                        fileEntityList.add(fileEntity);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }
        return fileEntityList;
    }

    public static ArrayList<FileEntity> scanFile(String folderPath) {
        ArrayList<FileEntity> fileEntityList = new ArrayList<>();
        File file;
        try {
            file = new File(folderPath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (!f.isDirectory()) {
                        FileEntity fileEntity = new FileEntity();
                        fileEntity.fileName = f.getName();
                        fileEntity.filePath = f.getPath();
                        fileEntityList.add(fileEntity);
                    }
                    scanFile((f.getPath()));
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }
        return fileEntityList;
    }

    public static String clearFileSuffix(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        if (fileName.contains(DOT)) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        LogUtil.d(TAG, fileName);
        return fileName;
    }


    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.icon_file_type_txt
     * @param newPath String 复制后路径 如：f:/fqf.icon_file_type_txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        boolean flag = false;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            //文件存在时
            if (oldfile.exists()) {
                //读入原文件
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    //字节数 文件大小
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                flag = true;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "拷贝异常：" + e.toString());
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }


    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                File f = new File(files[i].getAbsolutePath());
                f.delete();
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag)
            return false;
        //删除当前空目录

        return true;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return deleteFile(file);
    }

    public static boolean deleteFile(File file) {
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 从assets路径下读取对应文件转String输出
     *
     * @param mContext
     * @return
     */
    public static String getAssetsJson(Context mContext, String fileName) {
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String next;
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("getJson()异常：" + e.toString());
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }


    public static String getFileSuffix(String filePath) {
        LogUtil.d(TAG, filePath);
        if (filePath != null) {
            if (filePath.contains(DOT)) {
                return filePath.substring(filePath.lastIndexOf(DOT)).toLowerCase();
            } else {
                return "";
            }
        }
        return "";
    }

    public static String getFileName(String filePath, boolean keepSuffix) {
        // 判空操作必须要有 , 处理方式不唯一 , 根据实际情况可选其一 。
        if (filePath == null) {
            return "";
        }
        int start = filePath.lastIndexOf("/");
        int end = filePath.lastIndexOf(".");
        if (start != -1 && end != -1) {
            if (keepSuffix) {
                return filePath.substring(start + 1, filePath.length());
            } else {
                return filePath.substring(start + 1, end);
            }
        } else {
            return filePath;
        }
    }


    public static boolean isTxtFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile() && EmiConstants.SUFFIX_TXT.contains(getFileSuffix(filePath))) {
            return true;
        }
        return false;
    }


    public static boolean checkFileExsit(String filePath) {
        File file = new File(filePath);
        LogUtil.d("filePath = " + filePath);
        if (file.exists()) {
            LogUtil.i(TAG, "该文件存在");
        } else {
            LogUtil.e(TAG, "该文件不存在");
        }
        return file.exists();
    }

    /**
     * 获取某一文件目录下所有文件名(没有用递归，只获取第一层路径文件名)
     *
     * @param path
     * @return
     */
    public static List<String> getFileNameListByPath(String path) {
        List<String> fileNameList = new ArrayList<>();
        if (TextUtils.isEmpty(path)) {
            return fileNameList;
        }
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] fileArray = file.listFiles();
            for (File currentFile : fileArray) {
                fileNameList.add(currentFile.getName());
            }
        }
        return fileNameList;
    }


    public static String getTxtFileContent(String filePath) {
        StringBuilder stringBuilder = new StringBuilder("");
        FileInputStream fis;
        InputStreamReader inputReader;
        BufferedReader bufferedReader;
        try {
            fis = new FileInputStream(filePath);
            inputReader = new InputStreamReader(fis, "gbk");
            bufferedReader = new BufferedReader(inputReader);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineTxt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static List<FileInfo> getFileInfoListFromFileArray(File[] files) {
        List<FileInfo> fileInfoList = new ArrayList<>();
        if (files == null) {
            return fileInfoList;
        }
        for (File file : files) {
            fileInfoList.add(createFileInfoFromFile(file));
        }
        Collections.sort(fileInfoList, new FileNameComparator());
        return fileInfoList;
    }


    /**
     * 根据文件名进行比较排序
     */
    public static class FileNameComparator implements Comparator<FileInfo> {
        private final static int
                FIRST = -1,
                SECOND = 1;

        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            if (lhs.directory || rhs.directory) {
                if (lhs.isDirectory() == rhs.isDirectory()) {
                    return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
                } else if (lhs.isDirectory()) {
                    return FIRST;
                } else {
                    return SECOND;
                }
            }
            return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
        }
    }


    public static FileInfo createFileInfoFromFile(File file) {
        FileInfo fileInfo = new FileInfo();
        if (file == null) {
            return fileInfo;
        }
        fileInfo.fileName = file.getName();
        fileInfo.filePath = file.getPath();
        fileInfo.fileSize = file.length();
        fileInfo.directory = file.isDirectory();
        fileInfo.date = FileUtil.getFileLastModifiedTime(file);
        fileInfo.suffix = getSuffix(fileInfo.fileName);
        return fileInfo;
    }

    /**
     * 读取文件的最后修改时间的方法
     */
    public static String getFileLastModifiedTime(File f) {
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }

    /**
     * 获取文件扩展名
     *
     * @return
     */
    public static String getSuffix(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return filename.substring(index + 1);
    }


    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < B) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < KB) {
            fileSizeString = df.format((double) fileS / B) + "KB";
        } else if (fileS < MB) {
            fileSizeString = df.format((double) fileS / KB) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / MB) + "GB";
        }
        return fileSizeString;
    }


    public static int getFileTypeImageId(String fileName) {
        int id;
        if (checkSuffix(fileName, new String[]{"mp3"})) {
            id = R.drawable.rc_ad_list_audio_icon;
        } else if (checkSuffix(fileName, new String[]{"wmv", "rmvb", "avi", "mp4"})) {
            id = R.drawable.rc_ad_list_video_icon;
        } else if (checkSuffix(fileName, new String[]{"wav", "aac", "amr"})) {
            id = R.drawable.rc_ad_list_video_icon;
        } else if (checkSuffix(fileName, new String[]{"xls"})) {
            id = R.mipmap.icon_file_type_excel;
        } else if (checkSuffix(fileName, new String[]{"xlsx"})) {
            id = R.mipmap.icon_file_type_excel_2007;
        } else if (checkSuffix(fileName, new String[]{"txt"})) {
            id = R.mipmap.icon_file_type_txt;
        } else {
            id = R.drawable.rc_ad_list_other_icon;
        }
        return id;
    }

    private static boolean checkSuffix(String fileName,
                                       String[] fileSuffix) {
        for (String suffix : fileSuffix) {
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 文件过滤,将手机中隐藏的文件给过滤掉
     */
    public static File[] fileFilter(File file) {
        if (file == null) {
            return null;
        }
        return file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });

    }
}
