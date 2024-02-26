package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;

public class JaxUebersichtVersendeteMails extends JaxAbstractDTO {
	private static final long serialVersionUID = 3359889275785229022L;

	@Nonnull
	private LocalDateTime zeitpunktVersand;

	@Nonnull
	private String empfaengerAdresse;

	@Nonnull
	private String betreff;

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
}
