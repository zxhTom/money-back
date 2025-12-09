package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.PermitAll;

@Controller // 注意是@Controller，不是@RestController
public class IframeController {

    /**
     * 中转页面接口
     * @param url 通过参数传递的要嵌入的外部地址（必传）
     * @param height iframe高度（可选，默认600px）
     * @param title 页面标题（可选）
     * @param model 用于向页面传递数据
     * @return 返回Thymeleaf模板名称 "iframe-viewer"
     */
    @GetMapping("/embed")
    @PermitAll
    public String embedPage(
            @RequestParam("url") String url, // 必须传入url参数
            @RequestParam(value = "height", required = false, defaultValue = "600") Integer height,
            @RequestParam(value = "title", required = false, defaultValue = "嵌入页面") String title,
            Model model) {

        // 将参数放入Model，供前端页面使用
        model.addAttribute("embeddedUrl", url);
        model.addAttribute("iframeHeight", height);
        model.addAttribute("pageTitle", title);

        // 返回Thymeleaf模板的名称，对应 templates/iframe-viewer.html
        return "iframe-viewer";
    }
}
