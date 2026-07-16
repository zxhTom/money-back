package cn.iocoder.yudao.module.custom.framework.dataencrypt.core;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 捕获响应体，待过滤器决定是否加密后再真正写出（参考 ApiEncryptResponseWrapper）
 */
public class DataEncryptResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final ServletOutputStream servletOutputStream;
    private final PrintWriter printWriter;

    public DataEncryptResponseWrapper(HttpServletResponse response) {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.servletOutputStream = this.getOutputStream();
        this.printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8));
    }

    public byte[] getBody() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 将最终内容写入底层响应
     */
    public void writeBody(byte[] body) throws IOException {
        HttpServletResponse response = (HttpServletResponse) this.getResponse();
        response.resetBuffer();
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                byteArrayOutputStream.write(b);
            }

            @Override
            @SuppressWarnings("NullableProblems")
            public void write(byte[] b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            @SuppressWarnings("NullableProblems")
            public void write(byte[] b, int off, int len) {
                byteArrayOutputStream.write(b, off, len);
            }

        };
    }

}
