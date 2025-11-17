package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/hello")
    public R<String> hello() {
        return R.success("<h1>Hello fucking world</h1>");
    }

}
