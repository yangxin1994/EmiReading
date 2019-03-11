
package com.emi.emireading.widget.view.wheelview;


final class OnItemSelectedRunnable implements Runnable {
    final WheelView wheelView;

    OnItemSelectedRunnable(WheelView wheelView) {
        this.wheelView = wheelView;
    }

    @Override
    public final void run() {
        wheelView.onItemSelectedListener.onItemSelected(wheelView.getSelectedItem());
    }
}
