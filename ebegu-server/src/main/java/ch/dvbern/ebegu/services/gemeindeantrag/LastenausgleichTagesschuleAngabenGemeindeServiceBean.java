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
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer_;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistory;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer_;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.util.PredicateHelper;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
@Stateless
@Local(LastenausgleichTagesschuleAngabenGemeindeService.class)
public class LastenausgleichTagesschuleAngabenGemeindeServiceBean extends AbstractBaseService
	implements LastenausgleichTagesschuleAngabenGemeindeService {

	private static final String ANGABEN_KORREKTUR_NOT_NULL = "LastenausgleichTagesschuleAngabenGemeindeContainer angabenKorrektur must not be null";
	private static final String ANGABEN_DEKLARATION_NOT_NULL = "LastenausgleichTagesschuleAngabenGemeindeContainer angabenDeklaration must not be null";

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principal;

	@Inject
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService historyService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private EbeguConfiguration configuration;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private MailService mailService;

	private static final Logger LOG =
		LoggerFactory.getLogger(LastenausgleichTagesschuleAngabenGemeindeServiceBean.class);

	@Override
	@Nonnull
	public List<? extends GemeindeAntrag> createLastenausgleichTagesschuleGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		Objects.requireNonNull(gesuchsperiode);

		List<GemeindeAntrag> result = new ArrayList<>();
		final Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden();
		final Collection<Gemeinde> tsGemeinden = aktiveGemeinden.stream()
			.filter(gemeinde -> gemeinde.isTagesschuleActiveForGesuchsperiode(gesuchsperiode))
			.collect(Collectors.toList());

		for (Gemeinde gemeinde : tsGemeinden) {
			Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> existingOptional =
				findLastenausgleichTagesschuleAngabenGemeindeContainer(gemeinde, gesuchsperiode);
			if (existingOptional.isPresent()) {
				LOG.info(
					"LastenausgleichTagesschule Gemeinde Angaben existieren für {} und periode {} bereits",
					gemeinde.getName(),
					gesuchsperiode.getGesuchsperiodeString());
				continue;
			}
			LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer =
				new LastenausgleichTagesschuleAngabenGemeindeContainer();
			fallContainer.setGesuchsperiode(gesuchsperiode);
			fallContainer.setGemeinde(gemeinde);
			fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.NEU);
			fallContainer.setAngabenKorrektur(null);    // Wird bei Freigabe rueberkopiert
			LastenausgleichTagesschuleAngabenGemeinde angabenDeklaration =
				new LastenausgleichTagesschuleAngabenGemeinde();
			angabenDeklaration.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);
			fallContainer.setAngabenDeklaration(angabenDeklaration);
			final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
			angabenInstitutionService.createLastenausgleichTagesschuleInstitution(saved);
			LOG.info(
				"LastenausgleichTagesschule Gemeinde Angaben für {} und periode {} erstellt",
				gemeinde.getName(),
				gesuchsperiode.getGesuchsperiodeString());
			result.add(saved);
		}
		return result;
	}

	@Nonnull
	@Override
	public Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull String id
	) {
		Objects.requireNonNull(id, "id muss gesetzt sein");

		LastenausgleichTagesschuleAngabenGemeindeContainer container =
			persistence.find(LastenausgleichTagesschuleAngabenGemeindeContainer.class, id);
		return Optional.ofNullable(container);
	}

	@Nonnull
	@Override
	public Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		Objects.requireNonNull(gemeinde, "gemeinde muss gesetzt sein");
		Objects.requireNonNull(gesuchsperiode, "gesuchsperiode muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		Predicate gemeindePredicate =
			cb.equal(root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde), gemeinde);
		Predicate gesuchsperiodePredicate =
			cb.equal(root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gesuchsperiode), gesuchsperiode);

		query.where(cb.and(gemeindePredicate, gesuchsperiodePredicate));
		return Optional.ofNullable(persistence.getCriteriaSingleResult(query));
	}

	@Nonnull
	@Override
	public void deleteLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {

		if (!configuration.getIsDevmode()) {
			throw new EbeguRuntimeException(
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer",
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer ist nur im Devmode möglich");
		}

		if (!principal.isCallerInRole(UserRole.SUPER_ADMIN)) {
			throw new EbeguRuntimeException(
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer",
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer ist nur als SuperAdmin möglich");
		}

		findLastenausgleichTagesschuleAngabenGemeindeContainer(
			gemeinde,
			gesuchsperiode).ifPresent(container -> {
			deleteHistoryForContainer(container);
			persistence.remove(container);
		});
	}

	private void deleteHistoryForContainer(LastenausgleichTagesschuleAngabenGemeindeContainer container) {
		List<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> historyList =
			historyService.findHistoryForContainer(container);
		historyList.forEach(entry -> persistence.remove(entry));
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, false);
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer,
		boolean saveInStatusHistory
	) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved = persistence.merge(fallContainer);
		if (saveInStatusHistory) {
			historyService.saveLastenausgleichTagesschuleStatusChange(saved);
		}
		return saved;
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		// Nur moeglich, wenn noch OFFEN und die zwingenden Fragen beantwortet
		Preconditions.checkState(
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.NEU,
			"LastenausgleichAngabenGemeindeContainer muss im Status NEU sein");
		Preconditions.checkNotNull(
			fallContainer.getAlleAngabenInKibonErfasst(),
			"Die zwingenden Fragen muessen zu diesem Zeitpunkt beantwortet sein");

		fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE);
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		Preconditions.checkState(
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
			"LastenausgleichAngabenGemeindeContainer muss im Status IN_BEARBEITUNG_GEMEINDE sein");
		Preconditions.checkArgument(
			fallContainer.getAngabenInstitutionContainers()
				.stream()
				.allMatch(LastenausgleichTagesschuleAngabenInstitutionContainer::isAntragAbgeschlossen),
			"Alle LastenausgleichAngabenInstitution muessen abgeschlossen sein");
		Preconditions.checkState(
			fallContainer.getAngabenDeklaration() != null
				&& fallContainer.getAngabenDeklaration().getStatus()
				== LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN,
			"Das LastenausgleichAngabenGemeinde Formular muss abgeschlossen sein"
		);
		Objects.requireNonNull(fallContainer.getAngabenDeklaration());

		Preconditions.checkArgument(
			fallContainer.angabenDeklarationComplete(),
			"angabenDeklaration incomplete"
		);

		fallContainer.copyForFreigabe();
		fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON);
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
	}

	@Nonnull
	@Override
	public List<LastenausgleichTagesschuleAngabenGemeindeContainer> getAllLastenausgleicheTagesschulen() {
		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		if (!principal.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)) {
			Predicate gemeindeIn =
				root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde).in(gemeinden);
			query.where(gemeindeIn);
		}

		return persistence.getCriteriaResults(query);
	}

	@Nullable
	@Override
	public List<LastenausgleichTagesschuleAngabenGemeindeContainer> getLastenausgleicheTagesschulen(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status,
		@Nullable String timestampMutiert) {
		// institution users have much less permissions, so we handle this in on its own
		if (principal.isCallerInAnyOfRole(
			UserRole.ADMIN_INSTITUTION,
			UserRole.SACHBEARBEITER_INSTITUTION,
			UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
			UserRole.ADMIN_TRAEGERSCHAFT)) {
			return getLastenausgleicheTagesschulenForInstitution(periode, status);
		}
		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		Set<Predicate> predicates = new HashSet<>();

		if (!principal.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)) {

			Predicate gemeindeIn =
				root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde).in(gemeinden);
			predicates.add(gemeindeIn);
		}

		if (gemeinde != null) {
			predicates.add(
				cb.equal(
					root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde).get(Gemeinde_.name),
					gemeinde)
			);
		}
		if (periode != null) {
			predicates.add(PredicateHelper.getPredicateFilterGesuchsperiode(cb,
				root.join(LastenausgleichTagesschuleAngabenGemeindeContainer_.gesuchsperiode, JoinType.INNER),
				periode));
		}
		if (status != null) {
			if (!EnumUtil.isOneOf(status, LastenausgleichTagesschuleAngabenGemeindeStatus.values())) {
				return new ArrayList<>();
			}
			final Predicate statusPredicate = createStatusPredicate(status, cb, root);
			predicates.add(
				statusPredicate
			);
		}
		if (timestampMutiert != null) {
			final Predicate timestampMutiertPredicate = createTimestampMutiertPredicate(timestampMutiert, cb, root);
			predicates.add(
				timestampMutiertPredicate
			);
		}

		Predicate[] predicatesArray = new Predicate[predicates.size()];
		query.where(predicates.toArray(predicatesArray));

		List<LastenausgleichTagesschuleAngabenGemeindeContainer> containerList = persistence.getCriteriaResults(query);

		containerList.forEach(lastenausgleichTagesschuleAngabenGemeindeContainer -> {
			authorizer.checkReadAuthorization(lastenausgleichTagesschuleAngabenGemeindeContainer);
		});

		return containerList;
	}

	private Predicate createStatusPredicate(
		@NotNull String status,
		CriteriaBuilder cb,
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root) {
		final Predicate statusPredicate = cb.equal(
			root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.status),
			LastenausgleichTagesschuleAngabenGemeindeStatus.valueOf(status));
		return statusPredicate;
	}

	private Predicate createTimestampMutiertPredicate(
		@Nonnull String timestampMutiert,
		CriteriaBuilder cb,
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root) {

		Predicate timestampMutiertPredicate;
		try {
			// Wir wollen ohne Zeit vergleichen
			Expression<LocalDate> timestampAsLocalDate =
				root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.timestampMutiert).as(LocalDate.class);
			LocalDate searchDate = LocalDate.parse(timestampMutiert, Constants.DATE_FORMATTER);
			timestampMutiertPredicate = cb.equal(timestampAsLocalDate, searchDate);
		} catch (DateTimeParseException e) {
			// no valid date. we return false, since no antrag should be found
			timestampMutiertPredicate = cb.disjunction();
		}
		return timestampMutiertPredicate;
	}

	private List<LastenausgleichTagesschuleAngabenGemeindeContainer> getLastenausgleicheTagesschulenForInstitution(
		@Nullable String periode,
		@Nullable String status) {

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		final SetJoin<LastenausgleichTagesschuleAngabenGemeindeContainer,
			LastenausgleichTagesschuleAngabenInstitutionContainer>
			join = root.join(
			LastenausgleichTagesschuleAngabenGemeindeContainer_.angabenInstitutionContainers,
			JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<>();

		Predicate institutionIn = join.get(LastenausgleichTagesschuleAngabenInstitutionContainer_.institution)
			.in(Objects.requireNonNull(institutionService.getInstitutionenReadableForCurrentBenutzer(false)));

		predicates.add(institutionIn);

		Predicate notNeu = cb.not(
			cb.equal(
				root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.status),
				LastenausgleichTagesschuleAngabenGemeindeStatus.NEU)
		);
		query.where(institutionIn, notNeu);

		predicates.add(notNeu);

		if (periode != null) {
			predicates.add(PredicateHelper.getPredicateFilterGesuchsperiode(cb,
				root.join(LastenausgleichTagesschuleAngabenGemeindeContainer_.gesuchsperiode, JoinType.INNER),
				periode));
		}
		if (status != null) {
			if (!EnumUtil.isOneOf(status, LastenausgleichTagesschuleAngabenGemeindeStatus.values())) {
				return new ArrayList<>();
			}
			final Predicate statusPredicate = createStatusPredicate(status, cb, root);
			predicates.add(statusPredicate);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindePruefen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		// Nur moeglich, wenn noch nicht geprüft
		Preconditions.checkState(
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON ||
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG,
			"LastenausgleichAngabenGemeindeContainer muss im Status IN_PRUEFUNG oder ZWEITPRUEFUNG sein");
		Objects.requireNonNull(fallContainer.getAngabenKorrektur());
		Preconditions.checkState(
			fallContainer.getAngabenKorrektur().getStatus() == LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN,
			"Die Angaben der Gemeinde müssen abgeschlossen sein"
		);

		if (!fallContainer.isInZweitpruefung() && this.selectedForZweitpruefung(fallContainer)) {
			fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG);
			fallContainer.getAngabenKorrektur().setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);
		} else {
			fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.GEPRUEFT);
		}
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
	}

	@Override
	public void saveKommentar(@Nonnull String containerId, @Nonnull String kommentar) {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsContainer =
			this.findLastenausgleichTagesschuleAngabenGemeindeContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveKommentar",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);
		latsContainer.setInternerKommentar(kommentar);
		persistence.persist(latsContainer);
	}

	@Nonnull
	@Override
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFormularAbschliessen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer) {

		LastenausgleichTagesschuleAngabenGemeinde formular;

		if (fallContainer.isAtLeastInBearbeitungKanton()) {
			Preconditions.checkState(
				fallContainer.getAngabenKorrektur() != null,
				ANGABEN_KORREKTUR_NOT_NULL
			);
			formular = fallContainer.getAngabenKorrektur();
		} else {
			Preconditions.checkState(
				fallContainer.getAngabenDeklaration() != null,
				ANGABEN_DEKLARATION_NOT_NULL
			);
			formular = fallContainer.getAngabenDeklaration();
		}

		Preconditions.checkState(
			formular.getStatus()
				== LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG ||
				formular.getStatus()
					== LastenausgleichTagesschuleAngabenGemeindeFormularStatus.VALIDIERUNG_FEHLGESCHLAGEN,
			"angabenDeklaration muss im Status IN_BEARBEITUNG oder VALIDIERUNG_FEHLGESCHLAGEN sein"
		);
		Preconditions.checkState(
			fallContainer.angabenDeklarationComplete(),
			"angabenDeklaration incomplete"
		);
		Preconditions.checkState(
			fallContainer.allInstitutionenGeprueft(),
			"not all institutionen are geprueft"
		);

		try {
			Preconditions.checkArgument(
				formular.plausibilisierungLATSBerechtigteStundenHolds(),
				"plausibilisierung geleistete stunden zu normlohnkosten failed"
			);
			Preconditions.checkArgument(
				fallContainer.plausibilisierungTagesschulenStundenHoldsForDeklaration(),
				"plausibilisierung stunden tagesschulen failed"
			);

			formular.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN);
		} catch (IllegalArgumentException e) {
			formular.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.VALIDIERUNG_FEHLGESCHLAGEN);
		}

		return persistence.persist(fallContainer);
	}

	@Nonnull
	@Override
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeWiederOeffnen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer) {

		Preconditions.checkState(
			fallContainer.isInBearbeitungGemeinde() || fallContainer.isInPruefungKanton(),
			"LastenausgleichTagesschuleAngabenGemeindeContainer muss in Bearbeitung Gemeinde oder Kanton sein"
		);

		LastenausgleichTagesschuleAngabenGemeinde angaben;

		if (fallContainer.isAtLeastInBearbeitungKanton()) {
			Preconditions.checkState(
				fallContainer.getAngabenKorrektur() != null,
				ANGABEN_KORREKTUR_NOT_NULL
			);
			angaben = fallContainer.getAngabenKorrektur();
		} else {
			Preconditions.checkState(
				fallContainer.getAngabenDeklaration() != null,
				ANGABEN_DEKLARATION_NOT_NULL
			);
			angaben = fallContainer.getAngabenDeklaration();
		}

		Preconditions.checkState(
			angaben.getStatus() == LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN,
			"LastenausgleichTagesschuleAngabenGemeinde muss im Status ABGESCHLOSSEN sein"
		);

		angaben.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);

		return persistence.persist(fallContainer);
	}

	@Override
	public void deleteLastenausgleicheTagesschule(@Nonnull Gesuchsperiode gesuchsperiode) {
		List<LastenausgleichTagesschuleAngabenGemeindeContainer> containerList =
			getLastenausgleicheTagesschulen(null, gesuchsperiode.getGesuchsperiodeString(), null, null);
		if (containerList == null) {
			return;
		}
		containerList.forEach(container -> {

			List<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> historyList =
				historyService.findHistoryForContainer(container);
			historyList.forEach(entry -> {
				persistence.remove(entry);
			});

			persistence.remove(container);
		});
	}

	@Nonnull
	@Override
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeZurueckAnGemeinde(
			@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container) {
		Preconditions.checkState(
			container.isInPruefungKanton(),
			"LastenausgleichTagesschuleAngabenGemeindeContainer muss in Prüfung Kanton sein"
		);
		Preconditions.checkState(
			container.getAngabenDeklaration() != null,
			ANGABEN_DEKLARATION_NOT_NULL
		);
		Preconditions.checkState(
			container.getAngabenKorrektur() != null,
			ANGABEN_KORREKTUR_NOT_NULL
		);

		container.copyForZurueckAnGemeinde();

		// reopen gemeinde formular, don't reopen insti formulare
		container.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenDeklaration().setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);
		container.getAngabenKorrektur().setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);

		LastenausgleichTagesschuleAngabenGemeindeContainer saved = saveLastenausgleichTagesschuleGemeinde(container, true);

		mailService.sendInfoLATSAntragZurueckAnGemeinde(saved);

		return saved;
	}

	@Nonnull
	@Override
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeZurueckInPruefungKanton(
			@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container) {
		Preconditions.checkState(
			container.isAntragGeprueft(),
			"LastenausgleichTagesschuleAngabenGemeindeContainer Geprüft sein"
		);
		Preconditions.checkState(
			container.getAngabenDeklaration() != null,
			ANGABEN_DEKLARATION_NOT_NULL
		);
		Preconditions.checkState(
			container.getAngabenKorrektur() != null,
			ANGABEN_KORREKTUR_NOT_NULL
		);

		// reopen gemeinde korrektur formular, don't reopen insti or deklaration formulare
		container.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON);
		container.getAngabenKorrektur().setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);

		LastenausgleichTagesschuleAngabenGemeindeContainer saved = saveLastenausgleichTagesschuleGemeinde(container, true);

		return saved;
	}



	@Nonnull
	@Override
	public boolean selectedForZweitpruefung(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container) {

		Preconditions.checkState(
			container.getAngabenKorrektur() != null,
			ANGABEN_KORREKTUR_NOT_NULL
		);

		Preconditions.checkState(
			container.getAngabenKorrektur().getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse() != null,
			ANGABEN_KORREKTUR_NOT_NULL
		);

		Preconditions.checkState(
			container.getAngabenKorrektur().getGeleisteteBetreuungsstundenBesondereBeduerfnisse() != null,
			ANGABEN_KORREKTUR_NOT_NULL
		);

		AtomicBoolean selected = new AtomicBoolean(false);
		gemeindeService.getGemeindeStammdatenByGemeindeId(container.getGemeinde().getId())
			.ifPresentOrElse(stammdaten -> {

				KorrespondenzSpracheTyp spracheTyp = stammdaten.getKorrespondenzsprache();
				BigDecimal betreuungsstundenForAutoZweitpruefung;
				BigDecimal anteilGemeindenForZweitpruefung;

				if (spracheTyp == KorrespondenzSpracheTyp.FR) {
					betreuungsstundenForAutoZweitpruefung =
						applicationPropertyService.findApplicationPropertyAsBigDecimal(
							ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR,
								container.getGemeinde().getMandant());
					anteilGemeindenForZweitpruefung = applicationPropertyService.findApplicationPropertyAsBigDecimal(
						ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR,
							container.getGemeinde().getMandant());
				} else {
					betreuungsstundenForAutoZweitpruefung =
						applicationPropertyService.findApplicationPropertyAsBigDecimal(
							ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE,
								container.getGemeinde().getMandant());
					anteilGemeindenForZweitpruefung = applicationPropertyService.findApplicationPropertyAsBigDecimal(
						ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE,
							container.getGemeinde().getMandant());
				}

				BigDecimal randomNumber = new BigDecimal(Math.random(), MathContext.DECIMAL64);

				BigDecimal betreuungsstundenTotal = container.getAngabenKorrektur()
					.getGeleisteteBetreuungsstundenBesondereBeduerfnisse()
					.add(container.getAngabenKorrektur().getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse());

				selected.set(betreuungsstundenTotal.compareTo(betreuungsstundenForAutoZweitpruefung) >= 0 ||
					randomNumber.compareTo(anteilGemeindenForZweitpruefung) <= 0);
			}, () -> selected.set(false));

		return selected.get();
	}

	@Override
	public @Nullable LastenausgleichTagesschuleAngabenGemeindeContainer findContainerOfPreviousPeriode(@Nonnull String currentAntragId) {
		LastenausgleichTagesschuleAngabenGemeindeContainer currentAntrag =
			findLastenausgleichTagesschuleAngabenGemeindeContainer(currentAntragId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"findContainerOfPreviousPeriode",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				currentAntragId)
			);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		Join<LastenausgleichTagesschuleAngabenGemeindeContainer, Gesuchsperiode> joinGesuchperiode =
			root.join(LastenausgleichTagesschuleAngabenGemeindeContainer_.gesuchsperiode);

		Predicate predicateGemeinde = cb.equal(
			root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde),
			currentAntrag.getGemeinde()
		);

		Expression<Integer> yearExpression = cb.function(
			"YEAR",
			Integer.class,
			joinGesuchperiode.get(Gesuchsperiode_.gueltigkeit).get(DateRange_.gueltigBis)
		);
		Integer currentStartYear = currentAntrag.getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear();
		Predicate predicateYear = cb.equal(
			yearExpression,
			currentStartYear
		);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicateGemeinde, predicateYear));
		List<LastenausgleichTagesschuleAngabenGemeindeContainer> containerList = persistence.getCriteriaResults(query);

		if (containerList.size() > 1) {
			throw new EbeguRuntimeException("findContainerOfPreviousPeriode", "Too many results found for container " + currentAntrag.getId());
		}

		if (containerList.size() == 1) {
			LastenausgleichTagesschuleAngabenGemeindeContainer previousAntrag = containerList.get(0);
			authorizer.checkReadAuthorization(previousAntrag);
			return previousAntrag;
		}
		return null;

	}

	@Nullable
	@Override
	public Number calculateErwarteteBetreuungsstunden(String containerId) {

		LastenausgleichTagesschuleAngabenGemeindeContainer currentAntrag =
			findLastenausgleichTagesschuleAngabenGemeindeContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"calculateErwarteteBetreuungsstunden",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);

		authorizer.checkReadAuthorization(currentAntrag);


		Collection<InstitutionStammdaten> allTagesschulen = this.institutionStammdatenService.getAllTagesschulenForGemeinde(currentAntrag.getGemeinde());
		BigDecimal erwarteteBetreuungsstunden = BigDecimal.ZERO;
		for (InstitutionStammdaten stammdaten : allTagesschulen) {
			BigDecimal result = this.angabenInstitutionService.countBetreuungsstundenPerYearForTagesschuleAndPeriode(
				stammdaten,
				currentAntrag.getGesuchsperiode()
			);
			erwarteteBetreuungsstunden = erwarteteBetreuungsstunden.add(result);
		};
		return erwarteteBetreuungsstunden;
	}
}


