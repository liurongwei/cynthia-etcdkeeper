package com.cydia.etcdkeeper.form;

import com.cydia.etcdkeeper.annotations.CookieProperty;
import lombok.Data;

@Data
public class EtcdClientForm {
    @CookieProperty("etcd-endpoint")
    private String endpoint;

    @CookieProperty("etcd-version")
    private String apiVersion;

    @CookieProperty("tree-mode")
    private String treeMode;
}
