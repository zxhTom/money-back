package cn.iocoder.yudao.framework.common.core;

import lombok.Data;

@Data
public class SortField {
    private String field;      // 字段名：createTime、updateTime、salary等
    private String direction;  // 排序方向：ASC、DESC
}