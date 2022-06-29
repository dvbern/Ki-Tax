package ch.dvbern.ebegu.test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

/**
 * Vereinfachte Zusammenstellung von Testdaten. Kann weiter ergänzt werden
 * Verwendung:
 *
 * Zahlungsauftrag auftrag = ZahlungsauftragBuilder.create(builder -> builder
 * 			.withZahlungslauftyp(ZahlungslaufTyp.GEMEINDE_INSTITUTION)
 * 			.withGesuchsteller2("Müller", "Anna"));
 */
public class ZahlungsauftragBuilder {

	private final Zahlungsauftrag zahlungsauftrag;

	public ZahlungsauftragBuilder() {
		zahlungsauftrag = new Zahlungsauftrag();
		zahlungsauftrag.setZahlungslaufTyp(ZahlungslaufTyp.GEMEINDE_INSTITUTION);
		zahlungsauftrag.setDatumGeneriert(LocalDateTime.now());
		zahlungsauftrag.setDatumFaellig(LocalDate.now());
	}

	public ZahlungsauftragBuilder withZahlungslauftyp(@Nonnull ZahlungslaufTyp typ) {
		zahlungsauftrag.setZahlungslaufTyp(typ);
		return this;
	}

	public ZahlungsauftragBuilder withDatumGeneriert(LocalDate datumGeneriert) {
		zahlungsauftrag.setDatumGeneriert(datumGeneriert.atTime(LocalTime.of(12, 15)));
		return this;
	}

	public ZahlungsauftragBuilder withDatumFaellig(LocalDate datumFaellig) {
		zahlungsauftrag.setDatumFaellig(datumFaellig);
		return this;
	}

	public ZahlungsauftragBuilder withBeschrieb(String beschrieb) {
		zahlungsauftrag.setBeschrieb(beschrieb);
		return this;
	}

	public ZahlungsauftragBuilder withZahlung(
		@Nonnull BigDecimal betrag,
		@Nonnull String empfaenger,
		String empfaengerKonto) {
		Zahlung zahlung = new Zahlung();
		zahlung.setBetragTotalZahlung(betrag);
		zahlung.setEmpfaengerName(empfaenger);
		zahlung.setAuszahlungsdaten(new Auszahlungsdaten());
		zahlung.getAuszahlungsdaten().setIban(new IBAN(empfaengerKonto));
		zahlungsauftrag.getZahlungen().add(zahlung);
		zahlung.setZahlungsauftrag(zahlungsauftrag);
		return this;
	}

	public static Zahlungsauftrag create(Consumer<ZahlungsauftragBuilder> block) {
		ZahlungsauftragBuilder builder = new ZahlungsauftragBuilder();
		block.accept(builder);
		return builder.zahlungsauftrag;
	}


}
