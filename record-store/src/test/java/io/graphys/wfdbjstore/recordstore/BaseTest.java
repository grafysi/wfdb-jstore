package io.graphys.wfdbjstore.recordstore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.StopWatch;
import wfdb.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.properties")*/
public class BaseTest {
    protected final Logger logger = LogManager.getLogger(BaseTest.class);

    protected StopWatch stopWatch = new StopWatch();

    protected Random random = new Random();

    protected WfdbManager wfdbManager = WfdbManager.get();

    protected String CONSOLE_PATH = "data/output/console.csv";


    @BeforeEach
    void logStartTime() {
        stopWatch.start();
        //wfdb.setwfdb(wfdbManager.getLocalWfdbPath());
        //wfdb.setwfdb(String.join(";", wfdbManager.getWfdbPaths()));
        //wfdb.setwfdb(String.join(";", wfdbManager.getLocalWfdbPath()));
        wfdb.wfdbquit();
        //wfdb.wfdbquiet();
        logger.info("wfdb paths: {}", wfdb.getwfdb());
    }

    @AfterEach
    void logStopTime() {
        stopWatch.stop();
        logger.info("Executed in " + stopWatch.getTotalTime(TimeUnit.MILLISECONDS) + " ms.");
    }

    protected void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected int randomInt(int bound) {
        return random.nextInt(bound);
    }

    protected void doHeavyComputationOnBytes(byte[] bytes) {
        double sum = 0;
        for (byte aByte : bytes) {
            sum += Math.pow(aByte, 5);
        }
    }
}