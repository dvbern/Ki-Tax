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
	private boolean personenSucheDisabled;
	private String kitaxHost;
	private String kitaxEndpoint;
	private String notverordnungDefaultEinreichefristOeffentlich;
	private String notverordnungDefaultEinreichefristPrivat;
	private boolean ferienbetreuungAktiv;
	private boolean lastenausgleichTagesschulenAktiv;

	public JaxPublicAppConfig(
		String currentNode,
		boolean devmode,
		String whitelist,
		boolean dummyMode,
		String sentryEnvName,
		String backgroundColor,
		boolean zahlungentestmode,
		boolean personenSucheDisabled,
		String kitaxHost,
		String kitaxEndpoint,
		String notverordnungDefaultEinreichefristOeffentlich,
		String notverordnungDefaultEinreichefristPrivat,
		boolean ferienbetreuungAktiv,
		boolean lastenausgleichTagesschulenAktiv
	) {
		this.currentNode = currentNode;
		this.devmode = devmode;
		this.whitelist = whitelist;
		this.dummyMode = dummyMode;
		this.sentryEnvName = sentryEnvName;
		this.backgroundColor = backgroundColor;
		this.zahlungentestmode = zahlungentestmode;
		this.personenSucheDisabled = personenSucheDisabled;
		this.kitaxHost = kitaxHost;
		this.kitaxEndpoint = kitaxEndpoint;
		this.notverordnungDefaultEinreichefristOeffentlich = notverordnungDefaultEinreichefristOeffentlich;
		this.notverordnungDefaultEinreichefristPrivat = notverordnungDefaultEinreichefristPrivat;
		this.ferienbetreuungAktiv = ferienbetreuungAktiv;
		this.lastenausgleichTagesschulenAktiv = lastenausgleichTagesschulenAktiv;
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

	public boolean isPersonenSucheDisabled() {
		return personenSucheDisabled;
	}

	public void setPersonenSucheDisabled(boolean personenSucheDisabled) {
		this.personenSucheDisabled = personenSucheDisabled;
	}

	public String getKitaxHost() {
		return kitaxHost;
	}

	public void setKitaxHost(String kitaxHost) {
		this.kitaxHost = kitaxHost;
	}

	public String getKitaxEndpoint() {
		return kitaxEndpoint;
	}

	public void setKitaxEndpoint(String kitaxEndpoint) {
		this.kitaxEndpoint = kitaxEndpoint;
	}

	public String getNotverordnungDefaultEinreichefristOeffentlich() {
		return notverordnungDefaultEinreichefristOeffentlich;
	}

	public void setNotverordnungDefaultEinreichefristOeffentlich(String notverordnungDefaultEinreichefristOeffentlich) {
		this.notverordnungDefaultEinreichefristOeffentlich = notverordnungDefaultEinreichefristOeffentlich;
	}

	public String getNotverordnungDefaultEinreichefristPrivat() {
		return notverordnungDefaultEinreichefristPrivat;
	}

	public void setNotverordnungDefaultEinreichefristPrivat(String notverordnungDefaultEinreichefristPrivat) {
		this.notverordnungDefaultEinreichefristPrivat = notverordnungDefaultEinreichefristPrivat;
	}

	public boolean isFerienbetreuungAktiv() {
		return ferienbetreuungAktiv;
	}

	public void setFerienbetreuungAktiv(boolean ferienbetreuungAktiv) {
		this.ferienbetreuungAktiv = ferienbetreuungAktiv;
	}

	public boolean isLastenausgleichTagesschulenAktiv() {
		return lastenausgleichTagesschulenAktiv;
	}

	public void setLastenausgleichTagesschulenAktiv(boolean lastenausgleichTagesschulenAktiv) {
		this.lastenausgleichTagesschulenAktiv = lastenausgleichTagesschulenAktiv;
	}
}
