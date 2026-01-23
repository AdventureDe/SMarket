package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.market.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志 DAO 层接口
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
    // 继承 BaseMapper 后，自动拥有 insert, selectList 等方法
}