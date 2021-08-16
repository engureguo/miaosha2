package com.engure.seckill.exception;


import com.engure.seckill.vo.RespBean;
import com.engure.seckill.vo.RespTypeEnum;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {

    /**
     * 一般的异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public RespBean commonExHandler(Exception ex) {
        return RespBean.error(RespTypeEnum.ERROR);
    }

    /**
     * 全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = GlobalException.class)
    public RespBean exceptionHandler(GlobalException e) {
        RespTypeEnum respTypeEnum = e.getRespTypeEnum();
        return RespBean.error(respTypeEnum);
    }

    /**
     * 校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public RespBean exHandler(BindException e) {
        StringBuilder s = new StringBuilder("JSR303绑定异常：");
        for (ObjectError ex : e.getAllErrors()) {
            s.append(ex.getDefaultMessage()).append(";");
        }
        RespBean err = RespBean.error(RespTypeEnum.VALIDATE_ERROR);
        err.setMsg(s.toString());
        return err;
    }


}
