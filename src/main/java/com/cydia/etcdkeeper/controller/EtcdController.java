package com.cydia.etcdkeeper.controller;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.repository.ServerConfigRepository;
import com.cydia.etcdkeeper.vo.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/etcd/")
public class EtcdController {

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @RequestMapping("server-list")
    public JsonResult serverList(){
        List<ServerConfig> serverConfigList= serverConfigRepository.findAll();
        return JsonResult.builder().data(serverConfigList).build().success();
    }
}
