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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungTableFilterDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung_;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.Mitteilung_;
import ch.dvbern.ebegu.enums.Amt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.SearchMode;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguExistingAntragException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Mitteilungen
 */
@Stateless
@Local(MitteilungService.class)
@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
	ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
	ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TS, SACHBEARBEITER_TS })
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class MitteilungServiceBean extends AbstractBaseService implements MitteilungService {

	private static final Logger LOG = LoggerFactory.getLogger(MitteilungServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private MailService mailService;

	@Inject
	private DossierService dossierService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public Mitteilung sendMitteilung(@Nonnull Mitteilung mitteilung) {
		Objects.requireNonNull(mitteilung);

		checkMitteilungDataConsistency(mitteilung);

		if (MitteilungStatus.ENTWURF != mitteilung.getMitteilungStatus()) {
			throw new IllegalArgumentException("Mitteilung ist nicht im Status ENTWURF und kann nicht gesendet "
				+ "werden");
		}
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		mitteilung.setSentDatum(LocalDateTime.now());

		authorizer.checkWriteAuthorizationMitteilung(mitteilung);
		setSenderAndEmpfaenger(mitteilung);

		// Falls die Mitteilung an einen Gesuchsteller geht, muss dieser benachrichtigt werden. Es muss zuerst
		// geprueft werden, dass
		// die Mitteilung valid ist, dafuer brauchen wir den Validator

		try {
			Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
			final Set<ConstraintViolation<Mitteilung>> validationErrors = validator.validate(mitteilung);
			if (!validationErrors.isEmpty()) {
				throw new ConstraintViolationException(validationErrors);
			}

			if (MitteilungTeilnehmerTyp.GESUCHSTELLER == mitteilung.getEmpfaengerTyp()
				&& mitteilung.getEmpfaenger() != null) {
				mailService.sendInfoMitteilungErhalten(mitteilung);
			}

		} catch (MailException e) {
			String message = String.format(
				"Mail InfoMitteilungErhalten konnte nicht verschickt werden fuer Mitteilung %s",
				mitteilung.getId());
			Level logLevel = ebeguConfiguration.getIsDevmode() ? Level.INFO : Level.ERROR;
			throw new EbeguRuntimeException("sendMitteilung", message, ErrorCodeEnum.ERROR_MAIL, logLevel, e);
		}

		return persistence.merge(mitteilung);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkMitteilungDataConsistency(@Nonnull Mitteilung mitteilung) {
		if (!mitteilung.isNew()) {
			Mitteilung persistedMitteilung =
				findMitteilung(mitteilung.getId()).orElseThrow(() -> new EbeguEntityNotFoundException
					(
						"sendMitteilung",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						"MitteilungId invalid: " + mitteilung.getId()));

			// Die gespeicherte wie auch die uebergebene Mitteilung muss im Status ENTWURF sein
			if (MitteilungStatus.ENTWURF != persistedMitteilung.getMitteilungStatus()) {
				throw new IllegalArgumentException(
					"Mitteilung aus DB ist nicht im Status ENTWURF und kann nicht gesendet werden");
			}
			if (!persistedMitteilung.getSender().equals(mitteilung.getSender())) {
				throw new IllegalArgumentException("Mitteilung aus DB hat anderen Sender gesetzt");
			}
		}
	}

	private void setSenderAndEmpfaenger(@Nonnull Mitteilung mitteilung) {
		Benutzer benutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new IllegalStateException("Benutzer ist nicht eingeloggt!"));

		Optional<Benutzer> optEmpfaengerAmt = dossierService.getHauptOrDefaultVerantwortlicher(
			mitteilung.getDossier());
		final Benutzer empfaengerAmt = optEmpfaengerAmt.orElseThrow(() ->
			new EbeguRuntimeException(
				"setSenderAndEmpfaenger",
				ErrorCodeEnum.ERROR_VERANTWORTLICHER_NOT_FOUND,
				mitteilung.getId())
		);

		mitteilung.setSender(benutzer);
		switch (benutzer.getRole()) {
		case GESUCHSTELLER: {
			mitteilung.setEmpfaenger(empfaengerAmt);
			mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
			mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
			break;
		}
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION:
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT: {
			mitteilung.setEmpfaenger(empfaengerAmt);
			mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
			mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
			break;
		}
		case SACHBEARBEITER_BG:
		case ADMIN_BG:
		case SACHBEARBEITER_GEMEINDE:
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_TS:
		case ADMIN_TS: {
			Benutzer besitzer = mitteilung.getFall().getBesitzer();
			mitteilung.setEmpfaenger(besitzer);
			mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
			mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
			break;
		}
		case SUPER_ADMIN: {
			// Superadmin kann als verschiedene Rollen Mitteilungen schicken
			if (mitteilung instanceof Betreuungsmitteilung) {
				mitteilung.setEmpfaenger(empfaengerAmt);
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
			} else {
				Benutzer besitzer = mitteilung.getFall().getBesitzer();
				mitteilung.setEmpfaenger(besitzer);
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
			}
		}
		}
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public Mitteilung saveEntwurf(@Nonnull Mitteilung mitteilung) {
		Objects.requireNonNull(mitteilung);

		checkMitteilungDataConsistency(mitteilung);

		if (MitteilungStatus.ENTWURF != mitteilung.getMitteilungStatus()) {
			throw new IllegalArgumentException(
				"Mitteilung ist nicht im Status ENTWURF und kann nicht als Entwurf gespeichert werden");
		}
		setSenderAndEmpfaenger(mitteilung);
		authorizer.checkWriteAuthorizationMitteilung(mitteilung);
		return persistence.merge(mitteilung);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Mitteilung setMitteilungGelesen(@Nonnull String mitteilungsId) {
		return setMitteilungsStatusIfBerechtigt(
			mitteilungsId,
			MitteilungStatus.GELESEN,
			MitteilungStatus.NEU,
			MitteilungStatus.ERLEDIGT);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Mitteilung setMitteilungErledigt(@Nonnull String mitteilungsId) {
		return setMitteilungsStatusIfBerechtigt(mitteilungsId, MitteilungStatus.ERLEDIGT, MitteilungStatus.GELESEN);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public Optional<Mitteilung> findMitteilung(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Mitteilung mitteilung = persistence.find(Mitteilung.class, key);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		return Optional.ofNullable(mitteilung);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Optional<Betreuungsmitteilung> findBetreuungsmitteilung(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Betreuungsmitteilung mitteilung = persistence.find(Betreuungsmitteilung.class, key);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		return Optional.ofNullable(mitteilung);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS })
	public Collection<Betreuungsmitteilung> findAllBetreuungsmitteilungenForBetreuung(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");

		// Diese Methode wird nur beim Loeschen einer Online-Mutation durch den Admin beim Erstellen einer
		// Papier-Mutation verwendet.
		// Wir koennen in diesem Fall die normale AuthCheck verwenden, da niemand vom JA fuer die vorhandene
		// Online-Mutation des GS nach herkoemmlichem Schema berechtigt ist. Wir duerfen hier aber trotzdem
		// loeschen. Methode ist aber nur fuer ADMIN_BG und SUPER_ADMIN verfuegbar.

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<Betreuung> betreuungParam = cb.parameter(Betreuung.class, "betreuunParam");
		ParameterExpression<MitteilungStatus> statusParam = cb.parameter(MitteilungStatus.class, "statusParam");

		Predicate predicateLinkedObject = cb.equal(root.get(Betreuungsmitteilung_.betreuung), betreuungParam);
		predicates.add(predicateLinkedObject);

		Predicate predicateEntwurf = cb.notEqual(root.get(Mitteilung_.mitteilungStatus), statusParam);
		predicates.add(predicateEntwurf);

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<Betreuungsmitteilung> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("betreuunParam", betreuung);
		tq.setParameter("statusParam", MitteilungStatus.ENTWURF);

		return tq.getResultList();
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Collection<Mitteilung> findAllMitteilungenForBetreuung(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");

		authorizer.checkReadAuthorization(betreuung);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<Betreuung> betreuungParam = cb.parameter(Betreuung.class, "betreuunParam");

		Predicate predicateLinkedObject = cb.equal(root.get(Mitteilung_.betreuung), betreuungParam);
		predicates.add(predicateLinkedObject);

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<Mitteilung> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("betreuunParam", betreuung);

		return tq.getResultList();
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Collection<Mitteilung> getMitteilungenForCurrentRolle(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier, "dossier muss gesetzt sein");
		authorizer.checkReadAuthorizationDossier(dossier);
		return getMitteilungenForCurrentRolle(Mitteilung_.dossier, dossier);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public Collection<Mitteilung> getMitteilungenForCurrentRolle(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");
		authorizer.checkReadAuthorization(betreuung);
		return getMitteilungenForCurrentRolle(Mitteilung_.betreuung, betreuung);
	}

	private <T> Collection<Mitteilung> getMitteilungenForCurrentRolle(
		SingularAttribute<Mitteilung, T> attribute,
		@Nonnull T linkedEntity) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateLinkedObject = cb.equal(root.get(attribute), linkedEntity);
		predicates.add(predicateLinkedObject);

		Predicate predicateEntwurf = cb.notEqual(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.ENTWURF);
		predicates.add(predicateEntwurf);

		MitteilungTeilnehmerTyp mitteilungTeilnehmerTyp = getMitteilungTeilnehmerTypForCurrentUser();
		Predicate predicateSender = cb.equal(root.get(Mitteilung_.senderTyp), mitteilungTeilnehmerTyp);
		Predicate predicateEmpfaenger = cb.equal(root.get(Mitteilung_.empfaengerTyp), mitteilungTeilnehmerTyp);
		Predicate predicateSenderOrEmpfaenger = cb.or(predicateSender, predicateEmpfaenger);
		predicates.add(predicateSenderOrEmpfaenger);

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		List<Mitteilung> mitteilungen = persistence.getCriteriaResults(query);
		authorizer.checkReadAuthorizationMitteilungen(mitteilungen);
		return mitteilungen;
	}

	@Nullable
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public Mitteilung getEntwurfForCurrentRolle(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier, "dossier muss gesetzt sein");
		authorizer.checkReadAuthorizationDossier(dossier);
		return getEntwurfForCurrentRolle(Mitteilung_.dossier, dossier);
	}

	@Override
	@Nullable
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public Mitteilung getEntwurfForCurrentRolle(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");
		return getEntwurfForCurrentRolle(Mitteilung_.betreuung, betreuung);
	}

	private <T> Mitteilung getEntwurfForCurrentRolle(
		SingularAttribute<Mitteilung, T> attribute,
		@Nonnull T linkedEntity) {
		UserRole currentUserRole = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		Join<Mitteilung, Benutzer> joinSender = root.join(Mitteilung_.sender);
		SetJoin<Benutzer, Berechtigung> joinSenderBerechtigungen = joinSender.join(Benutzer_.berechtigungen);
		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateLinkedObject = cb.equal(root.get(attribute), linkedEntity);
		predicates.add(predicateLinkedObject);

		Predicate predicateEntwurf = cb.equal(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.ENTWURF);
		predicates.add(predicateEntwurf);

		MitteilungTeilnehmerTyp mitteilungTeilnehmerTyp = getMitteilungTeilnehmerTypForCurrentUser();
		Predicate predicateSender = cb.equal(root.get(Mitteilung_.senderTyp), mitteilungTeilnehmerTyp);
		predicates.add(predicateSender);

		if (currentUserRole.isRoleJugendamt()) {
			setActiveAndRolePredicates(cb, joinSenderBerechtigungen, predicates, UserRole.getJugendamtRoles());
		} else if (currentUserRole.isRoleSchulamt()) {
			setActiveAndRolePredicates(cb, joinSenderBerechtigungen, predicates, UserRole.getSchulamtRoles());
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		Mitteilung entwurf = persistence.getCriteriaSingleResult(query);
		authorizer.checkWriteAuthorizationMitteilung(entwurf);
		return entwurf;
	}

	private void setActiveAndRolePredicates(
		CriteriaBuilder cb, SetJoin<Benutzer, Berechtigung> joinSenderBerechtigungen,
		List<Predicate> predicates, List<UserRole> jugendamtRoles) {
		Predicate predicateActive = cb.between(
			cb.literal(LocalDate.now()),
			joinSenderBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinSenderBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
		Predicate predicateSenderGleichesAmt = joinSenderBerechtigungen.get(Berechtigung_.role).in(jugendamtRoles);
		predicates.add(cb.and(predicateActive, predicateSenderGleichesAmt));
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public void removeMitteilung(@Nonnull Mitteilung mitteilung) {
		Objects.requireNonNull(mitteilung);
		authorizer.checkWriteAuthorizationMitteilung(mitteilung);
		persistence.remove(mitteilung);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public void removeAllMitteilungenForFall(@Nonnull Fall fall) {
		// Alle Mitteilungen aller Dossiers dieses Falls
		Collection<Dossier> dossiersOfFall =
			criteriaQueryHelper.getEntitiesByAttribute(Dossier.class, fall, Dossier_.fall);
		for (Dossier dossier : dossiersOfFall) {
			Collection<Mitteilung> mitteilungen =
				criteriaQueryHelper.getEntitiesByAttribute(Mitteilung.class, dossier, Mitteilung_.dossier);
			for (Mitteilung mitteilung : mitteilungen) {
				authorizer.checkWriteAuthorizationMitteilung(mitteilung);
				persistence.remove(Mitteilung.class, mitteilung.getId());
			}
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public void removeAllBetreuungMitteilungenForGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkWriteAuthorization(gesuch);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		final Join<Betreuung, KindContainer> join = root.join(Mitteilung_.betreuung, JoinType.LEFT)
			.join(Betreuung_.kind, JoinType.LEFT);

		Predicate gesuchPred = cb.equal(join.get(KindContainer_.gesuch), gesuch);
		Predicate withBetreuungPred = cb.isNotNull(root.get(Mitteilung_.betreuung));

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, gesuchPred, withBetreuungPred));
		final List<Mitteilung> mitteilungen = persistence.getCriteriaResults(query);

		for (Mitteilung mitteilung : mitteilungen) {
			persistence.remove(Mitteilung.class, mitteilung.getId());
		}
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public Collection<Mitteilung> setAllNewMitteilungenOfDossierGelesen(@Nonnull Dossier dossier) {
		Collection<Mitteilung> mitteilungen = getMitteilungenForCurrentRolle(dossier);
		for (Mitteilung mitteilung : mitteilungen) {
			if (MitteilungStatus.NEU == mitteilung.getMitteilungStatus()) {
				setMitteilungsStatusIfBerechtigt(mitteilung, MitteilungStatus.GELESEN, MitteilungStatus.NEU);
			}
		}
		return mitteilungen;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Collection<Mitteilung> getNewMitteilungenOfDossierForCurrentRolle(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier, "dossier muss gesetzt sein");
		authorizer.checkReadAuthorizationDossier(dossier);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateFall = cb.equal(root.get(Mitteilung_.dossier), dossier);
		predicates.add(predicateFall);

		Predicate predicateNew = cb.equal(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.NEU);
		predicates.add(predicateNew);

		MitteilungTeilnehmerTyp mitteilungTeilnehmerTyp = getMitteilungTeilnehmerTypForCurrentUser();
		Predicate predicateEmpfaenger = cb.equal(root.get(Mitteilung_.empfaengerTyp), mitteilungTeilnehmerTyp);
		predicates.add(predicateEmpfaenger);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		List<Mitteilung> mitteilungen = persistence.getCriteriaResults(query);
		authorizer.checkReadAuthorizationMitteilungen(mitteilungen);
		return mitteilungen;
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS })
	public Long getAmountNewMitteilungenForCurrentBenutzer() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateNew = cb.equal(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.NEU);
		predicates.add(predicateNew);

		Benutzer loggedInBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException
			("getAmountNewMitteilungenForCurrentBenutzer", "No User is logged in"));
		Predicate predicateEmpfaenger = cb.equal(root.get(Mitteilung_.empfaenger), loggedInBenutzer);
		predicates.add(predicateEmpfaenger);

		query.select(cb.countDistinct(root));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaSingleResult(query);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public Betreuungsmitteilung sendBetreuungsmitteilung(@Nonnull Betreuungsmitteilung betreuungsmitteilung) {
		Objects.requireNonNull(betreuungsmitteilung);
		if (MitteilungTeilnehmerTyp.INSTITUTION != betreuungsmitteilung.getSenderTyp()) {
			throw new IllegalArgumentException(
				"Eine Betreuungsmitteilung darf nur bei einer Institution geschickt werden");
		}
		if (MitteilungTeilnehmerTyp.JUGENDAMT != betreuungsmitteilung.getEmpfaengerTyp()) {
			throw new IllegalArgumentException("Eine Betreuungsmitteilung darf nur an das Jugendamt geschickt werden");
		}
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU); // vorsichtshalber
		betreuungsmitteilung.setSentDatum(LocalDateTime.now());
		authorizer.checkWriteAuthorizationMitteilung(betreuungsmitteilung);
		setSenderAndEmpfaenger(betreuungsmitteilung);

		// A Betreuungsmitteilung is created and sent, therefore persist and not merge
		return persistence.persist(betreuungsmitteilung);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Gesuch applyBetreuungsmitteilung(@Nonnull Betreuungsmitteilung mitteilung) {
		final Gesuch gesuch = mitteilung.getBetreuung().extractGesuch();
		authorizer.checkWriteAuthorization(gesuch);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		// neustes Gesuch lesen
		final Optional<Gesuch> neustesGesuchOpt;
		try {
			neustesGesuchOpt = gesuchService.getNeustesGesuchFuerGesuch(gesuch);
		} catch (EJBTransactionRolledbackException exception) {
			//Wenn der Sachbearbeiter den neusten Antrag nicht lesen darf ist es ein noch nicht freigegebener ONLINE
			// Antrag
			if (exception.getCause().getClass().equals(EJBAccessException.class)) {
				throw new EbeguExistingAntragException(
					"applyBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION,
					exception,
					gesuch.getDossier().getId(),
					gesuch.getGesuchsperiode().getId());
			}
			throw exception;
		}
		if (neustesGesuchOpt.isPresent()) {
			final Gesuch neustesGesuch = neustesGesuchOpt.get();
			// Sobald irgendein Antrag dieser Periode geperrt ist, darf keine Mutationsmeldungs-Mutation erstellt
			// werden!
			if (neustesGesuch.isGesperrtWegenBeschwerde()) {
				throw new EbeguRuntimeException(
					"applyBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_FALL_GESPERRT);
			}
			if (AntragStatus.VERFUEGEN == neustesGesuch.getStatus()) {
				throw new EbeguRuntimeException(
					"applyBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_STATUS_VERFUEGEN);
			}
			if (!AntragStatus.getVerfuegtAndSTVStates().contains(neustesGesuch.getStatus())
				&& neustesGesuch.isMutation()) {
				//betreuungsaenderungen der bestehenden, offenen Mutation hinzufuegen (wenn wir hier sind muss es sich
				// um ein PAPIER) Antrag handeln
				applyBetreuungsmitteilungToMutation(neustesGesuch, mitteilung);
				return neustesGesuch;
			}
			if (AntragStatus.getVerfuegtAndSTVStates().contains(neustesGesuch.getStatus())) {
				// create Mutation if there is currently no Mutation
				final Optional<Gesuch> mutationOpt = this.gesuchService.antragMutieren(
					gesuch.getId(),
					LocalDate.now());
				if (mutationOpt.isPresent()) {
					Gesuch persistedMutation = gesuchService.createGesuch(mutationOpt.get());
					applyBetreuungsmitteilungToMutation(persistedMutation, mitteilung);
					return persistedMutation;
				}
			} else {
				throw new EbeguRuntimeException(
					"applyBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_GESUCH_NICHT_FREIGEGEBEN);
			}
		}
		return gesuch;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Optional<Betreuungsmitteilung> findNewestBetreuungsmitteilung(@Nonnull String betreuungId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);

		Predicate predicateLinkedObject =
			cb.equal(root.get(Betreuungsmitteilung_.betreuung).get(Betreuung_.id), betreuungId);
		Predicate predicateNotErledigt =
			cb.equal(root.get(Betreuungsmitteilung_.mitteilungStatus), MitteilungStatus.ERLEDIGT).not();

		query.orderBy(cb.desc(root.get(Betreuungsmitteilung_.sentDatum)));
		query.where(predicateLinkedObject, predicateNotErledigt);

		final List<Betreuungsmitteilung> result =
			persistence.getEntityManager().createQuery(query).setFirstResult(0).setMaxResults(1).getResultList();
		if (result.isEmpty()) {
			return Optional.empty();
		}
		Betreuungsmitteilung firstResult = result.get(0);
		authorizer.checkReadAuthorizationMitteilung(firstResult);
		return Optional.of(firstResult);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Mitteilung mitteilungUebergebenAnJugendamt(@Nonnull String mitteilungId) {
		Mitteilung mitteilung = findMitteilung(mitteilungId)
			.orElseThrow(() -> new EbeguRuntimeException("mitteilungUebergebenAnJugendamt", "Mitteilung not found"));

		authorizer.checkReadAuthorizationMitteilung(mitteilung);

		// Dass der eingeloggte Benutzer Schulamt ist, ist schon durch die Berechtigungen geprueft. Es muss noch
		// sichergestellt werden, dass die Meldung auch tatsaechlich dem Schulamt "gehoert"
		if (mitteilung.getEmpfaengerAmt() != Amt.SCHULAMT) {
			throw new IllegalArgumentException(
				"Der Empfänger der Mitteilung hat nicht die Rolle Schulamt");
		}

		// An wen soll die Meldung delegiert werden?
		Benutzer verantwortlicherBG = Stream.of(
			mitteilung.getDossier().getVerantwortlicherBG(),
			gemeindeService.getGemeindeStammdatenByGemeindeId(mitteilung
				.getDossier()
				.getGemeinde()
				.getId()).map(GemeindeStammdaten::getDefaultBenutzerBG).orElse(null))
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow(() -> new EbeguRuntimeException(
				"mitteilungUebergebenAnJugendamt",
				ErrorCodeEnum.ERROR_EMPFAENGER_JA_NOT_FOUND,
				mitteilung.getId()));

		// Den VerantwortlichenJA als Empfänger setzen
		mitteilung.setEmpfaenger(verantwortlicherBG);
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);

		return persistence.merge(mitteilung);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Mitteilung mitteilungUebergebenAnSchulamt(@Nonnull String mitteilungId) {
		Mitteilung mitteilung = findMitteilung(mitteilungId)
			.orElseThrow(() -> new EbeguRuntimeException("mitteilungUebergebenAnSchulamt", "Mitteilung not found"));

		authorizer.checkReadAuthorizationMitteilung(mitteilung);

		// Dass der eingeloggte Benutzer Jugendamt ist, ist schon durch die Berechtigungen geprueft. Es muss noch
		// sichergestellt werden, dass die Meldung auch tatsaechlich dem Jugendamt "gehoert"
		if (mitteilung.getEmpfaengerAmt() != Amt.JUGENDAMT) {
			throw new IllegalArgumentException(
				"Der Empfänger der Mitteilung hat nicht die Rolle Jugendamt");
		}

		// An wen soll die Meldung delegiert werden?
		Benutzer verantwortlicherTS = Stream.of(
			mitteilung.getDossier().getVerantwortlicherTS(),
			gemeindeService.getGemeindeStammdatenByGemeindeId(mitteilung
				.getDossier()
				.getGemeinde()
				.getId()).map(GemeindeStammdaten::getDefaultBenutzerTS).orElse(null))
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow(() -> new EbeguRuntimeException(
				"mitteilungUebergebenAnSchulamt",
				ErrorCodeEnum.ERROR_EMPFAENGER_SCH_NOT_FOUND,
				mitteilung.getId()));

		// Den VerantwortlichenJA als Empfänger setzen
		mitteilung.setEmpfaenger(verantwortlicherTS);
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);

		return persistence.merge(mitteilung);
	}

	@Nonnull
	@Override
	public Pair<Long, List<Mitteilung>> searchMitteilungen(
		@Nonnull MitteilungTableFilterDTO mitteilungTableFilterDto,
		@Nonnull Boolean includeClosed,
		@Nonnull Locale locale
	) {
		Pair<Long, List<Mitteilung>> result;
		Long countResult = searchMitteilungen(mitteilungTableFilterDto, includeClosed, SearchMode.COUNT, locale).getLeft();
		if (countResult.equals(0L)) {    // no result found
			result = new ImmutablePair<>(0L, Collections.emptyList());
		} else {
			Pair<Long, List<Mitteilung>> searchResult =
				searchMitteilungen(mitteilungTableFilterDto, includeClosed, SearchMode.SEARCH, locale);
			result = new ImmutablePair<>(countResult, searchResult.getRight());
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "PMD.NcssMethodCount" }) // Je nach Abfrage ist es String oder Long
	private Pair<Long, List<Mitteilung>> searchMitteilungen(
		@Nonnull MitteilungTableFilterDTO mitteilungTableFilterDto,
		@Nonnull Boolean includeClosed,
		@Nonnull SearchMode mode,
		@Nonnull Locale locale
	) {

		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguRuntimeException("searchAllAntraege", "No User is logged in"));

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery query = SearchUtil.getQueryForSearchMode(cb, mode, "searchMitteilungen");

		// Construct from-clause
		Root<Mitteilung> root = query.from(Mitteilung.class);

		// Join all the relevant relations
		Join<Mitteilung, Dossier> joinDossier = root.join(Mitteilung_.dossier, JoinType.INNER);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall);

		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		Join<Fall, Benutzer> joinBesitzer = joinFall.join(Fall_.besitzer, JoinType.LEFT);
		Join<Mitteilung, Benutzer> joinSender = root.join(Mitteilung_.sender, JoinType.LEFT);
		Join<Mitteilung, Benutzer> joinEmpfaenger = root.join(Mitteilung_.empfaenger, JoinType.LEFT);
		SetJoin<Benutzer, Berechtigung> joinEmpfaengerBerechtigungen = joinEmpfaenger.join(Benutzer_.berechtigungen);

		// Predicates derived from PredicateDTO (Filter coming from client)
		MitteilungPredicateObjectDTO predicateObjectDto = mitteilungTableFilterDto.getSearch().getPredicateObject();

		//prepare predicates
		List<Predicate> predicates = new ArrayList<>();

		// Keine Entwuerfe fuer Posteingang
		Predicate predicateEntwurf = cb.notEqual(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.ENTWURF);
		predicates.add(predicateEntwurf);

		// Richtiger Empfangs-Typ. Persoenlicher Empfaenger wird nicht beachtet sondern auf Client mit Filter geloest
		MitteilungTeilnehmerTyp mitteilungTeilnehmerTyp = getMitteilungTeilnehmerTypForCurrentUser();
		Predicate predicateEmpfaengerTyp = cb.equal(root.get(Mitteilung_.empfaengerTyp), mitteilungTeilnehmerTyp);
		predicates.add(predicateEmpfaengerTyp);

		filterGemeinde(user, joinGemeinde, predicates);

		if (predicateObjectDto != null) {

			// sender
			if (predicateObjectDto.getSender() != null) {
				predicates.add(
					cb.or(
						cb.like(
							joinSender.get(Benutzer_.nachname),
							SearchUtil.withWildcards(predicateObjectDto.getSender())),
						cb.like(
							joinSender.get(Benutzer_.vorname),
							SearchUtil.withWildcards(predicateObjectDto.getSender()))
					));
			}
			// fallNummer
			if (predicateObjectDto.getFallNummer() != null) {
				// Die Fallnummer muss als String mit LIKE verglichen werden: Bei Eingabe von "14" soll der Fall "114"
				// kommen
				Expression<String> fallNummerAsString = joinFall.get(Fall_.fallNummer).as(String.class);
				String fallNummerWithWildcards = SearchUtil.withWildcards(predicateObjectDto.getFallNummer());
				predicates.add(cb.like(fallNummerAsString, fallNummerWithWildcards));
			}
			// familienName
			if (predicateObjectDto.getFamilienName() != null) {
				predicates.add(
					cb.or(
						cb.like(
							joinBesitzer.get(Benutzer_.nachname),
							SearchUtil.withWildcards(predicateObjectDto.getFamilienName())),
						cb.like(
							joinBesitzer.get(Benutzer_.vorname),
							SearchUtil.withWildcards(predicateObjectDto.getFamilienName()))
					));
			}
			// subject
			if (predicateObjectDto.getSubject() != null) {
				predicates.add(cb.like(
					root.get(Mitteilung_.subject),
					SearchUtil.withWildcards(predicateObjectDto.getSubject())));
			}
			// sentDatum
			if (predicateObjectDto.getSentDatum() != null) {
				try {
					LocalDate searchDate = LocalDate.parse(
						predicateObjectDto.getSentDatum(),
						Constants.DATE_FORMATTER);
					predicates.add(cb.between(
						root.get(Mitteilung_.sentDatum),
						searchDate.atStartOfDay(),
						searchDate.plusDays(1).atStartOfDay()));
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Mitteilung geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			// empfaenger
			if (predicateObjectDto.getEmpfaenger() != null) {
				String[] strings = predicateObjectDto.getEmpfaenger().split(" ");
				predicates.add(
					cb.and(
						cb.equal(joinEmpfaenger.get(Benutzer_.vorname), strings[0]),
						cb.equal(joinEmpfaenger.get(Benutzer_.nachname), strings[1])
					));
			}
			// empfaengerAmt
			if (predicateObjectDto.getEmpfaengerAmt() != null) {
				Amt amt = Amt.valueOf(predicateObjectDto.getEmpfaengerAmt());
				switch (amt) {
				case JUGENDAMT:
					setActiveAndRolePredicates(
						cb,
						joinEmpfaengerBerechtigungen,
						predicates,
						UserRole.getJugendamtSuperadminRoles());
					break;
				case SCHULAMT:
					setActiveAndRolePredicates(
						cb,
						joinEmpfaengerBerechtigungen,
						predicates,
						UserRole.getSchulamtRoles());
					break;
				case GEMEINDE:
					setActiveAndRolePredicates(
						cb,
						joinEmpfaengerBerechtigungen,
						predicates,
						UserRole.getSuperadminAllGemeindeRoles());
					break;
				}
			}
			// mitteilungStatus
			if (predicateObjectDto.getMitteilungStatus() != null) {
				MitteilungStatus mitteilungStatus = MitteilungStatus.valueOf(predicateObjectDto.getMitteilungStatus());
				predicates.add(cb.equal(root.get(Mitteilung_.mitteilungStatus), mitteilungStatus));
			}
			// Inkl. abgeschlossene
			if (!includeClosed) {
				Predicate predicateNichtErledigt =
					cb.notEqual(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.ERLEDIGT);
				predicates.add(predicateNichtErledigt);
			}
			// gemeinde
			if (predicateObjectDto.getGemeinde() != null) {

				Predicate gemeindePredicate = cb.equal(
					joinDossier.get(Dossier_.gemeinde).get(Gemeinde_.name),
					predicateObjectDto.getGemeinde());
				predicates.add(gemeindePredicate);
			}
		}

		// Construct the select- and where-clause
		switch (mode) {
		case SEARCH:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(root.get(Mitteilung_.id)).where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			constructOrderByClause(
				mitteilungTableFilterDto,
				cb,
				query,
				root,
				joinFall,
				joinBesitzer,
				joinSender,
				joinEmpfaenger,
				joinEmpfaengerBerechtigungen,
				locale
			);
			break;
		case COUNT:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(cb.countDistinct(root.get(Gesuch_.id)))
				.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			break;
		}

		// Prepare and execute the query and build the result
		Pair<Long, List<Mitteilung>> result = null;
		switch (mode) {
		case SEARCH:
			List<String> gesuchIds =
				persistence.getCriteriaResults(query); //select all ids in order, may contain duplicates
			List<Mitteilung> pagedResult;
			if (mitteilungTableFilterDto.getPagination() != null) {
				int firstIndex = mitteilungTableFilterDto.getPagination().getStart();
				Integer maxresults = mitteilungTableFilterDto.getPagination().getNumber();
				List<String> orderedIdsToLoad =
					SearchUtil.determineDistinctIdsToLoad(gesuchIds, firstIndex, maxresults);
				pagedResult = findMitteilungen(orderedIdsToLoad);
			} else {
				pagedResult = findMitteilungen(gesuchIds);
			}
			result = new ImmutablePair<>(null, pagedResult);
			break;
		case COUNT:
			Long count = (Long) persistence.getCriteriaSingleResult(query);
			result = new ImmutablePair<>(count, null);
			break;
		}
		return result;
	}

	private void filterGemeinde(Benutzer user, Join<Dossier, Gemeinde> joinGemeinde, List<Predicate> predicates) {
		if (user.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Collection<Gemeinde> gemeindenForUser = user.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenForUser);
			predicates.add(inGemeinde);
		}
	}

	@SuppressWarnings("ReuseOfLocalVariable")
	private void constructOrderByClause(
		@Nonnull MitteilungTableFilterDTO tableFilterDTO,
		CriteriaBuilder cb,
		CriteriaQuery query,
		Root<Mitteilung> root,
		Join<Dossier, Fall> joinFall,
		Join<Fall, Benutzer> joinBesitzer,
		Join<Mitteilung, Benutzer> joinSender,
		Join<Mitteilung, Benutzer> joinEmpfaenger,
		SetJoin<Benutzer, Berechtigung> joinEmpfaengerBerechtigungen,
		@Nonnull Locale locale
	) {
		Expression<?> expression = null;
		if (tableFilterDTO.getSort() != null && tableFilterDTO.getSort().getPredicate() != null) {
			switch (tableFilterDTO.getSort().getPredicate()) {
			case "sender":
				expression = joinSender.get(Benutzer_.vorname);
				break;
			case "fallNummer":
				expression = joinFall.get(Fall_.fallNummer);
				break;
			case "familienName":
				expression = joinBesitzer.get(Benutzer_.vorname);
				break;
			case "subject":
				expression = root.get(Mitteilung_.subject);
				break;
			case "sentDatum":
				expression = root.get(Mitteilung_.sentDatum);
				break;
			case "empfaenger":
				expression = joinEmpfaenger.get(Benutzer_.vorname);
				break;
			case "empfaengerAmt":
				Predicate predicateActive = cb.between(
					cb.literal(LocalDate.now()),
					joinEmpfaengerBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
					joinEmpfaengerBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
				Predicate predicateJA =
					joinEmpfaengerBerechtigungen.get(Berechtigung_.role).in(UserRole.getJugendamtRoles());
				Expression<Boolean> isActiveJA = cb.and(predicateActive, predicateJA);
				String sJugendamt =
					ServerMessageUtil.getMessage(Amt.class.getSimpleName() + '_' + Amt.JUGENDAMT.name(), locale);
				String sSchulamt = ServerMessageUtil.getMessage(Amt.class.getSimpleName() + '_' + Amt.SCHULAMT.name(), locale);
				expression = cb.selectCase().when(isActiveJA, sJugendamt).otherwise(sSchulamt);
				break;
			case "mitteilungStatus":
				expression = root.get(Mitteilung_.mitteilungStatus);
				break;
			default:
				LOG.warn(
					"Using default sort by SentDatum because there is no specific clause for predicate {}",
					tableFilterDTO.getSort().getPredicate());
				expression = root.get(Mitteilung_.sentDatum);
				break;
			}
			query.orderBy(tableFilterDTO.getSort().getReverse() ? cb.asc(expression) : cb.desc(expression));
		} else {
			// Default sort when nothing is choosen
			expression = root.get(Mitteilung_.sentDatum);
			query.orderBy(cb.desc(expression));
		}
	}

	private List<Mitteilung> findMitteilungen(@Nonnull List<String> gesuchIds) {
		if (!gesuchIds.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
			Root<Mitteilung> root = query.from(Mitteilung.class);
			Predicate predicate = root.get(Mitteilung_.id).in(gesuchIds);
			query.where(predicate);
			//reduce to unique gesuche
			List<Mitteilung> listWithDuplicates = persistence.getCriteriaResults(query);
			LinkedHashSet<Mitteilung> set = new LinkedHashSet<>();
			//richtige reihenfolge beibehalten
			for (String gesuchId : gesuchIds) {
				listWithDuplicates.stream()
					.filter(gesuch -> gesuch.getId().equals(gesuchId))
					.findFirst()
					.ifPresent(set::add);
			}
			return new ArrayList<>(set);
		}
		return Collections.emptyList();
	}

	private void applyBetreuungsmitteilungToMutation(Gesuch gesuch, Betreuungsmitteilung mitteilung) {
		authorizer.checkWriteAuthorization(gesuch);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		final Optional<Betreuung> betreuungToChangeOpt = gesuch.extractBetreuungsFromBetreuungNummer(
			mitteilung.getBetreuung().getKind().getKindNummer(),
			mitteilung.getBetreuung().getBetreuungNummer());
		if (betreuungToChangeOpt.isPresent()) {
			Betreuung existingBetreuung = betreuungToChangeOpt.get();
			existingBetreuung.getBetreuungspensumContainers()
				.clear();//delete all current Betreuungspensen before we add the modified list
			for (final BetreuungsmitteilungPensum betPensumMitteilung : mitteilung.getBetreuungspensen()) {
				BetreuungspensumContainer betPenCont = new BetreuungspensumContainer();
				betPenCont.setBetreuung(existingBetreuung);
				Betreuungspensum betPensumJA = new Betreuungspensum(betPensumMitteilung);
				//gs container muss nicht mikopiert werden
				betPenCont.setBetreuungspensumJA(betPensumJA);
				existingBetreuung.getBetreuungspensumContainers().add(betPenCont);
			}
			// when we apply a Betreuungsmitteilung we have to change the status to BESTAETIGT
			existingBetreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
			betreuungService.saveBetreuung(existingBetreuung, false);
			mitteilung.setApplied(true);
			mitteilung.setMitteilungStatus(MitteilungStatus.ERLEDIGT);
			persistence.merge(mitteilung);
		}
	}

	@Nullable
	private MitteilungTeilnehmerTyp getMitteilungTeilnehmerTypForCurrentUser() {
		UserRole currentUserRole = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		//noinspection EnumSwitchStatementWhichMissesCases
		switch (currentUserRole) {
		case GESUCHSTELLER: {
			return MitteilungTeilnehmerTyp.GESUCHSTELLER;
		}
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION:
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT: {
			return MitteilungTeilnehmerTyp.INSTITUTION;
		}
		case SUPER_ADMIN:
		case ADMIN_BG:
		case SACHBEARBEITER_BG:
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE:
		case JURIST:
		case SACHBEARBEITER_TS:
		case ADMIN_TS:
		case REVISOR:
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT: {
			return MitteilungTeilnehmerTyp.JUGENDAMT;
		}
		default:
			return null;
		}
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	private Mitteilung setMitteilungsStatusIfBerechtigt(
		@Nonnull String mitteilungsId, @Nonnull MitteilungStatus statusRequested,
		@Nonnull MitteilungStatus... statusRequired) {
		Optional<Mitteilung> mitteilungOptional = findMitteilung(mitteilungsId);
		Mitteilung mitteilung = mitteilungOptional.orElseThrow(() -> new EbeguRuntimeException(
			"setMitteilungsStatusIfBerechtigt",
			"Mitteilung not found"));
		return setMitteilungsStatusIfBerechtigt(mitteilung, statusRequested, statusRequired);
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	private Mitteilung setMitteilungsStatusIfBerechtigt(
		@Nonnull Mitteilung mitteilung, @Nonnull MitteilungStatus statusRequested,
		@Nonnull MitteilungStatus... statusRequired) {
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		if (!Arrays.asList(statusRequired).contains(mitteilung.getMitteilungStatus())) {
			throw new IllegalStateException("Mitteilung "
				+ mitteilung.getId()
				+ " ist im falschen Status: "
				+ mitteilung.getMitteilungStatus()
				+ " anstatt "
				+ Arrays.toString(statusRequired));
		}
		// Es muss sowohl der EmpfaengerTyp (bei Institution und GS) wie auch das Amt (bei JA und SCH) uebereinstimmen
		UserRole currentUserRole = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		boolean sameEmpfaengerTyp = mitteilung.getEmpfaengerTyp() == getMitteilungTeilnehmerTypForCurrentUser();
		boolean sameAmt = mitteilung.getEmpfaengerAmt() == currentUserRole.getAmt();
		if (sameEmpfaengerTyp && sameAmt) {
			mitteilung.setMitteilungStatus(statusRequested);
		}
		return persistence.merge(mitteilung);
	}
}


