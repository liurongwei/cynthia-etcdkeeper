package com.cydia.etcdkeeper.pojo;

import lombok.Data;
import mousio.etcd4j.responses.EtcdKeysResponse;

import java.util.List;

@Data
public class EtcdWrapperNode {

    private EtcdKeysResponse.EtcdNode node;
}
