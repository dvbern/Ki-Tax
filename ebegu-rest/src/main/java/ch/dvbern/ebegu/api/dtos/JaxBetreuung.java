package ch.dvbern.ebegu.api.dtos;

import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO fuer Daten der Betreuungen,
 */
@XmlRootElement(name = "betreuung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuung extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297022381674937397L;

	@NotNull
	private JaxInstitutionStammdaten institutionStammdaten;

	@NotNull
	private Betreuungsstatus betreuungsstatus;

	@NotNull
	private List<JaxBetreuungspensumContainer> betreuungspensumContainers = new ArrayList<>();

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private String bemerkungen;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private String grundAblehnung;

	@Min(1)
	private Integer betreuungNummer = 1;

	@Nullable
	private JaxVerfuegung verfuegung;

	@NotNull
	private Boolean vertrag;

	@NotNull
	private Boolean erweiterteBeduerfnisse;



	public JaxInstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(JaxInstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	public Betreuungsstatus getBetreuungsstatus() {
		return betreuungsstatus;
	}

	public void setBetreuungsstatus(Betreuungsstatus betreuungsstatus) {
		this.betreuungsstatus = betreuungsstatus;
	}

	public List<JaxBetreuungspensumContainer> getBetreuungspensumContainers() {
		return betreuungspensumContainers;
	}

	public void setBetreuungspensumContainers(List<JaxBetreuungspensumContainer> betreuungspensumContainers) {
		this.betreuungspensumContainers = betreuungspensumContainers;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nullable
	public String getGrundAblehnung() {
		return grundAblehnung;
	}

	public void setGrundAblehnung(@Nullable String grundAblehnung) {
		this.grundAblehnung = grundAblehnung;
	}

	public Integer getBetreuungNummer() {
		return betreuungNummer;
	}

	public void setBetreuungNummer(Integer betreuungNummer) {
		this.betreuungNummer = betreuungNummer;
	}

	@Nullable
	public JaxVerfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(@Nullable JaxVerfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	public Boolean getVertrag() {
		return vertrag;
	}

	public void setVertrag(Boolean vertrag) {
		this.vertrag = vertrag;
	}

	public Boolean getErweiterteBeduerfnisse() {
		return erweiterteBeduerfnisse;
	}

	public void setErweiterteBeduerfnisse(Boolean erweiterteBeduerfnisse) {
		this.erweiterteBeduerfnisse = erweiterteBeduerfnisse;
	}
}
