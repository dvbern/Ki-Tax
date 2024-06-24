package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
	private boolean abweichungenEnabled;
	private boolean gemeindeVereinfachteKonfigAktiv;
	private boolean testfaelleEnabled;

}
