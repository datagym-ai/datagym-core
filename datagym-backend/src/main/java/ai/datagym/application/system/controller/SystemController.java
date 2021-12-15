package ai.datagym.application.system.controller;

import ai.datagym.application.system.models.SystemInfoTO;
import ai.datagym.application.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/system")
public class SystemController {
    private final SystemService systemService;

    @Autowired
    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/info")
    public SystemInfoTO getInfo(){
        return systemService.getInfo();
    }
}
