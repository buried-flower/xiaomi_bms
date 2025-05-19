package org.example.mi_bms.response;
import org.example.mi_bms.enums.ResultEnum;
import lombok.Data;

@Data
public class R<T> {
    //错误码
    private Integer code;

    //提示信息
    private String msg;

    //具体内容
    private T data;

    //通过字符串构造返回类
    public R(String s) {
        this.code = 10000;
        this.msg = s;
        this.data = null;
    }

    //私有构造类 通过enum枚举类构造返回类
    private R(ResultEnum resultEnum, T data) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
        this.data = data;
    }

    //发生常见异常时通过String构造返回类
    public static R error(String s) {
        return new R(s);
    }

    //成功的返回类
    public static<S> R success(S data) {
        return new R(ResultEnum.SUCCESS, data);  //将成功返回的data数据传进去
    }
}
