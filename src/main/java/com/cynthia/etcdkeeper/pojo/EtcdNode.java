package com.cynthia.etcdkeeper.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class EtcdNode {
    private String key;
    private boolean dir;
    private Long createdIndex;
    private Long modifiedIndex;
    private String value;
    private Date expiration;
    private Long ttl;
    private List<EtcdNode> nodes;

    public EtcdNode(){
        this.nodes= new ArrayList<>();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public void setCreatedIndex(Long createdIndex) {
        this.createdIndex = createdIndex;
    }

    public void setModifiedIndex(Long modifiedIndex) {
        this.modifiedIndex = modifiedIndex;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public void setNodes(List<EtcdNode> nodes) {
        this.nodes = nodes;
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
