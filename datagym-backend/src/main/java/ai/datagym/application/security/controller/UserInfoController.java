package ai.datagym.application.security.controller;

import ai.datagym.application.errorHandling.ServiceUnavailableException;
import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.api.model.OidcUserInfo;
import com.eforce21.cloud.login.client.aop.AuthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.cloud.login.client.service.ServiceOidc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestController
public class UserInfoController {

    @Value(value = "${eforce.accountmgmt-url:#{null}}")
    private Optional<String> accountMgmtUrl;

    private ServiceOidc serviceOidc;

    @Autowired
    public UserInfoController(ServiceOidc serviceOidc) {
        this.serviceOidc = serviceOidc;
    }

    @AuthUser
    @GetMapping("/api/userinfo")
    public OidcUserInfo doUserinfo() throws IOException {
        // @AuthUser annotated, so we're sure there's a user in securityContext.
        OauthUser user = SecurityContext.get();

        // Pass tokenhttp  to ask for more userinfo
        return serviceOidc.userinfo("Bearer: " + user.token()).execute().body();
    }

    @AuthUser
    @GetMapping(path = {"/api/accountSettings", "/api/accountSettings/{organisationId}"})
    public void redirectToAccManagement(HttpServletResponse httpServletResponse,
                                        @PathVariable(value = "organisationId", required = false) String organisationId) {
        if (accountMgmtUrl.isEmpty()) {
            throw new ServiceUnavailableException("account-management");
        }
        if (organisationId == null) {
            httpServletResponse.setHeader("Location", accountMgmtUrl.get());
        } else {
            httpServletResponse.setHeader("Location", accountMgmtUrl.get() + "#/personal/organisation/" + organisationId + "/details");
        }
        httpServletResponse.setStatus(302);
    }
}
