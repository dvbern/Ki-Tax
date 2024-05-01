package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang.NotImplementedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FinanzielleSituationSchwyzRechner extends AbstractFinanzielleSituationRechner {
	@Override
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller) {
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());

		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
		}

		calculateFinSit(finanzielleSituationGS1, finanzielleSituationGS2, finSitResultDTO);
	}

	@Override
	public void setEinkommensverschlechterungParameters(
		@Nonnull Gesuch gesuch,
		int basisJahrPlus,
		FinanzielleSituationResultateDTO einkVerResultDTO,
		boolean hasSecondGesuchsteller) {
	}

	private void calculateFinSit(
		@Nullable FinanzielleSituation finanzielleSituationGS1,
		@Nullable FinanzielleSituation finanzielleSituationGS2,
		@Nonnull FinanzielleSituationResultateDTO finSitResultDTO
	) {
		if (finanzielleSituationGS1 != null) {
			if (Boolean.TRUE.equals(finanzielleSituationGS1.getQuellenbesteuert())) {
				finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(calculateForQuellenBesteuerte(finanzielleSituationGS1));
			} else {
				finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(calculateForNichtQuellenBesteuerte(finanzielleSituationGS1));
			}
		}
		if(finanzielleSituationGS2 != null) {
			if (Boolean.TRUE.equals(finanzielleSituationGS2.getQuellenbesteuert())) {
				finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(calculateForQuellenBesteuerte(finanzielleSituationGS2));
			} else {
				finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(calculateForNichtQuellenBesteuerte(finanzielleSituationGS2));
			}
		}
		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			add(finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS1(), finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS2())
		);
	}

	private BigDecimal calculateForNichtQuellenBesteuerte(
		@Nonnull FinanzielleSituation finanzielleSituation) {

		var einkommenGS1 = calcEinkommen(finanzielleSituation);
		var einkaufBeruflicheVorsorgeGS1 = calcEinkaeufeVorsorge(finanzielleSituation);
		var abzuegeLiegenschaftsaufwandGS1 = calcAbzuegeLiegenschaftsaufwand(finanzielleSituation);
		var reinvermoegenNachAbzugGS1 = calcReinvermoegenNachAbzug(finanzielleSituation);

		return calculateMassgebendesEinkommen(
			einkommenGS1,
			einkaufBeruflicheVorsorgeGS1,
			abzuegeLiegenschaftsaufwandGS1,
			reinvermoegenNachAbzugGS1
		);
	}

	@Nonnull
	private BigDecimal calcEinkommen(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation != null) {
			total =  add(total, finanzielleSituation.getSteuerbaresEinkommen());
		}
		return MathUtil.positiveNonNullAndRound(total);
	}

	@Nonnull
	private BigDecimal calcEinkaeufeVorsorge(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation != null) {
			total =  add(total, finanzielleSituation.getEinkaeufeVorsorge());
		}
		return MathUtil.positiveNonNullAndRound(total);
	}

	@Nonnull
	private BigDecimal calcAbzuegeLiegenschaftsaufwand(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation != null) {
			total =  add(total, finanzielleSituation.getAbzuegeLiegenschaft());
		}
		return MathUtil.positiveNonNullAndRound(total);
	}

	@Nonnull
	private BigDecimal calcReinvermoegenNachAbzug(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation != null) {
			var reinvermoegenMitAbzug = subtract(finanzielleSituation.getSteuerbaresVermoegen(), new BigDecimal(200000));
			if(BigDecimal.ZERO.compareTo(reinvermoegenMitAbzug) == -1) {
				total = percent(reinvermoegenMitAbzug, 10);
			}
		}
		return MathUtil.positiveNonNullAndRound(total);
	}

	private BigDecimal calculateMassgebendesEinkommen(
		@Nonnull BigDecimal einkommen,
		@Nonnull BigDecimal einkaufBeruflicheVorsorge,
		@Nonnull BigDecimal abzuegeLiegenschaftsaufwand,
		@Nonnull BigDecimal reinvermoegenNachAbzug
	) {

		BigDecimal anrechenbaresEinkommen = add(einkommen, einkaufBeruflicheVorsorge);
		anrechenbaresEinkommen = add(anrechenbaresEinkommen, abzuegeLiegenschaftsaufwand);
		anrechenbaresEinkommen = add(anrechenbaresEinkommen, reinvermoegenNachAbzug);
		return MathUtil.positiveNonNullAndRound(anrechenbaresEinkommen);
	}

	private BigDecimal calculateForQuellenBesteuerte(
		@Nonnull FinanzielleSituation finanzielleSituation) {
		var bruttoeinkommen = calcBruttoeinkommen(finanzielleSituation);
		var bruttopauschale = calcBruttopauschale(finanzielleSituation);
		return calculateMassgebendesEinkommen(bruttoeinkommen, bruttopauschale);
	}

	@Nonnull
	private BigDecimal calcBruttoeinkommen(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation != null) {
			total =  add(total, finanzielleSituation.getBruttoLohn());
		}
		return MathUtil.positiveNonNullAndRound(total);
	}

	@Nonnull
	private BigDecimal calcBruttopauschale(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation != null) {
			total = percent(finanzielleSituation.getBruttoLohn(), 20);
		}
		return MathUtil.positiveNonNullAndRound(total);
	}

	private BigDecimal calculateMassgebendesEinkommen(
		@Nonnull BigDecimal bruttoeinkommen,
		@Nonnull BigDecimal bruttopauschale
	) {
		BigDecimal anrechenbaresEinkommen = subtract(bruttoeinkommen, bruttopauschale);
		return MathUtil.positiveNonNullAndRound(anrechenbaresEinkommen);
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// bei Schwyz rechnen wir nie nach Veranlagung.
		throw new NotImplementedException();
	}
}
