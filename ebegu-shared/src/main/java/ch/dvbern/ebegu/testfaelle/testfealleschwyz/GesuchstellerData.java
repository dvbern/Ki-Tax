package ch.dvbern.ebegu.testfaelle.testfealleschwyz;

import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GesuchstellerData {

	@Getter @Setter
	private String nachname;

	@Getter @Setter
	private String vorname;

	@Getter @Setter
	private Geschlecht geschlecht;

	@Getter @Setter
	private LocalDate geburtsdatum;

	@Getter @Setter
	private String svNummer;

	@Getter @Setter
	private String strasse;

	@Getter @Setter
	private String hausnummer;

	@Getter @Setter
	private String plz;

	@Getter @Setter
	private String ort;

	@Getter @Setter
	private int gesuchstellerNummer;

	@Getter @Setter
	private int erwerbspensum;

	@Getter @Setter
	private Taetigkeit taetigkeit;

	@Getter @Setter
	private String erwerbsBezeichnung;

	@Getter @Setter
	private DateRange erwerbGueltigkeit;

	@Getter @Setter
	private boolean gemeinsameSteuererklaerung;

	@Getter @Setter
	private boolean quellenbesteuert = false;

	@Getter @Setter
	private BigDecimal reineinkommen = BigDecimal.ZERO;

	@Getter @Setter
	private BigDecimal reinvermoegen = BigDecimal.ZERO;

	@Getter @Setter
	private BigDecimal bruttoLohn = BigDecimal.ZERO;

	@Getter @Setter
	private IBAN iban;

	@Getter @Setter
	private String kontoinhaber;


	public boolean hasAdress() {
		return gesuchstellerNummer == 1;
	}
}
