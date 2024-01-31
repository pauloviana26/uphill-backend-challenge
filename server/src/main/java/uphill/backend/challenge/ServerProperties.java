package uphill.backend.challenge;

import java.util.Properties;

public class ServerProperties {

    private static final String DEBUG_ENABLED_KEY = "debug.enable";
    private static final boolean DEFAULT_DEBUG_ENABLED = false;

    private final Properties properties;

    public ServerProperties() {
        this.properties = new Properties();
    }

    public boolean isDebugEnabled() {
        return Boolean
                .parseBoolean(properties.getProperty(DEBUG_ENABLED_KEY, String.valueOf(DEFAULT_DEBUG_ENABLED)));
    }
}
