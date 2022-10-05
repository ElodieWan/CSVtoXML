package fr.pantheonsorbonne.ufr27.miage.m1.app.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

//@ApplicationScoped // je ne vois pas l'intérêt de rendre point injectable
public class Point implements Cloneable{
    double x;
    double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public Point clone() throws CloneNotSupportedException {
        return (Point) super.clone();
    }
    
    @Override
    public String toString() {
        return "X : " + x + " " + "Y : " + y;
    }
}
