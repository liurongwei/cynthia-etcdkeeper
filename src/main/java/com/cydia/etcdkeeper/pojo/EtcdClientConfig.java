package com.cydia.etcdkeeper.pojo;

import lombok.Data;

/**
 * class_name: EtcdClientConfig
 * package: com.cydia.etcdkeeper.pojo
 * describe: client config class
 * create_user: liurongwei@yiche.com
 * create_date: 2019/4/28
 * create_time: 13:42
 **/
@Data
public class EtcdClientConfig {

    /**
     * connect to etcd server timeout in million seconds
     */
    private int timeout;
}
