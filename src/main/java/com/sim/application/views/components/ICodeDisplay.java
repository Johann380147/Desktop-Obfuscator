package com.sim.application.views.components;

import com.sim.application.classes.ScrollPosition;

public interface ICodeDisplay {
    public void setCode(String code);
    public void setScrollPosition(int pos);
    public int getScrollPosition();
}
