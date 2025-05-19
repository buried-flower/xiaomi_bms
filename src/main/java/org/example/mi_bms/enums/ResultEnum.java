package org.example.mi_bms.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
    SUCCESS(200,"ok"),
    ERROR(1,"系统出现故障，请迅速联系管理员"),
    HTTP_NOT_FOUND(404,"未找到相关内容"),
    SERVER_ERROR( 500, "服务器忙，请稍后在试");

    private final int code;
    private final String msg;

    ResultEnum(Integer code,String msg){
        this.code=code;
        this.msg=msg;
    }
}
