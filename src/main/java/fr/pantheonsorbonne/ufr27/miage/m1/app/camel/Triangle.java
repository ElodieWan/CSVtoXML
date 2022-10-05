package fr.pantheonsorbonne.ufr27.miage.m1.app.camel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Triangle {
    Point[] points;

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "Points 1 : " + points[0].toString() +
                "\nPoints 2 : " + points[1].toString() +
                "\nPoints 3 : " + points[2].toString();
    }
};
