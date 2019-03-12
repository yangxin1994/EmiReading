package com.emi.emireading.ui.load;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.entities.DataFileBean;
import com.emi.emireading.entities.DataFileFormat;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :zhoujian
 * @description : 加载并保存excel文件
 * @company :翼迈科技
 * @date 2018年02月24日上午 09:55
 * @Email: 971613168@qq.com
 */

public class LoadSaveExcelStrategy {
    private DataFileFormat dataFileFormat;
    private DataFileBean dataFileBean;
    private ArrayList<String> tableNameList;
    private ArrayList<String> cellValueList;
    private ArrayList<Integer> typeList;
    private OnProgressListener mOnProgressListener;
    private Handler mHandler;
    /**
     * 总行数
     */

    private int totalRows = 0;

    /**
     * 总列数
     */

    private int totalCells = 0;

    /**
     * 错误信息
     */

    private String errorInfo;

    private List<String> rowList;
    private String mLoadStrategyJson = "";

    /**
     * 构造方法
     */

    public LoadSaveExcelStrategy() {
        dataFileFormat = new DataFileFormat();
        dataFileBean = new DataFileBean();
        cellValueList = new ArrayList<>();
        typeList = new ArrayList<>();
        rowList = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper());
        if (EmiConfig.loadStrategy != null && !TextUtils.isEmpty(EmiConfig.loadStrategy.getCity())) {
            mLoadStrategyJson = JSON.toJSONString(EmiConfig.loadStrategy);
        }
    }


    public int getTotalRows() {
        return totalRows;
    }

    /**
     * @描述：得到总列数
     * @参数：@return
     * @返回值：int
     */

    public int getTotalCells() {
        return totalCells;
    }

    /**
     * @描述：得到错误信息
     * @参数：@return
     * @返回值：String
     */

    public String getErrorInfo() {
        return errorInfo;
    }

    /**
     * @描述：验证excel文件
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */

    public boolean validateExcel(String filePath) {
        /** 检查文件名是否为空或者是否是Excel格式的文件 */
        if (filePath == null) {
            errorInfo = "文件名为空";
            return false;
        }
        if (!(isExcel2003(filePath) || isExcel2007(filePath))) {
            errorInfo = "文件名不是excel格式";
            return false;
        }

        /** 检查文件是否存在 */

        File file = new File(filePath);
        if (!file.exists()) {
            errorInfo = "文件不存在";
            return false;
        }
        return true;
    }

    /**
     * @描述：根据文件名读取excel文件
     * @参数：@param filePath 文件完整路径
     * @参数：@return
     * @返回值：List
     */

    private List<List<String>> doReadExcel(String filePath, String fileName) {
        List<List<String>> dataList = new ArrayList<>();
        LogUtil.e("文件名：" + fileName + "路径：" + filePath);
        InputStream is = null;
        try {
            /** 验证文件是否合法 */
            if (!validateExcel(filePath)) {
                return null;
            }
            /** 判断文件的类型，是2003还是2007 */
            boolean isExcel2003 = true;
            if (isExcel2007(filePath)) {
                isExcel2003 = false;
            }
            /** 调用本类提供的根据流读取的方法 */
            File file = new File(filePath);
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            dataFileFormat.setFilePath(filePath);
            dataList = read(filePath, fileName, is, isExcel2003);
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (final NullPointerException ex) {
            ex.printStackTrace();
            LogUtil.e("readExcelFile()-->exception:" + ex.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    is = null;
                    e.printStackTrace();
                }
            }
        }

        /** 返回最后读取的结果 */

        return dataList;

    }

    /**
     * @描述：根据流读取Excel文件
     * @参数：@param inputStream
     * @参数：@param isExcel2003
     * @参数：@return
     * @返回值：List
     */

    private List<List<String>> read(String filePath, String fileName, InputStream inputStream, boolean isExcel2003) {
        List<List<String>> dataList = null;
        try {
            /** 根据版本选择创建Workbook的方式 */
            Workbook wb;
            if (isExcel2003) {
                wb = new HSSFWorkbook(inputStream);
            } else {
                System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
                System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
                System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
                wb = new XSSFWorkbook(inputStream);
            }
            dataList = read(wb, filePath, fileName);
        } catch (final Exception e) {
            e.printStackTrace();
            if (mOnProgressListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (EmiConfig.isDebug) {
                            mOnProgressListener.onError(e.toString());
                            StackTraceElement stackTraceElement = e.getStackTrace()[0];
                            System.out.println("File=" + stackTraceElement.getFileName());
                            System.out.println("Line=" + stackTraceElement.getLineNumber());
                            System.out.println("Method=" + stackTraceElement.getMethodName());
                        } else {
                            LogUtil.e("解析异常:"+e.toString());
                            mOnProgressListener.onError("解析异常");
                        }
                    }
                });
            }
        }
        return dataList;
    }

    /**
     * @描述：读取数据
     * @参数：@param Workbook
     * @参数：@return
     * @返回值：List<List<String>>
     */
    @SuppressWarnings("unchecked")
    private List<List<String>> read(Workbook wb, String filePath, final String fileName) {
        List<List<String>> dataList = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("######0");
        int progress;
        double allCount;
        double currentCount;
        UserInfo userInfo = new UserInfo();
        final ArrayList<UserInfo> userInfoArrayList = new ArrayList<>();
        //表头信息
        tableNameList = new ArrayList<>();
        /** 得到第一个shell */
        Sheet sheet = wb.getSheetAt(0);
        /** 得到Excel的行数 */
        this.totalRows = sheet.getPhysicalNumberOfRows();
        allCount = totalRows - 1;
        LogUtil.i("行数=" + totalRows);
        /** 得到Excel的列数 */
        if (this.totalRows >= 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        if (isExist(filePath)) {
            //如果数据库存在相同路径数据则先删除原数据
            deleteExistData(filePath);
        }

        /** 循环Excel的行 */
        for (int r = 0; r < this.totalRows; r++) {
            Row row = sheet.getRow(r);
            currentCount = r;
            if (r > 0) {
                userInfo = new UserInfo();
                userInfo.state = EmiConstants.STATE_NO_READ;
            }
            progress = Integer.parseInt(decimalFormat.format((currentCount / allCount) * 100));
            if (mOnProgressListener != null) {
                mOnProgressListener.onProgress(progress);
            }
            if (row == null) {
                continue;
            }
            rowList.clear();
            cellValueList.clear();
            typeList.clear();
            /** 循环Excel的列 */
            dataFileBean.setFilePath(filePath);
            dataFileBean.setFileName(fileName);
            for (int c = 0; c < this.getTotalCells(); c++) {
                Cell cell = row.getCell(c);
                String cellValue = "";
                int cellType = 1;
                if (null != cell) {
                    // 以下是判断数据的类型
                    switch (cell.getCellType()) {
                        // 数字
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            cellValue = cell.getNumericCellValue() + "";
                            break;
                        // 字符串
                        case HSSFCell.CELL_TYPE_STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        // 布尔
                        case HSSFCell.CELL_TYPE_BOOLEAN:
                            cellValue = cell.getBooleanCellValue() + "";
                            break;
                        // 公式
                        case HSSFCell.CELL_TYPE_FORMULA:
                            cellValue = cell.getCellFormula() + "";
                            break;
                        // 空值
                        case HSSFCell.CELL_TYPE_BLANK:
                            cellValue = "";
                            break;
                        // 错误
                        case HSSFCell.CELL_TYPE_ERROR:
                            cellValue = "非法字符";
                            break;
                        default:
                            cellValue = "未知类型";
                            break;
                    }
                    cellType = cell.getCellType();
                    if (row.getRowNum() > 0) {
                        loadUserInfo(userInfo, cell, cellValue, fileName, filePath);
                    }
                }
                rowList.add(cellValue);
                cellValueList.add(cellValue);
                typeList.add(cellType);
                dataFileBean.setCellValueList(cellValueList);
                dataFileBean.setCellTypeList(typeList);
                if (row.getRowNum() == 0) {
                    tableNameList.add(cellValue);
                }

            }
            /** 保存第r行的第c列 */
            if (row.getRowNum() > 0) {
                userInfoArrayList.add(userInfo);
            }
            dataList.add(rowList);
            if (row.getRowNum() > 0 && EmiConfig.IS_KEEP_SAME) {
                dataFileBean.save();
            }
        }
        if (EmiConfig.IS_KEEP_SAME) {
            dataFileFormat.setTableNameList(tableNameList);
            dataFileFormat.save();
        }
        if (mOnProgressListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnProgressListener.onFinish(userInfoArrayList, fileName);
                }
            });
        }
        return dataList;
    }


    /**
     * @描述：是否是2003的excel，返回true是2003
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */

    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    /**
     * @描述：是否是2007的excel，返回true是2007
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */

    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

    private boolean isExist(String filePath) {
        return LitePal.where("filePath = ?", filePath).find(DataFileBean.class).size() > 0;
    }

    private void deleteExistData(String filePath) {
        LitePal.deleteAll(DataFileBean.class, "filePath = ?", filePath);
        LitePal.deleteAll(DataFileFormat.class, "filePath = ?", filePath);
    }


    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.mOnProgressListener = onProgressListener;
    }

    private void loadUserInfo(UserInfo userInfo, Cell cell, String value, String fileName, String filePath) {
        int index = cell.getColumnIndex();
        boolean isString = cell.getCellType() == Cell.CELL_TYPE_STRING;
        boolean isNumber = cell.getCellType() == Cell.CELL_TYPE_NUMERIC;
        String[] mergeInfo;
        userInfo.fileType = EmiConstants.FILE_TYPE_EXCEL;
        userInfo.filename = fileName;
        userInfo.dirname = userInfo.filename;
        userInfo.filePath = filePath;
        userInfo.channel = "";
        userInfo.curreaddate = "";
        userInfo.channelAddress = "";
        userInfo.loadStrategyJson = mLoadStrategyJson;
        if (EmiConfig.IS_MERGE) {
            if (EmiConfig.MERGE_INFO_INDEX == index) {
                mergeInfo = value.split(EmiConfig.SPLIT_CHAR);
                userInfo.channelNumber = mergeInfo[EmiConfig.CHANNEL_INDEX];
                userInfo.meteraddr = EmiStringUtil.clearFirstZero(mergeInfo[EmiConfig.METER_ID_INDEX]);
                if (EmiConstants.FIRM_CODE_3378.equals(mergeInfo[EmiConfig.FIRM_CODE_INDEX])) {
                    userInfo.firmCode = EmiConstants.FIRM_CODE_7833;
                } else if (EmiConstants.FIRM_CODE_0110.equals(mergeInfo[EmiConfig.FIRM_CODE_INDEX])) {
                    userInfo.firmCode = EmiConstants.FIRM_CODE_1001;
                } else {
                    if(!TextUtils.isEmpty(mergeInfo[EmiConfig.FIRM_CODE_INDEX])){
                        userInfo.firmCode = mergeInfo[EmiConfig.FIRM_CODE_INDEX];
                    }
                }
            }
        } else {
            if (EmiConfig.METER_ID_INDEX == index) {
                userInfo.meteraddr = EmiStringUtil.handleString(value);
                userInfo.meteraddr = EmiStringUtil.clearFirstZero(userInfo.meteraddr);
            } else if (EmiConfig.CHANNEL_INDEX == index) {
                userInfo.channelNumber = EmiStringUtil.handleString(value);
                userInfo.channelNumber = EmiStringUtil.clearFirstZero(userInfo.channelNumber);
            } else if (EmiConfig.FIRM_CODE_INDEX == index) {
                if (EmiConstants.FIRM_CODE_0110.equals(value)) {
                    userInfo.firmCode = EmiConstants.FIRM_CODE_1001;
                } else if (EmiConstants.FIRM_CODE_3378.equals(value)) {
                    userInfo.firmCode = EmiConstants.FIRM_CODE_7833;
                } else {
                    if(!TextUtils.isEmpty(value)){
                        userInfo.firmCode = EmiUtils.subZeroAndDot(value);
                    }
                }
            }
        }
        if (EmiConfig.USER_ID_INDEX == index) {
            userInfo.accountnum = EmiStringUtil.handleString(value);
        }
        if (EmiConfig.WATER_ID_INDEX == index) {
            userInfo.waterId = EmiStringUtil.handleString(value);
        }
        if (EmiConfig.USER_NAME_INDEX == index) {
            if (isString) {
                userInfo.username = value;
            }
            if (isNumber) {
                userInfo.username = String.valueOf((int) Double.parseDouble(value));
            }
        }
        if (EmiConfig.METER_ID_INDEX == index) {
            userInfo.meteraddr = EmiStringUtil.handleString(value);
            userInfo.meteraddr = EmiStringUtil.clearFirstZero(userInfo.meteraddr);
        }
        if (EmiConfig.USER_ADDRESS_INDEX == index) {
            userInfo.useraddr = value;
        } else if (EmiConfig.CHANNEL_ADDRESS_INDEX == index) {
            userInfo.channelAddress = value;
        } else if (EmiConfig.LAST_USAGE_INDEX == index) {
            if (!value.contains(EmiConstants.DOT)) {
                try {
                    userInfo.lastyl = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    userInfo.lastyl = 0;
                }
            } else {
                try {
                    userInfo.lastyl = (int) Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    userInfo.lastyl = 0;
                }
            }
        } else if (EmiConfig.LAST_READING_INDEX == index) {
            if (!value.contains(EmiConstants.DOT)) {
                if (!value.isEmpty()) {
                    try {
                        userInfo.lastdata = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        userInfo.lastdata = 0;
                    }
                }
            } else {
                try {
                    userInfo.lastdata = (int) Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    userInfo.lastdata = 0;
                }
            }
        }
        if (EmiStringUtil.isEmpty(userInfo.waterId)) {
            userInfo.waterId = userInfo.accountnum;
        }
        if (EmiConfig.PEOPLE_RECORDING_INDEX == index) {
            if (TextUtils.isEmpty(value)) {
                userInfo.state = EmiConstants.STATE_NO_READ;
            } else {
                userInfo.state = EmiConstants.STATE_PEOPLE_RECORDING;
                userInfo.curdata = EmiStringUtil.stringConvertInt(value);
                if (userInfo.curdata < 0) {
                    userInfo.state = EmiConstants.STATE_FAILED;
                } else {
                    userInfo.channel = userInfo.channelNumber;
                }
                userInfo.curyl = userInfo.curdata - userInfo.lastdata;
                userInfo.curreaddate = TimeUtil.getCurrentTime();
            }
        }
    }


    public void readExcel(final String path, final String fileName) {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    doReadExcel(path, fileName);
                } catch (final Exception e) {
                    if (mOnProgressListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnProgressListener.onError(e.toString());
                            }
                        });
                    }
                }
            }
        });
    }


}

