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

package ch.dvbern.ebegu.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import static java.util.Objects.requireNonNull;

/**
 * Allgemeine Utils fuer EBEGU
 */
public final class EbeguUtil {

	private EbeguUtil() {
	}

	/**
	 * Berechnet ob die Daten bei der Familiensituation von einem GS auf 2 GS geaendert wurde.
	 */
	public static boolean fromOneGSToTwoGS(
		FamiliensituationContainer familiensituationContainer,
		LocalDate referenzdatum
	) {
		requireNonNull(familiensituationContainer);
		requireNonNull(familiensituationContainer.getFamiliensituationJA());
		requireNonNull(familiensituationContainer.getFamiliensituationErstgesuch());

		return fromOneGSToTwoGS(
			familiensituationContainer.getFamiliensituationErstgesuch(),
			familiensituationContainer.getFamiliensituationJA(),
			referenzdatum
		);
	}

	public static boolean fromOneGSToTwoGS(
		@Nonnull Familiensituation oldFamiliensituation,
		@Nonnull Familiensituation newFamiliensituation,
		@Nonnull LocalDate referenzdatum
	) {
		requireNonNull(oldFamiliensituation);
		requireNonNull(newFamiliensituation);
		requireNonNull(referenzdatum);

		return !oldFamiliensituation.hasSecondGesuchsteller(referenzdatum)
			&& newFamiliensituation.hasSecondGesuchsteller(referenzdatum);
	}

	/**
	 * Gibt aus einer Liste von Gesuchen nur das jeweils neueste (hoechste Laufummer) pro Fall zurueck.
	 * Die Rueckgabe erfolgt in einer Map mit GesuchId-Gesuch
	 */
	public static Map<String, Gesuch> groupByFallAndSelectNewestAntrag(List<Gesuch> allGesuche) {
		ArrayListMultimap<Fall, Gesuch> fallToAntragMultimap = ArrayListMultimap.create();
		allGesuche.forEach(gesuch -> fallToAntragMultimap.put(gesuch.getFall(), gesuch));
		// map erstellen in der nur noch das gesuch mit der hoechsten laufnummer drin ist
		Map<String, Gesuch> gesuchMap = new HashMap<>();
		for (Fall fall : fallToAntragMultimap.keySet()) {
			List<Gesuch> antraege = fallToAntragMultimap.get(fall);
			antraege.sort(Comparator.comparing(Gesuch::getLaufnummer).reversed());
			gesuchMap.put(antraege.get(0).getId(), antraege.get(0)); //nur neusten Antrag zurueckgeben
		}
		return gesuchMap;
	}

	/**
	 * Gibt aus einer Liste von Gesuchen nur das jeweils neueste (hoechste Laufummer) pro Dossier zurueck.
	 * Die Rueckgabe erfolgt in einer Map mit Gemeinde - Liste von (neuesten) Gesuchen
	 */
	public static Map<Gemeinde, List<Gesuch>> groupByDossierAndSelectNewestAntrag(@Nonnull List<Gesuch> allGesuche) {
		ArrayListMultimap<Dossier, Gesuch> dossierToAntragMultimap = ArrayListMultimap.create();
		allGesuche.forEach(gesuch -> dossierToAntragMultimap.put(gesuch.getDossier(), gesuch));
		// map erstellen in der nur noch das gesuch mit der hoechsten laufnummer drin ist
		Map<Gemeinde, List<Gesuch>> gesuchMap = new HashMap<>();
		for (Dossier dossier : dossierToAntragMultimap.keySet()) {
			List<Gesuch> antraege = dossierToAntragMultimap.get(dossier);
			antraege.sort(Comparator.comparing(Gesuch::getLaufnummer).reversed());
			Gesuch neuesterAntragProDossier = antraege.get(0);
			Gemeinde gmde = neuesterAntragProDossier.extractGemeinde();
			if (!gesuchMap.containsKey(gmde)) {
				gesuchMap.put(gmde, new ArrayList<>());
			}
			gesuchMap.get(gmde).add(neuesterAntragProDossier);
		}
		return gesuchMap;
	}

