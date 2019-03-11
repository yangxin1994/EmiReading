
package com.emi.emireading.widget.view.pickerview.wheel;

/**
 * Range for visible items.
 */
public class ItemsRange {
    /**
     * 第一个item对应数字
     */
    private int first;

    /**
     * item数量
     */
    private int count;

    public ItemsRange() {
        this(0, 0);
    }

    public ItemsRange(int first, int count) {
        this.first = first;
        this.count = count;
    }


    public int getFirst() {
        return first;
    }

    public int getLast() {
        return getFirst() + getCount() - 1;
    }

    public int getCount() {
        return count;
    }

    /**
     * item是否包含索引
     *
     * @param index the item number
     * @return true 如果包含返回true
     */
    public boolean contains(int index) {
        return index >= getFirst() && index <= getLast();
    }
}