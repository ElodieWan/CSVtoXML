package fr.pantheonsorbonne.ufr27.miage.m1.app.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.annotation.XmlRootElement;

//@ApplicationScoped // je ne vois pas l'intérêt de rentre triangle injectable
@XmlRootElement
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
