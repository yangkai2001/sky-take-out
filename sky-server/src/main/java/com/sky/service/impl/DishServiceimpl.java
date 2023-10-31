package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceimpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmeaIDishMapper;


    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品表插入一条数据
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }

    }

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult((page.getTotal()), page.getResult());
    }

    //菜品批量删除
    @Transactional
    public void  deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够被删除--是否在起售中
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //当前处于起售中不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

        }
        //判断当前菜品是否能够删除--是否被套餐关联了？？
        List<Long> setmealIds = setmeaIDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

        }
//         删除菜品
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
        //删除口味
//            dishFlavorMapper.deleteByDishId(id);
//        }
        //根据菜品id批量删除
        dishMapper.deleteByIds(ids);
        //根据菜品id批量删除与其相关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

    }


    public DishVO getByIdWithFlavor(Long id) {
       //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
    //根据id查询口味数据
        List<DishFlavor> dishFlavors  =dishFlavorMapper.getByDishId(id);
//将查询到的数据封装到dishVO中
        DishVO dishVO = new DishVO();
        //数据拷贝
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }




    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入口味数据
        List<DishFlavor> flavors= dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }
    }
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder().categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
    //修改起售禁售状态
    @Override
    public void startOrStop(Integer status, long id) {
        //修改状态sql语句
        //update employee set status = ? where id=?
//        Employee employee=new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
//可以使用build构造器生成对象
        Dish dish = Dish.builder()
                .status(status)
                .id(id).build();

        dishMapper.update(dish);
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
