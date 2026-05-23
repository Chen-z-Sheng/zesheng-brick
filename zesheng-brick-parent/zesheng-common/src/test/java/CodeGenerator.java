import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 代码生成器 - 生产级通用版
 * <p>
 * 使用说明：只需修改【配置参数】区域，无需修改其他逻辑
 * </p>
 *
 * @author czk
 * @since 2026-02-16
 */
public class CodeGenerator {

    // ==================================== 【配置参数 - 开始】 ====================================
    // -------------------------- 数据库连接配置（仅本地生成代码用，勿提交真实生产 RDS 凭据） --------------------------
    /**
     * 数据库连接URL
     */
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/zesheng_brick?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    /**
     * 数据库用户名
     */
    private static final String DB_USERNAME = "root";
    /**
     * 数据库密码
     */
    private static final String DB_PASSWORD = "your-local-mysql-password";

    // -------------------------- 模块配置 --------------------------
    /**
     * 模块名，生成的包路径：com.zesheng.{moduleName}
     */
    private static final String MODULE_NAME = "sys";
    /**
     * 代码生成输出目录（相对于项目根目录）
     */
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/zesheng-" + MODULE_NAME + "-service/src/main/java";
    /**
     * Mapper XML输出目录
     */
    private static final String XML_OUTPUT_DIR = System.getProperty("user.dir") + "/zesheng-" + MODULE_NAME + "-service/src/main/resources/mapper/" + MODULE_NAME;
    /**
     * 作者名称
     */
    private static final String AUTHOR = "czk";

    // -------------------------- 表配置 --------------------------
    /**
     * 要生成代码的表名（支持多个，用逗号分隔）
     */
    private static final String TABLE_NAME = "sys_role_permission";
    /**
     * 表名前缀（生成实体时自动去除）
     */
    private static final String TABLE_PREFIX = "sys_";
    /**
     * 实体类名称（首字母大写，如FormTemplates）
     */
    private static final String ENTITY_NAME = "RolePermission";
    /**
     * 表注释（用于接口文档）
     */
    private static final String TABLE_COMMENT = "系统端-权限表";

    // -------------------------- 模板配置 --------------------------
    /**
     * 模板文件所在目录（绝对路径）
     */
    private static final String TEMPLATE_DIR = System.getProperty("user.dir") + "/zesheng-common/src/main/resources/templates";
    // ==================================== 【配置参数 - 结束】 ====================================

    public static void main(String[] args) {
        // 1. 加载MySQL驱动（Java 17+必需）
        loadMysqlDriver();

        // 2. 执行MyBatis-Plus核心代码生成（Controller/Service/Mapper/Entity）
        generateCoreCode();

        // 3. 生成Request/Response模板文件
        generateRequestResponseFiles();

        System.out.println("✅ 代码生成完成！");
        System.out.println("📌 模块：" + MODULE_NAME + " | 表：" + TABLE_NAME + " | 实体：" + ENTITY_NAME);
    }

