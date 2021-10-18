package io.github.niemals.stop.dcr;

public class DockerNetworkSpec {

    private final String name;
    private String driver;

    public DockerNetworkSpec(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
