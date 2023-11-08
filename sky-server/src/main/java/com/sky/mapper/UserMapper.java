package com.sky.mapper;

import com.sky.entity.User;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);
//插入数据
    void insert(User user);
@Select("select * from user where id=#{id}")
    User getById(Long userId);



//根据动态条件来统计用户数量
    Integer countByMap(Map map);

}
