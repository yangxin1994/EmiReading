package com.emi.emireading.widget.view.emimenu;

import android.graphics.drawable.Drawable;


public class IconPowerMenuItem {
    private Drawable icon;
    private String title;
    public IconPowerMenuItem(Drawable icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public IconPowerMenuItem(String title) {
        this.title = title;
    }

    public void setIcon(Drawable drawable) {
        this.icon = drawable;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getTitle() {
        return this.title;
    }
}
