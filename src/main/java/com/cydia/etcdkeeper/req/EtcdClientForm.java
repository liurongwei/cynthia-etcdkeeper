package com.cydia.etcdkeeper.req;

import com.cydia.etcdkeeper.annotations.CookieProperty;
import lombok.Data;

@Data
public class EtcdClientForm {
    @CookieProperty("etcd-endpoint")
    private String endpoint;

    @CookieProperty("etcd-version")
    private String apiVersion;

    @CookieProperty("tree-mode")
    private String treeMode="path";
}
