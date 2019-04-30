package com.cydia.etcdkeeper.enums;

import lombok.Getter;

/**
 * class_name: ResultCodes
 * package: com.cydia.etcdkeeper.enums
 * describe: api code enums
 * create_user: liurongwei@yiche.com
 * create_date: 2019/4/29
 * create_time: 9:39
 **/
public enum  ResultCodes {
    SUCCESS(0,"success"),
    ERROR(500, "server failed")
    ;

    @Getter
    private int code;

    @Getter
    private String message;

    private ResultCodes(int code,String message){
        this.code = code;
        this.message= message;
    }
}
