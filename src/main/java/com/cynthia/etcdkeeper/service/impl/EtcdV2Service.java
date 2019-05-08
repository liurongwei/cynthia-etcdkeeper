package com.cynthia.etcdkeeper.service.impl;

import com.cynthia.etcdkeeper.config.EtcdConfig;
import com.cynthia.etcdkeeper.entity.ServerConfig;
import com.cynthia.etcdkeeper.exception.EtcdKeeperException;
import com.cynthia.etcdkeeper.pojo.EtcdNode;
import com.cynthia.etcdkeeper.repository.ServerConfigRepository;
import com.cynthia.etcdkeeper.req.EditNodeForm;
import com.cynthia.etcdkeeper.req.GetPathQuery;
import com.cynthia.etcdkeeper.service.EtcdService;
import com.cynthia.etcdkeeper.utils.EtcdUtils;
import com.cynthia.etcdkeeper.vo.EtcdInfoVo;
import com.google.gson.Gson;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import mousio.client.retry.RetryOnce;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.EtcdSecurityContext;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.*;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@Service("EtcdV2")
public class EtcdV2Service implements EtcdService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    private final Gson gson = new Gson();

    /**
     * connect to etcd and get the server info
     *
     * @param serverConfig server config object
     * @return connect result
     */
    @Override
    public EtcdInfoVo connect(ServerConfig serverConfig) {
        EtcdInfoVo etcdInfoVo = null;
        List<String> endpoints = EtcdUtils.getEndpoints(serverConfig.getEndpoints(), serverConfig.isUseTls());

        if (endpoints == null || endpoints.isEmpty()) {
            throw new EtcdKeeperException(String.format("etcd endpoints needs to specified, config details: %s",
                    gson.toJson(serverConfig)));
        }

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdHealthResponse healthResponse = client.getHealth();
            if (healthResponse == null) {
                throw new EtcdKeeperException(String.format("cann't connect etcd server %s", serverConfig.getEndpoints()));
            }

            etcdInfoVo = new EtcdInfoVo();
            EtcdSelfStatsResponse selfStatsResponse = client.getSelfStats();
            EtcdVersionResponse versionResponse = client.version();
            if (versionResponse != null) {
                etcdInfoVo.setVersion(versionResponse.server);
            }
            if (selfStatsResponse != null) {
                etcdInfoVo.setName(selfStatsResponse.getName());
            }
            etcdInfoVo.setSize("unkown");
        } catch (IOException e) {
            log.error(String.format("cann't connect etcd server %s, config : %s", serverConfig.getEndpoints(),
                    gson.toJson(serverConfig)), e);
            throw new EtcdKeeperException("etcd client error," + e.getMessage(), e);
        }

        return etcdInfoVo;
    }

    /**
     * query path
     *
     * @param query path query condition
     * @return EtcdNode object
     */
    @Override
    public EtcdNode getPath(GetPathQuery query) {
        EtcdNode rootNode = null;

        ServerConfig serverConfig = serverConfigRepository.getOne(query.getServerId());

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.getDir(query.getKey()).recursive().sorted().send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            String nodeJson = gson.toJson(node);

            rootNode = gson.fromJson(nodeJson, EtcdNode.class);

            if(StringUtils.isEmpty( rootNode.getKey())){
                rootNode.setKey(etcdConfig.getSeparator());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    /**
     * delete node form specified server
     *
     * @param form node & server parameters
     * @return deleted nodes
     */
    @Override
    public EtcdNode delete(EditNodeForm form) {
        EtcdNode node = null;

        ServerConfig serverConfig = serverConfigRepository.getOne(form.getServerId());

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = null;
            if (form.isDir()) {
                responsePromise = client.deleteDir(form.getKey()).recursive().send();
            } else {
                responsePromise = client.delete(form.getKey()).send();
            }
            EtcdKeysResponse.EtcdNode etcdNode = responsePromise.get().node;
            if(etcdNode!=null){
                node = gson.fromJson(gson.toJson(etcdNode), EtcdNode.class);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return node;
    }

    /**
     * edit key
     *
     * @param form form with key and value
     * @return edit node result
     */
    @Override
    public EtcdNode putKv(EditNodeForm form) {

        ServerConfig serverConfig = serverConfigRepository.getOne(form.getServerId());

        if(serverConfig==null ){
            throw new EtcdKeeperException(String.format("server not found, form %s", gson.toJson(form)));
        }

        EtcdNode node = null;

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = null;

            if (form.isDir()) {
                responsePromise = client.putDir(form.getKey())
                        .ttl(form.getTtl()==null || form.getTtl() <= 0 ? null : form.getTtl()).send();
            } else {
                responsePromise = client.put(form.getKey(), form.getValue())
                        .ttl(form.getTtl()==null || form.getTtl() <= 0 ? null : form.getTtl()).send();
            }

            responsePromise = client.put(form.getKey(), form.getValue()).send();
            EtcdKeysResponse.EtcdNode etcdNode = responsePromise.get().node;

            if(etcdNode!=null){
                node = gson.fromJson(gson.toJson(etcdNode), EtcdNode.class);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return node;
    }

    /**
     * query path
     *
     * @param query path query condition
     * @return EtcdNode object
     */
    @Override
    public EtcdNode getKey(GetPathQuery query) {
        EtcdNode rootNode = null;

        ServerConfig serverConfig = serverConfigRepository.getOne(query.getServerId());

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.get(query.getKey()).send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            String nodeJson = gson.toJson(node);

            rootNode = gson.fromJson(nodeJson, EtcdNode.class);

            if(StringUtils.isEmpty( rootNode.getKey())){
                rootNode.setKey(etcdConfig.getSeparator());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    private EtcdClient getClient(ServerConfig serverConfig) {

        List<URI> uriList = EtcdUtils.getUris(serverConfig.getEndpoints(), serverConfig.isUseTls());
        URI[] uriArray = uriList.toArray(new URI[uriList.size()]);

        EtcdNettyConfig config = new EtcdNettyConfig();
        config.setConnectTimeout(etcdConfig.getClient().getTimeout());

        EtcdClient client = null;
        EtcdNettyClient nettyClient = null;
        if (serverConfig.isUseTls()) {

            SslContext sslContext = null;
            try {
                if(serverConfig.isSecure()) {
                    sslContext = SslContextBuilder.forClient().trustManager(new File(serverConfig.getCaFile())).build();
                }
                else{
                    sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                }
            } catch (SSLException e) {
                throw new EtcdKeeperException(String.format("create ssl context for server config %s faild",
                        gson.toJson(serverConfig)), e);
            }

            if (serverConfig.isUseAuth()) {
                EtcdSecurityContext securityContext = new EtcdSecurityContext(sslContext, serverConfig.getUsername(), serverConfig.getPassword());
                nettyClient = new EtcdNettyClient(config, securityContext, uriArray);
            } else {
                nettyClient = new EtcdNettyClient(config, sslContext, uriArray);
            }

            client = new EtcdClient(nettyClient);
        } else {
            if (serverConfig.isUseAuth()) {
                EtcdSecurityContext securityContext = new EtcdSecurityContext(serverConfig.getUsername(), serverConfig.getPassword());
                nettyClient = new EtcdNettyClient(config, securityContext, uriArray);
            } else {
                nettyClient = new EtcdNettyClient(config, uriArray);
            }
            client = new EtcdClient(nettyClient);
        }

        //no try
        client.setRetryHandler(new RetryOnce(100));

        return client;
    }
}
