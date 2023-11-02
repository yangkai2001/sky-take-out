package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> list(ShoppingCart shoppingCart);
//修改购物车数据
@Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberByid(ShoppingCart shoppingCart);

//插入购物车数据
@Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time)"+
"values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);
//根据用户id来全部删除购物车数据
@Delete("delete from shopping_cart where user_id=#{userId}")
    void deletByUserId(Long userId);
@Delete("delete from shopping_cart where id=#{id}")
    void deletById(Long userId);
}
