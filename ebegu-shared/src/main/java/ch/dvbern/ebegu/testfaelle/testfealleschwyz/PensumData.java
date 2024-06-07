package ch.dvbern.ebegu.testfaelle.testfealleschwyz;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PensumData {

	@Getter @Setter
	private int pensum;

	@Getter @Setter
	private LocalDate gueltigAb;

	@Getter @Setter
	private LocalDate gueltigBis;

	@Getter @Setter
	private BigDecimal monatlicheBetreuungskosten;

	@Getter @Setter
	private boolean betreuungInFerienzeit;

	@Getter @Setter
	private BigDecimal monatlicheHauptmahlzeiten = BigDecimal.ZERO;

	@Getter @Setter
	private BigDecimal tarifProMahlzeit = BigDecimal.ZERO;

}
