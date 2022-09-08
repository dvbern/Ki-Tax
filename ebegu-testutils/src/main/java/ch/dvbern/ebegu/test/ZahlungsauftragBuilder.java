package ch.dvbern.ebegu.test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

/**
 * Vereinfachte Zusammenstellung von Testdaten. Kann weiter ergänzt werden
 * Verwendung:
 *
 * Zahlungsauftrag auftrag = ZahlungsauftragBuilder.create(builder -> builder
 * 			.withZahlungslauftyp(ZahlungslaufTyp.GEMEINDE_INSTITUTION)
 * 			.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 31))
 * 			.withDatumFaellig(LocalDate.of(2022, Month.AUGUST, 31))
 * 	);
 */
@SuppressWarnings("UnusedReturnValue")
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

	public ZahlungsauftragBuilder withDatumGeneriert(@Nonnull LocalDate datumGeneriert) {
		zahlungsauftrag.setDatumGeneriert(datumGeneriert.atTime(LocalTime.of(12, 15)));
		return this;
	}

	public ZahlungsauftragBuilder withGueltigkeit(@Nonnull DateRange gueltigkeit) {
		zahlungsauftrag.setGueltigkeit(gueltigkeit);
		return this;
	}

	public ZahlungsauftragBuilder withDatumFaellig(@Nonnull LocalDate datumFaellig) {
		zahlungsauftrag.setDatumFaellig(datumFaellig);
		return this;
	}

	public ZahlungsauftragBuilder withBeschrieb(@Nonnull String beschrieb) {
		zahlungsauftrag.setBeschrieb(beschrieb);
		return this;
	}

	public ZahlungsauftragBuilder withMandant(@Nonnull Mandant mandant) {
		zahlungsauftrag.setMandant(mandant);
		return this;
	}

	public ZahlungsauftragBuilder withZahlung(
		@Nonnull BigDecimal betrag,
		@Nonnull String empfaenger,
		@Nonnull String empfaengerKonto
	) {
		Zahlung zahlung = new Zahlung();
		zahlung.setBetragTotalZahlung(betrag);
		zahlung.setEmpfaengerName(empfaenger);
		zahlung.setAuszahlungsdaten(new Auszahlungsdaten());
		zahlung.getAuszahlungsdaten().setIban(new IBAN(empfaengerKonto));
		zahlung.getAuszahlungsdaten().setInfomaKreditorennummer(empfaengerKonto);
		zahlung.getAuszahlungsdaten().setInfomaBankcode("010");
		zahlung.getAuszahlungsdaten().setKontoinhaber(empfaenger);
		zahlungsauftrag.getZahlungen().add(zahlung);
		zahlung.setZahlungsauftrag(zahlungsauftrag);

		Zahlungsposition position = new Zahlungsposition();
		Verfuegung verfuegung = new Verfuegung();

		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setVerfuegung(verfuegung);
		verfuegung.getZeitabschnitte().add(zeitabschnitt);

		final Dossier dossier = TestDataUtil.createDefaultDossier();
		final Gesuchsperiode gp = TestDataUtil.createGesuchsperiodeXXYY(2021, 2022);
		final Gesuch gesuch = TestDataUtil.createGesuch(dossier, gp, AntragStatus.VERFUEGT);

		final Betreuung betreuung = new Betreuung();
		betreuung.setKind(new KindContainer());
		betreuung.getKind().setKindNummer(1);
		betreuung.getKind().setGesuch(gesuch);
		verfuegung.setBetreuung(betreuung);
		position.setVerfuegungZeitabschnitt(zeitabschnitt);
		zahlung.getZahlungspositionen().add(position);
		return this;
	}

	@Nonnull
	public static Zahlungsauftrag create(@Nonnull Consumer<ZahlungsauftragBuilder> block) {
		ZahlungsauftragBuilder builder = new ZahlungsauftragBuilder();
		block.accept(builder);
		return builder.zahlungsauftrag;
	}
}
