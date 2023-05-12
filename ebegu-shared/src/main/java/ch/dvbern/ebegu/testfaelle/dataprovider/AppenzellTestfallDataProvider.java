package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

public class AppenzellTestfallDataProvider extends AbstractTestfallDataProvider {

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.APPENZELL);
		familiensituation.setGeteilteObhut(Boolean.TRUE);
		familiensituation.setGemeinsamerHaushaltMitObhutsberechtigterPerson(Boolean.TRUE);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.FALSE);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.APPENZELL);
		familiensituation.setGeteilteObhut(Boolean.FALSE);
		familiensituation.setGemeinsamerHaushaltMitPartner(Boolean.FALSE);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = new FinSitZusatzangabenAppenzell();

		finSitZusatzangabenAppenzell.setSteuerbaresEinkommen(einkommen);
		finSitZusatzangabenAppenzell.setSteuerbaresVermoegen(vermoegen);
		finSitZusatzangabenAppenzell.setSaeule3a(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setSaeule3aNichtBvg(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setBeruflicheVorsorge(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setLiegenschaftsaufwand(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setEinkuenfteBgsa(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setVorjahresverluste(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setPolitischeParteiSpende(BigDecimal.ZERO);
		finSitZusatzangabenAppenzell.setLeistungAnJuristischePersonen(BigDecimal.ZERO);

		FinanzielleSituation finanzielleSituation = createDefaultFinanzielleSituation();
		finanzielleSituation.setFinSitZusatzangabenAppenzell(finSitZusatzangabenAppenzell);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.APPENZELL;
	}
}
