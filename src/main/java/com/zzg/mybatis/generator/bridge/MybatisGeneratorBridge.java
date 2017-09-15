package com.zzg.mybatis.generator.bridge;

import com.zzg.mybatis.generator.model.DatabaseConfig;
import com.zzg.mybatis.generator.model.DbType;
import com.zzg.mybatis.generator.model.GeneratorConfig;
import com.zzg.mybatis.generator.plugins.DbRemarksCommentGenerator;
import com.zzg.mybatis.generator.util.ConfigHelper;
import com.zzg.mybatis.generator.util.DbUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.IgnoredColumn;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The bridge between GUI and the mybatis generator. All the operation to  mybatis generator should proceed through this
 * class
 * <p>
 * Created by Owen on 6/30/16.
 */
public class MybatisGeneratorBridge {

	private static final Logger _LOG = LoggerFactory.getLogger(MybatisGeneratorBridge.class);

    private GeneratorConfig generatorConfig;

    private DatabaseConfig selectedDatabaseConfig;

    private ProgressCallback progressCallback;

    private List<IgnoredColumn> ignoredColumns;

    private List<ColumnOverride> columnOverrides;

    public MybatisGeneratorBridge() {
    }

    public void setGeneratorConfig(final GeneratorConfig generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    public void setDatabaseConfig(final DatabaseConfig databaseConfig) {
        this.selectedDatabaseConfig = databaseConfig;
    }

    public void generate() throws Exception {
        final Configuration configuration = new Configuration();
        final Context context = new Context(ModelType.CONDITIONAL);
        configuration.addContext(context);
        context.addProperty("javaFileEncoding", "UTF-8");
        // 防止MySQL保留字段冲突
        context.addProperty("beginningDelimiter", "`");
        context.addProperty("endingDelimiter", "`");
	    final String connectorLibPath = ConfigHelper.findConnectorLibPath(selectedDatabaseConfig.getDbType());
	    _LOG.info("connectorLibPath: {}", connectorLibPath);
	    configuration.addClasspathEntry(connectorLibPath);
        // Table configuration
        final TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(generatorConfig.getTableName());
        tableConfig.setDomainObjectName(generatorConfig.getDomainObjectName());
        tableConfig.setAllColumnDelimitingEnabled(true);

        // 针对 postgresql 单独配置
        if (DbType.valueOf(selectedDatabaseConfig.getDbType()).getDriverClass() == "org.postgresql.Driver") {
            tableConfig.setDelimitIdentifiers(true);
        }

        //添加GeneratedKey主键生成
		if (StringUtils.isNoneEmpty(generatorConfig.getGenerateKeys())) {
			tableConfig.setGeneratedKey(new GeneratedKey(generatorConfig.getGenerateKeys(), selectedDatabaseConfig.getDbType(), true, null));
		}

        if (generatorConfig.getMapperName() != null) {
            tableConfig.setMapperName(generatorConfig.getMapperName());
        }
        // add ignore columns
        if (ignoredColumns != null) {
            ignoredColumns.stream().forEach(ignoredColumn -> {
                tableConfig.addIgnoredColumn(ignoredColumn);
            });
        }
        if (columnOverrides != null) {
            columnOverrides.stream().forEach(columnOverride -> {
                tableConfig.addColumnOverride(columnOverride);
            });
        }
        if (generatorConfig.isUseActualColumnNames()) {
			tableConfig.addProperty("useActualColumnNames", "true");
        }
        final JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.setDriverClass(DbType.valueOf(selectedDatabaseConfig.getDbType()).getDriverClass());
        jdbcConfig.setConnectionURL(DbUtil.getConnectionUrlWithSchema(selectedDatabaseConfig));
        jdbcConfig.setUserId(selectedDatabaseConfig.getUsername());
        jdbcConfig.setPassword(selectedDatabaseConfig.getPassword());
        // java model
        final JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();
        modelConfig.setTargetPackage(generatorConfig.getModelPackage());
        modelConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getModelPackageTargetFolder());
        // Mapper configuration
        final SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();
        mapperConfig.setTargetPackage(generatorConfig.getMappingXMLPackage());
        mapperConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getMappingXMLTargetFolder());
        // DAO
        final JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(generatorConfig.getDaoPackage());
        daoConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getDaoTargetFolder());

        context.setId("myid");
        context.addTableConfiguration(tableConfig);
        context.setJdbcConnectionConfiguration(jdbcConfig);
        context.setJdbcConnectionConfiguration(jdbcConfig);
        context.setJavaModelGeneratorConfiguration(modelConfig);
        context.setSqlMapGeneratorConfiguration(mapperConfig);
        context.setJavaClientGeneratorConfiguration(daoConfig);
        // Comment
        final CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(DbRemarksCommentGenerator.class.getName());
        if (generatorConfig.isComment()) {
            commentConfig.addProperty("columnRemarks", "true");
        }
        if (generatorConfig.isAnnotation()) {
            commentConfig.addProperty("annotations", "true");
        }
        context.setCommentGeneratorConfiguration(commentConfig);

        /**
         * 添加 Plugins
         */
        // builder形式的set方法
        PluginConfiguration builderSetPluginConfiguration = new PluginConfiguration();
        builderSetPluginConfiguration.addProperty("type", "com.zzg.mybatis.generator.plugins.BuilderSetPlugin");
        builderSetPluginConfiguration.setConfigurationType("com.zzg.mybatis.generator.plugins.BuilderSetPlugin");
        context.addPluginConfiguration(builderSetPluginConfiguration);
        // 实体添加序列化
        final PluginConfiguration serializablePluginConfiguration = new PluginConfiguration();
        serializablePluginConfiguration.addProperty("type", "org.mybatis.generator.plugins.SerializablePlugin");
        serializablePluginConfiguration.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
        context.addPluginConfiguration(serializablePluginConfiguration);
        // 强化example
        final PluginConfiguration enhancedExamplePluginConfiguration = new PluginConfiguration();
        enhancedExamplePluginConfiguration.addProperty("type", "com.zzg.mybatis.generator.plugins.ExampleEnhancedPlugin");
        enhancedExamplePluginConfiguration.setConfigurationType("com.zzg.mybatis.generator.plugins.ExampleEnhancedPlugin");
        context.addPluginConfiguration(enhancedExamplePluginConfiguration);
        // 批量插入
        final PluginConfiguration batchInsertPluginConfiguration = new PluginConfiguration();
        batchInsertPluginConfiguration.addProperty("type", "com.zzg.mybatis.generator.plugins.BatchInsertPlugin");
        batchInsertPluginConfiguration.setConfigurationType("com.zzg.mybatis.generator.plugins.BatchInsertPlugin");
        context.addPluginConfiguration(batchInsertPluginConfiguration);
        // 逻辑删除
        final PluginConfiguration logicalDeletePluginConfiguration = new PluginConfiguration();
        logicalDeletePluginConfiguration.addProperty("type", "com.zzg.mybatis.generator.plugins.LogicalDeletePlugin");
        logicalDeletePluginConfiguration.addProperty("logicalDeleteColumn", "deleted");
        logicalDeletePluginConfiguration.addProperty("logicalDeleteValue", "1");
        logicalDeletePluginConfiguration.addProperty("logicalUnDeleteValue", "0");
        logicalDeletePluginConfiguration.setConfigurationType("com.zzg.mybatis.generator.plugins.LogicalDeletePlugin");
        context.addPluginConfiguration(logicalDeletePluginConfiguration);
        // toString, hashCode, equals插件
        if (generatorConfig.isNeedToStringHashcodeEquals()) {
            final PluginConfiguration pluginConfiguration1 = new PluginConfiguration();
            pluginConfiguration1.addProperty("type", "org.mybatis.generator.plugins.EqualsHashCodePlugin");
            pluginConfiguration1.setConfigurationType("org.mybatis.generator.plugins.EqualsHashCodePlugin");
            context.addPluginConfiguration(pluginConfiguration1);
            final PluginConfiguration pluginConfiguration2 = new PluginConfiguration();
            pluginConfiguration2.addProperty("type", "org.mybatis.generator.plugins.ToStringPlugin");
            pluginConfiguration2.setConfigurationType("org.mybatis.generator.plugins.ToStringPlugin");
            context.addPluginConfiguration(pluginConfiguration2);
        }
        // limit/offset插件
        if (generatorConfig.isOffsetLimit()) {
            if (DbType.MySQL.name().equals(selectedDatabaseConfig.getDbType())
		            || DbType.PostgreSQL.name().equals(selectedDatabaseConfig.getDbType())) {
                final PluginConfiguration pluginConfiguration = new PluginConfiguration();
                pluginConfiguration.addProperty("type", "com.zzg.mybatis.generator.plugins.MySQLLimitPlugin");
                pluginConfiguration.setConfigurationType("com.zzg.mybatis.generator.plugins.MySQLLimitPlugin");
                context.addPluginConfiguration(pluginConfiguration);
            }
        }
        context.setTargetRuntime("MyBatis3");

        final List<String> warnings = new ArrayList<>();
        final Set<String> fullyqualifiedTables = new HashSet<>();
        final Set<String> contexts = new HashSet<>();
        final ShellCallback shellCallback = new DefaultShellCallback(true); // override=true
        final MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);
    }

	public void setProgressCallback(final ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public void setIgnoredColumns(final List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(final List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }
}