	// All die isSame Methoden finde ich aber fürchterlich komplex. Besser wäre es, Comparators zu definieren.
	public static boolean isSame(@Nullable AbstractEntity thisEntity, @Nullable AbstractEntity otherEntity) {
		return (thisEntity == null && otherEntity == null)
			|| (thisEntity != null && otherEntity != null && thisEntity.isSame(otherEntity));
	}

	/**
	 * Returns true if both strings have the same content or both are null or emptystrings
	 * or one is emptystring and the other is null
	 */
	public static boolean isSameOrNullStrings(@Nullable String thisString, @Nullable String otherString) {
		return (StringUtils.isBlank(thisString) && StringUtils.isBlank(otherString))
			|| Objects.equals(thisString, otherString);
	}

	/**
	 * Returns true if both strings have the same content or both are null or emptystrings
	 * or one is emptystring and the other is null
	 * Achtung: Diese Logik befindet sich ebenfalls clientseitig hier:
	 * EbeguUtil.ts#isFinanzielleSituationRequiredForGesuch bzw.
	 * EbeguUtil.ts#isFinanzielleSituationRequired
	 */
	public static boolean isSameOrNullBoolean(@Nullable Boolean thisBoolean, @Nullable Boolean otherBoolean) {
		return (isNullOrFalse(thisBoolean) && isNullOrFalse(otherBoolean))
			|| Objects.equals(thisBoolean, otherBoolean);
	}

	public static boolean isNullOrFalse(@Nullable Boolean value) {
		return value == null || !value;
	}

	public static boolean isNotNullAndTrue(@Nullable Boolean value) {
		return value != null && value;
	}

	public static boolean isNotNullAndFalse(@Nullable Boolean value) {
		return value != null && !value;
	}

	/**
	 * Returns true if both list are null or if they have the same number of elements
	 */
	public static boolean areListsSameSize(@Nullable Set<Dokument> dokumente, @Nullable Set<Dokument> otherDokumente) {
		if (dokumente == null && otherDokumente == null) {
			return true;
		}
		if (dokumente != null && otherDokumente != null) {
			return dokumente.size() == otherDokumente.size();
		}
		return false;
	}

	/**
	 * finanzielle situation ist by default nicht zwingend
	 */
	public static boolean isFinanzielleSituationRequired(@Nonnull Gesuch gesuch) {
		return gesuch.getFamiliensituationContainer() != null && gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
			&& BooleanUtils.isFalse(gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger())
			&& BooleanUtils.isTrue(gesuch.getFamiliensituationContainer().getFamiliensituationJA().getVerguenstigungGewuenscht());
	}

	public static boolean isSozialhilfeBezuegerNull(@Nonnull Gesuch gesuch) {
		return (gesuch.getFamiliensituationContainer() != null && gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger() == null);
	}

	public static boolean isFinanzielleSituationNotIntroducedOrIncomplete(@Nonnull Gesuch gesuch,
		@Nullable WizardStepName wizardStepName) {
		if (gesuch.getGesuchsteller1() == null
			|| gesuch.getGesuchsteller1().getFinanzielleSituationContainer() == null
			|| gesuch.getEinkommensverschlechterungInfoContainer() == null) {
			return true;
		}

		if (wizardStepName == null || wizardStepName == WizardStepName.FINANZIELLE_SITUATION) {
			if (!isFinanzielleSituationVollstaendig(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA())) {
				return true;
			}
			if (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
				&& !isFinanzielleSituationVollstaendig(gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA())
			) {
				return true;
			}
		}
		if ((wizardStepName == null || wizardStepName == WizardStepName.EINKOMMENSVERSCHLECHTERUNG)
			&& gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEinkommensverschlechterung()) {
			if (gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1()) {
				if (gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer() == null) {
					return true;
				}
				if (!isAbstractFinanzielleSituationVollstaendig(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1())) {
					return true;
				}
				if (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer() != null
						&& !isAbstractFinanzielleSituationVollstaendig(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1())
				) {
					return true;
				}
			}
			if (gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus2()) {
				if (gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer() == null) {
					return true;
				}
				if (!isAbstractFinanzielleSituationVollstaendig(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2())) {
					return true;
				}
				if (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer() != null) {
					return !isAbstractFinanzielleSituationVollstaendig(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2());
				}
			}
		}
		return false;
	}

