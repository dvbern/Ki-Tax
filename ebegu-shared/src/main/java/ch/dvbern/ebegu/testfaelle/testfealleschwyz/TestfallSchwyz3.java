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


public class TestfallSchwyz3 extends AbstractSZTestfall {


	public TestfallSchwyz3(@Nonnull Gesuchsperiode gesuchsperiode, boolean betreuungenBestaetigt, @Nonnull Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	private Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createAlleinerziehend();
		erstgesuch.setEingangsdatum(LocalDate.of(2024, Month.JUNE, 7));

		GesuchstellerData gs1 = createGS1Data();
		GesuchstellerContainer gesuchsteller1 = createGesuchsteller(gs1);
		erstgesuch.setGesuchsteller1(gesuchsteller1);

		//Kinder
		KindContainer kind = createKindAndBetreuung();
		kind.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind);

		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensumContainer(gs1);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);

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
			.setVorname("Sarah")
			.setNachname("Fischer")
			.setGeschlecht(Geschlecht.WEIBLICH)
			.setGeburtsdatum(LocalDate.of(1999, Month.DECEMBER, 8))
			.setSvNummer("756.1834.4687.92")
			.setStrasse("Teststrasse")
			.setHausnummer("33")
			.setPlz("6400")
			.setOrt("Rom")
			.setErwerbspensum(60)
			.setErwerbsBezeichnung("Verkäuferin")
			.setTaetigkeit(Taetigkeit.ANGESTELLT)
			.setErwerbGueltigkeit(new DateRange(LocalDate.of(2024, Month.MAY, 1), Constants.END_OF_TIME))
			.setReineinkommen(BigDecimal.valueOf(27521))
			.setIban(new IBAN("CH6389144457847957247"))
			.setKontoinhaber("Sarah Fischer");
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
		PensumData pensumData = new PensumData()
			.setPensum(30)
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(900))
			.setGueltigAb(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1))
			.setGueltigBis(LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));

		BetreuungData betreuungData = new BetreuungData()
			.setAuszahlungAnEltern(true)
			.setInstiutionId(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen())
			.setBestaetigt(betreuungenBestaetigt);
		betreuungData.getBetreuungspensum().add(pensumData);
		return betreuungData;
	}

	private KindContainer createKindAndBetreuung() {
		KindData kindData = createKindData();
		kindData.getBetreuungDataList().add(createBeteruungData());
		return createKindContainer(kindData);
	}
}
