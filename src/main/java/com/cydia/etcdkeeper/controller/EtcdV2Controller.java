package com.cydia.etcdkeeper.controller;


import com.cydia.etcdkeeper.annotations.CookieProperty;
import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.form.ConnectForm;
import com.cydia.etcdkeeper.form.EtcdClientForm;
import com.cydia.etcdkeeper.service.impl.EtcdV2Service;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("v2")
public class EtcdV2Controller {

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private EtcdV2Service etcdV2Service;

    @RequestMapping(value = "/separator", produces="text/plain;charset=UTF-8")
    public String separator(){
        return etcdConfig.getSeparator();
    }

    @RequestMapping("config")
    public EtcdConfig getConfig(){
        return etcdConfig;
    }

    @RequestMapping(value = "connect",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public EtcdConnectResultVo connect(ConnectForm connectForm, @CookieProperty EtcdClientForm clientForm ){
        EtcdConnectResultVo connectResultVo = new EtcdConnectResultVo();
        try {
            connectResultVo = etcdV2Service.connect(connectForm.getHost(), false, null,null,null);
        }
        catch (InterruptedException e){
            log.error(e.getLocalizedMessage(),e);
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e2){
            log.error(e2.getLocalizedMessage(),e2);
        }

        log.info(new Gson().toJson(connectForm));
        log.info(new Gson().toJson(clientForm));

        return connectResultVo;
    }


    @RequestMapping("cache-test")
    @Cacheable(value = "cacheTest", key = "#num" , condition = "#num> 10")
    public String cacheTest(Integer num){

        return String.valueOf(num);
    }
}
