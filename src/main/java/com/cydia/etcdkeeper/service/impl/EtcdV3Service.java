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
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service("EtcdV3")
public class EtcdV3Service implements EtcdService {

    private final Gson gson = new Gson();

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    public EtcdInfoVo connect(ServerConfig serverConfig) {
        EtcdInfoVo etcdInfoVo;
        try (
                Client client = getClient(serverConfig);
        ) {

            KV kvClient = client.getKVClient();

            etcdInfoVo = new EtcdInfoVo();

            ByteSequence key = ByteSequence.from("foo".getBytes());
            CompletableFuture<GetResponse> response = kvClient.get(key);

            List<KeyValue> keyValues = null;
            try {
                keyValues = response.get().getKvs();
            } catch (InterruptedException e) {
                log.error(String.format("get keys from server %s faild , server config : %s",
                        serverConfig.getEndpoints(), gson.toJson(serverConfig)), e);
            } catch (ExecutionException e) {
                log.error(String.format("get keys from server %s faild , server config : %s",
                        serverConfig.getEndpoints(), gson.toJson(serverConfig)), e);
            }

            log.info(gson.toJson(keyValues));
        }

        return etcdInfoVo;
    }

    private Client getClient(@NotNull ServerConfig serverConfig) {

        List<String> endpoints = EtcdUtils.getEndpoints(serverConfig.getEndpoints(), serverConfig.isUseTls());

        ClientBuilder clientBuilder = Client.builder().endpoints(endpoints.toArray(new String[endpoints.size()]));

        if (serverConfig.isUseTls()) {
            try {
                clientBuilder.sslContext(SslContextBuilder
                        .forClient().trustManager(new File(serverConfig.getCaFile())).build());
            } catch (SSLException e) {
                throw new EtcdKeeperException(
                        String.format("ssl context build failed, server config: %s", gson.toJson(serverConfig)), e);
            }
        }

        if (serverConfig.isUseAuth()) {
            clientBuilder.user(ByteSequence.from(serverConfig.getUsername().getBytes()));
            clientBuilder.password(ByteSequence.from(serverConfig.getPassword().getBytes()));
        }

        return clientBuilder.build();

    }

    /**
     * edit key
     *
     * @param form form with key and value
     * @return edit node result
     */
    @Override
    public EtcdNode putKv(EditNodeForm form) {

        EtcdNode node = null;
        ServerConfig serverConfig = serverConfigRepository.getOne(form.getServerId());

        try(Client client =getClient(serverConfig)){

            KV kvClient = client.getKVClient();

            PutOption putOption = PutOption.newBuilder()
                    .build();
            CompletableFuture<PutResponse> putResponseCompletableFuture =
                    kvClient.put(ByteSequence.from( form.getKey().getBytes()),ByteSequence.from(form.getValue().getBytes()),putOption);

            PutResponse putResponse = putResponseCompletableFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            long revision = putResponse.getHeader().getRevision();
            if(revision > 0){
                node = new EtcdNode();
                node.key = new String(form.getKey().getBytes());
                node.value = new String(form.getValue().getBytes());
                node.createdIndex = revision;
                node.modifiedIndex = revision;
                node.ttl = (long)form.getTtl();
            }

        }
        catch (Exception e){
            throw new EtcdKeeperException(
                    String.format("execute put command failed,key: %s, server config : %s", form.getKey(), gson.toJson(serverConfig)), e);
        }

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

        EtcdNode node = null;

        ServerConfig serverConfig = serverConfigRepository.getOne(form.getServerId());

        try(Client client =getClient(serverConfig)) {

            KV kvClient = client.getKVClient();

            DeleteOption deleteOption = DeleteOption.newBuilder()
                    .withPrefix(ByteSequence.from( form.getKey().getBytes()))
                    .build();

            CompletableFuture<DeleteResponse> deleteResponseCompletableFuture =
                    kvClient.delete(ByteSequence.from( form.getKey().getBytes()),deleteOption);

            DeleteResponse deleteResponse = deleteResponseCompletableFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            if(deleteResponse!=null && deleteResponse.getDeleted()>0){
                node = new EtcdNode();
                node.key = new String(form.getKey().getBytes());
                node.value = new String(form.getValue().getBytes());
                node.createdIndex = deleteResponse.getHeader().getRevision();
                node.modifiedIndex = deleteResponse.getHeader().getRevision();
                node.ttl = (long)form.getTtl();
            }

        }
        catch (Exception e){
            throw new EtcdKeeperException(
                    String.format("execute delete command failed,key: %s, server config : %s", form.getKey(), gson.toJson(serverConfig)), e);
        }

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

        ServerConfig serverConfig = serverConfigRepository.getOne(query.getId());

        EtcdNode rootNode = new EtcdNode();
        rootNode.dir = true;
        rootNode.key = query.getKey();

        try (Client client = getClient(serverConfig)) {

            KV kvClient = client.getKVClient();

            GetOption getOption = GetOption.newBuilder()
                    .withSortField(GetOption.SortTarget.KEY)
                    .withPrefix(ByteSequence.from(rootNode.getKey().getBytes()))
                    .withKeysOnly(false)
                    .build();

            CompletableFuture<GetResponse> responseCompleteFuture =
                    kvClient.get(ByteSequence.from(rootNode.getKey().getBytes()), getOption);

            GetResponse getResponse = responseCompleteFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            List<KeyValue> kvList = getResponse.getKvs();

            if (kvList != null && !kvList.isEmpty()) {
                rootNode.nodes = new ArrayList<>();
                rootNode.dir = true;

                for (KeyValue kv : kvList
                ) {
                    EtcdNode node = new EtcdNode();
                    node.key = new String(kv.getKey().getBytes());
                    node.value = new String(kv.getValue().getBytes());
                    node.createdIndex = kv.getCreateRevision();
                    node.modifiedIndex = kv.getModRevision();
                    //node.dir = false;
                    node.ttl = kv.getLease();
                    rootNode.nodes.add(node);
                }

            }

        } catch (Exception e) {
            throw new EtcdKeeperException(
                    String.format("execute get path command failed, server config : %s", gson.toJson(serverConfig)), e);
        }
        return rootNode;
    }


    /**
     * query path
     *
     * @param query path query condition
     * @return EtcdNode object
     */
    @Override
    public EtcdNode getKey(GetPathQuery query) {
        ServerConfig serverConfig = serverConfigRepository.getOne(query.getId());
        EtcdNode node = null;
        try (Client client = getClient(serverConfig)) {

            KV kvClient = client.getKVClient();

            CompletableFuture<GetResponse> responseCompleteFuture =
                    kvClient.get(ByteSequence.from(query.getKey().getBytes()));

            GetResponse getResponse = responseCompleteFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            List<KeyValue> kvList = getResponse.getKvs();

            if (kvList != null && !kvList.isEmpty()) {

                node = new EtcdNode();

                KeyValue kv = kvList.get(0);
                node.key = new String(kv.getKey().getBytes());
                node.value = new String(kv.getValue().getBytes());
                node.createdIndex = kv.getCreateRevision();
                node.modifiedIndex = kv.getModRevision();
                node.ttl = kv.getLease();
            }

        } catch (Exception e) {
            throw new EtcdKeeperException(
                    String.format("execute get path command failed,key: %s, server config : %s", query.getKey(), gson.toJson(serverConfig)), e);
        }
        return node;
    }
}
