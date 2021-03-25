package com.sim.application.classes;

public class ScrollPosition {
    private double X;
    private double Y;

    public ScrollPosition() {
        this.X = 0.0;
        this.Y = 0.0;
    }

    public ScrollPosition(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public void setX(double x) {
        X = x;
    }

    public void setY(double y) {
        Y = y;
    }

    public void setPos(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }
}
