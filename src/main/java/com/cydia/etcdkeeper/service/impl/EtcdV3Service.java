package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.exception.EtcdKeeperException;
import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service("EtcdV3")
public class EtcdV3Service implements EtcdService {

    private final Gson gson = new Gson();

    public EtcdConnectResultVo connect(ServerConfig serverConfig) throws ExecutionException, InterruptedException {
        EtcdConnectResultVo resultVo = new EtcdConnectResultVo();

        String schema = serverConfig.isUseTls() ? "https" : "http";

        if (StringUtils.isBlank(serverConfig.getEndpoints())) {
            throw new EtcdKeeperException(String.format("etcd endpoints needs to specified, config details: $s",
                    gson.toJson(serverConfig)));
        }

        List<String> endpoints = Lists.newArrayList(serverConfig.getEndpoints().split(","));
        for (int i = 0; i < endpoints.size(); i++) {
            String endpoint = endpoints.get(i);
            if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
                endpoint = endpoint.split("//")[1];
            }
            endpoint = (serverConfig.isUseTls() ? "https" : "http") + "://" + endpoint;
        }

        Client client = Client.builder().endpoints(endpoints.toArray(new String[endpoints.size()])).build();

        KV kvClient = client.getKVClient();

        ByteSequence key = ByteSequence.from("foo".getBytes());
        CompletableFuture<GetResponse> response = kvClient.get(key);

        List<KeyValue> keyValues = response.get().getKvs();

        keyValues.get(0);

        return resultVo;
    }
}
