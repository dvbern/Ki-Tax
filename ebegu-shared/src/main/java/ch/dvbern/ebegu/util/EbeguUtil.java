/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.gemeindeantrag.OeffnungszeitenTagesschuleDTO;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import static java.util.Objects.requireNonNull;

/**
 * Allgemeine Utils fuer EBEGU
 */
public final class EbeguUtil {

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

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

	@Nonnull
	public static <T> Comparator<Collection<? extends T>> collectionComparator(@Nonnull Comparator<T> comparator) {
		return (a, b) -> areComparableCollections(a, b, comparator) ? 0 : 1;
	}

	/**
	 * @return TRUE, iff collection a and collection b are comparable according to the given comparator.
	 */
	public static <T> boolean areComparableCollections(
		@Nonnull Collection<? extends T> a,
		@Nonnull Collection<? extends T> b,
		@Nonnull Comparator<T> comparator) {

		int sizeDifference = a.size() - b.size();

		if (sizeDifference != 0) {
			return false;
		}

		// creating sorted copies, such that we can compare item by item (order matters!)
		List<T> aPos = new ArrayList<>(a);
		aPos.sort(comparator);

		List<T> bPos = new ArrayList<>(b);
		bPos.sort(comparator);

		return IntStream.range(0, aPos.size())
			.allMatch(i -> comparator.compare(aPos.get(i), bPos.get(i)) == 0);
	}

	/**
	 * finanzielle situation ist by default nicht zwingend
	 */
	public static boolean isFinanzielleSituationRequired(@Nonnull Gesuch gesuch) {
		return gesuch.getFamiliensituationContainer() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
			&& BooleanUtils.isFalse(gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.getSozialhilfeBezueger())
			&& BooleanUtils.isTrue(gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.getVerguenstigungGewuenscht());
	}

	public static boolean isFinanzielleSituationIntroducedAndComplete(
		@Nonnull Gesuch gesuch,
		@Nullable WizardStepName wizardStepName) {
		if (gesuch.getGesuchsteller1() == null
			|| gesuch.getGesuchsteller1().getFinanzielleSituationContainer() == null) {
			return false;
		}

		if (wizardStepName == null || wizardStepName.isFinSitWizardStepName()) {
			if (!isFinanzielleSituationVollstaendig(gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA(), gesuch.getFinSitTyp(), gesuch)) {
				return false;
			}
			if (isMandantSpecificFinSitGemeinsam(gesuch)) {
				return true;
			}
			if (gesuch.getGesuchsteller2() != null &&
				(gesuch.getGesuchsteller2().getFinanzielleSituationContainer() == null ||
					(gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
						&& !isFinanzielleSituationVollstaendig(gesuch.getGesuchsteller2()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(), gesuch.getFinSitTyp(), gesuch)))
			) {
				return false;
			}
		}

		if ((wizardStepName == null || wizardStepName.isEKVWizardStepName())) {
			return isEKVIndroducedAndComplete(gesuch);
		}
		return true;
	}

