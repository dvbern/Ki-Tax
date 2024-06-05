package ch.dvbern.ebegu.ws.oicd;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.time.LocalDateTime;

public class OIDCToken {

	@JsonProperty("access_token")
	private String token;

	@JsonProperty("expires_in")
	private String expiresIn;

	@JsonProperty("token_type")
	private String tokenType;

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
			return;
		}

		expiersAt = requestTime.plusSeconds(Integer.parseInt(expiresIn));
	}
	public String getAuthToken() {
		return tokenType + ' ' + token;
	}

	public void setRequestTime(LocalDateTime requestTime) {
		this.requestTime = requestTime;
	}

	public String toString(){
		return new ToStringBuilder(this)
			.append("Token", token)
			.append("expiresIn", expiresIn)
			.append("expiersAt", expiersAt)
			.toString();
	}
}
