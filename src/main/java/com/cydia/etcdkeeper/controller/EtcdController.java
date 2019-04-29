package com.cydia.etcdkeeper.controller;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.enums.ResultCodes;
import com.cydia.etcdkeeper.repository.ServerConfigRepository;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;
import com.cydia.etcdkeeper.vo.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/etcd/")
public class EtcdController {

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping("servers")
    public JsonResult serverList(){
        List<ServerConfig> serverConfigList= serverConfigRepository.findAll();
        return JsonResult.builder().data(serverConfigList).build().success();
    }

    @PostMapping("connect")
    public JsonResult connect(@RequestBody ServerConfig server){
        ServerConfig serverConfig = serverConfigRepository.getOne(server.getId());

        String beanName = "EtcdV"+ serverConfig.getApiVersion();
        EtcdService service = applicationContext.getBean( beanName,EtcdService.class);

        if(service==null){
            return JsonResult.builder().code(ResultCodes.Error.getCode())
                    .message(String.format("not fount EtcdServer bean of name %s", beanName)).build();
        }

        EtcdInfoVo resultVo = service.connect(serverConfig);

        return JsonResult.builder().data(resultVo).build().success();
    }
}
