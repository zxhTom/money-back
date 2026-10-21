package cn.iocoder.yudao.module.infra.framework.file.core.client;

import cn.iocoder.yudao.module.infra.framework.file.core.client.local.*;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.*;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class FilePathSecurityTest {
    @TempDir Path temp;
    private LocalFileClient client;
    private Path outside;
    @BeforeEach void setup() throws Exception {
        Path root=Files.createDirectory(temp.resolve("uploads"));outside=temp.resolve("outside.txt");
        Files.write(outside,"synthetic-private-fixture".getBytes(StandardCharsets.UTF_8));
        client=new LocalFileClient(1L,new LocalFileClientConfig().setBasePath(root.toString()).setDomain("https://example.invalid"));
    }
    @Test void cannotReadOutsideStorage() {assertThrows(IllegalArgumentException.class,() -> client.getContent("../outside.txt"));}
    @Test void cannotDeleteOutsideStorage() {
        assertThrows(IllegalArgumentException.class,() -> client.delete("../outside.txt"));assertTrue(Files.exists(outside));
    }
    @Test void cannotOverwriteOutsideStorage() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> client.upload(new byte[]{1},"../outside.txt","text/plain"));
        assertEquals("synthetic-private-fixture",new String(Files.readAllBytes(outside),StandardCharsets.UTF_8));
    }
    @Test void symbolicLinksCannotEscapeStorage() throws Exception {
        Files.createSymbolicLink(temp.resolve("uploads/alias"),outside);
        assertThrows(IllegalArgumentException.class,() -> client.getContent("alias"));
        assertThrows(IllegalArgumentException.class,() -> client.delete("alias"));
        assertThrows(IllegalArgumentException.class,() -> client.upload(new byte[]{1},"alias","text/plain"));
        assertTrue(Files.exists(outside));
    }
    @Test void legitimateNestedFilesKeepUploadReadDelete() {
        byte[] content="test".getBytes(StandardCharsets.UTF_8);
        String url=client.upload(content,"private/contracts/42/20261021/合同.pdf","application/pdf");
        assertTrue(url.contains("private/contracts/42/20261021/合同.pdf"));
        assertArrayEquals(content,client.getContent("private/contracts/42/20261021/合同.pdf"));
        client.delete("private/contracts/42/20261021/合同.pdf");assertNull(client.getContent("private/contracts/42/20261021/合同.pdf"));
    }
    @Test void absoluteWindowsAndAmbiguousPathsFailClosed() {
        for(String path:new String[]{"/etc/passwd","C:\\test","C:/test","a/../x","a/./x","a//x",".","..","x/","x\u0000y",""}) {
            assertThrows(IllegalArgumentException.class,() -> FilePathSecurity.relative(path),path);
        }
    }
    @Test void ftpRejectsTraversalBeforeNetworkAccess() {
        FtpFileClient ftp=new FtpFileClient(1L,new FtpFileClientConfig().setBasePath("/uploads"));
        assertThrows(IllegalArgumentException.class,() -> ftp.getContent("../outside.txt"));
        assertThrows(IllegalArgumentException.class,() -> ftp.delete("../outside.txt"));
        assertThrows(IllegalArgumentException.class,() -> ftp.upload(new byte[]{1},"../outside.txt","text/plain"));
    }
    @Test void sftpRejectsTraversalBeforeNetworkAccess() {
        SftpFileClient sftp=new SftpFileClient(1L,new SftpFileClientConfig().setBasePath("/uploads"));
        assertThrows(IllegalArgumentException.class,() -> sftp.getContent("../outside.txt"));
        assertThrows(IllegalArgumentException.class,() -> sftp.delete("../outside.txt"));
        assertThrows(IllegalArgumentException.class,() -> sftp.upload(new byte[]{1},"../outside.txt","text/plain"));
    }
}
