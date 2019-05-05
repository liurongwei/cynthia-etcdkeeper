package com.cydia.etcdkeeper.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectForm {

    private int id;

    private String endpoints;

}
