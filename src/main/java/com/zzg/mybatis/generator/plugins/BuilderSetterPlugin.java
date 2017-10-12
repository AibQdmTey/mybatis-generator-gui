/*
 * ModelTestPlugin.java
 * Copyright 2017 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.zzg.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 *
 * @author shiqi
 * Created by shiqi on 2017/9/13.
 */
public class BuilderSetterPlugin extends PluginAdapter {
    @Override
    public boolean validate(final List<String> list) {
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(final Method method,
            final TopLevelClass topLevelClass,
            final IntrospectedColumn introspectedColumn, final IntrospectedTable introspectedTable,
            final ModelClassType modelClassType) {
        method.setReturnType(topLevelClass.getType());
        // body
        method.addBodyLine("return this;");

        return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn,
                introspectedTable, modelClassType);
    }
}
