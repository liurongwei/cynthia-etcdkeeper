package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdHealthResponse;
import mousio.etcd4j.responses.EtcdSelfStatsResponse;
import mousio.etcd4j.responses.EtcdVersionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service("EtcdV2")
public class EtcdV2Service implements EtcdService {

    @Autowired
    private CacheManager cacheManager;

    public EtcdConnectResultVo connect(String endpoint, boolean usetls, String keyFile,
                                       String certFile, String caFile) throws ExecutionException,InterruptedException {
        EtcdConnectResultVo resultVo= new EtcdConnectResultVo();

        String schema = usetls ? "https": "http";

        String hostAndPort = endpoint;
        if(endpoint.startsWith("http://") || endpoint.startsWith("https://")){
            hostAndPort = endpoint.split("//")[1];
        }

        try( EtcdClient client= new EtcdClient(URI.create(String.format("%s://%s", schema, endpoint)))) {
            EtcdHealthResponse healthResponse = client.getHealth();
            EtcdSelfStatsResponse selfStatsResponse = client.getSelfStats();
            EtcdVersionResponse versionResponse = client.version();

            resultVo.getInfo().setVersion(versionResponse.server);
            resultVo.getInfo().setName(selfStatsResponse.getName());
            resultVo.getInfo().setSize("unknow");
            resultVo.setStatus("true".equals( healthResponse.getHealth()) ? "ok" : "error");
        }
        catch (IOException e){
            resultVo.setStatus("error");
            resultVo.setMessage("cann't get server status,"+ e.getLocalizedMessage());
        }

        return resultVo;
    }
}
