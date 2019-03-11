package com.emi.emireading.widget.view.popup;


/**
 * @author :zhoujian
 * @description : popup弹窗适配器
 * @company :翼迈科技
 * @date 2018年04月25日下午 02:30
 * @Email: 971613168@qq.com
 */
public class EmiPopupMenuItem {
    private String id;
    private int icon;
    private String text;

    public EmiPopupMenuItem() {}

    public EmiPopupMenuItem(String text) {
        this.text = text;
    }

    public EmiPopupMenuItem(int iconId, String text) {
        this.icon = iconId;
        this.text = text;
    }

    public EmiPopupMenuItem(String id, int iconId, String text) {
        this.id = id;
        this.icon = iconId;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;

}
    public void setIcon(int iconId) {
        this.icon = iconId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
