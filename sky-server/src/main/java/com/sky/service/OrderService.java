package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.stereotype.Service;

public interface OrderService {
    //添加订单数据
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
}
