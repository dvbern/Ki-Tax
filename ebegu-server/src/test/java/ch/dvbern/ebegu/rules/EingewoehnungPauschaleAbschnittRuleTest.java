package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculate;
import static ch.dvbern.ebegu.test.TestDataUtil.ENDE_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class EingewoehnungPauschaleAbschnittRuleTest {

	@Test
	void betreuungspensum_ohneEingewoehnungPaschale_hasNoEingewoehnungMessageAndNoPauschaleSet() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(START_PERIODE, ENDE_PERIODE, BetreuungsangebotTyp.KITA, 80,  BigDecimal.valueOf(800));
		assertThat(betreuung.getKind().getGesuch().getGesuchsteller1(), notNullValue());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(START_PERIODE, ENDE_PERIODE, 60));

		assertThat((betreuung.getBetreuungspensumContainers().size()), Matchers.is(1));
		//explizit im Test keine Eingewöhnungspauschale setzten --> Test ist immer korrekt auch wenn die Testdaten mal ändern sollten
		betreuung.getBetreuungspensumContainers().forEach(pensum -> {
			pensum.getBetreuungspensumJA().setEingewoehnungPauschale(null);
		});

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		assertThat(result, notNullValue());
		assertThat(result.size(), Matchers.is(1));
		assertThat(result.get(0).getBgCalculationInputAsiv().getEingewoehnungPauschale(), is(BigDecimal.ZERO));
		assertThat(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(false));
	}

	@Test
	void betreuungspensum_mitEingewoehnungPaschale_hasEingewoehnungMessageAndPauschaleFirstMonth() {
		BigDecimal eingewoehnungPauschale = BigDecimal.valueOf(500);

		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(START_PERIODE, ENDE_PERIODE, BetreuungsangebotTyp.KITA, 80,  BigDecimal.valueOf(800));
		assertThat(betreuung.getKind().getGesuch().getGesuchsteller1(), notNullValue());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(START_PERIODE, ENDE_PERIODE, 60));

		assertThat((betreuung.getBetreuungspensumContainers().size()), Matchers.is(1));
		betreuung.getBetreuungspensumContainers().forEach(pensum -> {
			pensum.getBetreuungspensumJA().setEingewoehnungPauschale(new EingewoehnungPauschale());
			pensum.getBetreuungspensumJA().getEingewoehnungPauschale().setPauschale(eingewoehnungPauschale);
		});

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		DateRange gueltigkeitFirstMonth = new DateRange(START_PERIODE, START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()));
		DateRange gueltigkeitWithoutFirstMonth = new DateRange(START_PERIODE.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), ENDE_PERIODE);
		assertThat(result, notNullValue());
		assertThat(result.size(), Matchers.is(2));
		assertThat(result.get(0).getBgCalculationInputAsiv().getEingewoehnungPauschale(), Matchers.is(eingewoehnungPauschale));
		assertThat(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(true));
		assertThat(result.get(0).getGueltigkeit(), is(gueltigkeitFirstMonth));

		assertThat(result.get(1).getBgCalculationInputAsiv().getEingewoehnungPauschale(), is(BigDecimal.ZERO));
		assertThat(result.get(1).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(false));
		assertThat(result.get(1).getGueltigkeit(), is(gueltigkeitWithoutFirstMonth));
	}

	@Test
	void betreuungspensum_eingewoehnungPaschaleStartsDuringMonth_hasEingewoehnugStartingDuringMonth() {
		BigDecimal eingewoehnungPauschale = BigDecimal.valueOf(500);

		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(START_PERIODE.plusDays(15), ENDE_PERIODE, BetreuungsangebotTyp.KITA, 80,  BigDecimal.valueOf(800));
		assertThat(betreuung.getKind().getGesuch().getGesuchsteller1(), notNullValue());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(START_PERIODE, ENDE_PERIODE, 60));

		assertThat((betreuung.getBetreuungspensumContainers().size()), Matchers.is(1));
		betreuung.getBetreuungspensumContainers().forEach(pensum -> {
			pensum.getBetreuungspensumJA().setEingewoehnungPauschale(new EingewoehnungPauschale());
			pensum.getBetreuungspensumJA().getEingewoehnungPauschale().setPauschale(eingewoehnungPauschale);
		});

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		DateRange gueltigkeitFirstMonthFirstPart = new DateRange(START_PERIODE, START_PERIODE.plusDays(14));
		DateRange gueltigkeitFirstMonthSecondPart = new DateRange(START_PERIODE.plusDays(15), START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()));
		DateRange gueltigkeitWithoutFirstMonth = new DateRange(START_PERIODE.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), ENDE_PERIODE);

		assertThat(result, notNullValue());
		assertThat(result.size(), Matchers.is(3));

		assertThat(result.get(0).getBgCalculationInputAsiv().getEingewoehnungPauschale(), Matchers.is(BigDecimal.ZERO));
		assertThat(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(false));
		assertThat(result.get(0).getGueltigkeit(), is(gueltigkeitFirstMonthFirstPart));

		assertThat(result.get(1).getBgCalculationInputAsiv().getEingewoehnungPauschale(), Matchers.is(eingewoehnungPauschale));
		assertThat(result.get(1).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(true));
		assertThat(result.get(1).getGueltigkeit(), is(gueltigkeitFirstMonthSecondPart));

		assertThat(result.get(2).getBgCalculationInputAsiv().getEingewoehnungPauschale(), is(BigDecimal.ZERO));
		assertThat(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(false));
		assertThat(result.get(2).getGueltigkeit(), is(gueltigkeitWithoutFirstMonth));
	}

	@Test
	void betreuungspensum_eingewoehnungPaschaleStartsDuringMonth_hasEingewoehnugEndingDuringMonth() {
		BigDecimal eingewoehnungPauschale = BigDecimal.valueOf(500);

		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(START_PERIODE, START_PERIODE.plusDays(15), BetreuungsangebotTyp.KITA, 80,  BigDecimal.valueOf(800));
		assertThat(betreuung.getKind().getGesuch().getGesuchsteller1(), notNullValue());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(START_PERIODE, ENDE_PERIODE, 60));

		assertThat((betreuung.getBetreuungspensumContainers().size()), Matchers.is(1));
		betreuung.getBetreuungspensumContainers().forEach(pensum -> {
			pensum.getBetreuungspensumJA().setEingewoehnungPauschale(new EingewoehnungPauschale());
			pensum.getBetreuungspensumJA().getEingewoehnungPauschale().setPauschale(eingewoehnungPauschale);
		});

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		DateRange gueltigkeitFirstMonthFirstPart = new DateRange(START_PERIODE, START_PERIODE.plusDays(15));
		DateRange gueltigkeitWithoutFirstPartOfFirstMonth = new DateRange(START_PERIODE.plusDays(16), ENDE_PERIODE);

		assertThat(result, notNullValue());
		assertThat(result.size(), Matchers.is(2));

		assertThat(result.get(0).getBgCalculationInputAsiv().getEingewoehnungPauschale(), Matchers.is(eingewoehnungPauschale));
		assertThat(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(true));
		assertThat(result.get(0).getGueltigkeit(), is(gueltigkeitFirstMonthFirstPart));

		assertThat(result.get(1).getBgCalculationInputAsiv().getEingewoehnungPauschale(), is(BigDecimal.ZERO));
		assertThat(result.get(1).getBemerkungenDTOList().containsMsgKey(MsgKey.EINGEWOEHUNG_PASCHALE), is(false));
		assertThat(result.get(1).getGueltigkeit(), is(gueltigkeitWithoutFirstPartOfFirstMonth));
	}
}
