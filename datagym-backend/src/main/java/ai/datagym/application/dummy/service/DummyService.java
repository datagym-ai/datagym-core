package ai.datagym.application.dummy.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;

public interface DummyService {
    /**
     * Creates a dummy project for the specific organisation
     * - If the organisation-id equals "null", the first organisation
     * gets choosed where the current user has admin-permissions
     *
     * @param orgId The specific organisation-id
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    void createDummyDataForOrg(String orgId) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchAlgorithmException;

    /**
     * Creates a dummy project for the specific organisation
     * - This internal method does not provide any additional security-checks
     *
     * @param orgId The specific organisation-id
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    void createProjectAndDatasetsInternal(String orgId) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchAlgorithmException;

}
