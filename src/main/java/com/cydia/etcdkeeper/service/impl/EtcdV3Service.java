package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.exception.EtcdKeeperException;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.utils.EtcdUtils;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import com.cydia.etcdkeeper.vo.EtcdInfoVo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
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

        if (endpoints== null || endpoints.size()<1 ) {
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
                    serverConfig.getEndpoints(), gson.toJson(serverConfig)),e);
        } catch (ExecutionException e) {
            log.error(String.format("get keys from server %s faild , server config : %s",
                    serverConfig.getEndpoints(), gson.toJson(serverConfig)),e);
        }

        keyValues.get(0);

        return resultVo;
    }
}
