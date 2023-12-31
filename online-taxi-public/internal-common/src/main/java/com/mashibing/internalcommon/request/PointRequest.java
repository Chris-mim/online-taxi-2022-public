package com.mashibing.internalcommon.request;

import lombok.Data;

import java.util.List;

@Data
public class PointRequest {


    private String tid;

    private String trid;

    private List<PointDTO> points;

}
