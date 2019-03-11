package com.emi.emireading.widget.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.emi.emireading.R;


/**
 * @author :zhoujian
 * @description : 自定义进度对话框
 * @company :翼迈科技
 * @date: 2017年08月29日上午 10:11
 * @Email: 971613168@qq.com
 */

public class EmiProgressDialog extends Dialog {
    private EmiProgressBar emiPb;

    public EmiProgressDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView(){
        setContentView(R.layout.emi_dialog_progress);
        emiPb = (EmiProgressBar) findViewById(R.id.emiPb);
        emiPb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emiPb.isFinish()){
                    EmiProgressDialog.this.dismiss();
                }
            }
        });
    }

  public void setMaxProgress(float progress){
      emiPb.setMaxProgress(progress);
  }

    public void setProgress(float progress){
        if (emiPb != null){
            emiPb.setProgress(progress);
        }
    }

    public void finishLoad(){
        if (emiPb != null){
            emiPb.finishLoad();
        }
    }

    public void setFinishText(String text){
        if (emiPb != null){
            emiPb.setFinishText(text);
        }

    }
    public void setProgressText(String text){
        if (emiPb != null){
            emiPb.setProgressText(text);
        }
    }
    public void reset(){
        if (emiPb != null){
            emiPb.reset();
        }
    }
}
