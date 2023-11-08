package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReporService {
    //统计指定时间区间内的营业额数据

    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
    //统计指定时间区间内的用户数据

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
//订单统计
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);
}
