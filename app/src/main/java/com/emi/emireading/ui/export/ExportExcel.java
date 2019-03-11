package com.emi.emireading.ui.export;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.R;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.config.EmiExportConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.entities.DataFileBean;
import com.emi.emireading.entities.DataFileFormat;
import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.JSON_EXPORT_ALIAS;
import static com.emi.emireading.core.config.EmiConstants.JSON_TABLE_NAME_FILE_DEFAULT_NAME;
import static com.emi.emireading.core.config.EmiConstants.KEY_CHANNEL_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.KEY_CURRENT_DATA;
import static com.emi.emireading.core.config.EmiConstants.KEY_CURRENT_USAGE;
import static com.emi.emireading.core.config.EmiConstants.KEY_FIRM_CODE;
import static com.emi.emireading.core.config.EmiConstants.KEY_LAST_DATA;
import static com.emi.emireading.core.config.EmiConstants.KEY_LAST_USAGE;
import static com.emi.emireading.core.config.EmiConstants.KEY_METER_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_READ_DATE;
import static com.emi.emireading.core.config.EmiConstants.KEY_READ_STATE;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_ID;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_NAME;
import static com.emi.emireading.core.config.EmiConstants.KEY_WATER_ID;
import static com.emi.emireading.core.config.EmiConstants.SHEET_NAME;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;

/**
 * @author :zhoujian
 * @description : 导出excel
 * @company :翼迈科技
 * @date 2018年02月27日下午 02:33
 * @Email: 971613168@qq.com
 */

public class ExportExcel {
    private OnProgressListener mOnProgressListener;
    private String mFileName;
    private String originalFilePath;
    private ExportStrategy exportStrategy;
    private String currentExportStrategyName;
    private ArrayList<String> valueList = new ArrayList<>(13);
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isShowPeopleRecord;
    /**
     * 本次读数的索引
     */
    private int currentDataIndex;

    public List<UserInfo> getDataList() {
        return mDataList;
    }

    private int lastDataIndex;
    private int lastUsageIndex;
    private int currentUsageIndex;
    private Context context;

    public void setDataList(List<UserInfo> mDataList) {
        this.mDataList = mDataList;
    }

    private List<UserInfo> mDataList;
    /**
     * 表头名称集合
     */
    private ArrayList<String> tableNameList = new ArrayList<>();

    private ArrayList<String> sortList = new ArrayList<>();


    public ExportExcel(ExportStrategy exportStrategy, String exportFilePath, List<UserInfo> dataList) {
        this.context = EmiReadingApplication.getAppContext();
        this.exportStrategy = exportStrategy;
        this.mFileName = EmiStringUtil.getFileNameWithSuffix(originalFilePath);
        this.isShowPeopleRecord = EmiUtils.isShowPeopleRecord();
        LogUtil.i("获取的文件名：" + mFileName);
        this.exportFilePath = exportFilePath;
        if (!dataList.isEmpty()) {
            this.originalFilePath = dataList.get(0).filePath;
        }
        this.currentExportStrategyName = exportStrategy.cityName;
        this.mDataList = dataList;
    }


    private String exportFilePath;


    public String getFilePath() {

        return originalFilePath;
    }

    public void setFilePath(String filePath) {
        this.originalFilePath = filePath;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }


    public boolean exportExcel() {
        if (originalFilePath != null && TextUtils.isDigitsOnly(mFileName)) {
            //从数据库找出对应的所有数据
            LogUtil.d("文件路径：" + originalFilePath);
            List<DataFileBean> dataFileBeanList = LitePal.where("filePath = ?", originalFilePath).find(DataFileBean.class);
            List<DataFileFormat> dataFileFormats = LitePal.where("filePath = ?", originalFilePath).find(DataFileFormat.class);
            if (dataFileBeanList.size() > 0) {
                boolean isCorrect = checkDataCorrect(originalFilePath, dataFileBeanList.get(0));
                if (isCorrect) {
                    //导出excel文件
                    exportOriginal(exportFilePath, dataFileBeanList, dataFileFormats.get(0).getTableNameList());
                }
                return true;
            } else {
                LogUtil.e("数据库中无数据");
                return false;
            }
        } else {
            LogUtil.e("路径为空");
            return false;
        }
    }

    private boolean checkDataCorrect(String path, DataFileBean dataFileBean) {
        List<DataFileFormat> dataFileFormats = LitePal.where("filePath = ?", path).find(DataFileFormat.class);
        if (dataFileFormats.size() == 1) {
            return dataFileFormats.get(0).getTableNameList().size() == dataFileBean.getCellValueList().size();
        } else {
            LogUtil.e("dataFileFormats.size()=" + dataFileFormats.size());
            return false;
        }
    }

