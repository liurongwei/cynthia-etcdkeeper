package com.cydia.etcdkeeper.req;

import lombok.Data;

@Data
public class GetPathQuery {

    private String key;

    private boolean prefix;
}
