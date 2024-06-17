package ch.dvbern.ebegu.testfaelle.testfealleschwyz;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.testfaelle.AbstractSZTestfall;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;


public class TestfallSchwyz4 extends AbstractSZTestfall {


	public TestfallSchwyz4(@Nonnull Gesuchsperiode gesuchsperiode, boolean betreuungenBestaetigt, @Nonnull Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	private Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createAlleinerziehend();
		erstgesuch.setEingangsdatum(LocalDate.of(2024, Month.JUNE, 3));

		GesuchstellerData gs1 = createGS1Data();
		GesuchstellerContainer gesuchsteller1 = createGesuchsteller(gs1);
		erstgesuch.setGesuchsteller1(gesuchsteller1);

		//Kinder
		KindContainer kind = createKindAndBetreuung();
		kind.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind);

		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum1 = createErwerbspensumContainer(gs1);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum1);
		ErwerbspensumContainer erwerbspensum2 = createErwerbspensumContainer();
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum2);

		//FinSit
		FinanzielleSituationContainer finanzielleSituationContainer = createFinSit(gs1);
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);
		setAuszahlungsdaten(erstgesuch, gs1);

		//EKV
		createEmptyEKVInfoContainer(erstgesuch);

		return erstgesuch;
	}

	private GesuchstellerData createGS1Data() {
		return new GesuchstellerData()
			.setGesuchstellerNummer(1)
			.setVorname("Bruno")
			.setNachname("Fischer")
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(1996, Month.JANUARY, 11))
			.setSvNummer("756.1894.4687.94")
			.setStrasse("Teststrasse")
			.setHausnummer("44")
			.setPlz("6400")
			.setOrt("Rom")
			.setErwerbspensum(90)
			.setErwerbsBezeichnung("Schreiner")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(
				new DateRange(LocalDate.of(2022, Month.MARCH, 1),
					LocalDate.of(2024, Month.NOVEMBER, 30)))
			.setReineinkommen(BigDecimal.valueOf(47450))
			.setIban(new IBAN("CH5589144686714478187"))
			.setKontoinhaber("Bruno Fischer");
	}

	private KindData createKindData() {
		return new KindData()
			.setVorname("Benjamin")
			.setNachname("Fischer")
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(2023, Month.MAY, 23))
			.setKinderabzug(Kinderabzug.HALBER_ABZUG)
			.setEinschulungTyp(EinschulungTyp.VORSCHULALTER)
			.setFamilienergaenzendBetreuug(true)
			.setUnterhaltspflichtig(true)
			.setGemeinsamesGesuch(true)
			.setLebtAlternierend(true)
			.setHohereBeitraege(false);
	}

	private BetreuungData createBeteruungData() {
		PensumData pensum = new PensumData()
			.setPensum(30)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(900))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));

		BetreuungData betreuungData = new BetreuungData()
			.setAuszahlungAnEltern(false)
			.setBegruendung("Test")
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen())
			.setBestaetigt(betreuungenBestaetigt);
		betreuungData.getBetreuungspensum().add(pensum);
		return betreuungData;
	}

	private KindContainer createKindAndBetreuung() {
		KindData kindData = createKindData();
		kindData.getBetreuungDataList().add(createBeteruungData());
		return createKindContainer(kindData);
	}

	protected ErwerbspensumContainer createErwerbspensumContainer() {
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(70);
		Objects.requireNonNull(erwerbspensum.getErwerbspensumJA());
		erwerbspensum.getErwerbspensumJA().setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.getErwerbspensumJA().setBezeichnung("Schreiner");
		erwerbspensum.getErwerbspensumJA().setGueltigkeit(new DateRange(LocalDate.of(2024, Month.DECEMBER, 1), Constants.END_OF_TIME));
		return erwerbspensum;
	}
}
