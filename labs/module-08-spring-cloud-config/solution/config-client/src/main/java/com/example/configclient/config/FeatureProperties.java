package com.example.configclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "feature")
public class FeatureProperties {

    private boolean newDashboard;
    private boolean darkMode;
    private boolean debugMode;

    public boolean isNewDashboard() {
        return newDashboard;
    }

    public void setNewDashboard(boolean newDashboard) {
        this.newDashboard = newDashboard;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
