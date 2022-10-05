package fr.pantheonsorbonne.ufr27.miage.m1.app.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CamelMain extends RouteBuilder {

    @Inject
    Triangle triangle;
    @Inject
    Point point;


    @Override
    public void configure() {
        from("file:target/data/triangles?noop=true")
                .unmarshal().csv()
                .process(new TriangleProcessor(triangle, point))
                .process(new EquilateralDetector(triangle))
                .choice()
                .when(header("isEquilateral").isEqualTo("true"))
                .marshal().jacksonXml(Triangle.class)
                .to("jms:queue/miage.wan.equilateral")
                .otherwise()
                .marshal().jacksonXml(Triangle.class)
                .to("jms:queue/miage.wan.autres");

        from("jms:queue/miage.wan.equilateral")
                .marshal()
                .jacksonXml(Triangle.class)
                .process(new PerimeterComputer())
                .marshal().json()
                .to("file:target/data/result");

        from("jms:queue/miage.wan.autres")
                .marshal()
                .jacksonXml(Triangle.class)
                .process(new PerimeterComputer())
                .marshal().json()
                .to("file:target/data/result");
    }

    private static class TriangleProcessor implements Processor {

        Triangle triangle;
        Point point;

        public TriangleProcessor(Triangle triangle, Point point) {
            this.triangle = triangle;
            this.point = point;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            List<List<String>> list = (List<List<String>>) exchange.getMessage().getBody();
            Point[] listPoints = new Point[list.size()];
            for (int i = 0; i < list.size(); i++) {
                point.setX(Double.parseDouble(list.get(i).get(0)));
                point.setY(Double.parseDouble(list.get(i).get(1)));
                listPoints[i] = point.clone();
            }
            triangle.setPoints(listPoints);
        }
    }

    private static class EquilateralDetector implements Processor {

        Triangle triangle;

        public EquilateralDetector(Triangle triangle) {
            this.triangle = triangle;
        }

        @Override
        public void process(Exchange exchange) throws Exception {

            double abY = triangle.getPoints()[1].getY() - triangle.getPoints()[0].getY();
            double abX = triangle.getPoints()[1].getX() - triangle.getPoints()[0].getX();
            double ab = Math.round((Math.hypot(abX, abY)*100)/100);

            double acY = triangle.getPoints()[2].getY() - triangle.getPoints()[0].getY();
            double acX = triangle.getPoints()[2].getX() - triangle.getPoints()[0].getX();
            double ac = Math.round((Math.hypot(acX, acY)*100)/100);

            double bcY = triangle.getPoints()[2].getY() - triangle.getPoints()[1].getY();
            double bcX = triangle.getPoints()[2].getX() - triangle.getPoints()[1].getX();
            double bc = Math.round((Math.hypot(bcX, bcY)*100)/100);

            exchange.getMessage().setHeader("isEquilateral", (ac == ab && ab == bc));
        }
    }

    private static class PerimeterComputer implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {

        }
    }
}
