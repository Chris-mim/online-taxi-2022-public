package com.mashibing.servicedriveruser.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * 自动生成代码工具类
 */
public class MysqlGenerator {
    public static void main(String[] args) {

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/service-driver-user?characterEncoding=utf-8&serverTimezone=GMT%2B8",
                "root","root")
                .globalConfig(builder -> builder.author("zhangjj").fileOverride().outputDir("E:\\idea workplace\\study\\飞滴出行网约车\\my-github\\online-taxi-2022-public\\online-taxi-public\\service-driver-user\\src\\main\\java"))
                .packageConfig(builder -> builder.parent("com.mashibing.serviceDriverUser").pathInfo(Collections.singletonMap(OutputFile.mapperXml,
                        "D:\\00-项目\\gitee\\online-taxi-2022-public\\online-taxi-public\\service-driver-user\\src\\main\\java\\com\\mashibing\\serviceDriverUser\\mapper")))
                .strategyConfig(builder -> builder.addInclude("car"))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
