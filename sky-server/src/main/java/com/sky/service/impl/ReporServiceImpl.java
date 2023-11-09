package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReporService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporServiceImpl implements ReporService {
    //统计指定时间区间内的营业额数据
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天的营业额
        List<Double> turrnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额指的是一天中：状态为“已完成”的订单金额合计
            //因为表的订单时间准确到了秒，故要准确的描绘一天时间的秒集
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);//订单为已完成的
            Double turnover = orderMapper.sumByMap(map);//计算一天的总营业额
            turnover = turnover == null ? 0.0 : turnover;//判断一天的营业额是否为空，如果为空将其赋值为0.0，因为为空的时候数据以为空
            turrnoverList.add(turnover);
        }


        //使用StringUtils工具类将datelist集合中数据取出并在其间加”，”分隔
        //使用StringUtils工具类将turrnoverList集合中数据取出并在其间加”，”分隔
        return TurnoverReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turrnoverList, ","))
                .build();


    }

    // 统计指定时间区间内的用户数据
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的用户总量
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额指的是一天中：状态为“已完成”的订单金额合计
            //因为表的订单时间准确到了秒，故要准确的描绘一天时间的秒集
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
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
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //每天的有效订单
        List<Integer> validOrderCountList = new ArrayList<>();
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
        Integer toltalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
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
        Double orderCompletionRate = 0.0;
        if (toltalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / toltalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(toltalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    //根据条件查询订单数量
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    //查询排名前10的销量
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder().nameList(StringUtils.join(names))
                .numberList(StringUtils.join(numbers)).build();
    }

    //导出运营数据报表
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1查询数据库,获取营业数据--查询最近30天的数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //通过POI将数据写入到excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板创建一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //获取表格文件的sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);
            //获取第四行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获取第五行
            row=sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date=dateBegin.plusDays(i);
                BusinessDataVO businessDate=workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));
                //获得一行
                row=sheet.getRow(i+7);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessDate.getTurnover());
                row.getCell(3).setCellValue(businessDate.getValidOrderCount());
                row.getCell(4).setCellValue(businessDate.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDate.getUnitPrice());
                row.getCell(6).setCellValue(businessDate.getNewUsers());

            }


            //通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream stream = response.getOutputStream();
            excel.write(stream);
            stream.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
