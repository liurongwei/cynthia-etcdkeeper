package com.cydia.etcdkeeper.pojo;

import lombok.Data;

@Data
public class EtcdConnection {

    private String host;

    private int port;

    private String keyFile;

    private String certFile;

    private String caFile;

    private boolean useTls;

    private boolean useAuth;

    private String username;

    private String password;
}