	private static boolean isEKVIndroducedAndComplete(@Nonnull Gesuch gesuch) {
		if (gesuch.getEinkommensverschlechterungInfoContainer() == null) {
			return false;
		}
		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.SCHWYZ) {
			return isEKVSchwyzIntroducedAndComplete(gesuch.getEinkommensverschlechterungInfoContainer(), gesuch);
		}
		if (gesuch.getEinkommensverschlechterungInfoContainer()
			.getEinkommensverschlechterungInfoJA()
			.getEinkommensverschlechterung()) {
			if (gesuch.getEinkommensverschlechterungInfoContainer()
				.getEinkommensverschlechterungInfoJA()
				.getEkvFuerBasisJahrPlus1()) {
				Objects.requireNonNull(gesuch.getGesuchsteller1());
				if (gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer() == null) {
					return false;
				}
				if (isEKVFuerBasisJahrPlus1Incomplete(gesuch)) {
					return false;
				}
			}
			if (gesuch.getEinkommensverschlechterungInfoContainer()
				.getEinkommensverschlechterungInfoJA()
				.getEkvFuerBasisJahrPlus2()) {

				Objects.requireNonNull(gesuch.getGesuchsteller1());
				return gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer() != null
					&& isEKVFuerBasisJahrPlus2Complete(gesuch);
			}
		}
		// EKV is not activated
		return true;
	}

	private static boolean isEKVFuerBasisJahrPlus2Complete(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
		return isEKVFuerJahrComplete(
			gesuch,
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2(),
			gesuch.getGesuchsteller2() != null
				&& gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer() != null ?
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2() :
				null);
	}

	private static boolean isEKVFuerBasisJahrPlus1Incomplete(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
		return !isEKVFuerJahrComplete(
			gesuch,
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1(),
			gesuch.getGesuchsteller2() != null
				&& gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer() != null ?
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1() :
				null);
	}

	private static boolean isEKVSchwyzIntroducedAndComplete(
		EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer,
		Gesuch gesuch) {
		boolean hasEKV = einkommensverschlechterungInfoContainer.getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1();

		if (!hasEKV) {
			return true;
		}

		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);

		if (familiensituation.getGesuchstellerKardinalitaet() == EnumGesuchstellerKardinalitaet.ALLEINE) {
			return isEKVSchwyzGSVollstaendig(gesuch.getGesuchsteller1());
		}

		if (Boolean.TRUE.equals(familiensituation.getGemeinsameSteuererklaerung())) {
			return isEKVSchwyzGSVollstaendig(gesuch.getGesuchsteller1());
		}

		return isEKVSchwyzGSVollstaendig(gesuch.getGesuchsteller1()) && isEKVSchwyzGSVollstaendig(gesuch.getGesuchsteller2());

	}

	private static boolean isEKVSchwyzGSVollstaendig(@Nullable GesuchstellerContainer gesuchstellerContainer) {
		if (gesuchstellerContainer == null) {
			return false;
		}
		var ekvGS1 = gesuchstellerContainer.getEinkommensverschlechterungContainer();
		if (ekvGS1 == null || ekvGS1.getEkvJABasisJahrPlus1() == null) {
			return false;
		}
		return isEKVSchwyzVollstaendig(ekvGS1.getEkvJABasisJahrPlus1());
	}

	private static boolean isEKVFuerJahrComplete(
		Gesuch gesuch, @Nullable Einkommensverschlechterung einkommensverschlechterungGS1,
		@Nullable Einkommensverschlechterung einkommensverschlechterungGS2) {
		if (einkommensverschlechterungGS1 == null || !isEinkommensverschlechterungVollstaendig(einkommensverschlechterungGS1, gesuch.getFinSitTyp(), gesuch)) {
			return false;
		}
		if (isMandantSpecificFinSitGemeinsam(gesuch)) {
			return true;
		}
		if (einkommensverschlechterungGS2 != null
			&& !isEinkommensverschlechterungVollstaendig(einkommensverschlechterungGS2, gesuch.getFinSitTyp(), gesuch)
		) {
			return false;
		}
		return true;
	}

	private static boolean isMandantSpecificFinSitGemeinsam(@Nonnull Gesuch gesuch) {
		switch (gesuch.getFinSitTyp()) {
		case LUZERN:
			return requireNonNull(requireNonNull(gesuch.getFamiliensituationContainer()).getFamiliensituationJA()).getFamilienstatus()
				== EnumFamilienstatus.VERHEIRATET;
		case APPENZELL:
		case APPENZELL_FOLGEMONAT:
		case SCHWYZ:
			return Boolean.TRUE.equals(requireNonNull(requireNonNull(gesuch.getFamiliensituationContainer()).getFamiliensituationJA())
				.getGemeinsameSteuererklaerung());
		default:
			return false;
		}
	}

	private static boolean isFinanzielleSituationVollstaendig(
		@Nullable FinanzielleSituation finanzielleSituation,
		FinanzielleSituationTyp finSitTyp,
		@Nonnull Gesuch gesuch) {
		if (finanzielleSituation == null) {
			return false;
		}
		boolean valid = false;
		// Solothurn hat nur einen Teil von seiner Feldern in der Abstract Methode so wir muessen
		if (finSitTyp.equals(FinanzielleSituationTyp.SOLOTHURN)) {
			valid =
				(finanzielleSituation.getBruttoLohn() != null && finanzielleSituation.getSteuerbaresVermoegen() != null)
					|| (finanzielleSituation.getNettolohn() != null
					&& finanzielleSituation.getUnterhaltsBeitraege() != null
					&& finanzielleSituation.getAbzuegeKinderAusbildung() != null
					&& finanzielleSituation.getSteuerbaresVermoegen() != null);
		} else if (finSitTyp.equals(FinanzielleSituationTyp.APPENZELL) || finSitTyp.equals(FinanzielleSituationTyp.APPENZELL_FOLGEMONAT)) {
			valid = isFinSitAppenzellVollstaendig(finanzielleSituation);
		} else if (finSitTyp == FinanzielleSituationTyp.SCHWYZ) {
			valid = isFinSitSchwyzVollstaendig(finanzielleSituation);
		} else {
			valid = isAbstractFinanzielleSituationVollstaendig(finanzielleSituation, finSitTyp, gesuch);
		}

		if (!valid) {
			return false;
		}
		// Zwingend ist nur das erste Jahr, FALLS ueberhaupt eines ausgefuellt wird.
		// Das einzige, das wir validieren koennen, ist das Jahr+1 bzw. Jahr+2 nicht ausgefuellt sein duerfen, falls
		// Basisjahr null
		if (finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1() != null
			|| finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2() != null) {
			// Basisjahr ist zwingend
			return finanzielleSituation.getGeschaeftsgewinnBasisjahr() != null;
		}
		return true;
	}

	private static boolean isFinSitAppenzellVollstaendig(FinanzielleSituation finanzielleSituation) {
		if (finanzielleSituation.getFinSitZusatzangabenAppenzell() == null) {
			return false;
		}
		return finanzielleSituation.getFinSitZusatzangabenAppenzell().isVollstaendig()
			&& finanzielleSituation.getSteuerbaresEinkommen() != null
			&& finanzielleSituation.getSteuerbaresVermoegen() != null;
	}

	public static boolean isFinSitSchwyzVollstaendig(FinanzielleSituation finanzielleSituation) {
		return finanzielleSituation.getBruttoLohn() != null ||
			(finanzielleSituation.getSteuerbaresEinkommen() != null
				&& finanzielleSituation.getEinkaeufeVorsorge() != null
				&& finanzielleSituation.getAbzuegeLiegenschaft() != null
				&& finanzielleSituation.getSteuerbaresVermoegen() != null);
	}

	public static boolean isEKVSchwyzVollstaendig(Einkommensverschlechterung ekv) {
		return ekv.getBruttoLohn() != null ||
			(ekv.getSteuerbaresEinkommen() != null
				&& ekv.getEinkaeufeVorsorge() != null
				&& ekv.getAbzuegeLiegenschaft() != null
				&& ekv.getSteuerbaresVermoegen() != null);
	}

	private static boolean isEinkommensverschlechterungVollstaendig(
		@Nonnull Einkommensverschlechterung einkommensverschlechterung,
		FinanzielleSituationTyp finSitTyp,
		@Nonnull Gesuch gesuch) {
		if (finSitTyp.equals(FinanzielleSituationTyp.SOLOTHURN)) {
			return einkommensverschlechterung.getBruttolohnAbrechnung1() != null
				&& einkommensverschlechterung.getBruttolohnAbrechnung2() != null
				&& einkommensverschlechterung.getBruttolohnAbrechnung3() != null
				&& einkommensverschlechterung.getExtraLohn() != null
				&& einkommensverschlechterung.getNettoVermoegen() != null;
		}

		return isAbstractFinanzielleSituationVollstaendig(einkommensverschlechterung, finSitTyp, gesuch);
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod") // FalsePositive: Die Methode ist benutzt
	private static boolean isAbstractFinanzielleSituationVollstaendig(
		@Nonnull AbstractFinanzielleSituation finanzielleSituation,
		FinanzielleSituationTyp finSitTyp,
		@Nonnull Gesuch gesuch) {
		if (finSitTyp.equals(FinanzielleSituationTyp.LUZERN)) {
			return isInfomaZahlungenVollstaendig(gesuch) && finanzielleSituation.isVollstaendig(finSitTyp);
		}
		return finanzielleSituation.isVollstaendig(finSitTyp);
	}

	private static boolean isInfomaZahlungenVollstaendig(Gesuch gesuch) {
		boolean valid = true;
		if (gesuch.getStatus().isReadableByJugendamtSchulamtSteueramt()) {
			valid = gesuch.getFamiliensituationContainer() != null
				&& gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
				&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten() != null
				&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().getInfomaBankcode()
				!= null
				&& gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getAuszahlungsdaten()
				.getInfomaKreditorennummer() != null;
		}
		return valid && gesuch.getFamiliensituationContainer() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten().getIban() != null;
	}

	public static boolean isFamilienSituationVollstaendig(@Nonnull Gesuch gesuch) {
		if (gesuch.getFamiliensituationContainer() == null) {
			return false;
		}
		var famSitJA = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
		if (famSitJA == null) {
			return false;
		}
		if (gesuch.getFamiliensituationContainer().getFamiliensituationJA().getFamilienstatus() == EnumFamilienstatus.APPENZELL) {
			return isFamilienSituationVollstaendigAppenzell(famSitJA);
		}
		if (gesuch.getFamiliensituationContainer().getFamiliensituationJA().getFamilienstatus() == EnumFamilienstatus.SCHWYZ) {
			return famSitJA.getGesuchstellerKardinalitaet() != null;
		}
		return (gesuch.getFamiliensituationContainer().getFamiliensituationJA().getVerguenstigungGewuenscht() != null
			|| BooleanUtils.isTrue(gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.getSozialhilfeBezueger()))
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger() != null;
	}

	public static boolean isFamilienSituationVollstaendigAppenzell(@Nonnull Familiensituation familiensituation) {
		if (BooleanUtils.isFalse(familiensituation.getGeteilteObhut())) {
			return familiensituation.getGemeinsamerHaushaltMitPartner() != null;
		}
		if (BooleanUtils.isTrue(familiensituation.getGeteilteObhut())) {
			if (BooleanUtils.isTrue(familiensituation.getGemeinsamerHaushaltMitObhutsberechtigterPerson())) {
				return true;
			} else if (BooleanUtils.isFalse(familiensituation.getGemeinsamerHaushaltMitObhutsberechtigterPerson())) {
				return familiensituation.getGemeinsamerHaushaltMitPartner() != null;
			}
		}
		return false;
	}

	public static boolean isErlaeuterungenZurVerfuegungRequired(@Nonnull Gesuch gesuch) {
		// Nicht beim Schwyz
		if(gesuch.getDossier().getFall().getMandant().getMandantIdentifier() == MandantIdentifier.SCHWYZ) {
			return false;
		}
		// Im Status ENTWURF sollen die Erläuterungen immer als Beilage aufgeführt werden
		if (!gesuch.getStatus().isAnyStatusOfVerfuegt()) {
			return true;
		}
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			// Status VERFUEGT mit Anspruch
			if (Betreuungsstatus.VERFUEGT == betreuung.getBetreuungsstatus() && (betreuung.hasAnspruch() ||
				gesuch.getDossier().getFall().getMandant().getMandantIdentifier() == MandantIdentifier.APPENZELL_AUSSERRHODEN)) {
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
	public static Sprache extractKorrespondenzsprache(
		@Nonnull Gesuch gesuch,
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
	public static Sprache extractKorrespondenzsprache(
		@Nonnull Gesuch gesuch,
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
	private static List<Sprache> extractGemeindeSprachenFromGesuch(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeService gemeindeService) {
		return extractGemeindeSprachen(gesuch.getDossier().getGemeinde(), gemeindeService);
	}

	/**
	 * Will look for the language(s) of the given Gemeinde and return them as a list.
	 * If the Gemeinde has no Stammdaten an Exception will be thrown because this shows a real problem in the data
	 * If the Gemeinde has no language configured it returns DEUTSCH as default language
	 */
	@Nonnull
	public static List<Sprache> extractGemeindeSprachen(
		@Nonnull Gemeinde gemeinde,
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

	public static String toFilename(@Nonnull String filename) {
		// Filename darf keine / enthalten
		filename = StringUtils.replace(filename, "/", "_");
		filename = StringUtils.replace(filename, "\\", "_");
		filename = StringUtils.replace(filename, " ", "_");
		return filename;
	}

	@Nonnull
	public static String removeWhiteSpaces(@Nonnull String str) {
		return WHITESPACE.matcher(str).replaceAll("");
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

	/**
	 * @return initial param if it is not null, otherwise returns default value.
	 */
	@Nonnull
	public static <T> T coalesce(@Nullable T initial, @Nonnull T defaultValue) {
		if (initial != null) {
			return initial;
		}

		return defaultValue;
	}

	public static String getUserMandantString(PrincipalBean principalBean) {
		String benutzerId = principalBean.getPrincipal().getName();
		if (Objects.equals(benutzerId, Constants.ANONYMOUS_USER_USERNAME) ||
			Objects.equals(benutzerId, Constants.LOGINCONNECTOR_USER_USERNAME)) {
			return benutzerId;
		}
		return principalBean.getBenutzer().getUsername() + ":" + principalBean.getBenutzer().getMandant().getName();
	}

	@Nonnull
	public static OeffnungszeitenTagesschuleDTO[] convertOeffnungszeiten(@Nonnull String oeffnungszeiten)
		throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		return mapper.readValue(oeffnungszeiten, OeffnungszeitenTagesschuleDTO[].class);
	}
}
