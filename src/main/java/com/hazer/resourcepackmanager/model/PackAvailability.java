package com.hazer.resourcepackmanager.model;

/**
 * Describes if and why a resource pack can be provided to players.
 */
public enum PackAvailability {
    /**
     * A pack was discovered and is currently active.
     */
    AVAILABLE,

    /**
     * The resourcepacks directory does not contain any .zip file.
     */
    NO_PACKS_FOUND,

    /**
     * The resourcepacks directory could not be read.
     */
    DIRECTORY_READ_ERROR,

    /**
     * A pack exists but URL generation failed due to invalid configuration.
     */
    URL_CONFIGURATION_ERROR
}
