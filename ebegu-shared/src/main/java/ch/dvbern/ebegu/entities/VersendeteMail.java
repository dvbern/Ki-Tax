package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.search.annotations.Field;

@Entity
public class VersendeteMail extends AbstractEntity {
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

	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Field
	private final MandantIdentifier mandantIdentifier;

	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	protected VersendeteMail() {
		this.zeitpunktVersand = LocalDateTime.now();
		this.empfaengerAdresse = "";
		this.betreff = "";
		this.mandantIdentifier = MandantIdentifier.BERN;
	}

	public VersendeteMail(@Nonnull LocalDateTime zeitpunktVersand, @Nonnull String empfaengerAdresse,
			@Nonnull String betreff, @Nonnull MandantIdentifier mandant) {
		this.zeitpunktVersand = zeitpunktVersand;
		this.empfaengerAdresse = empfaengerAdresse;
		this.betreff = betreff;
		this.mandantIdentifier = mandant;
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

	@Nonnull
	public MandantIdentifier getMandantIdentifier() {
		return mandantIdentifier;
	}
}
