package com.cynthia.etcdkeeper.req;

import lombok.Data;

@Data
public class GetPathQuery {

    private String key;

    private boolean prefix;

    private int serverId;
}
