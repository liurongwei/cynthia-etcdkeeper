package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.exception.EtcdKeeperException;
import com.cydia.etcdkeeper.pojo.EtcdNode;
import com.cydia.etcdkeeper.pojo.EtcdWrapperNode;
import com.cydia.etcdkeeper.repository.ServerConfigRepository;
import com.cydia.etcdkeeper.req.EditNodeForm;
import com.cydia.etcdkeeper.req.EtcdClientForm;
import com.cydia.etcdkeeper.req.GetPathQuery;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.utils.EtcdUtils;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;
import com.google.gson.Gson;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import mousio.client.retry.RetryOnce;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.EtcdSecurityContext;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
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
import java.util.concurrent.TimeoutException;

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

    public EtcdConnectResultVo connect(String endpoint, boolean usetls, String keyFile,
                                       String certFile, String caFile) {
        EtcdConnectResultVo resultVo = new EtcdConnectResultVo();

        try (EtcdClient client = getClient(endpoint, usetls)) {

            EtcdHealthResponse healthResponse = client.getHealth();
            if (healthResponse != null) {
                resultVo.setInfo(new EtcdInfoVo());

                EtcdSelfStatsResponse selfStatsResponse = client.getSelfStats();
                EtcdVersionResponse versionResponse = client.version();

                resultVo.getInfo().setVersion(versionResponse.server);
                resultVo.getInfo().setName(selfStatsResponse.getName());
                resultVo.getInfo().setSize("unknow");
                resultVo.setStatus("true".equals(healthResponse.getHealth()) ? "ok" : "error");
            } else {
                resultVo.setStatus("timeout");
                resultVo.setMessage(String.format("endpoint %s is unreachable", endpoint));
            }
        } catch (IOException e) {
            resultVo.setStatus("error");
            resultVo.setMessage("cann't get server status," + e.getLocalizedMessage());
        }

        return resultVo;
    }

    private EtcdClient getClient(String endpoint, boolean usetls) {

        String hostAndPort = endpoint;
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            hostAndPort = endpoint.split("//")[1];
        }

        String schema = usetls ? "https" : "http";

        EtcdNettyConfig config = new EtcdNettyConfig();
        config.setConnectTimeout(etcdConfig.getClient().getTimeout());
        EtcdNettyClient nettyClient = new EtcdNettyClient(config,
                URI.create(String.format("%s://%s", schema, hostAndPort)));
        EtcdClient client = new EtcdClient(nettyClient);

        //no try
        client.setRetryHandler(new RetryOnce(100));

        return client;
    }

    public EtcdWrapperNode getPath(EtcdClientForm clientForm, String key, boolean prefix) {

        EtcdWrapperNode rootNode = null;

        try (EtcdClient client = getClient(clientForm.getEndpoint(), false)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.get(key).recursive().sorted().send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode = new EtcdWrapperNode();
            rootNode.setNode(node);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    public EtcdWrapperNode getKey(EtcdClientForm clientForm, String key) {
        EtcdWrapperNode rootNode = null;

        try (EtcdClient client = getClient(clientForm.getEndpoint(), false)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.get(key).send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode = new EtcdWrapperNode();
            rootNode.setNode(node);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    public EtcdWrapperNode put(EtcdClientForm clientForm, EditNodeForm nodeForm) {
        EtcdWrapperNode rootNode = null;

        try (EtcdClient client = getClient(clientForm.getEndpoint(), false)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise;

            if (nodeForm.isDir()) {
                responsePromise = client.putDir(nodeForm.getKey())
                        .ttl(nodeForm.getTtl()==null || nodeForm.getTtl() <= 0 ? null : nodeForm.getTtl()).send();
            } else {
                responsePromise = client.put(nodeForm.getKey(), nodeForm.getValue())
                        .ttl(nodeForm.getTtl()==null || nodeForm.getTtl() <= 0 ? null : nodeForm.getTtl()).send();
            }

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode = new EtcdWrapperNode();
            rootNode.setNode(node);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

    public EtcdWrapperNode deleteKey(EtcdClientForm clientForm, EditNodeForm nodeForm) {
        EtcdWrapperNode rootNode = null;

        try (EtcdClient client = getClient(clientForm.getEndpoint(), false)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = null;

            if (nodeForm.isDir()) {
                responsePromise = client.deleteDir(nodeForm.getKey()).recursive().send();
            } else {
                responsePromise = client.delete(nodeForm.getKey()).send();
            }

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            rootNode = new EtcdWrapperNode();
            rootNode.setNode(node);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rootNode;
    }

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
            if (healthResponse != null) {
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

        ServerConfig serverConfig = serverConfigRepository.getOne(query.getId());

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.getDir(query.getKey()).recursive().sorted().send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            String nodeJson = gson.toJson(node);

            rootNode = gson.fromJson(nodeJson, EtcdNode.class);

            if(StringUtils.isEmpty( rootNode.key)){
                rootNode.key = etcdConfig.getSeparator();
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

        ServerConfig serverConfig = serverConfigRepository.getOne(query.getId());

        try (EtcdClient client = getClient(serverConfig)) {

            EtcdResponsePromise<EtcdKeysResponse> responsePromise = client.get(query.getKey()).send();

            EtcdKeysResponse.EtcdNode node = responsePromise.get().node;

            String nodeJson = gson.toJson(node);

            rootNode = gson.fromJson(nodeJson, EtcdNode.class);

            if(StringUtils.isEmpty( rootNode.key)){
                rootNode.key = etcdConfig.getSeparator();
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
                /*sslContext = SslContextBuilder.forClient().keyManager(new File(serverConfig.getCertFile()),
                        new File(serverConfig.getKeyFile())).build();*/

                sslContext = SslContextBuilder.forClient().trustManager(new File(serverConfig.getCaFile())).build();
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
