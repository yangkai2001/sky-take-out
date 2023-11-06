package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//spring task
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    //处理超时订单的方法
    @Scheduled(cron = "1 * * * *?")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        //当前时间减15分钟来与创建订单时间做对比
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

      List<Orders> ordersList=orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);
      if (ordersList!=null && ordersList.size()>0) {
          for (Orders orders : ordersList) {
           orders.setStatus(Orders.CANCELLED);//将订单状态改为也取消
           orders.setCancelReason("订单超时，自动取消");//取消原因
           orders.setCancelTime(LocalDateTime.now());//取消时间
           orderMapper.update(orders);//更改
          }
      }
    }
    //处理一直在派送中的订单
    @Scheduled(cron = "0 0 1 * * ? ")//每天凌晨一点处理
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,time);
        if (ordersList!=null &&ordersList.size()>0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);//将订单状态改为也取消
                orderMapper.update(orders);//更改
            }
        }

    }


}
