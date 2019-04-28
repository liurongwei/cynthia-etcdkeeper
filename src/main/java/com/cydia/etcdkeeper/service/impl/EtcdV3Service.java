package com.cydia.etcdkeeper.service.impl;

import com.cydia.etcdkeeper.service.EtcdService;
import com.cydia.etcdkeeper.vo.EtcdConnectResultVo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service("EtcdV3")
public class EtcdV3Service implements EtcdService {


    public EtcdConnectResultVo connect(String host, int port, boolean usetls, String keyFile,
                                       String certFile, String caFile) throws ExecutionException,InterruptedException {
        EtcdConnectResultVo resultVo= new EtcdConnectResultVo();

        String schema = usetls ? "https": "http";

        Client client= Client.builder().endpoints(String.format("%s://%s:%s", schema, host, port)).build();

        KV kvClient = client.getKVClient();

        ByteSequence key = ByteSequence.from("foo".getBytes());
        CompletableFuture<GetResponse> response = kvClient.get(key);

        List<KeyValue> keyValues = response.get().getKvs();

        keyValues.get(0);

        return resultVo;
    }
}
