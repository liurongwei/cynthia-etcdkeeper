package com.cydia.etcdkeeper.pojo;

import java.util.Date;
import java.util.List;

public final class EtcdNode {
    public String key;
    public boolean dir;
    public Long createdIndex;
    public Long modifiedIndex;
    public String value;
    public Date expiration;
    public Long ttl;
    public List<EtcdNode> nodes;

    public EtcdNode(){

    }

    public String getKey() {
        return this.key;
    }

    public boolean isDir() {
        return this.dir;
    }

    public Long getCreatedIndex() {
        return this.createdIndex;
    }

    public Long getModifiedIndex() {
        return this.modifiedIndex;
    }

    public String getValue() {
        return this.value;
    }

    public Date getExpiration() {
        return this.expiration;
    }

    public Long getTTL() {
        return this.ttl;
    }

    public List<EtcdNode> getNodes() {
        return this.nodes;
    }

    public String toString() {
        return "EtcdNode{key='" + this.key + '\'' + ", value='" + this.value + '\'' + ", dir=" + this.dir + ", createdIndex=" + this.createdIndex + ", modifiedIndex=" + this.modifiedIndex + ", expiration=" + this.expiration + ", ttl=" + this.ttl + ", nodes=" + this.nodes + '}';
    }
}
