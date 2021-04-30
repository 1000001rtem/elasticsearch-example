package ru.eremin.elasticsearch.example.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.Base58;

import java.time.Duration;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class ClothesElasticsearchContainer extends GenericContainer<ClothesElasticsearchContainer> {

    public ClothesElasticsearchContainer(String imageName) {
        super(imageName);
    }

    @Override
    protected void configure() {
        withNetworkAliases("elasticsearch-" + Base58.randomString(6));
        withEnv("discovery.type", "single-node");
        addExposedPorts(9200, 9300);
        addFixedExposedPort(9200, 9200);
        addFixedExposedPort(9300, 9300);
        setWaitStrategy(
                new HttpWaitStrategy()
                        .forPort(9200)
                        .forStatusCodeMatching(response -> response == HTTP_OK || response == HTTP_UNAUTHORIZED)
                        .withStartupTimeout(Duration.ofMinutes(2)));
    }
}
