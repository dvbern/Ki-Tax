package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;
import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;

public class SchwyzTestfallDataProvider extends AbstractTestfallDataProvider {

	protected SchwyzTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
		super(gesuchsperiode);
	}

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinanzielleSituation finanzielleSituation = createDefaultFinanzielleSituation();
		finanzielleSituation.setMomentanSelbststaendig(true);
		finanzielleSituation.setNettolohn(einkommen);
		finanzielleSituation.setSteuerbaresVermoegen(vermoegen);
		finanzielleSituation.setUnterhaltsBeitraege(BigDecimal.ZERO);
		finanzielleSituation.setAbzuegeKinderAusbildung(BigDecimal.ZERO);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.SOLOTHURN;
	}

	@Override
	public Kind createKind(
		Geschlecht geschlecht,
		String name,
		String vorname,
		LocalDate geburtsdatum,
		boolean is18GeburtstagBeforeGPEnds,
		Kinderabzug kinderabzug,
		boolean betreuung) {
		Kind kind = new Kind();
		kind.setGeschlecht(geschlecht);
		kind.setGeburtsdatum(geburtsdatum);
		kind.setVorname(vorname);
		kind.setNachname(name);
		kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		if (betreuung) {
			kind.setFamilienErgaenzendeBetreuung(true);
			kind.setUnterhaltspflichtig(true);
			kind.setLebtKindAlternierend(true);
			kind.setGemeinsamesGesuch(true);
		}
		return kind;
	}
}
