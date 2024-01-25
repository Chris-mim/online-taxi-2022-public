package com.mashibing.serviceorder.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.internalcommon.response.TrsearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("service-map")
public interface ServiceMapClient {

    /**
     * 周边搜索终端
     * @param center 经纬度，string类型。格式为：纬度,经度
     * @param radius int类型。单位：米，取值范围[1,5000]
     * @return
     */
    @PostMapping("/terminal/aroundsearch")
    ResponseResult<List<TerminalResponse>> aroundSearch(@RequestParam String center, @RequestParam Integer radius);

    /**
     * 查询终端某一段时间内的轨迹信息
     * @param tid 终端
     * @param starttime 开始时间
     * @param endtime 结束时间
     * @return
     */
    @GetMapping("/terminal/trsearch")
    ResponseResult<TrsearchResponse> trsearch(@RequestParam String tid,
                                                    @RequestParam Long starttime,
                                                    @RequestParam Long endtime);

 }
