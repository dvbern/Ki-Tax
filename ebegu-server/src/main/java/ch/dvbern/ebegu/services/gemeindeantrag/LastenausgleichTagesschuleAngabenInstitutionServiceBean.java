/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.BelegungTagesschule_;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer_;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer_;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;

/**
 * Service fuer den Lastenausgleich der Tagesschulen, Formulare der Institutionen
 */
@Stateless
@Local(LastenausgleichTagesschuleAngabenInstitutionService.class)
public class LastenausgleichTagesschuleAngabenInstitutionServiceBean extends AbstractBaseService
	implements LastenausgleichTagesschuleAngabenInstitutionService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private EinstellungService einstellungService;

	@Override
	public void createLastenausgleichTagesschuleInstitution(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer gemeindeContainer
	) {
		Objects.requireNonNull(gemeindeContainer);

		final Collection<InstitutionStammdaten> institutionStammdatenList =
			institutionStammdatenService.getAllTagesschulenForGesuchsperiodeAndGemeinde(
				gemeindeContainer.getGesuchsperiode(),
				gemeindeContainer.getGemeinde());
		for (InstitutionStammdaten institutionStammdaten : institutionStammdatenList) {
			LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer =
				new LastenausgleichTagesschuleAngabenInstitutionContainer();
			institutionContainer.setInstitution(institutionStammdaten.getInstitution());
			institutionContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);
			institutionContainer.setAngabenKorrektur(null); // Wird bei Freigabe rueber kopiert
			institutionContainer.setAngabenDeklaration(new LastenausgleichTagesschuleAngabenInstitution());
			institutionContainer.setAngabenGemeinde(gemeindeContainer);

			final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
				saveLastenausgleichTagesschuleInstitution(institutionContainer);

			gemeindeContainer.addLastenausgleichTagesschuleAngabenInstitutionContainer(saved);
		}
	}

	@Nonnull
	@Override
	public Optional<LastenausgleichTagesschuleAngabenInstitutionContainer> findLastenausgleichTagesschuleAngabenInstitutionContainer(
		@Nonnull String id
	) {
		Objects.requireNonNull(id, "id muss gesetzt sein");

		LastenausgleichTagesschuleAngabenInstitutionContainer container =
			persistence.find(LastenausgleichTagesschuleAngabenInstitutionContainer.class, id);
		return Optional.ofNullable(container);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer saveLastenausgleichTagesschuleInstitution(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer
	) {
		Objects.requireNonNull(institutionContainer);
		authorizer.checkWriteAuthorization(institutionContainer);

		return persistence.merge(institutionContainer);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer
	) {
		Objects.requireNonNull(institutionContainer);
		authorizer.checkWriteAuthorization(institutionContainer);

		// Nur moeglich, wenn noch nicht freigegeben
		Preconditions.checkState(
			institutionContainer.getStatus() == LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN,
			"LastenausgleichAngabenInstitution muss im Status OFFEN sein");

		Objects.requireNonNull(institutionContainer.getAngabenDeklaration());
		checkInstitutionAngabenComplete(
			institutionContainer.getAngabenDeklaration(),
			institutionContainer.getAngabenGemeinde().getAlleAngabenInKibonErfasst());

		institutionContainer.copyForFreigabe();
		institutionContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.IN_PRUEFUNG_GEMEINDE);
		return persistence.merge(institutionContainer);
	}

	@Nonnull
	@Override
	public LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionGeprueft(
		@Nonnull
			LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer) {
		Objects.requireNonNull(institutionContainer);
		authorizer.checkWriteAuthorization(institutionContainer);

		// Nur moeglich, wenn freigegeben, aber noch nicht geprüft
		Preconditions.checkState(
			institutionContainer.getStatus() == LastenausgleichTagesschuleAngabenInstitutionStatus.IN_PRUEFUNG_GEMEINDE,
			"LastenausgleichAngabenInstitution muss im Status OFFEN sein");

		Objects.requireNonNull(institutionContainer.getAngabenKorrektur());
		checkInstitutionAngabenComplete(
			institutionContainer.getAngabenKorrektur(),
			institutionContainer.getAngabenGemeinde().getAlleAngabenInKibonErfasst());

		institutionContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT);
		return persistence.merge(institutionContainer);

	}

	@Override
	public List<LastenausgleichTagesschuleAngabenInstitutionContainer> findLastenausgleichTagesschuleAngabenInstitutionByGemeindeAntragId(
		String gemeindeAntragId) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichTagesschuleAngabenInstitutionContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenInstitutionContainer.class);
		Root<LastenausgleichTagesschuleAngabenInstitutionContainer> root =
			query.from(LastenausgleichTagesschuleAngabenInstitutionContainer.class);

		Predicate gemeindeAntrag =
			cb.equal(root.get(LastenausgleichTagesschuleAngabenInstitutionContainer_.angabenGemeinde).get(
				LastenausgleichTagesschuleAngabenGemeindeContainer_.id), gemeindeAntragId);

		query.where(gemeindeAntrag);

		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitutionContainerWiederOeffnenGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer fallContainer) {

		authorizer.checkWriteAuthorization(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer.getAngabenGemeinde());

		Preconditions.checkState(
			fallContainer.getAngabenGemeinde().isInBearbeitungGemeinde(),
			"LastenausgleichTagesschuleAngabenGemeindeContainer muss in Bearbeitung Gemeinde sein"
		);

		Preconditions.checkState(
			fallContainer.getAngabenGemeinde().getAngabenDeklaration() != null,
			"LastenausgleichTagesschuleAngabenGemeindeContainer muss in Bearbeitung Gemeinde sein"
		);

		Preconditions.checkState(
			fallContainer.isAntragAbgeschlossen(),
			"LastenausgleichTagesschuleAngabenInstitutionContainer muss im Status GEPRUEFT sein");

		// gemeinde angaben have to be reopened if closed
		if (fallContainer.getAngabenGemeinde().getAngabenDeklaration().isAbgeschlossen()) {
			fallContainer.getAngabenGemeinde().getAngabenDeklaration().setStatus(
				LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);
		}

		fallContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.IN_PRUEFUNG_GEMEINDE);

		return persistence.persist(fallContainer);

	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitutionContainerWiederOeffnenTS(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer fallContainer) {
		authorizer.checkWriteAuthorization(fallContainer);

		Preconditions.checkState(
			fallContainer.getAngabenGemeinde().isInBearbeitungGemeinde(),
			"LastenausgleichTagesschuleAngabenGemeindeContainer muss in Bearbeitung Gemeinde sein"
		);

		Preconditions.checkState(
			fallContainer.isAntragInPruefungGemeinde(),
			"LastenausgleichTagesschuleAngabenInstitutionContainer muss im Status IN_PRUEFUNG_GEMEINDE sein");

		fallContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);

		return persistence.persist(fallContainer);
	}

	@Override
	public @Nonnull
	Map<String, Integer> calculateAnzahlEingeschriebeneKinder(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer container
	) {
		Preconditions.checkNotNull(container);

		authorizer.checkReadAuthorization(container);

		InstitutionStammdaten stammdaten = institutionStammdatenService.fetchInstitutionStammdatenByInstitution(
			container.getInstitution().getId(), false
		);
		if (stammdaten == null) {
			throw new EbeguEntityNotFoundException("calculateAngabenFromKiBon", container.getInstitution().getId());
		}

		List<AnmeldungTagesschule> anmeldungenTagesschule =
			findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriode(
				stammdaten, container.getGesuchsperiode()
			);

		return countAnzahlKinder(anmeldungenTagesschule);
	}

	@Nonnull
	Map<String, Integer> countAnzahlKinder(@Nonnull List<AnmeldungTagesschule> anmeldungenTagesschule) {
		int countVorschulalter = 0;
		int countKindergarten = 0;
		int countPrimarstufe = 0;
		int countSekundarstufe = 0;

		for (AnmeldungTagesschule anmeldungTagesschule : anmeldungenTagesschule) {
			EinschulungTyp einschulungTyp = anmeldungTagesschule.getKind().getKindJA().getEinschulungTyp();
			if (einschulungTyp == null) {
				continue;
			}
			if (!einschulungTyp.isEingeschult()) {
				countVorschulalter++;
			} else if (einschulungTyp.isKindergarten()) {
				countKindergarten++;
			} else if (einschulungTyp.isPrimarstufe()) {
				countPrimarstufe++;
			} else if (einschulungTyp.isSekundarstufe()) {
				countSekundarstufe++;
			}
		}

		HashMap<String, Integer> anzahlKinder = new HashMap<>();
		anzahlKinder.put("overall", countVorschulalter + countKindergarten + countPrimarstufe + countSekundarstufe);
		anzahlKinder.put("vorschulalter", countVorschulalter);
		anzahlKinder.put("kindergarten", countKindergarten);
		anzahlKinder.put("primarstufe", countPrimarstufe);
		anzahlKinder.put("sekundarstufe", countSekundarstufe);

		return anzahlKinder;
	}

	@Override
	@Nonnull
	public Map<String, BigDecimal> calculateDurchschnittKinderProTag(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer container
	) {
		Preconditions.checkNotNull(container);

		authorizer.checkReadAuthorization(container);

		InstitutionStammdaten stammdaten = institutionStammdatenService.fetchInstitutionStammdatenByInstitution(
			container.getInstitution().getId(), false
		);
		if (stammdaten == null) {
			throw new EbeguEntityNotFoundException(
				"calculateDurchschnittKinderProTag",
				container.getInstitution().getId());
		}

		List<AnmeldungTagesschule> anmeldungenTagesschule =
			findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriode(
				stammdaten, container.getGesuchsperiode()
			);

		return calculateDurchschnittKinderProTag(anmeldungenTagesschule);

	}

	@Nonnull
	protected Map<String, BigDecimal> calculateDurchschnittKinderProTag(List<AnmeldungTagesschule> anmeldungenTagesschule) {
		double fruehbetreuung = 0;
		double mittagbetreuung = 0;
		double nachmittagbetreuung1 = 0;
		double nachmittagbetreuung2 = 0;

		Map<DayOfWeek, Boolean> fruehbetreuungWeekdaysWithBetreuung = new EnumMap<>(DayOfWeek.class);
		Map<DayOfWeek, Boolean> mittagbetreuungWeekdaysWithBetreuung = new EnumMap<>(DayOfWeek.class);
		Map<DayOfWeek, Boolean> nachmittagbetreuung1WeekdaysWithBetreuung = new EnumMap<>(DayOfWeek.class);
		Map<DayOfWeek, Boolean> nachmittagbetreuung2WeekdaysWithBetreuung = new EnumMap<>(DayOfWeek.class);

		for (AnmeldungTagesschule anmeldungTagesschule : anmeldungenTagesschule) {
			BelegungTagesschule belegungTagesschule = anmeldungTagesschule.getBelegungTagesschule();
			if (belegungTagesschule == null) {
				continue;
			}
			for (BelegungTagesschuleModul modul : belegungTagesschule.getBelegungTagesschuleModule()) {
				ModulTagesschule modulTagesschule = modul.getModulTagesschule();
				ModulTagesschuleGroup group = modulTagesschule.getModulTagesschuleGroup();
				// we count Zweiwöchentliche Module as 0.5
				double increment = (modul.getIntervall() == BelegungTagesschuleModulIntervall.WOECHENTLICH) ? 1 : 0.5;
				if (group.isFruehbetreuung()) {
					fruehbetreuung += increment;
					fruehbetreuungWeekdaysWithBetreuung.put(modulTagesschule.getWochentag(), true);
				} else if (group.isMittagsbetreuung()) {
					mittagbetreuung += increment;
					mittagbetreuungWeekdaysWithBetreuung.put(modulTagesschule.getWochentag(), true);
				} else if (group.isNachmittagbetreuung1()) {
					nachmittagbetreuung1 += increment;
					nachmittagbetreuung1WeekdaysWithBetreuung.put(modulTagesschule.getWochentag(), true);
				} else if (group.isNachmittagbetreuung2()) {
					nachmittagbetreuung2 += increment;
					nachmittagbetreuung2WeekdaysWithBetreuung.put(modulTagesschule.getWochentag(), true);
				}
			}
		}

		HashMap<String, BigDecimal> durchschnittKinder = new HashMap<>();
		durchschnittKinder.put(
			"fruehbetreuung",
			this.divideByWeekdaysWithBetreuung(fruehbetreuung, fruehbetreuungWeekdaysWithBetreuung));
		durchschnittKinder.put(
			"mittagsbetreuung",
			this.divideByWeekdaysWithBetreuung(mittagbetreuung, mittagbetreuungWeekdaysWithBetreuung));
		durchschnittKinder.put(
			"nachmittagsbetreuung1",
			this.divideByWeekdaysWithBetreuung(nachmittagbetreuung1, nachmittagbetreuung1WeekdaysWithBetreuung));
		durchschnittKinder.put(
			"nachmittagsbetreuung2",
			this.divideByWeekdaysWithBetreuung(nachmittagbetreuung2, nachmittagbetreuung2WeekdaysWithBetreuung));

		return durchschnittKinder;
	}

	private BigDecimal divideByWeekdaysWithBetreuung(double number, Map<DayOfWeek, Boolean> weekdaysWithBetreuung) {
		BigDecimal dividend = new BigDecimal(String.valueOf(number));
		BigDecimal divisor = new BigDecimal(weekdaysWithBetreuung.size());
		return divisor.compareTo(BigDecimal.ZERO) == 0 ?
			BigDecimal.ZERO :
			MathUtil.ZWEI_NACHKOMMASTELLE.divide(dividend, divisor);
	}

	@Nonnull
	private List<AnmeldungTagesschule> findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriode(
		@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {

		List<Einstellung> einstellungList =
			einstellungService.findEinstellungen(EinstellungKey.LATS_STICHTAG, gesuchsperiode);
		if (einstellungList.size() != 1) {
			throw new EbeguRuntimeException(
				"findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriode",
				"Es sollte exakt eine Einstellung für den LATS_Stichtag und die Gesuchsperiode "
					+ gesuchsperiode.getGesuchsperiodeString()
					+ " gefunden werden");
		}
		LocalDate stichtag = Date.valueOf(einstellungList.get(0).getValue()).toLocalDate();
		Gesuchsperiode gesuchsperiodeAmStichtag = gesuchsperiodeService.getGesuchsperiodeAm(stichtag)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriode",
				"Keine Gesuchsperiode für Stichtag " + stichtag.toString() + " gefunden"
			));

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<AnmeldungTagesschule> query = cb.createQuery(AnmeldungTagesschule.class);
		Root<AnmeldungTagesschule> root = query.from(AnmeldungTagesschule.class);

		Join<AnmeldungTagesschule, KindContainer> joinKindContainer = root.join(AnmeldungTagesschule_.kind);
		Join<AnmeldungTagesschule, BelegungTagesschule> joinBelegungTagesschule =
			root.join(AnmeldungTagesschule_.belegungTagesschule);

		final Predicate predicateGesuch = cb.equal(
			joinKindContainer.get(KindContainer_.gesuch)
				.get(Gesuch_.gesuchsperiode)
				.get(Gesuchsperiode_.id),
			gesuchsperiodeAmStichtag.getId()
		);
		final Predicate predicateStammdaten = cb.equal(
			root.get(
				AnmeldungTagesschule_.institutionStammdaten).get(InstitutionStammdaten_.id),
			stammdaten.getId()
		);
		final Predicate predicateGueltig = cb.equal(root.get(AnmeldungTagesschule_.gueltig), Boolean.TRUE);
		final Predicate predicateEingeschrieben = cb.lessThanOrEqualTo(
			joinBelegungTagesschule.get(BelegungTagesschule_.eintrittsdatum),
			stichtag
		);
		final Predicate predicateUebernommen = cb.equal(
			root.get(AnmeldungTagesschule_.betreuungsstatus),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN
		);

		query.where(
			predicateGesuch,
			predicateStammdaten,
			predicateGueltig,
			predicateEingeschrieben,
			predicateUebernommen);

		return persistence.getCriteriaResults(query);

	}

	// we check this since the attributes can be cached and can be null then, but must not be when changing status
	private void checkInstitutionAngabenComplete(
		LastenausgleichTagesschuleAngabenInstitution institutionAngaben,
		Boolean alleAngabenInKibonErfasst) {
		if (Objects.isNull(institutionAngaben.getLehrbetrieb())) {
			throw new WebApplicationException("isLehrbetrieb must not be null", Status.BAD_REQUEST);
		}
		if (!alleAngabenInKibonErfasst) {
			if (Objects.isNull(institutionAngaben.getAnzahlEingeschriebeneKinder())) {
				throw new WebApplicationException("anzahlEingeschribeneKinder must not be null", Status.BAD_REQUEST);
			}
			if (Objects.isNull(institutionAngaben.getAnzahlEingeschriebeneKinderSekundarstufe())) {
				throw new WebApplicationException(
					"anzahlEingeschriebeneKinderBasisstufe must not be null",
					Status.BAD_REQUEST);
			}
			if (Objects.isNull(institutionAngaben.getAnzahlEingeschriebeneKinderKindergarten())) {
				throw new WebApplicationException(
					"anzahlEingeschriebeneKinderKindergarten must not be null",
					Status.BAD_REQUEST);
			}
			if (Objects.isNull(institutionAngaben.getAnzahlEingeschriebeneKinderPrimarstufe())) {
				throw new WebApplicationException(
					"anzahlEingeschriebeneKinderPrimarstufe must not be null",
					Status.BAD_REQUEST);
			}
			if (Objects.isNull(institutionAngaben.getDurchschnittKinderProTagFruehbetreuung())) {
				throw new WebApplicationException(
					"anzahlDurchschnittKinderProTagFruehbetreuung must not be null",
					Status.BAD_REQUEST);
			}
			if (Objects.isNull(institutionAngaben.getDurchschnittKinderProTagMittag())) {
				throw new WebApplicationException(
					"anzahlDurchschnittKinderProTagMittag must not be null",
					Status.BAD_REQUEST);
			}
			;
			if (Objects.isNull(institutionAngaben.getDurchschnittKinderProTagNachmittag1())) {
				throw new WebApplicationException(
					"anzahlDurchschnittKinderProTagNachmittag1 must not be null",
					Status.BAD_REQUEST);
			}
			if (Objects.isNull(institutionAngaben.getDurchschnittKinderProTagNachmittag2())) {
				throw new WebApplicationException(
					"anzahlDurchschnittKinderProTagNachmittag2 must not be null",
					Status.BAD_REQUEST);
			}
		}
		if (Objects.isNull(institutionAngaben.getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen())) {
			throw new WebApplicationException(
				"anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen must not be null",
				Status.BAD_REQUEST);
		}
		if (Objects.isNull(institutionAngaben.getBetreuungsverhaeltnisEingehalten())) {
			throw new WebApplicationException("betreuungsverhaeltnisEingehalten must not be null", Status.BAD_REQUEST);
		}
		if (Objects.isNull(institutionAngaben.getErnaehrungsGrundsaetzeEingehalten())) {
			throw new WebApplicationException(
				"ernaehrungsGrundsaetzeEingehalten must not be null",
				Status.BAD_REQUEST);
		}
		if (Objects.isNull(institutionAngaben.getSchuleAufBasisOrganisatorischesKonzept())) {
			throw new WebApplicationException(
				"schuleAufBasisOrganisatorischesKonzepts must not be null",
				Status.BAD_REQUEST);
		}
		if (Objects.isNull(institutionAngaben.getRaeumlicheVoraussetzungenEingehalten())) {
			throw new WebApplicationException(
				"raeumlicheVoraussetungenEingehalten must not be null",
				Status.BAD_REQUEST);
		}
		if (Objects.isNull(institutionAngaben.getSchuleAufBasisPaedagogischesKonzept())) {
			throw new WebApplicationException(
				"schuleAufBasisPaedagogischesKonzepts must not be null",
				Status.BAD_REQUEST);
		}
	}
}


