package com.cydia.etcdkeeper.controller;

import com.cydia.etcdkeeper.annotations.CookieProperty;
import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.enums.ResultCodes;
import com.cydia.etcdkeeper.pojo.EtcdNode;
import com.cydia.etcdkeeper.repository.ServerConfigRepository;
import com.cydia.etcdkeeper.req.EditNodeForm;
import com.cydia.etcdkeeper.req.EtcdClientForm;
import com.cydia.etcdkeeper.req.GetPathQuery;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.service.EtcdServiceFactory;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;
import com.cydia.etcdkeeper.vo.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
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
    public JsonResult connect(@RequestBody ServerConfig server){

        EtcdService service = serviceFactory.getService(server.getId());
        ServerConfig serverConfig = serverConfigRepository.getOne(server.getId());

        EtcdInfoVo resultVo = service.connect(serverConfig);

        return JsonResult.builder().data(resultVo).build().success();
    }

    @GetMapping("path")
    public JsonResult getPath(@CookieProperty EtcdClientForm clientForm, GetPathQuery query){

        EtcdService service = serviceFactory.getService(query.getId());

        EtcdNode node = service.getPath(query);

        return JsonResult.builder().data(node).build().success();
    }

    @GetMapping("key")
    public JsonResult getKey(@CookieProperty EtcdClientForm clientForm, GetPathQuery query){

        EtcdService service = serviceFactory.getService(query.getId());
        EtcdNode node = service.getKey(query);

        return JsonResult.builder().data(node).build().success();
    }

    @PutMapping("put")
    public JsonResult putKv(EditNodeForm form){

        EtcdService service = serviceFactory.getService(form.getServerId());

        EtcdNode node = service.putKv(form);

        return JsonResult.builder().data(node).build().success();
    }

    @DeleteMapping("delete")
    public JsonResult delete(EditNodeForm form){

        EtcdService service = serviceFactory.getService(form.getServerId());

        EtcdNode node = service.delete(form);

        return JsonResult.builder().data(node).build().success();
    }
}
