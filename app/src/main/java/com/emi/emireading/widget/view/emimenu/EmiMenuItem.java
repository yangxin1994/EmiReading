

package com.emi.emireading.widget.view.emimenu;

/**
 * @author :zhoujian
 * @description : 翼迈菜单控件实体类
 * @company :翼迈科技
 * @date 2018年07月4日下午 01:48
 * @Email: 971613168@qq.com
 */
public class EmiMenuItem {
    public String title;
    public boolean isSelected;

    public EmiMenuItem(String title, boolean isSelected) {
        this.title = title;
        this.isSelected = isSelected;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
