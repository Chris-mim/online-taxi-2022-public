package com.mashibing.servicemap.controller;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.internalcommon.response.TrsearchResponse;
import com.mashibing.servicemap.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/terminal")
public class TerminalController {

    @Autowired
    private TerminalService service;

    /**
     * 创建终端
     *
     * @param name
     * @param desc
     * @return
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestParam String name, @RequestParam String desc) {
        return service.add(name, desc);
    }

    /**
     * 周边搜索终端
     *
     * @param center 经纬度，string类型。格式为：纬度,经度
     * @param radius int类型。单位：米，取值范围[1,5000]
     * @return
     */
    @GetMapping("/aroundsearch")
    public ResponseResult<List<TerminalResponse>> aroundSearch(@RequestParam String center, @RequestParam Integer radius) {
        return service.aroundSearch(center, radius);
    }

    /**
     * 查询终端某一段时间内的轨迹信息
     * @param tid 终端
     * @param starttime 开始时间
     * @param endtime 结束时间
     * @return
     */
    @GetMapping("/trsearch")
    public ResponseResult<List<TrsearchResponse>> trsearch(@RequestParam String tid,
                                                           @RequestParam Long starttime,
                                                           @RequestParam Long endtime) {
        return service.trsearch(tid, starttime, endtime);
    }
}
