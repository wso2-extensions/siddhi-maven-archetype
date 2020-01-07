package teststemplate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class for abstract tests.
 *
 */
public abstract class AbstractTests {

    @BeforeClass
    public abstract void setUpCluster();

    @AfterClass
    public abstract void shutdownCluster();

    public void setClusterConfigs(String natsClusterId, String natsUrl, String natsInputDestination,
                                  String natsOutputDestination) {

    }

    @Test
    public void testMessageConsumption() {

    }

    @Test
    public void testMessagePublishingWithSiddhi() {

    }

    @Test
    public void testAppOutput() {

    }
}
