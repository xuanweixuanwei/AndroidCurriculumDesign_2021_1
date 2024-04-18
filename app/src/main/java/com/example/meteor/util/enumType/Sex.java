package com.example.meteor.util.enumType;

public enum Sex {
    UNKNOWN(0, "神秘"),
    MALE(1, "男"),
    FEMALE(2, "女");


    private Integer code;
    private String description;

    Sex(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 添加一个从字符转为枚举的静态方法
    public static Sex fromCode(Integer code) {
        for (Sex sex : values()) {
            if (sex.getCode() .equals(code) ) {
                return sex;
            }
        }
        return UNKNOWN; // 如果找不到对应code，返回默认值
    }
}