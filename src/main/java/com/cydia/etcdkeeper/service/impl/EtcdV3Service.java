package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.config.EtcdConfig;
import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.exception.EtcdKeeperException;
import com.cydia.etcdkeeper.pojo.EtcdNode;
import com.cydia.etcdkeeper.repository.ServerConfigRepository;
import com.cydia.etcdkeeper.req.EditNodeForm;
import com.cydia.etcdkeeper.req.GetPathQuery;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.utils.EtcdUtils;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;
import com.google.gson.Gson;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service("EtcdV3")
public class EtcdV3Service implements EtcdService {

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    private final Gson gson = new Gson();

    private Client getClient(ServerConfig serverConfig){

        List<String> endpoints = EtcdUtils.getEndpoints(serverConfig.getEndpoints(), serverConfig.isUseTls());
        ClientBuilder clientBuilder = Client.builder().endpoints(endpoints.toArray(new String[endpoints.size()]));

        if(serverConfig.isUseAuth()){
            clientBuilder.user(ByteSequence.from( serverConfig.getUsername().getBytes()));
            clientBuilder.password(ByteSequence.from(serverConfig.getPassword().getBytes()));
        }

        if(serverConfig.isUseTls()){
            try {
                SslContext sslContext = SslContextBuilder.forClient().trustManager(new File(serverConfig.getCaFile())).build();
            } catch (SSLException e) {
                log.error(String.format("build ssl context error, server config : %s",gson.toJson(serverConfig)),e);
                throw new EtcdKeeperException(String.format("build ssl context error, server config : %s",gson.toJson(serverConfig)), e);
            }
        }

        return clientBuilder.build();
    }

    public EtcdInfoVo connect(ServerConfig serverConfig) {
        EtcdInfoVo resultVo = new EtcdInfoVo();

        try(Client client = getClient(serverConfig)){

            KV kvClient = client.getKVClient();

            ByteSequence key = ByteSequence.from("foo".getBytes());
            CompletableFuture<GetResponse> response = kvClient.get(key);

            List<KeyValue> keyValues = null;
            try {
                keyValues = response.get().getKvs();
            } catch (InterruptedException e) {
                log.error(String.format("get keys from server %s faild , server config : %s",
                        serverConfig.getEndpoints(), gson.toJson(serverConfig)), e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error(String.format("get keys from server %s faild , server config : %s",
                        serverConfig.getEndpoints(), gson.toJson(serverConfig)), e);
            }

            keyValues.get(0);
        }

        return resultVo;
    }

    /**
     * edit key
     *
     * @param form form with key and value
     * @return edit node result
     */
    @Override
    public EtcdNode putKv(EditNodeForm form) {
        return null;
    }

    /**
     * delete node form specified server
     *
     * @param form node & server parameters
     * @return deleted nodes
     */
    @Override
    public EtcdNode delete(EditNodeForm form) {
        return null;
    }

    /**
     * query path
     *
     * @param query path query condition
     * @return EtcdNode object
     */
    @Override
    public EtcdNode getPath(GetPathQuery query) {
        return null;
    }

    /**
     * query path
     *
     * @param query path query condition
     * @return EtcdNode object
     */
    @Override
    public EtcdNode getKey(GetPathQuery query) {
        return null;
    }
}
