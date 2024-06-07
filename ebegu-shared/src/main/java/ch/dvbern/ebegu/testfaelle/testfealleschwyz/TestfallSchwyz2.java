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


public class TestfallSchwyz2 extends AbstractSZTestfall {

	private static final String NACHNAME = "Weber";


	public TestfallSchwyz2(@Nonnull Gesuchsperiode gesuchsperiode, boolean betreuungenBestaetigt, @Nonnull Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	private Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createVerheiratet();
		erstgesuch.setEingangsdatum(LocalDate.of(2024, Month.JUNE, 12));

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
		ErwerbspensumContainer erwerbspensum2 = createErwerbspensumContainer(gs2);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensum2);

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
			.setVorname("Tino")
			.setNachname(NACHNAME)
			.setGeschlecht(Geschlecht.MAENNLICH)
			.setGeburtsdatum(LocalDate.of(1993, Month.APRIL, 6))
			.setSvNummer("756.1234.5687.95")
			.setStrasse("Teststrasse")
			.setHausnummer("22")
			.setPlz("8640")
			.setOrt("Rapperswil-Jona")
			.setErwerbspensum(100)
			.setErwerbsBezeichnung("Schneider")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(new DateRange(LocalDate.of(2019, Month.FEBRUARY, 1), Constants.END_OF_TIME))
			.setReineinkommen(BigDecimal.valueOf(75482))
			.setReinvermoegen(BigDecimal.valueOf(5600))
			.setIban(new IBAN("CH9789144635882482971"))
			.setKontoInhaber("Tino Weber")
			.setGemeinsameSteuererklaerung(true);
	}

	private GesuchstellerData createGS2Data() {
		return new GesuchstellerData()
			.setGesuchstellerNummer(2)
			.setVorname("Tanja")
			.setNachname(NACHNAME)
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(1993, Month.NOVEMBER, 26))
			.setSvNummer("756.1234.4687.98")
			.setErwerbspensum(80)
			.setErwerbsBezeichnung("Fleischfachverkäuferin")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(new DateRange(LocalDate.of(2020, Month.MAY, 1), Constants.END_OF_TIME));
	}

	private KindData createKind1Data() {
		return new KindData()
			.setVorname("Tabea")
			.setNachname(NACHNAME)
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(2021, Month.NOVEMBER, 19))
			.setKinderabzug(Kinderabzug.GANZER_ABZUG)
			.setEinschulungTyp(EinschulungTyp.VORSCHULALTER)
			.setFamilienergaenzendBetreuug(true)
			.setGemeinsamesGesuch(true)
			.setUnterhaltspflichtig(true)
			.setLebtAlternierend(true)
			.setHohereBeitraege(false);
	}

	private BetreuungData createBeteruungDataKind1() {
		PensumData pensum = new PensumData()
			.setPensum(80)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(1600))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));

		BetreuungData betreuung = new BetreuungData()
			.setAuszahlungAnEltern(true)
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen())
			.setBestaetigt(betreuungenBestaetigt);
		betreuung.getBetreuungspensum().add(pensum);
		return betreuung;
	}

	private KindData createKind2Data() {
		return new KindData()
			.setVorname("Tamara")
			.setNachname(NACHNAME)
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(2024, Month.MAY, 29))
			.setKinderabzug(Kinderabzug.GANZER_ABZUG)
			.setFamilienergaenzendBetreuug(false);
	}

	private KindContainer createKindAndBetreuung1() {
		KindData kindData = createKind1Data();
		kindData.getBetreuungDataList().add(createBeteruungDataKind1());
		return createKindContainer(kindData);
	}

	private KindContainer createKindAndBetreuung2() {
		return createKindContainer(createKind2Data());
	}
}
