package de.hybris.platform.inboundservices.interceptor

import de.hybris.bootstrap.annotations.IntegrationTest
import de.hybris.platform.apiregistryservices.model.BasicCredentialModel
import de.hybris.platform.apiregistryservices.model.DestinationTargetModel
import de.hybris.platform.apiregistryservices.model.EndpointModel
import de.hybris.platform.apiregistryservices.model.ExposedDestinationModel
import de.hybris.platform.apiregistryservices.model.ExposedOAuthCredentialModel
import de.hybris.platform.integrationservices.model.InboundChannelConfigurationModel
import de.hybris.platform.integrationservices.model.IntegrationObjectModel
import de.hybris.platform.integrationservices.util.IntegrationTestUtil
import de.hybris.platform.servicelayer.ServicelayerSpockSpecification
import de.hybris.platform.webservicescommons.model.OAuthClientDetailsModel
import org.junit.Test
import spock.lang.Issue

@IntegrationTest
@Issue('https://cxjira.sap.com/browse/GRIFFIN-4231')
class ExposedDestinationICCMatchedCredentialIntegrationTest extends ServicelayerSpockSpecification {

    private static final String TEST_NAME = "ExposedDestinationICCMatchedCredential"
    private static final String IO = "${TEST_NAME}_IO"
    private static final String URL = "http://localhost:9002/test"
    private static final String USERNAME = "${TEST_NAME}_User"
    private static final String PASSWORD = "pass"
    private static final String BASIC_CRED = "${TEST_NAME}_BasicCred"
    private static final String OAUTH_DETAILS = "${TEST_NAME}_OAuthClientDetails"
    private static final String OAUTH_CRED = "${TEST_NAME}_OAuthCred"
    private static final String ENDPOINT = "${TEST_NAME}_Endpoint"
    private static final String TARGET = "${TEST_NAME}_DestinationTarget"
    private static final String DESTINATION1 = "${TEST_NAME}_Destination1"
    private static final String DESTINATION2 = "${TEST_NAME}_Destination2"

    def setup() {
        IntegrationTestUtil.importImpEx(
                'INSERT_UPDATE IntegrationObject; code[unique = true]',
                "                                      ; $IO ",
                'INSERT_UPDATE InboundChannelConfiguration; integrationObject(code)[unique = true]; authenticationType(code)',
                "                                         ; $IO                        ; BASIC",
                'INSERT_UPDATE BasicCredential;id[unique=true];username;password',
                "                                ;$BASIC_CRED; $USERNAME ; $PASSWORD ",
                'INSERT_UPDATE OAuthClientDetails;clientId[unique=true];resourceIds;scope;authorizedGrantTypes;clientSecret;authorities',
                "                                ;$OAUTH_DETAILS;hybris;basic;authorization_code,refresh_token,password,client_credentials;password;ROLE_TRUSTED_CLIENT",
                'INSERT_UPDATE ExposedOAuthCredential;id[unique=true];oAuthClientDetails(clientId);password',
                "                                    ;$OAUTH_CRED;$OAUTH_DETAILS;secret",
                'INSERT_UPDATE DestinationTarget;id[unique=true];destinationChannel(code)[default=DEFAULT];template',
                "                               ;$TARGET;;true",
                'INSERT_UPDATE Endpoint;id[unique=true];version;specUrl;specData;name;description',
                "                       ;$ENDPOINT;v1;$URL;e1;n1;des"
        )
    }

    def cleanup() {
        IntegrationTestUtil.removeAll InboundChannelConfigurationModel
        IntegrationTestUtil.removeAll IntegrationObjectModel
        IntegrationTestUtil.removeAll BasicCredentialModel
        IntegrationTestUtil.removeAll OAuthClientDetailsModel
        IntegrationTestUtil.removeAll ExposedOAuthCredentialModel
        IntegrationTestUtil.removeAll DestinationTargetModel
        IntegrationTestUtil.removeAll EndpointModel
        IntegrationTestUtil.removeAll ExposedDestinationModel
    }

    @Test
    def "Impex:there is no exception when credential of ExposedDestination match authenticationType of InboundChannelConfiguration"() {
        when:
        IntegrationTestUtil.importImpEx(
                "INSERT_UPDATE ExposedDestination;id[unique=true];url;endpoint(id);additionalProperties;destinationTarget(id)[default=$TARGET];active;credential(id);inboundChannelConfiguration(integrationObject(code))",
                "                                ;$DESTINATION1;$URL;$ENDPOINT;;;true;$BASIC_CRED;$IO",
                "                                ;$DESTINATION2;$URL;$ENDPOINT;;;true;$BASIC_CRED;$IO"
        )
        then:
        noExceptionThrown()
    }

    @Test
    def "Impex: throw exception when credential of ExposedDestination not match authenticationType of InboundChannelConfiguration"() {
        when:
        IntegrationTestUtil.importImpEx(
                "INSERT_UPDATE ExposedDestination;id[unique=true];url;endpoint(id);additionalProperties;destinationTarget(id)[default=$TARGET];active;credential(id);inboundChannelConfiguration(integrationObject(code))",
                "                              ;$DESTINATION1;$URL;$ENDPOINT;;;true;$OAUTH_CRED;$IO"
        )

        then:
        def e = thrown AssertionError
        e.message.contains "does not match assigned credential type of InboundChannelConfiguration"
    }
}
