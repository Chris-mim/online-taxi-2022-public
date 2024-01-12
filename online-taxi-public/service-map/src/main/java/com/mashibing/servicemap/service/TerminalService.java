package com.mashibing.servicemap.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.servicemap.remote.TerminalClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TerminalService {

    @Autowired
    private TerminalClient terminalClient;
    public ResponseResult<TerminalResponse> add(String name, String desc) {
        return terminalClient.add(name,desc);
    }
    /**
     * 周边搜索终端
     * @param center 经纬度，string类型。格式为：纬度,经度
     * @param radius int类型。单位：米，取值范围[1,5000]
     * @return
     */
    public ResponseResult<List<TerminalResponse>> aroundSearch(String center, Integer radius) {
        return terminalClient.aroundSearch(center,radius);
    }
}
