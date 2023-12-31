package com.mashibing.internalcommon.request;

import lombok.Data;

import java.util.List;

@Data
public class PointRequest {

    private Long carId;

    private String tid;

    private String trid;

    private List<PointDTO> points;

}
