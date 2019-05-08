package com.cynthia.etcdkeeper.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * class_name: EtcdConnectResultVo
 * package: com.cydia.etcdkeeper.vo
 * describe: etcd connect result view object
 * create_user: liurongwei@yiche.com
 * create_date: 2019/4/26
 * create_time: 17:19
 * example: {"info":{"name":"infra1","size":"98Byte","version":"3.3.11"},"status":"running"}
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtcdConnectResultVo {

    private String status;

    private String message;

    private EtcdInfoVo info;
}
