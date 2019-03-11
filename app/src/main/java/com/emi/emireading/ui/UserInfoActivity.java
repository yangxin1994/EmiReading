package com.emi.emireading.ui;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.CURRENT_ITEM_TAG_KEY;
import static com.emi.emireading.core.config.EmiConstants.EDIT_DATA_KEY;
import static com.emi.emireading.core.config.EmiConstants.EDIT_LIST_KEY;
import static com.emi.emireading.core.config.EmiConstants.STATE_NOT_UPLOAD;
import static com.emi.emireading.core.config.EmiConstants.STATE_UPLOAD_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_UPLOAD_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.UPLOAD_STATE_KEY;
import static com.emi.emireading.core.config.EmiConstants.USER_WATER_ID_KEY;


/**
 * 描述:
 * @author  chx
 * Created by chx on 2017/3/21.
 * 邮箱:snake_chenhx@163.com
 */

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private final int TAKE_PHOTO_REQUEST_CODE = 102;
    private SQLiteOpenHelper helper = null;
    private MyOperator mytab = null;
    private Button preButton = null;
    private Button saveButton = null;
    private Button returnButton = null;
    private Button afterButton = null;
    private EditText editmeterdata = null;
    private ImageView iv_goback;
    private TextView stateview = null;
    private CheckBox select = null;
    private String channelAddrStr = "通道板位置:";
    List<String> Itemshow = new ArrayList<String>();// 显示的信息
    List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private String accountId;//用户编号，即用户用水id
    private String fileName;
    private String photoName = "";
    private String userAddress;
    private int RE = 0;
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;
    private int curindex = 0;
    private ArrayAdapter<String> adapter;
    private String channel;
    private String strstate;
    private int uploadState;
    private TextView tvTakePhoto;
    private ListView listView;
    private final String TAG = "UserInfoActivity";
    private boolean queryFlag;
    private int currentIemTag;
    private ArrayList<UserInfo> userInfoList = new ArrayList<>();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        initView();
        getInfoFromBundle();
        try {
            getDetailInfo();
        }catch (IndexOutOfBoundsException e){
            ToastUtil.showShortToast("查询数据异常");
        }
        initEvents();
    }

    private void initView() {
        // 数据库操作辅助类
        this.helper = new SQLiteHelper(this);
        mytab = new MyOperator(helper.getWritableDatabase());
        listView = (ListView) findViewById(R.id.listView);
        saveButton = (Button) findViewById(R.id.bt_save);
        returnButton = (Button) findViewById(R.id.bt_back);
        editmeterdata = (EditText) findViewById(R.id.et_data);
        tvTakePhoto = (TextView) findViewById(R.id.tvTakePhoto);
        afterButton = (Button) findViewById(R.id.bt_next);
        preButton = (Button) findViewById(R.id.bt_up);
        select = (CheckBox) findViewById(R.id.checkBox);
        iv_goback = (ImageView) findViewById(R.id.iv_goback);
        tvTakePhoto.setOnClickListener(this);
        editmeterdata.setEnabled(true);
        saveButton.setEnabled(true);
        select.setChecked(true);
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);
    }

    private void getInfoFromBundle() {
        Bundle bundle = getIntent().getExtras();
        strstate = bundle.getString("state","0");
        currentIemTag = bundle.getInt(CURRENT_ITEM_TAG_KEY,-1);
        uploadState = bundle.getInt(UPLOAD_STATE_KEY);
        fileName = bundle.getString("filename","");
        channel = bundle.getString("channel");
        accountId = bundle.getString(USER_WATER_ID_KEY);
        curindex =bundle.getInt("index");


//        RE = Integer.parseInt(bundle.getString("RE"));
        LogUtil.i(TAG,"结果："+bundle.getString("RE"));
        queryFlag = bundle.getBoolean("queryFlag");
        LogUtil.i(TAG,"queryFlag结果："+queryFlag);
    }

    private void getDetailInfo() {
        List<UserInfo> userInfoList = null;
        if (channel.equals("")) {
            if(uploadState != STATE_UPLOAD_FAILED&&uploadState!=STATE_UPLOAD_SUCCESS){
                LogUtil.w(TAG,"uploadState="+uploadState);
                mytab = new MyOperator(helper.getWritableDatabase());
                int state = Integer.parseInt(strstate);
                userInfoList = mytab.find(fileName,state);
            }else {
                LogUtil.i(TAG,"uploadState="+uploadState);
                mytab = new MyOperator(helper.getWritableDatabase());
                userInfoList = mytab.findUploadData(fileName,uploadState);
            }
            if (queryFlag){
                userInfoList = EmiConfig.userInfoArrayList;
                LogUtil.w(TAG,"已拿到筛选后的集合数据");
//                userInfoList = mytab.findDataByAccountId(fileName,accountId);
            }
            HashMap<String, Object> item;
            for (UserInfo userInfo : userInfoList) {
                item = new HashMap<>();
                item.put("accountnum", userInfo.accountnum);
                item.put("username", userInfo.username);
                item.put("meteraddr", userInfo.meteraddr);
                item.put("channelAddress",userInfo.channelAddress);
                item.put("curdata", String.valueOf(userInfo.curdata));
                if (userInfo.lastdata == -1){
                    item.put("lastdata", "");
                }else {
                    item.put("lastdata", String.valueOf(userInfo.lastdata));
                }
                item.put("curyl", String.valueOf(userInfo.curyl));
                item.put("lastyl", String.valueOf(userInfo.lastyl));
                userAddress = userInfo.useraddr;
                item.put("useraddr",userAddress);
                if (userInfo.state == 1)
                    item.put("state", "正常");
                else if (userInfo.state == 2)
                    item.put("state", "水量异常");
                else if (userInfo.state == 3)
                    item.put("state", "失败");
                else if (userInfo.state == 4)
                    item.put("state", "人工补录");
                else if (userInfo.state == 5)
                    item.put("state", "未抄");
                else if (userInfo.state == 6)
                    item.put("state", "单抄");
                if (userInfo.curreaddate == null)
                    userInfo.curreaddate = "";
                item.put("curreaddate", userInfo.curreaddate);
                if (userInfo.uploadState == STATE_NOT_UPLOAD){
                    item.put("uploadState", "未上传");
                }else if (userInfo.uploadState == STATE_UPLOAD_FAILED){
                    item.put("uploadState", "上传失败");
                }else if (userInfo.uploadState == STATE_UPLOAD_SUCCESS){
                    item.put("uploadState", "上传成功");
                }
                data.add(item);
            }
        } else {
            if (RE == 0) {
                userInfoList = mytab.findchannel(channel);
                for (UserInfo userInfo : userInfoList) {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put("accountnum", userInfo.accountnum);
                    item.put("username", userInfo.username);
                    item.put("meteraddr", userInfo.meteraddr);
                    item.put("channelAddress", userInfo.channelAddress);
                    item.put("curdata", String.valueOf(userInfo.curdata));
                    if (userInfo.lastdata == -1){
                        item.put("lastdata", "");
                    }else {
                        item.put("lastdata", String.valueOf(userInfo.lastdata));
                    }
                    item.put("curyl", String.valueOf(userInfo.curyl));
                    item.put("lastyl", String.valueOf(userInfo.lastyl));
                    item.put("useraddr", userInfo.useraddr);
                    if (userInfo.state == 1)
                        item.put("state", "正常");
                    else if (userInfo.state == 2)
                        item.put("state", "水量异常");
                    else if (userInfo.state == 3)
                        item.put("state", "失败");
                    else if (userInfo.state == 4)
                        item.put("state", "人工补录");
                    else if (userInfo.state == 5)
                        item.put("state", "未抄");
                    if (userInfo.curreaddate == null)
                        userInfo.curreaddate = "";
                    item.put("curreaddate", userInfo.curreaddate);
                    if (userInfo.uploadState == STATE_NOT_UPLOAD){
                        item.put(UPLOAD_STATE_KEY, "未上传");
                    }else if (userInfo.uploadState == STATE_UPLOAD_FAILED){
                        item.put(UPLOAD_STATE_KEY, "上传失败");
                    }else if (userInfo.uploadState == STATE_UPLOAD_SUCCESS){
                        item.put(UPLOAD_STATE_KEY, "上传成功");
                    }
                    data.add(item);
                }
            } else if (RE == 1) //
            {
                userInfoList = mytab.findchannel1(channel, RE);
                for (UserInfo userInfo : userInfoList) {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put("accountnum", userInfo.accountnum);
                    item.put("username", userInfo.username);
                    item.put("meteraddr", userInfo.meteraddr);
                    item.put("channelAddress", userInfo.channelAddress);
                    item.put("curdata", String.valueOf(userInfo.curdata));
                    if (userInfo.lastdata == -1){
                        item.put("lastdata", "");
                    }else {
                        item.put("lastdata", String.valueOf(userInfo.lastdata));
                    }
                    item.put("curyl", String.valueOf(userInfo.curyl));
                    item.put("lastyl", String.valueOf(userInfo.lastyl));
                    item.put("useraddr", userInfo.useraddr);
                    if (userInfo.state == 1)
                        item.put("state", "正常");
                    else if (userInfo.state == 2)
                        item.put("state", "水量异常");
                    else if (userInfo.state == 3)
                        item.put("state", "失败");
                    else if (userInfo.state == 4)
                        item.put("state", "人工补录");
                    else if (userInfo.state == 5)
                        item.put("state", "未抄");
                    if (userInfo.curreaddate == null)
                        userInfo.curreaddate = "";
                    item.put("curreaddate", userInfo.curreaddate);
                    if (userInfo.uploadState == STATE_NOT_UPLOAD){
                        item.put(UPLOAD_STATE_KEY, "未上传");
                    }else if (userInfo.uploadState == STATE_UPLOAD_FAILED){
                        item.put(UPLOAD_STATE_KEY, "上传失败");
                    }else if (userInfo.uploadState == STATE_UPLOAD_SUCCESS){
                        item.put(UPLOAD_STATE_KEY, "上传成功");
                    }
                    data.add(item);
                }
            }
        }
        Itemshow.add("用户号：" + (String) data.get(curindex).get("accountnum"));
        Itemshow.add("用户名：" + (String) data.get(curindex).get("username"));
        Itemshow.add("用户地址：" + (String) data.get(curindex).get("useraddr"));
        Itemshow.add("表地址：" + (String) data.get(curindex).get("meteraddr"));
        Itemshow.add("上次读数：" + (String) data.get(curindex).get("lastdata"));
        Itemshow.add("上次用量：" + (String) data.get(curindex).get("lastyl"));
        Itemshow.add("本次读数：" + (String) data.get(curindex).get("curdata"));
        Itemshow.add("本次用量：" + (String) data.get(curindex).get("curyl"));
        Itemshow.add("抄表状态：" + (String) data.get(curindex).get("state"));
        if(!(EmiStringUtil.isEmpty((String) data.get(curindex).get("channelAddress")))){
            Itemshow.add( channelAddrStr+data.get(curindex).get("channelAddress"));
        }
        LogUtil.d(TAG,"通道板安装地址---->"+ data.get(curindex).get("channelAddress"));
        photoName  += (String) data.get(curindex).get("useraddr");
      /*  if (EMIConfig.CITY == CityEnum.City_HanShan){
            Itemshow.add("上传状态：" + (String) data.get(curindex).get(UPLOAD_STATE_KEY));
        }*/
        Itemshow.add("抄表时间：" + (String) data.get(curindex).get("curreaddate"));
    }


    private void initEvents() {
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Itemshow);
        listView.setAdapter(adapter);

        afterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Itemshow.clear();
                select.setChecked(true);
                clearInput();
                int length = data.size();
                if(curindex < length -1){
                if (curindex < length - 1) {
                    curindex = curindex + 1;
                }
                Itemshow.add("用户号："
                        + (String) data.get(curindex).get("accountnum"));
                Itemshow.add("用户名："
                        + (String) data.get(curindex).get("username"));
                Itemshow.add("用户地址："
                        + (String) data.get(curindex).get("useraddr"));
                Itemshow.add("表地址："
                        + (String) data.get(curindex).get("meteraddr"));
                Itemshow.add("上次读数："
                        + (String) data.get(curindex).get("lastdata"));
                Itemshow.add("上次用量："
                        + (String) data.get(curindex).get("lastyl"));
                Itemshow.add("本次读数："
                        + (String) data.get(curindex).get("curdata"));
                Itemshow.add("本次用量：" + (String) data.get(curindex).get("curyl"));
                Itemshow.add("抄表状态：" + (String) data.get(curindex).get("state"));
                    if(!(EmiStringUtil.isEmpty((String) data.get(curindex).get("channelAddress")))){
                        Itemshow.add(channelAddrStr +data.get(curindex).get("channelAddress"));
                    }
                Itemshow.add("抄表时间："
                        + (String) data.get(curindex).get("curreaddate"));
                    photoName  += (String) data.get(curindex).get("useraddr");
              /*  if (EMIConfig.CITY == CityEnum.City_HanShan){
                    Itemshow.add("上传状态：" + (String) data.get(curindex).get(UPLOAD_STATE_KEY));
                }*/
                adapter.notifyDataSetChanged();
            }else {
                    ToastUtil.showShortToast("没有下一户了");
                }
            }
        });
        iv_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        preButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInput();
                if (curindex >= 1) {
                    Itemshow.clear();
                    select.setChecked(true);
                    curindex = curindex - 1;
                    int length = data.size();
                    if (curindex >= length) {
                        curindex = 0;
                    }
                    Itemshow.add("用户号："
                            + (String) data.get(curindex).get("accountnum"));
                    Itemshow.add("用户名："
                            + (String) data.get(curindex).get("username"));
                    Itemshow.add("用户地址："
                            + (String) data.get(curindex).get("useraddr"));
                    Itemshow.add("表地址："
                            + (String) data.get(curindex).get("meteraddr"));
                    Itemshow.add("上次读数："
                            + (String) data.get(curindex).get("lastdata"));
                    Itemshow.add("上次用量："
                            + (String) data.get(curindex).get("lastyl"));
                    Itemshow.add("本次读数："
                            + (String) data.get(curindex).get("curdata"));
                    Itemshow.add("本次用量："
                            + (String) data.get(curindex).get("curyl"));
                    Itemshow.add("状态："
                            + (String) data.get(curindex).get("state"));
                    Itemshow.add("抄表时间："
                            + (String) data.get(curindex).get("curreaddate"));
                   LogUtil.d(TAG,"通道板安装地址---->"+ data.get(curindex).get("channelAddress"));
                    if(!(EmiStringUtil.isEmpty((String) data.get(curindex).get("channelAddress")))){
                        Itemshow.add(channelAddrStr +data.get(curindex).get("channelAddress"));
                    }
                    photoName  += (String) data.get(curindex).get("useraddr");
                   /* if (EMIConfig.CITY == CityEnum.City_HanShan){
                        Itemshow.add("上传状态：" + (String) data.get(curindex).get(UPLOAD_STATE_KEY));
                    }*/
                    adapter.notifyDataSetChanged();
                }else {
                    ToastUtil.showShortToast("上一户没有了");
                }
            }
        });
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editmeterdata();
            }

        });
        select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton bottonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    editmeterdata.setEnabled(true);
                    saveButton.setEnabled(true);
                } else {
                    editmeterdata.setEnabled(false);
                    saveButton.setEnabled(false);
                }
            }
        });
    }


    private void editmeterdata() {
        // TODO Auto-generated method stub
        String editbuf = editmeterdata.getText().toString();
        int a = editbuf.length();
        if (a > 0) {
            int meterdata = Integer.parseInt(editbuf);
            int state = 4;
            Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
            String curreaddate = year + "年" + month + "月" + day + "日"+hour+"时"+minute+"分"+second+"秒";
            String last = (String) (data.get(curindex).get("lastdata"));
            int i;
            try {
                i = Integer.parseInt(last);
            }catch (NumberFormatException e){
                i = 0;
            }
            int curyl = meterdata - i;
            String account = (String) data.get(curindex).get("accountnum");
            mytab = new MyOperator(helper.getWritableDatabase());
            mytab.Update1(account, meterdata, curyl, state, curreaddate);
            // mytab.Update(account,meterdata );
          /*  Toast t = Toast.makeText(this, "水表数据保存成功", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();*/
            ToastUtil.showShortToast( "水表数据保存成功");
            UserInfo userInfo = new UserInfo();
            userInfo.accountnum = account;
            userInfo.curyl = curyl;
            userInfo.state = state;
            userInfo.curreaddate =curreaddate;
            if(userInfoList.isEmpty()){
                userInfoList.add(userInfo);
            }else {
                boolean isSame = checkSame(account,userInfoList);
                if (isSame){
                    for (int j = 0; j < userInfoList.size(); j++) {
                       if (account.equals(userInfoList.get(j).accountnum)){
                           userInfoList.set(j,userInfo);
                       }
                    }
                }else {
                    userInfoList.add(userInfo);
                }
            }
            // ("本次读数：" + (String)data.get(curindex).get("curdata")) ;
            // stateview.setText("保存成功");
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EDIT_LIST_KEY,userInfoList);
        bundle.putInt(CURRENT_ITEM_TAG_KEY,currentIemTag);
        intent.putExtra(EDIT_DATA_KEY,bundle);
        setResult(RESULT_OK,intent);
        super.finish();
    }
    private boolean checkSame(String userId, ArrayList<UserInfo> userInfoList){
        boolean flag = false;
        for (UserInfo userInfo : userInfoList) {
            if (userId.equals(userInfo.accountnum)){
                flag = true;
                break;
            }else {
                flag = false;
            }
        }
        return flag;
    }


    private void clearInput(){
        editmeterdata.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvTakePhoto:
                takePhoto();
             /*   PhotoPicker.builder().setPreviewEnabled(true).setGridColumnCount(3)
                        .setShowCamera(false)
                        .start(UserInfoActivity.this);*/
                break;
                default:
                    break;
        }
    }




    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        //拍照成功
        if(requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK){
            Toast.makeText(UserInfoActivity.this,"拍照成功", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto(){
        //获取路径
        LogUtil.w(TAG,"照片名："+photoName);
        String path =EmiConfig.EMI_PHOTO_PATH+"/";
        Calendar calendar = Calendar.getInstance();
        photoName += calendar.get(Calendar.YEAR);
        photoName += calendar.get(Calendar.MONTH);
        photoName += calendar.get(Calendar.DAY_OF_MONTH);
        photoName += calendar.get(Calendar.HOUR_OF_DAY);
        photoName += calendar.get(Calendar.MINUTE);
        photoName += calendar.get(Calendar.SECOND);
        String photoFileName;//定义文件名
        photoFileName = photoName+".jpg";
        File file = new File(path,photoFileName);
        //文件夹不存在
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        Uri imageUri;
        if (Build.VERSION.SDK_INT < 24) {
            //拍照的照片路径
            imageUri = Uri.fromFile(file);
        }else {
            imageUri = FileProvider.getUriForFile(UserInfoActivity.this,getResources().getString(R.string.provider_authorities), file);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
    }

}



