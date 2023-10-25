package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端发送的密码进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
//新增员工
    public void save(EmployeeDTO employeeDTO) {
Employee employee=new Employee();
//对象属性拷
BeanUtils.copyProperties(employeeDTO,employee);
//设置账号状态，默认为1，锁定为0
employee.setStatus(StatusConstant.ENABLE);
//设置密码，默认为123456，用MD5进行加密运行
employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
//设置当前记录的创建时间与修改时间
//employee.setCreateTime(LocalDateTime.now());
//employee.setUpdateTime(LocalDateTime.now());
//设置当前记录创建人的id和修改人的id

//employee.setCreateUser(BaseContext.getCurrentId());
//employee.setUpdateUser(BaseContext.getCurrentId());
employeeMapper.insert(employee);

    }
//分页查询
    @Override
    public PageResult  pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //selec * from e'mployee limit 0，10
        //开始分页查询，使用分页查询插件进行动态获取页码数和显示条数
        //在pom文件中导入pagehelper插件
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        //employeePageQueryDTO.getPage()当前的页码，employeePageQueryDTO.getPageSize()每页记录数
     Page<Employee>page=employeeMapper.pagQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);
    }
//启用禁用员工账号
    @Override
    public void startOrStop(Integer status, long id) {
 //修改状态sql语句
        //update employee set status = ? where id=?
//        Employee employee=new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
//可以使用build构造器生成对象
        Employee employee = Employee.builder()
                .status(status)
                .id(id).build();

        employeeMapper.update(employee);
    }

    @Override
    public Employee getByid(long id) {
        Employee employee=employeeMapper.getByid(id);
        //使数据传输到页面时为加密传输，增加安全性
        employee.setPassword("****");
        return employee;
    }

    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //数据拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        //employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }
}
