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
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;


public class TestfallSchwyz1 extends AbstractSZTestfall {

	public TestfallSchwyz1(@Nonnull Gesuchsperiode gesuchsperiode, boolean betreuungenBestaetigt, @Nonnull Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	private Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createVerheiratet();
		erstgesuch.setEingangsdatum(LocalDate.of(2024, Month.AUGUST, 3));

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

		// Erwerbspensum
		ErwerbspensumContainer erwerbspensumgs1 = createErwerbspensumContainer(gs1);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensumgs1);

		ErwerbspensumContainer erwerbspensumgs2 = createErwerbspensumContainer(gs2);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensumgs2);

		//FinSit
		FinanzielleSituationContainer finanzielleSituationContainer = createFinSit(gs1);
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);
		setAuszahlungsdaten(erstgesuch, gs1);

		//EKV
		EinkommensverschlechterungContainer ekvContainer = createEinkommensverschlechterungContainer(erstgesuch, true, false);
		ekvContainer.getEkvJABasisJahrPlus1().setSteuerbaresEinkommen(MathUtil.DEFAULT.from(43280));
		ekvContainer.getEkvJABasisJahrPlus1().setSteuerbaresVermoegen(MathUtil.DEFAULT.from(72500));
		ekvContainer.getEkvJABasisJahrPlus1().setEinkaeufeVorsorge(BigDecimal.ZERO);
		ekvContainer.getEkvJABasisJahrPlus1().setAbzuegeLiegenschaft(BigDecimal.ZERO);
 	 	gesuchsteller1.setEinkommensverschlechterungContainer(ekvContainer);

		return erstgesuch;
	}

	private GesuchstellerData createGS1Data() {
		return new GesuchstellerData()
			.setGesuchstellerNummer(1)
			.setVorname("Laura")
			.setNachname("Müller")
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(1995, Month.MARCH, 15))
			.setSvNummer("756.1234.5678.97")
			.setStrasse("Teststrasse")
			.setHausnummer("11")
			.setPlz("6400")
			.setOrt("Rom")
			.setErwerbspensum(80)
			.setErwerbsBezeichnung("Köchin")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(new DateRange(LocalDate.of(2015, Month.JULY, 1), Constants.END_OF_TIME))
			.setReineinkommen(BigDecimal.valueOf(69192))
			.setReinvermoegen(BigDecimal.valueOf(72500))
			.setIban(new IBAN("CH2889144129525238874"))
			.setKontoinhaber("Laura Müller")
			.setGemeinsameSteuererklaerung(true);
	}

	private GesuchstellerData createGS2Data() {
		return new GesuchstellerData()
			.setGesuchstellerNummer(2)
			.setVorname("Leandro")
			.setNachname("Meier")
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(1995, Month.DECEMBER, 8))
			.setSvNummer("756.1234.5677.98")
			.setErwerbspensum(100)
			.setErwerbsBezeichnung("Bankkaufmann")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(new DateRange(
				LocalDate.of(2020, Month.MAY, 1),
				LocalDate.of(2025, Month.JANUARY, 31)));
	}

	private KindData createKind1Data() {
		return new KindData()
			.setVorname("Linus")
			.setNachname("Müller")
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(2022, Month.APRIL, 4))
			.setKinderabzug(Kinderabzug.GANZER_ABZUG)
			.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE)
			.setFamilienergaenzendBetreuug(true)
			.setGemeinsamesGesuch(true)
			.setUnterhaltspflichtig(true)
			.setLebtAlternierend(true)
			.setHohereBeitraege(false);
	}

	private BetreuungData createBeteruung1() {
		BetreuungData betreuungData = new BetreuungData()
			.setAuszahlungAnEltern(false)
			.setBegruendung("Test")
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen())
			.setBestaetigt(betreuungenBestaetigt);

		betreuungData.getBetreuungspensum().add(new PensumData()
			.setPensum(50)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1000))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31)));

		return betreuungData;
	}


	private BetreuungData createBeteruung2() {
		BetreuungData betreuung = new BetreuungData()
			.setAuszahlungAnEltern(false)
			.setBegruendung("Test")
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenWeissenstein())
			.setBestaetigt(betreuungenBestaetigt);

		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1061.05))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 11))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1450.80))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 12))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 31))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(967.24))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.SEPTEMBER, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.SEPTEMBER, 29))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(3765))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.SEPTEMBER, 30))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.SEPTEMBER, 30))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1556.20))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 20))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1758.55))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 21))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 31))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1248))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.NOVEMBER, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.NOVEMBER, 30))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1318.91))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 22))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(2161.39))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 23))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 31))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(778.1))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 5))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1488))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 6))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 31))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1139.48))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 23))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(2108.4))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 24))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 28))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(3890.50))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MARCH, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MARCH, 2))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1445.25))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MARCH, 3))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MARCH, 31))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1271.11))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.APRIL, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.APRIL, 27))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(3765))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.APRIL, 28))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.APRIL, 30))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1061.05))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MAY, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MAY, 11))
			.setBetreuungInFerienzeit(true));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1450.8))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MAY, 12))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.MAY, 31))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1352))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JUNE, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JUNE, 30))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(45)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1074.67))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 6))
			.setBetreuungInFerienzeit(false));
		betreuung.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1867.44))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 7))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31))
			.setBetreuungInFerienzeit(true));
		return betreuung;
	}

	private BetreuungData createBeteruung3() {
		BetreuungData betreuungData = new BetreuungData()
			.setAuszahlungAnEltern(false)
			.setBegruendung("Test")
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenMittagstisch())
			.setBestaetigt(betreuungenBestaetigt);

		betreuungData.getBetreuungspensum().add(new PensumData()
			.setPensum(60)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(147.60))
			.setMonatlicheHauptmahlzeiten(BigDecimal.valueOf(12.3))
			.setTarifProMahlzeit(BigDecimal.valueOf(12))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(Constants.END_OF_TIME));

		return betreuungData;
	}

	private KindContainer createKindAndBetreuung1() {
		KindData kindData = createKind1Data();
		kindData.getBetreuungDataList().add(createBeteruung1());
		kindData.getBetreuungDataList().add(createBeteruung2());
		kindData.getBetreuungDataList().add(createBeteruung3());
		return createKindContainer(kindData);
	}

}
