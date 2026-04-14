package com.atm.iso8583.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping({ "/dashboard", "/dashboard/" })
    public String monitoringDashboard() {
        return "forward:/dashboard/index.html";
    }

    @GetMapping("/monitoring.html")
    public String monitoringLegacyRedirect() {
        return "redirect:/dashboard/";
    }

    @GetMapping({ "/swagger-ui.html", "/swagger-ui-html", "/api/swagger-ui-html" })
    public String swaggerUiCompatibilityRedirect() {
        return "redirect:/api/swagger-ui.html";
    }
}
