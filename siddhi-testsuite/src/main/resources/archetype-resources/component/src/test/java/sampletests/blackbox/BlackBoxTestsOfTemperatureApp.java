package sampletests.blackbox;

import com.google.common.io.Resources;
import io.siddhi.distribution.test.framework.MySQLContainer;
import io.siddhi.distribution.test.framework.NatsContainer;
import io.siddhi.distribution.test.framework.SiddhiRunnerContainer;
import io.siddhi.distribution.test.framework.util.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import sampletests.AbstractTests;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for black box testing. Run black-box testing by configuring the Siddhi Runner instances to communicate
 * with the actual external systems.
 */
public class BlackBoxTestsOfTemperatureApp extends AbstractTemperatureAlertTests {
    private static final Logger logger = LoggerFactory.getLogger(BlackBoxTestsOfTemperatureApp.class);

    private static final String DATABSE_JDBC_URL = "jdbc:mysql://mysql:3306/";
    private static final String DATABSE_USERNAME = "username";
    private static final String DATABSE_PASSWORD = "password";
    private static final String DATABSE_DRIVER_NAME = "com.mysql.jdbc.Driver";
    private static final String NATS_CLUSTER_ID = "TemperatureCluster";
    private static final String NATS_BOOTSTRAP_URL = "nats://nats-streaming:443";
    private static final String NATS_INPUT_DESTINATION = "Temp-Alert-App_DeviceTempStream";
    private static final String NATS_OUTPUT_DESTINATION = "Temp-Alert-App_AlertStream";

    @BeforeClass
    public void setUpCluster() throws IOException, InterruptedException {
        Path jarsFromMaven = Paths.get("target", "artifacts/jars");
        URL appUrl = Resources.getResource("artifacts/apps");
        URL configUrl = Resources.getResource("artifacts/config/Datasource.yaml");

        natsClient = new NatsClient(NATS_CLUSTER_ID, NATS_BOOTSTRAP_URL);
        natsClient.connect();

        Map<String, String> envMap = new HashMap<>();
        envMap.put("CLUSTER_ID", NATS_CLUSTER_ID);
        envMap.put("INPUT_DESTINATION", NATS_INPUT_DESTINATION);
        envMap.put("OUTPUT_DESTINATION", NATS_OUTPUT_DESTINATION);
        envMap.put("NATS_URL", NATS_BOOTSTRAP_URL);
        envMap.put("DATABASE_URL", DATABSE_JDBC_URL);
        envMap.put("USERNAME", DATABSE_USERNAME);
        envMap.put("PASSWORD", DATABSE_PASSWORD);
        envMap.put("JDBC_DRIVER_NAME", DATABSE_DRIVER_NAME);
        siddhiRunnerContainer = new SiddhiRunnerContainer("siddhiio/siddhi-runner-alpine:latest-dev")
                .withSiddhiApps(appUrl.getPath())
                .withJars(jarsFromMaven.toString(), false)
                .withConfig(configUrl.getPath())
                .withEnv(envMap)
                .withLogConsumer(new Slf4jLogConsumer(logger));
        siddhiRunnerContainer.start();
        siddhiRunnerContainer.followOutput(siddhiLogConsumer, OutputFrame.OutputType.STDOUT);
        configureNatsConnection(NATS_CLUSTER_ID, NATS_BOOTSTRAP_URL, NATS_INPUT_DESTINATION, NATS_OUTPUT_DESTINATION);
        configureMySqlConnection(DATABSE_JDBC_URL, DATABSE_USERNAME, DATABSE_PASSWORD);
    }

    @AfterClass
    public void shutdownCluster() {
        if (siddhiRunnerContainer != null) {
            siddhiRunnerContainer.stop();
        }
    }
}
