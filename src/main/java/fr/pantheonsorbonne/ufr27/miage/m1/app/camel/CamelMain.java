package fr.pantheonsorbonne.ufr27.miage.m1.app.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CamelMain extends RouteBuilder {

    //je ne vois pas l'intéret d'injecter ces variables
    //@Inject
    //Triangle triangle;
    //@Inject
    //Point point;


    @Override
    public void configure() {
        from("file:target/data/triangles")
                .unmarshal().csv()
                .process(new TriangleProcessor())//ici, le processor transforme le csv en triangle
                .process(new EquilateralDetectorProcessor()) //ici le processor place les headers sur le message
                .log("Triangle ${body} => isEquilateral=${header.isEquilateral}")
                .choice()
                .when(header("isEquilateral").isEqualTo("true"))
                .marshal().jacksonXml(Triangle.class)
                .to("jms:queue/miage.wan.equilateral")
                .otherwise()
                .marshal().jacksonXml(Triangle.class)
                .to("jms:queue/miage.wan.autres");

        from("jms:queue/miage.wan.equilateral")
                .unmarshal()
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


        @Override
        public void process(Exchange exchange) throws Exception {
            //pas besoin de mettre triangle et point en attr ici
            Triangle triangle = new Triangle();

            List<List<String>> list = (List<List<String>>) exchange.getMessage().getBody();
            Point[] listPoints = new Point[list.size()];
            for (int i = 0; i < list.size(); i++) {

                Point point = new Point();

                point.setX(Double.parseDouble(list.get(i).get(0)));
                point.setY(Double.parseDouble(list.get(i).get(1)));
                listPoints[i] = point;//ici, pas besoin de close, on crée un nouveau DTO Point simplement
            }
            triangle.setPoints(listPoints);

            //il faut mettre le triangle dans le message ici
            exchange.getMessage().setBody(triangle);
        }
    }

    //ici, le nom de la classe doit refleter son usage en tant que processor
    private static class EquilateralDetectorProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Triangle triangle = exchange.getMessage().getBody(Triangle.class);
            double abY = triangle.getPoints()[1].getY() - triangle.getPoints()[0].getY();
            double abX = triangle.getPoints()[1].getX() - triangle.getPoints()[0].getX();
            double ab = Math.round((Math.hypot(abX, abY) * 100) / 100);

            double acY = triangle.getPoints()[2].getY() - triangle.getPoints()[0].getY();
            double acX = triangle.getPoints()[2].getX() - triangle.getPoints()[0].getX();
            double ac = Math.round((Math.hypot(acX, acY) * 100) / 100);

            double bcY = triangle.getPoints()[2].getY() - triangle.getPoints()[1].getY();
            double bcX = triangle.getPoints()[2].getX() - triangle.getPoints()[1].getX();
            //je ne connaissais pas hypot!
            double bc = Math.round((Math.hypot(bcX, bcY) * 100) / 100);
            //si vous vous souvenez de votre cours de L3, vous comprenez que == entre les doubles ne fonctionne pas!
            exchange.getMessage().setHeader("isEquilateral", (ac == ab && ab == bc));
        }
    }

    private static class PerimeterComputer implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            //TODO
        }
    }
}