    /**
     * 加载MySQL驱动（解决Java 17+驱动加载问题）
     */
    private static void loadMysqlDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Driver driver = (Driver) Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            if (!DriverManager.getDrivers().asIterator().hasNext()) {
                DriverManager.registerDriver(driver);
            }
        } catch (Exception e) {
            throw new RuntimeException("加载MySQL驱动失败", e);
        }
    }

    /**
     * 生成核心代码：Controller/Service/ServiceImpl/Mapper/Entity
     */
    private static void generateCoreCode() {
        // 处理实体名，自动去除表前缀对应的驼峰前缀（和Request/Response逻辑保持一致）
        String coreEntityName = ENTITY_NAME;
        if (TABLE_PREFIX != null && !TABLE_PREFIX.isBlank() && TABLE_PREFIX.endsWith("_")) {
            String prefixCamel = TABLE_PREFIX.substring(0, TABLE_PREFIX.length() - 1);
            if (!prefixCamel.isBlank()) {
                prefixCamel = prefixCamel.substring(0, 1).toUpperCase() + prefixCamel.substring(1);
                if (coreEntityName.startsWith(prefixCamel) && coreEntityName.length() > prefixCamel.length()) {
                    coreEntityName = coreEntityName.substring(prefixCamel.length());
                }
            }
        }
        // 确保处理后的实体名不为空
        if (coreEntityName.isBlank()) {
            coreEntityName = ENTITY_NAME;
        }
        final String finalEntityName = coreEntityName;

        FastAutoGenerator.create(DB_URL, DB_USERNAME, DB_PASSWORD)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author(AUTHOR)
                            .outputDir(OUTPUT_DIR)
                            .disableOpenDir() // 生成后不打开文件夹
                            .commentDate("yyyy-MM-dd"); // 注释日期格式
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent("com.zesheng")
                            .moduleName(MODULE_NAME)
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, XML_OUTPUT_DIR));
                })
                // 策略配置（关键：匹配BaseEntity字段）
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAME)
                            .addTablePrefix(TABLE_PREFIX)
                            // 实体策略
                            .entityBuilder()
                            .enableLombok() // 启用Lombok
                            .naming(NamingStrategy.underline_to_camel) // 表名转驼峰
                            .columnNaming(NamingStrategy.underline_to_camel) // 字段名转驼峰
                            .enableTableFieldAnnotation() // 生成字段注解
                            .enableFileOverride() // 覆盖已有文件
                            .convertFileName(entityName -> finalEntityName) // 自定义实体类名
                            // Service策略（带I前缀）
                            .serviceBuilder()
                            .formatServiceFileName("I%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .enableFileOverride()
                            .convertServiceFileName(entityName -> "I" + finalEntityName + "Service")
                            .convertServiceImplFileName(entityName -> finalEntityName + "ServiceImpl")
                            // Controller策略
                            .controllerBuilder()
                            .enableRestStyle() // RESTful风格
                            .enableHyphenStyle() // URL中连字符分隔
                            .formatFileName("%sController")
                            .enableFileOverride()
                            .convertFileName(entityName -> finalEntityName + "Controller")
                            // Mapper策略
                            .mapperBuilder()
                            .enableFileOverride()
                            .convertMapperFileName(entityName -> finalEntityName + "Mapper");
                })
                // 模板配置（指定自定义模板）
                .templateConfig(builder -> {
                    builder.controller(TEMPLATE_DIR + "/controller.java.ftl")
                            .service(TEMPLATE_DIR + "/service.java.ftl")
                            .serviceImpl(TEMPLATE_DIR + "/serviceImpl.java.ftl")
                            .entity(TEMPLATE_DIR + "/entity.java.ftl")
                            .mapper(TEMPLATE_DIR + "/mapper.java.ftl");
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    /**
     * 生成Request/Response文件
     */
    private static void generateRequestResponseFiles() {
        String basePath = OUTPUT_DIR;
        String modelPath = basePath + "/com/zesheng/" + MODULE_NAME + "/model";
        String requestPath = modelPath + "/request";
        String responsePath = modelPath + "/response";

        // 创建目录（确保存在）
        new File(requestPath).mkdirs();
        new File(responsePath).mkdirs();

        try {
            // 初始化Freemarker配置
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_DIR));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setNumberFormat("0"); // 避免数字格式问题

            // 读取数据库字段信息（关键：匹配模板过滤逻辑）
            List<Map<String, Object>> fields = getTableFields(TABLE_NAME);

            // 构建数据模型
            // 处理实体名，自动去除表前缀对应的驼峰前缀（比如sys_ → Sys，SysPermission → Permission）
            String processedEntityName = ENTITY_NAME;
            if (TABLE_PREFIX != null && !TABLE_PREFIX.isBlank() && TABLE_PREFIX.endsWith("_")) {
                String prefixCamel = TABLE_PREFIX.substring(0, TABLE_PREFIX.length() - 1);
                if (!prefixCamel.isBlank()) {
                    prefixCamel = prefixCamel.substring(0, 1).toUpperCase() + prefixCamel.substring(1);
                    if (processedEntityName.startsWith(prefixCamel) && processedEntityName.length() > prefixCamel.length()) {
                        processedEntityName = processedEntityName.substring(prefixCamel.length());
                    }
                }
            }
            // 确保处理后的实体名不为空
            if (processedEntityName.isBlank()) {
                processedEntityName = ENTITY_NAME;
            }
            String entityPath = processedEntityName.substring(0, 1).toLowerCase() + processedEntityName.substring(1);

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("entity", processedEntityName);
            dataModel.put("author", AUTHOR);
            dataModel.put("date", new Date().toString().substring(0, 10)); // 简化日期格式
            dataModel.put("moduleName", MODULE_NAME);
            dataModel.put("table", new HashMap<String, Object>() {{
                put("comment", TABLE_COMMENT);
                put("entityPath", entityPath);
                put("fields", fields);
            }});

            // 生成Request文件
            generateFile(cfg, dataModel, "request/${entity}Request.java.ftl", requestPath + "/" + ENTITY_NAME + "Request.java");
            generateFile(cfg, dataModel, "request/pageRequest.java.ftl", requestPath + "/" + ENTITY_NAME + "PageRequest.java");
            generateFile(cfg, dataModel, "request/saveRequest.java.ftl", requestPath + "/" + ENTITY_NAME + "SaveRequest.java");
            generateFile(cfg, dataModel, "request/updateRequest.java.ftl", requestPath + "/" + ENTITY_NAME + "UpdateRequest.java");

            // 生成Response文件
            generateFile(cfg, dataModel, "response/vo.java.ftl", responsePath + "/" + ENTITY_NAME + "Vo.java");
            generateFile(cfg, dataModel, "response/listResponse.java.ftl", responsePath + "/" + ENTITY_NAME + "ListResponse.java");
            generateFile(cfg, dataModel, "response/pageResponse.java.ftl", responsePath + "/" + ENTITY_NAME + "PageResponse.java");
            generateFile(cfg, dataModel, "response/saveResponse.java.ftl", responsePath + "/" + ENTITY_NAME + "SaveResponse.java");
            generateFile(cfg, dataModel, "response/updateResponse.java.ftl", responsePath + "/" + ENTITY_NAME + "UpdateResponse.java");

        } catch (Exception e) {
            throw new RuntimeException("生成Request/Response文件失败", e);
        }
    }

    /**
     * 根据模板生成文件（覆盖已有文件）
     */
    private static void generateFile(Configuration cfg, Map<String, Object> dataModel, String templateName, String outputPath) throws Exception {
        Template template = cfg.getTemplate(templateName);
        File outputFile = new File(outputPath);
        // 覆盖已有文件
        if (outputFile.exists()) {
            outputFile.delete();
        }
        // 写入文件（UTF-8编码）
        try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            template.process(dataModel, out);
        }
        System.out.println("📄 生成文件：" + outputPath);
    }

    /**
     * 读取数据库表字段信息（精准匹配类型转换）
     */
    private static List<Map<String, Object>> getTableFields(String tableName) {
        List<Map<String, Object>> fields = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            // 读取表字段（区分大小写，匹配MySQL）
            try (ResultSet rs = metaData.getColumns(null, "%", tableName, "%")) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String columnType = rs.getString("TYPE_NAME");
                    String comment = rs.getString("REMARKS");
                    int nullable = rs.getInt("NULLABLE");
                    int precision = rs.getInt("COLUMN_SIZE"); // 精度
                    int scale = rs.getInt("DECIMAL_DIGITS"); // 小数位数

                    Map<String, Object> field = new HashMap<>();
                    field.put("name", columnName);
                    field.put("propertyName", underscoreToCamel(columnName));
                    field.put("propertyType", convertJavaType(columnType, precision, scale));
                    field.put("comment", comment == null || comment.isEmpty() ? columnName : comment);
                    field.put("nullable", nullable == 1); // 1=允许为空，0=非空
                    fields.add(field);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("读取表字段信息失败，表名：" + tableName, e);
        }
        return fields;
    }

    /**
     * 下划线转驼峰（精准处理）
     */
    private static String underscoreToCamel(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // 避免连续下划线问题
        str = str.toLowerCase().replaceAll("_{2,}", "_");
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    /**
     * 数据库类型转Java类型（精准匹配，支持LocalDateTime）
     */
    private static String convertJavaType(String dbType, int precision, int scale) {
        if (dbType == null) {
            return "String";
        }
        String type = dbType.toLowerCase();

        // 整数类型
        if (type.contains("int")) {
            if (type.contains("tinyint")) {
                return precision == 1 ? "Boolean" : "Integer"; // tinyint(1)转Boolean
            } else if (type.contains("smallint")) {
                return "Integer";
            } else if (type.contains("mediumint")) {
                return "Integer";
            } else if (type.contains("bigint")) {
                return "Long";
            } else {
                return "Integer";
            }
        }
        // 字符串类型
        else if (type.contains("varchar") || type.contains("char") || type.contains("text") || type.contains("json")) {
            return "String";
        }
        // 小数类型
        else if (type.contains("decimal") || type.contains("numeric")) {
            return "java.math.BigDecimal";
        }
        // 日期时间类型（关键：匹配BaseEntity的LocalDateTime）
        else if (type.contains("datetime") || type.contains("timestamp")) {
            return "java.time.LocalDateTime";
        } else if (type.contains("date")) {
            return "java.time.LocalDate";
        } else if (type.contains("time")) {
            return "java.time.LocalTime";
        }
        // 浮点数类型
        else if (type.contains("double")) {
            return "Double";
        } else if (type.contains("float")) {
            return "Float";
        }
        // 布尔类型
        else if (type.contains("boolean") || type.contains("bit")) {
            return "Boolean";
        }
        // 其他类型默认String
        else {
            return "String";
        }
    }
}