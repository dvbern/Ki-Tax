/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.BfsGemeinde;
import ch.dvbern.ebegu.entities.BfsGemeinde_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode_;
import ch.dvbern.ebegu.entities.GemeindeStammdaten_;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.SequenceType;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Gemeinden
 */
@Stateless
@Local(GemeindeService.class)
@PermitAll
public class GemeindeServiceBean extends AbstractBaseService implements GemeindeService {

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private SequenceService sequenceService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private MailService mailService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gemeinde saveGemeinde(@Nonnull Gemeinde gemeinde) {
		requireNonNull(gemeinde);
		authorizer.checkWriteAuthorization(gemeinde);

		if (gemeinde.isNew()) {
			initGemeindeNummerAndMandant(gemeinde);
		}
		return persistence.merge(gemeinde);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gemeinde createGemeinde(@Nonnull Gemeinde gemeinde) {
		Optional<Gemeinde> gemeindeOpt =
			criteriaQueryHelper.getEntityByUniqueAttribute(Gemeinde.class, gemeinde.getName(), Gemeinde_.name);
		if (gemeindeOpt.isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Gemeinde.class,
				"name",
				gemeinde.getName(),
				ErrorCodeEnum.ERROR_DUPLICATE_GEMEINDE_NAME);
		}
		final Long bfsNummer = gemeinde.getBfsNummer();
		if (findGemeindeByBSF(bfsNummer).isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Gemeinde.class, "bsf",
				Long.toString(bfsNummer),
				ErrorCodeEnum.ERROR_DUPLICATE_GEMEINDE_BSF);
		}
		return saveGemeinde(gemeinde);
	}

	@Nonnull
	@Override
	public Optional<Gemeinde> findGemeinde(@Nonnull String id) {
		requireNonNull(id, "Gemeinde id muss gesetzt sein");
		Gemeinde gemeinde = persistence.find(Gemeinde.class, id);
		return Optional.ofNullable(gemeinde);
	}

	@Nonnull
	private Optional<Gemeinde> findGemeindeByBSF(@Nullable Long bsf) {
		Optional<Gemeinde> gemeindeOpt =
			criteriaQueryHelper.getEntityByUniqueAttribute(Gemeinde.class, bsf, Gemeinde_.bfsNummer);
		return gemeindeOpt;
	}

	@Nonnull
	@Override
	public Collection<Gemeinde> getAllGemeinden() {
		return criteriaQueryHelper.getAllOrdered(Gemeinde.class, Gemeinde_.name);
	}

	private long getNextGemeindeNummer() {
		Mandant mandant = requireNonNull(principalBean.getMandant());

		return sequenceService.createNumberTransactional(SequenceType.GEMEINDE_NUMMER, mandant);
	}

	private void initGemeindeNummerAndMandant(@Nonnull Gemeinde gemeinde) {
		if (gemeinde.getMandant() == null) {
			gemeinde.setMandant(requireNonNull(principalBean.getMandant()));
		}
		if (gemeinde.getGemeindeNummer() == 0) {
			gemeinde.setGemeindeNummer(getNextGemeindeNummer());
		}
	}

	@Nonnull
	@Override
	public Collection<Gemeinde> getAktiveGemeinden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gemeinde> query = cb.createQuery(Gemeinde.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);
		List<Predicate> predicates = new ArrayList<>();

		// Status muss aktiv sein
		Predicate predicateStatusActive = cb.equal(root.get(Gemeinde_.status), GemeindeStatus.AKTIV);
		predicates.add(predicateStatusActive);

		//		// TODO MANDANTEN when developing kibon for multple mandanten we need to filter the mandanten too.
		//		 Uncommenting the following code
		//		// and taking the FIXME into account should be enough
		//		// Nur Gemeinden meines Mandanten zurueckgeben
		//		final Principal principal = principalBean.getPrincipal();
		//		if (!Constants.ANONYMOUS_USER_USERNAME.equals(principal.getName())) {
		//			// user anonymous can get the list of active Gemeinden, though anonymous user doesn't really exist
		//			// FIXME MANDANTEN this is actually a problem if we work with different Mandanten because in
		//			 onBoarding there is no user at all
		//			// so we cannot get the mandant out of the user. In this case we need to send the mandant when
		//			calling this method
		//			Mandant mandant = principalBean.getMandant();
		//			if (mandant != null) {
		//				Predicate predicateMandant = cb.equal(root.get(Gemeinde_.mandant), mandant);
		//				predicates.add(predicateMandant);
		//			}
		//		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Optional<GemeindeStammdaten> getGemeindeStammdaten(@Nonnull String id) {
		requireNonNull(id, "Gemeinde Stammdaten id muss gesetzt sein");
		GemeindeStammdaten stammdaten = persistence.find(GemeindeStammdaten.class, id);
		if (stammdaten != null) {
			authorizer.checkReadAuthorization(stammdaten.getGemeinde());
		}
		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<GemeindeStammdaten> getGemeindeStammdatenByGemeindeId(@Nonnull String gemeindeId) {
		requireNonNull(gemeindeId, "Gemeinde id muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdaten> query = cb.createQuery(GemeindeStammdaten.class);
		Root<GemeindeStammdaten> root = query.from(GemeindeStammdaten.class);
		Predicate predicate = cb.equal(root.get(GemeindeStammdaten_.gemeinde).get(AbstractEntity_.id), gemeindeId);
		query.where(predicate);
		GemeindeStammdaten stammdaten = persistence.getCriteriaSingleResult(query);
		if (stammdaten != null) {
			authorizer.checkReadAuthorization(stammdaten.getGemeinde());
		}
		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public GemeindeStammdaten saveGemeindeStammdaten(@Nonnull GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		authorizer.checkWriteAuthorization(stammdaten.getGemeinde());
		if (stammdaten.isNew()) {
			initGemeindeNummerAndMandant(stammdaten.getGemeinde());
		}
		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public GemeindeStammdaten uploadLogo(
		@Nonnull String gemeindeId,
		@Nonnull byte[] content,
		@Nonnull String name,
		@Nonnull String type) {
		requireNonNull(gemeindeId);
		requireNonNull(content);
		requireNonNull(name);
		requireNonNull(type);

		final GemeindeStammdaten stammdaten = getGemeindeStammdatenByGemeindeId(gemeindeId).orElseThrow(
			() -> new EbeguEntityNotFoundException("uploadLogo", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId)
		);
		stammdaten.setLogoContent(content);
		stammdaten.setLogoName(name);
		stammdaten.setLogoType(type);
		return saveGemeindeStammdaten(stammdaten);
	}

	@Nonnull
	@Override
	public Collection<BfsGemeinde> getUnregisteredBfsGemeinden(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BfsGemeinde> query = cb.createQuery(BfsGemeinde.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);
		List<Predicate> predicates = new ArrayList<>();

		List<Long> registeredBfsNummern = getRegisteredBfsNummern(mandant);
		if (!registeredBfsNummern.isEmpty()) {
			Predicate predicate = root.get(BfsGemeinde_.bfsNummer).in(registeredBfsNummern).not();
			predicates.add(predicate);
		}

		// Wenn das Tagesschule-Flag nicht gesetzt ist, dürfen Verbunds-Gemeinden nicht ausgewählt werden können.
		boolean tagesschuleEnabled = mandant.isAngebotTS();
		if (!tagesschuleEnabled) {
			List<Long> verbundsBfsNummern = getVerbundsBfsNummern(mandant);
			Predicate predicateNoVerbund = root.get(BfsGemeinde_.bfsNummer).in(verbundsBfsNummern).not();
			predicates.add(predicateNoVerbund);
		}

		Predicate predicateMandant = cb.equal(root.get(BfsGemeinde_.mandant), mandant);
		predicates.add(predicateMandant);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		List<BfsGemeinde> unregisteredBfsGemeinden = persistence.getCriteriaResults(query);
		return unregisteredBfsGemeinden;
	}

	@Nonnull
	private List<Long> getRegisteredBfsNummern(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);

		Predicate predicateMandant = cb.equal(root.get(Gemeinde_.mandant), mandant);
		query.where(predicateMandant);
		query.select(root.get(Gemeinde_.bfsNummer));
		List<Long> registeredBfsNummern = persistence.getCriteriaResults(query);
		return registeredBfsNummern;
	}

	@Nonnull
	private List<Long> getVerbundsBfsNummern(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);

		Predicate predicateMandant = cb.equal(root.get(BfsGemeinde_.mandant), mandant);
		query.where(predicateMandant);
		query.select(root.get(BfsGemeinde_.verbund).get(BfsGemeinde_.bfsNummer));
		List<Long> registeredBfsNummern = persistence.getCriteriaResults(query);
		return registeredBfsNummern;
	}

	@Nonnull
	@Override
	public Optional<Gemeinde> findRegistredGemeindeVerbundIfExist(@Nonnull Long gemeindeBfsNummer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);

		Predicate predicateBFS = cb.equal(root.get(BfsGemeinde_.bfsNummer), gemeindeBfsNummer);
		query.where(predicateBFS);
		query.select(root.get(BfsGemeinde_.verbund).get(BfsGemeinde_.bfsNummer));
		Long gemeindeVerbundBfsNummer = persistence.getCriteriaSingleResult(query);
		if (gemeindeVerbundBfsNummer == null) {
			return Optional.empty();
		}

		return getAktiveGemeindeByBFSNummer(gemeindeVerbundBfsNummer);
	}

	@Nonnull
	@Override
	public Collection<BfsGemeinde> getAllBfsGemeinden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BfsGemeinde> query = cb.createQuery(BfsGemeinde.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);
		CriteriaQuery<BfsGemeinde> all = query.select(root);
		List<BfsGemeinde> allBfsGemeinden = persistence.getCriteriaResults(all);
		return allBfsGemeinden;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void updateAngebotBG(@Nonnull Gemeinde gemeinde, boolean value) {
		gemeinde.setAngebotBG(value);
		persistence.merge(gemeinde);

		if (value) {
			mailService.sendInfoGemeineAngebotAktiviert(gemeinde, GemeindeAngebotTyp.BETREUUNGSGUTSCHEIN);
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void updateAngebotTS(@Nonnull Gemeinde gemeinde, boolean value) {
		gemeinde.setAngebotTS(value);
		persistence.merge(gemeinde);

		if (value) {
			mailService.sendInfoGemeineAngebotAktiviert(gemeinde, GemeindeAngebotTyp.TAGESSCHULE);
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public void updateAngebotFI(@Nonnull Gemeinde gemeinde, boolean value) {
		gemeinde.setAngebotFI(value);
		persistence.merge(gemeinde);

		if (value) {
			mailService.sendInfoGemeineAngebotAktiviert(gemeinde, GemeindeAngebotTyp.FERIENINSEL);
		}
	}

	@Nonnull
	private Optional<Gemeinde> getAktiveGemeindeByBFSNummer(@Nonnull Long bfsNummer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gemeinde> query = cb.createQuery(Gemeinde.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);
		List<Predicate> predicates = new ArrayList<>();

		// Status muss aktiv sein
		Predicate predicateStatusActive = cb.equal(root.get(Gemeinde_.status), GemeindeStatus.AKTIV);
		predicates.add(predicateStatusActive);

		//BfsNummer muss gesetzt sein
		Predicate predicateBfsNummer = cb.equal(root.get(Gemeinde_.bfsNummer), bfsNummer);
		predicates.add(predicateBfsNummer);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		Gemeinde gemeinde = persistence.getCriteriaSingleResult(query);

		return Optional.ofNullable(gemeinde);
	}

	@Nonnull
	@Override
	public List<BfsGemeinde> findGemeindeVonVerbund(@Nonnull Long verbundBfsNummer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BfsGemeinde> query = cb.createQuery(BfsGemeinde.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);

		Join<Object, Object> join = root.join(BfsGemeinde_.VERBUND);

		Predicate predicateBFSVerbund = cb.equal(join.get(BfsGemeinde_.BFS_NUMMER), verbundBfsNummer);
		query.where(predicateBFSVerbund);
		List<BfsGemeinde> gemeindenVonVerbund = persistence.getCriteriaResults(query);

		return gemeindenVonVerbund;
	}

	@Nonnull
	@Override
	public Optional<BfsGemeinde> findBfsGemeinde(@Nonnull Long bfsNummer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BfsGemeinde> query = cb.createQuery(BfsGemeinde.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);
		Predicate predicateBFSVerbund = cb.equal(root.get(BfsGemeinde_.BFS_NUMMER), bfsNummer);
		query.where(predicateBFSVerbund);
		BfsGemeinde bfsGemeinde = persistence.getCriteriaSingleResult(query);

		return Optional.ofNullable(bfsGemeinde);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public GemeindeStammdatenGesuchsperiode uploadGemeindeGesuchsperiodeDokument(
		@Nonnull String gemeindeId,
		@Nonnull String gesuchsperiodeId,
		@Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp,
		@Nonnull byte[] content) {
		requireNonNull(gemeindeId);
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(content);
		GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode =
			findGemeindeStammdatenGesuchsperiode(gemeindeId, gesuchsperiodeId).orElse(null);
		if (gemeindeStammdatenGesuchsperiode == null) {
			gemeindeStammdatenGesuchsperiode = createGemeindeStammdatenGesuchsperiode(gemeindeId, gesuchsperiodeId);
		}
		// Aktuell hat man nur einen Typ bei allen anderen macht nichts
		if (dokumentTyp.equals(DokumentTyp.MERKBLATT_ANMELDUNG_TS)) {
			if (sprache == Sprache.DEUTSCH) {
				gemeindeStammdatenGesuchsperiode.setMerkblattAnmeldungTagesschuleDe(content);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gemeindeStammdatenGesuchsperiode.setMerkblattAnmeldungTagesschuleFr(content);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't overwrite accidentaly
				return gemeindeStammdatenGesuchsperiode;
			}
		} else {
			return gemeindeStammdatenGesuchsperiode;
		}
		return saveGemeindeStammdatenGesuchsperiode(gemeindeStammdatenGesuchsperiode);
	}

	@Nullable
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public byte[] downloadGemeindeGesuchsperiodeDokument(@Nonnull String gemeindeId, @Nonnull String gesuchsperiodeId,
		@Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp) {
		final Optional<GemeindeStammdatenGesuchsperiode> gemeindeStammdatenGesuchsperiode =
			findGemeindeStammdatenGesuchsperiode(gemeindeId, gesuchsperiodeId);
		if (dokumentTyp.equals(DokumentTyp.MERKBLATT_ANMELDUNG_TS)) {
			return gemeindeStammdatenGesuchsperiode
				.map(gemeindeStammdatenGesuchsperiode1 -> gemeindeStammdatenGesuchsperiode1.getMerkblattAnmeldungTagesschuleWithSprache(sprache))
				.orElse(null);
		}
		return new byte[0];
	}

	private Optional<GemeindeStammdatenGesuchsperiode> findGemeindeStammdatenGesuchsperiode(@Nonnull String gemeindeId,
		@Nonnull String gesuchsperiodeId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdatenGesuchsperiode> query =
			cb.createQuery(GemeindeStammdatenGesuchsperiode.class);
		Root<GemeindeStammdatenGesuchsperiode> root = query.from(GemeindeStammdatenGesuchsperiode.class);
		Predicate predicateGemeinde =
			cb.equal(root.get(GemeindeStammdatenGesuchsperiode_.gemeinde).get(AbstractEntity_.id),
				gemeindeId);
		Predicate predicateGesuchsperiode =
			cb.equal(root.get(GemeindeStammdatenGesuchsperiode_.gesuchsperiode).get(AbstractEntity_.id),
				gesuchsperiodeId);
		query.where(predicateGemeinde, predicateGesuchsperiode);
		GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode = persistence.getCriteriaSingleResult(query);
		if (gemeindeStammdatenGesuchsperiode != null) {
			authorizer.checkReadAuthorization(gemeindeStammdatenGesuchsperiode.getGemeinde());
		}
		return Optional.ofNullable(gemeindeStammdatenGesuchsperiode);
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public Collection<GemeindeStammdatenGesuchsperiode> findGemeindeStammdatenGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		return
			criteriaQueryHelper.getEntitiesByAttribute(GemeindeStammdatenGesuchsperiode.class, gesuchsperiode,
				GemeindeStammdatenGesuchsperiode_.gesuchsperiode);
	}

	private Collection<GemeindeStammdatenGesuchsperiode> getGemeindeStammdatenGesuchsperiodeByGesuchsperiodeId(@Nonnull String gesuchsperiodeId) {
		requireNonNull(gesuchsperiodeId, "Gesuchsperiode id muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdatenGesuchsperiode> query =
			cb.createQuery(GemeindeStammdatenGesuchsperiode.class);
		Root<GemeindeStammdatenGesuchsperiode> root = query.from(GemeindeStammdatenGesuchsperiode.class);
		Predicate predicateGesuchsperiode =
			cb.equal(root.get(GemeindeStammdatenGesuchsperiode_.gesuchsperiode).get(AbstractEntity_.id),
				gesuchsperiodeId);
		query.where(predicateGesuchsperiode);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	private GemeindeStammdatenGesuchsperiode saveGemeindeStammdatenGesuchsperiode(@Nonnull GemeindeStammdatenGesuchsperiode stammdaten) {
		requireNonNull(stammdaten);
		authorizer.checkWriteAuthorization(stammdaten.getGemeinde());
		return persistence.merge(stammdaten);
	}

	private GemeindeStammdatenGesuchsperiode createGemeindeStammdatenGesuchsperiode(@Nonnull String gemeindeId,
		@Nonnull String gesuchsperiodeId) {
		GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode = new GemeindeStammdatenGesuchsperiode();
		Gemeinde gemeinde = findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"uploadGesuchsperiodeDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			gemeindeId));
		gemeindeStammdatenGesuchsperiode.setGemeinde(gemeinde);
		Gesuchsperiode gesuchsperiode =
			gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"uploadGesuchsperiodeDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			gesuchsperiodeId));
		gemeindeStammdatenGesuchsperiode.setGesuchsperiode(gesuchsperiode);
		return gemeindeStammdatenGesuchsperiode;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public GemeindeStammdatenGesuchsperiode removeGemeindeGesuchsperiodeDokument(@Nonnull String gemeindeId,
		@Nonnull String gesuchsperiodeId,
		@Nonnull Sprache sprache, @Nonnull DokumentTyp dokumentTyp) {

		final GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode =
			findGemeindeStammdatenGesuchsperiode(gemeindeId, gesuchsperiodeId).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"removeGemeindeGesuchsperiodeDokument",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId)
		);
		if(dokumentTyp.equals(DokumentTyp.MERKBLATT_ANMELDUNG_TS)){
			if (sprache == Sprache.DEUTSCH) {
				gemeindeStammdatenGesuchsperiode.setMerkblattAnmeldungTagesschuleDe(null);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gemeindeStammdatenGesuchsperiode.setMerkblattAnmeldungTagesschuleFr(null);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't remove accidentaly
				return gemeindeStammdatenGesuchsperiode;
			}
		}
		else{
			return gemeindeStammdatenGesuchsperiode;
		}

		return saveGemeindeStammdatenGesuchsperiode(gemeindeStammdatenGesuchsperiode);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public boolean existGemeindeGesuchsperiodeDokument(@Nonnull String gemeindeId, @Nonnull String gesuchsperiodeId,
		@Nonnull Sprache sprache, @Nonnull DokumentTyp dokumentTyp) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);

		final GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode =
			findGemeindeStammdatenGesuchsperiode(gemeindeId, gesuchsperiodeId).orElse(null);
		if (gemeindeStammdatenGesuchsperiode == null) {
			return false;
		}

		if (dokumentTyp.equals(DokumentTyp.MERKBLATT_ANMELDUNG_TS)){
			return gemeindeStammdatenGesuchsperiode.getMerkblattAnmeldungTagesschuleWithSprache(sprache).length != 0;
		}

		return false;
	}

	@Override
	public void copyGesuchsperiodeGemeindeStammdaten(@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode) {
		getGemeindeStammdatenGesuchsperiodeByGesuchsperiodeId(lastGesuchsperiode.getId()).stream().forEach(
			gemeindeStammdatenGesuchsperiode -> {
				GemeindeStammdatenGesuchsperiode newGemeindeStammdatenGesuchsperiode =
					gemeindeStammdatenGesuchsperiode.copyForGesuchsperiode(gesuchsperiodeToCreate);
				persistence.merge(newGemeindeStammdatenGesuchsperiode);
			}
		);
	}
}