	private static boolean isFinanzielleSituationVollstaendig(@Nonnull FinanzielleSituation finanzielleSituation) {
		if (!isAbstractFinanzielleSituationVollstaendig(finanzielleSituation)) {
			return false;
		}
		// Zwingend ist nur das erste Jahr, FALLS ueberhaupt eines ausgefuellt wird.
		// Das einzige, das wir validieren koennen, ist das Jahr+1 bzw. Jahr+2 nicht ausgefuellt sein duerfen, falls Basisjahr null
		if (finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1() != null || finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2() != null) {
			// Basisjahr ist zwingend
			return finanzielleSituation.getGeschaeftsgewinnBasisjahr() != null;
		}
		return true;
	}

	private static boolean isAbstractFinanzielleSituationVollstaendig(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return finanzielleSituation.getSchulden() != null && finanzielleSituation.getBruttovermoegen() != null
			&& finanzielleSituation.getNettolohn() != null && finanzielleSituation.getFamilienzulage() != null
			&& finanzielleSituation.getErsatzeinkommen() != null && finanzielleSituation.getErhalteneAlimente() != null
			&& finanzielleSituation.getGeleisteteAlimente() != null;
	}

	public static boolean isFamilienSituationVollstaendig(@Nonnull Gesuch gesuch) {
		return gesuch.getFamiliensituationContainer() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
			&& (gesuch.getFamiliensituationContainer().getFamiliensituationJA().getVerguenstigungGewuenscht() != null
			|| BooleanUtils.isTrue(gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger()))
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger() != null;
	}

