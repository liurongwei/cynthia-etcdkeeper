package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.exception.EtcdKeeperException;
import com.cydia.etcdkeeper.pojo.EtcdNode;
import com.cydia.etcdkeeper.req.EditNodeForm;
import com.cydia.etcdkeeper.req.GetPathQuery;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.utils.EtcdUtils;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;
import com.google.gson.Gson;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service("EtcdV3")
public class EtcdV3Service implements EtcdService {

    private final Gson gson = new Gson();

    public EtcdInfoVo connect(ServerConfig serverConfig) {
        EtcdInfoVo resultVo = new EtcdInfoVo();

        List<String> endpoints = EtcdUtils.getEndpoints(serverConfig.getEndpoints(), serverConfig.isUseTls());

        if (endpoints == null || endpoints.size() < 1) {
            throw new EtcdKeeperException(String.format("etcd endpoints needs to specified, config details: $s",
                    gson.toJson(serverConfig)));
        }

        Client client = Client.builder().endpoints(endpoints.toArray(new String[endpoints.size()])).build();

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