    private void exportOriginal(String filePath, List<DataFileBean> dataFileBeanList, List<String> tableNameList) {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet(SHEET_NAME);
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        //创建表头
        HSSFRow tableRow = sheet.createRow(0);
        HSSFRow row;
        HSSFCell cell;
        List<String> cellValueList;
        List<Integer> cellTypeList;
        DataFileBean dataFileBean;
        for (int i = 0; i < tableNameList.size(); i++) {
            HSSFCell tableCell = tableRow.createCell(i);
            tableCell.setCellType(CellType.STRING);
            tableCell.setCellValue(tableNameList.get(i));
        }

        for (int i = 0; i < dataFileBeanList.size(); i++) {
            dataFileBean = dataFileBeanList.get(i);
            row = sheet.createRow(i + 1);
            cellValueList = dataFileBean.getCellValueList();
            cellTypeList = dataFileBean.getCellTypeList();
            for (int j = 0; j < cellValueList.size(); j++) {
                cell = row.createCell(j);
                cell.setCellValue(cellValueList.get(j));
                cell.setCellValue(cellValueList.get(j));
                cell.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            wb.write(fos);
            wb.close();
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义导出
     *
     * @param
     */
    public void userDefinedExport() {
        getExcelMould();
        if (mDataList != null) {
            exportByDefined(exportFilePath);
        } else {
            if (mOnProgressListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnProgressListener.onError("未获取到数据源！");
                    }
                });
            }
        }
    }

    /**
     * 获取模板
     */
    private void getExcelMould() {
        JSONObject jsonObject = JSONObject.parseObject(exportStrategy.getExportFormateJson());
        sortList.clear();
        String key;
        for (int i = 0; i < jsonObject.size(); i++) {
            key = jsonObject.getString(i + 1 + "");
            LogUtil.w("取到的值：" + key);
            if (KEY_CURRENT_DATA.equals(key)) {
                currentDataIndex = i;
            }
            if (KEY_LAST_DATA.equals(key)) {
                lastDataIndex = i;
            }
            if (EmiConstants.KEY_CURRENT_USAGE.equals(key)) {
                currentUsageIndex = i;
            }
            if (KEY_LAST_USAGE.equals(key)) {
                lastUsageIndex = i;
            }
            sortList.add(key);
        }
        getTableList();
    }

