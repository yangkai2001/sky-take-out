package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReporService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporServiceImpl implements ReporService {
    //统计指定时间区间内的营业额数据
@Autowired
private OrderMapper orderMapper;
@Autowired
private UserMapper userMapper;
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算指定日期的后一天对应的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天的营业额
        List<Double> turrnoverList=new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额指的是一天中：状态为“已完成”的订单金额合计
            //因为表的订单时间准确到了秒，故要准确的描绘一天时间的秒集
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);//订单为已完成的
            Double turnover=orderMapper.sumByMap(map);//计算一天的总营业额
            turnover=turnover==null? 0.0:turnover;//判断一天的营业额是否为空，如果为空将其赋值为0.0，因为为空的时候数据以为空
            turrnoverList.add(turnover);
        }





        //使用StringUtils工具类将datelist集合中数据取出并在其间加”，”分隔
        //使用StringUtils工具类将turrnoverList集合中数据取出并在其间加”，”分隔
return TurnoverReportVO.builder().dateList(StringUtils.join(dateList,","))
        .turnoverList(StringUtils.join(turrnoverList,","))
        .build();




    }

 // 统计指定时间区间内的用户数据
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算指定日期的后一天对应的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天的新增用户数量
        List<Integer> newUserList=new ArrayList<>();
        //存放每天的用户总量
        List<Integer> totalUserList=new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额指的是一天中：状态为“已完成”的订单金额合计
            //因为表的订单时间准确到了秒，故要准确的描绘一天时间的秒集
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();
            map.put("end",endTime);
            //先获取总用户数量
            Integer toltalUser = userMapper.countByMap(map);
            //获取一天新增的用户数量
            map.put("begin", beginTime);
          Integer newUser = userMapper.countByMap(map);
            totalUserList.add(toltalUser);
            newUserList.add(newUser);
        }
        //封装结果，和返回数据
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }


    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算指定日期的后一天对应的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //每天的订单总数
        List<Integer> orderCountList=new ArrayList<>();
        //每天的有效订单
        List<Integer> validOrderCountList=new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询每天的订单总量
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //每天的有效订单数,订单状态为5，以确定的订单
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //计算订单总和
        //1,用方法stream().reduce(Integer::sum).get();直接获取总数
        Integer toltalOrderCount=orderCountList.stream().reduce(Integer::sum).get();
       // 2，遍历集合获取总量
//
//      Integer toltalOrderCount=0;
//       for (Integer i : orderCountList) {
//
//            toltalOrderCount+=i;     }

        //计算有效订单的总数
        //1,用方法stream().reduce(Integer::sum).get();直接获取总数
     Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        // 2，遍历集合获取总量
 //      Integer validOrderCount=0;
//       for (Integer i : validOrderCountList) {
//
//           validOrderCount+=i;
//       }
        //计算有效率
        Double orderCompletionRate=0.0;
        if (toltalOrderCount!=0){
            orderCompletionRate=validOrderCount.doubleValue()/toltalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(toltalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
    //根据条件查询订单数量
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countByMap(map);
    }
}
