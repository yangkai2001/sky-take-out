package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    //插入数据
    void insert(Orders orders);



    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
//  * 分页条件查询并按下单时间排序
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
@Select("SELECT * from orders where id=#{id}")
OrderVO getById(Long id);

     // 根据状态统计订单数量

    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer confirmed);
}
