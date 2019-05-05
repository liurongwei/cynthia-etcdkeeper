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
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.maintenance.StatusResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("EtcdV3")
public class EtcdV3Service implements EtcdService {

    @Autowired
    private EtcdConfig etcdConfig;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    private final Gson gson = new Gson();

    private Client getClient(@NotNull ServerConfig serverConfig) {

        List<String> endpoints = EtcdUtils.getEndpoints(serverConfig.getEndpoints(), serverConfig.isUseTls());

        ClientBuilder clientBuilder = Client.builder().endpoints(endpoints.toArray(new String[endpoints.size()]));

        if (serverConfig.isUseTls()) {
            try {
                SslContext sslContext = GrpcSslContexts.forClient()
                        //.trustManager(caFile)
                        //.keyManager(certFile, keyFile)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                clientBuilder.sslContext(sslContext);
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

    public EtcdInfoVo connect(ServerConfig serverConfig) {
        EtcdInfoVo etcdInfoVo;
        try (
                Client client = getClient(serverConfig)
        ) {
            Maintenance maintenanceClient = client.getMaintenanceClient();
            CompletableFuture<StatusResponse> statusResponseCompletableFuture =
                    maintenanceClient.statusMember(EtcdUtils.getUris(serverConfig.getEndpoints(), serverConfig.isUseTls()).get(0));
            StatusResponse statusResponse = statusResponseCompletableFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            etcdInfoVo = new EtcdInfoVo();
            etcdInfoVo.setVersion(statusResponse.getVersion());
            etcdInfoVo.setSize(FileUtils.byteCountToDisplaySize(statusResponse.getDbSize()));
            etcdInfoVo.setName("");

        } catch (Exception e) {
            throw new EtcdKeeperException(
                    String.format("connect to endpoints: %s failed, %s,server config : %s", serverConfig.getEndpoints()
                            , e.getMessage()
                            , gson.toJson(serverConfig)), e);
        }

        return etcdInfoVo;
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

        try (Client client = getClient(serverConfig)) {

            KV kvClient = client.getKVClient();

            PutOption.Builder putOptionBuilder = PutOption.newBuilder();

            if (form.getTtl() != null && form.getTtl() > 0) {
                Lease leaseClient = client.getLeaseClient();
                CompletableFuture<LeaseGrantResponse> leaseGrantResponseCompletableFuture = leaseClient.grant(form.getTtl());

                LeaseGrantResponse leaseGrantResponse = leaseGrantResponseCompletableFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

                if (leaseGrantResponse.getID() > 0) {
                    putOptionBuilder.withLeaseId(leaseGrantResponse.getID());
                }
            }


            CompletableFuture<PutResponse> putResponseCompletableFuture =
                    kvClient.put(ByteSequence.from(form.getKey().getBytes()), ByteSequence.from(form.getValue().getBytes()), putOptionBuilder.build());


            PutResponse putResponse = putResponseCompletableFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            long revision = putResponse.getHeader().getRevision();
            if (revision > 0) {
                node = new EtcdNode();
                node.setKey(new String(form.getKey().getBytes()));
                if (form.getValue() != null) {
                    node.setValue(new String(form.getValue().getBytes()));
                }
                node.setCreatedIndex(revision);
                node.setModifiedIndex(revision);
                if (form.getTtl() != null) {
                    node.setTtl((long) form.getTtl());
                }
            }

        } catch (Exception e) {
            throw new EtcdKeeperException(
                    String.format("execute put command failed,key: %s, server config : %s", form.getKey(), gson.toJson(serverConfig)), e);
        }

        return node;
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

        try (Client client = getClient(serverConfig)) {

            KV kvClient = client.getKVClient();

            DeleteOption deleteOption = DeleteOption.newBuilder()
                    .withPrefix(ByteSequence.from(form.getKey().getBytes()))
                    .build();

            CompletableFuture<DeleteResponse> deleteResponseCompletableFuture =
                    kvClient.delete(ByteSequence.from(form.getKey().getBytes()), deleteOption);

            DeleteResponse deleteResponse = deleteResponseCompletableFuture.get(etcdConfig.getClient().getTimeout(), TimeUnit.MILLISECONDS);

            if (deleteResponse == null || deleteResponse.getDeleted() == 0) {
                throw new EtcdKeeperException(
                        String.format("delete key:%s failed, it dose n't exists, form : %s", form.getKey(), gson.toJson(form)));
            }

            node = new EtcdNode();
            node.setKey(new String(form.getKey().getBytes()));
            if (form.getValue() != null) {
                node.setValue(new String(form.getValue().getBytes()));
            }
            node.setCreatedIndex(deleteResponse.getHeader().getRevision());
            node.setModifiedIndex(deleteResponse.getHeader().getRevision());
            if (form.getTtl() != null) {
                node.setTtl((long) form.getTtl());
            }

        } catch (Exception e) {
            throw new EtcdKeeperException(
                    String.format("execute delete command failed,key: %s, server config : %s", form.getKey(), gson.toJson(serverConfig)), e);
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
    public EtcdNode getPath(GetPathQuery query) {

        ServerConfig serverConfig = serverConfigRepository.getOne(query.getServerId());

        EtcdNode rootNode = new EtcdNode();
        rootNode.setDir(true);
        rootNode.setKey(query.getKey());

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
                rootNode.setDir(true);

                for (KeyValue kv : kvList
                ) {
                    EtcdNode node = new EtcdNode();
                    node.setKey(new String(kv.getKey().getBytes()));
                    node.setValue(new String(kv.getValue().getBytes()));
                    node.setCreatedIndex(kv.getCreateRevision());
                    node.setModifiedIndex(kv.getModRevision());
                    node.setTtl(kv.getLease());
                    rootNode.getNodes().add(node);
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
        ServerConfig serverConfig = serverConfigRepository.getOne(query.getServerId());
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
                node.setKey(new String(kv.getKey().getBytes()));
                node.setValue(new String(kv.getValue().getBytes()));
                node.setCreatedIndex(kv.getCreateRevision());
                node.setModifiedIndex(kv.getModRevision());
                node.setTtl(kv.getLease());
            }

        } catch (Exception e) {
            throw new EtcdKeeperException(
                    String.format("execute get path command failed,key: %s, server config : %s", query.getKey(), gson.toJson(serverConfig)), e);
        }
        return node;
    }
}
