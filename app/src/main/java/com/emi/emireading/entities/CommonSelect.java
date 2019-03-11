package com.emi.emireading.entities;

/**
 * @author chx
 */

public class CommonSelect {
    private String content;
    private boolean isSelect;

    public CommonSelect() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public CommonSelect(String content, boolean isSelect) {
        this.content = content;
        this.isSelect = isSelect;
    }
}
