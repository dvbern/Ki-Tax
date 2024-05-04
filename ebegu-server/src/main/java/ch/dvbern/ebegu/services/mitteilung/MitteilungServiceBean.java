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

package ch.dvbern.ebegu.services.mitteilung;

import java.math.BigDecimal;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungTableFilterDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung_;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung_;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.Mitteilung_;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung_;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.entities.SteuerdatenResponse_;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BetreuungspensumAbweichungStatus;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.MessageTypes;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.SearchMode;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.enums.Verantwortung;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.errors.EbeguExistingAntragException;
import ch.dvbern.ebegu.errors.EbeguExistingAntragRuntimeException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.inbox.util.TechnicalUserConfigurationVisitor;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHelper;
import ch.dvbern.ebegu.nesko.utils.KibonAnfrageUtil;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.MitteilungUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.BetreuungsmitteilungPensumMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.DefaultMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.EingewoehnungsPauschaleMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.MahlzeitenVerguenstigungMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.MittagstischMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.SchulergaenzendeBetreuungMessageFactory;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND;
import static ch.dvbern.ebegu.util.betreuungsmitteilung.messages.BetreuungsmitteilungPensumMessageFactory.combine;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Service fuer Mitteilungen
 */
@SuppressWarnings({ "PMD.NcssTypeCount", "PMD.AvoidDuplicateLiterals" })
@Stateless
@Local(MitteilungService.class)
public class MitteilungServiceBean extends AbstractBaseService implements MitteilungService {

	private static final Logger LOG = LoggerFactory.getLogger(MitteilungServiceBean.class.getSimpleName());

	static final TechnicalUserConfigurationVisitor technicalUserConfigVisitor = new TechnicalUserConfigurationVisitor();
	@Inject
	private Persistence persistence;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private MailService mailService;

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

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Override
	@Nonnull
	public Mitteilung sendMitteilung(@Nonnull Mitteilung mitteilung) {
		Objects.requireNonNull(mitteilung);

		checkMitteilungDataConsistency(mitteilung);

		if (MitteilungStatus.NEU != mitteilung.getMitteilungStatus()) {
			throw new IllegalArgumentException("Mitteilung ist nicht im Status ENTWURF und kann nicht gesendet "
					+ "werden");
		}
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		mitteilung.setSentDatum(LocalDateTime.now());

		setSenderAndEmpfaengerAndCheckAuthorization(mitteilung);

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
			throw new EbeguRuntimeException(ebeguConfiguration.getDefaultLogLevel(),
					"sendMitteilung",
					message,
					ErrorCodeEnum.ERROR_MAIL,
					e);
		}

