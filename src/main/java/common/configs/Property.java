package common.configs;

public enum Property {

    ADMIN_LOGIN("admin.username"),
    ADMIN_PASSWORD("admin.password"),
    API_BASE_URL("apiBaseUrl"),
    API_VERSION("apiVersion"),
    UI_REMOTE("uiRemote"),
    UI_BASE_URL("uiBaseUrl"),
    BROWSER("browser"),
    BROWSER_SIZE("browserSize"),
    EXPLICIT_WAIT_SECONDS("explicitWaitSeconds");

    private final String key;

    Property(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}