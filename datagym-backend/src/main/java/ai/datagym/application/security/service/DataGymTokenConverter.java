package ai.datagym.application.security.service;

import ai.datagym.application.externalAPI.entity.ApiToken;
import ai.datagym.application.externalAPI.repo.ApiTokenRepository;
import com.eforce21.cloud.login.client.ctx.TokenUserImpl;
import com.eforce21.cloud.login.client.web.TokenConverter;
import com.eforce21.lib.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class DataGymTokenConverter implements TokenConverter {
    private final ApiTokenRepository apiTokenRepository;

    @Autowired
    public DataGymTokenConverter(ApiTokenRepository apiTokenRepository) {
        this.apiTokenRepository = apiTokenRepository;
    }

    /**
     * Checks if {@param token} is in the database and deleted = false. If token is existing and NOT deleted
     * creates new {@link TokenUserImpl} with Admin-role in the Organisation of the ApiToken.
     * If  {@link ApiToken} is not found or deleted = true, throws {@link ForbiddenException}
     * */
    @Override
    public TokenUserImpl convert(String token) {
        // Get ApiToken from DB. If Token doesn't exist or Token isDeleted = true, throw Exception
        ApiToken tokenById = getTokenByIdRequired(token);

        long currentTimeMillis = System.currentTimeMillis();
        tokenById.setLastUsed(currentTimeMillis);

        apiTokenRepository.save(tokenById);

        String owner = tokenById.getOwner();

        Map<String, String> orgs = new HashMap<>();
        orgs.put(owner, "ADMIN");

        // Create new TokenUser with "type_token"-permissions.
        // The TokenUser is in the "owner"-Organisation and has "Admin"-role in this Organisation
        return new TokenUserImpl(Collections.emptySet(), orgs);
    }

    private ApiToken getTokenByIdRequired(String token) {
        return apiTokenRepository.findApiTokenByIdAndDeletedFalse(token)
                .orElseThrow(ForbiddenException::new);
    }
}
