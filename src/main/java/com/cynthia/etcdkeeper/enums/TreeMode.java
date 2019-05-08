package com.cynthia.etcdkeeper.enums;

/**
 * class_name: TreeMode
 * package: com.cynthia.etcdkeeper.enums
 * describe: the tree list mode
 * create_user: liurongwei@yiche.com
 * create_date: 2019/5/6
 * create_time: 13:33
 **/
public enum  TreeMode {
    LIST("list"),PATH("path");

    private String value;
    private TreeMode(String value){
        this.value = value;
    }
}
