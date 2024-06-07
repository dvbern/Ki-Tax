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


public class TestfallSchwyz5 extends AbstractSZTestfall {

	private static final String NACHNAME = "Bauer";


	public TestfallSchwyz5(@Nonnull Gesuchsperiode gesuchsperiode, boolean betreuungenBestaetigt, @Nonnull Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	private Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createVerheiratet();
		erstgesuch.setEingangsdatum(LocalDate.of(2024, Month.JUNE, 5));
		Objects.requireNonNull(erstgesuch.getFamiliensituationContainer());
		Objects.requireNonNull(erstgesuch.getFamiliensituationContainer().getFamiliensituationJA());
		erstgesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(false);

		GesuchstellerData gs1 = createGS1Data();
		GesuchstellerContainer gesuchsteller1 = createGesuchsteller(gs1);
		erstgesuch.setGesuchsteller1(gesuchsteller1);

		GesuchstellerData gs2 = createGS2Data();
		GesuchstellerContainer gesuchsteller2 = createGesuchsteller(gs2);
		erstgesuch.setGesuchsteller2(gesuchsteller2);

		//Kinder
		KindContainer kind1 = createKindAndBetreuung1();
		kind1.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind1);

		KindContainer kind2 = createKindAndBetreuung2();
		kind2.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind2);

		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensumContainer(gs1);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);

		//FinSit
		FinanzielleSituationContainer finanzielleSituationContainer = createFinSit(gs1);
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);
		setAuszahlungsdaten(erstgesuch, gs1);

		FinanzielleSituationContainer finanzielleSituationContainer2 = createFinSit(gs2);
		finanzielleSituationContainer2.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(finanzielleSituationContainer2);

		//EKV
		createEmptyEKVInfoContainer(erstgesuch);

		return erstgesuch;
	}

	private GesuchstellerData createGS1Data() {
		return new GesuchstellerData()
			.setGesuchstellerNummer(1)
			.setVorname(NACHNAME)
			.setNachname("Melanie")
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(2001, Month.AUGUST, 29))
			.setSvNummer("756.1894.9687.99")
			.setStrasse("Teststrasse")
			.setHausnummer("55")
			.setPlz("6400")
			.setOrt("Rom")
			.setErwerbspensum(60)
			.setErwerbsBezeichnung("Projektleitung")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(new DateRange(LocalDate.of(2023, Month.MARCH, 1), Constants.END_OF_TIME))
			.setQuellenbesteuert(true)
			.setBruttoLohn(BigDecimal.valueOf(72000))
			.setIban(new IBAN("CH8189144657532686446"))
			.setKontoinhaber("Melanie Bauer")
			.setGemeinsameSteuererklaerung(false);
	}

	private GesuchstellerData createGS2Data() {
		return new GesuchstellerData()
			.setGesuchstellerNummer(2)
			.setVorname("Moriz")
			.setNachname(NACHNAME)
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(2000, Month.FEBRUARY, 18))
			.setSvNummer("756.6894.9687.94")
			.setQuellenbesteuert(true)
			.setBruttoLohn(BigDecimal.valueOf(56000));
	}

	private KindData createKind1Data() {
		return new KindData()
			.setVorname(NACHNAME)
			.setNachname("Marco")
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(2021, Month.OCTOBER, 9))
			.setKinderabzug(Kinderabzug.GANZER_ABZUG)
			.setEinschulungTyp(EinschulungTyp.VORSCHULALTER)
			.setFamilienergaenzendBetreuug(true)
			.setGemeinsamesGesuch(false)
			.setUnterhaltspflichtig(false)
			.setHohereBeitraege(false);
	}

	private BetreuungData createBeteruungKind1() {
		BetreuungData betreuungData = new BetreuungData()
			.setAuszahlungAnEltern(true)
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen())
			.setBestaetigt(betreuungenBestaetigt);

		betreuungData.getBetreuungspensum().add(new PensumData()
			.setPensum(50)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1000))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.SEPTEMBER, 30)));
		betreuungData.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1200))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 31)));
		betreuungData.getBetreuungspensum().add(new PensumData()
			.setPensum(70)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1400))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MARCH, 31)));
		betreuungData.getBetreuungspensum().add(new PensumData()
			.setPensum(50)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1000))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.APRIL, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31)));


		return betreuungData;
	}

	private KindData createKind2Data() {
		return new KindData()
			.setVorname("Bauer")
			.setNachname("Mila")
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(2021, Month.OCTOBER, 9))
			.setKinderabzug(Kinderabzug.GANZER_ABZUG)
			.setEinschulungTyp(EinschulungTyp.VORSCHULALTER)
			.setFamilienergaenzendBetreuug(true)
			.setGemeinsamesGesuch(false)
			.setUnterhaltspflichtig(false)
			.setHohereBeitraege(false);
	}

	private BetreuungData createBeteruungKind2() {
		BetreuungData betreuung = new BetreuungData()
			.setAuszahlungAnEltern(true)
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen())
			.setBestaetigt(betreuungenBestaetigt);

		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(50)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1000))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.SEPTEMBER, 30)));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1200))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 31)));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(70)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1400))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MARCH, 31)));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(50)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1000))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.APRIL, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31)));
		return betreuung;
	}

	private KindContainer createKindAndBetreuung1() {
		KindData kindData = createKind1Data();
		kindData.getBetreuungDataList().add(createBeteruungKind1());
		return createKindContainer(kindData);
	}

	private KindContainer createKindAndBetreuung2() {
		KindData kindData = createKind2Data();
		kindData.getBetreuungDataList().add(createBeteruungKind2());
		return createKindContainer(kindData);
	}
}
