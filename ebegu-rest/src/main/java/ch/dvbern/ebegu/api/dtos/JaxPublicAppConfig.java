package ch.dvbern.ebegu.api.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a dto that transfers all public application properties at once
 */
@XmlRootElement(name = "pubConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxPublicAppConfig {

	private String currentNode;
	private String devmode;
	private String whitelist;
	private String dummyMode;
	private String sentryEnvName;
	private String backgroundColor;
	private String zahlungentestmode;

	public JaxPublicAppConfig(String currentNode, String devmode, String whitelist, String dummyMode, String sentryEnvName, String backgroundColor,
		String zahlungentestmode) {
		this.currentNode = currentNode;
		this.devmode = devmode;
		this.whitelist = whitelist;
		this.dummyMode = dummyMode;
		this.sentryEnvName = sentryEnvName;
		this.backgroundColor = backgroundColor;
		this.zahlungentestmode = zahlungentestmode;
	}

	public String getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(String currentNode) {
		this.currentNode = currentNode;
	}

	public String getDevmode() {
		return devmode;
	}

	public void setDevmode(String devmode) {
		this.devmode = devmode;
	}

	public String getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist;
	}

	public String getDummyMode() {
		return dummyMode;
	}

	public void setDummyMode(String dummyMode) {
		this.dummyMode = dummyMode;
	}

	public String getSentryEnvName() {
		return sentryEnvName;
	}

	public void setSentryEnvName(String sentryEnvName) {
		this.sentryEnvName = sentryEnvName;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getZahlungentestmode() {
		return zahlungentestmode;
	}

	public void setZahlungentestmode(String zahlungentestmode) {
		this.zahlungentestmode = zahlungentestmode;
	}
}
