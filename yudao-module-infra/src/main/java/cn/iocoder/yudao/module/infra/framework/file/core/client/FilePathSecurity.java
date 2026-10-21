package cn.iocoder.yudao.module.infra.framework.file.core.client;

/** Storage keys are relative paths, never filesystem commands or absolute paths. */
public final class FilePathSecurity {
    private FilePathSecurity() { }
    public static String relative(String path) {
        if (path == null || path.isEmpty() || path.startsWith("/") || path.contains("\\")
                || path.contains(":") || path.chars().anyMatch(c -> c < 0x20 || c == 0x7f)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                throw new IllegalArgumentException("非法文件路径");
            }
        }
        return path;
    }
}
