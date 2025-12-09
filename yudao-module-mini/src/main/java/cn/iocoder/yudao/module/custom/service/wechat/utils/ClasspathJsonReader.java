package cn.iocoder.yudao.module.custom.service.wechat.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class ClasspathJsonReader {

    private final ResourceLoader resourceLoader;

    public ClasspathJsonReader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 方法1：读取Classpath下的JSON文件为JSONObject
     *
     * @param filePath Classpath下的相对路径，如：config/app-config.json
     * @return JSONObject对象，如果文件不存在或解析失败返回包含错误信息的JSONObject
     */
    public JSONObject readJsonObject(String filePath) {
        try {
            // 1. 获取Classpath资源
            Resource resource = new ClassPathResource(filePath);

            // 2. 检查资源是否存在
            if (!resource.exists()) {
                return buildErrorJson("JSON文件不存在", filePath, "FILE_NOT_FOUND");
            }

            // 3. 读取文件内容（Java 8兼容方式）
            String content = readResourceContent(resource);

            // 4. 解析为JSONObject
            JSONObject jsonObject = JSON.parseObject(content);

            // 5. 添加元数据（可选）
            addMetadata(jsonObject, filePath, resource);

            return jsonObject;

        } catch (Exception e) {
            return buildErrorJson("读取JSON文件失败: " + e.getMessage(), filePath, "READ_ERROR");
        }
    }

    /**
     * 方法2：读取Classpath下的JSON文件为JSONArray
     */
    public JSONArray readJsonArray(String filePath) {
        try {
            Resource resource = new ClassPathResource(filePath);

            if (!resource.exists()) {
                // 返回空数组而不是错误对象，根据业务需求选择
                return new JSONArray();
            }

            String content = readResourceContent(resource);
            return JSON.parseArray(content);

        } catch (Exception e) {
            // 返回包含错误信息的数组
            JSONArray errorArray = new JSONArray();
            JSONObject error = new JSONObject();
            error.put("error", true);
            error.put("message", "读取JSON数组失败: " + e.getMessage());
            error.put("filePath", filePath);
            errorArray.add(error);
            return errorArray;
        }
    }

    /**
     * 方法3：读取JSON文件并转换为指定Java对象
     */
    public <T> Optional<T> readJsonAsObject(String filePath, Class<T> clazz) {
        try {
            JSONObject jsonObject = readJsonObject(filePath);

            // 检查是否包含错误
            if (jsonObject.containsKey("error") && jsonObject.getBooleanValue("error")) {
                return Optional.empty();
            }

            // 转换为Java对象
            T object = jsonObject.toJavaObject(clazz);
            return Optional.of(object);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 方法4：安全读取JSON文件（文件不存在时返回默认值）
     */
    public JSONObject readJsonObjectSafe(String filePath, JSONObject defaultValue) {
        JSONObject result = readJsonObject(filePath);

        // 如果读取失败（包含error字段），返回默认值
        if (result.containsKey("error") && result.getBooleanValue("error")) {
            return defaultValue != null ? defaultValue : new JSONObject();
        }

        return result;
    }

    /**
     * 方法5：使用ResourceLoader读取JSON文件
     * 可以读取classpath、file、http等多种资源
     */
    public JSONObject readJsonWithResourceLoader(String resourcePath) {
        try {
            // 添加classpath前缀（如果需要）
            String fullPath = resourcePath.startsWith("classpath:")
                    ? resourcePath
                    : "classpath:" + resourcePath;

            Resource resource = resourceLoader.getResource(fullPath);

            if (!resource.exists()) {
                return buildErrorJson("资源不存在", resourcePath, "RESOURCE_NOT_FOUND");
            }

            String content = readResourceContent(resource);
            return JSON.parseObject(content);

        } catch (Exception e) {
            return buildErrorJson("使用ResourceLoader读取失败: " + e.getMessage(),
                    resourcePath, "LOADER_ERROR");
        }
    }

    /**
     * 方法6：读取JSON文件并验证必需字段
     */
    public JSONObject readAndValidateJson(String filePath, String... requiredFields) {
        JSONObject jsonObject = readJsonObject(filePath);

        // 如果读取失败直接返回
        if (jsonObject.containsKey("error") && jsonObject.getBooleanValue("error")) {
            return jsonObject;
        }

        // 验证必需字段
        for (String field : requiredFields) {
            if (!jsonObject.containsKey(field)) {
                return buildErrorJson("缺少必需字段: " + field, filePath, "VALIDATION_ERROR");
            }
        }

        return jsonObject;
    }

    /**
     * 核心方法：读取Resource内容（Java 8兼容）
     */
    private String readResourceContent(Resource resource) throws IOException {
        // 方式1：使用Spring的StreamUtils（推荐）
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }

        // 方式2：手动读取（如果没有Spring工具类）
        /*
        try (InputStream inputStream = resource.getInputStream();
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString(StandardCharsets.UTF_8.name());
        }
        */
    }

    /**
     * 添加元数据到JSON对象
     */
    private void addMetadata(JSONObject jsonObject, String filePath, Resource resource) {
        try {
            JSONObject metadata = new JSONObject();
            metadata.put("filePath", filePath);
            metadata.put("loadedAt", System.currentTimeMillis());
            metadata.put("description", "从Classpath加载的JSON文件");
            metadata.put("exists", resource.exists());
            metadata.put("readable", resource.isReadable());

            // 添加文件信息（如果可访问）
            if (resource.isFile()) {
                File file = resource.getFile();
                metadata.put("fileSize", file.length());
                metadata.put("lastModified", file.lastModified());
            }

            // 将元数据放在单独的字段中
            jsonObject.put("_metadata", metadata);

        } catch (Exception e) {
            // 元数据添加失败不影响主功能
            jsonObject.put("_metadataWarning", "无法添加完整元数据: " + e.getMessage());
        }
    }

    /**
     * 构建错误JSON对象
     */
    private JSONObject buildErrorJson(String message, String filePath, String errorCode) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", true);
        errorJson.put("errorCode", errorCode);
        errorJson.put("message", message);
        errorJson.put("filePath", filePath);
        errorJson.put("timestamp", System.currentTimeMillis());
        return errorJson;
    }

    /**
     * 检查JSON文件是否存在
     */
    public boolean jsonFileExists(String filePath) {
        try {
            Resource resource = new ClassPathResource(filePath);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取JSON文件的基本信息
     */
    public JSONObject getJsonFileInfo(String filePath) {
        try {
            Resource resource = new ClassPathResource(filePath);

            JSONObject info = new JSONObject();
            info.put("filePath", filePath);
            info.put("exists", resource.exists());
            info.put("readable", resource.isReadable());

            if (resource.exists()) {
                String content = readResourceContent(resource);
                JSONObject json = JSON.parseObject(content);

                info.put("isValidJson", true);
                info.put("keyCount", json.keySet().size());
                info.put("contentLength", content.length());

                // 尝试获取文件大小
                try {
                    if (resource.isFile()) {
                        File file = resource.getFile();
                        info.put("fileSize", file.length());
                    }
                } catch (Exception e) {
                    info.put("fileSize", "未知");
                }
            } else {
                info.put("isValidJson", false);
                info.put("keyCount", 0);
            }

            return info;

        } catch (Exception e) {
            return buildErrorJson("获取文件信息失败: " + e.getMessage(), filePath, "INFO_ERROR");
        }
    }
}
