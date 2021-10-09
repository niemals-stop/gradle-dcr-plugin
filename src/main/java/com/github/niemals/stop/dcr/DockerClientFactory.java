package com.github.niemals.stop.dcr;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.net.URI;

public class DockerClientFactory {

    public static DockerClient localhost() {
        final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        final URI dockerHost = config.getDockerHost();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .build();
        final DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        try {
            client.pingCmd()
                    .exec();
            return client;
        } catch (final Exception error) {
            throw new IllegalStateException(String.format("Unable to connect to Docker daemon at '%s'. " +
                    "Is the Docker daemon running and accessible?", dockerHost),
                    error
            );
        }
    }
}
