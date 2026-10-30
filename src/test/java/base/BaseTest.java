package base;

import common.annotations.APIVersion;
import common.extensions.TimingExtension;
import common.generators.TestDataStorage;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TimingExtension.class)
public class BaseTest {

    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    void afterTest(TestInfo testInfo) {
        softly.assertAll();

        if (isDatabaseVersion(testInfo)) {
            TestDataStorage.getStorage().deleteCreatedEntitiesExceptAccounts();
        } else {
            TestDataStorage.getStorage().deleteCreatedEntities();
        }
    }

    private boolean isDatabaseVersion(TestInfo testInfo) {
        APIVersion version = testInfo.getTestMethod()
                .map(method -> method.getAnnotation(APIVersion.class))
                .orElse(null);

        if (version == null) {
            version = testInfo.getTestClass()
                    .map(clazz -> clazz.getAnnotation(APIVersion.class))
                    .orElse(null);
        }

        return version != null
                && "with_database".equals(version.value());
    }
}
