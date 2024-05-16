package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;

public class JaxVersendeteMail extends JaxAbstractDTO {
	private static final long serialVersionUID = 3359889275785229022L;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime zeitpunktVersand;

	@Nonnull
	private String empfaengerAdresse;

	@Nonnull
	private String betreff;

	@Nonnull
	private MandantIdentifier mandantIdentifier;

	@Nonnull
	public LocalDateTime getZeitpunktVersand() {
		return zeitpunktVersand;
	}

	public void setZeitpunktVersand(@Nonnull LocalDateTime zeitpunktVersand) {
		this.zeitpunktVersand = zeitpunktVersand;
	}

	@Nonnull
	public String getEmpfaengerAdresse() {
		return empfaengerAdresse;
	}

	public void setEmpfaengerAdresse(@Nonnull String empfaengerAdresse) {
		this.empfaengerAdresse = empfaengerAdresse;
	}

	@Nonnull
	public String getBetreff() {
		return betreff;
	}

	public void setBetreff(@Nonnull String betreff) {
		this.betreff = betreff;
	}

	@Nonnull
	public MandantIdentifier getMandantIdentifier() {
		return mandantIdentifier;
	}

	public void setMandantIdentifier(@Nonnull MandantIdentifier mandantIdentifier) {
		this.mandantIdentifier = mandantIdentifier;
	}
}
