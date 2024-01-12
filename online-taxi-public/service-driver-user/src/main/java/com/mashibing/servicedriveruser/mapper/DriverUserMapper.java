package com.mashibing.servicedriveruser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashibing.internalcommon.dto.DriverUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverUserMapper extends BaseMapper<DriverUser> {
    int select1(@Param("cityCode") String cityCode);
}
