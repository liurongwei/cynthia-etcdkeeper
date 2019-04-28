package com.cydia.etcdkeeper.controller;

import com.cydia.etcdkeeper.annotations.CookieProperty;
import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.req.ConnectForm;
import com.cydia.etcdkeeper.req.EtcdClientForm;
import com.cydia.etcdkeeper.service.impl.EtcdV3Service;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping(value = "/connect",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public EtcdConnectResultVo connect(ConnectForm connectForm, @CookieProperty EtcdClientForm clientForm ){
        EtcdConnectResultVo connectResultVo = new EtcdConnectResultVo();

        try {
            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setEndpoints(clientForm.getEndpoint());
            serverConfig.setTitle("test");

            connectResultVo = etcdV3Service.connect(serverConfig);
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
