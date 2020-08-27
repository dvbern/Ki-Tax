/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BIGDECIMAL_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_X_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.INTEGER_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.PERCENT_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldGesuchstellerKinderBetreuung implements MergeFieldProvider {

	gesuchstellerTitle(new SimpleMergeField<>("gesuchstellerTitle", STRING_CONVERTER)),
	stichtagTitle(new SimpleMergeField<>("stichtagTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	angebotTitle(new SimpleMergeField<>("angebotTitle", STRING_CONVERTER)),
	parameterTitle(new SimpleMergeField<>("parameterTitle", STRING_CONVERTER)),
	periodeTitle(new SimpleMergeField<>("periodeTitle", STRING_CONVERTER)),
	eingangsdatumTitle(new SimpleMergeField<>("eingangsdatumTitle", STRING_CONVERTER)),
	nachnameTitle(new SimpleMergeField<>("nachnameTitle", STRING_CONVERTER)),
	vornameTitle(new SimpleMergeField<>("vornameTitle", STRING_CONVERTER)),
	verfuegungsdatumTitle(new SimpleMergeField<>("verfuegungsdatumTitle", STRING_CONVERTER)),
	gemeindeTitle(new SimpleMergeField<>("gemeindeTitle", STRING_CONVERTER)),
	fallIdTitle(new SimpleMergeField<>("fallIdTitle", STRING_CONVERTER)),
	gesuchsteller1Title(new SimpleMergeField<>("gesuchsteller1Title", STRING_CONVERTER)),
	gesuchsteller2Title(new SimpleMergeField<>("gesuchsteller2Title", STRING_CONVERTER)),
	strasseTitle(new SimpleMergeField<>("strasseTitle", STRING_CONVERTER)),
	strasseNrTitel(new SimpleMergeField<>("strasseNrTitel", STRING_CONVERTER)),
	zusatzTitle(new SimpleMergeField<>("zusatzTitle", STRING_CONVERTER)),
	plzTitle(new SimpleMergeField<>("plzTitle", STRING_CONVERTER)),
	ortTitle(new SimpleMergeField<>("ortTitle", STRING_CONVERTER)),
	diplomatTitle(new SimpleMergeField<>("diplomatTitle", STRING_CONVERTER)),
	beschaeftigungspensumTitle(new SimpleMergeField<>("beschaeftigungspensumTitle", STRING_CONVERTER)),
	totalTitle(new SimpleMergeField<>("totalTitle", STRING_CONVERTER)),
	angestelltTitle(new SimpleMergeField<>("angestelltTitle", STRING_CONVERTER)),
	weiterbildungTitle(new SimpleMergeField<>("weiterbildungTitle", STRING_CONVERTER)),
	selbstaendigTitle(new SimpleMergeField<>("selbstaendigTitle", STRING_CONVERTER)),
	arbeitssuchendTitle(new SimpleMergeField<>("arbeitssuchendTitle", STRING_CONVERTER)),
	integrationTitle(new SimpleMergeField<>("integrationTitle", STRING_CONVERTER)),
	freiwilligenarbeitTitle(new SimpleMergeField<>("gesFreiwilligenarbeitTitle", STRING_CONVERTER)),
	gesIndikationTitle(new SimpleMergeField<>("gesIndikationTitle", STRING_CONVERTER)),
	familieTitle(new SimpleMergeField<>("familieTitle", STRING_CONVERTER)),
	famSituationTitle(new SimpleMergeField<>("famSituationTitle", STRING_CONVERTER)),
	famGroesseTitle(new SimpleMergeField<>("famGroesseTitle", STRING_CONVERTER)),
	einkommenTitle(new SimpleMergeField<>("einkommenTitle", STRING_CONVERTER)),
	einkommenVorAbzugTitle(new SimpleMergeField<>("einkommenVorAbzugTitle", STRING_CONVERTER)),
	famAbzugTitle(new SimpleMergeField<>("famAbzugTitle", STRING_CONVERTER)),
	massEinkommenTitle(new SimpleMergeField<>("massEinkommenTitle", STRING_CONVERTER)),
	einkommensjahrTitle(new SimpleMergeField<>("einkommensjahrTitle", STRING_CONVERTER)),
	einkommensverschlechterungTitle(new SimpleMergeField<>("einkommensverschlechterungTitle", STRING_CONVERTER)),
	geprueftSTVTitle(new SimpleMergeField<>("geprueftSTVTitle", STRING_CONVERTER)),
	veranlagtTitle(new SimpleMergeField<>("veranlagtTitle", STRING_CONVERTER)),
	kinderTitle(new SimpleMergeField<>("kinderTitle", STRING_CONVERTER)),
	kindTitle(new SimpleMergeField<>("kindTitle", STRING_CONVERTER)),
	vonTitle(new SimpleMergeField<>("vonTitle", STRING_CONVERTER)),
	bisTitle(new SimpleMergeField<>("bisTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	fachstelleTitle(new SimpleMergeField<>("fachstelleTitle", STRING_CONVERTER)),
	babyFaktorTitle(new SimpleMergeField<>("babyFaktorTitle", STRING_CONVERTER)),
	besonderebeduerfnisseTitle(new SimpleMergeField<>("besonderebeduerfnisseTitle", STRING_CONVERTER)),
	sprichtAmtsspracheTitle(new SimpleMergeField<>("sprichtAmtsspracheTitle", STRING_CONVERTER)),
	schulstufeTitle(new SimpleMergeField<>("schulstufeTitle", STRING_CONVERTER)),
	bis1MonateTitle(new SimpleMergeField<>("bis1MonateTitle", STRING_CONVERTER)),
	bis2MonateTitle(new SimpleMergeField<>("bis2MonateTitle", STRING_CONVERTER)),
	bis3MonateTitle(new SimpleMergeField<>("bis3MonateTitle", STRING_CONVERTER)),
	abMonateTitle(new SimpleMergeField<>("abMonateTitle", STRING_CONVERTER)),
	betreuungVonTitle(new SimpleMergeField<>("betreuungVonTitle", STRING_CONVERTER)),
	betreuungBisTitle(new SimpleMergeField<>("betreuungBisTitle", STRING_CONVERTER)),
	betreuungStatus(new SimpleMergeField<>("betreuungStatus", STRING_CONVERTER)),
	anteilMonatTitle(new SimpleMergeField<>("anteilMonatTitle", STRING_CONVERTER)),
	betreuungTitle(new SimpleMergeField<>("betreuungTitle", STRING_CONVERTER)),
	pensumTitle(new SimpleMergeField<>("pensumTitle", STRING_CONVERTER)),
	kostenTitle(new SimpleMergeField<>("kostenTitle", STRING_CONVERTER)),
	anspruchberechtigtKantonTitle(new SimpleMergeField<>("anspruchberechtigtKantonTitle", STRING_CONVERTER)),
	anspruchberechtigtGemeindeTitle(new SimpleMergeField<>("anspruchberechtigtGemeindeTitle", STRING_CONVERTER)),
	anspruchberechtigtTotalTitle(new SimpleMergeField<>("anspruchberechtigtTotalTitle", STRING_CONVERTER)),
	bgPensumKantonTitle(new SimpleMergeField<>("bgPensumKantonTitle", STRING_CONVERTER)),
	bgPensumGemeindeTitle(new SimpleMergeField<>("bgPensumGemeindeTitle", STRING_CONVERTER)),
	bgPensumTotalTitle(new SimpleMergeField<>("bgPensumTotalTitle", STRING_CONVERTER)),
	bgPensumStdTitle(new SimpleMergeField<>("bgPensumStdTitle", STRING_CONVERTER)),
	bgPensumZeiteinheitTitle(new SimpleMergeField<>("bgPensumZeiteinheitTitle", STRING_CONVERTER)),
	bgMonatspensumTitle(new SimpleMergeField<>("bgMonatspensumTitle", STRING_CONVERTER)),
	vollkostenTitle(new SimpleMergeField<>("vollkostenTitle", STRING_CONVERTER)),
	elternbeitragTitle(new SimpleMergeField<>("elternbeitragTitle", STRING_CONVERTER)),
	gutscheinKantonTitel(new SimpleMergeField<>("gutscheinKantonTitel", STRING_CONVERTER)),
	gutscheinGemeindeTitel(new SimpleMergeField<>("gutscheinGemeindeTitel", STRING_CONVERTER)),
	gutscheinTotalTitel(new SimpleMergeField<>("gutscheinTotalTitel", STRING_CONVERTER)),
	statusTitle(new SimpleMergeField<>("statusTitle", STRING_CONVERTER)),
	gesuchstellerKinderBetreuungTitle(new SimpleMergeField<>("gesuchstellerKinderBetreuungTitle", STRING_CONVERTER)),
	bgNummerTitle(new SimpleMergeField<>("bgNummerTitle", STRING_CONVERTER)),

	stichtag(new SimpleMergeField<>("stichtag", DATE_CONVERTER)),
	auswertungVon(new SimpleMergeField<>("auswertungVon", DATE_CONVERTER)),
	auswertungBis(new SimpleMergeField<>("auswertungBis", DATE_CONVERTER)),
	auswertungPeriode(new SimpleMergeField<>("auswertungPeriode", STRING_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow")),

	bgNummer(new SimpleMergeField<>("bgNummer", STRING_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	betreuungsTyp(new SimpleMergeField<>("betreuungsTyp", STRING_CONVERTER)),
	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	gesuchStatus(new SimpleMergeField<>("gesuchStatus", STRING_CONVERTER)),

	eingangsdatum(new SimpleMergeField<>("eingangsdatum", DATE_CONVERTER)),
	verfuegungsdatum(new SimpleMergeField<>("verfuegungsdatum", DATE_CONVERTER)),
	fallId(new SimpleMergeField<>("fallId", INTEGER_CONVERTER)),
	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),

	gs1Name(new SimpleMergeField<>("gs1Name", STRING_CONVERTER)),
	gs1Vorname(new SimpleMergeField<>("gs1Vorname", STRING_CONVERTER)),
	gs1Strasse(new SimpleMergeField<>("gs1Strasse", STRING_CONVERTER)),
	gs1Hausnummer(new SimpleMergeField<>("gs1Hausnummer", STRING_CONVERTER)),
	gs1Zusatzzeile(new SimpleMergeField<>("gs1Zusatzzeile", STRING_CONVERTER)),
	gs1Plz(new SimpleMergeField<>("gs1Plz", STRING_CONVERTER)),
	gs1Ort(new SimpleMergeField<>("gs1Ort", STRING_CONVERTER)),
	gs1Diplomatenstatus(new SimpleMergeField<>("gs1Diplomatenstatus", BOOLEAN_X_CONVERTER)),
	gs1EwpAngestellt(new SimpleMergeField<>("gs1EwpAngestellt", PERCENT_CONVERTER)),
	gs1EwpAusbildung(new SimpleMergeField<>("gs1EwpAusbildung", PERCENT_CONVERTER)),
	gs1EwpSelbstaendig(new SimpleMergeField<>("gs1EwpSelbstaendig", PERCENT_CONVERTER)),
	gs1EwpRav(new SimpleMergeField<>("gs1EwpRav", PERCENT_CONVERTER)),
	gs1EwpGesundhtl(new SimpleMergeField<>("gs1EwpGesundhtl", PERCENT_CONVERTER)),
	gs1EwpIntegration(new SimpleMergeField<>("gs1EwpIntegration", PERCENT_CONVERTER)),
	gs1EwpFreiwillig(new SimpleMergeField<>("gs1EwpFreiwillig", PERCENT_CONVERTER)),

	gs2Name(new SimpleMergeField<>("gs2Name", STRING_CONVERTER)),
	gs2Vorname(new SimpleMergeField<>("gs2Vorname", STRING_CONVERTER)),
	gs2Strasse(new SimpleMergeField<>("gs2Strasse", STRING_CONVERTER)),
	gs2Hausnummer(new SimpleMergeField<>("gs2Hausnummer", STRING_CONVERTER)),
	gs2Zusatzzeile(new SimpleMergeField<>("gs2Zusatzzeile", STRING_CONVERTER)),
	gs2Plz(new SimpleMergeField<>("gs2Plz", STRING_CONVERTER)),
	gs2Ort(new SimpleMergeField<>("gs2Ort", STRING_CONVERTER)),
	gs2Diplomatenstatus(new SimpleMergeField<>("gs2Diplomatenstatus", BOOLEAN_X_CONVERTER)),
	gs2EwpAngestellt(new SimpleMergeField<>("gs2EwpAngestellt", PERCENT_CONVERTER)),
	gs2EwpAusbildung(new SimpleMergeField<>("gs2EwpAusbildung", PERCENT_CONVERTER)),
	gs2EwpSelbstaendig(new SimpleMergeField<>("gs2EwpSelbstaendig", PERCENT_CONVERTER)),
	gs2EwpRav(new SimpleMergeField<>("gs2EwpRav", PERCENT_CONVERTER)),
	gs2EwpGesundhtl(new SimpleMergeField<>("gs2EwpGesundhtl", PERCENT_CONVERTER)),
	gs2EwpIntegration(new SimpleMergeField<>("gs2EwpIntegration", PERCENT_CONVERTER)),
	gs2EwpFreiwillig(new SimpleMergeField<>("gs2EwpFreiwillig", PERCENT_CONVERTER)),

	familiensituation(new SimpleMergeField<>("familiensituation", STRING_CONVERTER)),
	familiengroesse(new SimpleMergeField<>("familiengroesse", BIGDECIMAL_CONVERTER)),

	massgEinkVorFamilienabzug(new SimpleMergeField<>("massgEinkVorFamilienabzug", BIGDECIMAL_CONVERTER)),
	familienabzug(new SimpleMergeField<>("familienabzug", BIGDECIMAL_CONVERTER)),
	massgEink(new SimpleMergeField<>("massgEink", BIGDECIMAL_CONVERTER)),
	einkommensjahr(new SimpleMergeField<>("einkommensjahr", INTEGER_CONVERTER)),
	ekvVorhanden(new SimpleMergeField<>("ekvVorhanden", BOOLEAN_X_CONVERTER)),
	stvGeprueft(new SimpleMergeField<>("stvGeprueft", BOOLEAN_X_CONVERTER)),
	veranlagt(new SimpleMergeField<>("veranlagt", BOOLEAN_X_CONVERTER)),

	kindName(new SimpleMergeField<>("kindName", STRING_CONVERTER)),
	kindVorname(new SimpleMergeField<>("kindVorname", STRING_CONVERTER)),
	kindGeburtsdatum(new SimpleMergeField<>("kindGeburtsdatum", DATE_CONVERTER)),
	kindFachstelle(new SimpleMergeField<>("kindFachstelle", STRING_CONVERTER)),
	kindErwBeduerfnisse(new SimpleMergeField<>("kindErwBeduerfnisse", BOOLEAN_X_CONVERTER)),
	kindSprichtAmtssprache(new SimpleMergeField<>("kindSprichtAmtssprache", BOOLEAN_X_CONVERTER)),
	eingeschult(new SimpleMergeField<>("eingeschult", STRING_CONVERTER)),

	zeitabschnittVon(new SimpleMergeField<>("zeitabschnittVon", DATE_CONVERTER)),
	zeitabschnittBis(new SimpleMergeField<>("zeitabschnittBis", DATE_CONVERTER)),
	betreuungsStatus(new SimpleMergeField<>("betreuungsStatus", STRING_CONVERTER)),
	betreuungsPensum(new SimpleMergeField<>("betreuungsPensum", PERCENT_CONVERTER)),

	anspruchsPensumKanton(new SimpleMergeField<>("anspruchsPensumKanton", PERCENT_CONVERTER)),
	anspruchsPensumGemeinde(new SimpleMergeField<>("anspruchsPensumGemeinde", PERCENT_CONVERTER)),
	anspruchsPensumTotal(new SimpleMergeField<>("anspruchsPensumTotal", PERCENT_CONVERTER)),

	bgPensumKanton(new SimpleMergeField<>("bgPensumKanton", PERCENT_CONVERTER)),
	bgPensumGemeinde(new SimpleMergeField<>("bgPensumGemeinde", PERCENT_CONVERTER)),
	bgPensumTotal(new SimpleMergeField<>("bgPensumTotal", PERCENT_CONVERTER)),

	bgStunden(new SimpleMergeField<>("bgStunden", BIGDECIMAL_CONVERTER)),
	bgPensumZeiteinheit(new SimpleMergeField<>("bgPensumZeiteinheit", STRING_CONVERTER)),

	vollkosten(new SimpleMergeField<>("vollkosten", BIGDECIMAL_CONVERTER)),
	elternbeitrag(new SimpleMergeField<>("elternbeitrag", BIGDECIMAL_CONVERTER)),
	verguenstigungKanton(new SimpleMergeField<>("verguenstigungKanton", BIGDECIMAL_CONVERTER)),
	verguenstigungGemeinde(new SimpleMergeField<>("verguenstigungGemeinde", BIGDECIMAL_CONVERTER)),
	verguenstigungTotal(new SimpleMergeField<>("verguenstigungTotal", BIGDECIMAL_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldGesuchstellerKinderBetreuung(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