    /**
     * 加载列数
     *
     * @param userInfo
     */
    private void loadColumn(UserInfo userInfo) {
        boolean isLujiang = "庐江".equals(currentExportStrategyName);
        valueList.clear();
        StringBuilder sb = new StringBuilder("");
        for (String filedName : sortList) {
            //用户编号
            if (KEY_USER_ID.equals(filedName)) {
                valueList.add(userInfo.accountnum);
            }
            //用户名
            if (KEY_USER_NAME.equals(filedName)) {
                valueList.add(userInfo.username);
            }
            //用户地址
            if (KEY_USER_ADDRESS.equals(filedName)) {
                valueList.add(String.valueOf(userInfo.useraddr));
            }
            //用水id
            if (KEY_WATER_ID.equals(filedName)) {
                if (isLujiang) {
                    valueList.add("");
                } else {
                    valueList.add(userInfo.waterId);
                }
            }
            //上次读数
            if (KEY_LAST_DATA.equals(filedName)) {
                if (EmiExportConstants.IS_DOUBLE) {
                    valueList.add(convertDecimal(sb, String.valueOf(userInfo.lastdata)));
                } else {
                    valueList.add(String.valueOf(userInfo.lastdata));
                }
            }
            //上次用水量
            if (KEY_LAST_USAGE.equals(filedName)) {
                if (EmiExportConstants.IS_DOUBLE) {
                    valueList.add(convertDecimal(sb, String.valueOf(userInfo.lastyl)));
                } else {
                    valueList.add(String.valueOf(userInfo.lastyl));
                }
            }
            //本次读数
            if (KEY_CURRENT_DATA.equals(filedName)) {
                if (userInfo.state == STATE_FAILED || userInfo.state == STATE_NO_READ) {
                    valueList.add(EmiConstants.ERROR_METER_DATA);
                } else {
                    if (EmiExportConstants.IS_DOUBLE) {
                        valueList.add(convertDecimal(sb, String.valueOf(userInfo.curdata)));
                    } else {
                        valueList.add(String.valueOf(userInfo.curdata));
                    }
                }
            }
            //本次用水量
            if (KEY_CURRENT_USAGE.equals(filedName)) {
                if (userInfo.state == STATE_NO_READ || userInfo.state == STATE_FAILED) {
                    valueList.add(EmiConstants.ERROR_METER_DATA);
                } else {
                    if (EmiExportConstants.IS_DOUBLE) {
                        valueList.add(convertDecimal(sb, String.valueOf(userInfo.curyl)));
                    } else {
                        valueList.add(String.valueOf(userInfo.curyl));
                    }
                }

            }
            //抄表日期
            if (KEY_READ_DATE.equals(filedName)) {
                if (!TextUtils.isEmpty(userInfo.curreaddate)) {
                    if (isLujiang) {
                        valueList.add("");
                    } else {
                        valueList.add(TimeUtil.getTimeString(userInfo.curreaddate));
                    }
                    LogUtil.i("当前日期：" + TimeUtil.getTimeString(userInfo.curreaddate));
                } else {
                    valueList.add("");
                }
            }
            //通道板号
            if (KEY_CHANNEL_NUMBER.equals(filedName)) {
                valueList.add(userInfo.channelNumber);
            }
            //通道板地址
            if (KEY_CHANNEL_ADDRESS.equals(filedName)) {
                if (isLujiang) {
                    valueList.add("");
                } else {
                    valueList.add(userInfo.channelAddress);
                }
            }
            //表地址
            if (KEY_METER_ADDRESS.equals(filedName)) {
                valueList.add(userInfo.meteraddr);
            }
            //厂商代码
            if (KEY_FIRM_CODE.equals(filedName)) {
                if (isLujiang) {
                    valueList.add("");
                } else {
                    valueList.add(userInfo.firmCode);
                }
            }
            if (KEY_READ_STATE.equals(filedName)) {
                switch (userInfo.state) {
                    case STATE_SUCCESS:
                        if (isLujiang) {
                            valueList.add("1");
                        } else {
                            valueList.add(context.getString(R.string.state_success));
                        }
                        break;
                    case STATE_NO_READ:
                        if (isLujiang) {
                            valueList.add("5");
                        } else {
                            valueList.add(context.getString(R.string.state_no_read));
                        }
                        break;
                    case STATE_PEOPLE_RECORDING:
                        if (isLujiang) {
                            valueList.add("1");
                        } else {
                            if (isShowPeopleRecord) {
                                valueList.add(context.getString(R.string.state_people_record));
                            } else {
                                valueList.add(context.getString(R.string.state_success));
                            }
                        }
                        break;
                    case STATE_WARNING:
                        if (isLujiang) {
                            valueList.add("1");
                        } else {
                            valueList.add(context.getString(R.string.state_success));
                        }
                        break;
                    case STATE_FAILED:
                        if (isLujiang) {
                            valueList.add("3");
                        } else {
                            valueList.add(context.getString(R.string.state_failed));
                        }
                        break;
                    default:
                        if (isLujiang) {
                            valueList.add("5");
                        } else {
                            valueList.add(context.getString(R.string.state_no_read));
                        }
                        break;
                }
            }
        }
    }

