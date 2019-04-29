package com.cydia.etcdkeeper.service;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;

/**
 * class_name: IEtcdService
 * package: com.cydia.etcdkeeper.service
 * describe: etcd compatiable interface
 * create_user: liurongwei@yiche.com
 * create_date: 2019/4/26
 * create_time: 17:43
 **/
public interface EtcdService {

    /**
     * connect to etcd and get the server info
     * @param serverConfig server config object
     * @return connect result
     */
    EtcdInfoVo connect(ServerConfig serverConfig);
}
