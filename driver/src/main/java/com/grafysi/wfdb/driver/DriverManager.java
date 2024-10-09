package com.grafysi.wfdb.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grafysi.wfdb.driver.exception.WfdbException;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.description.MetadataConnectionDescription;
import io.graphys.wfdbjstore.protocol.description.SignalConnectionDescription;
import io.graphys.wfdbjstore.protocol.exchange.MediaType;
import io.netty.channel.nio.NioEventLoopGroup;

public class DriverManager {

    private static final String AUTH_SCHEME = "basic";

    private static final DriverManager singleton;

    static {
        singleton = new DriverManager();
        Runtime.getRuntime().addShutdownHook(new Thread(singleton::close));
    }

    private static DriverManager getInstance() {
        return singleton;
    }

    private final ObjectMapper objectMapper;

    private final NioEventLoopGroup workers;

    private DriverManager() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        workers = new NioEventLoopGroup(2);
    }

    private void close() {
        workers.close();
    }

    private <T extends Connection> T createConnection(String host, int port, Class<T> connectionClass, Description description) throws WfdbException {

        var client = new Wave4jClient(workers, host, port, objectMapper);

        try {
            if (connectionClass.equals(MetadataConnection.class)) {

                return (T) new MetadataConnection(client, (MetadataConnectionDescription) description);

            } else if (connectionClass.equals(SignalConnection.class)) {

                return (T) new SignalConnection(client, (SignalConnectionDescription) description);

            } else {

                throw new WfdbException("Unsupported connection type: " + connectionClass.getName());

            }

        } catch (ClassCastException e) {
            throw new WfdbException(e);
        }
    }

    public static MetadataConnection newMetadataConnection(String host, int port, String username,
                                                           String password, String database, String dbVersion) throws WfdbException {
        var description = MetadataConnectionDescription.builder()
                .scheme(AUTH_SCHEME)
                .token(username + ";" + password)
                .dbName(database)
                .dbVersion(dbVersion)
                .isReactive(true)
                .reportMediaType(MediaType.APPLICATION_JSON)
                .build();

        return getInstance().createConnection(host, port, MetadataConnection.class, description);
    }

    public static SignalConnection newSignalConnection(String host, int port, String username, String password,
                                                        String database, String dbVersion, String record) throws WfdbException {
        var description = SignalConnectionDescription.builder()
                .scheme(AUTH_SCHEME)
                .token(username + ";" + password)
                .dbName(database)
                .dbVersion(dbVersion)
                .isReactive(true)
                .reportMediaType(MediaType.APPLICATION_JSON)
                .recordName(record)
                .build();

        return getInstance().createConnection(host, port, SignalConnection.class, description);
    }
}



















