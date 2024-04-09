package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

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
	private boolean lastenausgleichAktiv;
	private boolean ferienbetreuungAktiv;
	private final boolean angebotMittagstischEnabled;
	private boolean lastenausgleichTagesschulenAktiv;
	private boolean gemeindeKennzahlenAktiv;
	private BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungDe;
	private BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungFr;
	private BigDecimal lastenausgleichTagesschulenAutoZweitpruefungDe;
	private BigDecimal lastenausgleichTagesschulenAutoZweitpruefungFr;
	private String primaryColor;
	private String primaryColorDark;
	private String primaryColorLight;
	private String logoFileName;
	private String logoFileNameWhite;
	private boolean multimandantAktiviert;
	private final boolean infomaZahlungen;
	private boolean frenchEnabled;
	private boolean geresEnabledForMandant;
	private boolean ebeguKibonAnfrageTestGuiEnabled;
	private final String steuerschnittstelleAktivAb;
	private boolean zusatzinformationenInstitution;
	private String activatedDemoFeatures;
	private final boolean checkboxAuszahlungInZukunft;
	private boolean institutionenDurchGemeindenEinladen;
	private boolean erlaubenInstitutionenZuWaehlen;
	private boolean angebotTSActivated;
	private boolean angebotFIActivated;
	private boolean angebotTFOActivated;
	private boolean auszahlungAnEltern;

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
		boolean lastenausgleichAktiv,
		boolean ferienbetreuungAktiv,
		boolean lastenausgleichTagesschulenAktiv,
		boolean gemeindeKennzahlenAktiv,
		BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungDe,
		BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungFr,
		BigDecimal lastenausgleichTagesschulenAutoZweitpruefungDe,
		BigDecimal lastenausgleichTagesschulenAutoZweitpruefungFr,
		String primaryColor,
		String primaryColorDark,
		String primaryColorLight,
		String logoFileName,
		String logoFileNameWhite,
		boolean multimandantAktiviert,
		boolean infomaZahlungen,
		boolean frenchEnabled,
		boolean geresEnabled,
		boolean ebeguKibonAnfrageTestGuiEnabled,
		String steuerschnittstelleAktivAb,
		boolean zusatzinformationenInstitution,
		String activatedDemoFeatures,
		boolean checkboxAuszahlungInZukunft,
		boolean institutionenDurchGemeindenEinladen,
		boolean erlaubenInstitutionenZuWaehlen,
		boolean angebotTSActivated,
		boolean angebotFIActivated,
		boolean angebotMittagstischEnabled,
		boolean angebotTFOActivated,
		boolean auszahlungAnEltern
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
		this.lastenausgleichAktiv = lastenausgleichAktiv;
		this.ferienbetreuungAktiv = ferienbetreuungAktiv;
		this.angebotMittagstischEnabled = angebotMittagstischEnabled;
		this.lastenausgleichTagesschulenAktiv = lastenausgleichTagesschulenAktiv;
		this.gemeindeKennzahlenAktiv = gemeindeKennzahlenAktiv;
		this.lastenausgleichTagesschulenAnteilZweitpruefungDe = lastenausgleichTagesschulenAnteilZweitpruefungDe;
		this.lastenausgleichTagesschulenAnteilZweitpruefungFr = lastenausgleichTagesschulenAnteilZweitpruefungFr;
		this.lastenausgleichTagesschulenAutoZweitpruefungDe = lastenausgleichTagesschulenAutoZweitpruefungDe;
		this.lastenausgleichTagesschulenAutoZweitpruefungFr = lastenausgleichTagesschulenAutoZweitpruefungFr;
		this.primaryColor = primaryColor;
		this.primaryColorDark = primaryColorDark;
		this.primaryColorLight = primaryColorLight;
		this.logoFileName = logoFileName;
		this.logoFileNameWhite = logoFileNameWhite;
		this.multimandantAktiviert = multimandantAktiviert;
		this.angebotTSActivated = angebotTSActivated;
		this.infomaZahlungen = infomaZahlungen;
		this.frenchEnabled = frenchEnabled;
		this.geresEnabledForMandant = geresEnabled;
		this.ebeguKibonAnfrageTestGuiEnabled = ebeguKibonAnfrageTestGuiEnabled;
		this.steuerschnittstelleAktivAb = steuerschnittstelleAktivAb;
		this.zusatzinformationenInstitution = zusatzinformationenInstitution;
		this.activatedDemoFeatures = activatedDemoFeatures;
		this.checkboxAuszahlungInZukunft = checkboxAuszahlungInZukunft;
		this.institutionenDurchGemeindenEinladen = institutionenDurchGemeindenEinladen;
		this.erlaubenInstitutionenZuWaehlen = erlaubenInstitutionenZuWaehlen;
		this.angebotFIActivated = angebotFIActivated;
		this.angebotTFOActivated = angebotTFOActivated;
		this.auszahlungAnEltern = auszahlungAnEltern;
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

	public boolean isLastenausgleichAktiv() {
		return lastenausgleichAktiv;
	}

	public void setLastenausgleichAktiv(boolean lastenausgleichAktiv) {
		this.lastenausgleichAktiv = lastenausgleichAktiv;
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

	public BigDecimal getLastenausgleichTagesschulenAnteilZweitpruefungDe() {
		return lastenausgleichTagesschulenAnteilZweitpruefungDe;
	}

	public void setLastenausgleichTagesschulenAnteilZweitpruefungDe(BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungDe) {
		this.lastenausgleichTagesschulenAnteilZweitpruefungDe = lastenausgleichTagesschulenAnteilZweitpruefungDe;
	}

	public BigDecimal getLastenausgleichTagesschulenAnteilZweitpruefungFr() {
		return lastenausgleichTagesschulenAnteilZweitpruefungFr;
	}

	public void setLastenausgleichTagesschulenAnteilZweitpruefungFr(BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungFr) {
		this.lastenausgleichTagesschulenAnteilZweitpruefungFr = lastenausgleichTagesschulenAnteilZweitpruefungFr;
	}

	public BigDecimal getLastenausgleichTagesschulenAutoZweitpruefungDe() {
		return lastenausgleichTagesschulenAutoZweitpruefungDe;
	}

	public void setLastenausgleichTagesschulenAutoZweitpruefungDe(BigDecimal lastenausgleichTagesschulenAutoZweitpruefungDe) {
		this.lastenausgleichTagesschulenAutoZweitpruefungDe = lastenausgleichTagesschulenAutoZweitpruefungDe;
	}

	public BigDecimal getLastenausgleichTagesschulenAutoZweitpruefungFr() {
		return lastenausgleichTagesschulenAutoZweitpruefungFr;
	}

	public void setLastenausgleichTagesschulenAutoZweitpruefungFr(BigDecimal lastenausgleichTagesschulenAutoZweitpruefungFr) {
		this.lastenausgleichTagesschulenAutoZweitpruefungFr = lastenausgleichTagesschulenAutoZweitpruefungFr;
	}

	public boolean isGemeindeKennzahlenAktiv() {
		return gemeindeKennzahlenAktiv;
	}

	public void setGemeindeKennzahlenAktiv(boolean gemeindeKennzahlenAktiv) {
		this.gemeindeKennzahlenAktiv = gemeindeKennzahlenAktiv;
	}

	public boolean isMultimandantAktiviert() {
		return multimandantAktiviert;
	}

	public void setMultimandantAktiviert(boolean multimandantAktiviert) {
		this.multimandantAktiviert = multimandantAktiviert;
	}

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getPrimaryColorDark() {
		return primaryColorDark;
	}

	public void setPrimaryColorDark(String primaryColorDark) {
		this.primaryColorDark = primaryColorDark;
	}

	public String getPrimaryColorLight() {
		return primaryColorLight;
	}

	public void setPrimaryColorLight(String primaryColorLight) {
		this.primaryColorLight = primaryColorLight;
	}

	public String getLogoFileName() {
		return logoFileName;
	}

	public void setLogoFileName(String logoFileName) {
		this.logoFileName = logoFileName;
	}

	public String getLogoFileNameWhite() {
		return logoFileNameWhite;
	}

	public void setLogoFileNameWhite(String logoFileNameWhite) {
		this.logoFileNameWhite = logoFileNameWhite;
	}

	public boolean isAngebotTSActivated() {
		return angebotTSActivated;
	}

	public void setAngebotTSActivated(boolean angebotTSActivated) {
		this.angebotTSActivated = angebotTSActivated;
	}

	public boolean isInfomaZahlungen() {
		return infomaZahlungen;
	}

	public boolean isFrenchEnabled() {
		return frenchEnabled;
	}

	public void setFrenchEnabled(boolean frenchEnabled) {
		this.frenchEnabled = frenchEnabled;
	}

	public boolean isGeresEnabledForMandant() {
		return geresEnabledForMandant;
	}

	public void setGeresEnabledForMandant(boolean geresEnabledForMandant) {
		this.geresEnabledForMandant = geresEnabledForMandant;
	}

	public boolean isEbeguKibonAnfrageTestGuiEnabled() {
		return ebeguKibonAnfrageTestGuiEnabled;
	}

	public void setEbeguKibonAnfrageTestGuiEnabled(boolean ebeguKibonAnfrageTestGuiEnabled) {
		this.ebeguKibonAnfrageTestGuiEnabled = ebeguKibonAnfrageTestGuiEnabled;
	}

	public String getSteuerschnittstelleAktivAb() {
		return steuerschnittstelleAktivAb;
	}

	public boolean isZusatzinformationenInstitution() {
		return zusatzinformationenInstitution;
	}

	public void setZusatzinformationenInstitution(boolean zusatzinformationenInstitution) {
		this.zusatzinformationenInstitution = zusatzinformationenInstitution;
	}

	public String getActivatedDemoFeatures() {
		return activatedDemoFeatures;
	}

	public void setActivatedDemoFeatures(String activatedDemoFeatures) {
		this.activatedDemoFeatures = activatedDemoFeatures;
	}

	public boolean isCheckboxAuszahlungInZukunft() {
		return checkboxAuszahlungInZukunft;
	}

	public boolean isInstitutionenDurchGemeindenEinladen() {
		return institutionenDurchGemeindenEinladen;
	}

	public void setInstitutionenDurchGemeindenEinladen(boolean institutionenDurchGemeindenEinladen) {
		this.institutionenDurchGemeindenEinladen = institutionenDurchGemeindenEinladen;
	}

	public boolean isErlaubenInstitutionenZuWaehlen() {
		return erlaubenInstitutionenZuWaehlen;
	}

	public void setErlaubenInstitutionenZuWaehlen(boolean erlaubenInstitutionenZuWaehlen) {
		this.erlaubenInstitutionenZuWaehlen = erlaubenInstitutionenZuWaehlen;
	}

	public boolean isAngebotFIActivated() {
		return angebotFIActivated;
	}

	public void setAngebotFIActivated(boolean angebotFIActivated) {
		this.angebotFIActivated = angebotFIActivated;
	}

	public boolean isAngebotTFOActivated() {
		return angebotTFOActivated;
	}

	public void setAngebotTFOActivated(boolean angebotTFOActivated) {
		this.angebotTFOActivated = angebotTFOActivated;
	}

	public boolean isAngebotMittagstischEnabled() {
		return angebotMittagstischEnabled;
	}

	public boolean isAuszahlungAnEltern() {
		return auszahlungAnEltern;
	}

	public void setAuszahlungAnEltern(boolean auszahlungAnEltern) {
		this.auszahlungAnEltern = auszahlungAnEltern;
	}
}
