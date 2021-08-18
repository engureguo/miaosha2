package com.engure.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.engure.seckill.pojo.Order;
import com.engure.seckill.pojo.SeckillOrder;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.service.IOrderService;
import com.engure.seckill.service.ISeckillOrderService;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.RespTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * 秒杀接口
 *
 *
 * 压测： /seckill/doSeckill
 *  QPS 1000 * 10:   前          后
 *      windows    1339
 *
 *
 */

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @RequestMapping("doSeckill")
    public String kill(User user, Long goodsId, Model model) {

        if (user == null) return "login";
        if (goodsId == null) return "redirect:/goods/toList";

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断秒杀库存，而不是商品库存
        if (goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg", RespTypeEnum.OUT_OF_STOCK.getMsg());
            return "secKillFail";
        }

        // 判断不得多买
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId)); // 在秒杀记录中查看用户是否秒杀过该商品
        if (null != seckillOrder) {
            model.addAttribute("errmsg", RespTypeEnum.REPEATED_BUY_ERROR.getMsg());
            return "secKillFail";
        }

        Order order = orderService.seckill(user, goodsVo);

        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);

        return "orderDetail";
    }

}