		return persistence.merge(mitteilung);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkMitteilungDataConsistency(@Nonnull Mitteilung mitteilung) {
		if (!mitteilung.isNew()) {
			Mitteilung persistedMitteilung =
					findMitteilung(mitteilung.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
							"sendMitteilung",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
							"MitteilungId invalid: " + mitteilung.getId()));

			// Die gespeicherte wie auch die uebergebene Mitteilung muss im Status NEU sein
			if (MitteilungStatus.NEU != persistedMitteilung.getMitteilungStatus()) {
				throw new IllegalArgumentException(
						"Mitteilung aus DB ist nicht im Status ENTWURF und kann nicht gesendet werden");
			}
			if (!persistedMitteilung.getSender().equals(mitteilung.getSender())) {
				throw new IllegalArgumentException("Mitteilung aus DB hat anderen Sender gesetzt");
			}
		}
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void setSenderAndEmpfaengerAndCheckAuthorization(@Nonnull Mitteilung mitteilung) {
		Optional<Benutzer> currentBenutzer = benutzerService.getCurrentBenutzer();
		//wenn man direkt aus Kafka Event liest sind man nicht eingeloggt, aber man hat der Rolle SUPER_ADMIN
		if (currentBenutzer.isEmpty() && !principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			throw new IllegalStateException("Benutzer ist nicht eingeloggt!");
		}
		if (currentBenutzer.isEmpty() && principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			mitteilung.setEmpfaenger(getEmpfaengerBeiMitteilungAnGemeinde(mitteilung));
		} else if (currentBenutzer.isPresent()) {
			switch (currentBenutzer.get().getRole()) {
			case GESUCHSTELLER: {
				mitteilung.setEmpfaenger(getEmpfaengerBeiMitteilungAnGemeinde(mitteilung));
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
				break;
			}
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT: {
				mitteilung.setEmpfaenger(getEmpfaengerBeiMitteilungAnGemeinde(mitteilung));
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
				if (mitteilung.getInstitution() != null) {
					//Bei Institution Mitteilungen sollen schon der Institution ID Bestimmt sein
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.INSTITUTION);
				} else if (mitteilung.getFall().getSozialdienstFall() != null) {
					// Sozialdienst hat kein Empfanger
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.SOZIALDIENST);
				} else {
					mitteilung.setEmpfaenger(mitteilung.getFall().getBesitzer());
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
				}
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				break;
			}
			case ADMIN_SOZIALDIENST:
			case SACHBEARBEITER_SOZIALDIENST: {
				mitteilung.setEmpfaenger(getEmpfaengerBeiMitteilungAnGemeinde(mitteilung));
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.SOZIALDIENST);
				break;
			}
			case SUPER_ADMIN: {
				// Superadmin kann als verschiedene Rollen Mitteilungen schicken
				if (mitteilung instanceof Betreuungsmitteilung) {
					mitteilung.setEmpfaenger(getEmpfaengerBeiMitteilungAnGemeinde(mitteilung));
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
				} else if (mitteilung.getBetreuung() != null) {
					//Die Betreuung ist gesetzt bei Mitteilungen an die Gemeinde, so ruckwirkend wird auch sein
					//es gibt keine Benutzer als empfanger
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.INSTITUTION);
				} else if (mitteilung.getInstitution() != null) {
					//Bei Institution Mitteilungen sollen schon der Institution ID Bestimmt sein
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.INSTITUTION);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				} else if (mitteilung.getFall().getSozialdienstFall() != null) {
					// Sozialdienst hat kein Empfanger
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.SOZIALDIENST);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				} else {
					mitteilung.setEmpfaenger(mitteilung.getFall().getBesitzer());
					mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				}
			}
			}
			authorizer.checkWriteAuthorizationMitteilung(mitteilung);
			// Der Sender darf erst nach dem CHECK gesetzt werden! Sonst kann eine Mitteilung gekaptert werden
			mitteilung.setSender(currentBenutzer.get());
		}
	}

	private Benutzer getEmpfaengerBeiMitteilungAnGemeinde(@Nonnull Mitteilung mitteilung) {
		Benutzer empfaenger = mitteilung.getDossier().getVerantwortlicherBG();
		if (empfaenger == null) {
			empfaenger = mitteilung.getDossier().getVerantwortlicherTS();
		}
		if (empfaenger == null) {
			String gemeindeId = mitteilung.getDossier().getGemeinde().getId();
			Optional<GemeindeStammdaten> stammdatenOptional =
					gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId);
			if (stammdatenOptional.isPresent()) {
				// Wir kontrollieren bei den Mitteilungen explizit nicht, ob die Rolle stimmt!
				// Wir nehmen den Allgemeinen Default, weil wir auf der Mitteilung kein Gesuch haben
				// und daher nicht wissen, ob es ein reines BG- oder TS-Gesuch ist
				empfaenger = stammdatenOptional.get().getDefaultBenutzer();
			}
		}
		if (empfaenger == null) {
			throw new EbeguRuntimeException("getEmpfaengerBeiMitteilungAnGemeinde",
					ErrorCodeEnum.ERROR_VERANTWORTLICHER_NOT_FOUND,
					mitteilung.getId());
		}
		return empfaenger;
	}

	@Nonnull
	@Override
	public Mitteilung setMitteilungGelesen(@Nonnull String mitteilungsId) {
		return setMitteilungsStatusIfBerechtigt(mitteilungsId,
				MitteilungStatus.GELESEN,
				MitteilungStatus.NEU,
				MitteilungStatus.ERLEDIGT,
				MitteilungStatus.IGNORIERT);
	}

	@Nonnull
	@Override
	public Mitteilung setMitteilungErledigt(@Nonnull String mitteilungsId) {
		return setMitteilungsStatusIfBerechtigt(mitteilungsId,
				MitteilungStatus.ERLEDIGT,
				MitteilungStatus.GELESEN,
				MitteilungStatus.NEU);
	}

	@Nonnull
	@Override
	public Mitteilung setMitteilungUngelesen(@Nonnull String mitteilungsId) {
		return setMitteilungsStatusIfBerechtigt(mitteilungsId, MitteilungStatus.NEU, MitteilungStatus.GELESEN);
	}

	@Nonnull
	@Override
	public Mitteilung setMitteilungIgnoriert(@Nonnull String mitteilungsId) {
		return setMitteilungsStatusIfBerechtigt(mitteilungsId,
				MitteilungStatus.IGNORIERT,
				MitteilungStatus.GELESEN,
				MitteilungStatus.NEU);
	}

	@Nonnull
	@Override
	public Optional<Mitteilung> findMitteilung(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Mitteilung mitteilung = persistence.find(Mitteilung.class, key);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		return Optional.ofNullable(mitteilung);
	}

	@Override
	@Nonnull
	public Optional<Betreuungsmitteilung> findBetreuungsmitteilung(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Betreuungsmitteilung mitteilung = persistence.find(Betreuungsmitteilung.class, key);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		return Optional.ofNullable(mitteilung);
	}

	@Nonnull
	@Override
	public Optional<NeueVeranlagungsMitteilung> findVeranlagungsMitteilungById(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		NeueVeranlagungsMitteilung mitteilung = persistence.find(NeueVeranlagungsMitteilung.class, key);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		return Optional.ofNullable(mitteilung);
	}

	@Override
	public void removeOffeneBetreuungsmitteilungenForBetreuung(Betreuung betreuung) {
		Collection<Betreuungsmitteilung> existing = findOffeneBetreuungsmitteilungenForBetreuung(betreuung);
		existing.forEach(e -> persistence.remove(e));
	}

	@Nonnull
	@Override
	public Collection<Betreuungsmitteilung> findOffeneBetreuungsmitteilungenForBetreuung(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<Betreuung> betreuungParam = cb.parameter(Betreuung.class, "betreuunParam");

		Predicate predicateBetreuung = cb.equal(root.get(Mitteilung_.betreuung), betreuungParam);
		predicates.add(predicateBetreuung);

		final Predicate predicateNotApplied = cb.isFalse(root.get(Betreuungsmitteilung_.APPLIED));
		predicates.add(predicateNotApplied);

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<Betreuungsmitteilung> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("betreuunParam", betreuung);
		return tq.getResultList();
	}

	@Nonnull
	@Override
	public Collection<Betreuungsmitteilung> findOffeneBetreuungsmitteilungenByRefNr(@Nonnull String refNr) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();

		CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);

		Join<Betreuungsmitteilung, Betreuung> betreuungJoin = root.join(Mitteilung_.betreuung);

		ParameterExpression<String> refNrParam = cb.parameter(String.class, AbstractPlatz_.REF_NR);

		query.where(
			cb.equal(betreuungJoin.get(AbstractPlatz_.refNr), refNrParam),
			cb.isFalse(root.get(Betreuungsmitteilung_.applied))
		);

		return persistence.getEntityManager().createQuery(query)
			.setParameter(refNrParam, refNr)
			.getResultList();
	}

	@Nonnull
	@Override
	public Collection<Betreuungsmitteilung> findAllBetreuungsmitteilungenForBetreuung(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<Betreuung> betreuungParam = cb.parameter(Betreuung.class, "betreuunParam");

		Predicate predicateLinkedObject = cb.equal(root.get(Mitteilung_.betreuung), betreuungParam);
		predicates.add(predicateLinkedObject);

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<Betreuungsmitteilung> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("betreuunParam", betreuung);
		return tq.getResultList();
	}

	@Nonnull
	@Override
	public Collection<BetreuungspensumAbweichung> findAllBetreuungspensumAbweichungenForBetreuung(
			@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungspensumAbweichung> query = cb.createQuery(BetreuungspensumAbweichung.class);
		Root<BetreuungspensumAbweichung> root = query.from(BetreuungspensumAbweichung.class);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<Betreuung> betreuungParam = cb.parameter(Betreuung.class, "betreuunParam");

		Predicate predicateLinkedObject = cb.equal(root.get(BetreuungspensumAbweichung_.betreuung), betreuungParam);
		predicates.add(predicateLinkedObject);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<BetreuungspensumAbweichung> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("betreuunParam", betreuung);
		return tq.getResultList();
	}

	@Nonnull
	@Override
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
	public Collection<Mitteilung> getMitteilungenForCurrentRolle(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier, "dossier muss gesetzt sein");
		authorizer.checkReadAuthorizationDossier(dossier);
		return getMitteilungenForCurrentRolle(Mitteilung_.dossier, dossier).stream()
				.filter(this::isMitteilungReadableForInstitution)
				.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public Collection<Mitteilung> getMitteilungenForCurrentRolle(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung muss gesetzt sein");
		authorizer.checkReadAuthorization(betreuung);
		return getMitteilungenForCurrentRolle(Mitteilung_.betreuung, betreuung);
	}

	private <T> Collection<Mitteilung> getMitteilungenForCurrentRolle(
			SingularAttribute<Mitteilung, T> attribute, @Nonnull T linkedEntity) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateLinkedObject = cb.equal(root.get(attribute), linkedEntity);
		predicates.add(predicateLinkedObject);

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

	private void setActiveAndRolePredicates(
			CriteriaBuilder cb,
			SetJoin<Benutzer, Berechtigung> joinSenderBerechtigungen,
			List<Predicate> predicates,
			List<UserRole> jugendamtRoles) {
		Predicate predicateActive = cb.between(cb.literal(LocalDate.now()),
				joinSenderBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
				joinSenderBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
		Predicate predicateSenderGleichesAmt = joinSenderBerechtigungen.get(Berechtigung_.role).in(jugendamtRoles);
		predicates.add(cb.and(predicateActive, predicateSenderGleichesAmt));
	}

	@Override
	public void removeMitteilung(@Nonnull Mitteilung mitteilung) {
		Objects.requireNonNull(mitteilung);
		authorizer.checkWriteAuthorizationMitteilung(mitteilung);
		persistence.remove(mitteilung);
	}

	@Override
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
	public void removeAllBetreuungMitteilungenForGesuch(@Nonnull Gesuch gesuch) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);
		final Join<Betreuung, KindContainer> join =
				root.join(Mitteilung_.betreuung, JoinType.LEFT).join(AbstractPlatz_.kind, JoinType.LEFT);

		Predicate gesuchPred = cb.equal(join.get(KindContainer_.gesuch), gesuch);
		Predicate withBetreuungPred = cb.isNotNull(root.get(Mitteilung_.betreuung));

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, gesuchPred, withBetreuungPred));
		final List<Mitteilung> mitteilungen = persistence.getCriteriaResults(query);

		// Wir pruefen nur die grundsaetzliche Schreibberechtigung fuer das Gesuch
		authorizer.checkWriteAuthorization(gesuch);

		for (Mitteilung mitteilung : mitteilungen) {
			persistence.remove(Mitteilung.class, mitteilung.getId());
		}
	}

	@Override
	public void removeAllBetreuungspensumAbweichungenForGesuch(@Nonnull Gesuch gesuch) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungspensumAbweichung> query = cb.createQuery(BetreuungspensumAbweichung.class);
		Root<BetreuungspensumAbweichung> root = query.from(BetreuungspensumAbweichung.class);
		final Join<Betreuung, KindContainer> join = root.join(BetreuungspensumAbweichung_.betreuung, JoinType.LEFT)
				.join(AbstractPlatz_.kind, JoinType.LEFT);

		Predicate gesuchPred = cb.equal(join.get(KindContainer_.gesuch), gesuch);
		query.where(gesuchPred);
		final List<BetreuungspensumAbweichung> abweichungen = persistence.getCriteriaResults(query);

		for (BetreuungspensumAbweichung abweichung : abweichungen) {
			authorizer.checkWriteAuthorization(abweichung.getBetreuung());
			persistence.remove(BetreuungspensumAbweichung.class, abweichung.getId());
		}
	}

	@Nonnull
	@Override
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

		predicates.add(getPredicateEmpfaengerMitteilungTyp(cb, root));

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		List<Mitteilung> mitteilungen = persistence.getCriteriaResults(query);
		authorizer.checkReadAuthorizationMitteilungen(mitteilungen);
		return mitteilungen;
	}

	@Override
	@Nonnull
	public Long getAmountNewMitteilungenForCurrentBenutzer() {
		Benutzer loggedInBenutzer = benutzerService.getCurrentBenutzer()
				.orElseThrow(() -> new EbeguRuntimeException(
						"getAmountNewMitteilungenForCurrentBenutzer",
						"No User is logged in"));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);

		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateNew = cb.equal(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.NEU);
		predicates.add(predicateNew);

		if (loggedInBenutzer.getRole().isRoleSozialdienstabhaengig()) {
			predicates.add(countNewMitteilungenPredicatesForSozialdienstBenutzer(loggedInBenutzer, cb, root));

			predicates.add(getPredicateEmpfaengerMitteilungTyp(cb, root));

		} else if (loggedInBenutzer.getRole().isRoleTraegerschaftInstitution()) {
			predicates.add(countNewMitteilungenPredicatesForInstitutionBenutzer(loggedInBenutzer, cb, root));

			predicates.add(getPredicateEmpfaengerMitteilungTyp(cb, root));

		} else {
			predicates.add(cb.equal(root.get(Mitteilung_.empfaenger), loggedInBenutzer));
		}
		query.select(cb.countDistinct(root));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaSingleResult(query);
	}

	private Predicate countNewMitteilungenPredicatesForSozialdienstBenutzer(
			@Nonnull Benutzer loggedInBenutzer, CriteriaBuilder cb, Root<Mitteilung> root) {

		Join<Mitteilung, Dossier> joinDossier = root.join(Mitteilung_.dossier, JoinType.INNER);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall);
		Join<Fall, SozialdienstFall> joinSozialdienst = joinFall.join(Fall_.sozialdienstFall, JoinType.LEFT);

		return cb.equal(joinSozialdienst.get(SozialdienstFall_.sozialdienst), loggedInBenutzer.getSozialdienst());
	}

	private Predicate countNewMitteilungenPredicatesForInstitutionBenutzer(
			@Nonnull Benutzer loggedInBenutzer, CriteriaBuilder cb, Root<Mitteilung> root) {
		Predicate institution = null;
		if (loggedInBenutzer.getRole() == UserRole.ADMIN_INSTITUTION
				|| loggedInBenutzer.getRole() == UserRole.SACHBEARBEITER_INSTITUTION) {
			institution = cb.equal(root.get(Mitteilung_.institution), loggedInBenutzer.getInstitution());
		} else {
			Join<Mitteilung, Institution> joinInstitution = root.join(Mitteilung_.institution, JoinType.LEFT);
			institution =
					cb.equal(joinInstitution.get(Institution_.traegerschaft), loggedInBenutzer.getTraegerschaft());
		}

		return institution;
	}

	private Predicate getPredicateEmpfaengerMitteilungTyp(CriteriaBuilder cb, Root<Mitteilung> root) {
		MitteilungTeilnehmerTyp mitteilungTeilnehmerTyp = getMitteilungTeilnehmerTypForCurrentUser();
		return cb.equal(root.get(Mitteilung_.empfaengerTyp), mitteilungTeilnehmerTyp);
	}

	@Override
	public void replaceBetreungsmitteilungen(@Valid @Nonnull Betreuungsmitteilung betreuungsmitteilung) {
		removeOffeneBetreuungsmitteilungenForBetreuung(requireNonNull(betreuungsmitteilung.getBetreuung()));
		sendBetreuungsmitteilung(betreuungsmitteilung);
	}

	@Nonnull
	@Override
	@CanIgnoreReturnValue
	public Betreuungsmitteilung sendBetreuungsmitteilung(@Valid @Nonnull Betreuungsmitteilung betreuungsmitteilung) {
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

		setSenderAndEmpfaengerAndCheckAuthorization(betreuungsmitteilung);

		// A Betreuungsmitteilung is created and sent, therefore persist and not merge
		return persistence.persist(betreuungsmitteilung);
	}

	@Nonnull
	@Override
	public Gesuch applyBetreuungsmitteilung(@Nonnull Betreuungsmitteilung mitteilung) {
		try {
			Objects.requireNonNull(mitteilung.getBetreuung());
			return doApplymitteilung(mitteilung, mitteilung.getBetreuung().extractGesuch());
		} catch (EbeguExistingAntragException e) {
			throw new EbeguExistingAntragRuntimeException("applyBetreuungsmitteilung",
					e.getErrorCodeEnum(),
					e,
					e.getDossierId(),
					e.getGesuchperiodeId());
		} catch (EbeguException e) {
			throw new EbeguRuntimeException("applyBetreuungsmitteilung",
					"error while applying betreuungsmitteilungen",
					e.getErrorCodeEnum(),
					e);
		}
	}

	@Nonnull
	@Override
	public Gesuch neueVeranlagungssmitteilungBearbeiten(@Nonnull NeueVeranlagungsMitteilung mitteilung) {
		try {
			Objects.requireNonNull(mitteilung.getSteuerdatenResponse().getKibonAntragId());
			Gesuch gesuch = gesuchService.findGesuch(mitteilung.getSteuerdatenResponse().getKibonAntragId())
					.orElseThrow(() -> new EbeguException("neueVeranlagungssmitteilungBearbeiten",
							ERROR_ENTITY_NOT_FOUND,
							null,
							mitteilung.getSteuerdatenResponse().getKibonAntragId()));
			return doApplymitteilung(mitteilung, gesuch);
		} catch (EbeguExistingAntragException e) {
			throw new EbeguExistingAntragRuntimeException("neueVeranlagungssmitteilungBearbeiten",
					e.getErrorCodeEnum(),
					e,
					e.getDossierId(),
					e.getGesuchperiodeId());
		} catch (EbeguException e) {
			throw new EbeguRuntimeException("neueVeranlagungssmitteilungBearbeiten",
					e.getErrorCodeEnum(),
					e);
		}
	}

	@Nonnull
	private Gesuch doApplymitteilung(@Nonnull Mitteilung mitteilung, @Nonnull Gesuch gesuch)
			throws EbeguException, EbeguExistingAntragException {
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		if (gesuch.getStatus() == AntragStatus.FREIGEGEBEN || gesuch.getStatus() == AntragStatus.FREIGABEQUITTUNG) {
			throw new EbeguExistingAntragException("doApplymitteilung",
					ErrorCodeEnum.ERROR_NOCH_NICHT_FREIGEGEBENE_ANTRAG,
					gesuch.getDossier().getId(),
					gesuch.getGesuchsperiode().getId());
		}

		// neustes Gesuch lesen
		final Optional<Gesuch> neustesGesuchOpt;
		try {
			neustesGesuchOpt = gesuchService.getNeustesGesuchFuerGesuch(gesuch);
		} catch (EJBTransactionRolledbackException exception) {
			// Wenn der Sachbearbeiter den neusten Antrag nicht lesen darf ist es ein noch nicht freigegebener ONLINE
			// Antrag
			if (exception.getCause().getClass().equals(EJBAccessException.class)) {
				throw new EbeguExistingAntragException("doApplymitteilung",
						ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION,
						gesuch.getDossier().getId(),
						gesuch.getGesuchsperiode().getId(),
						exception);
			}
			throw exception;
		}
		if (neustesGesuchOpt.isPresent()) {
			final Gesuch neustesGesuch = neustesGesuchOpt.get();
			// Sobald irgendein Antrag dieser Periode geperrt ist, darf keine Mutationsmeldungs-Mutation erstellt
			// werden!
			if (neustesGesuch.isGesperrtWegenBeschwerde()) {
				throw new EbeguException("doApplymitteilung",
						ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_FALL_GESPERRT,
						neustesGesuch.getId());
			}
			if (AntragStatus.VERFUEGEN == neustesGesuch.getStatus()) {
				throw new EbeguException("doApplymitteilung",
						ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_STATUS_VERFUEGEN,
						neustesGesuch.getId());
			}
			if (!AntragStatus.getVerfuegtAbgeschlossenIgnoriertAndSTVStates().contains(neustesGesuch.getStatus())
					&& neustesGesuch.isMutation()) {
				// veranlagungsmitteilungen dürfen nicht zu bestehender Mutation hinzugefügt werden
				if (mitteilung instanceof NeueVeranlagungsMitteilung) {
					throw new EbeguException("doApplymitteilung",
							ErrorCodeEnum.ERROR_EXISTING_MUTATION_VERANLAGUNGSMITTEILUNG,
							neustesGesuch.getId());
				}
				// aenderungen der bestehenden, offenen Mutation hinzufuegen (wenn wir hier sind muss es sich
				// um ein PAPIER) Antrag handeln
				authorizer.checkWriteAuthorization(neustesGesuch);
				applyMitteilungToMutation(neustesGesuch, mitteilung);
				return neustesGesuch;
			}
			if (AntragStatus.getVerfuegtAbgeschlossenIgnoriertAndSTVStates().contains(neustesGesuch.getStatus())) {
				// create Mutation if there is currently no Mutation
				Gesuch mutation = Gesuch.createMutation(gesuch.getDossier(), neustesGesuch.getGesuchsperiode(), LocalDate.now());
				mutation = gesuchService.createGesuch(mutation);
				authorizer.checkWriteAuthorization(mutation);
				if (mitteilung instanceof NeueVeranlagungsMitteilung) {
					setSteuerdatenResponseInMutationTemp(gesuch, mutation);
				}
				applyMitteilungToMutation(mutation, mitteilung);
				return mutation;
			}
			throw new EbeguException("doApplymitteilung",
					ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_GESUCH_NICHT_FREIGEGEBEN_INBEARBEITUNG,
					neustesGesuch.getId());
		}
		return gesuch;
	}

	private void setSteuerdatenResponseInMutationTemp(Gesuch gesuch, Gesuch mutation) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Objects.requireNonNull(mutation.getGesuchsteller1());
		Objects.requireNonNull(mutation.getGesuchsteller1().getFinanzielleSituationContainer());
		mutation.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setSteuerdatenResponse(gesuch.getGesuchsteller1()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA()
						.getSteuerdatenResponse());
		if (mutation.getGesuchsteller2() != null
				&& mutation.getGesuchsteller2().getFinanzielleSituationContainer() != null
				&& gesuch.getGesuchsteller2() != null
				&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null) {
			mutation.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA()
					.setSteuerdatenResponse(gesuch.getGesuchsteller2()
							.getFinanzielleSituationContainer()
							.getFinanzielleSituationJA()
							.getSteuerdatenResponse());
		}
	}

	@Nonnull
	@Override
	public Optional<Betreuungsmitteilung> findNewestBetreuungsmitteilung(@Nonnull String betreuungId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);

		Predicate predicateLinkedObject =
				cb.equal(root.get(Mitteilung_.betreuung).get(AbstractEntity_.id), betreuungId);
		Predicate predicateNotErledigt =
				cb.equal(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.ERLEDIGT).not();

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
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
	public Mitteilung mitteilungWeiterleiten(@Nonnull String mitteilungId, @Nonnull String userName) {
		Mitteilung mitteilung = findMitteilung(mitteilungId).orElseThrow(() -> new EbeguRuntimeException(
				"mitteilungUebergebenAnJugendamt",
				"Mitteilung not found"));

		authorizer.checkReadAuthorizationMitteilung(mitteilung);

		Optional<Benutzer> benutzerOptional =
				benutzerService.findBenutzer(userName, mitteilung.getDossier().getFall().getMandant());

		if (benutzerOptional.isPresent()) {
			// Den VerantwortlichenJA als Empfänger setzen
			mitteilung.setEmpfaenger(benutzerOptional.get());
			mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		} else {
			throw new EbeguRuntimeException("mitteilungWeiterleiten",
					ErrorCodeEnum.ERROR_EMPFAENGER_NOT_FOUND,
					mitteilung.getId());
		}

		return persistence.merge(mitteilung);
	}

	@Override
	public void createMutationsmeldungAbweichungen(@Nonnull Betreuungsmitteilung mitteilung, @Nonnull Betreuung betreuung) {

		// convert BetreuungspensumAbweichung to MitteilungPensum
		// (1) Zusammenfuegen der bestehenden Pensen mit den evtl. hinzugefuegten Abweichungen. Resultat ist ein Pensum
		// pro Monat mit entweder dem vertraglichen oder dem abgewichenen Pensum ODER 0.
		List<BetreuungspensumAbweichung> initialAbweichungen =
				betreuung.fillAbweichungen(betreuungService.getMultiplierForAbweichnungen(betreuung));
		// (2) Da die Eingewöhnungspauschale in den Abweichungen readonly ist, müssen wir sie von den Readonly-Attributen
		// übernehmen
		initialAbweichungen.forEach(abweichung -> {
			if (abweichung.getEingewoehnungPauschale() == null && abweichung.getVertraglicheEingewoehnungPauschale() != null) {
				abweichung.setEingewoehnungPauschale(abweichung.getVertraglicheEingewoehnungPauschale());
			}
		});
		// (3) Die Abschnitte werden zu BetreuungsMitteilungspensen konvertiert.
		Set<BetreuungsmitteilungPensum> pensenFromAbweichungen = initialAbweichungen.stream()
			.filter(abweichung -> abweichung.getVertraglichesPensum() != null)
				.map(abweichung -> abweichung.convertAbweichungToMitteilungPensum(mitteilung))
				.collect(Collectors.toSet());

		mitteilung.setBetreuungspensen(pensenFromAbweichungen);

		Locale locale = EbeguUtil.extractKorrespondenzsprache(betreuung.extractGesuch(), gemeindeService).getLocale();

		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguEntityNotFoundException("sendBetreuungsmitteilung"));

		MitteilungUtil.initializeBetreuungsmitteilung(mitteilung, betreuung, currentBenutzer, locale);
		mitteilung.setMessage(createNachrichtForMutationsmeldung(mitteilung, pensenFromAbweichungen, locale));

		sendBetreuungsmitteilung(mitteilung);
	}

	@Override
	public boolean hasInstitutionOffeneMitteilungen(Institution institution) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
		Root<Mitteilung> root = query.from(Mitteilung.class);

		Predicate predicateLinkedObject = cb.equal(root.get(Mitteilung_.institution), institution);
		Predicate predicateNeu = cb.equal(root.get(Mitteilung_.mitteilungStatus), MitteilungStatus.NEU);
		Predicate predicateEmpfaenger =
				cb.equal(root.get(Mitteilung_.empfaengerTyp), MitteilungTeilnehmerTyp.INSTITUTION);

		query.where(predicateLinkedObject, predicateNeu, predicateEmpfaenger);

		final List<Mitteilung> result = persistence.getCriteriaResults(query);
		return !result.isEmpty();
	}

	@Override
	public void adaptOffeneMutationsmitteilungenToInstiGueltigkeitChange(
			@Nonnull Institution institution, @Nonnull DateRange gueltigkeit) {
		Collection<Betreuungsmitteilung> offeneMutationsmitteilungenForInstitution =
				findAllBetreuungsMitteilungenForInstitution(institution);

		offeneMutationsmitteilungenForInstitution.forEach(mitteilung -> {
			Set<BetreuungsmitteilungPensum> betreuungspensen = betreuungService.capBetreuungspensenToGueltigkeit(
				mitteilung.getBetreuungspensen(),
				gueltigkeit);
			mitteilung.setBetreuungspensen(betreuungspensen);

			if (betreuungspensen.isEmpty()) {
				persistence.remove(mitteilung);
			} else {
				Objects.requireNonNull(mitteilung.getBetreuung());
				final Locale locale =
					EbeguUtil.extractKorrespondenzsprache(mitteilung.getBetreuung().extractGesuch(), gemeindeService).getLocale();
				mitteilung.setMessage(createNachrichtForMutationsmeldung(mitteilung, betreuungspensen, locale));
				persistence.merge(mitteilung);
			}
		});
	}

	/**
	 * Betreuung war nicht storniert -> ok
	 * Betreuung storniert aber keine andere fuer dasselbe Kind und Institution -> ok
	 * Betreuung storniert und andere mit Pensum -> ko
	 * Betreuung storniert mit anderen die auch storniert sind ->
	 * if a mutation Mitteilung exist for this kind and Institution -> ko
	 * otherwise => ok
	 */
	@Override
	public boolean isBetreuungGueltigForMutation(Betreuung betreuung) {
		// Stornieren set all Pensum to 0, if there's a pensum who's not set to 0 it means it's gueltig for Mutation
		if (!(betreuung.getBetreuungspensumContainers()
				.stream()
				.noneMatch(betreuungspensumContainer -> betreuungspensumContainer.getBetreuungspensumJA()
						.getPensum()
						.compareTo(BigDecimal.ZERO) != 0))) {
			return true;
		}
		//Check if there is at least one more Betreuung for the same institution and kind
		List<Betreuung> sameInstitutionBetreuungList = betreuung.extractGesuch()
				.extractAllBetreuungen()
				.stream()
				.filter(betreuungFromGesuch -> betreuungFromGesuch.getInstitutionStammdaten()
						.getId()
						.equals(betreuung.getInstitutionStammdaten().getId()) && !betreuungFromGesuch.getId()
						.equals(betreuung.getId()) && betreuungFromGesuch.getKind()
						.getId()
						.equals(betreuung.getKind().getId()))
				.collect(Collectors.toList());
		if (sameInstitutionBetreuungList.isEmpty()) {
			return true;
		}
		// otherwise we need to check if another one got a Pensum
		if (sameInstitutionBetreuungList.stream()
				.map(Betreuung::getBetreuungspensumContainers)
				.filter(betreuungspensumContainers -> !betreuungspensumContainers.stream()
						.filter(betreuungspensumContainer -> betreuungspensumContainer.getBetreuungspensumJA()
								.getPensum()
								.compareTo(BigDecimal.ZERO) != 0)
						.collect(Collectors.toList())
						.isEmpty())
				.collect(Collectors.toList())
				.isEmpty()) { //if none are found with a pensum set it means they are alle storniert or set to 0, we
			// need to check if
			// there's already a
			// mutationmitteilung for one of them, if none then we can open a new one
			return sameInstitutionBetreuungList.stream()
					.filter(betreuungToCheck -> !findOffeneBetreuungsmitteilungenForBetreuung(betreuungToCheck).isEmpty())
					.collect(Collectors.toList())
					.isEmpty();
		}
		return false;
	}

	@Nonnull
	@Override
	public NeueVeranlagungsMitteilung sendNeueVeranlagungsmitteilung(
			@Nonnull NeueVeranlagungsMitteilung neueVeranlagungsMitteilung) {
		Objects.requireNonNull(neueVeranlagungsMitteilung);

		neueVeranlagungsMitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		neueVeranlagungsMitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		neueVeranlagungsMitteilung.setSender(getTechnicalKibonBenutzer(neueVeranlagungsMitteilung.getDossier()));
		neueVeranlagungsMitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		neueVeranlagungsMitteilung.setSentDatum(LocalDateTime.now());
		neueVeranlagungsMitteilung.setEmpfaenger(getEmpfaengerBeiMitteilungAnGemeinde(neueVeranlagungsMitteilung));

		return persistence.merge(neueVeranlagungsMitteilung);
	}

	@Nonnull
	private Benutzer getTechnicalKibonBenutzer(Dossier dossier) {
		MandantIdentifier mandantIdentifier = dossier.getFall().getMandant().getMandantIdentifier();
		String technicalUserID = technicalUserConfigVisitor.process(mandantIdentifier).getTechnicalUser();

		return benutzerService.findBenutzerById(technicalUserID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(EMPTY, ERROR_ENTITY_NOT_FOUND, technicalUserID));
	}

	@Override
	public String createNachrichtForMutationsmeldung(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Set<BetreuungsmitteilungPensum> changedBetreuungen,
		@Nonnull Locale locale) {

		List<BetreuungsmitteilungPensum> sorted = changedBetreuungen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		BetreuungsmitteilungPensumMessageFactory factory = messageFactory(mitteilung, locale);

		return IntStream.rangeClosed(1, sorted.size())
			.mapToObj(index -> factory.messageForPensum(index, sorted.get(index - 1)))
			.collect(Collectors.joining(StringUtils.LF));
	}

	private BetreuungsmitteilungPensumMessageFactory messageFactory(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Locale locale
	) {
		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		Mandant mandant = betreuung.extractGesuch().extractMandant();

		if (betreuung.isAngebotMittagstisch()) {
			return new MittagstischMessageFactory(mandant, locale);
		}

		boolean mvzEnabled = einstellungService.findEinstellung(
			EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
			betreuung.extractGemeinde(),
			betreuung.extractGesuchsperiode()).getValueAsBoolean();

		Einstellung einstellungAnzeigeTyp = einstellungService.findEinstellung(
			EinstellungKey.PENSUM_ANZEIGE_TYP,
			betreuung.extractGemeinde(),
			betreuung.extractGesuchsperiode());

		BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp = getBetreuungspensumAnzeigeTyp(einstellungAnzeigeTyp);
		BigDecimal multiplier = getMultiplierForMutationsMitteilung(mitteilung, betreuungspensumAnzeigeTyp);

		BetreuungsmitteilungPensumMessageFactory pensumFactory = mvzEnabled ?
			new MahlzeitenVerguenstigungMessageFactory(mandant, locale, betreuungspensumAnzeigeTyp, multiplier) :
			new DefaultMessageFactory(mandant, locale, betreuungspensumAnzeigeTyp, multiplier);

		BetreuungsmitteilungPensumMessageFactory schulergaenzendeBetreuungFactory = showSchulergaenzendeBetreuung(betreuung) ?
			new SchulergaenzendeBetreuungMessageFactory(mandant, locale) :
			BetreuungsmitteilungPensumMessageFactory.empty();

		return combine(
			" ",
			combine(
				", ",
				pensumFactory,
				new EingewoehnungsPauschaleMessageFactory(mandant, locale)
			),
			schulergaenzendeBetreuungFactory
		);
	}

	@Override
	public boolean showSchulergaenzendeBetreuung(Betreuung betreuung) {
		return requireNonNull(betreuung.getKind().getKindJA().getEinschulungTyp()).isEingeschult()
			&& einstellungService.findEinstellung(
			EinstellungKey.SCHULERGAENZENDE_BETREUUNGEN,
			betreuung.extractGemeinde(),
			betreuung.extractGesuchsperiode()).getValueAsBoolean();
	}

	@Nonnull
	private BetreuungspensumAnzeigeTyp getBetreuungspensumAnzeigeTyp(Einstellung einstellungAnzeigeTyp) {
		return BetreuungspensumAnzeigeTyp.valueOf(einstellungAnzeigeTyp.getValue());
	}

	private BigDecimal getMultiplierForMutationsMitteilung(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp
	) {
		if (betreuungspensumAnzeigeTyp != BetreuungspensumAnzeigeTyp.NUR_STUNDEN) {
			return BigDecimal.ONE;
		}

		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		if (betreuung.isAngebotKita()) {
			BigDecimal oeffnungstageKita = einstellungService.findEinstellung(EinstellungKey.OEFFNUNGSTAGE_KITA,
					betreuung.extractGemeinde(),
					betreuung.extractGesuchsperiode()).getValueAsBigDecimal();
			return BetreuungUtil.calculateOeffnungszeitPerMonthProcentual(MathUtil.EXACT.multiply(oeffnungstageKita,
					BetreuungUtil.ANZAHL_STUNDEN_PRO_TAG_KITA));
		}
		BigDecimal oeffnungstageTFO = einstellungService.findEinstellung(EinstellungKey.OEFFNUNGSTAGE_TFO,
				betreuung.extractGemeinde(),
				betreuung.extractGesuchsperiode()).getValueAsBigDecimal();

		BigDecimal oeffnungsstundenTFO = einstellungService.findEinstellung(EinstellungKey.OEFFNUNGSSTUNDEN_TFO,
				betreuung.extractGemeinde(),
				betreuung.extractGesuchsperiode()).getValueAsBigDecimal();

		return MathUtil.DEFAULT.divide(MathUtil.DEFAULT.divide(MathUtil.DEFAULT.multiply(oeffnungstageTFO,
				oeffnungsstundenTFO), new BigDecimal(12)), new BigDecimal(100));
	}

	private Collection<Betreuungsmitteilung> findAllBetreuungsMitteilungenForInstitution(Institution institution) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(Betreuungsmitteilung.class);
		Root<Betreuungsmitteilung> root = query.from(Betreuungsmitteilung.class);

		Join<Betreuungsmitteilung, Betreuung> betreuungJoin = root.join(Mitteilung_.betreuung);
		Join<Betreuung, InstitutionStammdaten> stammdatenJoin =
				betreuungJoin.join(AbstractPlatz_.institutionStammdaten);

		Predicate predicateInstitution = cb.equal(stammdatenJoin.get(InstitutionStammdaten_.institution), institution);
		Predicate notApplied = cb.notEqual(root.get(Betreuungsmitteilung_.APPLIED), true);

		query.where(predicateInstitution, notApplied);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Pair<Long, List<Mitteilung>> searchMitteilungen(
			@Nonnull MitteilungTableFilterDTO mitteilungTableFilterDto,
			@Nonnull Boolean includeClosed) {
		Pair<Long, List<Mitteilung>> result;
		Long countResult = searchMitteilungen(mitteilungTableFilterDto, includeClosed, SearchMode.COUNT).getLeft();
		if (countResult.equals(0L)) {    // no result found
			result = new ImmutablePair<>(0L, Collections.emptyList());
		} else {
			Pair<Long, List<Mitteilung>> searchResult =
					searchMitteilungen(mitteilungTableFilterDto, includeClosed, SearchMode.SEARCH);
			result = new ImmutablePair<>(countResult, searchResult.getRight());
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "PMD.NcssMethodCount" }) // Je nach Abfrage ist es String oder Long
	private Pair<Long, List<Mitteilung>> searchMitteilungen(
			@Nonnull MitteilungTableFilterDTO mitteilungTableFilterDto,
			@Nonnull Boolean includeClosed,
			@Nonnull SearchMode mode) {

		Mandant mandant = principalBean.getMandant();
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
		// Predicates derived from PredicateDTO (Filter coming from client)
		MitteilungPredicateObjectDTO predicateObjectDto = mitteilungTableFilterDto.getSearch().getPredicateObject();

		//prepare predicates
		List<Predicate> predicates = new ArrayList<>();

		// Richtiger Empfangs-Typ. Persoenlicher Empfaenger wird nicht beachtet sondern auf Client mit Filter geloest
		predicates.add(getPredicateEmpfaengerMitteilungTyp(cb, root));

		// Mandant
		Predicate mandantPredicate =
				cb.equal(root.get(Mitteilung_.dossier).get(Dossier_.fall).get(Fall_.MANDANT), mandant);
		predicates.add(mandantPredicate);

		filterGemeinde(user, joinGemeinde, predicates);

		Join<Mitteilung, Benutzer> joinEmpfaenger = null;
		SetJoin<Benutzer, Berechtigung> joinEmpfaengerBerechtigungen = null;
		if (user.getCurrentBerechtigung().getRole().isRoleSozialdienstabhaengig()) {
			if (user.getSozialdienst() != null) {
				Join<Fall, SozialdienstFall> joinSozialdienst = joinFall.join(Fall_.sozialdienstFall, JoinType.LEFT);
				Predicate sozialdienstFall =
						cb.equal(joinSozialdienst.get(SozialdienstFall_.sozialdienst), user.getSozialdienst());
				predicates.add(sozialdienstFall);
			} else {
				throw new EbeguRuntimeException("mitteilungTableFilterDto",
						"Sozialdienst not defined for Sozialdienstuser");
			}
		} else if (user.getCurrentBerechtigung().getRole().isRoleTraegerschaftInstitution()) {
			if (user.getInstitution() != null) {
				Predicate institution = cb.equal(root.get(Mitteilung_.institution), user.getInstitution());
				predicates.add(institution);
			} else {
				Join<Mitteilung, Institution> joinInstitution = root.join(Mitteilung_.institution, JoinType.LEFT);
				Predicate traegerschaft =
						cb.equal(joinInstitution.get(Institution_.traegerschaft), user.getTraegerschaft());
				predicates.add(traegerschaft);
			}
		} else {
			// nur hier definieren, Left join auf dem Root koennen nicht null sein eben wenn nicht vewendet und
			// nullable FK
			joinEmpfaenger = root.join(Mitteilung_.empfaenger, JoinType.LEFT);
			joinEmpfaengerBerechtigungen = joinEmpfaenger.join(Benutzer_.berechtigungen);
		}

		if (predicateObjectDto != null) {

			predicates.add(getMessageTypePredicate(root, predicateObjectDto));

			// sender
			if (predicateObjectDto.getSender() != null) {
				predicates.add(cb.or(cb.like(joinSender.get(Benutzer_.nachname),
								SearchUtil.withWildcards(predicateObjectDto.getSender())),
						cb.like(joinSender.get(Benutzer_.vorname),
								SearchUtil.withWildcards(predicateObjectDto.getSender()))));
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
				predicates.add(cb.or(cb.like(joinBesitzer.get(Benutzer_.nachname),
								SearchUtil.withWildcards(predicateObjectDto.getFamilienName())),
						cb.like(joinBesitzer.get(Benutzer_.vorname),
								SearchUtil.withWildcards(predicateObjectDto.getFamilienName()))));
			}
			// subject
			if (predicateObjectDto.getSubject() != null) {
				predicates.add(cb.like(root.get(Mitteilung_.subject),
						SearchUtil.withWildcards(predicateObjectDto.getSubject())));
			}
			// sentDatum
			if (predicateObjectDto.getSentDatum() != null) {
				try {
					LocalDate searchDate = LocalDate.parse(predicateObjectDto.getSentDatum(),
							Constants.DATE_FORMATTER);
					predicates.add(cb.between(root.get(Mitteilung_.sentDatum),
							searchDate.atStartOfDay(),
							searchDate.plusDays(1).atStartOfDay()));
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Mitteilung geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			// empfaenger
			if (predicateObjectDto.getEmpfaenger() != null && joinEmpfaenger != null) {
				predicates.add(cb.and(cb.equal(
						joinEmpfaenger.get(Benutzer_.fullName),
						predicateObjectDto.getEmpfaenger())));
			}
			if (predicateObjectDto.getEmpfaengerVerantwortung() != null && joinEmpfaengerBerechtigungen != null) {
				Verantwortung verantwortung = Verantwortung.valueOf(predicateObjectDto.getEmpfaengerVerantwortung());
				switch (verantwortung) {
				case VERANTWORTUNG_BG:
					setActiveAndRolePredicates(cb,
							joinEmpfaengerBerechtigungen,
							predicates,
							UserRole.getJugendamtSuperadminRoles());
					break;
				case VERANTWORTUNG_TS:
					setActiveAndRolePredicates(cb,
							joinEmpfaengerBerechtigungen,
							predicates,
							UserRole.getTsAndGemeindeRoles());
					break;
				case VERANTWORTUNG_BG_TS:
					setActiveAndRolePredicates(cb,
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
				Predicate predicateNichtErledigt = cb.not(root.get(Mitteilung_.mitteilungStatus)
						.in(MitteilungStatus.IGNORIERT, MitteilungStatus.ERLEDIGT));
				predicates.add(predicateNichtErledigt);
			}
			// gemeinde
			if (predicateObjectDto.getGemeinde() != null) {

				Predicate gemeindePredicate = cb.equal(joinDossier.get(Dossier_.gemeinde).get(Gemeinde_.name),
						predicateObjectDto.getGemeinde());
				predicates.add(gemeindePredicate);
			}
		}

		// Construct the select- and where-clause
		switch (mode) {
		case SEARCH:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(root.get(AbstractEntity_.id))
					.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			constructOrderByClause(mitteilungTableFilterDto,
					cb,
					query,
					root,
					joinFall,
					joinBesitzer,
					joinSender,
					joinEmpfaenger,
					joinEmpfaengerBerechtigungen,
					user.getMandant());
			break;
		case COUNT:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(cb.countDistinct(root.get(AbstractEntity_.id)))
					.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			break;
		}

		// Prepare and execute the query and build the result
		Pair<Long, List<Mitteilung>> result = null;
		switch (mode) {
		case SEARCH:
			List<String> mitteilungIds =
					persistence.getCriteriaResults(query); //select all ids in order, may contain duplicates
			List<Mitteilung> pagedResult;
			if (mitteilungTableFilterDto.getPagination() != null) {
				int firstIndex = mitteilungTableFilterDto.getPagination().getStart();
				Integer maxresults = mitteilungTableFilterDto.getPagination().getNumber();
				List<String> orderedIdsToLoad =
						SearchUtil.determineDistinctIdsToLoad(mitteilungIds, firstIndex, maxresults);
				pagedResult = findMitteilungen(orderedIdsToLoad);
			} else {
				pagedResult = findMitteilungen(mitteilungIds);
			}
			pagedResult.forEach(m -> authorizer.checkReadAuthorizationMitteilung(m));
			result = new ImmutablePair<>(null, pagedResult);
			break;
		case COUNT:
			Long count = (Long) persistence.getCriteriaSingleResult(query);
			result = new ImmutablePair<>(count, null);
			break;
		}
		return result;
	}

	private static Predicate getMessageTypePredicate(
			Root<Mitteilung> root, MitteilungPredicateObjectDTO predicateObjectDto) {
		List<Class> classes = Arrays.stream(predicateObjectDto.getMessageTypes()).map(messageType -> {
			try {
				return Class.forName("ch.dvbern.ebegu.entities." + getEntityNameForMessageType(messageType));
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage() + ". Using default class Mitteilung");
				return Mitteilung.class;
			}
		}).collect(Collectors.toList());

		return root.type().in(classes);
	}

	private static String getEntityNameForMessageType(MessageTypes messageTypes) {
		switch (messageTypes) {
		case BETREUUNGSMITTEILUNG:
			return "Betreuungsmitteilung";
		case NEUEVERANLAGUNGMITTEILUNG:
			return "NeueVeranlagungsMitteilung";
		case MITTEILUNG:
		default:
			return "Mitteilung";
		}
	}

	private void filterGemeinde(Benutzer user, Join<Dossier, Gemeinde> joinGemeinde, List<Predicate> predicates) {
		if (user.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Collection<Gemeinde> gemeindenForUser = user.extractGemeindenForUser();
			if (!gemeindenForUser.isEmpty()) {
				Predicate inGemeinde = joinGemeinde.in(gemeindenForUser);
				predicates.add(inGemeinde);
			}
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
			@Nullable Join<Mitteilung, Benutzer> joinEmpfaenger,
			@Nullable SetJoin<Benutzer, Berechtigung> joinEmpfaengerBerechtigungen,
			Mandant mandant) {
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
				if (joinEmpfaenger != null) {
					expression = joinEmpfaenger.get(Benutzer_.vorname);
				}
				break;
			case "mitteilungStatus":
				expression = root.get(Mitteilung_.mitteilungStatus);
				break;
			case "empfaengerVerantwortung":
				if (joinEmpfaengerBerechtigungen != null) {
					Predicate predicateActive = cb.between(cb.literal(LocalDate.now()),
							joinEmpfaengerBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit)
									.get(DateRange_.gueltigAb),
							joinEmpfaengerBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit)
									.get(DateRange_.gueltigBis));
					Predicate predicateBg =
							joinEmpfaengerBerechtigungen.get(Berechtigung_.role).in(UserRole.getBgOnlyRoles());
					Predicate predicateTs =
							joinEmpfaengerBerechtigungen.get(Berechtigung_.role).in(UserRole.getTsOnlyRoles());
					Expression<Boolean> isActiveBg = cb.and(predicateActive, predicateBg);
					Expression<Boolean> isActiveTs = cb.and(predicateActive, predicateTs);
					Locale browserSprache = LocaleThreadLocal.get(); // Nur fuer Sortierung!
					String bg = ServerMessageUtil.getMessage(Verantwortung.VERANTWORTUNG_BG.name(),
							browserSprache,
							mandant);
					String ts = ServerMessageUtil.getMessage(Verantwortung.VERANTWORTUNG_TS.name(),
							browserSprache,
							mandant);
					String bg_ts = ServerMessageUtil.getMessage(Verantwortung.VERANTWORTUNG_BG_TS.name(),
							browserSprache,
							mandant);
					expression = cb.selectCase().when(isActiveBg, bg).when(isActiveTs, ts).otherwise(bg_ts);
				}
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

	private List<Mitteilung> findMitteilungen(@Nonnull List<String> mitteilungIds) {
		if (!mitteilungIds.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Mitteilung> query = cb.createQuery(Mitteilung.class);
			Root<Mitteilung> root = query.from(Mitteilung.class);
			Predicate predicate = root.get(AbstractEntity_.id).in(mitteilungIds);
			query.where(predicate);
			//reduce to unique gesuche
			List<Mitteilung> listWithDuplicates = persistence.getCriteriaResults(query);
			LinkedHashSet<Mitteilung> set = new LinkedHashSet<>();
			//richtige reihenfolge beibehalten
			for (String mitteilungId : mitteilungIds) {
				listWithDuplicates.stream()
						.filter(mitteilung -> mitteilung.getId().equals(mitteilungId))
						.findFirst()
						.ifPresent(set::add);
			}
			return new ArrayList<>(set);
		}
		return Collections.emptyList();
	}

	private void applyMitteilungToMutation(
			@Nonnull Gesuch gesuch, @Nonnull Mitteilung mitteilung) throws EbeguException {
		if (mitteilung instanceof Betreuungsmitteilung) {
			Betreuungsmitteilung betreuungsmitteilung = (Betreuungsmitteilung) mitteilung;
			applyBetreuungsmitteilungToMutation(gesuch, betreuungsmitteilung);
		} else if (mitteilung instanceof NeueVeranlagungsMitteilung) {
			NeueVeranlagungsMitteilung neueVeranlagungsMitteilung = (NeueVeranlagungsMitteilung) mitteilung;
			neueVeranlagungsMitteilungImAntragErsetzen(gesuch, neueVeranlagungsMitteilung);
		}
	}

	private void neueVeranlagungsMitteilungImAntragErsetzen(
			@Nonnull Gesuch gesuch, @Nonnull NeueVeranlagungsMitteilung mitteilung) throws EbeguException {
		Objects.requireNonNull(mitteilung.getSteuerdatenResponse());
		Objects.requireNonNull(mitteilung.getSteuerdatenResponse().getZpvNrAntragsteller());
		Objects.requireNonNull(mitteilung.getSteuerdatenResponse().getGeburtsdatumAntragsteller());
		authorizer.checkWriteAuthorization(gesuch);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);

		if (!KibonAnfrageUtil.hasGesuchSteuerdatenResponseWithZpvNummer(gesuch,
			mitteilung.getSteuerdatenResponse().getZpvNrAntragsteller())) {
			throw new EbeguRuntimeException(
					"neueVeranlagungsMitteilungImAntragErsetzen",
					"Die neue Veranlagung konnte nicht mit einem gueltigen Antragstellenden verlinkt werden.");
		}

		GesuchstellerTyp gesuchstellerTyp = KibonAnfrageUtil.getGesuchstellerTypByGeburtsdatum(gesuch,
				mitteilung.getSteuerdatenResponse().getGeburtsdatumAntragsteller());

		if (gesuchstellerTyp == null) {
			throw new EbeguRuntimeException(
					"neueVeranlagungsMitteilungImAntragErsetzen",
					"Die neue Veranlagung konnte nicht mit einem gueltigen Antragstellenden verlinkt werden.");
		}

		// status muss bei Veranlagungsmitteilung immer rechtskräftig sein. Prüfungen wurden beim Erstellen der
		// Veranlagungsmitteilungen gemacht.
		if (mitteilung.getSteuerdatenResponse().getVeranlagungsstand() != Veranlagungsstand.RECHTSKRAEFTIG) {
			throw new EbeguRuntimeException("neueVeranlagungsMitteilungImAntragErsetzen",
					"Veranlagungsstand muss rechtskräftig sein");
		}

		KibonAnfrageContext kibonAnfrageContext = new KibonAnfrageContext(
				gesuch,
				gesuchstellerTyp,
				KibonAnfrageUtil.getZpvFromBesitzer(gesuch));
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.RECHTSKRAEFTIG);

		if (!kibonAnfrageContext.isSteuerZugriffErlaubt()) {
			throw new EbeguException("neueVeranlagungsMitteilungImAntragErsetzen",
					ErrorCodeEnum.ERROR_FIN_SIT_MANUELLE_EINGABE,
					gesuch.getId());
		}

		// GEMEINSAME STEUERERKLÄRUNG (VERHEIRATET, ...)
		if (kibonAnfrageContext.hasGS2() && kibonAnfrageContext.isGemeinsam()) {
			if (mitteilung.getSteuerdatenResponse().getZpvNrPartner() == null) {
				throw new EbeguException("neueVeranlagungsMitteilungImAntragErsetzen",
						ErrorCodeEnum.ERROR_FIN_SIT_GEMEINSAM_NEUE_VERANLAGUNG_ALLEIN,
						gesuch.getId());
			}

			KibonAnfrageHelper.updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(mitteilung.getSteuerdatenResponse(),
					kibonAnfrageContext);
			finanzielleSituationService.saveFinanzielleSituation(
					kibonAnfrageContext.getFinSitCont(GesuchstellerTyp.GESUCHSTELLER_1), gesuch.getId());
			finanzielleSituationService.saveFinanzielleSituation(
					kibonAnfrageContext.getFinSitCont(GesuchstellerTyp.GESUCHSTELLER_2), gesuch.getId());
		} else {
			if (mitteilung.getSteuerdatenResponse().getZpvNrPartner() != null) {
				throw new EbeguException("neueVeranlagungsMitteilungImAntragErsetzen",
						ErrorCodeEnum.ERROR_FIN_SIT_ALLEIN_NEUE_VERANLAGUNG_GEMEINSAM,
						gesuch.getId());
			}

			handleSteuerdatenResponse(kibonAnfrageContext, mitteilung, gesuch);
		}
		mitteilung.setMitteilungStatus(MitteilungStatus.ERLEDIGT);
		persistence.merge(mitteilung);
	}

	private void handleSteuerdatenResponse(
			KibonAnfrageContext kibonAnfrageContext,
			NeueVeranlagungsMitteilung mitteilung,
			Gesuch gesuch) {
		KibonAnfrageHelper.handleSteuerdatenResponse(kibonAnfrageContext, mitteilung.getSteuerdatenResponse());
		finanzielleSituationService.saveFinanzielleSituation(
				kibonAnfrageContext.getFinanzielleSituationContainerToUse(),
				gesuch.getId());
	}

	private void applyBetreuungsmitteilungToMutation(
			@Nonnull Gesuch gesuch, @Nonnull Betreuungsmitteilung mitteilung) {
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(mitteilung);
		Objects.requireNonNull(mitteilung.getBetreuung());

		authorizer.checkWriteAuthorization(gesuch);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);

		final Optional<Betreuung> betreuungToChangeOpt =
				gesuch.extractBetreuungsFromBetreuungNummer(mitteilung.getBetreuung().getKind().getKindNummer(),
						mitteilung.getBetreuung().getBetreuungNummer());
		if (betreuungToChangeOpt.isPresent()) {
			Betreuung existingBetreuung = betreuungToChangeOpt.get();
			existingBetreuung.getBetreuungspensumContainers()
					.clear();//delete all current Betreuungspensen before we add the modified list
			boolean betreuungsMitteilungVollstaendig = true;
			for (final BetreuungsmitteilungPensum betPensumMitteilung : mitteilung.getBetreuungspensen()) {
				if (!betPensumMitteilung.isVollstaendig()) {
					betreuungsMitteilungVollstaendig = false;
				}
				BetreuungspensumContainer betPenCont = new BetreuungspensumContainer();
				betPenCont.setBetreuung(existingBetreuung);
				Betreuungspensum betPensumJA = new Betreuungspensum(betPensumMitteilung);
				//gs container muss nicht mikopiert werden
				betPenCont.setBetreuungspensumJA(betPensumJA);

				existingBetreuung.getBetreuungspensumContainers().add(betPenCont);

				if (betPensumMitteilung.getBetreuungspensumAbweichung() != null) {
					betPensumMitteilung.getBetreuungspensumAbweichung()
							.setStatus(BetreuungspensumAbweichungStatus.UEBERNOMMEN);
				}
			}
			// when we apply a Betreuungsmitteilung we have to change the status to BESTAETIGT wenn Vollstaendig,
			// sonst warten
			if (mitteilung.isBetreuungStornieren()) {
				existingBetreuung.setBetreuungsstatus(Betreuungsstatus.STORNIERT);
			} else if (betreuungsMitteilungVollstaendig) {
				existingBetreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
			} else {
				existingBetreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
			}
			betreuungService.saveBetreuung(existingBetreuung, false, null);
			mitteilung.setApplied(true);
			mitteilung.setMitteilungStatus(MitteilungStatus.ERLEDIGT);
			// Nach erfolgreicher Uebernahme der Daten in die neue Mitteilung soll die Mitteilung mit dieser
			// Betreuung verknuepft werden, damit der Link auf der Mitteilung immer auf die Betreuung zeigt,
			// in der die Daten vorhanden sind
			mitteilung.setBetreuung(existingBetreuung);
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
		case SACHBEARBEITER_SOZIALDIENST:
		case ADMIN_SOZIALDIENST:
			return MitteilungTeilnehmerTyp.SOZIALDIENST;
		default:
			return null;
		}
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	private Mitteilung setMitteilungsStatusIfBerechtigt(
			@Nonnull String mitteilungsId,
			@Nonnull MitteilungStatus statusRequested,
			@Nonnull MitteilungStatus... statusRequired) {
		Optional<Mitteilung> mitteilungOptional = findMitteilung(mitteilungsId);
		Mitteilung mitteilung = mitteilungOptional.orElseThrow(() -> new EbeguRuntimeException(
				"setMitteilungsStatusIfBerechtigt",
				"Mitteilung not found"));
		return setMitteilungsStatusIfBerechtigt(mitteilung, statusRequested, statusRequired);
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	private Mitteilung setMitteilungsStatusIfBerechtigt(
			@Nonnull Mitteilung mitteilung,
			@Nonnull MitteilungStatus statusRequested,
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
		// Der EmpfaengerTyp (bei Institution und GS) muss uebereinstimmen
		boolean sameEmpfaengerTyp = mitteilung.getEmpfaengerTyp() == getMitteilungTeilnehmerTypForCurrentUser();
		if (sameEmpfaengerTyp) {
			mitteilung.setMitteilungStatus(statusRequested);
		}
		return persistence.merge(mitteilung);
	}

	@Override
	public boolean hasBenutzerAnyMitteilungenAsSenderOrEmpfaenger(@Nonnull Benutzer benutzer) {

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery query = SearchUtil.getQueryForSearchMode(cb, SearchMode.COUNT, "searchMitteilungen");
		Root<Mitteilung> root = query.from(Mitteilung.class);

		// Join all the relevant relations
		Join<Mitteilung, Benutzer> joinSender = root.join(Mitteilung_.sender, JoinType.LEFT);
		Join<Mitteilung, Benutzer> joinEmpfaenger = root.join(Mitteilung_.empfaenger, JoinType.LEFT);

		Predicate predicate = cb.or(cb.equal(joinSender, benutzer), cb.equal(joinEmpfaenger, benutzer));

		query.select(cb.countDistinct(root.get(AbstractEntity_.id))).where(predicate);

		Long count = (Long) persistence.getCriteriaSingleResult(query);

		return count > 0;
	}

	/**
	 * Wir muessen nur pruefen ob der Institution stimmt fuer die Institution und Traegerschaft Benutzern
	 * als der Zugriff auf der Dossier heiss nicht das man alles sehen darf mit EmpfaengerTyp Institution
	 */
	private boolean isMitteilungReadableForInstitution(Mitteilung mitteilung) {
		if (principalBean.isCallerInAnyOfRole(UserRole.ADMIN_INSTITUTION, UserRole.SACHBEARBEITER_INSTITUTION)) {
			if (principalBean.getBenutzer().getInstitution() != null) {
				if (mitteilung.getInstitution() != null) {
					return principalBean.getBenutzer().getInstitution().equals(mitteilung.getInstitution());
				}
				if (mitteilung.getBetreuung() != null) {
					return principalBean.getBenutzer()
							.getInstitution()
							.equals(mitteilung.getBetreuung().getInstitutionStammdaten().getInstitution());
				}
				throw new EbeguRuntimeException(
						"isMitteilungReadableForInstitution",
						"Mitteilung for INSTITUTION must have institution or betreuung, this is likely a data "
								+ "error. "
								+ "See KSKB-2613");
			}
			return false;
		} else if (principalBean.isCallerInAnyOfRole(UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT)) {
			if (principalBean.getBenutzer().getTraegerschaft() != null) {
				if (mitteilung.getInstitution() != null && mitteilung.getInstitution().getTraegerschaft() != null) {
					return principalBean.getBenutzer()
							.getTraegerschaft()
							.equals(mitteilung.getInstitution().getTraegerschaft());
				}
				if (mitteilung.getBetreuung() != null
						&& mitteilung.getBetreuung().getInstitutionStammdaten().getInstitution().getTraegerschaft()
						!= null) {
					return principalBean.getBenutzer()
							.getTraegerschaft()
							.equals(mitteilung.getBetreuung()
									.getInstitutionStammdaten()
									.getInstitution()
									.getTraegerschaft());
				}
				if ((mitteilung.getInstitution() != null && mitteilung.getInstitution().getTraegerschaft() == null) || (
						mitteilung.getBetreuung() != null
								&& mitteilung.getBetreuung()
								.getInstitutionStammdaten()
								.getInstitution()
								.getTraegerschaft() == null)) {
					return false;
				}
				throw new EbeguRuntimeException(
						"isMitteilungReadableForInstitution",
						"Mitteilung for TRAEGERSCHAFT should have institution, traegerschaft or betreuung");
			}
			return false;
		}
		return true;
	}

	@Nullable
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String applyBetreuungsmitteilungIfPossible(
			@Nonnull Betreuungsmitteilung betreuungsmitteilung) {
		try {
			Objects.requireNonNull(betreuungsmitteilung.getBetreuung());
			Gesuch mutation =
					doApplymitteilung(betreuungsmitteilung, betreuungsmitteilung.getBetreuung().extractGesuch());
			if (canGesuchBeAutomatischVerfuegt(mutation)) {

				verfuegungService.gesuchAutomatischVerfuegen(mutation);
			}
			persistence.getEntityManager().flush();
		} catch (EbeguException e) {
			final Locale locale = LocaleThreadLocal.get();
			String translatedError =
					ServerMessageUtil.translateEnumValue(e.getErrorCodeEnum(), locale, principalBean.getMandant());
			return translatedError;
		}
		return null;
	}

	private boolean canGesuchBeAutomatischVerfuegt(Gesuch mutation) {
		// wenn das Gesuch nicht neu erstellt wurde, darf es nie automatisch verfügt werden
		if (!mutation.isNewlyCreatedMutation()) {
			return false;
		}

		Gesuch erstGesuch = gesuchService.findErstgesuchForGesuch(mutation);
		Gesuch vorgaenger = gesuchService.findVorgaengerGesuchNotIgnoriert(requireNonNull(mutation.getVorgaengerId()));
		if (hasNichtEintretenBetreuung(vorgaenger) || vorgaenger.getFinSitStatus().equals(FinSitStatus.ABGELEHNT)) {
			return false;
		}
		return erstGesuch.getEingangsart().isOnlineGesuch();
	}

	private static boolean hasNichtEintretenBetreuung(Gesuch vorgaenger) {
		return vorgaenger.getKindContainers()
			.stream()
			.anyMatch(kindContainer -> kindContainer.getBetreuungen()
				.stream()
				.anyMatch(betreuung -> betreuung.getBetreuungsstatus() == Betreuungsstatus.NICHT_EINGETRETEN));
	}

	@Override
	public Optional<Betreuungsmitteilung> findAndRefreshBetreuungsmitteilung(String id) {
		Betreuungsmitteilung mitteilung = persistence.find(Betreuungsmitteilung.class, id);
		persistence.getEntityManager().refresh(mitteilung);
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		return Optional.ofNullable(mitteilung);
	}

	@Override
	public Collection<NeueVeranlagungsMitteilung> findOffeneNeueVeranlagungsmitteilungenForGesuch(List<String> gesuchIds) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<NeueVeranlagungsMitteilung> query = cb.createQuery(NeueVeranlagungsMitteilung.class);
		Root<NeueVeranlagungsMitteilung> root = query.from(NeueVeranlagungsMitteilung.class);
		List<Predicate> predicates = new ArrayList<>();

		Join<NeueVeranlagungsMitteilung, SteuerdatenResponse> steuerDatenJoin =
				root.join(NeueVeranlagungsMitteilung_.steuerdatenResponse, JoinType.LEFT);

		Predicate predicateId = steuerDatenJoin.get(SteuerdatenResponse_.kibonAntragId).in(gesuchIds);
		query.where(predicateId);
		predicates.add(predicateId);

		final Predicate predicateNotApplied =
				root.get(Mitteilung_.mitteilungStatus).in(MitteilungStatus.NEU, MitteilungStatus.GELESEN);
		predicates.add(predicateNotApplied);

		query.orderBy(cb.desc(root.get(Mitteilung_.sentDatum)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		List<NeueVeranlagungsMitteilung> criteriaResults = persistence.getCriteriaResults(query);
		return criteriaResults;
	}
}


