package com.cydia.etcdkeeper.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * class_name: EtcdConnectResultVo
 * package: com.cydia.etcdkeeper.vo
 * describe: TODO
 * create_user: liurongwei@yiche.com
 * create_date: 2019/4/26
 * create_time: 17:19
 * example: {"info":{"name":"infra1","size":"98Byte","version":"3.3.11"},"status":"running"}
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtcdConnectResultVo {

    public EtcdConnectResultVo(){
        //this.info= new EtcdInfoVo();
    }

    private String status;

    private String message;

    private EtcdInfoVo info;

    @Data
    public static class EtcdInfoVo{
        private String name;

        private String size;

        private String version;
    }
}
