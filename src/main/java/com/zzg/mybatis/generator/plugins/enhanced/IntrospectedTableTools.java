/*
 * IntrospectedTableTools.java
 * Copyright 2017 Qunhe Tech, all rights reserved.
 * Qunhe PROPRIETARY/CONFIDENTIAL, any form of usage is subject to approval.
 */

package com.zzg.mybatis.generator.plugins.enhanced;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.StringUtility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ---------------------------------------------------------------------------
 * IntrospectedTable 的一些拓展增强
 * ---------------------------------------------------------------------------
 *
 * @author: hewei
 * @time:2017/6/13 13:48
 * ---------------------------------------------------------------------------
 */
public class IntrospectedTableTools {

    /**
     * 设置DomainObjectName和MapperName
     *
     * @param introspectedTable
     * @param context
     * @param domainObjectName
     */
    public static void setDomainObjectName(final IntrospectedTable introspectedTable,
            final Context context, final String domainObjectName)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        // 配置信息（没啥用）
        introspectedTable.getTableConfiguration().setDomainObjectName(domainObjectName);

        // FullyQualifiedTable修正
        final Field domainObjectNameField = FullyQualifiedTable.class.getDeclaredField(
                "domainObjectName");
        domainObjectNameField.setAccessible(true);
        domainObjectNameField.set(introspectedTable.getFullyQualifiedTable(), domainObjectName);

        // 重新修正introspectedTable属性信息
        final Method calculateJavaClientAttributes = IntrospectedTable.class.getDeclaredMethod(
                "calculateJavaClientAttributes");
        calculateJavaClientAttributes.setAccessible(true);
        calculateJavaClientAttributes.invoke(introspectedTable);

        final Method calculateModelAttributes = IntrospectedTable.class.getDeclaredMethod(
                "calculateModelAttributes");
        calculateModelAttributes.setAccessible(true);
        calculateModelAttributes.invoke(introspectedTable);

        final Method calculateXmlAttributes = IntrospectedTable.class.getDeclaredMethod(
                "calculateXmlAttributes");
        calculateXmlAttributes.setAccessible(true);
        calculateXmlAttributes.invoke(introspectedTable);
    }

    /**
     * 安全获取column 通过正则获取的name可能包含beginningDelimiter&&endingDelimiter
     *
     * @param introspectedTable
     * @param columnName
     * @return
     */
    public static IntrospectedColumn safeGetColumn(final IntrospectedTable introspectedTable,
            String columnName) {
        // columnName
        columnName = columnName.trim();
        // 过滤
        final String beginningDelimiter = introspectedTable.getContext().getBeginningDelimiter();
        if (StringUtility.stringHasValue(beginningDelimiter)) {
            columnName = columnName.replaceFirst("^" + beginningDelimiter, "");
        }
        final String endingDelimiter = introspectedTable.getContext().getEndingDelimiter();
        if (StringUtility.stringHasValue(endingDelimiter)) {
            columnName = columnName.replaceFirst(endingDelimiter + "$", "");
        }

        return introspectedTable.getColumn(columnName);
    }
}
