package base;

import common.extensions.TimingExtension;
import common.generators.TestDataStorage;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TimingExtension.class)
public class BaseTest {

    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        TestDataStorage.getStorage().deleteCreatedEntities();
        softly.assertAll();
    }
}
