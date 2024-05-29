package ch.dvbern.ebegu.ws.neskovanp.oicd;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.OIDCServiceException;
import ch.dvbern.ebegu.ws.oicd.OIDCClient;
import ch.dvbern.ebegu.ws.oicd.OIDCToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
public class OIDCTokenManagerBean {

	private static final Logger LOG = LoggerFactory.getLogger(OIDCTokenManagerBean.class);

	private OIDCToken currentToken;

	@Inject
	private EbeguConfiguration config;

	public OIDCToken getValidOICDToken() throws OIDCServiceException {
		if (currentToken == null || currentToken.isExpired()) {
			if(currentToken != null) {
				LOG.warn("There is an invalid Token: {}", currentToken);
			}
			issueOICDToken();
		}

		return currentToken;
	}

	private void issueOICDToken() throws OIDCServiceException {
		OIDCClient oicdClient = new OIDCClient()
			.cientId(config.getEbeguKibonAnfrageOIDCClientId())
			.secret(config.getEbeguKibonAnfrageOIDCSecret())
			.endpoint(config.getEbeguKibonAnfrageOIDCEndpoint());

		currentToken = oicdClient.issueToken();
	}
}
