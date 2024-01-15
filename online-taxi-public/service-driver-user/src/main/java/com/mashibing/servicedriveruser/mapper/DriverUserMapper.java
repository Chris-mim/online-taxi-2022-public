package com.mashibing.servicedriveruser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashibing.internalcommon.dto.DriverUser;
import com.mashibing.internalcommon.response.OrderDriverResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverUserMapper extends BaseMapper<DriverUser> {
    int selectAvailableDriverCount(@Param("cityCode") String cityCode);

    OrderDriverResponse selectOneAvailableDriver(@Param("carId") Long carId, @Param("bindState")Long bindState);


}
