package com.github.niemals.stop.dcr;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DockerNetworkPlugin implements Plugin<Project> {

    private static final String PLUGIN_NAME = "dockerNetworks";

    @Override
    public void apply(Project project) {
        final Logger log = project.getLogger();

        final NamedDomainObjectContainer<DockerNetworkSpec> networks = project.container(DockerNetworkSpec.class);
        project.getExtensions().add(PLUGIN_NAME, networks);
        project.task("createDockerNetworks", t -> t.doLast(task -> {
            try (final DockerClient client = DockerClientFactory.localhost()) {
                networks.forEach(networkSpec -> {
                    final boolean alreadyExists = !client.listNetworksCmd()
                            .withNameFilter(networkSpec.getName())
                            .exec()
                            .isEmpty();
                    if (alreadyExists) {
                        log.info("Network '{}' already exists. Skipped.", networkSpec.getName());
                    } else {
                        final CreateNetworkResponse response = client.createNetworkCmd()
                                .withName(networkSpec.getName())
                                .withCheckDuplicate(true)
                                .withAttachable(true)
                                .withLabels(buildLabels(project))
                                .exec();
                        log.info("Network '{}' created.", response.getId());
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }));

        project.task("removeDockerNetworks", t -> t.doLast(task -> {
            try (final DockerClient client = DockerClientFactory.localhost()) {
                final String[] networkNames = networks.stream().map(DockerNetworkSpec::getName)
                        .toArray(String[]::new);
                if (networkNames.length > 0) {
                    client.listNetworksCmd()
                            .withFilter("label", buildLabels(project)
                                    .entrySet()
                                    .stream()
                                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                                    .collect(Collectors.toList()))
                            .exec()
                            .forEach(network -> {
                                client.removeNetworkCmd(network.getId())
                                        .exec();
                                log.info("Network '{}' removed.", network.getName());
                            });
                } else {
                    log.info("Configured networks '{}' don't exist.", Arrays.toString(networkNames));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }));
    }

    static Map<String, String> buildLabels(Project project) {
        return ImmutableMap.of(
                "project", project.getName(),
                "managedBy", PLUGIN_NAME
        );
    }

}
