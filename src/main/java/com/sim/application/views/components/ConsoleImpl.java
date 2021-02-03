package com.sim.application.views.components;

public interface ConsoleImpl {
    public enum Status { NORMAL, WARNING, ERROR }

    public void addLog(String timeStamp, String content, Status status);
}
