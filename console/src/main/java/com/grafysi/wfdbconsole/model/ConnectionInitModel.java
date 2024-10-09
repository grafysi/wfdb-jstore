package com.grafysi.wfdbconsole.model;

import lombok.Builder;

@Builder
public record ConnectionInitModel(String name, String host, Integer port, String user,
                                  String password, String database, String dbVersion) {

}
