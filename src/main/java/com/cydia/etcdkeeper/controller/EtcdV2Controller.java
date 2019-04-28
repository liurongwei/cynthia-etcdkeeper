package com.cydia.etcdkeeper.controller;


import com.cydia.etcdkeeper.annotations.CookieProperty;
import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.pojo.EtcdWrapperNode;
import com.cydia.etcdkeeper.req.ConnectForm;
import com.cydia.etcdkeeper.req.CreateNodeForm;
import com.cydia.etcdkeeper.req.EtcdClientForm;
import com.cydia.etcdkeeper.req.GetPathQuery;
import com.cydia.etcdkeeper.service.impl.EtcdV2Service;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import lombok.extern.slf4j.Slf4j;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("v2")
public class EtcdV2Controller {

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private EtcdV2Service etcdV2Service;

    @RequestMapping(value = "/separator", produces={MediaType.TEXT_PLAIN_VALUE})
    public String separator(){
        return etcdConfig.getSeparator();
    }

    @RequestMapping("/config")
    public EtcdConfig getConfig(){
        return etcdConfig;
    }

    @PostMapping(value = "/connect",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public EtcdConnectResultVo connect(ConnectForm connectForm, @CookieProperty EtcdClientForm clientForm ){
        EtcdConnectResultVo connectResultVo =
            connectResultVo = etcdV2Service.connect(connectForm.getHost(), false, null,null,null);
        return connectResultVo;
    }


    @RequestMapping("/cache-test")
    @Cacheable(value = "cacheTest", key = "#num" , condition = "#num> 10")
    public String cacheTest(Integer num){

        return String.valueOf(num);
    }


    @RequestMapping("/getpath")
    public EtcdWrapperNode getPath(@CookieProperty EtcdClientForm clientForm, GetPathQuery query){
        log.info(clientForm.toString());

        log.info(query.toString());

        EtcdWrapperNode node = etcdV2Service.getPath(clientForm, query.getKey(),query.isPrefix());

        return node;
    }

    @RequestMapping("/get")
    public EtcdWrapperNode getKey(@CookieProperty EtcdClientForm clientForm, GetPathQuery query){
        log.info(clientForm.toString());

        log.info(query.toString());

        EtcdWrapperNode node = etcdV2Service.getKey(clientForm, query.getKey());

        return node;
    }

    @PutMapping(value = "/put")
    public EtcdWrapperNode put(@CookieProperty EtcdClientForm clientForm, CreateNodeForm nodeForm){
        log.info(clientForm.toString());

        log.info(nodeForm.toString());

        EtcdWrapperNode node = etcdV2Service.put(clientForm,nodeForm);

        return node;
    }
}
