package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.pojo.EtcdWrapperNode;
import com.cydia.etcdkeeper.req.CreateNodeForm;
import com.cydia.etcdkeeper.req.EtcdClientForm;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import lombok.extern.slf4j.Slf4j;
import mousio.client.retry.RetryOnce;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
import mousio.etcd4j.responses.EtcdHealthResponse;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdSelfStatsResponse;
import mousio.etcd4j.responses.EtcdVersionResponse;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Service("EtcdV2")
public class EtcdV2Service implements EtcdService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EtcdConfig etcdConfig;

    public EtcdConnectResultVo connect(String endpoint, boolean usetls, String keyFile,
                                       String certFile, String caFile) {
        EtcdConnectResultVo resultVo= new EtcdConnectResultVo();

        String schema = usetls ? "https": "http";

        try( EtcdClient client= getClient(endpoint, usetls) ) {

            EtcdHealthResponse healthResponse = client.getHealth();
            if(healthResponse!=null) {
                resultVo.setInfo(new EtcdConnectResultVo.EtcdInfoVo());

                EtcdSelfStatsResponse selfStatsResponse = client.getSelfStats();
                EtcdVersionResponse versionResponse = client.version();

                resultVo.getInfo().setVersion(versionResponse.server);
                resultVo.getInfo().setName(selfStatsResponse.getName());
                resultVo.getInfo().setSize("unknow");
                resultVo.setStatus("true".equals(healthResponse.getHealth()) ? "ok" : "error");
            }
            else{
                resultVo.setStatus("timeout");
                resultVo.setMessage(String.format( "endpoint %s is unreachable", endpoint));
            }
        }
        catch (IOException e){
            resultVo.setStatus("error");
            resultVo.setMessage("cann't get server status,"+ e.getLocalizedMessage());
        }

        return resultVo;
    }

    private EtcdClient getClient(String endpoint, boolean usetls){

        String hostAndPort = endpoint;
        if(endpoint.startsWith("http://") || endpoint.startsWith("https://")){
            hostAndPort = endpoint.split("//")[1];
        }

        String schema = usetls ? "https": "http";

        EtcdNettyConfig config = new EtcdNettyConfig();
        config.setConnectTimeout(etcdConfig.getClient().getTimeout());
        EtcdNettyClient nettyClient = new EtcdNettyClient(config,
                URI.create(String.format("%s://%s", schema, endpoint)));
        EtcdClient client = new EtcdClient(nettyClient);

        //no try
        client.setRetryHandler(new RetryOnce(100));

        return client;
    }

    public EtcdWrapperNode getPath(EtcdClientForm clientForm, String key , boolean prefix){

        EtcdWrapperNode rootNode = null;

        try(EtcdClient client = getClient(clientForm.getEndpoint(),false)){

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.get(key).recursive().sorted().send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode= new EtcdWrapperNode();
            rootNode.setNode(node);
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    public EtcdWrapperNode getKey(EtcdClientForm clientForm, String key){
        EtcdWrapperNode rootNode = null;

        try(EtcdClient client = getClient(clientForm.getEndpoint(),false)){

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.get(key).send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode= new EtcdWrapperNode();
            rootNode.setNode(node);
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    public EtcdWrapperNode put(EtcdClientForm clientForm, CreateNodeForm nodeForm){
        EtcdWrapperNode rootNode = null;

        try(EtcdClient client = getClient(clientForm.getEndpoint(),false)){

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.put(nodeForm.getKey(),nodeForm.getValue())
                    .ttl(nodeForm.getTtl()<=0 ? null : nodeForm.getTtl()).send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode= new EtcdWrapperNode();
            rootNode.setNode(node);
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }
}