    /**
     * 自定义格式导出
     *
     * @param exportFilePath
     */
    @SuppressWarnings("unchecked")
    private void exportByDefined(String exportFilePath) {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet(SHEET_NAME);
        int maxWidthSize = 33792;
        int currentWidthSize;
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        //创建表头
        try {
            DecimalFormat decimalFormat = new DecimalFormat("######0");
            int progress;
            double allCount;
            double currentCount;
            HSSFRow tableRow = sheet.createRow(0);
            //每一列宽度集合
            ArrayList<Integer> columnSizeList = new ArrayList<>();
            HSSFCellStyle cellStyle = null;
            if (EmiExportConstants.IS_NUMBER) {
                cellStyle = wb.createCellStyle();
                HSSFDataFormat format = wb.createDataFormat();
                //设置数字类型
                cellStyle.setDataFormat(format.getFormat("0.0"));
            }
            HSSFRow row;
            HSSFCell cell;
            String value;
            int valueLength = 200;
            int tableLength = 200;
            String currentValue;
            String currentTableValue = null;
            for (int i = 0; i < tableNameList.size(); i++) {
                HSSFCell tableCell = tableRow.createCell(i);
                tableCell.setCellType(CellType.STRING);
                tableCell.setCellValue(tableNameList.get(i));
            }
            allCount = mDataList.size();
            for (int i = 0; i < mDataList.size(); i++) {
                row = sheet.createRow(i + 1);
                currentCount = i + 1;
                progress = Integer.parseInt(decimalFormat.format((currentCount / allCount) * 100));
                loadColumn(mDataList.get(i));
                if (columnSizeList.size() < valueList.size()) {
                    for (int k = 0; k < valueList.size(); k++) {
                        //比较表头长度和内容长度,哪个长就取哪个长度
                        currentValue = valueList.get(k);
                        if (tableNameList.size() > k) {
                            currentTableValue = tableNameList.get(k);
                        }
                        if (!TextUtils.isEmpty(currentValue)) {
                            valueLength = valueList.get(k).getBytes().length;
                        }
                        if (!TextUtils.isEmpty(currentTableValue)) {
                            tableLength = tableNameList.get(k).getBytes().length;
                        }
                        if (valueLength > 255) {
                            valueLength = 200;
                        }
                        if (tableLength > 255) {
                            tableLength = 200;
                        }
                        if (StringUtils.isNumeric(valueList.get(k))) {
                            valueLength = valueLength * 2;
                        }
                        if (valueLength > tableLength) {
                            columnSizeList.add(valueLength * 256);
                        } else {
                            columnSizeList.add(tableLength * 256);
                        }
                    }
                }
                for (int j = 0; j < valueList.size(); j++) {
                    value = valueList.get(j);
                    cell = row.createCell(j);
                    if (EmiExportConstants.IS_NUMBER && StringUtils.isNumeric(value)) {
                        if (j == currentDataIndex || j == currentUsageIndex || j == lastDataIndex || j == lastUsageIndex) {
                            cell.setCellType(CellType.NUMERIC);
                            if (EmiExportConstants.IS_DOUBLE) {
                                cell.setCellStyle(cellStyle);
                                cell.setCellValue(Double.parseDouble(value));
                            } else {
                                cell.setCellValue(Integer.parseInt(value));
                            }
                        } else {
                            cell.setCellType(CellType.STRING);
                            cell.setCellValue(value);
                        }
                    } else {
                        cell.setCellType(CellType.STRING);
                        cell.setCellValue(value);
                    }
                    currentWidthSize = columnSizeList.get(j);
                    if (currentWidthSize >= maxWidthSize) {
                        sheet.setColumnWidth(j, maxWidthSize);
                    } else {
                        sheet.setColumnWidth(j, currentWidthSize);
                    }
                }
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(progress);
                }
            }
            FileOutputStream fos = new FileOutputStream(exportFilePath);
            wb.write(fos);
            fos.close();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnProgressListener != null) {
                        mOnProgressListener.onFinish(mDataList, mFileName);
                    }
                }
            });

        } catch (final IOException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnProgressListener != null) {
                        mOnProgressListener.onError(e.toString());
                    }
                }
            });
        }
    }

    /**
     * 获取表头信息
     */
    private void getTableList() {
        tableNameList.clear();
        String tableName = FileUtil.getAssetsJson(EmiReadingApplication.getAppContext(), JSON_EXPORT_ALIAS);
        String defaultTableName = FileUtil.getAssetsJson(EmiReadingApplication.getAppContext(), JSON_TABLE_NAME_FILE_DEFAULT_NAME);
        JSONObject tableNameObject = JSONObject.parseObject(tableName);
        JSONObject defaultableNameObject = JSONObject.parseObject(defaultTableName);
        if ("庐江".equals(currentExportStrategyName)) {
            exportStrategy.setTableNameJson(tableNameObject.toJSONString());
        }
        if (!TextUtils.isEmpty(tableName)) {
            if (exportStrategy.tableNameJson != null && EmiStringUtil.isJSONValid(exportStrategy.tableNameJson)) {
                JSONObject jsonObject = JSONObject.parseObject(exportStrategy.getTableNameJson());
                String userDefinedName;
                for (String tableNameKey : sortList) {
                    userDefinedName = jsonObject.getString(tableNameKey);
                    if (userDefinedName != null) {
                        tableNameList.add(userDefinedName);
                        LogUtil.d("解析到的名称：" + userDefinedName);
                    } else {
                        tableNameList.add(defaultableNameObject.getString(tableNameKey));
                        LogUtil.i("解析到的名称：" + defaultableNameObject.getString(tableNameKey));
                    }
                }
            } else {
                for (String tableNameKey : sortList) {
                    tableNameList.add(defaultableNameObject.getString(tableNameKey));
                    LogUtil.w("解析到的名称：" + defaultableNameObject.getString(tableNameKey));
                }
            }
        }
    }


    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.mOnProgressListener = onProgressListener;
    }


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private String convertDecimal(StringBuilder sb, String value) {
        sb.setLength(0);
        sb.append("");
        sb.append(value);
        sb.append(".0");
        return sb.toString();
    }

    public ExportExcel(ArrayList<String> tableNameList, List<UserInfo> dataList) {
        this.context = EmiReadingApplication.getAppContext();
        this.tableNameList = tableNameList;
        LogUtil.i("获取的文件名：" + mFileName);
        this.mDataList = dataList;
    }


    public String exportReaderFile() {
        if (mDataList == null || mDataList.isEmpty()) {
            onErrorCallback("数据源为空!");
            return "";
        }
        String fileName = EmiStringUtil.getFileNameWithSuffix(mDataList.get(0).getFilePath());
        if (TextUtils.isEmpty(fileName)) {
            onErrorCallback("导出路径错误");
            return "";
        }
        mFileName = fileName;
        exportFilePath = EmiConfig.NeedFilePath + File.separator + fileName;
        LogUtil.i("导出路径:" + exportFilePath);
        if (tableNameList == null || tableNameList.isEmpty()) {
            onErrorCallback("列名为空!");
            return "";
        }
        return createExcel();
    }


    private void onErrorCallback(final String errorMsg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnProgressListener != null) {
                    mOnProgressListener.onError(errorMsg);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private String createExcel() {
        // 创建工作簿对象
        try {
            int progress;
            double allCount = mDataList.size();
            double currentCount;
            int valueLength = 200;
            int tableLength = 200;
            int maxWidthSize = 33792;
            int currentWidthSize;
            //每一列宽度集合
            ArrayList<Integer> columnSizeList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("######0");
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(SHEET_NAME);
            //设置单元格样式,居中显示
            HSSFCellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);
            //创建第一行
            HSSFRow row = sheet.createRow(0);
            for (int i = 0, len = tableNameList.size(); i < len; i++) {
                //创建单元格
                HSSFCell cell = row.createCell(i);
                //指定值
                cell.setCellValue(tableNameList.get(i));
                //设置样式
                cell.setCellStyle(style);
            }
            Cell cell;
            String value;
            String currentValue;
            String currentTableValue = null;
            for (int i = 0; i < mDataList.size(); i++) {
                row = sheet.createRow(i + 1);
                currentCount = i + 1;
                progress = Integer.parseInt(decimalFormat.format((currentCount / allCount) * 100));
                loadValue(mDataList.get(i));
                if (columnSizeList.size() < valueList.size()) {
                    for (int k = 0; k < valueList.size(); k++) {
                        //比较表头长度和内容长度,哪个长就取哪个长度
                        currentValue = valueList.get(k);
                        if (tableNameList.size() > k) {
                            currentTableValue = tableNameList.get(k);
                        }
                        if (!TextUtils.isEmpty(currentValue)) {
                            valueLength = valueList.get(k).getBytes().length;
                        }
                        if (!TextUtils.isEmpty(currentTableValue)) {
                            tableLength = tableNameList.get(k).getBytes().length;
                        }
                        if (valueLength > 255) {
                            valueLength = 200;
                        }
                        if (tableLength > 255) {
                            tableLength = 200;
                        }
                        if (StringUtils.isNumeric(valueList.get(k))) {
                            valueLength = valueLength * 2;
                        }
                        if (valueLength > tableLength) {
                            columnSizeList.add(valueLength * 256);
                        } else {
                            columnSizeList.add(tableLength * 256);
                        }
                    }
                }
                for (int j = 0; j < valueList.size(); j++) {
                    value = valueList.get(j);
                    cell = row.createCell(j);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(value);
                    cell.setCellStyle(style);
                    currentWidthSize = columnSizeList.get(j);
                    if (currentWidthSize >= maxWidthSize) {
                        sheet.setColumnWidth(j, maxWidthSize);

                    } else {
                        sheet.setColumnWidth(j, currentWidthSize);
                    }
                }
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(progress);
                }
            }
            FileOutputStream fos = new FileOutputStream(exportFilePath);
            workbook.write(fos);
            fos.close();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnProgressListener != null) {
                        mOnProgressListener.onFinish(mDataList, mFileName);
                    }
                }
            });
        } catch (Exception e) {
            onErrorCallback(e.toString());
        }
        return exportFilePath;
    }


    private void loadValue(UserInfo userInfo) {
        valueList.clear();
        valueList.add(userInfo.accountnum);
        valueList.add(userInfo.username);
        valueList.add(userInfo.useraddr);
        if (userInfo.curdata <= 0 && userInfo.state == STATE_NO_READ) {
            valueList.add(String.valueOf(userInfo.lastdata));
        } else {
            valueList.add(String.valueOf(userInfo.curdata));
        }
        valueList.add(userInfo.channelNumber);
        valueList.add(userInfo.meteraddr);
        valueList.add(userInfo.firmCode);
        valueList.add("");
    }


}


