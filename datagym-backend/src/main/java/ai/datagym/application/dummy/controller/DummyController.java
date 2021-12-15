package ai.datagym.application.dummy.controller;

import ai.datagym.application.dummy.service.DummyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping(value = "/api/dummy")
@Validated
public class DummyController {
    private final DummyService dummyService;

    public DummyController(DummyService dummyService) {
        this.dummyService = dummyService;
    }

    @GetMapping("/{organisationId}")
    public void createDummyDataForOrg(@PathVariable("organisationId") @NotNull String orgId) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchAlgorithmException {
        dummyService.createDummyDataForOrg(orgId);
    }
}
