package com.cydia.etcdkeeper.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * class_name: EtcdUtils
 * package: com.cydia.etcdkeeper.utils
 * describe: etcd utils
 * create_user: liurongwei@yiche.com
 * create_date: 2019/4/29
 * create_time: 13:07
 **/
@Slf4j
public final class EtcdUtils {

    private EtcdUtils(){

    }

    /**
     * get endpoints
     * @param endpoints endpoint list string
     * @param useTls is use tls
     * @return formated endpoint list
     */
    public static List<String> getEndpoints(String endpoints , boolean useTls){

        String schema = useTls ? "https" : "http";

        if (StringUtils.isBlank(endpoints)) {
            return new ArrayList<>();
        }

        List<String> endpointList = new ArrayList<>();
        String[] endpointArray = endpoints.split(",");
        for (int i = 0; i < endpointArray.length; i++) {
            String endpoint = endpointArray[i];
            if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
                endpoint = endpoint.split("//")[1];
            }
            endpoint = schema + "://" + endpoint;
            endpointList.add(endpoint);
        }

        return endpointList;
    }

    /**
     * get uri list of a endpoints string
     * @param endpoints endpints string
     * @param useTls is use tls
     * @return uri list
     */
    public static List<URI> getUris(String endpoints , boolean useTls){
        List<String> endpointList =  getEndpoints(endpoints,useTls);

        List<URI> uriList = new ArrayList<>();
        for (String endpoint: endpointList
                ) {
            try {
                uriList.add( new URI(endpoint));
            } catch (URISyntaxException e) {
                log.error(String.format("endpoint %s is a invalid uri string", endpoints),e);
            }
        }

        return uriList;
    }
}
