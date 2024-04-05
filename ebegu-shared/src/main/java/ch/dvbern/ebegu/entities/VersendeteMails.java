package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
public class VersendeteMails extends AbstractEntity {
	private static final long serialVersionUID = 3359889299785229122L;

	@Nonnull
	@Column(nullable = false)
	private final LocalDateTime zeitpunktVersand;

	@Nonnull
	@Column(nullable = false)
	private final String empfaengerAdresse;

	@Nonnull
	@Column(nullable = false)
	private final String betreff;

	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	protected VersendeteMails() {
		this.zeitpunktVersand = null;
		this.empfaengerAdresse = "";
		this.betreff = "";
	}

	public VersendeteMails(@Nonnull LocalDateTime zeitpunktVersand, @Nonnull String empfaengerAdresse,
			@Nonnull String betreff) {
		this.zeitpunktVersand = zeitpunktVersand;
		this.empfaengerAdresse = empfaengerAdresse;
		this.betreff = betreff;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.equals(other);
	}

	@Nonnull
	public LocalDateTime getZeitpunktVersand() {
		return zeitpunktVersand;
	}

	@Nonnull
	public String getEmpfaengerAdresse() {
		return empfaengerAdresse;
	}

	@Nonnull
	public String getBetreff() {
		return betreff;
	}
}
