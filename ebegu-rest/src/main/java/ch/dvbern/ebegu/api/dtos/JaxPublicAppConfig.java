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
	private boolean devmode;
	private String whitelist;
	private boolean dummyMode;
	private String sentryEnvName;
	private String backgroundColor;
	private boolean zahlungentestmode;

	public JaxPublicAppConfig(
		String currentNode,
		boolean devmode,
		String whitelist,
		boolean dummyMode,
		String sentryEnvName,
		String backgroundColor,
		boolean zahlungentestmode
	) {
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

	public boolean getDevmode() {
		return devmode;
	}

	public void setDevmode(boolean devmode) {
		this.devmode = devmode;
	}

	public String getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist;
	}

	public boolean getDummyMode() {
		return dummyMode;
	}

	public void setDummyMode(boolean dummyMode) {
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

	public boolean getZahlungentestmode() {
		return zahlungentestmode;
	}

	public void setZahlungentestmode(boolean zahlungentestmode) {
		this.zahlungentestmode = zahlungentestmode;
	}
}
