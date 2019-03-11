package com.emi.emireading.core.bean;

import java.util.List;


public interface IExpandable<T> {
    boolean isExpanded();
    void setExpanded(boolean expanded);
    List<T> getSubItems();


    int getLevel();
}
