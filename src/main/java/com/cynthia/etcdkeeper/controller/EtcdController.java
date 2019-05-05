package com.cynthia.etcdkeeper.controller;

import com.cynthia.etcdkeeper.annotations.CookieProperty;
import com.cynthia.etcdkeeper.config.EtcdConfig;
import com.cynthia.etcdkeeper.entity.ServerConfig;
import com.cynthia.etcdkeeper.pojo.EtcdNode;
import com.cynthia.etcdkeeper.repository.ServerConfigRepository;
import com.cynthia.etcdkeeper.req.ConnectForm;
import com.cynthia.etcdkeeper.req.EditNodeForm;
import com.cynthia.etcdkeeper.req.EtcdClientForm;
import com.cynthia.etcdkeeper.req.GetPathQuery;
import com.cynthia.etcdkeeper.service.EtcdService;
import com.cynthia.etcdkeeper.service.EtcdServiceFactory;
import com.cynthia.etcdkeeper.vo.EtcdInfoVo;
import com.cynthia.etcdkeeper.vo.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etcd/")
public class EtcdController {

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private EtcdServiceFactory serviceFactory;

    @RequestMapping("servers")
    public JsonResult serverList(){
        List<ServerConfig> serverConfigList= serverConfigRepository.findAll();
        return JsonResult.builder().data(serverConfigList).build().success();
    }

    @RequestMapping(value = "separator")
    public JsonResult separator(){
        return JsonResult.builder().data( etcdConfig.getSeparator()).build().success();
    }

    @PostMapping("connect")
    public JsonResult connect(@RequestBody ConnectForm server){

        EtcdService service = serviceFactory.getService(server.getId());
        ServerConfig serverConfig = serverConfigRepository.getOne(server.getId());

        EtcdInfoVo resultVo = service.connect(serverConfig);

        return JsonResult.builder().data(resultVo).build().success();
    }

    @GetMapping("path")
    public JsonResult getPath(@CookieProperty EtcdClientForm clientForm, GetPathQuery query){

        EtcdService service = serviceFactory.getService(query.getServerId());

        EtcdNode node = service.getPath(query);

        return JsonResult.builder().data(node).build().success();
    }

    @GetMapping("key")
    public JsonResult getKey(@CookieProperty EtcdClientForm clientForm, GetPathQuery query){

        EtcdService service = serviceFactory.getService(query.getServerId());
        EtcdNode node = service.getKey(query);

        return JsonResult.builder().data(node).build().success();
    }

    @PutMapping("put")
    public JsonResult putKv(@RequestBody EditNodeForm form){

        EtcdService service = serviceFactory.getService(form.getServerId());

        EtcdNode node = service.putKv(form);

        return JsonResult.builder().data(node).build().success();
    }

    @DeleteMapping("delete")
    public JsonResult delete(@RequestBody EditNodeForm form){

        EtcdService service = serviceFactory.getService(form.getServerId());

        EtcdNode node = service.delete(form);

        return JsonResult.builder().data(node).build().success();
    }
}
