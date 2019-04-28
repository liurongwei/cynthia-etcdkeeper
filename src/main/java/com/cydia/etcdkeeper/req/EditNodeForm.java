package com.cydia.etcdkeeper.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditNodeForm {

    private boolean dir;

    private String key;

    private String value;

    private int ttl;
}
