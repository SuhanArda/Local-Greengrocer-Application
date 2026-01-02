package com.greengrocer.models;

/**
 * Model for system-wide settings.
 * Maps to SystemSettings table
 * 
 * @author Burak Özevin
 */
public class SystemSetting {
    /** The unique key for the setting. */
    private String key;
    /** The value of the setting. */
    private String value;
    /** Description of what the setting controls. */
    private String description;

    /**
     * Default constructor.
     * 
     * @author Burak Özevin
     */
    public SystemSetting() {
    }

    /**
     * Full constructor for SystemSetting.
     *
     * @param key         the setting key
     * @param value       the setting value
     * @param description the setting description
     * 
     * @author Burak Özevin
     */
    public SystemSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    /**
     * Gets the setting key.
     * 
     * @return the setting key
     * 
     * @author Burak Özevin
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the setting key.
     * 
     * @param key the setting key to set
     * 
     * @author Burak Özevin
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the setting value.
     * 
     * @return the setting value
     * 
     * @author Burak Özevin
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the setting value.
     * 
     * @param value the setting value to set
     * 
     * @author Burak Özevin
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the setting description.
     * 
     * @return the setting description
     * 
     * @author Burak Özevin
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the setting description.
     * 
     * @param description the setting description to set
     * 
     * @author Burak Özevin
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
