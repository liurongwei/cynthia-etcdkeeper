package com.cynthia.etcdkeeper.vo;

import com.cynthia.etcdkeeper.enums.ResultCodes;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonResult<T> {

    private int status;

    private String message;

    private T data;

    public JsonResult success(){
        this.setStatus(ResultCodes.SUCCESS.getCode());
        this.setMessage(ResultCodes.SUCCESS.getMessage());
        return this;
    }

    public JsonResult failed(){
        this.setStatus(ResultCodes.ERROR.getCode());
        this.setMessage(ResultCodes.ERROR.getMessage());
        return this;
    }
}
