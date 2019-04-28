package com.cydia.etcdkeeper.controller;

import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.service.impl.EtcdV3Service;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/v3")
public class EtcdV3Controller {

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private EtcdV3Service etcdV3Service;

    @RequestMapping(value = "/separator", produces="text/plain;charset=UTF-8")
    public String separator(){
        return etcdConfig.getSeparator();
    }

    @RequestMapping("connect")
    public EtcdConnectResultVo connect(){
        EtcdConnectResultVo connectResultVo = new EtcdConnectResultVo();

        try {
            connectResultVo = etcdV3Service.connect("192.168.87.9", 2379, false, null, null, null);
        }
        catch (InterruptedException e){
            log.error(e.getMessage(),e);
        }
        catch (ExecutionException e){
            log.error(e.getMessage(),e);
        }

        return connectResultVo;
    }
}
