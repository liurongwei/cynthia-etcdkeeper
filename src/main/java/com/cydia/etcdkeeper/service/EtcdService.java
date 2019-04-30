package com.cydia.etcdkeeper.service;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.pojo.EtcdNode;
import com.cydia.etcdkeeper.req.EditNodeForm;
import com.cydia.etcdkeeper.req.GetPathQuery;
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

    /**
     * query path
     * @param query path query condition
     * @return EtcdNode object
     */
    EtcdNode getPath(GetPathQuery query);


    /**
     * query path
     * @param query path query condition
     * @return EtcdNode object
     */
    EtcdNode getKey(GetPathQuery query);


    /**
     * edit key
     * @param form form with key and value
     * @return edit node result
     */
    EtcdNode putKv(EditNodeForm form);


    /**
     * delete node form specified server
     * @param form node & server parameters
     * @return deleted nodes
     */
    EtcdNode delete(EditNodeForm form);
}
