package py.com.fpuna.autotracks.test.service;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import py.com.fpuna.autotracks.matching2.SpatialTemporalMatching;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.Point;

@RunWith(Arquillian.class)
public class STMatchingTest {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackages(true, "py.com.fpuna.autotracks");
        return deployment;
    }

    @Inject
    SpatialTemporalMatching matcher;

    @Test
    public void test_guardar_ruta() {
        List<Point> points = new ArrayList<>();

        Point point = new Point();
        point.setLatitude(-25.3085082);
        point.setLongitude(-57.6342736);
        point.setTime(1397181039701l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.3088353);
        point.setLongitude(-57.6319605);
        point.setTime(1397181078775l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.3088092);
        point.setLongitude(-57.6319038);
        point.setTime(1397181113974l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.3078341);
        point.setLongitude(-57.6318348);
        point.setTime(1397181149860l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.3032043);
        point.setLongitude(-57.6321905);
        point.setTime(1397181187904l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.3027618);
        point.setLongitude(-57.6322861);
        point.setTime(1397181221703l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.298997);
        point.setLongitude(-57.6338365);
        point.setTime(1397181261008l);
        points.add(point);

        point = new Point();
        point.setLatitude(-25.2987758);
        point.setLongitude(-57.6350507);
        point.setTime(1397181296859l);
        points.add(point);

        List<Candidate> candidates = matcher.match(points);

        Assert.assertEquals(points.size(), candidates.size());
    }

}
