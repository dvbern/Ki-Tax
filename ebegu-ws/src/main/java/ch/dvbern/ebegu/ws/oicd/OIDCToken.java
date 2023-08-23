package ch.dvbern.ebegu.ws.oicd;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDateTime;

public class OIDCToken {

	@JsonProperty("access_token")
	private String token;

	@JsonProperty("expires_in")
	private String expiresIn;

	@JsonProperty("refresh_expires_in")
	private String refreshExpiresIn;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("not-before-policy")
	private String notBeforePolicy;

	@JsonProperty("scope")
	private String scope;

	private LocalDateTime expiersAt;

	private LocalDateTime requestTime;

	public boolean isExpired() {
		if (expiersAt == null) {
			calculateExpiringTime();
		}

		return LocalDateTime.now().isAfter(expiersAt);
	}

	private void calculateExpiringTime() {
		if (requestTime == null || StringUtils.isBlank(expiresIn)) {
			expiersAt = LocalDateTime.now();
		}

		expiersAt = requestTime.plusSeconds(Integer.parseInt(expiresIn));
	}
	public String getAuthToken() {
		return tokenType + ' ' + token;
	}

	public void setRequestTime(LocalDateTime requestTime) {
		this.requestTime = requestTime;
	}
}
