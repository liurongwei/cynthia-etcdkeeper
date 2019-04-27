package com.cydia.etcdkeeper.vo;

import lombok.Data;

@Data
public class UserVo {

    private Integer id;
    private String name;
    private int age;
    private String address;

    public UserVo(Integer id, String name, int age, String address) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
    }
}
