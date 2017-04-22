package com.darkidiot.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.BeforeClass;

import java.lang.reflect.Type;

/**
 * GsonTest 测试类
 * Copyright (c) for darkidiot
 * Date:2017/4/22
 * Author: <a href="darkidiot@icloud.com">darkidiot</a>
 * School: CUIT
 * Desc:
 */
public class GsonTest {

    private static final Gson gson = new GsonBuilder().create();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Type typeToken = new TypeToken<String>() {
        }.getType();
    }

}
