package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface  DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
     @Select("select count(id) from  dish  where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    //自动填充
    @AutoFill(value = OperationType.INSERT)
    void  insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    //根据主键id查询菜品
@Select("SELECT * from dish where id=#{id}")
    Dish getById(Long id);
//根据主键删除菜品
@Delete("delete from dish where id=#{id}")
    void deleteById(Long id);
//根据菜品id批量删除 使用动态sql
    void deleteByIds(List<Long> ids);
//修改菜品表数据有动态sql
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);
//动态条件查询菜品
    List<Dish> list(Dish dish);
    //根据套餐id查询菜品
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long id);
}
