/*
 * CommentPlugin.java
 * Copyright 2017 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.zzg.mybatis.generator.plugins;


import com.zzg.mybatis.generator.plugins.enhanced.BasePlugin;

import java.util.Properties;

/**
 * ---------------------------------------------------------------------------
 * 评论插件
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/8 11:21
 * ---------------------------------------------------------------------------
 */
public class CommentPlugin extends BasePlugin {
    public static final String PRO_TEMPLATE = "template";  // 模板 property

    /**
     * 插件具体实现查看BasePlugin
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }
}
