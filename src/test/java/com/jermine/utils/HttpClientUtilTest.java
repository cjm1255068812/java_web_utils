package com.jermine.utils;

import cn.hutool.http.HttpUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpClientUtilTest {

    @Test
    public void post() {
    }

    @Test
    public void get() {
        System.out.println(HttpClientUtil.get("https://blog.csdn.net/phoenix/web/v1/comment/template/List", null, null));
        // System.out.println(HttpUtil.get("https://blog.csdn.net/phoenix/web/v1/comment/template/List"));
    }
}