	public static boolean isErlaeuterungenZurVerfuegungRequired(@Nonnull Gesuch gesuch) {
		// Im Status ENTWURF sollen die Erläuterungen immer als Beilage aufgeführt werden
		if (!gesuch.getStatus().isAnyStatusOfVerfuegt()) {
			return true;
		}
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			// Status VERFUEGT mit Anspruch
			if (Betreuungsstatus.VERFUEGT == betreuung.getBetreuungsstatus() && betreuung.hasAnspruch()) {
				return true;
			}
		}
		return false;
	}

	public static String getPaddedFallnummer(long fallNummer) {
		return Strings.padStart(Long.toString(fallNummer), Constants.FALLNUMMER_LENGTH, '0');
	}

	/**
	 * Will return the desired Korrespondenzsprache of the Gesuchsteller if this happens to be configured as allowed
	 * language for the Gemeinde
	 * In any other case it will return the first language that is allowed by the Gemeinde.
	 * WARNING! since allowed languages are not prioritized in the Gemeinde, the method cannot know if it should
	 * return one language or another
	 * for this reason it will return just the first language it finds.
	 */
	@Nonnull
	public static Sprache extractKorrespondenzsprache(@Nonnull Gesuch gesuch,
		@Nonnull GemeindeService gemeindeService) {
		final List<Sprache> gemeindeSprachen = extractGemeindeSprachenFromGesuch(gesuch, gemeindeService);
		final Sprache gesuchstellerGewuenschteSprache = extractGesuchstellerSprache(gesuch);

		if (gesuchstellerGewuenschteSprache != null && gemeindeSprachen.contains(gesuchstellerGewuenschteSprache)) {
			return gesuchstellerGewuenschteSprache;
		}

		return gemeindeSprachen.get(0);
	}

	/**
	 * Will return the desired Korrespondenzsprache of the Gesuchsteller if this happens to be configured as allowed
	 * language for the Gemeinde
	 * In any other case it will return the first language that is allowed by the Gemeinde.
	 * WARNING! since allowed languages are not prioritized in the Gemeinde, the method cannot know if it should
	 * return one language or another
	 * for this reason it will return just the first language it finds.
	 */
	@Nonnull
	public static Sprache extractKorrespondenzsprache(@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten gemeindeStammdaten) {
		final List<Sprache> gemeindeSprachen = extractGemeindeSprachen(gemeindeStammdaten);
		final Sprache gesuchstellerGewuenschteSprache = extractGesuchstellerSprache(gesuch);

		if (gesuchstellerGewuenschteSprache != null && gemeindeSprachen.contains(gesuchstellerGewuenschteSprache)) {
			return gesuchstellerGewuenschteSprache;
		}

		return gemeindeSprachen.get(0);
	}

	@Nullable
	private static Sprache extractGesuchstellerSprache(@Nonnull Gesuch gesuch) {
		if (gesuch.getGesuchsteller1() == null) {
			return null;
		}
		return gesuch.getGesuchsteller1().getGesuchstellerJA().getKorrespondenzSprache();
	}

	/**
	 * Will looked for the language(s) of the given Gemeinde and return them as a list.
	 * If the Gemeinde has no Stammdaten an Exception will be thrown because this shows a real problem in the data
	 * If the Gemeinde has no language configured it returns DEUTSCH as default language
	 */
	@Nonnull
	private static List<Sprache> extractGemeindeSprachenFromGesuch(@Nonnull Gesuch gesuch,
		@Nonnull GemeindeService gemeindeService) {
		return extractGemeindeSprachen(gesuch.getDossier().getGemeinde(), gemeindeService);
	}

	/**
	 * Will look for the language(s) of the given Gemeinde and return them as a list.
	 * If the Gemeinde has no Stammdaten an Exception will be thrown because this shows a real problem in the data
	 * If the Gemeinde has no language configured it returns DEUTSCH as default language
	 */
	@Nonnull
	public static List<Sprache> extractGemeindeSprachen(@Nonnull Gemeinde gemeinde,
		@Nonnull GemeindeService gemeindeService) {
		final String gemeindeId = gemeinde.getId();
		final GemeindeStammdaten gemeindeStammdatenOpt = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"extractGemeindeSprachen",
				ErrorCodeEnum.ERROR_NO_GEMEINDE_STAMMDATEN,
				gemeindeId)
			);

		final Sprache[] gemeindeSprachen = gemeindeStammdatenOpt.getKorrespondenzsprache().getSprache();
		if (gemeindeSprachen.length <= 0) {
			return Collections.singletonList(Sprache.DEUTSCH);
		}
		return Arrays.asList(gemeindeSprachen);
	}

	/**
	 * Will looked for the language(s) of the given Gemeinde and return them as a list.
	 * If the Gemeinde has no Stammdaten an Exception will be thrown because this shows a real problem in the data
	 * If the Gemeinde has no language configured it returns DEUTSCH as default language
	 */
	@Nonnull
	public static List<Sprache> extractGemeindeSprachen(@Nonnull GemeindeStammdaten gemeindeStammdaten) {
		final Sprache[] gemeindeSprachen = gemeindeStammdaten.getKorrespondenzsprache().getSprache();
		if (gemeindeSprachen.length <= 0) {
			return Collections.singletonList(Sprache.DEUTSCH);
		}
		return Arrays.asList(gemeindeSprachen);
	}

	@Nonnull
	public static Boolean toBoolean(@Nullable Boolean aBoolean, boolean booleanIfNull) {
		if (aBoolean == null) {
			return booleanIfNull;
		}
		return aBoolean;
	}

	public static boolean isKorrekturmodusGemeinde(@Nonnull Gesuch gesuch) {
		return Eingangsart.ONLINE == gesuch.getEingangsart() &&
			AntragStatus.getAllFreigegebeneStatus().contains(gesuch.getStatus());
	}

	@Nonnull
	public static String preProcessString(@Nonnull String username) {
		return username.toLowerCase(Locale.GERMAN).trim();
	}

	/**
	 * Von allen Betreuungen der Liste gib den Typ zurueck der Betreuung, die ueber die anderen dominiert.
	 * KITA > TAGESSCHULE > FERINEINSEL
	 */
	@Nonnull
	public static BetreuungsangebotTyp getDominantBetreuungsangebotTyp(List<AbstractPlatz> betreuungenFromGesuch) {
		BetreuungsangebotTyp dominantType = BetreuungsangebotTyp.FERIENINSEL; // less dominant type
		for (AbstractPlatz betreuung : betreuungenFromGesuch) {
			if (betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESSCHULE) {
				dominantType = BetreuungsangebotTyp.TAGESSCHULE;
			}
			if (!betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp().isSchulamt()) {
				dominantType = BetreuungsangebotTyp.KITA;
				break;
			}
		}
		return dominantType;
	}
}
