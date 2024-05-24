package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public abstract class AbstractTestfallDataProvider {

	protected final Gesuchsperiode gesuchsperiode;

	protected AbstractTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public abstract Familiensituation createVerheiratet();
	public abstract Familiensituation createAlleinerziehend();

	public abstract FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen);

	public abstract FinanzielleSituationTyp getFinanzielleSituationTyp();

	protected Familiensituation createDefaultFieldsOfFamiliensituation() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setVerguenstigungGewuenscht(true); // by default verguenstigung gewuenscht
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(true);
		familiensituation.setAuszahlungsdaten(createDefaultAuszahlungsdaten());
		return familiensituation;
	}

	protected Auszahlungsdaten createDefaultAuszahlungsdaten() {
		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN("CH2089144969768441935"));
		auszahlungsdaten.setKontoinhaber("kiBon Test");
		return auszahlungsdaten;
	}

	protected FinanzielleSituation createDefaultFinanzielleSituation() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(true);
		finanzielleSituation.setSteuererklaerungAusgefuellt(true);
		setFinSitDefaultValues(finanzielleSituation);
		return finanzielleSituation;
	}

	/**
	 * Schreibt in alle Felder der finanziellenSituation, die nicht Null sein dürfen, eine 0. Diese kann später in den
	 * Testfällen überschrieben werden.
	 */
	private void setFinSitDefaultValues(@Nonnull FinanzielleSituation finanzielleSituation) {
		finanzielleSituation.setFamilienzulage(BigDecimal.ZERO);
		finanzielleSituation.setErsatzeinkommen(BigDecimal.ZERO);
		finanzielleSituation.setErhalteneAlimente(BigDecimal.ZERO);
		finanzielleSituation.setGeleisteteAlimente(BigDecimal.ZERO);
		finanzielleSituation.setNettolohn(BigDecimal.ZERO);
		finanzielleSituation.setBruttovermoegen(BigDecimal.ZERO);
		finanzielleSituation.setSchulden(BigDecimal.ZERO);
	}

	public Kind createKind(
		Geschlecht geschlecht,
		String name,
		String vorname,
		LocalDate geburtsdatum,
		boolean is18GeburtstagBeforeGPEnds,
		Kinderabzug kinderabzug,
		boolean betreuung) {
		Kind kind = new Kind();
		final TestKindParameter testKindParameter =
			TestKindParameter.builder()
				.geburtsdatum(geburtsdatum)
				.betreuung(betreuung)
				.kind(kind)
				.geschlecht(geschlecht)
				.name(name)
				.is18GeburtstagBeforeGPEnds(is18GeburtstagBeforeGPEnds)
				.kinderabzug(kinderabzug)
				.vorname(vorname)
				.build();
		setRequiredKindData(testKindParameter);

		return kind;
	}

	public static void setRequiredKindData(TestKindParameter testKindParameter) {
		final Kind kind = testKindParameter.getKind();
		kind.setGeschlecht(testKindParameter.getGeschlecht());
		kind.setNachname(testKindParameter.getName());
		kind.setVorname(testKindParameter.getVorname());
		kind.setGeburtsdatum(testKindParameter.getGeburtsdatum());
		kind.setKinderabzugErstesHalbjahr(testKindParameter.getKinderabzug());
		kind.setKinderabzugZweitesHalbjahr(testKindParameter.getKinderabzug());
		if (Boolean.TRUE.equals(testKindParameter.getIs18GeburtstagBeforeGPEnds())) {
			kind.setInErstausbildung(false);
		} else {
			kind.setObhutAlternierendAusueben(false);
		}
		kind.setFamilienErgaenzendeBetreuung(testKindParameter.isBetreuung());
		if (testKindParameter.isBetreuung()) {
			kind.setSprichtAmtssprache(Boolean.TRUE);
			kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		}
	}

	public Erwerbspensum createErwerbspensum(int prozent) {
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		erwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.setPensum(prozent);
		return erwerbspensum;
	}

	/**
	 * @param gesuchstellerNumber is required in overriding methods
	 */
	public Gesuchsteller createGesuchsteller(String name, String vorname, int gesuchstellerNumber) {
		Gesuchsteller gesuchsteller = new Gesuchsteller();
		gesuchsteller.setGeschlecht(Geschlecht.WEIBLICH);
		gesuchsteller.setNachname(name);
		gesuchsteller.setVorname(vorname);
		gesuchsteller.setGeburtsdatum(LocalDate.of(1980, Month.MARCH, 25));
		gesuchsteller.setDiplomatenstatus(false);
		gesuchsteller.setMail("test@mailbucket.dvbern.ch");
		gesuchsteller.setMobile("079 000 00 00");
		gesuchsteller.setKorrespondenzSprache(Sprache.DEUTSCH);
		return gesuchsteller;
	}
}

