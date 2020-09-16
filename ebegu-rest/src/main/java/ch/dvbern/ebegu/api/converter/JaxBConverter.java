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

package ch.dvbern.ebegu.api.converter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxAbstractFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.JaxAbstractInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxAbwesenheit;
import ch.dvbern.ebegu.api.dtos.JaxAbwesenheitContainer;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.api.dtos.JaxAdresseContainer;
import ch.dvbern.ebegu.api.dtos.JaxAlwaysEditableProperties;
import ch.dvbern.ebegu.api.dtos.JaxAntragStatusHistory;
import ch.dvbern.ebegu.api.dtos.JaxApplicationProperties;
import ch.dvbern.ebegu.api.dtos.JaxBelegungFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxBelegungFerieninselTag;
import ch.dvbern.ebegu.api.dtos.JaxBelegungTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxBelegungTagesschuleModul;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigung;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigungHistory;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungsmitteilung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungsmitteilungPensum;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensum;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumAbweichung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungsstandort;
import ch.dvbern.ebegu.api.dtos.JaxBfsGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxDokument;
import ch.dvbern.ebegu.api.dtos.JaxDokumentGrund;
import ch.dvbern.ebegu.api.dtos.JaxDokumente;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxEbeguVorlage;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterung;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungContainer;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfo;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.api.dtos.JaxEinstellung;
import ch.dvbern.ebegu.api.dtos.JaxEinstellungenFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxEinstellungenTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxEnversRevision;
import ch.dvbern.ebegu.api.dtos.JaxErweiterteBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxErweiterteBetreuungContainer;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensum;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxExternalClient;
import ch.dvbern.ebegu.api.dtos.JaxFachstelle;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituation;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituationContainer;
import ch.dvbern.ebegu.api.dtos.JaxFerieninselZeitraum;
import ch.dvbern.ebegu.api.dtos.JaxFile;
import ch.dvbern.ebegu.api.dtos.JaxFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.JaxFinanzielleSituationContainer;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeKonfiguration;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsteller;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionListDTO;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenSummary;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionUpdate;
import ch.dvbern.ebegu.api.dtos.JaxKind;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleich;
import ch.dvbern.ebegu.api.dtos.JaxMahnung;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.api.dtos.JaxMitteilung;
import ch.dvbern.ebegu.api.dtos.JaxModulTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxModulTagesschuleGroup;
import ch.dvbern.ebegu.api.dtos.JaxPensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.api.dtos.JaxPensumFachstelle;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungDokument;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungFormular;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungMitteilung;
import ch.dvbern.ebegu.api.dtos.JaxSozialhilfeZeitraum;
import ch.dvbern.ebegu.api.dtos.JaxSozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.api.dtos.JaxTextRessource;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.api.dtos.JaxTsCalculationResult;
import ch.dvbern.ebegu.api.dtos.JaxUnbezahlterUrlaub;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegung;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegungZeitabschnitt;
import ch.dvbern.ebegu.api.dtos.JaxVorlage;
import ch.dvbern.ebegu.api.dtos.JaxWizardStep;
import ch.dvbern.ebegu.api.dtos.JaxZahlung;
import ch.dvbern.ebegu.api.dtos.JaxZahlungsauftrag;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Betreuungsstandort;
import ch.dvbern.ebegu.entities.BfsGemeinde;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.EbeguVorlage;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninselZeitraum;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.RueckforderungDokument;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraum;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.TextRessource;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Vorlage;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguFingerWegException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.AdresseService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.EinkommensverschlechterungInfoService;
import ch.dvbern.ebegu.services.EinkommensverschlechterungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.FachstelleService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.FerieninselStammdatenService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.GesuchstellerAdresseService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.PensumAusserordentlicherAnspruchService;
import ch.dvbern.ebegu.services.PensumFachstelleService;
import ch.dvbern.ebegu.services.SozialhilfeZeitraumService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.StreamsUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.lib.date.DateConvertUtils;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.STEUERAMT;
import static java.util.Objects.requireNonNull;

@Dependent
@SuppressWarnings({ "PMD.NcssTypeCount", "unused", "checkstyle:CyclomaticComplexity" })
public class JaxBConverter extends AbstractConverter {

	@Inject
	private PrincipalBean principalBean;

	public static final String DROPPED_DUPLICATE_CONTAINER = "dropped duplicate container ";
	public static final String DROPPED_DUPLICATE_ABWEICHUNG = "dropped duplicate abweichung ";
	public static final String DOSSIER_TO_ENTITY = "dossierToEntity";
	private static final Logger LOGGER = LoggerFactory.getLogger(JaxBConverter.class);
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private GesuchstellerAdresseService gesuchstellerAdresseService;
	@Inject
	private PensumAusserordentlicherAnspruchService pensumAusserordentlicherAnspruchService;
	@Inject
	private FachstelleService fachstelleService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private ErwerbspensumService erwerbspensumService;
	@Inject
	private FallService fallService;
	@Inject
	private DossierService dossierService;
	@Inject
	private FamiliensituationService familiensituationService;
	@Inject
	private EinkommensverschlechterungInfoService einkommensverschlechterungInfoService;
	@Inject
	private EinkommensverschlechterungService einkommensverschlechterungService;
	@Inject
	private MandantService mandantService;
	@Inject
	private TraegerschaftService traegerschaftService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private BetreuungService betreuungService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private AdresseService adresseService;
	@Inject
	private EinstellungService einstellungService;
	@Inject
	private KindService kindService;
	@Inject
	private Persistence persistence;
	@Inject
	private PensumFachstelleService pensumFachstelleService;
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;
	@Inject
	private FerieninselStammdatenService ferieninselStammdatenService;

	public JaxBConverter() {
		//nop
	}

	/**
	 * Behandlung des Version-Attributes: Dieses wird neu auf den Client geschickt, um
	 * OptimisticLocking von Hibernate verwenden zu koennen.
	 * Da es aber bei attachten entities nicht möglich ist die Version manuell zu setzen
	 * müssen wir die entities detachen um die Version vom Client reinschreiben zu können.
	 *
	 * So merkt hibernate beim mergen wenn die Versionsnummer in der Zwischenzeit
	 * incremented wurde und höher ist als die die auf den client ging. Falls dies
	 * der Fall ist, wird eine OptimisticLockingException geworfen.
	 *
	 * Damit nach dem Speichern die richtige (in der Regel inkrementierte) Version
	 * auf den Client geht muss das betroffene Entity wirklich schon gemerged
	 * worden sein oder man muss die Version manuell um eins erhöhen im dto.
	 * Gelöst wird das aktuell indem em.flush() gemacht wird vor dem erstellen des
	 * Rückgabe-DTOs.
	 *
	 * Muss (falls OptimisticLocking gewuenscht wird) beim Start, also beim Konvertieren
	 * von JAX zu Entity aufgerufen werden.
	 */
	@Nonnull
	private <T extends AbstractEntity> T checkVersionSaveAndFlush(@Nonnull T entity, long version) {
		persistence.getEntityManager().detach(entity); // DETACH -- otherwise we cannot set the version manually
		entity.setVersion(version); // SETVERSION -- set the version we had
		T saved = persistence.merge(entity); // MERGE -- hibernate will throw an exception if the version does not match the version in the DB
		persistence.getEntityManager().flush(); // FLUSH -- otherwise the version is not incremented yet
		return saved; // return the saved object with the updated version number (beware: it is only updated if there was an actual change)
	}

	/**
	 * Behandlung des Version-Attributes fuer OptimisticLocking.
	 * Nachdem die Business-Logik durchgefuehrt worden ist, stimmt moeglicherweise die
	 * Version bereits wieder nicht mehr. Darum muss am Schluss, also beim Konvertieren
	 * von Entity zurueck zu Jax, nochmals geflusht werden, damit der Client die
	 * richtige Version zurueckerhaelt, sonst klappt das naechste Speichern nicht mehr.
	 */
	private void flush() {
		persistence.getEntityManager().flush(); // FLUSH -- otherwise the version is not incremented yet
	}

	@Nonnull
	public JaxApplicationProperties applicationPropertyToJAX(@Nonnull final ApplicationProperty applicationProperty) {
		final JaxApplicationProperties jaxProperty = new JaxApplicationProperties();
		convertAbstractVorgaengerFieldsToJAX(applicationProperty, jaxProperty);
		jaxProperty.setName(applicationProperty.getName().toString());
		jaxProperty.setValue(applicationProperty.getValue());

		return jaxProperty;
	}

	@Nonnull
	public ApplicationProperty applicationPropertieToEntity(
		final JaxApplicationProperties jaxAP,
		@Nonnull final ApplicationProperty applicationProperty) {

		requireNonNull(applicationProperty);
		requireNonNull(jaxAP);

		convertAbstractVorgaengerFieldsToEntity(jaxAP, applicationProperty);
		applicationProperty.setName(Enum.valueOf(ApplicationPropertyKey.class, jaxAP.getName()));
		applicationProperty.setValue(jaxAP.getValue());

		return applicationProperty;
	}

	@Nonnull
	public JaxEinstellung einstellungToJAX(@Nonnull final Einstellung einstellung) {
		final JaxEinstellung jaxEinstellung = new JaxEinstellung();
		convertAbstractFieldsToJAX(einstellung, jaxEinstellung);
		jaxEinstellung.setKey(einstellung.getKey());
		jaxEinstellung.setValue(einstellung.getValue());
		jaxEinstellung.setGemeindeId(null == einstellung.getGemeinde() ? null : einstellung.getGemeinde().getId());
		jaxEinstellung.setGesuchsperiodeId(einstellung.getGesuchsperiode().getId());
		// Mandant wird aktuell nicht gemappt
		return jaxEinstellung;
	}

	@Nonnull
	public Einstellung einstellungToEntity(
		final JaxEinstellung jaxEinstellung,
		@Nonnull final Einstellung einstellung) {
		requireNonNull(einstellung);
		requireNonNull(jaxEinstellung);
		convertAbstractFieldsToEntity(jaxEinstellung, einstellung);
		einstellung.setKey(jaxEinstellung.getKey());
		einstellung.setValue(jaxEinstellung.getValue());
		if (jaxEinstellung.getGemeindeId() != null) {
			einstellung.setGemeinde(gemeindeService.findGemeinde(jaxEinstellung.getGemeindeId()).orElse(null));
		}
		final Optional<Gesuchsperiode> gesuchsperiode =
			gesuchsperiodeService.findGesuchsperiode(jaxEinstellung.getGesuchsperiodeId());
		if (!gesuchsperiode.isPresent()) {
			throw new EbeguEntityNotFoundException(
				"einstellungToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				jaxEinstellung.getGesuchsperiodeId());
		}
		einstellung.setGesuchsperiode(gesuchsperiode.get());
		// Mandant wird aktuell nicht gemappt
		return einstellung;
	}

	@Nonnull
	public GesuchstellerAdresse gesuchstellerAdresseToEntity(
		@Nonnull final JaxAdresse jaxAdresse,
		@Nonnull final GesuchstellerAdresse gesuchstellerAdresse) {

		adresseToEntity(jaxAdresse, gesuchstellerAdresse);
		gesuchstellerAdresse.setAdresseTyp(jaxAdresse.getAdresseTyp());
		gesuchstellerAdresse.setNichtInGemeinde(jaxAdresse.isNichtInGemeinde());

		return gesuchstellerAdresse;
	}

	@Nonnull
	public JaxAdresse gesuchstellerAdresseToJAX(@Nonnull final GesuchstellerAdresse gesuchstellerAdresse) {
		final JaxAdresse jaxAdresse = adresseToJAX(gesuchstellerAdresse);
		jaxAdresse.setAdresseTyp(gesuchstellerAdresse.getAdresseTyp());
		jaxAdresse.setNichtInGemeinde(gesuchstellerAdresse.isNichtInGemeinde());

		return jaxAdresse;
	}

	@Nonnull
	@CanIgnoreReturnValue
	public Adresse adresseToEntity(@Nonnull final JaxAdresse jaxAdresse, @Nonnull final Adresse adresse) {
		requireNonNull(adresse);
		requireNonNull(jaxAdresse);
		convertAbstractDateRangedFieldsToEntity(jaxAdresse, adresse);
		adresse.setStrasse(jaxAdresse.getStrasse());
		adresse.setHausnummer(jaxAdresse.getHausnummer());
		adresse.setZusatzzeile(jaxAdresse.getZusatzzeile());
		adresse.setPlz(jaxAdresse.getPlz());
		adresse.setOrt(jaxAdresse.getOrt());
		// Gemeinde ist read-only und wird nicht gesetzt
		adresse.setLand(jaxAdresse.getLand());
		adresse.setOrganisation(jaxAdresse.getOrganisation());
		//adresse gilt per default von start of time an
		adresse.getGueltigkeit().setGueltigAb(jaxAdresse.getGueltigAb() == null ?
			Constants.START_OF_TIME :
			jaxAdresse.getGueltigAb());

		return adresse;
	}

	@Nonnull
	public JaxAdresse adresseToJAX(@Nonnull final Adresse adresse) {
		final JaxAdresse jaxAdresse = new JaxAdresse();
		convertAbstractDateRangedFieldsToJAX(adresse, jaxAdresse);
		jaxAdresse.setStrasse(adresse.getStrasse());
		jaxAdresse.setHausnummer(adresse.getHausnummer());
		jaxAdresse.setZusatzzeile(adresse.getZusatzzeile());
		jaxAdresse.setPlz(adresse.getPlz());
		jaxAdresse.setOrt(adresse.getOrt());
		jaxAdresse.setGemeinde(adresse.getGemeinde());
		jaxAdresse.setBfsNummer(adresse.getBfsNummer());
		jaxAdresse.setLand(adresse.getLand());
		jaxAdresse.setOrganisation(adresse.getOrganisation());
		return jaxAdresse;
	}

	@Nonnull
	public JaxEnversRevision enversRevisionToJAX(
		@Nonnull final DefaultRevisionEntity revisionEntity,
		@Nonnull final AbstractEntity abstractEntity,
		final RevisionType accessType) {

		final JaxEnversRevision jaxEnversRevision = new JaxEnversRevision();
		if (abstractEntity instanceof ApplicationProperty) {
			jaxEnversRevision.setEntity(applicationPropertyToJAX((ApplicationProperty) abstractEntity));
		} else {
			throw new NotImplementedException("Diese Funktion ist erst fuer ApplicationProperties umgesetzt!");
		}
		jaxEnversRevision.setRev(revisionEntity.getId());
		jaxEnversRevision.setRevTimeStamp(requireNonNull(DateConvertUtils.asLocalDateTime(revisionEntity.getRevisionDate())));
		jaxEnversRevision.setAccessType(accessType);

		return jaxEnversRevision;
	}

	public Gesuchsteller gesuchstellerToEntity(
		@Nonnull final JaxGesuchsteller gesuchstellerJAXP,
		@Nonnull final Gesuchsteller gesuchsteller) {

		requireNonNull(gesuchsteller);
		requireNonNull(gesuchstellerJAXP);

		convertAbstractPersonFieldsToEntity(gesuchstellerJAXP, gesuchsteller);
		gesuchsteller.setMail(gesuchstellerJAXP.getMail());
		gesuchsteller.setTelefon(gesuchstellerJAXP.getTelefon());
		gesuchsteller.setMobile(gesuchstellerJAXP.getMobile());
		gesuchsteller.setTelefonAusland(gesuchstellerJAXP.getTelefonAusland());
		gesuchsteller.setDiplomatenstatus(gesuchstellerJAXP.isDiplomatenstatus());
		gesuchsteller.setKorrespondenzSprache(gesuchstellerJAXP.getKorrespondenzSprache());

		return gesuchsteller;
	}

	private void sortAndAddAdressenToGesuchstellerContainer(
		@Nonnull JaxGesuchstellerContainer gesuchstellerContJAXP,
		@Nonnull GesuchstellerContainer gesuchstellerCont) {
		// Zuerst wird geguckt, welche Entities nicht im JAX sind und werden dann geloescht
		for (Iterator<GesuchstellerAdresseContainer> iterator = gesuchstellerCont.getAdressen().iterator();
			 iterator.hasNext(); ) {
			GesuchstellerAdresseContainer next = iterator.next();
			boolean needsToBeRemoved = true;
			for (JaxAdresseContainer jaxAdresse : gesuchstellerContJAXP.getAdressen()) {
				if (next.extractIsKorrespondenzAdresse() || next.extractIsRechnungsAdresse() || next.getId()
					.equals(jaxAdresse.getId())) {
					// Korrespondezadresse, Rechnungsadresse und Adressen die gefunden werden, werden nicht geloescht
					needsToBeRemoved = false;
				}
			}
			if (needsToBeRemoved) {
				iterator.remove();
			}
		}
		// Jetzt werden alle Adressen vom Jax auf Entity kopiert
		gesuchstellerContJAXP.getAdressen()
			.forEach(jaxAdresse -> gesuchstellerCont.addAdresse(toStoreableAddresse(jaxAdresse)));

		// Zuletzt werden alle gueltigen Adressen sortiert und mit dem entsprechenden AB und BIS aktualisiert
		List<GesuchstellerAdresseContainer> wohnadressen = gesuchstellerCont.getAdressen().stream()
			.filter(gesuchstellerAdresse -> !gesuchstellerAdresse.extractIsKorrespondenzAdresse()
				&& !gesuchstellerAdresse.extractIsRechnungsAdresse())
			.sorted(Comparator.comparing(o -> requireNonNull(o.extractGueltigkeit()).getGueltigAb()))
			.collect(Collectors.toList());
		for (int i = 0; i < wohnadressen.size(); i++) {
			if ((i < wohnadressen.size() - 1)) {
				requireNonNull(wohnadressen.get(i).extractGueltigkeit()).setGueltigBis(
					requireNonNull(wohnadressen.get(i + 1).extractGueltigkeit()).getGueltigAb().minusDays(1));
			} else {
				requireNonNull(wohnadressen.get(i).extractGueltigkeit())
					.setGueltigBis(Constants.END_OF_TIME); // by default das letzte Datum hat BIS=END_OF_TIME
			}
		}
	}

	@Nonnull
	private GesuchstellerAdresseContainer toStoreableAddresse(
		@Nonnull final JaxAdresseContainer adresseToPrepareForSaving) {

		if (adresseToPrepareForSaving.getId() == null) {
			return gesuchstellerAdresseContainerToEntity(
				adresseToPrepareForSaving,
				new GesuchstellerAdresseContainer());
		}

		//wenn schon vorhanden updaten
		GesuchstellerAdresseContainer altAdr =
			gesuchstellerAdresseService.findAdresse(adresseToPrepareForSaving.getId())
				.orElseGet(GesuchstellerAdresseContainer::new);

		return gesuchstellerAdresseContainerToEntity(adresseToPrepareForSaving, altAdr);
	}

	public JaxGesuchstellerContainer gesuchstellerContainerToJAX(GesuchstellerContainer persistedGesuchstellerCont) {
		JaxGesuchstellerContainer jaxGesuchstellerCont = new JaxGesuchstellerContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedGesuchstellerCont, jaxGesuchstellerCont);

		if (persistedGesuchstellerCont.getGesuchstellerGS() != null) {
			jaxGesuchstellerCont.setGesuchstellerGS(gesuchstellerToJAX(persistedGesuchstellerCont.getGesuchstellerGS()));
		}
		if (persistedGesuchstellerCont.getGesuchstellerJA() != null) {
			jaxGesuchstellerCont.setGesuchstellerJA(gesuchstellerToJAX(persistedGesuchstellerCont.getGesuchstellerJA()));
		}

		if (!persistedGesuchstellerCont.isNew()) {
			//relationen laden
			final Optional<GesuchstellerAdresseContainer> alternativeAdr =
				gesuchstellerAdresseService.getKorrespondenzAdr(persistedGesuchstellerCont.getId());
			final Optional<GesuchstellerAdresseContainer> rechnungsAdr =
				gesuchstellerAdresseService.getRechnungsAdr(persistedGesuchstellerCont.getId());
			alternativeAdr.ifPresent(adresse -> jaxGesuchstellerCont.setAlternativeAdresse(
				gesuchstellerAdresseContainerToJAX(adresse)));
			rechnungsAdr.ifPresent(adresse -> jaxGesuchstellerCont.setRechnungsAdresse(
				gesuchstellerAdresseContainerToJAX(adresse)));

			jaxGesuchstellerCont.setAdressen(gesuchstellerAdresseContainerListToJAX(
				persistedGesuchstellerCont.getAdressen().stream().filter(gesuchstellerAdresse
					-> !gesuchstellerAdresse.extractIsKorrespondenzAdresse()
					&& !gesuchstellerAdresse.extractIsRechnungsAdresse()).sorted((o1, o2) ->
				{
					if (o1.extractGueltigkeit() == null && o2.extractGueltigkeit() == null) {
						return 0;
					}
					if (o1.extractGueltigkeit() == null) {
						return 1;
					}
					if (o2.extractGueltigkeit() == null) {
						return -1;
					}
					return requireNonNull(o1.extractGueltigkeit()).getGueltigAb().compareTo(
						requireNonNull(o2.extractGueltigkeit()).getGueltigAb());
				}).collect(Collectors.toList())
			));
		}

		// Finanzielle Situation
		if (persistedGesuchstellerCont.getFinanzielleSituationContainer() != null) {
			final JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer =
				finanzielleSituationContainerToJAX(persistedGesuchstellerCont
					.getFinanzielleSituationContainer());
			jaxGesuchstellerCont.setFinanzielleSituationContainer(jaxFinanzielleSituationContainer);
		}
		// Erwerbspensen
		final Collection<ErwerbspensumContainer> persistedPensen =
			persistedGesuchstellerCont.getErwerbspensenContainers();
		final List<JaxErwerbspensumContainer> listOfPensen =
			persistedPensen.stream().map(this::erwerbspensumContainerToJAX).collect(Collectors.toList());
		jaxGesuchstellerCont.setErwerbspensenContainers(listOfPensen);

		// Einkommensverschlechterung
		if (persistedGesuchstellerCont.getEinkommensverschlechterungContainer() != null) {
			final JaxEinkommensverschlechterungContainer jaxEinkVerContainer =
				einkommensverschlechterungContainerToJAX(persistedGesuchstellerCont
					.getEinkommensverschlechterungContainer());
			jaxGesuchstellerCont.setEinkommensverschlechterungContainer(jaxEinkVerContainer);
		}

		return jaxGesuchstellerCont;
	}

	private List<JaxAdresseContainer> gesuchstellerAdresseContainerListToJAX(
		@Nonnull Collection<GesuchstellerAdresseContainer> adressen) {

		return adressen.stream()
			.map(this::gesuchstellerAdresseContainerToJAX)
			.collect(Collectors.toList());
	}

	private JaxAdresseContainer gesuchstellerAdresseContainerToJAX(GesuchstellerAdresseContainer persistedAdresse) {
		JaxAdresseContainer jaxAdresse = new JaxAdresseContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedAdresse, jaxAdresse);

		if (persistedAdresse.getGesuchstellerAdresseGS() != null) {
			jaxAdresse.setAdresseGS(gesuchstellerAdresseToJAX(persistedAdresse.getGesuchstellerAdresseGS()));
		}
		if (persistedAdresse.getGesuchstellerAdresseJA() != null) {
			jaxAdresse.setAdresseJA(gesuchstellerAdresseToJAX(persistedAdresse.getGesuchstellerAdresseJA()));
		}
		return jaxAdresse;
	}

	@Nonnull
	public JaxGesuchsteller gesuchstellerToJAX(@Nonnull final Gesuchsteller persistedGesuchsteller) {
		Validate.isTrue(
			!persistedGesuchsteller.isNew(),
			"Gesuchsteller kann nicht nach REST transformiert werden weil sie noch " +
				"nicht persistiert wurde; Grund dafuer ist, dass wir die aktuelle Wohnadresse aus der Datenbank lesen "
				+ "wollen");
		final JaxGesuchsteller jaxGesuchsteller = new JaxGesuchsteller();
		convertAbstractPersonFieldsToJAX(persistedGesuchsteller, jaxGesuchsteller);
		jaxGesuchsteller.setMail(persistedGesuchsteller.getMail());
		jaxGesuchsteller.setTelefon(persistedGesuchsteller.getTelefon());
		jaxGesuchsteller.setMobile(persistedGesuchsteller.getMobile());
		jaxGesuchsteller.setTelefonAusland(persistedGesuchsteller.getTelefonAusland());
		jaxGesuchsteller.setDiplomatenstatus(persistedGesuchsteller.isDiplomatenstatus());
		jaxGesuchsteller.setKorrespondenzSprache(persistedGesuchsteller.getKorrespondenzSprache());

		return jaxGesuchsteller;
	}

	public Familiensituation familiensituationToEntity(
		@Nonnull final JaxFamiliensituation familiensituationJAXP,
		@Nonnull final Familiensituation familiensituation) {

		requireNonNull(familiensituation);
		requireNonNull(familiensituationJAXP);

		// wenn der Gesuchsteller keine Mahlzeitenvergünstigung wuenscht,
		// muessen wir sicher stellen, dass alle relevanten Felder wieder auf null gesetzt werden.
		// Falls er eine wuenscht, muss er mindestens die IBAN Nummer sowie den Kontoinhaber ausfuellen.
		if (!familiensituationJAXP.isKeineMahlzeitenverguenstigungBeantragt()) {
			familiensituation.setKeineMahlzeitenverguenstigungBeantragt(familiensituationJAXP.isKeineMahlzeitenverguenstigungBeantragt());
			if (familiensituationJAXP.getIban() != null) {
				familiensituation.setIban(new IBAN(familiensituationJAXP.getIban()));
			}
			familiensituation.setKontoinhaber(familiensituationJAXP.getKontoinhaber());
			familiensituation.setAbweichendeZahlungsadresse(familiensituationJAXP.isAbweichendeZahlungsadresse());

			if (familiensituationJAXP.getZahlungsadresse() != null) {
				familiensituation.setZahlungsadresse(adresseToEntity(familiensituationJAXP.getZahlungsadresse(),
					familiensituation.getZahlungsadresse() == null ? new Adresse() :
						familiensituation.getZahlungsadresse()));
			}
		} else {
			familiensituation.setIban(null);
			familiensituation.setKontoinhaber(null);
			familiensituation.setAbweichendeZahlungsadresse(false);
			familiensituation.setZahlungsadresse(null);
		}
		convertAbstractVorgaengerFieldsToEntity(familiensituationJAXP, familiensituation);
		familiensituation.setFamilienstatus(familiensituationJAXP.getFamilienstatus());
		familiensituation.setGemeinsameSteuererklaerung(familiensituationJAXP.getGemeinsameSteuererklaerung());
		familiensituation.setAenderungPer(familiensituationJAXP.getAenderungPer());
		familiensituation.setStartKonkubinat(familiensituationJAXP.getStartKonkubinat());
		familiensituation.setSozialhilfeBezueger(familiensituationJAXP.getSozialhilfeBezueger());
		familiensituation.setVerguenstigungGewuenscht(familiensituationJAXP.getVerguenstigungGewuenscht());

		return familiensituation;
	}

	public JaxFamiliensituation familiensituationToJAX(@Nonnull final Familiensituation persistedFamiliensituation) {
		final JaxFamiliensituation jaxFamiliensituation = new JaxFamiliensituation();
		convertAbstractVorgaengerFieldsToJAX(persistedFamiliensituation, jaxFamiliensituation);
		jaxFamiliensituation.setFamilienstatus(persistedFamiliensituation.getFamilienstatus());
		jaxFamiliensituation.setGemeinsameSteuererklaerung(persistedFamiliensituation.getGemeinsameSteuererklaerung());
		jaxFamiliensituation.setAenderungPer(persistedFamiliensituation.getAenderungPer());
		jaxFamiliensituation.setStartKonkubinat(persistedFamiliensituation.getStartKonkubinat());
		jaxFamiliensituation.setSozialhilfeBezueger(persistedFamiliensituation.getSozialhilfeBezueger());
		jaxFamiliensituation.setVerguenstigungGewuenscht(persistedFamiliensituation.getVerguenstigungGewuenscht());
		jaxFamiliensituation.setKeineMahlzeitenverguenstigungBeantragt(persistedFamiliensituation.isKeineMahlzeitenverguenstigungBeantragt());
		if (persistedFamiliensituation.getIban() != null) {
			jaxFamiliensituation.setIban(persistedFamiliensituation.getIban().getIban());
		}
		jaxFamiliensituation.setKontoinhaber(persistedFamiliensituation.getKontoinhaber());
		jaxFamiliensituation.setAbweichendeZahlungsadresse(persistedFamiliensituation.isAbweichendeZahlungsadresse());
		if (persistedFamiliensituation.getZahlungsadresse() != null) {
			jaxFamiliensituation.setZahlungsadresse(adresseToJAX(persistedFamiliensituation.getZahlungsadresse()));
		}

		return jaxFamiliensituation;
	}

	public FamiliensituationContainer familiensituationContainerToEntity(
		@Nonnull final JaxFamiliensituationContainer containerJAX,
		@Nonnull final FamiliensituationContainer container) {

		requireNonNull(container);
		requireNonNull(containerJAX);

		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);
		Familiensituation famsitToMergeWith;

		if (containerJAX.getFamiliensituationGS() != null) {
			famsitToMergeWith = Optional.ofNullable(container.getFamiliensituationGS())
				.orElseGet(Familiensituation::new);
			container.setFamiliensituationGS(familiensituationToEntity(
				containerJAX.getFamiliensituationGS(),
				famsitToMergeWith));
		}
		if (containerJAX.getFamiliensituationJA() != null) {
			famsitToMergeWith = Optional.ofNullable(container.getFamiliensituationJA())
				.orElseGet(Familiensituation::new);
			container.setFamiliensituationJA(familiensituationToEntity(
				containerJAX.getFamiliensituationJA(),
				famsitToMergeWith));
		}
		if (containerJAX.getFamiliensituationErstgesuch() != null) {
			famsitToMergeWith = Optional.ofNullable(container.getFamiliensituationErstgesuch())
				.orElseGet(Familiensituation::new);
			container.setFamiliensituationErstgesuch(familiensituationToEntity(
				containerJAX.getFamiliensituationErstgesuch(),
				famsitToMergeWith));
		}
		if (containerJAX.getSozialhilfeZeitraumContainers() != null) {
			sozialhilfeZeitraumContainersToEntity(containerJAX.getSozialhilfeZeitraumContainers(),
				container.getSozialhilfeZeitraumContainers());
		}

		return container;
	}

	public JaxEinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainerToJAX(
		final EinkommensverschlechterungInfoContainer
			persistedEinkommensverschlechterungInfo) {
		final JaxEinkommensverschlechterungInfoContainer jaxEkvic = new JaxEinkommensverschlechterungInfoContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedEinkommensverschlechterungInfo, jaxEkvic);
		if (persistedEinkommensverschlechterungInfo.getEinkommensverschlechterungInfoGS() != null) {
			jaxEkvic.setEinkommensverschlechterungInfoGS(einkommensverschlechterungInfoToJAX(
				persistedEinkommensverschlechterungInfo
					.getEinkommensverschlechterungInfoGS()));
		}
		jaxEkvic.setEinkommensverschlechterungInfoJA(einkommensverschlechterungInfoToJAX(
			persistedEinkommensverschlechterungInfo
				.getEinkommensverschlechterungInfoJA()));
		return jaxEkvic;
	}

	public EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainerToEntity(
		@Nonnull final JaxEinkommensverschlechterungInfoContainer containerJAX,
		@Nonnull final EinkommensverschlechterungInfoContainer container) {

		requireNonNull(container);
		requireNonNull(containerJAX);

		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);
		EinkommensverschlechterungInfo evkInfoToMergeWith;
		//Im moment kann eine einmal gespeicherte Finanzielle Situation nicht mehr entfernt werden.
		if (containerJAX.getEinkommensverschlechterungInfoGS() != null) {
			evkInfoToMergeWith = Optional.ofNullable(container.getEinkommensverschlechterungInfoGS())
				.orElseGet(EinkommensverschlechterungInfo::new);
			container.setEinkommensverschlechterungInfoGS(einkommensverschlechterungInfoToEntity(
				containerJAX.getEinkommensverschlechterungInfoGS(),
				evkInfoToMergeWith));
		}
		if (containerJAX.getEinkommensverschlechterungInfoJA() != null) {
			evkInfoToMergeWith = Optional.of(container.getEinkommensverschlechterungInfoJA())
				.orElseGet(EinkommensverschlechterungInfo::new);
			container.setEinkommensverschlechterungInfoJA(einkommensverschlechterungInfoToEntity(
				containerJAX.getEinkommensverschlechterungInfoJA(),
				evkInfoToMergeWith));
		}
		return container;
	}

	public JaxFamiliensituationContainer familiensituationContainerToJAX(final FamiliensituationContainer persistedFamiliensituation) {
		final JaxFamiliensituationContainer jaxfc = new JaxFamiliensituationContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedFamiliensituation, jaxfc);
		if (persistedFamiliensituation.getFamiliensituationGS() != null) {
			jaxfc.setFamiliensituationGS(familiensituationToJAX(persistedFamiliensituation.getFamiliensituationGS()));
		}
		if (persistedFamiliensituation.getFamiliensituationJA() != null) {
			jaxfc.setFamiliensituationJA(familiensituationToJAX(persistedFamiliensituation.getFamiliensituationJA()));
		}
		if (persistedFamiliensituation.getFamiliensituationErstgesuch() != null) {
			jaxfc.setFamiliensituationErstgesuch(familiensituationToJAX(persistedFamiliensituation.getFamiliensituationErstgesuch()));
		}

		jaxfc.setSozialhilfeZeitraumContainers(sozialhilfeZeitraumContainersToJAX(persistedFamiliensituation.getSozialhilfeZeitraumContainers()));

		return jaxfc;
	}

	public EinkommensverschlechterungInfo einkommensverschlechterungInfoToEntity(
		@Nonnull final JaxEinkommensverschlechterungInfo einkommensverschlechterungInfoJAXP,
		@Nonnull final EinkommensverschlechterungInfo einkommensverschlechterungInfo) {

		requireNonNull(einkommensverschlechterungInfo);
		requireNonNull(einkommensverschlechterungInfoJAXP);

		convertAbstractVorgaengerFieldsToEntity(einkommensverschlechterungInfoJAXP, einkommensverschlechterungInfo);
		einkommensverschlechterungInfo.setEinkommensverschlechterung(einkommensverschlechterungInfoJAXP.getEinkommensverschlechterung());
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(einkommensverschlechterungInfoJAXP.getEkvFuerBasisJahrPlus1());
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(einkommensverschlechterungInfoJAXP.getEkvFuerBasisJahrPlus2());
		einkommensverschlechterungInfo.setEkvBasisJahrPlus1Annulliert(einkommensverschlechterungInfoJAXP.getEkvBasisJahrPlus1Annulliert());
		einkommensverschlechterungInfo.setEkvBasisJahrPlus2Annulliert(einkommensverschlechterungInfoJAXP.getEkvBasisJahrPlus2Annulliert());
		return einkommensverschlechterungInfo;
	}

	public JaxEinkommensverschlechterungInfo einkommensverschlechterungInfoToJAX(
		@Nonnull final EinkommensverschlechterungInfo persistedEinkommensverschlechterungInfo) {

		final JaxEinkommensverschlechterungInfo ekvi = new JaxEinkommensverschlechterungInfo();
		convertAbstractVorgaengerFieldsToJAX(persistedEinkommensverschlechterungInfo, ekvi);

		ekvi.setEinkommensverschlechterung(persistedEinkommensverschlechterungInfo.getEinkommensverschlechterung());
		ekvi.setEkvFuerBasisJahrPlus1(persistedEinkommensverschlechterungInfo.getEkvFuerBasisJahrPlus1());
		ekvi.setEkvFuerBasisJahrPlus2(persistedEinkommensverschlechterungInfo.getEkvFuerBasisJahrPlus2());
		ekvi.setEkvBasisJahrPlus1Annulliert(persistedEinkommensverschlechterungInfo.getEkvBasisJahrPlus1Annulliert());
		ekvi.setEkvBasisJahrPlus2Annulliert(persistedEinkommensverschlechterungInfo.getEkvBasisJahrPlus2Annulliert());

		return ekvi;
	}

	public Fall fallToEntity(@Nonnull final JaxFall fallJAXP, @Nonnull final Fall fall) {
		requireNonNull(fall);
		requireNonNull(fallJAXP);
		convertAbstractVorgaengerFieldsToEntity(fallJAXP, fall);
		//Fall nummer wird auf server bzw DB verwaltet und daher hier nicht gesetzt, dasselbe fuer NextKindNumber
		if (fallJAXP.getBesitzer() != null) {
			Optional<Benutzer> besitzer = benutzerService.findBenutzer(fallJAXP.getBesitzer().getUsername());
			if (besitzer.isPresent()) {
				fall.setBesitzer(besitzer.get()); // because the user doesn't come from the client but from the server
			} else {
				throw new EbeguEntityNotFoundException(
					"fallToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					fallJAXP.getBesitzer());
			}
		}
		return fall;
	}

	public JaxFall fallToJAX(@Nonnull final Fall persistedFall) {
		final JaxFall jaxFall = new JaxFall();
		convertAbstractVorgaengerFieldsToJAX(persistedFall, jaxFall);
		jaxFall.setFallNummer(persistedFall.getFallNummer());
		jaxFall.setNextNumberKind(persistedFall.getNextNumberKind());
		if (persistedFall.getBesitzer() != null) {
			jaxFall.setBesitzer(benutzerToJaxBenutzer(persistedFall.getBesitzer()));
		}
		return jaxFall;
	}

	public Dossier dossierToEntity(@Nonnull final JaxDossier dossierJAX, @Nonnull final Dossier dossier) {
		requireNonNull(dossier);
		requireNonNull(dossierJAX);
		requireNonNull(dossierJAX.getFall());
		requireNonNull(dossierJAX.getFall().getId());
		convertAbstractVorgaengerFieldsToEntity(dossierJAX, dossier);
		// Fall darf nicht überschrieben werden
		final Optional<Fall> fallFromDB = fallService.findFall(dossierJAX.getFall().getId());
		if (fallFromDB.isPresent()) {
			dossier.setFall(fallFromDB.get());
		} else {
			throw new EbeguEntityNotFoundException(
				DOSSIER_TO_ENTITY,
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				dossierJAX.getFall());
		}
		// Gemeinde darf nicht ueberschrieben werden
		if (dossierJAX.getGemeinde() != null) {
			requireNonNull(dossierJAX.getGemeinde().getId());
			Optional<Gemeinde> gemeindeFromDB = gemeindeService.findGemeinde(dossierJAX.getGemeinde().getId());
			if (gemeindeFromDB.isPresent()) {
				dossier.setGemeinde(gemeindeFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					DOSSIER_TO_ENTITY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAX.getFall());
			}
		}
		if (dossierJAX.getVerantwortlicherBG() != null) {
			Optional<Benutzer> verantwortlicher =
				benutzerService.findBenutzer(dossierJAX.getVerantwortlicherBG().getUsername());
			if (verantwortlicher.isPresent()) {
				// because the user doesn't come from the client but from the server
				dossier.setVerantwortlicherBG(verantwortlicher.get());
			} else {
				throw new EbeguEntityNotFoundException(
					DOSSIER_TO_ENTITY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAX.getVerantwortlicherBG());
			}
		} else {
			dossier.setVerantwortlicherBG(null);
		}
		if (dossierJAX.getVerantwortlicherTS() != null) {
			Optional<Benutzer> verantwortlicherTS =
				benutzerService.findBenutzer(dossierJAX.getVerantwortlicherTS().getUsername());
			if (verantwortlicherTS.isPresent()) {
				// because the user doesn't come from the client but from the server
				dossier.setVerantwortlicherTS(verantwortlicherTS.get());
			} else {
				throw new EbeguEntityNotFoundException(
					DOSSIER_TO_ENTITY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAX.getVerantwortlicherTS());
			}
		} else {
			dossier.setVerantwortlicherTS(null);
		}
		return dossier;
	}

	public JaxDossier dossierToJAX(@Nonnull final Dossier persistedDossier) {
		final JaxDossier jaxDossier = new JaxDossier();
		convertAbstractVorgaengerFieldsToJAX(persistedDossier, jaxDossier);
		jaxDossier.setFall(this.fallToJAX(persistedDossier.getFall()));
		jaxDossier.setGemeinde(gemeindeToJAX(persistedDossier.getGemeinde()));
		if (persistedDossier.getVerantwortlicherBG() != null) {
			jaxDossier.setVerantwortlicherBG(benutzerToJaxBenutzerNoDetails(persistedDossier.getVerantwortlicherBG()));
		}
		if (persistedDossier.getVerantwortlicherTS() != null) {
			jaxDossier.setVerantwortlicherTS(benutzerToJaxBenutzerNoDetails(persistedDossier.getVerantwortlicherTS()));
		}
		return jaxDossier;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	public Gesuch gesuchToEntity(@Nonnull final JaxGesuch antragJAXP, @Nonnull final Gesuch antrag) {
		requireNonNull(antrag);
		requireNonNull(antragJAXP);
		requireNonNull(antragJAXP.getDossier());
		requireNonNull(antragJAXP.getDossier().getId());

		convertAbstractVorgaengerFieldsToEntity(antragJAXP, antrag);
		final String exceptionString = "gesuchToEntity";

		Optional<Dossier> dossierOptional = dossierService.findDossier(antragJAXP.getDossier().getId());
		if (dossierOptional.isPresent()) {
			antrag.setDossier(this.dossierToEntity(antragJAXP.getDossier(), dossierOptional.get()));
		} else {
			throw new EbeguEntityNotFoundException(
				exceptionString,
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				antragJAXP.getDossier());
		}
		if (antragJAXP.getGesuchsperiode() != null && antragJAXP.getGesuchsperiode().getId() != null) {
			final Optional<Gesuchsperiode> gesuchsperiode =
				gesuchsperiodeService.findGesuchsperiode(antragJAXP.getGesuchsperiode().getId());
			if (gesuchsperiode.isPresent()) {
				// Gesuchsperiode darf nicht vom Client ueberschrieben werden
				antrag.setGesuchsperiode(gesuchsperiode.get());
			} else {
				throw new EbeguEntityNotFoundException(
					exceptionString,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					antragJAXP.getGesuchsperiode().getId());
			}
		}

		antrag.setEingangsdatum(antragJAXP.getEingangsdatum());
		antrag.setRegelnGueltigAb(antragJAXP.getRegelnGueltigAb());
		antrag.setFreigabeDatum(antragJAXP.getFreigabeDatum());
		antrag.setStatus(AntragStatusConverterUtil.convertStatusToEntity(antragJAXP.getStatus()));
		if (antragJAXP.getTyp() != null) {
			antrag.setTyp(antragJAXP.getTyp());
		}
		antrag.setEingangsart(antragJAXP.getEingangsart());

		if (antragJAXP.getGesuchsteller1() != null && antragJAXP.getGesuchsteller1().getId() != null) {
			final Optional<GesuchstellerContainer> gesuchsteller1 =
				gesuchstellerService.findGesuchsteller(antragJAXP.getGesuchsteller1().getId());
			if (gesuchsteller1.isPresent()) {
				antrag.setGesuchsteller1(gesuchstellerContainerToEntity(
					antragJAXP.getGesuchsteller1(),
					gesuchsteller1.get()));
			} else {
				throw new EbeguEntityNotFoundException(
					exceptionString,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					antragJAXP.getGesuchsteller1().getId());
			}
		}
		if (antragJAXP.getGesuchsteller2() != null && antragJAXP.getGesuchsteller2().getId() != null) {
			final Optional<GesuchstellerContainer> gesuchsteller2 =
				gesuchstellerService.findGesuchsteller(antragJAXP.getGesuchsteller2().getId());
			if (gesuchsteller2.isPresent()) {
				antrag.setGesuchsteller2(gesuchstellerContainerToEntity(
					antragJAXP.getGesuchsteller2(),
					gesuchsteller2.get()));
			} else {
				throw new EbeguEntityNotFoundException(
					exceptionString,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					antragJAXP.getGesuchsteller2().getId());
			}
		}
		if (antragJAXP.getFamiliensituationContainer() != null) {
			if (antragJAXP.getFamiliensituationContainer().getId() != null) {
				final Optional<FamiliensituationContainer> familiensituationContainer =
					familiensituationService.findFamiliensituation(antragJAXP
						.getFamiliensituationContainer().getId());
				if (familiensituationContainer.isPresent()) {
					antrag.setFamiliensituationContainer(familiensituationContainerToEntity(
						antragJAXP.getFamiliensituationContainer(),
						familiensituationContainer.get()));
				} else {
					throw new EbeguEntityNotFoundException(
						exceptionString,
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						antragJAXP.getFamiliensituationContainer()
							.getId());
				}
			} else {
				antrag.setFamiliensituationContainer(familiensituationContainerToEntity(
					antragJAXP.getFamiliensituationContainer(),
					new
						FamiliensituationContainer()));
			}
		}

		if (antragJAXP.getEinkommensverschlechterungInfoContainer() != null) {
			if (antragJAXP.getEinkommensverschlechterungInfoContainer().getId() != null) {
				final Optional<EinkommensverschlechterungInfoContainer> evkiSituation =
					einkommensverschlechterungInfoService
						.findEinkommensverschlechterungInfo(antragJAXP.getEinkommensverschlechterungInfoContainer()
							.getId());
				if (evkiSituation.isPresent()) {
					antrag.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfoContainerToEntity(
						antragJAXP
							.getEinkommensverschlechterungInfoContainer(),
						evkiSituation.get()));
				} else {
					throw new EbeguEntityNotFoundException(
						exceptionString,
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						antragJAXP
							.getEinkommensverschlechterungInfoContainer().getId());
				}
			} else {
				antrag.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfoContainerToEntity(
					antragJAXP
						.getEinkommensverschlechterungInfoContainer(),
					new EinkommensverschlechterungInfoContainer()));
			}
		}

		antrag.setBemerkungen(antragJAXP.getBemerkungen());
		antrag.setBemerkungenSTV(antragJAXP.getBemerkungenSTV());
		antrag.setBemerkungenPruefungSTV(antragJAXP.getBemerkungenPruefungSTV());
		antrag.setLaufnummer(antragJAXP.getLaufnummer());
		antrag.setGesuchBetreuungenStatus(antragJAXP.getGesuchBetreuungenStatus());
		antrag.setGeprueftSTV(antragJAXP.isGeprueftSTV());
		antrag.setVerfuegungEingeschrieben(antragJAXP.isVerfuegungEingeschrieben());
		antrag.setGesperrtWegenBeschwerde(antragJAXP.isGesperrtWegenBeschwerde());
		antrag.setFinSitStatus(antragJAXP.getFinSitStatus());
		antrag.setDokumenteHochgeladen(antragJAXP.isDokumenteHochgeladen());
		return antrag;
	}

	public GesuchstellerAdresseContainer gesuchstellerAdresseContainerToEntity(
		JaxAdresseContainer jaxAdresseCont,
		GesuchstellerAdresseContainer adresseCont) {

		requireNonNull(jaxAdresseCont);
		requireNonNull(adresseCont);

		convertAbstractVorgaengerFieldsToEntity(jaxAdresseCont, adresseCont);
		// ein einmal erstellter GS Container kann nie mehr entfernt werden, daher mergen wir hier nichts wenn null
		// kommt vom client
		if (jaxAdresseCont.getAdresseGS() != null) {
			GesuchstellerAdresse gesuchstellerAdresseGS = new GesuchstellerAdresse();
			if (adresseCont.getGesuchstellerAdresseGS() != null) {
				gesuchstellerAdresseGS = adresseCont.getGesuchstellerAdresseGS();
			}
			adresseCont.setGesuchstellerAdresseGS(gesuchstellerAdresseToEntity(
				jaxAdresseCont.getAdresseGS(),
				gesuchstellerAdresseGS));
		}
		// ein erstellter AdresseJA Container kann durch das Jugendamt entfernt werden wenn es sich um eine
		// Korrespondenzaddr oder eine Rechnungsaddr handelt
		if (jaxAdresseCont.getAdresseJA() != null) {
			GesuchstellerAdresse gesuchstellerAdresseJA = new GesuchstellerAdresse();
			if (adresseCont.getGesuchstellerAdresseJA() != null) {
				gesuchstellerAdresseJA = adresseCont.getGesuchstellerAdresseJA();
			}
			adresseCont.setGesuchstellerAdresseJA(gesuchstellerAdresseToEntity(
				jaxAdresseCont.getAdresseJA(),
				gesuchstellerAdresseJA));
		} else {
			Validate.isTrue(
				adresseCont.extractIsKorrespondenzAdresse() || adresseCont.extractIsRechnungsAdresse(),
				"Nur bei der Korrespondenz- oder "
					+ "Rechnungsadresse kann der AdresseJA Container entfernt werden");
			adresseCont.setGesuchstellerAdresseJA(null);
		}

		return adresseCont;
	}

	public GesuchstellerContainer gesuchstellerContainerToEntity(
		JaxGesuchstellerContainer jaxGesuchstellerCont,
		GesuchstellerContainer gesuchstellerCont) {
		requireNonNull(gesuchstellerCont);
		requireNonNull(jaxGesuchstellerCont);
		requireNonNull(jaxGesuchstellerCont.getAdressen(), "Adressen muessen gesetzt sein");

		convertAbstractVorgaengerFieldsToEntity(jaxGesuchstellerCont, gesuchstellerCont);
		//kind daten koennen nicht verschwinden
		if (jaxGesuchstellerCont.getGesuchstellerGS() != null) {
			Gesuchsteller gesuchstellerGS = new Gesuchsteller();
			if (gesuchstellerCont.getGesuchstellerGS() != null) {
				gesuchstellerGS = gesuchstellerCont.getGesuchstellerGS();
			}
			gesuchstellerCont.setGesuchstellerGS(gesuchstellerToEntity(
				jaxGesuchstellerCont.getGesuchstellerGS(),
				gesuchstellerGS));
		}
		if (jaxGesuchstellerCont.getGesuchstellerJA() != null) {
			Gesuchsteller gesuchstellerJA = new Gesuchsteller();
			if (gesuchstellerCont.getGesuchstellerJA() != null) {
				gesuchstellerJA = gesuchstellerCont.getGesuchstellerJA();
			}
			gesuchstellerCont.setGesuchstellerJA(gesuchstellerToEntity(
				jaxGesuchstellerCont.getGesuchstellerJA(),
				gesuchstellerJA));
		}

		//Relationen
		//Wir fuehren derzeit immer maximal  eine alternative Korrespondenzadressse -> diese updaten wenn vorhanden
		if (jaxGesuchstellerCont.getAlternativeAdresse() != null) {
			final GesuchstellerAdresseContainer currentAltAdr = gesuchstellerAdresseService
				.getKorrespondenzAdr(gesuchstellerCont.getId()).orElse(new GesuchstellerAdresseContainer());
			final GesuchstellerAdresseContainer altAddrToMerge = gesuchstellerAdresseContainerToEntity(
				jaxGesuchstellerCont.getAlternativeAdresse(),
				currentAltAdr);
			gesuchstellerCont.addAdresse(altAddrToMerge);
		} else {
			//else case: Wenn das haeklein "Zustell / Postadresse" auf client weggenommen wird muss die
			// Korrespondezadr auf dem Server geloescht werden.
			gesuchstellerCont.getAdressen().removeIf(GesuchstellerAdresseContainer::extractIsKorrespondenzAdresse);
		}
		if (jaxGesuchstellerCont.getRechnungsAdresse() != null) {
			final GesuchstellerAdresseContainer currentrechnungsAdr = gesuchstellerAdresseService
				.getRechnungsAdr(gesuchstellerCont.getId()).orElse(new GesuchstellerAdresseContainer());
			final GesuchstellerAdresseContainer rechnungsAddrToMerge = gesuchstellerAdresseContainerToEntity(
				jaxGesuchstellerCont.getRechnungsAdresse(),
				currentrechnungsAdr);
			gesuchstellerCont.addAdresse(rechnungsAddrToMerge);
		} else {
			//else case: Wenn das haeklein "abweichende Rchnungsadresse" auf client weggenommen wird muss diese
			// adresse auf dem Server geloescht werden.
			gesuchstellerCont.getAdressen().removeIf(GesuchstellerAdresseContainer::extractIsRechnungsAdresse);
		}
		sortAndAddAdressenToGesuchstellerContainer(jaxGesuchstellerCont, gesuchstellerCont);

		// Finanzielle Situation
		if (jaxGesuchstellerCont.getFinanzielleSituationContainer() != null) {
			gesuchstellerCont.setFinanzielleSituationContainer(finanzielleSituationContainerToStorableEntity(
				jaxGesuchstellerCont.getFinanzielleSituationContainer(),
				gesuchstellerCont.getFinanzielleSituationContainer()));
		}
		//Erwerbspensum
		requireNonNull(jaxGesuchstellerCont.getErwerbspensenContainers())
			.stream()
			.map(this::erwerbspensumContainerToStoreableEntity)
			.forEach(gesuchstellerCont::addErwerbspensumContainer);

		//Einkommensverschlechterung
		final JaxEinkommensverschlechterungContainer einkommensverschlechterungContainer =
			jaxGesuchstellerCont.getEinkommensverschlechterungContainer();
		if (einkommensverschlechterungContainer != null) {
			gesuchstellerCont.setEinkommensverschlechterungContainer(einkommensverschlechterungContainerToStorableEntity(
				einkommensverschlechterungContainer));
		}

		return gesuchstellerCont;
	}

	public JaxGesuch gesuchToJAX(@Nonnull final Gesuch persistedGesuch) {
		final JaxGesuch jaxGesuch = new JaxGesuch();
		convertAbstractVorgaengerFieldsToJAX(persistedGesuch, jaxGesuch);
		jaxGesuch.setDossier(this.dossierToJAX(persistedGesuch.getDossier()));
		if (persistedGesuch.getGesuchsperiode() != null) {
			jaxGesuch.setGesuchsperiode(gesuchsperiodeToJAX(persistedGesuch.getGesuchsperiode()));
		}
		jaxGesuch.setEingangsdatum(persistedGesuch.getEingangsdatum());
		jaxGesuch.setRegelnGueltigAb(persistedGesuch.getRegelnGueltigAb());
		jaxGesuch.setFreigabeDatum(persistedGesuch.getFreigabeDatum());
		jaxGesuch.setStatus(AntragStatusConverterUtil.convertStatusToDTO(
			persistedGesuch,
			persistedGesuch.getStatus()));
		jaxGesuch.setTyp(persistedGesuch.getTyp());
		jaxGesuch.setEingangsart(persistedGesuch.getEingangsart());

		if (persistedGesuch.getGesuchsteller1() != null) {
			jaxGesuch.setGesuchsteller1(this.gesuchstellerContainerToJAX(persistedGesuch.getGesuchsteller1()));
		}
		if (persistedGesuch.getGesuchsteller2() != null) {
			jaxGesuch.setGesuchsteller2(this.gesuchstellerContainerToJAX(persistedGesuch.getGesuchsteller2()));
		}
		if (persistedGesuch.getFamiliensituationContainer() != null) {
			jaxGesuch.setFamiliensituationContainer(this.familiensituationContainerToJAX(persistedGesuch.getFamiliensituationContainer()));
		}
		for (final KindContainer kind : persistedGesuch.getKindContainers()) {
			jaxGesuch.getKindContainers().add(kindContainerToJAX(kind));
		}
		if (persistedGesuch.getEinkommensverschlechterungInfoContainer() != null) {
			jaxGesuch.setEinkommensverschlechterungInfoContainer(this.einkommensverschlechterungInfoContainerToJAX(
				persistedGesuch
					.getEinkommensverschlechterungInfoContainer()));
		}
		jaxGesuch.setBemerkungen(persistedGesuch.getBemerkungen());
		jaxGesuch.setBemerkungenSTV(persistedGesuch.getBemerkungenSTV());
		jaxGesuch.setBemerkungenPruefungSTV(persistedGesuch.getBemerkungenPruefungSTV());
		jaxGesuch.setLaufnummer(persistedGesuch.getLaufnummer());
		jaxGesuch.setGesuchBetreuungenStatus(persistedGesuch.getGesuchBetreuungenStatus());
		jaxGesuch.setGeprueftSTV(persistedGesuch.isGeprueftSTV());
		jaxGesuch.setVerfuegungEingeschrieben(persistedGesuch.isVerfuegungEingeschrieben());
		jaxGesuch.setGesperrtWegenBeschwerde(persistedGesuch.isGesperrtWegenBeschwerde());
		jaxGesuch.setDatumGewarntNichtFreigegeben(persistedGesuch.getDatumGewarntNichtFreigegeben());
		jaxGesuch.setDatumGewarntFehlendeQuittung(persistedGesuch.getDatumGewarntFehlendeQuittung());
		jaxGesuch.setTimestampVerfuegt(persistedGesuch.getTimestampVerfuegt());
		jaxGesuch.setGueltig(persistedGesuch.isGueltig());
		jaxGesuch.setDokumenteHochgeladen(persistedGesuch.getDokumenteHochgeladen());
		jaxGesuch.setFinSitStatus(persistedGesuch.getFinSitStatus());
		return jaxGesuch;
	}

	public JaxMandant mandantToJAX(@Nonnull final Mandant persistedMandant) {
		final JaxMandant jaxMandant = new JaxMandant();
		convertAbstractVorgaengerFieldsToJAX(persistedMandant, jaxMandant);
		jaxMandant.setName(persistedMandant.getName());
		jaxMandant.setAngebotTS(persistedMandant.isAngebotTS());
		jaxMandant.setAngebotFI(persistedMandant.isAngebotFI());
		return jaxMandant;
	}

	/**
	 * Diese Methode verwenden nur wenn man der Institution Count und InstitutionNamen benoetigt
	 */
	public JaxTraegerschaft traegerschaftToJAX(final Traegerschaft persistedTraegerschaft) {
		final JaxTraegerschaft jaxTraegerschaft = new JaxTraegerschaft();
		convertAbstractVorgaengerFieldsToJAX(persistedTraegerschaft, jaxTraegerschaft);
		jaxTraegerschaft.setName(persistedTraegerschaft.getName());
		jaxTraegerschaft.setActive(persistedTraegerschaft.getActive());

		Collection<Institution> institutionen =
			institutionService.getAllInstitutionenFromTraegerschaft(persistedTraegerschaft.getId());
		// its enough if we just pass the names here, we only want to display it later
		jaxTraegerschaft.setInstitutionNames(institutionen.stream()
			.map(Institution::getName)
			.collect(Collectors.joining(", ")));
		jaxTraegerschaft.setInstitutionCount(institutionen.size());
		return jaxTraegerschaft;
	}

	/**
	 * Diese Methode verwenden ausser wenn man der Institution Count und InstitutionNamen benoetigt
	 */
	public JaxTraegerschaft traegerschaftLightToJAX(final Traegerschaft persistedTraegerschaft) {
		final JaxTraegerschaft jaxTraegerschaft = new JaxTraegerschaft();
		convertAbstractVorgaengerFieldsToJAX(persistedTraegerschaft, jaxTraegerschaft);
		jaxTraegerschaft.setName(persistedTraegerschaft.getName());
		jaxTraegerschaft.setActive(persistedTraegerschaft.getActive());
		return jaxTraegerschaft;
	}

	public Mandant mandantToEntity(final JaxMandant mandantJAXP, final Mandant mandant) {
		requireNonNull(mandant);
		requireNonNull(mandantJAXP);
		convertAbstractVorgaengerFieldsToEntity(mandantJAXP, mandant);
		mandant.setName(mandantJAXP.getName());
		mandant.setAngebotTS(mandantJAXP.isAngebotTS());
		mandant.setAngebotFI(mandantJAXP.isAngebotFI());
		return mandant;
	}

	public Traegerschaft traegerschaftToEntity(
		@Nonnull final JaxTraegerschaft traegerschaftJAXP,
		@Nonnull final Traegerschaft traegerschaft) {

		requireNonNull(traegerschaft);
		requireNonNull(traegerschaftJAXP);
		convertAbstractVorgaengerFieldsToEntity(traegerschaftJAXP, traegerschaft);
		traegerschaft.setName(traegerschaftJAXP.getName());
		traegerschaft.setActive(traegerschaftJAXP.getActive());

		return traegerschaft;
	}

	public Fachstelle fachstelleToEntity(final JaxFachstelle fachstelleJAXP, final Fachstelle fachstelle) {
		requireNonNull(fachstelleJAXP);
		requireNonNull(fachstelle);
		convertAbstractVorgaengerFieldsToEntity(fachstelleJAXP, fachstelle);
		fachstelle.setName(fachstelleJAXP.getName());
		fachstelle.setFachstelleAnspruch(fachstelleJAXP.isFachstelleAnspruch());
		fachstelle.setFachstelleErweiterteBetreuung(fachstelleJAXP.isFachstelleErweiterteBetreuung());
		return fachstelle;
	}

	public JaxFachstelle fachstelleToJAX(@Nonnull final Fachstelle persistedFachstelle) {
		final JaxFachstelle jaxFachstelle = new JaxFachstelle();
		convertAbstractVorgaengerFieldsToJAX(persistedFachstelle, jaxFachstelle);
		jaxFachstelle.setName(persistedFachstelle.getName());
		jaxFachstelle.setFachstelleAnspruch(persistedFachstelle.isFachstelleAnspruch());
		jaxFachstelle.setFachstelleErweiterteBetreuung(persistedFachstelle.isFachstelleErweiterteBetreuung());
		return jaxFachstelle;
	}

	@Nonnull
	public JaxExternalClient externalClientToJAX(@Nonnull final ExternalClient persistedExternalClient) {
		JaxExternalClient jaxExternalClient = new JaxExternalClient();
		convertAbstractFieldsToJAX(persistedExternalClient, jaxExternalClient);
		jaxExternalClient.setClientName(persistedExternalClient.getClientName());
		jaxExternalClient.setType(persistedExternalClient.getType());

		return jaxExternalClient;
	}

	public JaxInstitution institutionToJAX(final Institution persistedInstitution) {
		final JaxInstitution jaxInstitution = new JaxInstitution();
		convertAbstractVorgaengerFieldsToJAX(persistedInstitution, jaxInstitution);
		jaxInstitution.setName(persistedInstitution.getName());
		Objects.requireNonNull(persistedInstitution.getMandant());
		jaxInstitution.setMandant(mandantToJAX(persistedInstitution.getMandant()));
		jaxInstitution.setStatus(persistedInstitution.getStatus());
		jaxInstitution.setStammdatenCheckRequired(persistedInstitution.isStammdatenCheckRequired());
		if (persistedInstitution.getTraegerschaft() != null) {
			jaxInstitution.setTraegerschaft(traegerschaftLightToJAX(persistedInstitution.getTraegerschaft()));
		}
		return jaxInstitution;
	}

	public JaxInstitutionListDTO institutionListDTOToJAX(final Entry<Institution, InstitutionStammdaten> entry) {
		final JaxInstitutionListDTO jaxInstitutionListDTO = new JaxInstitutionListDTO();
		convertAbstractVorgaengerFieldsToJAX(entry.getKey(), jaxInstitutionListDTO);
		jaxInstitutionListDTO.setName(entry.getKey().getName());
		Objects.requireNonNull(entry.getKey().getMandant());
		jaxInstitutionListDTO.setMandant(mandantToJAX(entry.getKey().getMandant()));
		jaxInstitutionListDTO.setStatus(entry.getKey().getStatus());
		jaxInstitutionListDTO.setStammdatenCheckRequired(entry.getKey().isStammdatenCheckRequired());

		if (entry.getKey().getTraegerschaft() != null) {
			jaxInstitutionListDTO.setTraegerschaft(traegerschaftLightToJAX(entry.getKey().getTraegerschaft()));
		}

		jaxInstitutionListDTO.setBetreuungsangebotTyp(entry.getValue().getBetreuungsangebotTyp());

		return jaxInstitutionListDTO;
	}

	public boolean institutionToEntity(@Nonnull JaxInstitutionUpdate update, @Nonnull Institution institution,
		@Nonnull InstitutionStammdaten stammdaten) {
		boolean nameUpdated = updateName(update, institution);
		boolean traegerschaftUpdated = updateTraegerschaft(update, institution);
		boolean statusUpdated = updateStatus(institution, stammdaten);

		return nameUpdated || traegerschaftUpdated || statusUpdated;
	}

	/**
	 * @return TRUE when the name of the institution was updated
	 */
	private boolean updateName(@Nonnull JaxInstitutionUpdate update, @Nonnull Institution institution) {
		Optional<String> newName = update.getName()
			// we are only interrested in the value, when it is different
			.filter(name -> !institution.getName().equals(name));

		newName.ifPresent(institution::setName);

		return newName.isPresent();
	}

	/**
	 * @return TRUE when the Traegerschaft of the institution was updated
	 */
	private boolean updateTraegerschaft(@Nonnull JaxInstitutionUpdate update, @Nonnull Institution institution) {
		if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)) {
			// only SUPER_ADMIN may change Traegerschaft
			return false;
		}

		Traegerschaft newTraegerschaft = update.getTraegerschaftId()
			.flatMap(id -> traegerschaftService.findTraegerschaft(id))
			.orElse(null);

		if (!Objects.equals(institution.getTraegerschaft(), newTraegerschaft)) {
			institution.setTraegerschaft(newTraegerschaft);

			return true;
		}

		return false;
	}

	/**
	 * @return TRUE when the Status of the institution was updated
	 */
	private boolean updateStatus(@Nonnull Institution institution, @Nonnull InstitutionStammdaten stammdaten) {
		if (institution.getStatus() == InstitutionStatus.EINGELADEN ||
			(institution.getStatus() == InstitutionStatus.KONFIGURATION && stammdaten.isTagesschuleActivatable()) ||
			(institution.getStatus() == InstitutionStatus.KONFIGURATION && stammdaten.getInstitutionStammdatenFerieninsel() != null)) {
			institution.setStatus(InstitutionStatus.AKTIV);
			return true;
		}

		return false;
	}

	@Nonnull
	public Institution institutionToNewEntity(@Nonnull JaxInstitution institutionJAXP) {
		requireNonNull(institutionJAXP);
		Institution institution = new Institution();
		convertAbstractVorgaengerFieldsToEntity(institutionJAXP, institution);
		institution.setName(institutionJAXP.getName());
		institution.setStatus(institutionJAXP.getStatus());
		institution.setStammdatenCheckRequired(institutionJAXP.isStammdatenCheckRequired());

		if (institutionJAXP.getMandant() != null && institutionJAXP.getMandant().getId() != null) {
			final Optional<Mandant> mandantFromDB = mandantService.findMandant(institutionJAXP.getMandant().getId());
			if (mandantFromDB.isPresent()) {
				// Mandant darf nicht vom Client ueberschrieben werden
				institution.setMandant(mandantFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"institutionToEntity -> mandant",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					institutionJAXP.getMandant().getId());
			}
		}

		// Traegerschaft ist nicht required!
		Traegerschaft traegerschaft = Optional.ofNullable(institutionJAXP.getTraegerschaft())
			.map(JaxTraegerschaft::getId)
			.flatMap(id -> traegerschaftService.findTraegerschaft(id))
			.orElse(null);

		// Traegerschaft darf nicht vom Client ueberschrieben werden
		institution.setTraegerschaft(traegerschaft);

		return institution;
	}

	public <T extends JaxAbstractInstitutionStammdaten> T institutionStammdatenSummaryToJAX(
		@Nonnull final InstitutionStammdaten persistedInstStammdaten,
		@Nonnull final T jaxInstStammdaten
	) {
		convertAbstractDateRangedFieldsToJAX(persistedInstStammdaten, jaxInstStammdaten);

		jaxInstStammdaten.setBetreuungsangebotTyp(persistedInstStammdaten.getBetreuungsangebotTyp());
		jaxInstStammdaten.setMail(persistedInstStammdaten.getMail());
		jaxInstStammdaten.setTelefon(persistedInstStammdaten.getTelefon());
		jaxInstStammdaten.setWebseite(persistedInstStammdaten.getWebseite());
		jaxInstStammdaten.setOeffnungszeiten(persistedInstStammdaten.getOeffnungszeiten());
		if (persistedInstStammdaten.getInstitutionStammdatenBetreuungsgutscheine() != null) {
			jaxInstStammdaten.setInstitutionStammdatenBetreuungsgutscheine(
				institutionStammdatenBetreuungsgutscheineToJAX(
					persistedInstStammdaten.getInstitutionStammdatenBetreuungsgutscheine()
				));
		}
		if (persistedInstStammdaten.getInstitutionStammdatenTagesschule() != null) {
			jaxInstStammdaten.setInstitutionStammdatenTagesschule(institutionStammdatenTagesschuleToJAX(
				persistedInstStammdaten
					.getInstitutionStammdatenTagesschule()));
		}
		if (persistedInstStammdaten.getInstitutionStammdatenFerieninsel() != null) {
			jaxInstStammdaten.setInstitutionStammdatenFerieninsel(institutionStammdatenFerieninselToJAX(
				persistedInstStammdaten
					.getInstitutionStammdatenFerieninsel()));
		}
		jaxInstStammdaten.setInstitution(institutionToJAX(persistedInstStammdaten.getInstitution()));
		jaxInstStammdaten.setAdresse(adresseToJAX(persistedInstStammdaten.getAdresse()));
		jaxInstStammdaten.setSendMailWennOffenePendenzen(persistedInstStammdaten.getSendMailWennOffenePendenzen());
		return jaxInstStammdaten;
	}

	public JaxInstitutionStammdaten institutionStammdatenToJAX(
		@Nonnull final InstitutionStammdaten persistedInstStammdaten
	) {
		final JaxInstitutionStammdaten jaxInstStammdaten =
			institutionStammdatenSummaryToJAX(persistedInstStammdaten, new JaxInstitutionStammdaten());

		Collection<Benutzer> administratoren = benutzerService.getInstitutionAdministratoren(
			persistedInstStammdaten.getInstitution());
		Collection<Benutzer> sachbearbeiter = benutzerService.getInstitutionSachbearbeiter(
			persistedInstStammdaten.getInstitution());
		jaxInstStammdaten.setAdministratoren(administratoren.stream()
			.map(Benutzer::getFullName)
			.collect(Collectors.joining(", ")));
		jaxInstStammdaten.setSachbearbeiter(sachbearbeiter.stream()
			.map(Benutzer::getFullName)
			.collect(Collectors.joining(", ")));
		return jaxInstStammdaten;
	}

	public void institutionStammdatenToEntity(
		@Nonnull JaxInstitutionStammdaten institutionStammdatenJAXP,
		@Nonnull InstitutionStammdaten institutionStammdaten) {

		requireNonNull(institutionStammdatenJAXP);
		requireNonNull(institutionStammdaten);

		convertAbstractDateRangedFieldsToEntity(institutionStammdatenJAXP, institutionStammdaten);

		institutionStammdaten.setMail(institutionStammdatenJAXP.getMail());
		institutionStammdaten.setTelefon(institutionStammdatenJAXP.getTelefon());
		institutionStammdaten.setWebseite(institutionStammdatenJAXP.getWebseite());
		institutionStammdaten.setBetreuungsangebotTyp(institutionStammdatenJAXP.getBetreuungsangebotTyp());
		if (institutionStammdatenJAXP.getInstitutionStammdatenBetreuungsgutscheine() != null) {
			// wenn InstitutionStammdatenBetreuungsgutscheine vorhanden ist es ein BG und Objekt muss, wenn noch
			// nicht vorhanden, erzeugt werden
			InstitutionStammdatenBetreuungsgutscheine isBG =
				Optional.ofNullable(institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine())
					.orElseGet(InstitutionStammdatenBetreuungsgutscheine::new);

			InstitutionStammdatenBetreuungsgutscheine convertedIsBG =
				institutionStammdatenBetreuungsgutscheineToEntity(
					institutionStammdatenJAXP.getInstitutionStammdatenBetreuungsgutscheine(),
					isBG
				);
			institutionStammdaten.setInstitutionStammdatenBetreuungsgutscheine(convertedIsBG);
		}
		if (institutionStammdatenJAXP.getInstitutionStammdatenTagesschule() != null) {
			// wenn InstitutionStammdatenTagesschule vorhanden ist es eine Tagesschule und Objekt muss, wenn noch
			// nicht vorhanden, erzeugt werden
			InstitutionStammdatenTagesschule isTS =
				Optional.ofNullable(institutionStammdaten.getInstitutionStammdatenTagesschule())
					.orElseGet(InstitutionStammdatenTagesschule::new);

			InstitutionStammdatenTagesschule convertedIsTS = institutionStammdatenTagesschuleToEntity(
				institutionStammdatenJAXP.getInstitutionStammdatenTagesschule(),
				isTS
			);
			institutionStammdaten.setInstitutionStammdatenTagesschule(convertedIsTS);
		}
		if (institutionStammdatenJAXP.getInstitutionStammdatenFerieninsel() != null) {
			InstitutionStammdatenFerieninsel isFI =
				Optional.ofNullable(institutionStammdaten.getInstitutionStammdatenFerieninsel())
					.orElseGet(InstitutionStammdatenFerieninsel::new);

			InstitutionStammdatenFerieninsel convertedIsFI = institutionStammdatenFerieninselToEntity(
				institutionStammdatenJAXP.getInstitutionStammdatenFerieninsel(),
				isFI
			);
			institutionStammdaten.setInstitutionStammdatenFerieninsel(convertedIsFI);
		}
		institutionStammdaten.setSendMailWennOffenePendenzen(institutionStammdatenJAXP.isSendMailWennOffenePendenzen());

		adresseToEntity(institutionStammdatenJAXP.getAdresse(), institutionStammdaten.getAdresse());
	}

	@Nonnull
	public JaxInstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheineToJAX(
		@Nonnull final InstitutionStammdatenBetreuungsgutscheine persistedInstStammdaten
	) {
		final JaxInstitutionStammdatenBetreuungsgutscheine jaxInstStammdaten =
			new JaxInstitutionStammdatenBetreuungsgutscheine();
		convertAbstractFieldsToJAX(persistedInstStammdaten, jaxInstStammdaten);

		if (persistedInstStammdaten.getIban() != null) {
			jaxInstStammdaten.setIban(persistedInstStammdaten.getIban().getIban());
		}
		jaxInstStammdaten.setKontoinhaber(persistedInstStammdaten.getKontoinhaber());
		jaxInstStammdaten.setAlterskategorieBaby(persistedInstStammdaten.getAlterskategorieBaby());
		jaxInstStammdaten.setAlterskategorieVorschule(persistedInstStammdaten.getAlterskategorieVorschule());
		jaxInstStammdaten.setAlterskategorieKindergarten(persistedInstStammdaten.getAlterskategorieKindergarten());
		jaxInstStammdaten.setAlterskategorieSchule(persistedInstStammdaten.getAlterskategorieSchule());
		jaxInstStammdaten.setSubventioniertePlaetze(persistedInstStammdaten.getSubventioniertePlaetze());
		jaxInstStammdaten.setAnzahlPlaetze(persistedInstStammdaten.getAnzahlPlaetze());
		jaxInstStammdaten.setAnzahlPlaetzeFirmen(persistedInstStammdaten.getAnzahlPlaetzeFirmen());
		jaxInstStammdaten.setTarifProHauptmahlzeit(persistedInstStammdaten.getTarifProHauptmahlzeit());
		jaxInstStammdaten.setTarifProNebenmahlzeit(persistedInstStammdaten.getTarifProNebenmahlzeit());
		jaxInstStammdaten.setOeffnungstage(persistedInstStammdaten.getOeffnungsTage());
		jaxInstStammdaten.setOeffnungsAbweichungen(persistedInstStammdaten.getOeffnungsAbweichungen());
		if (persistedInstStammdaten.getOffenVon() != null) {
			jaxInstStammdaten.setOffenVon(dateToHoursAndMinutes(persistedInstStammdaten.getOffenVon()));
		}
		if (persistedInstStammdaten.getOffenBis() != null) {
			jaxInstStammdaten.setOffenBis(dateToHoursAndMinutes(persistedInstStammdaten.getOffenBis()));
		}

		jaxInstStammdaten.setBetreuungsstandorte(betreuungsstandortListToJax(persistedInstStammdaten.getBetreuungsstandorte()));

		if (persistedInstStammdaten.getAdresseKontoinhaber() != null) {
			jaxInstStammdaten.setAdresseKontoinhaber(adresseToJAX(persistedInstStammdaten.getAdresseKontoinhaber()));
		}
		return jaxInstStammdaten;
	}

	@Nonnull
	private Set<JaxBetreuungsstandort> betreuungsstandortListToJax(@Nullable final Set<Betreuungsstandort> betreuungsstandorte) {
		if (betreuungsstandorte == null) {
			return new HashSet<>();
		}

		return betreuungsstandorte.stream()
			.map(this::betreuungsstandortToJax)
			.collect(Collectors.toSet());
	}

	private JaxBetreuungsstandort betreuungsstandortToJax(Betreuungsstandort betreuungsstandort) {
		final JaxBetreuungsstandort jaxBetreuungsstandort = new JaxBetreuungsstandort();
		convertAbstractFieldsToJAX(betreuungsstandort, jaxBetreuungsstandort);
		jaxBetreuungsstandort.setAdresse(adresseToJAX(betreuungsstandort.getAdresse()));
		jaxBetreuungsstandort.setMail(betreuungsstandort.getMail());
		jaxBetreuungsstandort.setTelefon(betreuungsstandort.getTelefon());
		jaxBetreuungsstandort.setWebseite(betreuungsstandort.getWebseite());
		return jaxBetreuungsstandort;
	}

	@Nonnull
	public InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheineToEntity(
		@Nonnull final JaxInstitutionStammdatenBetreuungsgutscheine institutionStammdatenJAXP,
		@Nonnull final InstitutionStammdatenBetreuungsgutscheine institutionStammdaten
	) {
		convertAbstractFieldsToEntity(institutionStammdatenJAXP, institutionStammdaten);
		if (institutionStammdatenJAXP.getIban() != null) {
			institutionStammdaten.setIban(new IBAN(institutionStammdatenJAXP.getIban()));
		}
		institutionStammdaten.setKontoinhaber(institutionStammdatenJAXP.getKontoinhaber());
		institutionStammdaten.setAlterskategorieBaby(institutionStammdatenJAXP.isAlterskategorieBaby());
		institutionStammdaten.setAlterskategorieVorschule(institutionStammdatenJAXP.isAlterskategorieVorschule());
		institutionStammdaten.setAlterskategorieKindergarten(institutionStammdatenJAXP.isAlterskategorieKindergarten());
		institutionStammdaten.setAlterskategorieSchule(institutionStammdatenJAXP.isAlterskategorieSchule());
		institutionStammdaten.setSubventioniertePlaetze(institutionStammdatenJAXP.isSubventioniertePlaetze());
		institutionStammdaten.setAnzahlPlaetze(institutionStammdatenJAXP.getAnzahlPlaetze());
		institutionStammdaten.setAnzahlPlaetzeFirmen(institutionStammdatenJAXP.getAnzahlPlaetzeFirmen());
		institutionStammdaten.setTarifProHauptmahlzeit(institutionStammdatenJAXP.getTarifProHauptmahlzeit());
		institutionStammdaten.setTarifProNebenmahlzeit(institutionStammdatenJAXP.getTarifProNebenmahlzeit());
		institutionStammdaten.setOeffnungsTage(institutionStammdatenJAXP.getOeffnungstage());
		institutionStammdaten.setOeffnungsAbweichungen(institutionStammdatenJAXP.getOeffnungsAbweichungen());
		if (institutionStammdatenJAXP.getOffenVon() != null) {
			institutionStammdaten.setOffenVon(hoursAndMinutesToDate(institutionStammdatenJAXP.getOffenVon()));
		}
		if (institutionStammdatenJAXP.getOffenBis() != null) {
			institutionStammdaten.setOffenBis(hoursAndMinutesToDate(institutionStammdatenJAXP.getOffenBis()));
		}

		institutionStammdaten.setBetreuungsstandorte(betreuungsstandortListToEntity(
			institutionStammdatenJAXP.getBetreuungsstandorte(),
			institutionStammdaten.getBetreuungsstandorte(),
			institutionStammdaten)
		);

		Adresse convertedAdresse = null;
		if (institutionStammdatenJAXP.getAdresseKontoinhaber() != null) {
			Adresse a = Optional.ofNullable(institutionStammdaten.getAdresseKontoinhaber()).orElseGet(Adresse::new);
			convertedAdresse = adresseToEntity(institutionStammdatenJAXP.getAdresseKontoinhaber(), a);
		}
		institutionStammdaten.setAdresseKontoinhaber(convertedAdresse);
		return institutionStammdaten;
	}

	private Set<Betreuungsstandort> betreuungsstandortListToEntity(@Nonnull Set<JaxBetreuungsstandort> jaxBetreuungsstandortList,
		@Nonnull Set<Betreuungsstandort> betreuungsstandortList,
		@Nonnull InstitutionStammdatenBetreuungsgutscheine owner) {
		final List<Betreuungsstandort> convertedBetreuungsstandorte = new ArrayList<>();
		for (final JaxBetreuungsstandort jaxBetreuungsstandort : jaxBetreuungsstandortList) {
			final Betreuungsstandort betreuungsstandorteToMergeWith = betreuungsstandortList
				.stream()
				.filter(existingStandort -> existingStandort.getId().equals(jaxBetreuungsstandort.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(Betreuungsstandort::new);
			final Betreuungsstandort betreuungsstandortToAdd =
				betreuungsstandortToEntity(jaxBetreuungsstandort, betreuungsstandorteToMergeWith);
			betreuungsstandortToAdd.setInstitutionStammdatenBetreuungsgutscheine(owner);
			if (convertedBetreuungsstandorte.contains(betreuungsstandortToAdd)) {
				LOGGER.warn("dropped duplicate Betreuungsstandort {}", betreuungsstandortToAdd);
			} else {
				convertedBetreuungsstandorte.add(betreuungsstandortToAdd);
			}
		}
		betreuungsstandortList.clear();
		betreuungsstandortList.addAll(convertedBetreuungsstandorte);
		return betreuungsstandortList;
	}

	private Betreuungsstandort betreuungsstandortToEntity(
		JaxBetreuungsstandort jaxBetreuungsstandort,
		Betreuungsstandort betreuungsstandort) {

		convertAbstractFieldsToEntity(jaxBetreuungsstandort, betreuungsstandort);

		betreuungsstandort.setAdresse(adresseToEntity(
			jaxBetreuungsstandort.getAdresse(),
			betreuungsstandort.getAdresse()));
		betreuungsstandort.setMail(jaxBetreuungsstandort.getMail());
		betreuungsstandort.setTelefon(jaxBetreuungsstandort.getTelefon());
		betreuungsstandort.setWebseite(jaxBetreuungsstandort.getWebseite());

		return betreuungsstandort;
	}

	@Nonnull
	public JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninselToJAX(
		@Nonnull final InstitutionStammdatenFerieninsel persistedInstStammdatenFerieninsel) {

		final JaxInstitutionStammdatenFerieninsel jaxInstStammdatenFerieninsel =
			new JaxInstitutionStammdatenFerieninsel();
		convertAbstractFieldsToJAX(persistedInstStammdatenFerieninsel, jaxInstStammdatenFerieninsel);
		jaxInstStammdatenFerieninsel.setGemeinde(gemeindeToJAX(persistedInstStammdatenFerieninsel.getGemeinde()));

		jaxInstStammdatenFerieninsel.setEinstellungenFerieninsel(
			persistedInstStammdatenFerieninsel.getEinstellungenFerieninsel()
				.stream()
				.map(this::einstellungFerieninselToJAX)
				.collect(Collectors.toSet())
		);

		return jaxInstStammdatenFerieninsel;
	}

	@Nonnull
	private JaxEinstellungenFerieninsel einstellungFerieninselToJAX(
		@Nonnull final EinstellungenFerieninsel persistedEinstellungFerieninsel
	) {
		JaxEinstellungenFerieninsel jaxEinstellungFI = new JaxEinstellungenFerieninsel();
		convertAbstractFieldsToJAX(persistedEinstellungFerieninsel, jaxEinstellungFI);
		jaxEinstellungFI.setAusweichstandortFruehlingsferien(persistedEinstellungFerieninsel.getAusweichstandortFruehlingsferien());
		jaxEinstellungFI.setAusweichstandortHerbstferien(persistedEinstellungFerieninsel.getAusweichstandortHerbstferien());
		jaxEinstellungFI.setAusweichstandortSommerferien(persistedEinstellungFerieninsel.getAusweichstandortSommerferien());
		jaxEinstellungFI.setAusweichstandortSportferien(persistedEinstellungFerieninsel.getAusweichstandortSportferien());
		jaxEinstellungFI.setGesuchsperiode(gesuchsperiodeToJAX(persistedEinstellungFerieninsel.getGesuchsperiode()));

		return jaxEinstellungFI;
	}

	@Nullable
	public InstitutionStammdatenFerieninsel institutionStammdatenFerieninselToEntity(
		final JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninselJAXP,
		final InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel) {

		requireNonNull(institutionStammdatenFerieninselJAXP);
		requireNonNull(institutionStammdatenFerieninsel);

		convertAbstractFieldsToEntity(
			institutionStammdatenFerieninselJAXP,
			institutionStammdatenFerieninsel
		);

		// Die Gemeinde muss neu von der DB gelesen werden
		String gemeindeID = institutionStammdatenFerieninselJAXP.getGemeinde().getId();
		Objects.requireNonNull(gemeindeID);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeID)
			.orElseThrow(() -> new EbeguRuntimeException(
				"findGemeinde",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gemeindeID));
		institutionStammdatenFerieninsel.setGemeinde(gemeinde);

		Set<EinstellungenFerieninsel> convertedEinstellungenFerieninsel = einstellungenTagesschuleListToEntity(
			institutionStammdatenFerieninselJAXP.getEinstellungenFerieninsel(),
			institutionStammdatenFerieninsel.getEinstellungenFerieninsel(),
			institutionStammdatenFerieninsel);

		institutionStammdatenFerieninsel.getEinstellungenFerieninsel().clear();
		institutionStammdatenFerieninsel.getEinstellungenFerieninsel().addAll(convertedEinstellungenFerieninsel);

		return institutionStammdatenFerieninsel;
	}

	@Nonnull
	private Set<EinstellungenFerieninsel> einstellungenTagesschuleListToEntity(
		@Nonnull Set<JaxEinstellungenFerieninsel> jaxEinstellungenFerieninselSet,
		@Nonnull Set<EinstellungenFerieninsel> einstellungenFerieninselSet,
		@Nonnull InstitutionStammdatenFerieninsel owner) {

		final Set<EinstellungenFerieninsel> convertedEinstellungen = new TreeSet<>();
		for (final JaxEinstellungenFerieninsel jaxEinstellung : jaxEinstellungenFerieninselSet) {
			final EinstellungenFerieninsel einstellungenToMergeWith = einstellungenFerieninselSet
				.stream()
				.filter(existingEinstellung -> existingEinstellung.getId().equals(jaxEinstellung.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(EinstellungenFerieninsel::new);
			final EinstellungenFerieninsel einstellungToAdd =
				einstellungFerieninselToEntity(jaxEinstellung, einstellungenToMergeWith);
			einstellungToAdd.setInstitutionStammdatenFerieninsel(owner);
			final boolean added = convertedEinstellungen.add(einstellungToAdd);
			if (!added) {
				LOGGER.warn("dropped duplicate EinstellungenTagesschule {}", einstellungToAdd);
			}
		}
		return convertedEinstellungen;
	}

	@Nonnull
	private EinstellungenFerieninsel einstellungFerieninselToEntity(
		@Nonnull final JaxEinstellungenFerieninsel jaxEinstellungFerieninsel,
		@Nonnull EinstellungenFerieninsel einstellungFerieninsel
	) {

		convertAbstractFieldsToEntity(jaxEinstellungFerieninsel, einstellungFerieninsel);

		einstellungFerieninsel.setAusweichstandortFruehlingsferien(jaxEinstellungFerieninsel.getAusweichstandortFruehlingsferien());
		einstellungFerieninsel.setAusweichstandortHerbstferien(jaxEinstellungFerieninsel.getAusweichstandortHerbstferien());
		einstellungFerieninsel.setAusweichstandortSommerferien(jaxEinstellungFerieninsel.getAusweichstandortSommerferien());
		einstellungFerieninsel.setAusweichstandortSportferien(jaxEinstellungFerieninsel.getAusweichstandortSportferien());

		// Die Gesuchsperiode muss neu von der DB gelesen werden
		String gesuchsperiodeId = jaxEinstellungFerieninsel.getGesuchsperiode().getId();
		Objects.requireNonNull(gesuchsperiodeId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new EbeguRuntimeException(
				"einstellungenTagesschuleToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));
		einstellungFerieninsel.setGesuchsperiode(gesuchsperiode);

		return einstellungFerieninsel;
	}

	public JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschuleToJAX(
		@Nonnull final InstitutionStammdatenTagesschule persistedInstStammdatenTagesschule) {

		final JaxInstitutionStammdatenTagesschule jaxInstStammdatenTagesschule =
			new JaxInstitutionStammdatenTagesschule();
		convertAbstractFieldsToJAX(persistedInstStammdatenTagesschule, jaxInstStammdatenTagesschule);
		jaxInstStammdatenTagesschule.setGemeinde(gemeindeToJAX(persistedInstStammdatenTagesschule.getGemeinde()));
		jaxInstStammdatenTagesschule.setEinstellungenTagesschule(einstellungenTagesschuleListToJAX(persistedInstStammdatenTagesschule.getEinstellungenTagesschule()));
		return jaxInstStammdatenTagesschule;
	}

	@Nullable
	public InstitutionStammdatenTagesschule institutionStammdatenTagesschuleToEntity(
		final JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschuleJAXP,
		final InstitutionStammdatenTagesschule institutionStammdatenTagesschule) {

		requireNonNull(institutionStammdatenTagesschuleJAXP);
		requireNonNull(institutionStammdatenTagesschule);

		convertAbstractFieldsToEntity(institutionStammdatenTagesschuleJAXP, institutionStammdatenTagesschule);

		// Die Gemeinde muss neu von der DB gelesen werden
		String gemeindeID = institutionStammdatenTagesschuleJAXP.getGemeinde().getId();
		Objects.requireNonNull(gemeindeID);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeID)
			.orElseThrow(() -> new EbeguRuntimeException(
				"findGemeinde",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gemeindeID));
		institutionStammdatenTagesschule.setGemeinde(gemeinde);

		final Set<EinstellungenTagesschule> convertedEinstellungenTagesschule =
			einstellungenTagesschuleListToEntity(institutionStammdatenTagesschuleJAXP.getEinstellungenTagesschule(),
				institutionStammdatenTagesschule.getEinstellungenTagesschule(), institutionStammdatenTagesschule);
		//change the existing collection to reflect changes
		// Already tested: All existing module of the list remain as they were, that means their data are updated
		// and the objects are not created again. ID and InsertTimeStamp are the same as before
		institutionStammdatenTagesschule.getEinstellungenTagesschule().clear();
		institutionStammdatenTagesschule.getEinstellungenTagesschule().addAll(convertedEinstellungenTagesschule);
		return institutionStammdatenTagesschule;
	}

	@Nonnull
	private Set<JaxEinstellungenTagesschule> einstellungenTagesschuleListToJAX(@Nullable final Set<EinstellungenTagesschule> einstellungenTagesschuleSet) {
		if (einstellungenTagesschuleSet == null) {
			return Collections.emptySet();
		}
		return einstellungenTagesschuleSet.stream()
			.map(this::einstellungenTagesschuleToJAX)
			.collect(Collectors.toSet());
	}

	@Nonnull
	private JaxEinstellungenTagesschule einstellungenTagesschuleToJAX(@Nonnull final EinstellungenTagesschule persistedEinstellungenTagesschule) {
		final JaxEinstellungenTagesschule jaxEinstellungenTagesschule = new JaxEinstellungenTagesschule();

		convertAbstractFieldsToJAX(persistedEinstellungenTagesschule, jaxEinstellungenTagesschule);
		jaxEinstellungenTagesschule.setGesuchsperiode(gesuchsperiodeToJAX(persistedEinstellungenTagesschule.getGesuchsperiode()));
		jaxEinstellungenTagesschule.setModulTagesschuleGroups(modulTagesschuleGroupListToJax(persistedEinstellungenTagesschule.getModulTagesschuleGroups()));
		jaxEinstellungenTagesschule.setModulTagesschuleTyp(persistedEinstellungenTagesschule.getModulTagesschuleTyp());
		jaxEinstellungenTagesschule.setErlaeuterung(persistedEinstellungenTagesschule.getErlaeuterung());
		jaxEinstellungenTagesschule.setTagi(persistedEinstellungenTagesschule.isTagi());
		return jaxEinstellungenTagesschule;
	}

	@Nonnull
	private Set<EinstellungenTagesschule> einstellungenTagesschuleListToEntity(
		@Nonnull Set<JaxEinstellungenTagesschule> jaxEinstellungenTagesschuleSet,
		@Nonnull Set<EinstellungenTagesschule> einstellungenTagesschuleSet,
		@Nonnull InstitutionStammdatenTagesschule owner) {

		final Set<EinstellungenTagesschule> convertedEinstellungen = new TreeSet<>();
		for (final JaxEinstellungenTagesschule jaxEinstellung : jaxEinstellungenTagesschuleSet) {
			final EinstellungenTagesschule einstellungenToMergeWith = einstellungenTagesschuleSet
				.stream()
				.filter(existingEinstellung -> existingEinstellung.getId().equals(jaxEinstellung.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(EinstellungenTagesschule::new);
			final EinstellungenTagesschule einstellungToAdd =
				einstellungenTagesschuleToEntity(jaxEinstellung, einstellungenToMergeWith);
			einstellungToAdd.setInstitutionStammdatenTagesschule(owner);
			final boolean added = convertedEinstellungen.add(einstellungToAdd);
			if (!added) {
				LOGGER.warn("dropped duplicate EinstellungenTagesschule {}", einstellungToAdd);
			}
		}
		return convertedEinstellungen;
	}

	@Nonnull
	private EinstellungenTagesschule einstellungenTagesschuleToEntity(
		final JaxEinstellungenTagesschule jaxEinstellungenTagesschule,
		final EinstellungenTagesschule einstellungenTagesschule
	) {
		requireNonNull(jaxEinstellungenTagesschule);
		requireNonNull(einstellungenTagesschule);

		convertAbstractFieldsToEntity(jaxEinstellungenTagesschule, einstellungenTagesschule);

		// Die Gesuchsperiode muss neu von der DB gelesen werden
		String gesuchsperiodeId = jaxEinstellungenTagesschule.getGesuchsperiode().getId();
		Objects.requireNonNull(gesuchsperiodeId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new EbeguRuntimeException(
				"einstellungenTagesschuleToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));
		einstellungenTagesschule.setGesuchsperiode(gesuchsperiode);

		final Set<ModulTagesschuleGroup> convertedModuleTagesschule =
			modulTagesschuleGroupListToEntity(jaxEinstellungenTagesschule.getModulTagesschuleGroups(),
				einstellungenTagesschule.getModulTagesschuleGroups(), einstellungenTagesschule);
		if (convertedModuleTagesschule != null) {
			//change the existing collection to reflect changes
			// Already tested: All existing module of the list remain as they were, that means their data are updated
			// and the objects are not created again. ID and InsertTimeStamp are the same as before
			einstellungenTagesschule.getModulTagesschuleGroups().clear();
			einstellungenTagesschule.getModulTagesschuleGroups().addAll(convertedModuleTagesschule);
		}

		einstellungenTagesschule.setModulTagesschuleTyp(jaxEinstellungenTagesschule.getModulTagesschuleTyp());
		einstellungenTagesschule.setErlaeuterung(jaxEinstellungenTagesschule.getErlaeuterung());
		einstellungenTagesschule.setTagi(jaxEinstellungenTagesschule.isTagi());

		return einstellungenTagesschule;
	}

	@Nullable
	private ModulTagesschuleGroup modulTagesschuleGroupToEntity(
		@Nullable JaxModulTagesschuleGroup jaxModulTagesschuleGroup,
		@Nonnull ModulTagesschuleGroup modulTagesschuleGroup,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule) {

		if (jaxModulTagesschuleGroup == null) {
			return null;
		}

		convertAbstractFieldsToEntity(jaxModulTagesschuleGroup, modulTagesschuleGroup);
		modulTagesschuleGroup.setEinstellungenTagesschule(einstellungenTagesschule);
		modulTagesschuleGroup.setModulTagesschuleName(jaxModulTagesschuleGroup.getModulTagesschuleName());
		modulTagesschuleGroup.setIdentifier(jaxModulTagesschuleGroup.getIdentifier());
		modulTagesschuleGroup.setBezeichnung(textRessourceToEntity(
			jaxModulTagesschuleGroup.getBezeichnung(), modulTagesschuleGroup.getBezeichnung()));
		modulTagesschuleGroup.setZeitVon(hoursAndMinutesToDate(jaxModulTagesschuleGroup.getZeitVon()));
		modulTagesschuleGroup.setZeitBis(hoursAndMinutesToDate(jaxModulTagesschuleGroup.getZeitBis()));
		modulTagesschuleGroup.setVerpflegungskosten(jaxModulTagesschuleGroup.getVerpflegungskosten());
		modulTagesschuleGroup.setIntervall(jaxModulTagesschuleGroup.getIntervall());
		modulTagesschuleGroup.setWirdPaedagogischBetreut(jaxModulTagesschuleGroup.isWirdPaedagogischBetreut());
		modulTagesschuleGroup.setReihenfolge(jaxModulTagesschuleGroup.getReihenfolge());

		Set<ModulTagesschule> convertedModules = moduleTagesschuleListToEntity(jaxModulTagesschuleGroup.getModule(),
			modulTagesschuleGroup.getModule(),
			einstellungenTagesschule);
		if (convertedModules != null) {
			for (ModulTagesschule convertedModule : convertedModules) {
				convertedModule.setModulTagesschuleGroup(modulTagesschuleGroup);
			}
		}
		if (convertedModules != null) {
			modulTagesschuleGroup.getModule().clear();
			modulTagesschuleGroup.getModule().addAll(convertedModules);
		}

		return modulTagesschuleGroup;
	}

	@Nullable
	private Set<ModulTagesschuleGroup> modulTagesschuleGroupListToEntity(
		@Nullable List<JaxModulTagesschuleGroup> jaxModulTagesschuleGroups,
		@Nullable Set<ModulTagesschuleGroup> modulTagesschuleGroupsOfInstitution,
		@Nonnull EinstellungenTagesschule institutionStammdatenTagesschule) {

		if (modulTagesschuleGroupsOfInstitution != null && jaxModulTagesschuleGroups != null) {
			final Set<ModulTagesschuleGroup> transformedModule = new TreeSet<>();
			for (final JaxModulTagesschuleGroup jaxModulTagesschule : jaxModulTagesschuleGroups) {
				final ModulTagesschuleGroup modulTagesschuleToMergeWith = modulTagesschuleGroupsOfInstitution.stream()
					.filter(existingModul -> existingModul.getId().equalsIgnoreCase(jaxModulTagesschule.getId()))
					.reduce(StreamsUtil.toOnlyElement())
					.orElse(new ModulTagesschuleGroup());
				final ModulTagesschuleGroup modulTagesschuleToAdd =
					modulTagesschuleGroupToEntity(jaxModulTagesschule, modulTagesschuleToMergeWith,
						institutionStammdatenTagesschule);
				if (modulTagesschuleToAdd != null) {
					final boolean added = transformedModule.add(modulTagesschuleToAdd);
					if (!added) {
						LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", modulTagesschuleToAdd);
					}
				}
			}
			return transformedModule;
		}
		return null;
	}

	@Nullable
	private Set<ModulTagesschule> moduleTagesschuleListToEntity(
		@Nullable Set<JaxModulTagesschule> jaxModuleTagesschule,
		@Nullable Set<ModulTagesschule> moduleOfInstitution,
		@Nonnull EinstellungenTagesschule institutionStammdatenTagesschule) {

		if (moduleOfInstitution != null && jaxModuleTagesschule != null) {
			final Set<ModulTagesschule> transformedModule = new TreeSet<>();
			for (final JaxModulTagesschule jaxModulTagesschule : jaxModuleTagesschule) {
				final ModulTagesschule modulTagesschuleToMergeWith = moduleOfInstitution.stream()
					.filter(existingModul -> existingModul.getId().equalsIgnoreCase(jaxModulTagesschule.getId()))
					.reduce(StreamsUtil.toOnlyElement())
					.orElse(new ModulTagesschule());
				final ModulTagesschule modulTagesschuleToAdd =
					modulTagesschuleToEntity(jaxModulTagesschule, modulTagesschuleToMergeWith);
				if (modulTagesschuleToAdd != null) {
					final boolean added = transformedModule.add(modulTagesschuleToAdd);
					if (!added) {
						LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", modulTagesschuleToAdd);
					}
				}
			}
			return transformedModule;
		}
		return null;
	}

	@Nullable
	private ModulTagesschule modulTagesschuleToEntity(
		@Nullable JaxModulTagesschule jaxModulTagesschule,
		@Nonnull ModulTagesschule modulTagesschule) {

		if (jaxModulTagesschule == null) {
			return null;
		}
		convertAbstractFieldsToEntity(jaxModulTagesschule, modulTagesschule);
		modulTagesschule.setWochentag(jaxModulTagesschule.getWochentag());
		return modulTagesschule;
	}

	private LocalTime hoursAndMinutesToDate(@Nonnull String hoursAndMinutes) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("H:mm");
		LocalTime time = LocalTime.parse(hoursAndMinutes, dateTimeFormatter);
		return time;
	}

	private String dateToHoursAndMinutes(@Nonnull LocalTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("H:mm");
		String format = date.format(dateTimeFormatter);
		return format;
	}

	@Nonnull
	public FinanzielleSituationContainer finanzielleSituationContainerToStorableEntity(
		@Nonnull final JaxFinanzielleSituationContainer containerJAX,
		@Nullable FinanzielleSituationContainer container) {

		requireNonNull(containerJAX);

		FinanzielleSituationContainer containerToMergeWith = container != null ?
			container :
			new FinanzielleSituationContainer();

		if (containerJAX.getId() != null) {
			final Optional<FinanzielleSituationContainer> existingFSC =
				finanzielleSituationService.findFinanzielleSituation(containerJAX.getId());
			if (existingFSC.isPresent()) {
				containerToMergeWith = existingFSC.get();
			}
		}
		final FinanzielleSituationContainer mergedContainer =
			finanzielleSituationContainerToEntity(containerJAX, containerToMergeWith);
		return mergedContainer;
	}

	public EinkommensverschlechterungContainer einkommensverschlechterungContainerToStorableEntity(
		@Nonnull final JaxEinkommensverschlechterungContainer containerJAX) {

		requireNonNull(containerJAX);

		EinkommensverschlechterungContainer containerToMergeWith = new EinkommensverschlechterungContainer();
		if (containerJAX.getId() != null) {
			final Optional<EinkommensverschlechterungContainer> existingEkvC =
				einkommensverschlechterungService.findEinkommensverschlechterungContainer(containerJAX.getId());
			if (existingEkvC.isPresent()) {
				containerToMergeWith = existingEkvC.get();
			}
		}

		return einkommensverschlechterungContainerToEntity(containerJAX, containerToMergeWith);
	}

	@Nonnull
	public JaxKind kindToJAX(@Nonnull final Kind persistedKind) {
		final JaxKind jaxKind = new JaxKind();
		convertAbstractPersonFieldsToJAX(persistedKind, jaxKind);
		jaxKind.setKinderabzugErstesHalbjahr(persistedKind.getKinderabzugErstesHalbjahr());
		jaxKind.setKinderabzugZweitesHalbjahr(persistedKind.getKinderabzugZweitesHalbjahr());
		jaxKind.setFamilienErgaenzendeBetreuung(persistedKind.getFamilienErgaenzendeBetreuung());
		jaxKind.setSprichtAmtssprache(persistedKind.getSprichtAmtssprache());
		jaxKind.setAusAsylwesen(persistedKind.getAusAsylwesen());
		jaxKind.setZemisNummer(persistedKind.getZemisNummer());
		jaxKind.setEinschulungTyp(persistedKind.getEinschulungTyp());
		jaxKind.setPensumFachstelle(pensumFachstelleToJax(persistedKind.getPensumFachstelle()));
		jaxKind.setPensumAusserordentlicherAnspruch(pensumAusserordentlicherAnspruchToJax(
			persistedKind.getPensumAusserordentlicherAnspruch()));
		return jaxKind;
	}

	@Nullable
	public JaxPensumFachstelle pensumFachstelleToJax(@Nullable final PensumFachstelle persistedPensumFachstelle) {
		if (persistedPensumFachstelle == null) {
			return null;
		}
		final JaxPensumFachstelle jaxPensumFachstelle = new JaxPensumFachstelle();
		convertAbstractPensumFieldsToJAX(persistedPensumFachstelle, jaxPensumFachstelle);
		jaxPensumFachstelle.setFachstelle(fachstelleToJAX(persistedPensumFachstelle.getFachstelle()));
		jaxPensumFachstelle.setIntegrationTyp(persistedPensumFachstelle.getIntegrationTyp());
		return jaxPensumFachstelle;
	}

	public PensumFachstelle pensumFachstelleToEntity(
		final JaxPensumFachstelle pensumFachstelleJAXP,
		final PensumFachstelle pensumFachstelle
	) {
		requireNonNull(pensumFachstelleJAXP.getFachstelle(), "Fachstelle muss existieren");
		requireNonNull(
			pensumFachstelleJAXP.getFachstelle().getId(),
			"Fachstelle muss bereits gespeichert sein");
		convertAbstractPensumFieldsToEntity(pensumFachstelleJAXP, pensumFachstelle);

		final Optional<Fachstelle> fachstelleFromDB =
			fachstelleService.findFachstelle(pensumFachstelleJAXP.getFachstelle().getId());
		if (fachstelleFromDB.isPresent()) {
			// Fachstelle darf nicht vom Client ueberschrieben werden
			pensumFachstelle.setFachstelle(fachstelleFromDB.get());
		} else {
			throw new EbeguEntityNotFoundException(
				"pensumFachstelleToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				pensumFachstelleJAXP.getFachstelle()
					.getId());
		}
		pensumFachstelle.setIntegrationTyp(pensumFachstelleJAXP.getIntegrationTyp());

		return pensumFachstelle;
	}

	public PensumFachstelle toStorablePensumFachstelle(@Nonnull final JaxPensumFachstelle pensumFsToSave) {
		PensumFachstelle pensumToMergeWith = new PensumFachstelle();
		if (pensumFsToSave.getId() != null) {
			final Optional<PensumFachstelle> pensumFachstelleOpt =
				pensumFachstelleService.findPensumFachstelle(pensumFsToSave.getId());
			if (pensumFachstelleOpt.isPresent()) {
				pensumToMergeWith = pensumFachstelleOpt.get();
			} else {
				throw new EbeguEntityNotFoundException(
					"toStorablePensumFachstelle",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					pensumFsToSave.getId());
			}
		}
		return pensumFachstelleToEntity(pensumFsToSave, pensumToMergeWith);
	}

	@Nullable
	public JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruchToJax(
		@Nullable final PensumAusserordentlicherAnspruch persistedPensumAusserordentlicherAnspruch) {

		if (persistedPensumAusserordentlicherAnspruch == null) {
			return null;
		}
		final JaxPensumAusserordentlicherAnspruch jaxPensumAusserordentlicherAnspruch =
			new JaxPensumAusserordentlicherAnspruch();
		convertAbstractPensumFieldsToJAX(
			persistedPensumAusserordentlicherAnspruch,
			jaxPensumAusserordentlicherAnspruch);
		jaxPensumAusserordentlicherAnspruch.setBegruendung(persistedPensumAusserordentlicherAnspruch.getBegruendung());
		return jaxPensumAusserordentlicherAnspruch;
	}

	public PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruchToEntity(
		@Nonnull final JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruchJAXP,
		@Nonnull final PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch) {

		convertAbstractPensumFieldsToEntity(pensumAusserordentlicherAnspruchJAXP, pensumAusserordentlicherAnspruch);
		pensumAusserordentlicherAnspruch.setBegruendung(pensumAusserordentlicherAnspruchJAXP.getBegruendung());
		return pensumAusserordentlicherAnspruch;
	}

	@Nonnull
	private PensumAusserordentlicherAnspruch toStorablePensumAusserordentlicherAnspruch(
		@Nonnull final JaxPensumAusserordentlicherAnspruch pensumFsToSave) {

		PensumAusserordentlicherAnspruch pensumToMergeWith = new PensumAusserordentlicherAnspruch();
		if (pensumFsToSave.getId() != null) {
			final Optional<PensumAusserordentlicherAnspruch> pensumAusserordentlicherAnspruchOpt =
				pensumAusserordentlicherAnspruchService.findPensumAusserordentlicherAnspruch(pensumFsToSave.getId());
			if (pensumAusserordentlicherAnspruchOpt.isPresent()) {
				pensumToMergeWith = pensumAusserordentlicherAnspruchOpt.get();
			} else {
				throw new EbeguEntityNotFoundException("toStorablePensumAusserordentlicherAnspruch",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, pensumFsToSave.getId());
			}
		}
		return pensumAusserordentlicherAnspruchToEntity(pensumFsToSave, pensumToMergeWith);
	}

	public JaxKindContainer kindContainerToJAX(final KindContainer persistedKind) {
		final JaxKindContainer jaxKindContainer = new JaxKindContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedKind, jaxKindContainer);
		if (persistedKind.getKindGS() != null) {
			jaxKindContainer.setKindGS(kindToJAX(persistedKind.getKindGS()));
		}
		if (persistedKind.getKindJA() != null) {
			jaxKindContainer.setKindJA(kindToJAX(persistedKind.getKindJA()));
		}
		jaxKindContainer.setBetreuungen(new TreeSet<>());
		Set<JaxBetreuung> betreuungen = betreuungListToJax(persistedKind.getBetreuungen());
		jaxKindContainer.getBetreuungen().addAll(betreuungen);
		Set<JaxBetreuung> anmeldungenTagesschule =
			anmeldungTagesschuleListToJax(persistedKind.getAnmeldungenTagesschule());
		jaxKindContainer.getBetreuungen().addAll(anmeldungenTagesschule);
		Set<JaxBetreuung> anmeldungenFerieninsel =
			anmeldungFerieninselListToJax(persistedKind.getAnmeldungenFerieninsel());
		jaxKindContainer.getBetreuungen().addAll(anmeldungenFerieninsel);
		jaxKindContainer.setKindNummer(persistedKind.getKindNummer());
		jaxKindContainer.setNextNumberBetreuung(persistedKind.getNextNumberBetreuung());
		return jaxKindContainer;
	}

	public Kind kindToEntity(final JaxKind kindJAXP, final Kind kind) {
		requireNonNull(kindJAXP);
		requireNonNull(kind);
		convertAbstractPersonFieldsToEntity(kindJAXP, kind);
		kind.setKinderabzugErstesHalbjahr(kindJAXP.getKinderabzugErstesHalbjahr());
		kind.setKinderabzugZweitesHalbjahr(kindJAXP.getKinderabzugZweitesHalbjahr());
		kind.setFamilienErgaenzendeBetreuung(kindJAXP.getFamilienErgaenzendeBetreuung());
		kind.setSprichtAmtssprache(kindJAXP.getSprichtAmtssprache());
		kind.setAusAsylwesen(kindJAXP.getAusAsylwesen());
		kind.setZemisNummer(kindJAXP.getZemisNummer());
		kind.setEinschulungTyp(kindJAXP.getEinschulungTyp());

		PensumFachstelle updtPensumFachstelle = null;
		if (kindJAXP.getPensumFachstelle() != null) {
			updtPensumFachstelle = toStorablePensumFachstelle(kindJAXP.getPensumFachstelle());
		}
		kind.setPensumFachstelle(updtPensumFachstelle);

		PensumAusserordentlicherAnspruch updtPensumAusserordentlicherAnspruch = null;
		if (kindJAXP.getPensumAusserordentlicherAnspruch() != null) {
			updtPensumAusserordentlicherAnspruch = toStorablePensumAusserordentlicherAnspruch(
				kindJAXP.getPensumAusserordentlicherAnspruch());
		}
		kind.setPensumAusserordentlicherAnspruch(updtPensumAusserordentlicherAnspruch);

		return kind;
	}

	public KindContainer kindContainerToEntity(
		@Nonnull final JaxKindContainer kindContainerJAXP,
		@Nonnull final KindContainer kindContainer) {
		requireNonNull(kindContainer);
		requireNonNull(kindContainerJAXP);
		convertAbstractVorgaengerFieldsToEntity(kindContainerJAXP, kindContainer);
		//kind daten koennen nicht verschwinden
		if (kindContainerJAXP.getKindGS() != null) {
			Kind kindGS = new Kind();
			if (kindContainer.getKindGS() != null) {
				kindGS = kindContainer.getKindGS();
			}
			kindContainer.setKindGS(kindToEntity(kindContainerJAXP.getKindGS(), kindGS));
		}
		if (kindContainerJAXP.getKindJA() != null) {
			Kind kindJA = new Kind();
			if (kindContainer.getKindJA() != null) {
				kindJA = kindContainer.getKindJA();
			}
			kindContainer.setKindJA(kindToEntity(kindContainerJAXP.getKindJA(), kindJA));
		}
		// nextNumberBetreuung wird nur im Server gesetzt, darf aus dem Client nicht uebernommen werden
		return kindContainer;
	}

	/**
	 * Sucht das Gesuch in der DB und fuegt es mit dem als Parameter gegebenen Gesuch zusammen.
	 * Sollte es in der DB nicht existieren, gibt die Methode ein neues Gesuch mit den gegebenen Daten zurueck
	 *
	 * @param gesuchToFind das Gesuch als JAX
	 * @return das Gesuch als Entity
	 */
	@Nonnull
	public Gesuch gesuchToStoreableEntity(final JaxGesuch gesuchToFind) {
		requireNonNull(gesuchToFind);
		Gesuch gesuchToMergeWith = new Gesuch();
		if (gesuchToFind.getId() != null) {
			final Optional<Gesuch> altGesuch = gesuchService.findGesuch(gesuchToFind.getId());
			if (altGesuch.isPresent()) {
				gesuchToMergeWith = altGesuch.get();
			}
		}
		return gesuchToEntity(gesuchToFind, gesuchToMergeWith);
	}

	@Nonnull
	public FinanzielleSituationContainer finanzielleSituationContainerToEntity(
		@Nonnull final JaxFinanzielleSituationContainer containerJAX,
		@Nonnull final FinanzielleSituationContainer container) {
		requireNonNull(container);
		requireNonNull(containerJAX);
		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);
		container.setJahr(containerJAX.getJahr());
		FinanzielleSituation finSitToMergeWith;
		//Im moment kann eine einmal gespeicherte Finanzielle Situation nicht mehr entfernt werden.
		if (containerJAX.getFinanzielleSituationGS() != null) {
			finSitToMergeWith =
				Optional.ofNullable(container.getFinanzielleSituationGS()).orElse(new FinanzielleSituation());
			container.setFinanzielleSituationGS(finanzielleSituationToEntity(
				containerJAX.getFinanzielleSituationGS(),
				finSitToMergeWith));
		}
		if (containerJAX.getFinanzielleSituationJA() != null) {
			finSitToMergeWith =
				Optional.ofNullable(container.getFinanzielleSituationJA()).orElse(new FinanzielleSituation());
			container.setFinanzielleSituationJA(finanzielleSituationToEntity(
				containerJAX.getFinanzielleSituationJA(),
				finSitToMergeWith));
		}
		return container;
	}

	@Nonnull
	public JaxFinanzielleSituationContainer finanzielleSituationContainerToJAX(
		@Nonnull final FinanzielleSituationContainer
			persistedFinanzielleSituation) {
		final JaxFinanzielleSituationContainer jaxPerson = new JaxFinanzielleSituationContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedFinanzielleSituation, jaxPerson);
		jaxPerson.setJahr(persistedFinanzielleSituation.getJahr());
		jaxPerson.setFinanzielleSituationGS(finanzielleSituationToJAX(persistedFinanzielleSituation.getFinanzielleSituationGS()));
		jaxPerson.setFinanzielleSituationJA(finanzielleSituationToJAX(persistedFinanzielleSituation.getFinanzielleSituationJA()));
		return jaxPerson;
	}

	public EinkommensverschlechterungContainer einkommensverschlechterungContainerToEntity(
		@Nonnull final JaxEinkommensverschlechterungContainer containerJAX,
		@Nonnull final EinkommensverschlechterungContainer container) {
		requireNonNull(container);
		requireNonNull(containerJAX);
		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);

		Einkommensverschlechterung einkommensverschlechterung;

		if (containerJAX.getEkvGSBasisJahrPlus1() != null) {
			einkommensverschlechterung = Optional.ofNullable(container.getEkvGSBasisJahrPlus1())
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvGSBasisJahrPlus1(einkommensverschlechterungToEntity(
				containerJAX.getEkvGSBasisJahrPlus1(),
				einkommensverschlechterung));
		}
		if (containerJAX.getEkvGSBasisJahrPlus2() != null) {
			einkommensverschlechterung = Optional.ofNullable(container.getEkvGSBasisJahrPlus2())
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvGSBasisJahrPlus2(einkommensverschlechterungToEntity(
				containerJAX.getEkvGSBasisJahrPlus2(),
				einkommensverschlechterung));
		}
		if (containerJAX.getEkvJABasisJahrPlus1() != null) {
			einkommensverschlechterung = Optional.ofNullable(container.getEkvJABasisJahrPlus1())
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvJABasisJahrPlus1(einkommensverschlechterungToEntity(
				containerJAX.getEkvJABasisJahrPlus1(),
				einkommensverschlechterung));
		}
		if (containerJAX.getEkvJABasisJahrPlus2() != null) {
			einkommensverschlechterung = Optional.ofNullable(container.getEkvJABasisJahrPlus2())
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvJABasisJahrPlus2(einkommensverschlechterungToEntity(
				containerJAX.getEkvJABasisJahrPlus2(),
				einkommensverschlechterung));
		}

		return container;
	}

	@Nullable
	public JaxEinkommensverschlechterungContainer einkommensverschlechterungContainerToJAX(
		@Nullable final EinkommensverschlechterungContainer persistedEkv) {

		if (persistedEkv == null) {
			return null;
		}

		final JaxEinkommensverschlechterungContainer evsc = new JaxEinkommensverschlechterungContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedEkv, evsc);
		evsc.setEkvGSBasisJahrPlus1(einkommensverschlechterungToJAX(persistedEkv.getEkvGSBasisJahrPlus1()));
		evsc.setEkvGSBasisJahrPlus2(einkommensverschlechterungToJAX(persistedEkv.getEkvGSBasisJahrPlus2()));
		evsc.setEkvJABasisJahrPlus1(einkommensverschlechterungToJAX(persistedEkv.getEkvJABasisJahrPlus1()));
		evsc.setEkvJABasisJahrPlus2(einkommensverschlechterungToJAX(persistedEkv.getEkvJABasisJahrPlus2()));

		return evsc;
	}

	private AbstractFinanzielleSituation abstractFinanzielleSituationToEntity(
		@Nonnull final JaxAbstractFinanzielleSituation abstractFinanzielleSituationJAXP,
		@Nonnull final AbstractFinanzielleSituation abstractFinanzielleSituation) {

		requireNonNull(abstractFinanzielleSituation);
		requireNonNull(abstractFinanzielleSituationJAXP);

		convertAbstractVorgaengerFieldsToEntity(abstractFinanzielleSituationJAXP, abstractFinanzielleSituation);
		abstractFinanzielleSituation.setNettolohn(abstractFinanzielleSituationJAXP.getNettolohn());
		abstractFinanzielleSituation.setFamilienzulage(abstractFinanzielleSituationJAXP.getFamilienzulage());
		abstractFinanzielleSituation.setErsatzeinkommen(abstractFinanzielleSituationJAXP.getErsatzeinkommen());
		abstractFinanzielleSituation.setErhalteneAlimente(abstractFinanzielleSituationJAXP.getErhalteneAlimente());
		abstractFinanzielleSituation.setBruttovermoegen(abstractFinanzielleSituationJAXP.getBruttovermoegen());
		abstractFinanzielleSituation.setSchulden(abstractFinanzielleSituationJAXP.getSchulden());
		abstractFinanzielleSituation.setGeschaeftsgewinnBasisjahr(abstractFinanzielleSituationJAXP.getGeschaeftsgewinnBasisjahr());
		abstractFinanzielleSituation.setGeleisteteAlimente(abstractFinanzielleSituationJAXP.getGeleisteteAlimente());

		return abstractFinanzielleSituation;
	}

	private void abstractFinanzielleSituationToJAX(
		@Nullable final AbstractFinanzielleSituation persistedAbstractFinanzielleSituation,
		JaxAbstractFinanzielleSituation jaxAbstractFinanzielleSituation) {

		if (persistedAbstractFinanzielleSituation == null) {
			return;
		}

		convertAbstractVorgaengerFieldsToJAX(persistedAbstractFinanzielleSituation, jaxAbstractFinanzielleSituation);
		jaxAbstractFinanzielleSituation.setNettolohn(persistedAbstractFinanzielleSituation.getNettolohn());
		jaxAbstractFinanzielleSituation.setFamilienzulage(persistedAbstractFinanzielleSituation.getFamilienzulage());
		jaxAbstractFinanzielleSituation.setErsatzeinkommen(persistedAbstractFinanzielleSituation.getErsatzeinkommen());
		jaxAbstractFinanzielleSituation.setErhalteneAlimente(persistedAbstractFinanzielleSituation.getErhalteneAlimente());
		jaxAbstractFinanzielleSituation.setBruttovermoegen(persistedAbstractFinanzielleSituation.getBruttovermoegen());
		jaxAbstractFinanzielleSituation.setSchulden(persistedAbstractFinanzielleSituation.getSchulden());
		jaxAbstractFinanzielleSituation.setGeschaeftsgewinnBasisjahr(persistedAbstractFinanzielleSituation.getGeschaeftsgewinnBasisjahr());
		jaxAbstractFinanzielleSituation.setGeleisteteAlimente(persistedAbstractFinanzielleSituation.getGeleisteteAlimente());
	}

	private FinanzielleSituation finanzielleSituationToEntity(
		@Nonnull final JaxFinanzielleSituation finanzielleSituationJAXP,
		@Nonnull final FinanzielleSituation finanzielleSituation) {

		requireNonNull(finanzielleSituation);
		requireNonNull(finanzielleSituationJAXP);

		abstractFinanzielleSituationToEntity(finanzielleSituationJAXP, finanzielleSituation);
		finanzielleSituation.setSteuerveranlagungErhalten(finanzielleSituationJAXP.getSteuerveranlagungErhalten());
		finanzielleSituation.setSteuererklaerungAusgefuellt(finanzielleSituationJAXP.getSteuererklaerungAusgefuellt());
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(finanzielleSituationJAXP.getGeschaeftsgewinnBasisjahrMinus2());
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(finanzielleSituationJAXP.getGeschaeftsgewinnBasisjahrMinus1());

		return finanzielleSituation;
	}

	@Nullable
	private JaxFinanzielleSituation finanzielleSituationToJAX(
		@Nullable final FinanzielleSituation persistedFinanzielleSituation) {

		if (persistedFinanzielleSituation == null) {
			return null;
		}

		JaxFinanzielleSituation jaxFinanzielleSituation = new JaxFinanzielleSituation();
		abstractFinanzielleSituationToJAX(persistedFinanzielleSituation, jaxFinanzielleSituation);
		jaxFinanzielleSituation.setSteuerveranlagungErhalten(persistedFinanzielleSituation.getSteuerveranlagungErhalten());
		jaxFinanzielleSituation.setSteuererklaerungAusgefuellt(persistedFinanzielleSituation.getSteuererklaerungAusgefuellt());
		jaxFinanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(persistedFinanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2());
		jaxFinanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(persistedFinanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1());

		return jaxFinanzielleSituation;
	}

	private Einkommensverschlechterung einkommensverschlechterungToEntity(
		@Nonnull final JaxEinkommensverschlechterung einkommensverschlechterungJAXP,
		@Nonnull final Einkommensverschlechterung einkommensverschlechterung) {

		requireNonNull(einkommensverschlechterung);
		requireNonNull(einkommensverschlechterungJAXP);

		abstractFinanzielleSituationToEntity(einkommensverschlechterungJAXP, einkommensverschlechterung);
		einkommensverschlechterung.setGeschaeftsgewinnBasisjahrMinus1(einkommensverschlechterungJAXP.getGeschaeftsgewinnBasisjahrMinus1());

		return einkommensverschlechterung;
	}

	@Nullable
	private JaxEinkommensverschlechterung einkommensverschlechterungToJAX(
		@Nullable final Einkommensverschlechterung persistedEinkommensverschlechterung) {

		if (persistedEinkommensverschlechterung == null) {
			return null;
		}

		JaxEinkommensverschlechterung eikvs = new JaxEinkommensverschlechterung();

		abstractFinanzielleSituationToJAX(persistedEinkommensverschlechterung, eikvs);
		eikvs.setGeschaeftsgewinnBasisjahrMinus1(persistedEinkommensverschlechterung.getGeschaeftsgewinnBasisjahrMinus1());

		return eikvs;
	}

	public ErwerbspensumContainer erwerbspensumContainerToStoreableEntity(
		@Nonnull final JaxErwerbspensumContainer jaxEwpCont) {

		requireNonNull(jaxEwpCont);

		ErwerbspensumContainer containerToMergeWith = Optional.ofNullable(jaxEwpCont.getId())
			.flatMap(erwerbspensumService::findErwerbspensum)
			.orElseGet(ErwerbspensumContainer::new);

		return erwerbspensumContainerToEntity(jaxEwpCont, containerToMergeWith);
	}

	public ErwerbspensumContainer erwerbspensumContainerToEntity(
		@Nonnull final JaxErwerbspensumContainer jaxEwpCont,
		@Nonnull final ErwerbspensumContainer erwerbspensumCont) {

		requireNonNull(jaxEwpCont);
		requireNonNull(erwerbspensumCont);

		convertAbstractVorgaengerFieldsToEntity(jaxEwpCont, erwerbspensumCont);
		if (jaxEwpCont.getErwerbspensumGS() != null) {
			Erwerbspensum pensumToMergeWith = Optional.ofNullable(erwerbspensumCont.getErwerbspensumGS())
				.orElseGet(Erwerbspensum::new);
			Erwerbspensum erwerbspensumGS = erwerbspensumToEntity(jaxEwpCont.getErwerbspensumGS(), pensumToMergeWith);
			erwerbspensumCont.setErwerbspensumGS(erwerbspensumGS);
		}
		if (jaxEwpCont.getErwerbspensumJA() != null) {
			Erwerbspensum pensumToMergeWith = Optional.ofNullable(erwerbspensumCont.getErwerbspensumJA())
				.orElseGet(Erwerbspensum::new);
			Erwerbspensum erwerbspensumJA = erwerbspensumToEntity(jaxEwpCont.getErwerbspensumJA(), pensumToMergeWith);
			erwerbspensumCont.setErwerbspensumJA(erwerbspensumJA);
		}

		return erwerbspensumCont;
	}

	@Nonnull
	public JaxErwerbspensumContainer erwerbspensumContainerToJAX(
		@Nonnull final ErwerbspensumContainer storedErwerbspensumCont) {

		requireNonNull(storedErwerbspensumCont);

		final JaxErwerbspensumContainer jaxEwpCont = new JaxErwerbspensumContainer();
		convertAbstractVorgaengerFieldsToJAX(storedErwerbspensumCont, jaxEwpCont);
		jaxEwpCont.setErwerbspensumGS(erbwerbspensumToJax(storedErwerbspensumCont.getErwerbspensumGS()));
		jaxEwpCont.setErwerbspensumJA(erbwerbspensumToJax(storedErwerbspensumCont.getErwerbspensumJA()));

		return jaxEwpCont;
	}

	private Erwerbspensum erwerbspensumToEntity(
		@Nonnull final JaxErwerbspensum jaxErwerbspensum,
		@Nonnull final Erwerbspensum erwerbspensum) {

		requireNonNull(jaxErwerbspensum);
		requireNonNull(erwerbspensum);

		convertAbstractPensumFieldsToEntity(jaxErwerbspensum, erwerbspensum);
		erwerbspensum.setTaetigkeit(jaxErwerbspensum.getTaetigkeit());
		erwerbspensum.setBezeichnung(jaxErwerbspensum.getBezeichnung());

		if (jaxErwerbspensum.getUnbezahlterUrlaub() != null) {
			UnbezahlterUrlaub existingUrlaub = new UnbezahlterUrlaub();
			if (jaxErwerbspensum.getUnbezahlterUrlaub().getId() != null) {
				existingUrlaub = erwerbspensumService.
					findUnbezahlterUrlaub(jaxErwerbspensum.getUnbezahlterUrlaub().getId())
					.orElse(new UnbezahlterUrlaub());
			}
			erwerbspensum.setUnbezahlterUrlaub(unbezahlterUrlaubToEntity(
				jaxErwerbspensum.getUnbezahlterUrlaub(),
				existingUrlaub));
		} else {
			erwerbspensum.setUnbezahlterUrlaub(null);
		}

		return erwerbspensum;
	}

	@Nullable
	private JaxErwerbspensum erbwerbspensumToJax(@Nullable final Erwerbspensum pensum) {
		if (pensum == null) {
			return null;
		}
		JaxErwerbspensum jaxErwerbspensum = new JaxErwerbspensum();
		convertAbstractPensumFieldsToJAX(pensum, jaxErwerbspensum);
		jaxErwerbspensum.setTaetigkeit(pensum.getTaetigkeit());
		jaxErwerbspensum.setBezeichnung(pensum.getBezeichnung());
		jaxErwerbspensum.setUnbezahlterUrlaub(unbezahlterUrlaubToJax(pensum.getUnbezahlterUrlaub()));
		return jaxErwerbspensum;
	}

	private UnbezahlterUrlaub unbezahlterUrlaubToEntity(
		@Nonnull final JaxUnbezahlterUrlaub jaxUrlaub,
		@Nonnull final UnbezahlterUrlaub urlaub) {

		requireNonNull(jaxUrlaub);
		requireNonNull(urlaub);
		convertAbstractDateRangedFieldsToEntity(jaxUrlaub, urlaub);
		return urlaub;
	}

	@Nullable
	private JaxUnbezahlterUrlaub unbezahlterUrlaubToJax(@Nullable final UnbezahlterUrlaub urlaub) {
		if (urlaub == null) {
			return null;
		}
		JaxUnbezahlterUrlaub jaxUrlaub = new JaxUnbezahlterUrlaub();
		convertAbstractDateRangedFieldsToJAX(urlaub, jaxUrlaub);
		return jaxUrlaub;
	}

	@Nonnull
	private <T extends AbstractPlatz> T abstractPlatzToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final T betreuung) {
		requireNonNull(betreuung);
		requireNonNull(betreuungJAXP);

		convertAbstractVorgaengerFieldsToEntity(betreuungJAXP, betreuung);

		// InstitutionStammdaten muessen bereits existieren
		if (betreuungJAXP.getInstitutionStammdaten() != null) {
			final String instStammdatenID = betreuungJAXP.getInstitutionStammdaten().getId();
			requireNonNull(instStammdatenID, "Die Institutionsstammdaten muessen gesetzt sein");
			final Optional<InstitutionStammdaten> optInstStammdaten =
				institutionStammdatenService.findInstitutionStammdaten(instStammdatenID);
			final InstitutionStammdaten instStammdatenToMerge =
				optInstStammdaten.orElseThrow(() -> new EbeguEntityNotFoundException(
					"betreuungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					instStammdatenID));
			// InstitutionsStammdaten darf nicht vom Client ueberschrieben werden
			betreuung.setInstitutionStammdaten(instStammdatenToMerge);
		}
		betreuung.setBetreuungNummer(betreuungJAXP.getBetreuungNummer());

		// try to load the Kind with the ID given by BetreuungJax
		if (betreuungJAXP.getKindId() != null) {
			KindContainer kindContainer = kindService.findKind(betreuungJAXP.getKindId())
				.orElseThrow(() -> new EbeguEntityNotFoundException("betreuungToEntity", betreuungJAXP.getKindId()));
			betreuung.setKind(kindContainer);
		}
		//ACHTUNG: Verfuegung wird hier nicht synchronisiert aus sicherheitsgruenden
		return betreuung;
	}

	@Nonnull
	public AnmeldungTagesschule anmeldungTagesschuleToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final AnmeldungTagesschule anmeldungTagesschule) {
		AnmeldungTagesschule betreuung = abstractPlatzToEntity(betreuungJAXP, anmeldungTagesschule);
		betreuung.setBetreuungsstatus(betreuungJAXP.getBetreuungsstatus());
		betreuung.setAnmeldungMutationZustand(betreuungJAXP.getAnmeldungMutationZustand());
		betreuung.setKeineDetailinformationen(betreuungJAXP.isKeineDetailinformationen());
		// Die korrekten EinstellungenTagesschule ermitteln fuer diese Betreuung
		InstitutionStammdatenTagesschule institutionStammdatenTagesschule =
			betreuung.getInstitutionStammdaten().getInstitutionStammdatenTagesschule();
		if (!betreuung.isKeineDetailinformationen()) {
			Objects.requireNonNull(institutionStammdatenTagesschule);
			Objects.requireNonNull(betreuungJAXP.getGesuchsperiode());
			Objects.requireNonNull(betreuungJAXP.getGesuchsperiode().getId());
			EinstellungenTagesschule einstellungenTagesschule =
				getEinstellungenTagesschule(institutionStammdatenTagesschule,
					betreuungJAXP.getGesuchsperiode().getId());
			if (betreuungJAXP.getBelegungTagesschule() != null) {
				requireNonNull(
					einstellungenTagesschule,
					"EinstellungTagesschule muessen gesetzt sein");
				if (betreuung.getBelegungTagesschule() != null) {
					betreuung.setBelegungTagesschule(belegungTagesschuleToEntity(
						betreuungJAXP.getBelegungTagesschule(),
						betreuung.getBelegungTagesschule(),
						einstellungenTagesschule));
				} else {
					betreuung.setBelegungTagesschule(belegungTagesschuleToEntity(
						betreuungJAXP.getBelegungTagesschule(),
						new BelegungTagesschule(),
						einstellungenTagesschule));
				}
			} else {
				betreuung.setBelegungTagesschule(null);
			}
		} else {
			betreuung.setBelegungTagesschule(null);
		}
		return betreuung;
	}

	@Nullable
	private EinstellungenTagesschule getEinstellungenTagesschule(
		@Nonnull InstitutionStammdatenTagesschule stammdatenTagesschule,
		@Nonnull String gesuchsperiodeId
	) {
		for (EinstellungenTagesschule einstellungenTagesschule : stammdatenTagesschule.getEinstellungenTagesschule()) {
			if (gesuchsperiodeId.equals(einstellungenTagesschule.getGesuchsperiode().getId())) {
				return einstellungenTagesschule;
			}
		}
		return null;
	}

	@Nonnull
	public AnmeldungFerieninsel anmeldungFerieninselToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final AnmeldungFerieninsel anmeldungFerieninsel) {
		AnmeldungFerieninsel betreuung = abstractPlatzToEntity(betreuungJAXP, anmeldungFerieninsel);
		betreuung.setBetreuungsstatus(betreuungJAXP.getBetreuungsstatus());
		betreuung.setAnmeldungMutationZustand(betreuungJAXP.getAnmeldungMutationZustand());
		if (betreuung.getBelegungFerieninsel() != null) {
			betreuung.setBelegungFerieninsel(belegungFerieninselToEntity(
				betreuungJAXP.getBelegungFerieninsel(),
				betreuung.getBelegungFerieninsel()));
		} else {
			betreuung.setBelegungFerieninsel(belegungFerieninselToEntity(
				betreuungJAXP.getBelegungFerieninsel(),
				new BelegungFerieninsel()));
		}
		return betreuung;
	}

	@Nonnull
	public Betreuung betreuungToEntity(@Nonnull final JaxBetreuung betreuungJAXP, @Nonnull final Betreuung betreuung) {
		requireNonNull(betreuung);
		requireNonNull(betreuungJAXP);

		abstractPlatzToEntity(betreuungJAXP, betreuung);
		betreuung.setGrundAblehnung(betreuungJAXP.getGrundAblehnung());
		betreuung.setDatumAblehnung(betreuungJAXP.getDatumAblehnung());
		betreuung.setDatumBestaetigung(betreuungJAXP.getDatumBestaetigung());

		betreuungsPensumContainersToEntity(
			betreuungJAXP.getBetreuungspensumContainers(),
			betreuung.getBetreuungspensumContainers()
		);
		setBetreuungInbetreuungsPensumContainers(betreuung.getBetreuungspensumContainers(), betreuung);

		betreuung.setErweiterteBetreuungContainer(erweiterteBetreuungContainerToEntity(
			betreuungJAXP.getErweiterteBetreuungContainer(),
			betreuung.getErweiterteBetreuungContainer()
		));
		betreuung.getErweiterteBetreuungContainer().setBetreuung(betreuung);

		abwesenheitContainersToEntity(betreuungJAXP.getAbwesenheitContainers(), betreuung.getAbwesenheitContainers());
		setBetreuungInAbwesenheiten(betreuung.getAbwesenheitContainers(), betreuung);

		betreuung.setBetreuungsstatus(betreuungJAXP.getBetreuungsstatus());
		betreuung.setVertrag(betreuungJAXP.getVertrag());

		betreuung.setBetreuungMutiert(betreuungJAXP.getBetreuungMutiert());
		betreuung.setAbwesenheitMutiert(betreuungJAXP.getAbwesenheitMutiert());

		//ACHTUNG: Verfuegung wird hier nicht synchronisiert aus sicherheitsgruenden
		return betreuung;
	}

	public Set<BetreuungspensumAbweichung> betreuungspensumAbweichungenToEntity(
		final @Nonnull List<JaxBetreuungspensumAbweichung> abweichungenJAXP,
		final @Nonnull Set<BetreuungspensumAbweichung> abweichungen) {

		final Set<BetreuungspensumAbweichung> transformedAbweichungen = new TreeSet<>();
		for (final JaxBetreuungspensumAbweichung jaxAbweichung : abweichungenJAXP) {
			final BetreuungspensumAbweichung abweichungToMergeWith = abweichungen
				.stream()
				.filter(existingAbweichung -> existingAbweichung.getId().equals(jaxAbweichung.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new BetreuungspensumAbweichung());
			final BetreuungspensumAbweichung abweichungToAdd =
				betreuungspensumAbweichungToEntity(jaxAbweichung, abweichungToMergeWith);
			final boolean added = transformedAbweichungen.add(abweichungToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_ABWEICHUNG + "{}", abweichungToAdd);
			}
		}

		// change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		abweichungen.clear();
		abweichungen.addAll(transformedAbweichungen);

		return abweichungen;
	}

	private BetreuungspensumAbweichung betreuungspensumAbweichungToEntity(
		final @Nonnull JaxBetreuungspensumAbweichung jaxAbweichung,
		final @Nonnull BetreuungspensumAbweichung abweichung
	) {
		convertAbstractPensumFieldsToEntity(jaxAbweichung, abweichung);
		abweichung.setMonatlicheHauptmahlzeiten(jaxAbweichung.getMonatlicheHauptmahlzeiten());
		abweichung.setMonatlicheNebenmahlzeiten(jaxAbweichung.getMonatlicheNebenmahlzeiten());
		abweichung.setStatus(jaxAbweichung.getStatus());

		return abweichung;
	}

	private ErweiterteBetreuung erweiterteBetreuungToEntity(
		@Nonnull final JaxErweiterteBetreuung erweiterteBetreuungJAXP,
		@Nonnull final ErweiterteBetreuung erweiterteBetreuung) {

		requireNonNull(erweiterteBetreuung);
		requireNonNull(erweiterteBetreuungJAXP);

		convertAbstractVorgaengerFieldsToEntity(erweiterteBetreuungJAXP, erweiterteBetreuung);

		erweiterteBetreuung.setErweiterteBeduerfnisse(erweiterteBetreuungJAXP.getErweiterteBeduerfnisse());
		erweiterteBetreuung.setErweiterteBeduerfnisseBestaetigt(
			erweiterteBetreuungJAXP.isErweiterteBeduerfnisseBestaetigt());
		erweiterteBetreuung.setKeineKesbPlatzierung(erweiterteBetreuungJAXP.getKeineKesbPlatzierung());
		erweiterteBetreuung.setBetreuungInGemeinde(erweiterteBetreuungJAXP.getBetreuungInGemeinde());

		//falls Erweiterte Beduerfnisse true ist, muss eine Fachstelle gesetzt sein
		if (Boolean.TRUE.equals(erweiterteBetreuung.getErweiterteBeduerfnisse())) {
			requireNonNull(erweiterteBetreuungJAXP.getFachstelle(), "Fachstelle muss existieren");
			requireNonNull(
				erweiterteBetreuungJAXP.getFachstelle().getId(),
				"Fachstelle muss bereits gespeichert sein");

			final Optional<Fachstelle> fachstelleFromDB =
				fachstelleService.findFachstelle(erweiterteBetreuungJAXP.getFachstelle().getId());

			if (!fachstelleFromDB.isPresent()) {
				throw new EbeguEntityNotFoundException(
					"erweiterteBetreuungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					erweiterteBetreuungJAXP.getFachstelle().getId());
			}
			// Fachstelle darf nicht vom Client ueberschrieben werden
			erweiterteBetreuung.setFachstelle(fachstelleFromDB.get());
		}

		return erweiterteBetreuung;
	}

	private BelegungTagesschule belegungTagesschuleToEntity(
		@Nonnull JaxBelegungTagesschule belegungTagesschuleJAXP,
		@Nonnull BelegungTagesschule belegungTagesschule,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule) {

		convertAbstractVorgaengerFieldsToEntity(belegungTagesschuleJAXP, belegungTagesschule);

		final Set<BelegungTagesschuleModul> convertedBelegungTagesschuleModule =
			belegungTagesschuleModulListToEntity(belegungTagesschuleJAXP.getBelegungTagesschuleModule(),
				belegungTagesschule.getBelegungTagesschuleModule(), belegungTagesschule);
		belegungTagesschule.getBelegungTagesschuleModule().clear();
		belegungTagesschule.getBelegungTagesschuleModule().addAll(convertedBelegungTagesschuleModule);

		belegungTagesschule.setEintrittsdatum(belegungTagesschuleJAXP.getEintrittsdatum());
		belegungTagesschule.setAbholungTagesschule(belegungTagesschuleJAXP.getAbholungTagesschule());
		belegungTagesschule.setPlanKlasse(belegungTagesschuleJAXP.getPlanKlasse());
		belegungTagesschule.setAbweichungZweitesSemester(belegungTagesschuleJAXP.isAbweichungZweitesSemester());
		belegungTagesschule.setBemerkung(belegungTagesschuleJAXP.getBemerkung());
		return belegungTagesschule;
	}

	public BelegungTagesschuleModul belegungTagesschuleModulToEntity(
		@Nonnull JaxBelegungTagesschuleModul belegungTagesschuleModulJAXP,
		@Nonnull BelegungTagesschuleModul belegungTagesschuleModul,
		@Nonnull BelegungTagesschule parent
	) {
		belegungTagesschuleModul.setIntervall(belegungTagesschuleModulJAXP.getIntervall());
		belegungTagesschuleModul.setModulTagesschule(
			persistence.find(ModulTagesschule.class, belegungTagesschuleModulJAXP.getModulTagesschule().getId()));
		belegungTagesschuleModul.setBelegungTagesschule(parent);
		return belegungTagesschuleModul;
	}

	@Nonnull
	private Set<BelegungTagesschuleModul> belegungTagesschuleModulListToEntity(
		@Nonnull Set<JaxBelegungTagesschuleModul> jaxBelegungTagesschuleModulList,
		@Nonnull Set<BelegungTagesschuleModul> belegungTagesschuleModulList,
		@Nonnull BelegungTagesschule parent) {

		final Set<BelegungTagesschuleModul> convertedBelegungTagesschuleModule = new TreeSet<>();
		for (final JaxBelegungTagesschuleModul jaxBelegungTagesschuleModul : jaxBelegungTagesschuleModulList) {
			final BelegungTagesschuleModul belegungModulToMergeWith = belegungTagesschuleModulList
				.stream()
				.filter(existingBelegungModul -> existingBelegungModul.getId().equals(jaxBelegungTagesschuleModul.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(BelegungTagesschuleModul::new);
			final BelegungTagesschuleModul belegungModulToAdd =
				belegungTagesschuleModulToEntity(jaxBelegungTagesschuleModul, belegungModulToMergeWith, parent);
			final boolean added = convertedBelegungTagesschuleModule.add(belegungModulToAdd);
			if (!added) {
				LOGGER.warn("dropped duplicate BelegungTagesschuleModul {}", belegungModulToAdd);
			}
		}
		return convertedBelegungTagesschuleModule;
	}

	@Nonnull
	public AnmeldungTagesschule anmeldungTagesschuleToStoreableEntity(@Nonnull final JaxBetreuung betreuungJAXP) {
		requireNonNull(betreuungJAXP);

		AnmeldungTagesschule betreuungToMergeWith = Optional.ofNullable(betreuungJAXP.getId())
			.flatMap(id -> betreuungService.findAnmeldungTagesschule(id))
			.orElseGet(AnmeldungTagesschule::new);
		return this.anmeldungTagesschuleToEntity(betreuungJAXP, betreuungToMergeWith);
	}

	@Nonnull
	public AnmeldungFerieninsel anmeldungFerieninselToStoreableEntity(@Nonnull final JaxBetreuung betreuungJAXP) {
		requireNonNull(betreuungJAXP);

		AnmeldungFerieninsel betreuungToMergeWith = Optional.ofNullable(betreuungJAXP.getId())
			.flatMap(id -> betreuungService.findAnmeldungFerieninsel(id))
			.orElseGet(AnmeldungFerieninsel::new);

		return this.anmeldungFerieninselToEntity(betreuungJAXP, betreuungToMergeWith);
	}

	public Betreuung betreuungToStoreableEntity(@Nonnull final JaxBetreuung betreuungJAXP) {
		requireNonNull(betreuungJAXP);

		Betreuung betreuungToMergeWith = Optional.ofNullable(betreuungJAXP.getId())
			.flatMap(id -> betreuungService.findBetreuung(id))
			.orElseGet(Betreuung::new);

		return this.betreuungToEntity(betreuungJAXP, betreuungToMergeWith);
	}

	public <T extends AbstractPlatz> T platzToStoreableEntity(@Nonnull final JaxBetreuung betreuungJAXP) {
		if (betreuungJAXP.getInstitutionStammdaten().getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESSCHULE) {
			return (T) anmeldungTagesschuleToStoreableEntity(betreuungJAXP);
		} else if (betreuungJAXP.getInstitutionStammdaten().getBetreuungsangebotTyp() == BetreuungsangebotTyp.FERIENINSEL) {
			return (T) anmeldungFerieninselToStoreableEntity(betreuungJAXP);
		}
		return (T) betreuungToStoreableEntity(betreuungJAXP);
	}

	public void setBetreuungInbetreuungsAbweichungen(
		final Set<BetreuungspensumAbweichung> betreuungspensumAbweichungen,
		final Betreuung betreuung) {

		betreuungspensumAbweichungen.forEach(c -> c.setBetreuung(betreuung));
	}

	private void setBetreuungInbetreuungsPensumContainers(
		final Set<BetreuungspensumContainer> betreuungspensumContainers,
		final Betreuung betreuung) {

		betreuungspensumContainers.forEach(c -> c.setBetreuung(betreuung));
	}

	private void setBetreuungInAbwesenheiten(
		final Set<AbwesenheitContainer> abwesenheiten,
		final Betreuung betreuung) {

		abwesenheiten.forEach(abwesenheit -> abwesenheit.setBetreuung(betreuung));
	}

	/**
	 * Goes through the whole list of jaxBetPenContainers. For each (jax)Container that already exists as Entity it
	 * merges both and adds the resulting
	 * (jax) container to the list. If the container doesn't exist it creates a new one and adds it to the list. Thus
	 * all containers that existed as entity
	 * but not in the list of jax, won't be added to the list and are then removed (cascade and orphanremoval)
	 *
	 * @param jaxBetPenContainers      Betreuungspensen DTOs from Client
	 * @param existingBetreuungspensen List of currently stored BetreungspensumContainers
	 */
	private void betreuungsPensumContainersToEntity(
		final List<JaxBetreuungspensumContainer> jaxBetPenContainers,
		final Collection<BetreuungspensumContainer> existingBetreuungspensen) {

		final Set<BetreuungspensumContainer> transformedBetPenContainers = new TreeSet<>();
		for (final JaxBetreuungspensumContainer jaxBetPensContainer : jaxBetPenContainers) {
			final BetreuungspensumContainer containerToMergeWith = existingBetreuungspensen
				.stream()
				.filter(existingBetPenEntity -> existingBetPenEntity.getId().equals(jaxBetPensContainer.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new BetreuungspensumContainer());
			final BetreuungspensumContainer contToAdd =
				betreuungspensumContainerToEntity(jaxBetPensContainer, containerToMergeWith);
			final boolean added = transformedBetPenContainers.add(contToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", contToAdd);
			}
		}

		// change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		existingBetreuungspensen.clear();
		existingBetreuungspensen.addAll(transformedBetPenContainers);
	}

	private void abwesenheitContainersToEntity(
		final List<JaxAbwesenheitContainer> jaxAbwesenheitContainers,
		final Collection<AbwesenheitContainer> existingAbwesenheiten) {

		final Set<AbwesenheitContainer> transformedAbwesenheitContainers = new TreeSet<>();
		for (final JaxAbwesenheitContainer jaxAbwesenheitContainer : jaxAbwesenheitContainers) {
			final AbwesenheitContainer containerToMergeWith = existingAbwesenheiten
				.stream()
				.filter(existingAbwesenheitEntity -> existingAbwesenheitEntity.getId()
					.equals(jaxAbwesenheitContainer.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new AbwesenheitContainer());
			final String oldID = containerToMergeWith.getId();
			final AbwesenheitContainer contToAdd =
				abwesenheitContainerToEntity(jaxAbwesenheitContainer, containerToMergeWith);
			contToAdd.setId(oldID);
			final boolean added = transformedAbwesenheitContainers.add(contToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", contToAdd);
			}
		}

		// change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		existingAbwesenheiten.clear();
		existingAbwesenheiten.addAll(transformedAbwesenheitContainers);
	}

	private Abwesenheit abwesenheitToEntity(final JaxAbwesenheit jaxAbwesenheit, final Abwesenheit abwesenheit) {
		convertAbstractDateRangedFieldsToEntity(jaxAbwesenheit, abwesenheit);
		return abwesenheit;
	}

	private BetreuungspensumContainer betreuungspensumContainerToEntity(
		final JaxBetreuungspensumContainer jaxBetPenContainers,
		final BetreuungspensumContainer bpContainer) {

		requireNonNull(jaxBetPenContainers);
		requireNonNull(bpContainer);

		convertAbstractVorgaengerFieldsToEntity(jaxBetPenContainers, bpContainer);
		if (jaxBetPenContainers.getBetreuungspensumGS() != null) {
			Betreuungspensum betPensGS = new Betreuungspensum();
			if (bpContainer.getBetreuungspensumGS() != null) {
				betPensGS = bpContainer.getBetreuungspensumGS();
			}
			bpContainer.setBetreuungspensumGS(betreuungspensumToEntity(
				jaxBetPenContainers.getBetreuungspensumGS(),
				betPensGS));
		}
		if (jaxBetPenContainers.getBetreuungspensumJA() != null) {
			Betreuungspensum betPensJA = new Betreuungspensum();
			if (bpContainer.getBetreuungspensumJA() != null) {
				betPensJA = bpContainer.getBetreuungspensumJA();
			}
			bpContainer.setBetreuungspensumJA(betreuungspensumToEntity(
				jaxBetPenContainers.getBetreuungspensumJA(),
				betPensJA));
		}

		return bpContainer;
	}

	private AbwesenheitContainer abwesenheitContainerToEntity(
		final JaxAbwesenheitContainer jaxAbwesenheitContainers,
		final AbwesenheitContainer abwesenheitContainer) {

		requireNonNull(jaxAbwesenheitContainers);
		requireNonNull(abwesenheitContainer);

		convertAbstractVorgaengerFieldsToEntity(jaxAbwesenheitContainers, abwesenheitContainer);
		if (jaxAbwesenheitContainers.getAbwesenheitGS() != null) {
			Abwesenheit abwesenheitGS = new Abwesenheit();
			if (abwesenheitContainer.getAbwesenheitGS() != null) {
				abwesenheitGS = abwesenheitContainer.getAbwesenheitGS();
			}
			// Das Setzen von alten IDs ist noetigt im Fall dass Betreuungsangebot fuer eine existierende Abwesenheit
			// geaendert wird, da sonst doppelte Verknuepfungen gemacht werden
			final String oldID = abwesenheitGS.getId();
			final Abwesenheit convertedAbwesenheitGS =
				abwesenheitToEntity(jaxAbwesenheitContainers.getAbwesenheitGS(), abwesenheitGS);
			convertedAbwesenheitGS.setId(oldID);
			abwesenheitContainer.setAbwesenheitGS(convertedAbwesenheitGS);
		}
		if (jaxAbwesenheitContainers.getAbwesenheitJA() != null) {
			Abwesenheit abwesenheitJA = new Abwesenheit();
			if (abwesenheitContainer.getAbwesenheitJA() != null) {
				abwesenheitJA = abwesenheitContainer.getAbwesenheitJA();
			}
			//siehe Kommentar oben bei abwesenheitGS
			final String oldID = abwesenheitJA.getId();
			final Abwesenheit convertedAbwesenheitJA =
				abwesenheitToEntity(jaxAbwesenheitContainers.getAbwesenheitJA(), abwesenheitJA);
			convertedAbwesenheitJA.setId(oldID);
			abwesenheitContainer.setAbwesenheitJA(convertedAbwesenheitJA);
		}
		return abwesenheitContainer;
	}

	private Betreuungspensum betreuungspensumToEntity(
		final JaxBetreuungspensum jaxBetreuungspensum,
		final Betreuungspensum betreuungspensum) {

		convertAbstractPensumFieldsToEntity(jaxBetreuungspensum, betreuungspensum);
		betreuungspensum.setMonatlicheHauptmahlzeiten(jaxBetreuungspensum.getMonatlicheHauptmahlzeiten());
		betreuungspensum.setMonatlicheNebenmahlzeiten(jaxBetreuungspensum.getMonatlicheNebenmahlzeiten());
		betreuungspensum.setTarifProHauptmahlzeit(jaxBetreuungspensum.getTarifProHauptmahlzeit());
		betreuungspensum.setTarifProNebenmahlzeit(jaxBetreuungspensum.getTarifProNebenmahlzeit());
		betreuungspensum.setNichtEingetreten(jaxBetreuungspensum.getNichtEingetreten());
		betreuungspensum.setMonatlicheBetreuungskosten(jaxBetreuungspensum.getMonatlicheBetreuungskosten());

		return betreuungspensum;
	}

	@Nonnull
	private Set<JaxBetreuung> betreuungListToJax(@Nullable final Set<Betreuung> betreuungen) {
		if (betreuungen == null) {
			return Collections.emptySet();
		}

		return betreuungen.stream()
			.map(this::betreuungToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@Nonnull
	private Set<JaxBetreuung> anmeldungTagesschuleListToJax(@Nullable final Set<AnmeldungTagesschule> betreuungen) {
		if (betreuungen == null) {
			return Collections.emptySet();
		}

		return betreuungen.stream()
			.map(this::anmeldungTagesschuleToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@Nonnull
	private Set<JaxBetreuung> anmeldungFerieninselListToJax(@Nullable final Set<AnmeldungFerieninsel> betreuungen) {
		if (betreuungen == null) {
			return Collections.emptySet();
		}

		return betreuungen.stream()
			.map(this::anmeldungFerieninselToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private BetreuungsmitteilungPensum betreuungsmitteilungpensumToEntity(
		final JaxBetreuungsmitteilungPensum jaxBetreuungspensum,
		final BetreuungsmitteilungPensum betreuungspensum) {

		convertAbstractPensumFieldsToEntity(jaxBetreuungspensum, betreuungspensum);
		betreuungspensum.setMonatlicheHauptmahlzeiten(jaxBetreuungspensum.getMonatlicheHauptmahlzeiten());
		betreuungspensum.setMonatlicheNebenmahlzeiten(jaxBetreuungspensum.getMonatlicheNebenmahlzeiten());
		betreuungspensum.setTarifProHauptmahlzeit(jaxBetreuungspensum.getTarifProHauptmahlzeit());
		betreuungspensum.setTarifProNebenmahlzeit(jaxBetreuungspensum.getTarifProNebenmahlzeit());

		return betreuungspensum;
	}

	private JaxBetreuungsmitteilungPensum betreuungsmitteilungPensumToJax(
		final BetreuungsmitteilungPensum betreuungspensum) {

		final JaxBetreuungsmitteilungPensum jaxBetreuungspensum = new JaxBetreuungsmitteilungPensum();

		convertAbstractPensumFieldsToJAX(betreuungspensum, jaxBetreuungspensum);
		jaxBetreuungspensum.setMonatlicheHauptmahlzeiten(betreuungspensum.getMonatlicheHauptmahlzeiten());
		jaxBetreuungspensum.setMonatlicheNebenmahlzeiten(betreuungspensum.getMonatlicheNebenmahlzeiten());
		jaxBetreuungspensum.setTarifProHauptmahlzeit(betreuungspensum.getTarifProHauptmahlzeit());
		jaxBetreuungspensum.setTarifProNebenmahlzeit(betreuungspensum.getTarifProNebenmahlzeit());

		return jaxBetreuungspensum;
	}

	/**
	 * converts the given betreuungList into a JaxBetreuungList
	 *
	 * @return List with Betreuung DTOs
	 */
	public Collection<JaxBetreuung> betreuungListToJax(Collection<Betreuung> betreuungList) {
		return betreuungList.stream()
			.map(this::betreuungToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	private JaxBetreuung abstractPlatzToJAX(@Nonnull final AbstractPlatz betreuungFromServer) {
		final JaxBetreuung jaxBetreuung = new JaxBetreuung();
		convertAbstractVorgaengerFieldsToJAX(betreuungFromServer, jaxBetreuung);
		jaxBetreuung.setInstitutionStammdaten(institutionStammdatenSummaryToJAX(
			betreuungFromServer.getInstitutionStammdaten(), new JaxInstitutionStammdatenSummary()));
		jaxBetreuung.setBetreuungNummer(betreuungFromServer.getBetreuungNummer());
		jaxBetreuung.setKindFullname(betreuungFromServer.getKind().getKindJA().getFullName());
		jaxBetreuung.setKindNummer(betreuungFromServer.getKind().getKindNummer());
		jaxBetreuung.setKindId(betreuungFromServer.getKind().getId());
		if (betreuungFromServer.getKind().getGesuch() != null) {
			jaxBetreuung.setGesuchId(betreuungFromServer.getKind().getGesuch().getId());
			jaxBetreuung.setGesuchsperiode(gesuchsperiodeToJAX(betreuungFromServer.getKind()
				.getGesuch()
				.getGesuchsperiode()));
		}
		jaxBetreuung.setGueltig(betreuungFromServer.isGueltig());
		jaxBetreuung.setBgNummer(betreuungFromServer.getBGNummer());
		return jaxBetreuung;
	}

	@Nonnull
	public JaxBetreuung anmeldungTagesschuleToJAX(@Nonnull final AnmeldungTagesschule betreuungFromServer) {
		JaxBetreuung jaxBetreuung = abstractPlatzToJAX(betreuungFromServer);
		jaxBetreuung.setBetreuungsstatus(betreuungFromServer.getBetreuungsstatus());
		jaxBetreuung.setAnmeldungMutationZustand(betreuungFromServer.getAnmeldungMutationZustand());
		jaxBetreuung.setKeineDetailinformationen(betreuungFromServer.isKeineDetailinformationen());
		jaxBetreuung.setBelegungTagesschule(belegungTagesschuleToJax(betreuungFromServer.getBelegungTagesschule()));
		// Für die Anzeige auf dem GUI interessiert es uns nicht, ob es eine echte/gespeicherte Verfügung
		// oder eine Preview-Verfügung ist
		if (betreuungFromServer.getVerfuegungOrVerfuegungPreview() != null) {
			jaxBetreuung.setVerfuegung(verfuegungToJax(betreuungFromServer.getVerfuegungOrVerfuegungPreview()));
		}
		setMandatoryFieldsOnJaxBetreuungForAnmeldungen(jaxBetreuung);
		return jaxBetreuung;
	}

	@Nonnull
	public JaxBetreuung anmeldungFerieninselToJAX(@Nonnull final AnmeldungFerieninsel betreuungFromServer) {
		JaxBetreuung jaxBetreuung = abstractPlatzToJAX(betreuungFromServer);
		jaxBetreuung.setBetreuungsstatus(betreuungFromServer.getBetreuungsstatus());
		jaxBetreuung.setAnmeldungMutationZustand(betreuungFromServer.getAnmeldungMutationZustand());
		jaxBetreuung.setBelegungFerieninsel(belegungFerieninselToJAX(betreuungFromServer.getBelegungFerieninsel()));
		setMandatoryFieldsOnJaxBetreuungForAnmeldungen(jaxBetreuung);
		return jaxBetreuung;
	}

	private void setMandatoryFieldsOnJaxBetreuungForAnmeldungen(@Nonnull JaxBetreuung jaxBetreuung) {
		// Wir verwenden Client-seitig dasselbe Objekt für Betreuungen und Anmeldungen
		// Auf JaxBetreuung sind einige Felder zwingend, die für Anmeldungen nicht benötigt werden,
		// diese müssen hier initialisiert werden
		jaxBetreuung.setVertrag(Boolean.TRUE);
		jaxBetreuung.setErweiterteBetreuungContainer(new JaxErweiterteBetreuungContainer());
	}

	@Nonnull
	public JaxBetreuung betreuungToJAX(@Nonnull final Betreuung betreuungFromServer) {
		JaxBetreuung jaxBetreuung = abstractPlatzToJAX(betreuungFromServer);
		jaxBetreuung.setGrundAblehnung(betreuungFromServer.getGrundAblehnung());
		jaxBetreuung.setDatumAblehnung(betreuungFromServer.getDatumAblehnung());
		jaxBetreuung.setDatumBestaetigung(betreuungFromServer.getDatumBestaetigung());
		jaxBetreuung.setBetreuungspensumContainers(betreuungsPensumContainersToJax(betreuungFromServer.getBetreuungspensumContainers()));
		jaxBetreuung.setErweiterteBetreuungContainer(erweiterteBetreuungContainerToJax(betreuungFromServer.getErweiterteBetreuungContainer()));
		jaxBetreuung.setAbwesenheitContainers(abwesenheitContainersToJax(betreuungFromServer.getAbwesenheitContainers()));
		jaxBetreuung.setBetreuungsstatus(betreuungFromServer.getBetreuungsstatus());
		jaxBetreuung.setVertrag(betreuungFromServer.getVertrag());
		// Für die Anzeige auf dem GUI interessiert es uns nicht, ob es eine echte/gespeicherte Verfügung
		// oder eine Preview-Verfügung ist
		if (betreuungFromServer.getVerfuegungOrVerfuegungPreview() != null) {
			jaxBetreuung.setVerfuegung(verfuegungToJax(betreuungFromServer.getVerfuegungOrVerfuegungPreview()));
		}

		jaxBetreuung.setBetreuungMutiert(betreuungFromServer.getBetreuungMutiert());
		jaxBetreuung.setAbwesenheitMutiert(betreuungFromServer.getAbwesenheitMutiert());
		return jaxBetreuung;
	}

	@Nonnull
	public <T extends AbstractPlatz> JaxBetreuung platzToJAX(@Nonnull final T platz) {
		if (platz.getBetreuungsangebotTyp().isTagesschule()) {
			return anmeldungTagesschuleToJAX((AnmeldungTagesschule) platz);
		} else if (platz.getBetreuungsangebotTyp().isFerieninsel()) {
			return anmeldungFerieninselToJAX((AnmeldungFerieninsel) platz);
		}
		return betreuungToJAX((Betreuung) platz);
	}

	@Nonnull
	public List<JaxBetreuungspensumAbweichung> betreuungspensumAbweichungenToJax(@Nonnull Betreuung betreuung) {
		return betreuung.fillAbweichungen().stream().map(this::betreuungspensumAbweichungToJax)
			.collect(Collectors.toList());
	}

	@Nonnull
	private JaxBetreuungspensumAbweichung betreuungspensumAbweichungToJax(
		@Nonnull BetreuungspensumAbweichung abweichung) {
		JaxBetreuungspensumAbweichung jaxAbweichung = new JaxBetreuungspensumAbweichung();
		convertAbstractPensumFieldsToJAX(abweichung, jaxAbweichung);
		jaxAbweichung.setVertraglicheKosten(abweichung.getVertraglicheKosten());
		jaxAbweichung.setVertraglichesPensum(abweichung.getVertraglichesPensum());
		jaxAbweichung.setVertraglicheHauptmahlzeiten(abweichung.getVertraglicheHauptmahlzeiten());
		jaxAbweichung.setVertraglicheNebenmahlzeiten(abweichung.getVertraglicheNebenmahlzeiten());
		jaxAbweichung.setStatus(abweichung.getStatus());
		jaxAbweichung.setMonatlicheHauptmahlzeiten(abweichung.getMonatlicheHauptmahlzeiten());
		jaxAbweichung.setMonatlicheNebenmahlzeiten(abweichung.getMonatlicheNebenmahlzeiten());
		jaxAbweichung.setTarifProHauptmahlzeit(abweichung.getTarifProHauptmahlzeit());
		jaxAbweichung.setTarifProNebenmahlzeit(abweichung.getTarifProNebenmahlzeit());
		jaxAbweichung.setVertraglicherTarifHaupt(abweichung.getVertraglicherTarifHauptmahlzeit());
		jaxAbweichung.setVertraglicherTarifNeben(abweichung.getVertraglicherTarifNebenmahlzeit());

		return jaxAbweichung;
	}

	@Nullable
	private JaxBelegungTagesschule belegungTagesschuleToJax(@Nullable BelegungTagesschule belegungFromServer) {
		if (belegungFromServer == null) {
			return null;
		}
		final JaxBelegungTagesschule jaxBelegungTagesschule = new JaxBelegungTagesschule();
		convertAbstractVorgaengerFieldsToJAX(belegungFromServer, jaxBelegungTagesschule);
		jaxBelegungTagesschule.setBelegungTagesschuleModule(belegungTagesschuleModuleListToJax(belegungFromServer.getBelegungTagesschuleModule()));
		jaxBelegungTagesschule.setEintrittsdatum(belegungFromServer.getEintrittsdatum());
		jaxBelegungTagesschule.setAbholungTagesschule(belegungFromServer.getAbholungTagesschule());
		jaxBelegungTagesschule.setPlanKlasse(belegungFromServer.getPlanKlasse());
		jaxBelegungTagesschule.setAbweichungZweitesSemester(belegungFromServer.isAbweichungZweitesSemester());
		jaxBelegungTagesschule.setBemerkung(belegungFromServer.getBemerkung());

		return jaxBelegungTagesschule;
	}

	private Set<JaxBelegungTagesschuleModul> belegungTagesschuleModuleListToJax(Set<BelegungTagesschuleModul> belegungTagesschuleModule) {
		if (belegungTagesschuleModule == null) {
			return Collections.emptySet();
		}
		return belegungTagesschuleModule.stream()
			.map(this::belegungTagesschuleModulToJax)
			.collect(Collectors.toSet());
	}

	@Nullable
	private JaxBelegungTagesschuleModul belegungTagesschuleModulToJax(@Nullable BelegungTagesschuleModul modulTagesschule) {
		if (modulTagesschule == null) {
			return null;
		}
		final JaxBelegungTagesschuleModul jaxBelegungTagesschuleModul = new JaxBelegungTagesschuleModul();
		convertAbstractFieldsToJAX(modulTagesschule, jaxBelegungTagesschuleModul);
		jaxBelegungTagesschuleModul.setIntervall(modulTagesschule.getIntervall());
		jaxBelegungTagesschuleModul.setModulTagesschule(Objects.requireNonNull(modulTagesschuleToJAX(modulTagesschule.getModulTagesschule())));
		return jaxBelegungTagesschuleModul;
	}

	@Nonnull
	private List<JaxModulTagesschuleGroup> modulTagesschuleGroupListToJax(@Nullable final Set<ModulTagesschuleGroup> module) {
		if (module == null) {
			return Collections.emptyList();
		}
		return module.stream()
			.map(this::modulTagesschuleGroupToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	private Set<JaxModulTagesschule> moduleTagesschuleListToJax(@Nullable final Set<ModulTagesschule> module) {
		if (module == null) {
			return Collections.emptySet();
		}

		return module.stream()
			.map(this::modulTagesschuleToJAX)
			.collect(Collectors.toSet());
	}

	@Nullable
	public JaxModulTagesschule modulTagesschuleToJAX(@Nullable ModulTagesschule modulTagesschule) {
		if (modulTagesschule == null) {
			return null;
		}

		final JaxModulTagesschule jaxModulTagesschule = new JaxModulTagesschule();
		convertAbstractFieldsToJAX(modulTagesschule, jaxModulTagesschule);
		jaxModulTagesschule.setWochentag(modulTagesschule.getWochentag());
		return jaxModulTagesschule;
	}

	@Nonnull
	public JaxModulTagesschuleGroup modulTagesschuleGroupToJAX(@Nonnull ModulTagesschuleGroup modulTagesschuleGroup) {
		final JaxModulTagesschuleGroup jaxModulTagesschuleGroup = new JaxModulTagesschuleGroup();
		convertAbstractFieldsToJAX(modulTagesschuleGroup, jaxModulTagesschuleGroup);
		jaxModulTagesschuleGroup.setModulTagesschuleName(modulTagesschuleGroup.getModulTagesschuleName());
		jaxModulTagesschuleGroup.setIdentifier(modulTagesschuleGroup.getIdentifier());
		jaxModulTagesschuleGroup.setBezeichnung(textRessourceToJAX(modulTagesschuleGroup.getBezeichnung()));
		jaxModulTagesschuleGroup.setZeitVon(dateToHoursAndMinutes(modulTagesschuleGroup.getZeitVon()));
		jaxModulTagesschuleGroup.setZeitBis(dateToHoursAndMinutes(modulTagesschuleGroup.getZeitBis()));
		jaxModulTagesschuleGroup.setVerpflegungskosten(modulTagesschuleGroup.getVerpflegungskosten());
		jaxModulTagesschuleGroup.setIntervall(modulTagesschuleGroup.getIntervall());
		jaxModulTagesschuleGroup.setWirdPaedagogischBetreut(modulTagesschuleGroup.isWirdPaedagogischBetreut());
		jaxModulTagesschuleGroup.setReihenfolge(modulTagesschuleGroup.getReihenfolge());
		jaxModulTagesschuleGroup.setModule(moduleTagesschuleListToJax(modulTagesschuleGroup.getModule()));
		return jaxModulTagesschuleGroup;
	}

	/**
	 * converts the given verfuegung into a JaxVerfuegung
	 *
	 * @return dto with the values of the verfuegung
	 */
	@Nullable
	public JaxVerfuegung verfuegungToJax(@Nullable Verfuegung verfuegung) {
		if (verfuegung == null) {
			return null;
		}

		final JaxVerfuegung jaxVerfuegung = new JaxVerfuegung();
		convertAbstractVorgaengerFieldsToJAX(verfuegung, jaxVerfuegung);
		jaxVerfuegung.setGeneratedBemerkungen(verfuegung.getGeneratedBemerkungen());
		jaxVerfuegung.setManuelleBemerkungen(verfuegung.getManuelleBemerkungen());
		jaxVerfuegung.setKategorieKeinPensum(verfuegung.isKategorieKeinPensum());
		jaxVerfuegung.setKategorieMaxEinkommen(verfuegung.isKategorieMaxEinkommen());
		jaxVerfuegung.setKategorieNichtEintreten(verfuegung.isKategorieNichtEintreten());
		jaxVerfuegung.setKategorieNormal(verfuegung.isKategorieNormal());

		List<JaxVerfuegungZeitabschnitt> zeitabschnitte = verfuegung.getZeitabschnitte().stream()
			.map(this::verfuegungZeitabschnittToJax)
			.collect(Collectors.toList());
		jaxVerfuegung.setZeitabschnitte(zeitabschnitte);

		return jaxVerfuegung;
	}

	@Nullable
	public Verfuegung verfuegungToEntity(@Nullable JaxVerfuegung jaxVerfuegung) {
		throw new EbeguFingerWegException("verfuegungToEntity", ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	@Nullable
	private JaxVerfuegungZeitabschnitt verfuegungZeitabschnittToJax(@Nullable VerfuegungZeitabschnitt zeitabschnitt) {
		if (zeitabschnitt == null) {
			return null;
		}

		// Achtung: Hier sollten nur Daten aus dem RelevantBGCalculation*Result* verwendet werden, da die Daten aus den
		// RelevantBgCalculation*Input* nicht gespeichert werden und somit bei verfuegten Angeboten nicht mehr
		// zugaenglich
		// sind. Ausnahme sind Daten, die ZUM VERFUEGEN gebraucht werden, wie z.B.
		// getRelevantBgCalculationInput().isSameVerfuegteVerfuegungsrelevanteDaten()

		final JaxVerfuegungZeitabschnitt jaxZeitabschn = new JaxVerfuegungZeitabschnitt();
		convertAbstractDateRangedFieldsToJAX(zeitabschnitt, jaxZeitabschn);
		jaxZeitabschn.setAbzugFamGroesse(zeitabschnitt.getAbzugFamGroesse());
		jaxZeitabschn.setErwerbspensumGS1(zeitabschnitt.getRelevantBgCalculationInput().getErwerbspensumGS1());
		jaxZeitabschn.setErwerbspensumGS2(zeitabschnitt.getRelevantBgCalculationInput().getErwerbspensumGS2());
		jaxZeitabschn.setBetreuungspensumProzent(zeitabschnitt.getBetreuungspensumProzent());
		jaxZeitabschn.setFachstellenpensum(zeitabschnitt.getRelevantBgCalculationInput().getFachstellenpensum());
		jaxZeitabschn.setAnspruchspensumRest(zeitabschnitt.getRelevantBgCalculationInput().getAnspruchspensumRest());
		jaxZeitabschn.setBgPensum(zeitabschnitt.getBgPensum());
		jaxZeitabschn.setAnspruchspensumProzent(zeitabschnitt.getAnspruchberechtigtesPensum());
		jaxZeitabschn.setBetreuungspensumZeiteinheit(zeitabschnitt.getBetreuungspensumZeiteinheit());
		jaxZeitabschn.setVollkosten(zeitabschnitt.getVollkosten());
		jaxZeitabschn.setVerguenstigungOhneBeruecksichtigungVollkosten(zeitabschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten());
		jaxZeitabschn.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(zeitabschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		jaxZeitabschn.setVerguenstigung(zeitabschnitt.getVerguenstigung());
		jaxZeitabschn.setMinimalerElternbeitrag(zeitabschnitt.getMinimalerElternbeitrag());
		jaxZeitabschn.setMinimalerElternbeitragGekuerzt(zeitabschnitt.getMinimalerElternbeitragGekuerzt());
		jaxZeitabschn.setElternbeitrag(zeitabschnitt.getElternbeitrag());
		jaxZeitabschn.setMassgebendesEinkommenVorAbzugFamgr(zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr());
		jaxZeitabschn.setBemerkungen(zeitabschnitt.getBemerkungen());
		jaxZeitabschn.setFamGroesse(zeitabschnitt.getFamGroesse());
		jaxZeitabschn.setEinkommensjahr(zeitabschnitt.getEinkommensjahr());
		jaxZeitabschn.setVerfuegteAnzahlZeiteinheiten(zeitabschnitt.getVerfuegteAnzahlZeiteinheiten());
		jaxZeitabschn.setAnspruchsberechtigteAnzahlZeiteinheiten(zeitabschnitt.getAnspruchsberechtigteAnzahlZeiteinheiten());
		jaxZeitabschn.setZeiteinheit(zeitabschnitt.getZeiteinheit());
		jaxZeitabschn.setKategorieKeinPensum(zeitabschnitt.getRelevantBgCalculationInput().isKategorieKeinPensum());
		jaxZeitabschn.setKategorieMaxEinkommen(zeitabschnitt.getRelevantBgCalculationInput().isKategorieMaxEinkommen());
		jaxZeitabschn.setZuSpaetEingereicht(zeitabschnitt.isZuSpaetEingereicht());
		jaxZeitabschn.setMinimalesEwpUnterschritten(zeitabschnitt.isMinimalesEwpUnterschritten());
		jaxZeitabschn.setZahlungsstatus(zeitabschnitt.getZahlungsstatus());
		jaxZeitabschn.setSameVerfuegteVerfuegungsrelevanteDaten(zeitabschnitt.getRelevantBgCalculationInput().isSameVerfuegteVerfuegungsrelevanteDaten());
		jaxZeitabschn.setSameAusbezahlteVerguenstigung(zeitabschnitt.getRelevantBgCalculationInput().isSameAusbezahlteVerguenstigung());
		jaxZeitabschn.setTsCalculationResultMitPaedagogischerBetreuung(
			tsCalculationResultToJax(zeitabschnitt.getTsCalculationResultMitPaedagogischerBetreuung()));
		jaxZeitabschn.setTsCalculationResultOhnePaedagogischerBetreuung(
			tsCalculationResultToJax(zeitabschnitt.getTsCalculationResultOhnePaedagogischerBetreuung()));
		jaxZeitabschn.setVerguenstigungHauptmahlzeitTotal(zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigungHauptmahlzeitenTotal());
		jaxZeitabschn.setVerguenstigungNebenmahlzeitTotal(zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigungNebenmahlzeitenTotal());
		return jaxZeitabschn;
	}

	public VerfuegungZeitabschnitt verfuegungZeitabschnittToEntity(@Nullable JaxVerfuegungZeitabschnitt jaxVerfuegungZeitabschnitt) {
		throw new EbeguFingerWegException("verfuegungZeitabschnittToEntity", ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	@Nullable
	private JaxTsCalculationResult tsCalculationResultToJax(@Nullable TSCalculationResult zeitabschnitt) {
		if (zeitabschnitt == null) {
			return null;
		}
		JaxTsCalculationResult result = new JaxTsCalculationResult();
		result.setBetreuungszeitProWoche(zeitabschnitt.getBetreuungszeitProWoche());
		result.setBetreuungszeitProWocheFormatted(zeitabschnitt.getBetreuungszeitProWocheFormatted());
		result.setVerpflegungskosten(zeitabschnitt.getVerpflegungskosten());
		result.setVerpflegungskostenVerguenstigt(zeitabschnitt.getVerpflegungskostenVerguenstigt());
		result.setGebuehrProStunde(zeitabschnitt.getGebuehrProStunde());
		result.setTotalKostenProWoche(zeitabschnitt.getTotalKostenProWoche());
		return result;
	}

	/**
	 * calls betreuungsPensumContainerToJax for each betreuungspensumContainer found in given the list
	 */
	@Nonnull
	private List<JaxBetreuungspensumContainer> betreuungsPensumContainersToJax(
		@Nullable Set<BetreuungspensumContainer> betreuungspensumContainers) {

		if (betreuungspensumContainers == null) {
			return Collections.emptyList();
		}

		return betreuungspensumContainers.stream()
			.map(this::betreuungsPensumContainerToJax)
			.collect(Collectors.toList());
	}

	@Nonnull
	private List<JaxAbwesenheitContainer> abwesenheitContainersToJax(
		@Nullable Set<AbwesenheitContainer> abwesenheiten) {

		if (abwesenheiten == null) {
			return Collections.emptyList();
		}

		return abwesenheiten.stream()
			.map(this::abwesenheitContainerToJax)
			.collect(Collectors.toList());
	}

	@Nullable
	private JaxAbwesenheit abwesenheitToJax(@Nullable Abwesenheit abwesenheit) {
		if (abwesenheit == null) {
			return null;
		}

		JaxAbwesenheit jaxAbwesenheit = new JaxAbwesenheit();
		convertAbstractDateRangedFieldsToJAX(abwesenheit, jaxAbwesenheit);

		return jaxAbwesenheit;
	}

	@Nullable
	private JaxAbwesenheitContainer abwesenheitContainerToJax(@Nullable AbwesenheitContainer abwesenheitContainer) {
		if (abwesenheitContainer == null) {
			return null;
		}

		JaxAbwesenheitContainer jaxAbwesenheitContainer = new JaxAbwesenheitContainer();
		convertAbstractVorgaengerFieldsToJAX(abwesenheitContainer, jaxAbwesenheitContainer);

		if (abwesenheitContainer.getAbwesenheitGS() != null) {
			jaxAbwesenheitContainer.setAbwesenheitGS(abwesenheitToJax(abwesenheitContainer.getAbwesenheitGS()));
		}

		if (abwesenheitContainer.getAbwesenheitJA() != null) {
			jaxAbwesenheitContainer.setAbwesenheitJA(abwesenheitToJax(abwesenheitContainer.getAbwesenheitJA()));
		}

		return jaxAbwesenheitContainer;
	}

	@Nullable
	private JaxBetreuungspensumContainer betreuungsPensumContainerToJax(
		@Nullable BetreuungspensumContainer betreuungspensumContainer) {

		if (betreuungspensumContainer == null) {
			return null;
		}

		JaxBetreuungspensumContainer jaxBetreuungspensumContainer = new JaxBetreuungspensumContainer();
		convertAbstractVorgaengerFieldsToJAX(betreuungspensumContainer, jaxBetreuungspensumContainer);

		if (betreuungspensumContainer.getBetreuungspensumGS() != null) {
			JaxBetreuungspensum jaxPensum = betreuungspensumToJax(betreuungspensumContainer.getBetreuungspensumGS());
			jaxBetreuungspensumContainer.setBetreuungspensumGS(jaxPensum);
		}

		if (betreuungspensumContainer.getBetreuungspensumJA() != null) {
			JaxBetreuungspensum jaxPensum = betreuungspensumToJax(betreuungspensumContainer.getBetreuungspensumJA());
			jaxBetreuungspensumContainer.setBetreuungspensumJA(jaxPensum);
		}

		return jaxBetreuungspensumContainer;
	}

	@Nonnull
	private JaxBetreuungspensum betreuungspensumToJax(@Nonnull Betreuungspensum betreuungspensum) {

		JaxBetreuungspensum jaxBetreuungspensum = new JaxBetreuungspensum();
		convertAbstractPensumFieldsToJAX(betreuungspensum, jaxBetreuungspensum);
		jaxBetreuungspensum.setNichtEingetreten(betreuungspensum.getNichtEingetreten());
		jaxBetreuungspensum.setMonatlicheHauptmahlzeiten(betreuungspensum.getMonatlicheHauptmahlzeiten());
		jaxBetreuungspensum.setMonatlicheNebenmahlzeiten(betreuungspensum.getMonatlicheNebenmahlzeiten());
		jaxBetreuungspensum.setTarifProHauptmahlzeit(betreuungspensum.getTarifProHauptmahlzeit());
		jaxBetreuungspensum.setTarifProNebenmahlzeit(betreuungspensum.getTarifProNebenmahlzeit());

		return jaxBetreuungspensum;
	}

	@Nonnull
	public ErweiterteBetreuungContainer erweiterteBetreuungContainerToEntity(
		@Nonnull final JaxErweiterteBetreuungContainer containerJAX,
		@Nullable ErweiterteBetreuungContainer container) {
		requireNonNull(containerJAX);

		container = container == null ? new ErweiterteBetreuungContainer() : container;

		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);

		if (containerJAX.getErweiterteBetreuungGS() != null) {
			ErweiterteBetreuung erwBetToMergeWith =
				Optional.ofNullable(container.getErweiterteBetreuungGS()).orElse(new ErweiterteBetreuung());
			container.setErweiterteBetreuungGS(erweiterteBetreuungToEntity(
				containerJAX.getErweiterteBetreuungGS(),
				erwBetToMergeWith));
		}
		if (containerJAX.getErweiterteBetreuungJA() != null) {
			ErweiterteBetreuung erwBetToMergeWith =
				Optional.ofNullable(container.getErweiterteBetreuungJA()).orElse(new ErweiterteBetreuung());
			container.setErweiterteBetreuungJA(erweiterteBetreuungToEntity(
				containerJAX.getErweiterteBetreuungJA(),
				erwBetToMergeWith));
		}
		return container;
	}

	@Nonnull
	private JaxErweiterteBetreuungContainer erweiterteBetreuungContainerToJax(
		@Nonnull ErweiterteBetreuungContainer erweiterteBetreuungContainer) {

		JaxErweiterteBetreuungContainer jaxErweiterteBetreuungContainer = new JaxErweiterteBetreuungContainer();
		convertAbstractVorgaengerFieldsToJAX(erweiterteBetreuungContainer, jaxErweiterteBetreuungContainer);

		if (erweiterteBetreuungContainer.getErweiterteBetreuungGS() != null) {
			JaxErweiterteBetreuung jaxErweiterteBetreuung =
				erweiterteBetreuungToJax(erweiterteBetreuungContainer.getErweiterteBetreuungGS());
			jaxErweiterteBetreuungContainer.setErweiterteBetreuungGS(jaxErweiterteBetreuung);
		}

		if (erweiterteBetreuungContainer.getErweiterteBetreuungJA() != null) {
			JaxErweiterteBetreuung jaxErweiterteBetreuung =
				erweiterteBetreuungToJax(erweiterteBetreuungContainer.getErweiterteBetreuungJA());
			jaxErweiterteBetreuungContainer.setErweiterteBetreuungJA(jaxErweiterteBetreuung);
		}

		return jaxErweiterteBetreuungContainer;
	}

	@Nonnull
	private JaxErweiterteBetreuung erweiterteBetreuungToJax(@Nonnull ErweiterteBetreuung erweiterteBetreuung) {

		requireNonNull(erweiterteBetreuung, "Erweiterte Betreuung muss gesetzt sein");

		JaxErweiterteBetreuung jaxErweiterteBetreuung = new JaxErweiterteBetreuung();
		convertAbstractVorgaengerFieldsToJAX(erweiterteBetreuung, jaxErweiterteBetreuung);
		jaxErweiterteBetreuung.setErweiterteBeduerfnisse(erweiterteBetreuung.getErweiterteBeduerfnisse());
		jaxErweiterteBetreuung.setErweiterteBeduerfnisseBestaetigt(
			erweiterteBetreuung.isErweiterteBeduerfnisseBestaetigt());
		jaxErweiterteBetreuung.setKeineKesbPlatzierung(erweiterteBetreuung.getKeineKesbPlatzierung());
		jaxErweiterteBetreuung.setBetreuungInGemeinde(erweiterteBetreuung.getBetreuungInGemeinde());

		if (erweiterteBetreuung.getFachstelle() != null) {
			jaxErweiterteBetreuung.setFachstelle(fachstelleToJAX(erweiterteBetreuung.getFachstelle()));
		}

		return jaxErweiterteBetreuung;
	}

	@Nonnull
	public JaxGesuchsperiode gesuchsperiodeToJAX(@Nonnull Gesuchsperiode persistedGesuchsperiode) {

		JaxGesuchsperiode jaxGesuchsperiode = new JaxGesuchsperiode();
		convertAbstractDateRangedFieldsToJAX(persistedGesuchsperiode, jaxGesuchsperiode);
		jaxGesuchsperiode.setStatus(persistedGesuchsperiode.getStatus());

		return jaxGesuchsperiode;
	}

	@Nonnull
	public Gesuchsperiode gesuchsperiodeToEntity(
		@Nonnull JaxGesuchsperiode jaxGesuchsperiode,
		@Nonnull Gesuchsperiode gesuchsperiode) {

		convertAbstractDateRangedFieldsToEntity(jaxGesuchsperiode, gesuchsperiode);
		gesuchsperiode.setStatus(jaxGesuchsperiode.getStatus());

		return gesuchsperiode;
	}

	@Nonnull
	public Benutzer jaxBenutzerToBenutzer(
		@Nonnull JaxBenutzer jaxBenutzer,
		@Nonnull Benutzer benutzer) {

		benutzer.setUsername(jaxBenutzer.getUsername());
		benutzer.setExternalUUID(
			Strings.isNullOrEmpty(jaxBenutzer.getExternalUUID()) ? null : jaxBenutzer.getExternalUUID()
		);
		benutzer.setEmail(jaxBenutzer.getEmail());
		benutzer.setNachname(jaxBenutzer.getNachname());
		benutzer.setVorname(jaxBenutzer.getVorname());
		benutzer.setStatus(jaxBenutzer.getStatus());
		if (jaxBenutzer.getMandant() != null && jaxBenutzer.getMandant().getId() != null) {
			// Mandant darf nicht vom Client ueberschrieben werden
			Mandant mandantFromDB = mandantService.findMandant(jaxBenutzer.getMandant().getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"jaxBenutzerToBenutzer -> mandant",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					benutzer.getMandant().getId()));
			benutzer.setMandant(mandantFromDB);
		} else {
			throw new EbeguEntityNotFoundException(
				"jaxBenutzerToBenutzer -> mandant",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}
		// Berechtigungen
		final Set<Berechtigung> convertedBerechtigungen = berechtigungenListToEntity(
			jaxBenutzer.getBerechtigungen(),
			benutzer.getBerechtigungen(),
			benutzer
		);
		//change the existing collection to reflect changes
		// Already tested: All existing module of the list remain as they were, that means their data are updated
		// and the objects are not created again. ID and InsertTimeStamp are the same as before
		benutzer.getBerechtigungen().clear();
		benutzer.getBerechtigungen().addAll(convertedBerechtigungen);
		return benutzer;
	}

	@Nonnull
	private Set<Berechtigung> berechtigungenListToEntity(
		@Nonnull Set<JaxBerechtigung> jaxBerechtigungenList,
		@Nonnull Set<Berechtigung> berechtigungenList,
		@Nonnull Benutzer benutzer) {

		final Set<Berechtigung> convertedBerechtigungen = new TreeSet<>();
		for (final JaxBerechtigung jaxBerechtigung : jaxBerechtigungenList) {
			final Berechtigung berechtigungToMergeWith = berechtigungenList
				.stream()
				.filter(existingBerechtigung -> existingBerechtigung.getId().equals(jaxBerechtigung.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(Berechtigung::new);
			final Berechtigung berechtigungToAdd = berechtigungToEntity(jaxBerechtigung, berechtigungToMergeWith);
			berechtigungToAdd.setBenutzer(benutzer);
			final boolean added = convertedBerechtigungen.add(berechtigungToAdd);
			if (!added) {
				LOGGER.warn("dropped duplicate berechtigung {}", berechtigungToAdd);
			}
		}
		return convertedBerechtigungen;
	}

	public JaxBenutzer benutzerToJaxBenutzer(@Nonnull Benutzer benutzer) {
		JaxBenutzer jaxLoginElement = new JaxBenutzer();
		jaxLoginElement.setVorname(benutzer.getVorname());
		jaxLoginElement.setNachname(benutzer.getNachname());
		jaxLoginElement.setEmail(benutzer.getEmail());
		if (benutzer.getMandant() != null) {
			jaxLoginElement.setMandant(mandantToJAX(benutzer.getMandant()));
		}
		jaxLoginElement.setUsername(benutzer.getUsername());
		jaxLoginElement.setExternalUUID(benutzer.getExternalUUID());
		jaxLoginElement.setStatus(benutzer.getStatus());
		jaxLoginElement.setCurrentBerechtigung(berechtigungToJax(benutzer.getCurrentBerechtigung()));
		// Berechtigungen
		Set<JaxBerechtigung> jaxBerechtigungen = new TreeSet<>();
		if (benutzer.getBerechtigungen() != null) {
			jaxBerechtigungen = benutzer.getBerechtigungen().stream()
				.map(this::berechtigungToJax)
				.sorted()
				.collect(Collectors.toCollection(TreeSet::new));
		}
		jaxLoginElement.setBerechtigungen(jaxBerechtigungen);

		return jaxLoginElement;
	}

	public JaxBenutzerNoDetails benutzerToJaxBenutzerNoDetails(@Nonnull Benutzer benutzer) {
		JaxBenutzerNoDetails jaxLoginElement = new JaxBenutzerNoDetails();
		jaxLoginElement.setVorname(benutzer.getVorname());
		jaxLoginElement.setNachname(benutzer.getNachname());
		jaxLoginElement.setUsername(benutzer.getUsername());
		Set<String> gemeindeIds = benutzer.getBerechtigungen()
			.stream()
			.flatMap(berechtigung -> berechtigung.getGemeindeList()
				.stream())
			.map(AbstractEntity::getId)
			.collect(Collectors.toSet());
		jaxLoginElement.setGemeindeIds(gemeindeIds);
		return jaxLoginElement;
	}

	public Berechtigung berechtigungToEntity(JaxBerechtigung jaxBerechtigung, Berechtigung berechtigung) {
		convertAbstractDateRangedFieldsToEntity(jaxBerechtigung, berechtigung);
		berechtigung.setRole(jaxBerechtigung.getRole());

		// wir muessen Traegerschaft und Institution auch updaten wenn sie null sind. Es koennte auch so aus dem IAM
		// kommen
		if (jaxBerechtigung.getInstitution() != null && jaxBerechtigung.getInstitution().getId() != null) {
			final Optional<Institution> institutionFromDB =
				institutionService.findInstitution(jaxBerechtigung.getInstitution().getId(), false);
			if (institutionFromDB.isPresent()) {
				// Institution darf nicht vom Client ueberschrieben werden
				berechtigung.setInstitution(institutionFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"berechtigungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxBerechtigung.getInstitution().getId());
			}
		} else {
			berechtigung.setInstitution(null);
		}

		if (jaxBerechtigung.getTraegerschaft() != null && jaxBerechtigung.getTraegerschaft().getId() != null) {
			final Optional<Traegerschaft> traegerschaftFromDB =
				traegerschaftService.findTraegerschaft(jaxBerechtigung.getTraegerschaft().getId());
			if (traegerschaftFromDB.isPresent()) {
				// Traegerschaft darf nicht vom Client ueberschrieben werden
				berechtigung.setTraegerschaft(traegerschaftFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"berechtigungToEntity -> traegerschaft",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxBerechtigung.getTraegerschaft().getId());
			}
		} else {
			berechtigung.setTraegerschaft(null);
		}

		// Gemeinden: Duerfen nicht vom Frontend übernommen werden, sondern müssen aus der DB gelesen werden!
		loadGemeindenFromJax(jaxBerechtigung, berechtigung);
		return berechtigung;
	}

	private void loadGemeindenFromJax(@Nonnull JaxBerechtigung jaxBerechtigung, @Nonnull Berechtigung berechtigung) {
		final Set<Gemeinde> gemeindeListe = new HashSet<>();
		for (JaxGemeinde jaxGemeinde : jaxBerechtigung.getGemeindeList()) {
			if (jaxGemeinde.getId() != null) {
				Gemeinde gemeinde = gemeindeService.findGemeinde(jaxGemeinde.getId())
					.orElseThrow(() -> new EbeguRuntimeException(
						"findGemeinde",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						jaxGemeinde.getId()));
				gemeindeListe.add(gemeinde);
			}
		}
		berechtigung.setGemeindeList(gemeindeListe);
	}

	public JaxBerechtigung berechtigungToJax(Berechtigung berechtigung) {
		JaxBerechtigung jaxBerechtigung = new JaxBerechtigung();
		convertAbstractDateRangedFieldsToJAX(berechtigung, jaxBerechtigung);
		jaxBerechtigung.setRole(berechtigung.getRole());
		if (berechtigung.getInstitution() != null) {
			jaxBerechtigung.setInstitution(institutionToJAX(berechtigung.getInstitution()));
		}
		if (berechtigung.getTraegerschaft() != null) {
			jaxBerechtigung.setTraegerschaft(traegerschaftLightToJAX(berechtigung.getTraegerschaft()));
		}
		// Gemeinden
		Set<JaxGemeinde> jaxGemeinden = berechtigung.getGemeindeList().stream()
			.map(this::gemeindeToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
		jaxBerechtigung.setGemeindeList(jaxGemeinden);

		return jaxBerechtigung;
	}

	public JaxBerechtigungHistory berechtigungHistoryToJax(BerechtigungHistory history) {
		JaxBerechtigungHistory jaxHistory = new JaxBerechtigungHistory();
		convertAbstractDateRangedFieldsToJAX(history, jaxHistory);
		requireNonNull(history.getUserErstellt());
		jaxHistory.setUserErstellt(history.getUserErstellt());
		jaxHistory.setUsername(history.getUsername());
		jaxHistory.setRole(history.getRole());
		if (history.getInstitution() != null) {
			jaxHistory.setInstitution(institutionToJAX(history.getInstitution()));
		}
		if (history.getTraegerschaft() != null) {
			jaxHistory.setTraegerschaft(traegerschaftLightToJAX(history.getTraegerschaft()));
		}
		jaxHistory.setGemeinden(history.getGemeinden());
		jaxHistory.setStatus(history.getStatus());
		jaxHistory.setGeloescht(history.getGeloescht());
		return jaxHistory;
	}

	public JaxDokumente dokumentGruendeToJAX(Set<DokumentGrund> dokumentGrunds) {
		JaxDokumente jaxDokumente = new JaxDokumente();

		for (DokumentGrund dokumentGrund : dokumentGrunds) {
			jaxDokumente.getDokumentGruende().add(dokumentGrundToJax(dokumentGrund));
		}

		return jaxDokumente;

	}

	public JaxDokumentGrund dokumentGrundToJax(DokumentGrund dokumentGrund) {
		JaxDokumentGrund jaxDokumentGrund = convertAbstractVorgaengerFieldsToJAX(
			dokumentGrund,
			new JaxDokumentGrund()
		);

		jaxDokumentGrund.setDokumentGrundTyp(dokumentGrund.getDokumentGrundTyp());
		jaxDokumentGrund.setTag(dokumentGrund.getTag());
		jaxDokumentGrund.setPersonType(dokumentGrund.getPersonType());
		jaxDokumentGrund.setPersonNumber(dokumentGrund.getPersonNumber());
		jaxDokumentGrund.setDokumentTyp(dokumentGrund.getDokumentTyp());
		jaxDokumentGrund.setNeeded(dokumentGrund.isNeeded());
		jaxDokumentGrund.setDokumente(new HashSet<>());
		dokumentGrund.getDokumente().stream()
			.map(this::dokumentToJax)
			.forEach(d -> jaxDokumentGrund.getDokumente().add(d));

		return jaxDokumentGrund;
	}

	private JaxDokument dokumentToJax(Dokument dokument) {
		JaxDokument jaxDokument = convertAbstractVorgaengerFieldsToJAX(dokument, new JaxDokument());
		convertFileToJax(dokument, jaxDokument);
		jaxDokument.setTimestampUpload(dokument.getTimestampUpload());
		if (StringUtils.isNotEmpty(dokument.getUserErstellt())) {
			benutzerService.findBenutzer(dokument.getUserErstellt())
				.map(this::benutzerToJaxBenutzer)
				.ifPresent(jaxDokument::setUserUploaded);
		}
		return jaxDokument;
	}

	public DokumentGrund dokumentGrundToEntity(
		@Nonnull final JaxDokumentGrund dokumentGrundJAXP,
		@Nonnull final DokumentGrund dokumentGrund) {

		requireNonNull(dokumentGrund);
		requireNonNull(dokumentGrundJAXP);

		convertAbstractVorgaengerFieldsToEntity(dokumentGrundJAXP, dokumentGrund);

		dokumentGrund.setDokumentGrundTyp(dokumentGrundJAXP.getDokumentGrundTyp());
		dokumentGrund.setTag(dokumentGrundJAXP.getTag());
		dokumentGrund.setPersonType(dokumentGrundJAXP.getPersonType());
		dokumentGrund.setPersonNumber(dokumentGrundJAXP.getPersonNumber());
		dokumentGrund.setDokumentTyp(dokumentGrundJAXP.getDokumentTyp());
		dokumentGrund.setNeeded(dokumentGrundJAXP.isNeeded());

		dokumenteToEntity(dokumentGrundJAXP.getDokumente(), dokumentGrund.getDokumente(), dokumentGrund);
		return dokumentGrund;
	}

	/**
	 * Goes through the whole list of jaxDokuments. For each (jax)dokument that already exists as Entity it merges
	 * both and adds the resulting (jax) dokument to the list. If the dokument doesn't exist it creates a new one and
	 * adds it to the list. Thus all dokumente that existed as entity but not in the list of jax, won't be added to
	 * the list and then removed (cascade and orphanremoval)
	 *
	 * @param jaxDokuments      Dokumente DTOs from Client
	 * @param existingDokumente List of currently stored Dokumente
	 */
	private void dokumenteToEntity(
		final Set<JaxDokument> jaxDokuments,
		final Collection<Dokument> existingDokumente,
		final DokumentGrund dokumentGrund) {

		final Set<Dokument> transformedDokumente = new HashSet<>();
		for (final JaxDokument jaxDokument : jaxDokuments) {
			final Dokument dokumenteToMergeWith = existingDokumente
				.stream()
				.filter(existingDokumentEntity -> existingDokumentEntity.getId().equals(jaxDokument.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new Dokument());
			final Dokument dokToAdd = dokumentToEntity(jaxDokument, dokumenteToMergeWith, dokumentGrund);
			final boolean added = transformedDokumente.add(dokToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", dokToAdd);
			}
		}

		//change the existing collection to reflect changes
		// Already tested: All existing Dokumente of the list remain as they were, that means their data are updated
		// and the objects are not created again. ID and InsertTimeStamp are the same as before
		existingDokumente.clear();
		existingDokumente.addAll(transformedDokumente);
	}

	private Dokument dokumentToEntity(JaxDokument jaxDokument, Dokument dokument, DokumentGrund dokumentGrund) {
		requireNonNull(dokument);
		requireNonNull(jaxDokument);
		requireNonNull(dokumentGrund);
		convertAbstractVorgaengerFieldsToEntity(jaxDokument, dokument);

		dokument.setDokumentGrund(dokumentGrund);
		dokument.setTimestampUpload(jaxDokument.getTimestampUpload());
		convertFileToEnity(jaxDokument, dokument);
		return dokument;
	}

	public JaxDownloadFile downloadFileToJAX(DownloadFile downloadFile) {
		JaxDownloadFile jaxDownloadFile = new JaxDownloadFile();
		convertFileToJax(downloadFile, jaxDownloadFile);
		jaxDownloadFile.setAccessToken(downloadFile.getAccessToken());
		return jaxDownloadFile;
	}

	public JaxWizardStep wizardStepToJAX(WizardStep wizardStep) {
		final JaxWizardStep jaxWizardStep = convertAbstractVorgaengerFieldsToJAX(wizardStep, new JaxWizardStep());
		jaxWizardStep.setGesuchId(wizardStep.getGesuch().getId());
		jaxWizardStep.setVerfuegbar(wizardStep.getVerfuegbar());
		jaxWizardStep.setWizardStepName(wizardStep.getWizardStepName());
		jaxWizardStep.setWizardStepStatus(wizardStep.getWizardStepStatus());
		jaxWizardStep.setBemerkungen(wizardStep.getBemerkungen());
		return jaxWizardStep;
	}

	public WizardStep wizardStepToEntity(final JaxWizardStep jaxWizardStep, final WizardStep wizardStep) {
		convertAbstractVorgaengerFieldsToEntity(jaxWizardStep, wizardStep);
		wizardStep.setVerfuegbar(jaxWizardStep.isVerfuegbar());
		wizardStep.setWizardStepName(jaxWizardStep.getWizardStepName());
		wizardStep.setWizardStepStatus(jaxWizardStep.getWizardStepStatus());
		wizardStep.setBemerkungen(jaxWizardStep.getBemerkungen());
		return wizardStep;
	}

	public JaxEbeguVorlage ebeguVorlageToJax(EbeguVorlage ebeguVorlage) {
		JaxEbeguVorlage jaxEbeguVorlage = new JaxEbeguVorlage();
		convertAbstractDateRangedFieldsToJAX(ebeguVorlage, jaxEbeguVorlage);

		jaxEbeguVorlage.setName(ebeguVorlage.getName());
		if (ebeguVorlage.getVorlage() != null) {
			jaxEbeguVorlage.setVorlage(vorlageToJax(ebeguVorlage.getVorlage()));
		}

		return jaxEbeguVorlage;
	}

	private JaxVorlage vorlageToJax(Vorlage vorlage) {
		JaxVorlage jaxVorlage = convertAbstractVorgaengerFieldsToJAX(vorlage, new JaxVorlage());
		convertFileToJax(vorlage, jaxVorlage);
		return jaxVorlage;
	}

	private JaxFile convertFileToJax(FileMetadata fileMetadata, JaxFile jaxFile) {
		jaxFile.setFilename(fileMetadata.getFilename());
		jaxFile.setFilepfad(fileMetadata.getFilepfad());
		jaxFile.setFilesize(fileMetadata.getFilesize());
		return jaxFile;
	}

	public EbeguVorlage ebeguVorlageToEntity(
		@Nonnull final JaxEbeguVorlage ebeguVorlageJAXP,
		@Nonnull final EbeguVorlage ebeguVorlage) {
		requireNonNull(ebeguVorlage);
		requireNonNull(ebeguVorlageJAXP);
		convertAbstractDateRangedFieldsToEntity(ebeguVorlageJAXP, ebeguVorlage);

		ebeguVorlage.setName(ebeguVorlageJAXP.getName());
		if (ebeguVorlageJAXP.getVorlage() != null) {
			if (ebeguVorlage.getVorlage() == null) {
				ebeguVorlage.setVorlage(new Vorlage());
			}
			vorlageToEntity(ebeguVorlageJAXP.getVorlage(), ebeguVorlage.getVorlage());
		}

		return ebeguVorlage;
	}

	private Vorlage vorlageToEntity(JaxVorlage jaxVorlage, Vorlage vorlage) {
		requireNonNull(vorlage);
		requireNonNull(jaxVorlage);
		convertAbstractVorgaengerFieldsToEntity(jaxVorlage, vorlage);
		convertFileToEnity(jaxVorlage, vorlage);
		return vorlage;
	}

	private FileMetadata convertFileToEnity(JaxFile jaxFile, FileMetadata fileMetadata) {
		requireNonNull(fileMetadata);
		requireNonNull(jaxFile);
		fileMetadata.setFilename(jaxFile.getFilename());
		fileMetadata.setFilepfad(jaxFile.getFilepfad());
		fileMetadata.setFilesize(jaxFile.getFilesize());
		return fileMetadata;
	}

	public JaxAntragStatusHistory antragStatusHistoryToJAX(AntragStatusHistory antragStatusHistory) {
		final JaxAntragStatusHistory jaxAntragStatusHistory =
			convertAbstractVorgaengerFieldsToJAX(antragStatusHistory, new JaxAntragStatusHistory());
		jaxAntragStatusHistory.setGesuchId(antragStatusHistory.getGesuch().getId());
		jaxAntragStatusHistory.setStatus(AntragStatusConverterUtil.convertStatusToDTO(
			antragStatusHistory.getGesuch(),
			antragStatusHistory.getStatus())
		);
		jaxAntragStatusHistory.setBenutzer(benutzerToJaxBenutzer(antragStatusHistory.getBenutzer()));
		jaxAntragStatusHistory.setTimestampVon(antragStatusHistory.getTimestampVon());
		jaxAntragStatusHistory.setTimestampBis(antragStatusHistory.getTimestampBis());
		return jaxAntragStatusHistory;

	}

	@Nonnull
	public Collection<JaxAntragStatusHistory> antragStatusHistoryCollectionToJAX(
		@Nullable Collection<AntragStatusHistory> antragStatusHistoryCollection) {

		if (antragStatusHistoryCollection == null) {
			return Collections.emptyList();
		}

		return antragStatusHistoryCollection.stream()
			.map(this::antragStatusHistoryToJAX)
			.collect(Collectors.toList());
	}

	/**
	 * Using the existing GesuchStatus and the UserRole it will translate the Status into the right one for this role.
	 */
	public void disguiseStatus(Gesuch gesuch, JaxAntragDTO antrag, @Nullable UserRole userRole) {
		if (userRole != null) {
			switch (userRole) {
			case GESUCHSTELLER:
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT:
				switch (gesuch.getStatus()) {
				case PRUEFUNG_STV:
				case GEPRUEFT_STV:
				case IN_BEARBEITUNG_STV:
					antrag.setStatus(AntragStatusDTO.VERFUEGT);
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * transformiert ein gesuch in ein JaxAntragDTO unter beruecksichtigung der rollen und erlaubten institutionen
	 * - Fuer die Rolle Steueramt werden saemtlichen Daten von den Kindern nicht geladen
	 * - Fuer die Rolle Institution/Traegerschaft werden nur die relevanten Institutionen und Angebote geladen
	 */
	public JaxAntragDTO gesuchToAntragDTO(
		Gesuch gesuch, @Nullable UserRole userRole,
		Collection<Institution> allowedInst) {
		//wir koennen nicht mit den container auf dem gesuch arbeiten weil das gesuch attached ist. hibernate
		//wuerde uns dann die kinder wegloeschen, daher besser transformieren
		Collection<JaxKindContainer> jaxKindContainers = new ArrayList<>(gesuch.getKindContainers().size());

		JaxAntragDTO antrag = gesuchToAntragDTOBasic(gesuch);

		if (userRole != STEUERAMT) {
			for (final KindContainer kind : gesuch.getKindContainers()) {
				jaxKindContainers.add(kindContainerToJAX(kind));
			}
			antrag.setKinder(createKinderList(jaxKindContainers));
		}

		if (EnumUtil.isOneOf(
			userRole,
			ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION)) {
			RestUtil.purgeKinderAndBetreuungenOfInstitutionen(jaxKindContainers, allowedInst);
		}

		disguiseStatus(gesuch, antrag, userRole);

		if (userRole != STEUERAMT) {
			antrag.setAngebote(createAngeboteList(jaxKindContainers));
			antrag.setInstitutionen(createInstitutionenList(jaxKindContainers));
		}

		return antrag;
	}

	public JaxAntragDTO gesuchToAntragDTO(Gesuch gesuch, @Nullable UserRole userRole) {
		JaxAntragDTO antrag = gesuchToAntragDTOBasic(gesuch);
		antrag.setKinder(createKinderList(gesuch.getKindContainers()));
		antrag.setAngebote(createAngeboteList(gesuch.getKindContainers()));
		antrag.setInstitutionen(createInstitutionenList(gesuch.getKindContainers()));
		disguiseStatus(gesuch, antrag, userRole);
		return antrag;
	}

	@Nonnull
	private JaxAntragDTO gesuchToAntragDTOBasic(@Nonnull Gesuch gesuch) {
		JaxAntragDTO antrag = new JaxAntragDTO();
		antrag.setAntragId(gesuch.getId());
		antrag.setFallNummer(gesuch.getFall().getFallNummer());
		antrag.setDossierId(gesuch.getDossier().getId());
		antrag.setFamilienName(gesuch.getGesuchsteller1() != null ? gesuch.getGesuchsteller1().extractNachname() : "");
		antrag.setEingangsdatum(gesuch.getEingangsdatum());
		antrag.setRegelnGueltigAb(gesuch.getRegelnGueltigAb());
		antrag.setEingangsdatumSTV(gesuch.getEingangsdatumSTV());
		antrag.setAenderungsdatum(gesuch.getTimestampMutiert());
		antrag.setAntragTyp(gesuch.getTyp());
		antrag.setStatus(AntragStatusConverterUtil.convertStatusToDTO(gesuch, gesuch.getStatus()));
		antrag.setGesuchsperiodeGueltigAb(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
		antrag.setGesuchsperiodeGueltigBis(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis());
		antrag.setGemeinde(gesuch.getDossier().getGemeinde().getName());
		Benutzer verantwortlicherBG = gesuch.getDossier().getVerantwortlicherBG();
		if (verantwortlicherBG != null) {
			setVerantwortlicherBGToAntragDTO(antrag, verantwortlicherBG);
		}
		Benutzer verantwortlicherTS = gesuch.getDossier().getVerantwortlicherTS();
		if (verantwortlicherTS != null) {
			setVerantwortlicherTSToAntragDTO(antrag, verantwortlicherTS);
		}
		antrag.setVerfuegt(gesuch.getStatus().isAnyStatusOfVerfuegt());
		antrag.setBeschwerdeHaengig(gesuch.getStatus() == AntragStatus.BESCHWERDE_HAENGIG);
		antrag.setLaufnummer(gesuch.getLaufnummer());
		antrag.setEingangsart(gesuch.getEingangsart());
		Benutzer besitzer = gesuch.getFall().getBesitzer();
		String besitzerUsername = besitzer == null ? null : besitzer.getUsername();
		antrag.setBesitzerUsername(besitzerUsername);
		antrag.setGesuchBetreuungenStatus(gesuch.getGesuchBetreuungenStatus());
		antrag.setDokumenteHochgeladen(gesuch.getDokumenteHochgeladen());
		antrag.setFinSitStatus(gesuch.getFinSitStatus());

		return antrag;
	}

	private void setVerantwortlicherTSToAntragDTO(@Nonnull JaxAntragDTO antrag, @Nonnull Benutzer verantwortlicherTS) {
		antrag.setVerantwortlicherTS(verantwortlicherTS.getFullName());
		antrag.setVerantwortlicherUsernameTS(verantwortlicherTS.getUsername());
	}

	private void setVerantwortlicherBGToAntragDTO(@Nonnull JaxAntragDTO antrag, @Nonnull Benutzer verantwortlicherBG) {
		antrag.setVerantwortlicherBG(verantwortlicherBG.getFullName());
		antrag.setVerantwortlicherUsernameBG(verantwortlicherBG.getUsername());
	}

	public Mahnung mahnungToEntity(@Nonnull final JaxMahnung jaxMahnung, @Nonnull final Mahnung mahnung) {
		requireNonNull(mahnung);
		requireNonNull(jaxMahnung);
		requireNonNull(jaxMahnung.getGesuch());
		requireNonNull(jaxMahnung.getGesuch().getId());

		convertAbstractVorgaengerFieldsToEntity(jaxMahnung, mahnung);

		Gesuch gesuchFromDB = gesuchService.findGesuch(jaxMahnung.getGesuch().getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"mahnungToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				jaxMahnung.getGesuch()));

		// hier laden wir das Gesuch aus der db aber konvertieren die Gesuchsdaten vom Client NICHT
		mahnung.setGesuch(gesuchFromDB);

		mahnung.setMahnungTyp(jaxMahnung.getMahnungTyp());
		mahnung.setDatumFristablauf(jaxMahnung.getDatumFristablauf());
		mahnung.setBemerkungen(jaxMahnung.getBemerkungen());
		mahnung.setTimestampAbgeschlossen(jaxMahnung.getTimestampAbgeschlossen());
		mahnung.setAbgelaufen(jaxMahnung.getAbgelaufen());

		return mahnung;
	}

	public JaxMahnung mahnungToJAX(@Nonnull final Mahnung persistedMahnung) {
		final JaxMahnung jaxMahnung = new JaxMahnung();
		convertAbstractVorgaengerFieldsToJAX(persistedMahnung, jaxMahnung);

		jaxMahnung.setGesuch(this.gesuchToJAX(persistedMahnung.getGesuch()));
		jaxMahnung.setMahnungTyp(persistedMahnung.getMahnungTyp());
		jaxMahnung.setDatumFristablauf(persistedMahnung.getDatumFristablauf());
		jaxMahnung.setBemerkungen(persistedMahnung.getBemerkungen());
		jaxMahnung.setTimestampAbgeschlossen(persistedMahnung.getTimestampAbgeschlossen());
		jaxMahnung.setAbgelaufen(persistedMahnung.getAbgelaufen());

		return jaxMahnung;
	}

	/**
	 * Geht durch die ganze Liste von KindContainers durch und gibt ein Set mit den BetreuungsangebotTyp aller
	 * Institutionen zurueck.
	 * Da ein Set zurueckgegeben wird, sind die Daten nie dupliziert.
	 */
	private Set<BetreuungsangebotTyp> createAngeboteList(Set<KindContainer> kindContainers) {
		return kindContainers.stream()
			.flatMap(kc -> kc.getAllPlaetze().stream())
			.map(b -> b.getInstitutionStammdaten().getBetreuungsangebotTyp())
			.collect(Collectors.toSet());
	}

	private Set<BetreuungsangebotTyp> createAngeboteList(Collection<JaxKindContainer> jaxKindContainers) {
		return jaxKindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(b -> b.getInstitutionStammdaten().getBetreuungsangebotTyp())
			.collect(Collectors.toSet());
	}

	private Set<String> createKinderList(Set<KindContainer> kindContainers) {
		return kindContainers.stream()
			.map(kc -> kc.getKindJA().getVorname())
			.collect(Collectors.toSet());
	}

	private Set<String> createKinderList(Collection<JaxKindContainer> jaxKindContainers) {
		return jaxKindContainers.stream()
			.map(kc -> kc.getKindJA().getVorname())
			.collect(Collectors.toSet());
	}

	/**
	 * Geht durch die ganze Liste von KindContainers durch und gibt ein Set mit den Namen aller Institutionen zurueck.
	 * Da ein Set zurueckgegeben wird, sind die Daten nie dupliziert.
	 */
	@SuppressWarnings("Duplicates")
	private Set<String> createInstitutionenList(Set<KindContainer> kindContainers) {
		return kindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(Betreuung::getInstitutionStammdaten)
			.map(is -> is.getInstitution().getName())
			.collect(Collectors.toSet());
	}

	@SuppressWarnings("Duplicates")
	private Set<String> createInstitutionenList(Collection<JaxKindContainer> jaxKindContainers) {
		return jaxKindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(JaxBetreuung::getInstitutionStammdaten)
			.filter(Objects::nonNull)
			.map(is -> is.getInstitution().getName())
			.collect(Collectors.toSet());
	}

	public Mitteilung mitteilungToEntity(JaxMitteilung mitteilungJAXP, Mitteilung mitteilung) {
		requireNonNull(mitteilung);
		requireNonNull(mitteilungJAXP);
		requireNonNull(mitteilungJAXP.getDossier());
		requireNonNull(mitteilungJAXP.getDossier().getId());

		convertAbstractVorgaengerFieldsToEntity(mitteilungJAXP, mitteilung);

		if (mitteilungJAXP.getEmpfaenger() != null) {
			Benutzer empfaenger = benutzerService.findBenutzer(mitteilungJAXP.getEmpfaenger().getUsername())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"mitteilungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					mitteilungJAXP.getEmpfaenger()));
			// because the user doesn't come from the client but from the server
			mitteilung.setEmpfaenger(empfaenger);
		}

		mitteilung.setEmpfaengerTyp(mitteilungJAXP.getEmpfaengerTyp());
		Dossier dossier = dossierService.findDossier(mitteilungJAXP.getDossier().getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"mitteilungToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				mitteilungJAXP.getDossier()));
		requireNonNull(mitteilungJAXP.getDossier());
		mitteilung.setDossier(this.dossierToEntity(mitteilungJAXP.getDossier(), dossier));

		if (mitteilungJAXP.getBetreuung() != null) {
			mitteilung.setBetreuung(betreuungToEntity(mitteilungJAXP.getBetreuung(), new Betreuung()));
		}
		mitteilung.setMessage(mitteilungJAXP.getMessage());
		mitteilung.setMitteilungStatus(mitteilungJAXP.getMitteilungStatus());

		if (mitteilungJAXP.getSender() != null) {
			Benutzer sender = benutzerService.findBenutzer(mitteilungJAXP.getSender().getUsername())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"mitteilungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					mitteilungJAXP.getSender()));
			// because the user doesn't come from the client but from the server
			mitteilung.setSender(sender);
		}

		mitteilung.setSenderTyp(mitteilungJAXP.getSenderTyp());
		mitteilung.setSubject(mitteilungJAXP.getSubject());
		mitteilung.setSentDatum(mitteilungJAXP.getSentDatum());

		return mitteilung;
	}

	public JaxMitteilung mitteilungToJAX(Mitteilung persistedMitteilung, JaxMitteilung jaxMitteilung) {
		convertAbstractVorgaengerFieldsToJAX(persistedMitteilung, jaxMitteilung);
		if (persistedMitteilung.getEmpfaenger() != null) {
			jaxMitteilung.setEmpfaenger(benutzerToJaxBenutzer(persistedMitteilung.getEmpfaenger()));
		}
		jaxMitteilung.setEmpfaengerTyp(persistedMitteilung.getEmpfaengerTyp());
		jaxMitteilung.setDossier(this.dossierToJAX(persistedMitteilung.getDossier()));
		if (persistedMitteilung.getBetreuung() != null) {
			jaxMitteilung.setBetreuung(betreuungToJAX(persistedMitteilung.getBetreuung()));
		}
		jaxMitteilung.setMessage(persistedMitteilung.getMessage());
		jaxMitteilung.setMitteilungStatus(persistedMitteilung.getMitteilungStatus());
		jaxMitteilung.setSender(benutzerToJaxBenutzer(persistedMitteilung.getSender()));
		jaxMitteilung.setSenderTyp(persistedMitteilung.getSenderTyp());
		jaxMitteilung.setSubject(persistedMitteilung.getSubject());
		jaxMitteilung.setSentDatum(persistedMitteilung.getSentDatum());

		return jaxMitteilung;
	}

	/**
	 * Creates the Betreuungsmitteilung without taking into accoutn if it already exists or not
	 */
	@Nonnull
	public Betreuungsmitteilung betreuungsmitteilungToEntity(
		@Nonnull JaxBetreuungsmitteilung mitteilungJAXP,
		@Nonnull Betreuungsmitteilung betreuungsmitteilung) {

		requireNonNull(mitteilungJAXP);
		requireNonNull(betreuungsmitteilung);

		mitteilungToEntity(mitteilungJAXP, betreuungsmitteilung);

		betreuungsmitteilung.setApplied(mitteilungJAXP.getApplied());
		if (mitteilungJAXP.getBetreuungspensen() != null) {
			Set<BetreuungsmitteilungPensum> pensen = mitteilungJAXP.getBetreuungspensen().stream()
				.map(jaxPensum -> toBetreuungsmitteilungPensum(jaxPensum, betreuungsmitteilung))
				.collect(Collectors.toSet());

			betreuungsmitteilung.setBetreuungspensen(pensen);
		}
		return betreuungsmitteilung;
	}

	@Nonnull
	private BetreuungsmitteilungPensum toBetreuungsmitteilungPensum(
		@Nonnull JaxBetreuungsmitteilungPensum jaxPensum,
		@Nonnull Betreuungsmitteilung betreuungsmitteilung) {

		BetreuungsmitteilungPensum p = betreuungsmitteilungpensumToEntity(jaxPensum, new BetreuungsmitteilungPensum());
		p.setBetreuungsmitteilung(betreuungsmitteilung);

		return p;
	}

	@Nonnull
	public JaxBetreuungsmitteilung betreuungsmitteilungToJAX(@Nonnull Betreuungsmitteilung persistedMitteilung) {
		final JaxBetreuungsmitteilung jaxBetreuungsmitteilung = new JaxBetreuungsmitteilung();
		mitteilungToJAX(persistedMitteilung, jaxBetreuungsmitteilung);

		jaxBetreuungsmitteilung.setApplied(persistedMitteilung.isApplied());
		if (persistedMitteilung.getBetreuungspensen() != null) {
			List<JaxBetreuungsmitteilungPensum> pensen = persistedMitteilung.getBetreuungspensen().stream()
				.map(this::betreuungsmitteilungPensumToJax)
				.collect(Collectors.toList());
			jaxBetreuungsmitteilung.setBetreuungspensen(pensen);
		}
		return jaxBetreuungsmitteilung;
	}

	public JaxZahlungsauftrag zahlungsauftragToJAX(
		final Zahlungsauftrag persistedZahlungsauftrag,
		boolean convertZahlungen) {

		return getJaxZahlungsauftrag(persistedZahlungsauftrag, convertZahlungen);
	}

	private JaxZahlungsauftrag getJaxZahlungsauftrag(
		Zahlungsauftrag persistedZahlungsauftrag,
		boolean convertZahlungen) {

		final JaxZahlungsauftrag jaxZahlungsauftrag = new JaxZahlungsauftrag();
		convertAbstractDateRangedFieldsToJAX(persistedZahlungsauftrag, jaxZahlungsauftrag);
		jaxZahlungsauftrag.setStatus(persistedZahlungsauftrag.getStatus());
		jaxZahlungsauftrag.setBeschrieb(persistedZahlungsauftrag.getBeschrieb());
		jaxZahlungsauftrag.setBetragTotalAuftrag(persistedZahlungsauftrag.getBetragTotalAuftrag());
		jaxZahlungsauftrag.setGemeinde(gemeindeToJAX(persistedZahlungsauftrag.getGemeinde()));
		jaxZahlungsauftrag.setDatumFaellig(persistedZahlungsauftrag.getDatumFaellig());
		jaxZahlungsauftrag.setDatumGeneriert(persistedZahlungsauftrag.getDatumGeneriert());
		jaxZahlungsauftrag.setHasNegativeZahlungen(persistedZahlungsauftrag.getHasNegativeZahlungen());

		if (convertZahlungen) {
			List<JaxZahlung> zahlungen = persistedZahlungsauftrag.getZahlungen().stream()
				.map(this::zahlungToJAX)
				.collect(Collectors.toList());
			jaxZahlungsauftrag.getZahlungen().addAll(zahlungen);
		}
		return jaxZahlungsauftrag;
	}

	public JaxZahlungsauftrag zahlungsauftragToJAX(
		final Zahlungsauftrag persistedZahlungsauftrag,
		@Nullable UserRole userRole,
		Collection<Institution> allowedInst) {

		final JaxZahlungsauftrag jaxZahlungsauftrag = getJaxZahlungsauftrag(persistedZahlungsauftrag, true);

		// nur die Zahlungen welche inst sehen darf
		if (EnumUtil.isOneOf(
			userRole,
			ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION)
		) {
			RestUtil.purgeZahlungenOfInstitutionen(jaxZahlungsauftrag, allowedInst);
			// es muss nochmal das Auftragstotal berechnet werden. Diesmal nur mit den erlaubten Zahlungen
			// Dies nur fuer Institutionen
			BigDecimal total = BigDecimal.ZERO;
			boolean hasAnyNegativeZahlung = false;
			for (JaxZahlung zahlung : jaxZahlungsauftrag.getZahlungen()) {
				total = MathUtil.DEFAULT.add(total, zahlung.getBetragTotalZahlung());
				if (MathUtil.isNegative(zahlung.getBetragTotalZahlung())) {
					hasAnyNegativeZahlung = true;
				}
			}
			jaxZahlungsauftrag.setBetragTotalAuftrag(total);
			jaxZahlungsauftrag.setHasNegativeZahlungen(hasAnyNegativeZahlung);
		} else {
			jaxZahlungsauftrag.setBetragTotalAuftrag(persistedZahlungsauftrag.getBetragTotalAuftrag());
		}
		return jaxZahlungsauftrag;
	}

	public JaxZahlung zahlungToJAX(final Zahlung persistedZahlung) {
		final JaxZahlung jaxZahlung = new JaxZahlung();
		convertAbstractVorgaengerFieldsToJAX(persistedZahlung, jaxZahlung);
		jaxZahlung.setStatus(persistedZahlung.getStatus());
		jaxZahlung.setBetragTotalZahlung(persistedZahlung.getBetragTotalZahlung());
		jaxZahlung.setInstitutionsName(persistedZahlung.getInstitutionStammdaten().getInstitution().getName());
		jaxZahlung.setBetreuungsangebotTyp(persistedZahlung.getInstitutionStammdaten().getBetreuungsangebotTyp());
		jaxZahlung.setInstitutionsId(persistedZahlung.getInstitutionStammdaten().getInstitution().getId());
		return jaxZahlung;
	}

	@Nonnull
	public GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdatenToEntity(
		@Nonnull JaxGemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdatenJAX,
		@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten) {

		requireNonNull(ferieninselStammdatenJAX);
		requireNonNull(ferieninselStammdaten);

		convertAbstractVorgaengerFieldsToEntity(ferieninselStammdatenJAX, ferieninselStammdaten);
		ferieninselStammdaten.setFerienname(ferieninselStammdatenJAX.getFerienname());
		ferieninselStammdaten.setAnmeldeschluss(ferieninselStammdatenJAX.getAnmeldeschluss());

		ferieninselZeitraumListToEntity(
			ferieninselStammdatenJAX.getZeitraumList(),
			ferieninselStammdaten.getZeitraumList());

		return ferieninselStammdaten;
	}

	private void ferieninselZeitraumListToEntity(
		@Nonnull List<JaxFerieninselZeitraum> zeitraeumeListJAX,
		@Nonnull Collection<GemeindeStammdatenGesuchsperiodeFerieninselZeitraum> zeitraeumeList) {

		final Set<GemeindeStammdatenGesuchsperiodeFerieninselZeitraum> transformedZeitraeume = new TreeSet<>();
		for (final JaxFerieninselZeitraum zeitraumJAX : zeitraeumeListJAX) {
			final GemeindeStammdatenGesuchsperiodeFerieninselZeitraum zeitraumToMergeWith = zeitraeumeList
				.stream()
				.filter(existingZeitraum -> existingZeitraum.getId().equals(zeitraumJAX.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(GemeindeStammdatenGesuchsperiodeFerieninselZeitraum::new);
			final GemeindeStammdatenGesuchsperiodeFerieninselZeitraum zeitraumToAdd =
				(GemeindeStammdatenGesuchsperiodeFerieninselZeitraum) convertAbstractDateRangedFieldsToEntity(zeitraumJAX, zeitraumToMergeWith);

			// only save a Zeitraum if the dates are set
			requireNonNull(zeitraumToAdd.getGueltigkeit().getGueltigAb());
			requireNonNull(zeitraumToAdd.getGueltigkeit().getGueltigBis());

			final boolean added = transformedZeitraeume.add(zeitraumToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", zeitraumToAdd);
			}
		}
		zeitraeumeList.clear();
		zeitraeumeList.addAll(transformedZeitraeume);
	}

	@Nonnull
	public JaxGemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdatenToJAX(
		@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel persistedFerieninselStammdaten) {

		final JaxGemeindeStammdatenGesuchsperiodeFerieninsel jaxGemeindeStammdatenGesuchsperiodeFerieninsel =
			new JaxGemeindeStammdatenGesuchsperiodeFerieninsel();

		convertAbstractVorgaengerFieldsToJAX(persistedFerieninselStammdaten,
			jaxGemeindeStammdatenGesuchsperiodeFerieninsel);
		jaxGemeindeStammdatenGesuchsperiodeFerieninsel.setFerienname(persistedFerieninselStammdaten.getFerienname());
		jaxGemeindeStammdatenGesuchsperiodeFerieninsel.setAnmeldeschluss(persistedFerieninselStammdaten.getAnmeldeschluss());
		jaxGemeindeStammdatenGesuchsperiodeFerieninsel.setFerienActive(persistedFerieninselStammdaten.isFerienActive());
		for (GemeindeStammdatenGesuchsperiodeFerieninselZeitraum ferieninselZeitraum :
			persistedFerieninselStammdaten.getZeitraumList()) {
			JaxFerieninselZeitraum jaxFerieninselZeitraum = new JaxFerieninselZeitraum();
			convertAbstractDateRangedFieldsToJAX(ferieninselZeitraum, jaxFerieninselZeitraum);
			jaxGemeindeStammdatenGesuchsperiodeFerieninsel.getZeitraumList().add(jaxFerieninselZeitraum);
		}
		return jaxGemeindeStammdatenGesuchsperiodeFerieninsel;
	}

	@Nullable
	public BelegungFerieninsel belegungFerieninselToEntity(
		@Nullable JaxBelegungFerieninsel belegungFerieninselJAX,
		@Nonnull BelegungFerieninsel belegungFerieninsel) {

		if (belegungFerieninselJAX == null) {
			return null;
		}

		requireNonNull(belegungFerieninsel);

		convertAbstractVorgaengerFieldsToEntity(belegungFerieninselJAX, belegungFerieninsel);
		belegungFerieninsel.setFerienname(belegungFerieninselJAX.getFerienname());
		belegungFerieninselTageListToEntity(belegungFerieninselJAX.getTage(), belegungFerieninsel.getTage());

		return belegungFerieninsel;
	}

	private void belegungFerieninselTageListToEntity(
		@Nonnull List<JaxBelegungFerieninselTag> jaxTagList,
		@Nonnull Collection<BelegungFerieninselTag> tagList) {

		final Set<BelegungFerieninselTag> transformedTagList = new TreeSet<>();
		for (final JaxBelegungFerieninselTag jaxTag : jaxTagList) {
			final BelegungFerieninselTag tagToMergeWith = tagList.stream()
				.filter(existingTagEntity -> existingTagEntity.getId().equals(jaxTag.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(BelegungFerieninselTag::new);
			final BelegungFerieninselTag tagToAdd = belegungFerieninselTagToEntity(jaxTag, tagToMergeWith);
			final boolean added = transformedTagList.add(tagToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", tagToAdd);
			}
		}

		//change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		tagList.clear();
		tagList.addAll(transformedTagList);
	}

	private BelegungFerieninselTag belegungFerieninselTagToEntity(
		@Nonnull JaxBelegungFerieninselTag jaxTag,
		@Nonnull BelegungFerieninselTag tag) {

		requireNonNull(jaxTag);
		requireNonNull(tag);

		convertAbstractVorgaengerFieldsToEntity(jaxTag, tag);
		tag.setTag(jaxTag.getTag());

		return tag;
	}

	@Nullable
	public JaxBelegungFerieninsel belegungFerieninselToJAX(@Nullable BelegungFerieninsel persistedBelegungFerieninsel) {
		if (persistedBelegungFerieninsel == null) {
			return null;
		}

		final JaxBelegungFerieninsel jaxBelegungFerieninsel = new JaxBelegungFerieninsel();
		convertAbstractVorgaengerFieldsToJAX(persistedBelegungFerieninsel, jaxBelegungFerieninsel);
		jaxBelegungFerieninsel.setFerienname(persistedBelegungFerieninsel.getFerienname());
		jaxBelegungFerieninsel.setTage(belegungFerieninselTageListToJAX(persistedBelegungFerieninsel.getTage()));

		return jaxBelegungFerieninsel;
	}

	@Nonnull
	public List<JaxBelegungFerieninselTag> belegungFerieninselTageListToJAX(
		@Nonnull Collection<BelegungFerieninselTag> persistedFerieninselTageList) {

		return persistedFerieninselTageList.stream()
			.map(this::belegungFerieninselTagToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public JaxBelegungFerieninselTag belegungFerieninselTagToJAX(
		@Nonnull BelegungFerieninselTag persistedFerieninselTag) {

		JaxBelegungFerieninselTag jaxTag = new JaxBelegungFerieninselTag();
		convertAbstractVorgaengerFieldsToJAX(persistedFerieninselTag, jaxTag);
		jaxTag.setTag(persistedFerieninselTag.getTag());

		return jaxTag;
	}

	@Nonnull
	public Gemeinde gemeindeToEntity(@Nonnull final JaxGemeinde jaxGemeinde, @Nonnull final Gemeinde gemeinde) {
		requireNonNull(gemeinde);
		requireNonNull(jaxGemeinde);
		requireNonNull(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		requireNonNull(jaxGemeinde.getTagesschulanmeldungenStartdatum());
		requireNonNull(jaxGemeinde.getFerieninselanmeldungenStartdatum());
		convertAbstractFieldsToEntity(jaxGemeinde, gemeinde);
		gemeinde.setName(jaxGemeinde.getName());
		gemeinde.setStatus(jaxGemeinde.getStatus());
		gemeinde.setGemeindeNummer(jaxGemeinde.getGemeindeNummer());
		gemeinde.setBfsNummer(jaxGemeinde.getBfsNummer());
		gemeinde.setBetreuungsgutscheineStartdatum(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		gemeinde.setTagesschulanmeldungenStartdatum(jaxGemeinde.getTagesschulanmeldungenStartdatum());
		gemeinde.setFerieninselanmeldungenStartdatum(jaxGemeinde.getFerieninselanmeldungenStartdatum());
		gemeinde.setAngebotBG(jaxGemeinde.isAngebotBG());
		gemeinde.setAngebotTS(jaxGemeinde.isAngebotTS());
		gemeinde.setAngebotFI(jaxGemeinde.isAngebotFI());
		return gemeinde;
	}

	public JaxGemeinde gemeindeToJAX(@Nonnull final Gemeinde persistedGemeinde) {
		final JaxGemeinde jaxGemeinde = new JaxGemeinde();
		convertAbstractFieldsToJAX(persistedGemeinde, jaxGemeinde);
		jaxGemeinde.setKey(persistedGemeinde.getId());
		jaxGemeinde.setName(persistedGemeinde.getName());
		jaxGemeinde.setStatus(persistedGemeinde.getStatus());
		jaxGemeinde.setGemeindeNummer(persistedGemeinde.getGemeindeNummer());
		jaxGemeinde.setBfsNummer(persistedGemeinde.getBfsNummer());
		jaxGemeinde.setBetreuungsgutscheineStartdatum(persistedGemeinde.getBetreuungsgutscheineStartdatum());
		jaxGemeinde.setTagesschulanmeldungenStartdatum(persistedGemeinde.getTagesschulanmeldungenStartdatum());
		jaxGemeinde.setFerieninselanmeldungenStartdatum(persistedGemeinde.getFerieninselanmeldungenStartdatum());
		jaxGemeinde.setAngebotBG(persistedGemeinde.isAngebotBG());
		jaxGemeinde.setAngebotTS(persistedGemeinde.isAngebotTS());
		jaxGemeinde.setAngebotFI(persistedGemeinde.isAngebotFI());
		return jaxGemeinde;
	}

	@Nonnull
	public JaxBfsGemeinde gemeindeBfsToJax(@Nonnull final BfsGemeinde bfsGemeinde) {
		// Aktuell brauchen wir nur den Namen und die BFS-Nummer
		JaxBfsGemeinde jaxBfsGemeinde = new JaxBfsGemeinde();
		jaxBfsGemeinde.setName(bfsGemeinde.getName());
		jaxBfsGemeinde.setBfsNummer(bfsGemeinde.getBfsNummer());
		return jaxBfsGemeinde;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	public GemeindeStammdaten gemeindeStammdatenToEntity(
		@Nonnull final JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull final GemeindeStammdaten stammdaten
	) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getAdresse());
		requireNonNull(jaxStammdaten);
		requireNonNull(jaxStammdaten.getGemeinde());
		requireNonNull(jaxStammdaten.getGemeinde().getId());
		requireNonNull(jaxStammdaten.getAdresse());
		requireNonNull(jaxStammdaten.getStandardRechtsmittelbelehrung());
		requireNonNull(jaxStammdaten.getBenachrichtigungBgEmailAuto());
		requireNonNull(jaxStammdaten.getBenachrichtigungTsEmailAuto());
		requireNonNull(jaxStammdaten.getStandardDokSignature());

		convertAbstractFieldsToEntity(jaxStammdaten, stammdaten);

		if (jaxStammdaten.getDefaultBenutzerBG() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzerBG().getUsername())
				.ifPresent(stammdaten::setDefaultBenutzerBG);
		}
		if (jaxStammdaten.getDefaultBenutzerTS() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzerTS().getUsername())
				.ifPresent(stammdaten::setDefaultBenutzerTS);
		}
		if (jaxStammdaten.getDefaultBenutzer() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzer().getUsername())
				.ifPresent(stammdaten::setDefaultBenutzer);
		}

		// Die Gemeinde selbst ändert nicht, nur wieder von der DB lesen
		gemeindeService.findGemeinde(jaxStammdaten.getGemeinde().getId())
			.ifPresent(stammdaten::setGemeinde);

		gemeindeStammdatenAdressenToEntity(jaxStammdaten, stammdaten);
		stammdaten.setMail(jaxStammdaten.getMail());
		stammdaten.setTelefon(jaxStammdaten.getTelefon());
		stammdaten.setWebseite(jaxStammdaten.getWebseite());

		if (jaxStammdaten.isKorrespondenzspracheDe() && jaxStammdaten.isKorrespondenzspracheFr()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		} else if (jaxStammdaten.isKorrespondenzspracheDe()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		} else if (jaxStammdaten.isKorrespondenzspracheFr()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.FR);
		} else {
			throw new IllegalArgumentException("Die Korrespondenzsprache muss gesetzt sein");
		}

		stammdaten.setKontoinhaber(jaxStammdaten.getKontoinhaber());
		stammdaten.setBic(jaxStammdaten.getBic());
		if (jaxStammdaten.getIban() != null) {
			stammdaten.setIban(new IBAN(jaxStammdaten.getIban()));
		}

		stammdaten.setStandardRechtsmittelbelehrung(jaxStammdaten.getStandardRechtsmittelbelehrung());
		stammdaten.setBenachrichtigungBgEmailAuto(jaxStammdaten.getBenachrichtigungBgEmailAuto());
		stammdaten.setBenachrichtigungTsEmailAuto(jaxStammdaten.getBenachrichtigungTsEmailAuto());
		stammdaten.setStandardDokSignature(jaxStammdaten.getStandardDokSignature());

		stammdaten.setStandardDokTitle(jaxStammdaten.getStandardDokTitle());
		stammdaten.setStandardDokUnterschriftTitel(jaxStammdaten.getStandardDokUnterschriftTitel());
		stammdaten.setStandardDokUnterschriftName(jaxStammdaten.getStandardDokUnterschriftName());
		stammdaten.setStandardDokUnterschriftTitel2(jaxStammdaten.getStandardDokUnterschriftTitel2());
		stammdaten.setStandardDokUnterschriftName2(jaxStammdaten.getStandardDokUnterschriftName2());
		stammdaten.setTsVerantwortlicherNachVerfuegungBenachrichtigen(jaxStammdaten.getTsVerantwortlicherNachVerfuegungBenachrichtigen());

		stammdaten.setBgEmail(jaxStammdaten.getBgEmail());
		stammdaten.setBgTelefon(jaxStammdaten.getBgTelefon());
		stammdaten.setTsEmail(jaxStammdaten.getTsEmail());
		stammdaten.setTsTelefon(jaxStammdaten.getTsTelefon());

		if (jaxStammdaten.getRechtsmittelbelehrung() != null) {
			if (stammdaten.getRechtsmittelbelehrung() == null) {
				stammdaten.setRechtsmittelbelehrung(new TextRessource());
			}
			stammdaten.setRechtsmittelbelehrung(textRessourceToEntity(
				jaxStammdaten.getRechtsmittelbelehrung(),
				stammdaten.getRechtsmittelbelehrung()));
		}

		stammdaten.setUsernameScolaris(jaxStammdaten.getUsernameScolaris());

		return stammdaten;
	}

	private void gemeindeStammdatenAdressenToEntity(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		adresseToEntity(jaxStammdaten.getAdresse(), stammdaten.getAdresse());

		if (jaxStammdaten.getBgAdresse() != null) {
			if (stammdaten.getBgAdresse() == null) {
				stammdaten.setBgAdresse(new Adresse());
			}
			adresseToEntity(jaxStammdaten.getBgAdresse(), stammdaten.getBgAdresse());
		} else {
			stammdaten.setBgAdresse(null);
		}

		if (jaxStammdaten.getTsAdresse() != null) {
			if (stammdaten.getTsAdresse() == null) {
				stammdaten.setTsAdresse(new Adresse());
			}
			adresseToEntity(jaxStammdaten.getTsAdresse(), stammdaten.getTsAdresse());
		} else {
			stammdaten.setTsAdresse(null);
		}

		if (jaxStammdaten.getBeschwerdeAdresse() != null) {
			if (stammdaten.getBeschwerdeAdresse() == null) {
				stammdaten.setBeschwerdeAdresse(new Adresse());
			}
			adresseToEntity(jaxStammdaten.getBeschwerdeAdresse(), stammdaten.getBeschwerdeAdresse());
		} else {
			stammdaten.setBeschwerdeAdresse(null);
		}
	}

	public JaxGemeindeStammdaten gemeindeStammdatenToJAX(@Nonnull final GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getGemeinde());
		requireNonNull(stammdaten.getAdresse());
		final JaxGemeindeStammdaten jaxStammdaten = new JaxGemeindeStammdaten();
		convertAbstractFieldsToJAX(stammdaten, jaxStammdaten);
		Collection<Benutzer> administratoren = benutzerService.getGemeindeAdministratoren(stammdaten.getGemeinde());
		Collection<Benutzer> sachbearbeiter = benutzerService.getGemeindeSachbearbeiter(stammdaten.getGemeinde());
		jaxStammdaten.setAdministratoren(administratoren.stream()
			.map(Benutzer::getFullName)
			.collect(Collectors.joining(", ")));
		jaxStammdaten.setSachbearbeiter(sachbearbeiter.stream()
			.map(Benutzer::getFullName)
			.collect(Collectors.joining(", ")));
		jaxStammdaten.setGemeinde(gemeindeToJAX(stammdaten.getGemeinde()));
		jaxStammdaten.setMail(stammdaten.getMail());
		jaxStammdaten.setTelefon(stammdaten.getTelefon());
		jaxStammdaten.setWebseite(stammdaten.getWebseite());
		gemeindeStammdatenToJAXSetKorrespondenzsprache(jaxStammdaten, stammdaten);
		gemeindeStammdatenToJAXSetDefaultBenutzer(jaxStammdaten, stammdaten);
		gemeindeStammdatenAdressenToJax(jaxStammdaten, stammdaten);
		jaxStammdaten.setBgTelefon(stammdaten.getBgTelefon());
		jaxStammdaten.setBgEmail(stammdaten.getBgEmail());
		jaxStammdaten.setTsTelefon(stammdaten.getTsTelefon());
		jaxStammdaten.setTsEmail(stammdaten.getTsEmail());

		// Konfiguration: Wir laden immer alle Gesuchsperioden
		for (Gesuchsperiode gesuchsperiode : gesuchsperiodeService.getAllGesuchsperioden()) {
			jaxStammdaten.getKonfigurationsListe().add(loadGemeindeKonfiguration(
				stammdaten.getGemeinde(),
				gesuchsperiode));
		}
		jaxStammdaten.setKontoinhaber(stammdaten.getKontoinhaber());
		jaxStammdaten.setBic(stammdaten.getBic());
		if (stammdaten.getIban() != null) {
			jaxStammdaten.setIban(stammdaten.getIban().getIban());
		}

		jaxStammdaten.setStandardRechtsmittelbelehrung(stammdaten.getStandardRechtsmittelbelehrung());
		jaxStammdaten.setBenachrichtigungBgEmailAuto(stammdaten.getBenachrichtigungBgEmailAuto());
		jaxStammdaten.setBenachrichtigungTsEmailAuto(stammdaten.getBenachrichtigungTsEmailAuto());
		jaxStammdaten.setStandardDokSignature(stammdaten.getStandardDokSignature());

		jaxStammdaten.setStandardDokTitle(stammdaten.getStandardDokTitle());
		jaxStammdaten.setStandardDokUnterschriftTitel(stammdaten.getStandardDokUnterschriftTitel());
		jaxStammdaten.setStandardDokUnterschriftName(stammdaten.getStandardDokUnterschriftName());
		jaxStammdaten.setStandardDokUnterschriftTitel2(stammdaten.getStandardDokUnterschriftTitel2());
		jaxStammdaten.setStandardDokUnterschriftName2(stammdaten.getStandardDokUnterschriftName2());
		jaxStammdaten.setTsVerantwortlicherNachVerfuegungBenachrichtigen(stammdaten.getTsVerantwortlicherNachVerfuegungBenachrichtigen());

		if (stammdaten.getRechtsmittelbelehrung() != null) {
			jaxStammdaten.setRechtsmittelbelehrung(textRessourceToJAX(stammdaten.getRechtsmittelbelehrung()));
		}

		jaxStammdaten.setUsernameScolaris(stammdaten.getUsernameScolaris());

		return jaxStammdaten;
	}

	private void gemeindeStammdatenToJAXSetDefaultBenutzer(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		jaxStammdaten.setBenutzerListeBG(benutzerService.getBenutzerBgOrGemeinde(stammdaten.getGemeinde())
			.stream().map(this::benutzerToJaxBenutzer).collect(Collectors.toList()));
		jaxStammdaten.setBenutzerListeTS(benutzerService.getBenutzerTsOrGemeinde(stammdaten.getGemeinde())
			.stream().map(this::benutzerToJaxBenutzer).collect(Collectors.toList()));

		if (!stammdaten.isNew()) {
			if (stammdaten.getDefaultBenutzerBG() != null) {
				jaxStammdaten.setDefaultBenutzerBG(benutzerToJaxBenutzer(stammdaten.getDefaultBenutzerBG()));
			}
			if (stammdaten.getDefaultBenutzerTS() != null) {
				jaxStammdaten.setDefaultBenutzerTS(benutzerToJaxBenutzer(stammdaten.getDefaultBenutzerTS()));
			}
			if (stammdaten.getDefaultBenutzer() != null) {
				jaxStammdaten.setDefaultBenutzer(benutzerToJaxBenutzer(stammdaten.getDefaultBenutzer()));
			}
		}
	}

	private void gemeindeStammdatenAdressenToJax(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		jaxStammdaten.setAdresse(adresseToJAX(stammdaten.getAdresse()));
		if (stammdaten.getBeschwerdeAdresse() != null) {
			jaxStammdaten.setBeschwerdeAdresse(adresseToJAX(stammdaten.getBeschwerdeAdresse()));
		}
		if (stammdaten.getBgAdresse() != null) {
			jaxStammdaten.setBgAdresse(adresseToJAX(stammdaten.getBgAdresse()));
		}
		if (stammdaten.getTsAdresse() != null) {
			jaxStammdaten.setTsAdresse(adresseToJAX(stammdaten.getTsAdresse()));
		}
	}

	private void gemeindeStammdatenToJAXSetKorrespondenzsprache(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		if (KorrespondenzSpracheTyp.DE == stammdaten.getKorrespondenzsprache()) {
			jaxStammdaten.setKorrespondenzspracheDe(true);
			jaxStammdaten.setKorrespondenzspracheFr(false);
		} else if (KorrespondenzSpracheTyp.FR == stammdaten.getKorrespondenzsprache()) {
			jaxStammdaten.setKorrespondenzspracheDe(false);
			jaxStammdaten.setKorrespondenzspracheFr(true);
		} else if (KorrespondenzSpracheTyp.DE_FR == stammdaten.getKorrespondenzsprache()) {
			jaxStammdaten.setKorrespondenzspracheDe(true);
			jaxStammdaten.setKorrespondenzspracheFr(true);
		}
	}

	public TextRessource textRessourceToEntity(
		@Nonnull final JaxTextRessource textRessourceJAX,
		@Nullable TextRessource textRessource) {
		requireNonNull(textRessourceJAX);

		if (textRessource == null) {
			textRessource = new TextRessource();
		}

		convertAbstractFieldsToEntity(textRessourceJAX, textRessource);

		textRessource.setTextDeutsch(textRessourceJAX.getTextDeutsch());
		textRessource.setTextFranzoesisch(textRessourceJAX.getTextFranzoesisch());

		return textRessource;
	}

	public JaxTextRessource textRessourceToJAX(@Nonnull final TextRessource textRessource) {
		Objects.requireNonNull(textRessource);

		final JaxTextRessource jaxTextRessource = new JaxTextRessource();

		convertAbstractFieldsToJAX(textRessource, jaxTextRessource);

		jaxTextRessource.setTextDeutsch(textRessource.getTextDeutsch());
		jaxTextRessource.setTextFranzoesisch(textRessource.getTextFranzoesisch());

		return jaxTextRessource;
	}

	private JaxGemeindeKonfiguration loadGemeindeKonfiguration(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		JaxGemeindeKonfiguration konfiguration = new JaxGemeindeKonfiguration();
		konfiguration.setGesuchsperiodeName(gesuchsperiode.getGesuchsperiodeDisplayName(LocaleThreadLocal.get()));
		konfiguration.setGesuchsperiodeStatusName(gesuchsperiode.getGesuchsperiodeStatusName(LocaleThreadLocal.get()));
		konfiguration.setGesuchsperiode(gesuchsperiodeToJAX(gesuchsperiode));
		Map<EinstellungKey, Einstellung> konfigurationMap = einstellungService
			.getAllEinstellungenByGemeindeAsMap(gemeinde, gesuchsperiode);
		konfiguration.getKonfigurationen().addAll(konfigurationMap.entrySet().stream()
			.filter(map -> map.getKey().isGemeindeEinstellung())
			.map(x -> einstellungToJAX(x.getValue()))
			.collect(Collectors.toList()));

		Collection<Einstellung> einstellungenByMandant =
			einstellungService.getAllEinstellungenByMandant(gesuchsperiode);
		konfiguration.setErwerbspensumZuschlagMax(
			einstellungenByMandant.stream()
				.filter(einstellung ->
					einstellung.getKey() == EinstellungKey.ERWERBSPENSUM_ZUSCHLAG)
				.findFirst().get().getValueAsInteger()
		);
		konfiguration.setErwerbspensumMiminumVorschuleMax(
			einstellungenByMandant.stream()
				.filter(einstellung ->
					einstellung.getKey() == EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT)
				.findFirst().get().getValueAsInteger()
		);
		konfiguration.setErwerbspensumMiminumSchulkinderMax(
			einstellungenByMandant.stream()
				.filter(einstellung ->
					einstellung.getKey() == EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT)
				.findFirst().get().getValueAsInteger()
		);

		List<JaxGemeindeStammdatenGesuchsperiodeFerieninsel> ferieninselStammdaten =
			ferieninselStammdatenService.findGesuchsperiodeFerieninselByGemeindeAndPeriode(gemeinde.getId(),
				gesuchsperiode.getId())
				.stream()
				.map(this::ferieninselStammdatenToJAX)
				.collect(Collectors.toList());

		konfiguration.setFerieninselStammdaten(ferieninselStammdaten);

		return konfiguration;
	}

	@Nullable
	public JaxLastenausgleich lastenausgleichToJAX(@Nullable final Lastenausgleich persistedLastenausgleich) {
		if (persistedLastenausgleich == null) {
			return null;
		}
		JaxLastenausgleich jaxLastenausgleich = new JaxLastenausgleich();
		convertAbstractFieldsToJAX(persistedLastenausgleich, jaxLastenausgleich);
		jaxLastenausgleich.setJahr(persistedLastenausgleich.getJahr());
		jaxLastenausgleich.setTotalAlleGemeinden(persistedLastenausgleich.getTotalAlleGemeinden());

		return jaxLastenausgleich;
	}

	public void alwaysEditablePropertiesToGesuch(@Nonnull final JaxAlwaysEditableProperties properties,
		@Nonnull Gesuch gesuch) {

		// fields on GS1
		Gesuchsteller gs1 = gesuch.extractGesuchsteller1().orElseThrow(() -> new EbeguEntityNotFoundException(
			"alwaysEditablePropertiesToGesuch", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

		gs1.setMail(properties.getMailGS1());
		gs1.setMobile(properties.getMobileGS1());
		gs1.setTelefon(properties.getTelefonGS1());
		gs1.setTelefonAusland(properties.getTelefonAuslandGS1());

		// fields on GS2
		if (gesuch.getGesuchsteller2() != null) {

			Gesuchsteller gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();

			gs2.setMail(properties.getMailGS2());
			gs2.setMobile(properties.getMobileGS2());
			gs2.setTelefon(properties.getTelefonGS2());
			gs2.setTelefonAusland(properties.getTelefonAuslandGS2());

		}

		// fields on Familiensituation
		Familiensituation famSit = gesuch.extractFamiliensituation();

		if (famSit != null) {
			famSit.setKeineMahlzeitenverguenstigungBeantragt(properties.isKeineMahlzeitenverguenstigungBeantragt());

			if (properties.isKeineMahlzeitenverguenstigungBeantragt()) {
				properties.setIban(null);
				properties.setKontoinhaber(null);
				properties.setAbweichendeZahlungsadresse(false);
				properties.setZahlungsadresse(null);
			}

			if (properties.getIban() != null) {
				famSit.setIban(new IBAN(properties.getIban()));
			} else {
				famSit.setIban(null);
			}
			famSit.setKontoinhaber(properties.getKontoinhaber());
			famSit.setAbweichendeZahlungsadresse(properties.isAbweichendeZahlungsadresse());

			if (properties.isAbweichendeZahlungsadresse() && properties.getZahlungsadresse() != null) {
				famSit.setZahlungsadresse(this.adresseToEntity(properties.getZahlungsadresse(),
					famSit.getZahlungsadresse() == null ? new Adresse() : famSit.getZahlungsadresse()));
			}
		}
	}

	public void lastenausgleichGrundlagenToEntity() {
		throw new EbeguFingerWegException("lastenausgleichGrundlagenToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	public void lastenausgleichGrundlagenToJAX() {
		throw new EbeguFingerWegException("lastenausgleichGrundlagenToJAX", ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	public void lastenausgleichDetailListToEntity() {
		throw new EbeguFingerWegException("lastenausgleichDetailListToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	public void lastenausgleichDetailListToJax() {
		throw new EbeguFingerWegException("lastenausgleichDetailListToJax", ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	public void lastenausgleichDetailToEntity() {
		throw new EbeguFingerWegException("lastenausgleichDetailToEntity", ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	public void lastenausgleichDetailToJAX() {
		throw new EbeguFingerWegException("lastenausgleichDetailToJAX", ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE);
	}

	@Nonnull
	public SozialhilfeZeitraumContainer sozialhilfeZeitraumContainerToStorableEntity(@Nonnull final JaxSozialhilfeZeitraumContainer jaxShZCont) {
		SozialhilfeZeitraumContainer containerToMergeWith =
			Optional.ofNullable(jaxShZCont.getId())
				.flatMap(sozialhilfeZeitraumService::findSozialhilfeZeitraum)
				.orElseGet(SozialhilfeZeitraumContainer::new);
		return sozialhilfeZeitraumContainerToEntity(jaxShZCont, containerToMergeWith);
	}

	@Nonnull
	public SozialhilfeZeitraumContainer sozialhilfeZeitraumContainerToEntity(
		@Nonnull final JaxSozialhilfeZeitraumContainer jaxShZCont,
		@Nonnull final SozialhilfeZeitraumContainer sozialhilfeZeitraumCont) {

		convertAbstractVorgaengerFieldsToEntity(jaxShZCont, sozialhilfeZeitraumCont);
		if (jaxShZCont.getSozialhilfeZeitraumGS() != null) {
			SozialhilfeZeitraum shzToMergeWith =
				Optional.ofNullable(sozialhilfeZeitraumCont.getSozialhilfeZeitraumGS())
					.orElseGet(SozialhilfeZeitraum::new);
			SozialhilfeZeitraum sozialhilfeZeitraumGS =
				sozialhilfeZeitraumToEntity(jaxShZCont.getSozialhilfeZeitraumGS(),
					shzToMergeWith);
			sozialhilfeZeitraumCont.setSozialhilfeZeitraumGS(sozialhilfeZeitraumGS);
		}
		if (jaxShZCont.getSozialhilfeZeitraumJA() != null) {
			SozialhilfeZeitraum shzToMergeWith =
				Optional.ofNullable(sozialhilfeZeitraumCont.getSozialhilfeZeitraumJA())
					.orElseGet(SozialhilfeZeitraum::new);
			SozialhilfeZeitraum sozialhilfeZeitraumJA =
				sozialhilfeZeitraumToEntity(jaxShZCont.getSozialhilfeZeitraumJA(),
					shzToMergeWith);
			sozialhilfeZeitraumCont.setSozialhilfeZeitraumJA(sozialhilfeZeitraumJA);
		}

		return sozialhilfeZeitraumCont;
	}

	@Nonnull
	public JaxSozialhilfeZeitraumContainer sozialhilfeZeitraumContainerToJAX(
		@Nonnull final SozialhilfeZeitraumContainer storedSozialhilfeZeitraumCont) {

		final JaxSozialhilfeZeitraumContainer jaxShZCont = new JaxSozialhilfeZeitraumContainer();
		convertAbstractVorgaengerFieldsToJAX(storedSozialhilfeZeitraumCont, jaxShZCont);
		jaxShZCont.setSozialhilfeZeitraumGS(sozialhilfeZeitraumToJax(storedSozialhilfeZeitraumCont.getSozialhilfeZeitraumGS()));
		jaxShZCont.setSozialhilfeZeitraumJA(sozialhilfeZeitraumToJax(storedSozialhilfeZeitraumCont.getSozialhilfeZeitraumJA()));

		return jaxShZCont;
	}

	@Nonnull
	private SozialhilfeZeitraum sozialhilfeZeitraumToEntity(
		@Nonnull final JaxSozialhilfeZeitraum jaxSozialhilfeZeitraum,
		@Nonnull final SozialhilfeZeitraum sozialhilfeZeitraum) {

		convertAbstractDateRangedFieldsToEntity(jaxSozialhilfeZeitraum, sozialhilfeZeitraum);

		return sozialhilfeZeitraum;
	}

	@Nullable
	private JaxSozialhilfeZeitraum sozialhilfeZeitraumToJax(@Nullable final SozialhilfeZeitraum sozialhilfeZeitraum) {
		if (sozialhilfeZeitraum == null) {
			return null;
		}
		JaxSozialhilfeZeitraum jaxSozialhilfeZeitraum = new JaxSozialhilfeZeitraum();
		convertAbstractDateRangedFieldsToJAX(sozialhilfeZeitraum, jaxSozialhilfeZeitraum);
		return jaxSozialhilfeZeitraum;
	}

	private void sozialhilfeZeitraumContainersToEntity(
		@Nonnull final List<JaxSozialhilfeZeitraumContainer> jaxShZContainers,
		@Nonnull final Collection<SozialhilfeZeitraumContainer> existingSozialhilfeZeitraeume
	) {
		final Set<SozialhilfeZeitraumContainer> transformedShZContainers = new HashSet<>();
		for (final JaxSozialhilfeZeitraumContainer jaxShZContainer : jaxShZContainers) {
			final SozialhilfeZeitraumContainer containerToMergeWith = existingSozialhilfeZeitraeume
				.stream()
				.filter(existingShZEntity -> existingShZEntity.getId().equals(jaxShZContainer.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new SozialhilfeZeitraumContainer());
			final SozialhilfeZeitraumContainer contToAdd =
				sozialhilfeZeitraumContainerToEntity(jaxShZContainer, containerToMergeWith);
			final boolean added = transformedShZContainers.add(contToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", contToAdd);
			}
		}

		existingSozialhilfeZeitraeume.clear();
		existingSozialhilfeZeitraeume.addAll(transformedShZContainers);
	}

	@Nonnull
	private List<JaxSozialhilfeZeitraumContainer> sozialhilfeZeitraumContainersToJAX(@Nullable final Set<SozialhilfeZeitraumContainer> sozialhilfeZeitraumContainers) {
		if (sozialhilfeZeitraumContainers == null) {
			return Collections.emptyList();
		}

		return sozialhilfeZeitraumContainers.stream()
			.map(this::sozialhilfeZeitraumContainerToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public List<JaxExternalClient> externalClientsToJAX(@Nonnull Collection<ExternalClient> externalClients) {
		return externalClients.stream()
			.map(this::externalClientToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public List<JaxRueckforderungFormular> rueckforderungFormularListToJax(@Nonnull List<RueckforderungFormular> rueckforderungFormularList) {
		// wir deaktivieren flush() in der #rueckforderungFormularToJax Methode und führen es dann einmal aus.
		// ansonsten dauert das konvertieren zu lange.
		flush();

		List<JaxRueckforderungFormular> converted = rueckforderungFormularList.stream()
			.map(rueckforderungFormular -> this.rueckforderungFormularToJax(rueckforderungFormular, false))
			.collect(Collectors.toList());

		return converted;
	}

	@Nonnull
	public JaxRueckforderungFormular  rueckforderungFormularToJax(@Nonnull RueckforderungFormular rueckforderungFormular) {
		// per Default soll flush() ausgeführt werden
		return rueckforderungFormularToJax(rueckforderungFormular, true);
	}

	@Nonnull
	@SuppressWarnings("PMD.NcssMethodCount")
	public JaxRueckforderungFormular rueckforderungFormularToJax(@Nonnull RueckforderungFormular rueckforderungFormular, boolean flush) {

		// OptimisticLocking: Version richtig behandeln
		// da Flush die Performance verringert kann dies optional deaktiviert werden. Dies kann insbesondere dann gemacht
		// werden, wenn eine Liste an Ruckforderungsformulare konvertiert wird.
		if (flush) {
			flush();
		}

		JaxRueckforderungFormular jaxFormular = new JaxRueckforderungFormular();

		convertAbstractFieldsToJAX(rueckforderungFormular, jaxFormular);

		jaxFormular.setInstitutionStammdatenSummary(institutionStammdatenSummaryToJAX(rueckforderungFormular.getInstitutionStammdaten(), new JaxInstitutionStammdatenSummary()));
		jaxFormular.setStatus(rueckforderungFormular.getStatus());
		if (rueckforderungFormular.getVerantwortlicher() != null) {
			jaxFormular.setVerantwortlicherName(rueckforderungFormular.getVerantwortlicher().getFullName());
		}
		jaxFormular.setUncheckedDocuments(rueckforderungFormular.hasUncheckedDocuments());
		jaxFormular.setHasBeenProvisorisch(rueckforderungFormular.isHasBeenProvisorisch());

		jaxFormular.setStufe1KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
		jaxFormular.setStufe1InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
		jaxFormular.setStufe2KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe2KantonKostenuebernahmeAnzahlStunden());
		jaxFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
		jaxFormular.setStufe1KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
		jaxFormular.setStufe1InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlTage());
		jaxFormular.setStufe2KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe2KantonKostenuebernahmeAnzahlTage());
		jaxFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlTage());
		jaxFormular.setStufe1KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung());
		jaxFormular.setStufe1InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeBetreuung());
		jaxFormular.setStufe2KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe2KantonKostenuebernahmeBetreuung());
		jaxFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeBetreuung());
		jaxFormular.setStufe1FreigabeBetrag(rueckforderungFormular.getStufe1FreigabeBetrag());
		jaxFormular.setStufe1FreigabeDatum(rueckforderungFormular.getStufe1FreigabeDatum());
		jaxFormular.setStufe1FreigabeAusbezahltAm(rueckforderungFormular.getStufe1FreigabeAusbezahltAm());
		jaxFormular.setStufe2VerfuegungBetrag(rueckforderungFormular.getStufe2VerfuegungBetrag());
		jaxFormular.setStufe2VerfuegungDatum(rueckforderungFormular.getStufe2VerfuegungDatum());
		jaxFormular.setStufe2VerfuegungAusbezahltAm(rueckforderungFormular.getStufe2VerfuegungAusbezahltAm());
		jaxFormular.setInstitutionTyp(rueckforderungFormular.getInstitutionTyp());
		jaxFormular.setExtendedEinreichefrist(rueckforderungFormular.getExtendedEinreichefrist());
		jaxFormular.setRelevantEinreichungsfrist(rueckforderungFormular.getRelevantEinreichungsfrist());
		jaxFormular.setBetragEntgangeneElternbeitraege(rueckforderungFormular.getBetragEntgangeneElternbeitraege());
		jaxFormular.setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(rueckforderungFormular.getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten());
		jaxFormular.setAnzahlNichtAngeboteneEinheiten(rueckforderungFormular.getAnzahlNichtAngeboteneEinheiten());
		jaxFormular.setKurzarbeitBeantragt(rueckforderungFormular.getKurzarbeitBeantragt());
		jaxFormular.setKurzarbeitBetrag(rueckforderungFormular.getKurzarbeitBetrag());
		jaxFormular.setKurzarbeitDefinitivVerfuegt(rueckforderungFormular.getKurzarbeitDefinitivVerfuegt());
		jaxFormular.setKurzarbeitKeinAntragBegruendung(rueckforderungFormular.getKurzarbeitKeinAntragBegruendung());
		jaxFormular.setKurzarbeitSonstiges(rueckforderungFormular.getKurzarbeitSonstiges());
		jaxFormular.setCoronaErwerbsersatzBeantragt(rueckforderungFormular.getCoronaErwerbsersatzBeantragt());
		jaxFormular.setCoronaErwerbsersatzBetrag(rueckforderungFormular.getCoronaErwerbsersatzBetrag());
		jaxFormular.setCoronaErwerbsersatzDefinitivVerfuegt(rueckforderungFormular.getCoronaErwerbsersatzDefinitivVerfuegt());
		jaxFormular.setCoronaErwerbsersatzKeinAntragBegruendung(rueckforderungFormular.getCoronaErwerbsersatzKeinAntragBegruendung());
		jaxFormular.setCoronaErwerbsersatzSonstiges(rueckforderungFormular.getCoronaErwerbsersatzSonstiges());
		jaxFormular.setStufe2VoraussichtlicheBetrag(rueckforderungFormular.getStufe2VoraussichtlicheBetrag());
		jaxFormular.setKorrespondenzSprache(rueckforderungFormular.getKorrespondenzSprache());
		jaxFormular.setBemerkungFuerVerfuegung(rueckforderungFormular.getBemerkungFuerVerfuegung());

		jaxFormular.setRueckforderungMitteilungen(rueckforderungMitteilungenToJax(rueckforderungFormular.getRueckforderungMitteilungen(), rueckforderungFormular.getInstitutionStammdaten().getInstitution().getName()));

		return jaxFormular;

	}

	@Nonnull
	public RueckforderungFormular rueckforderungFormularToEntity(@Nonnull JaxRueckforderungFormular rueckforderungFormularJax, @Nonnull RueckforderungFormular rueckforderungFormular) {

		convertAbstractFieldsToEntity(rueckforderungFormularJax, rueckforderungFormular);

		//InstitutionStammdaten
		String instStammdatenID = rueckforderungFormularJax.getInstitutionStammdaten().getId();
		requireNonNull(instStammdatenID, "Die Institutionsstammdaten muessen gesetzt sein");
		InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.findInstitutionStammdaten(instStammdatenID)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"rueckforderungFormularToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					rueckforderungFormularJax.getInstitutionStammdaten().getId())
				);
		rueckforderungFormular.setInstitutionStammdaten(institutionStammdaten);
		rueckforderungFormular.setStatus(rueckforderungFormularJax.getStatus());
		rueckforderungFormular.setHasBeenProvisorisch(rueckforderungFormularJax.isHasBeenProvisorisch());

		rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlStunden(rueckforderungFormularJax.getStufe1KantonKostenuebernahmeAnzahlStunden());
		rueckforderungFormular.setStufe1InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormularJax.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
		rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlStunden(rueckforderungFormularJax.getStufe2KantonKostenuebernahmeAnzahlStunden());
		rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormularJax.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
		rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlTage(rueckforderungFormularJax.getStufe1KantonKostenuebernahmeAnzahlTage());
		rueckforderungFormular.setStufe1InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormularJax.getStufe1InstitutionKostenuebernahmeAnzahlTage());
		rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlTage(rueckforderungFormularJax.getStufe2KantonKostenuebernahmeAnzahlTage());
		rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormularJax.getStufe2InstitutionKostenuebernahmeAnzahlTage());
		rueckforderungFormular.setStufe1KantonKostenuebernahmeBetreuung(rueckforderungFormularJax.getStufe1KantonKostenuebernahmeBetreuung());
		rueckforderungFormular.setStufe1InstitutionKostenuebernahmeBetreuung(rueckforderungFormularJax.getStufe1InstitutionKostenuebernahmeBetreuung());
		rueckforderungFormular.setStufe2KantonKostenuebernahmeBetreuung(rueckforderungFormularJax.getStufe2KantonKostenuebernahmeBetreuung());
		rueckforderungFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormularJax.getStufe2InstitutionKostenuebernahmeBetreuung());
		rueckforderungFormular.setStufe1FreigabeBetrag(rueckforderungFormularJax.getStufe1FreigabeBetrag());
		rueckforderungFormular.setStufe1FreigabeDatum(rueckforderungFormularJax.getStufe1FreigabeDatum());
		// Stufe1FreigabeAusbezahltAm darf nie vom Client uebernommen werden, es muss Clientseitig gesetzt werden
		rueckforderungFormular.setStufe2VerfuegungBetrag(rueckforderungFormularJax.getStufe2VerfuegungBetrag());
		rueckforderungFormular.setStufe2VerfuegungDatum(rueckforderungFormularJax.getStufe2VerfuegungDatum());
		// Stufe2VerfuegungAusbezahltAm darf nie vom Client uebernommen werden, es muss Clientseitig gesetzt werden
		rueckforderungFormular.setRueckforderungMitteilungen(rueckforderungMitteilungenToEntity(rueckforderungFormularJax.getRueckforderungMitteilungen(), rueckforderungFormular.getRueckforderungMitteilungen()));
		rueckforderungFormular.setInstitutionTyp(rueckforderungFormularJax.getInstitutionTyp());
		rueckforderungFormular.setExtendedEinreichefrist(rueckforderungFormularJax.getExtendedEinreichefrist());
		rueckforderungFormular.setBetragEntgangeneElternbeitraege(rueckforderungFormularJax.getBetragEntgangeneElternbeitraege());
		rueckforderungFormular.setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(rueckforderungFormularJax.getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten());
		rueckforderungFormular.setAnzahlNichtAngeboteneEinheiten(rueckforderungFormularJax.getAnzahlNichtAngeboteneEinheiten());
		rueckforderungFormular.setKurzarbeitBeantragt(rueckforderungFormularJax.getKurzarbeitBeantragt());
		rueckforderungFormular.setKurzarbeitBetrag(rueckforderungFormularJax.getKurzarbeitBetrag());
		rueckforderungFormular.setKurzarbeitDefinitivVerfuegt(rueckforderungFormularJax.getKurzarbeitDefinitivVerfuegt());
		rueckforderungFormular.setKurzarbeitKeinAntragBegruendung(rueckforderungFormularJax.getKurzarbeitKeinAntragBegruendung());
		rueckforderungFormular.setKurzarbeitSonstiges(rueckforderungFormularJax.getKurzarbeitSonstiges());
		rueckforderungFormular.setCoronaErwerbsersatzBeantragt(rueckforderungFormularJax.getCoronaErwerbsersatzBeantragt());
		rueckforderungFormular.setCoronaErwerbsersatzBetrag(rueckforderungFormularJax.getCoronaErwerbsersatzBetrag());
		rueckforderungFormular.setCoronaErwerbsersatzDefinitivVerfuegt(rueckforderungFormularJax.getCoronaErwerbsersatzDefinitivVerfuegt());
		rueckforderungFormular.setCoronaErwerbsersatzKeinAntragBegruendung(rueckforderungFormularJax.getCoronaErwerbsersatzKeinAntragBegruendung());
		rueckforderungFormular.setCoronaErwerbsersatzSonstiges(rueckforderungFormularJax.getCoronaErwerbsersatzSonstiges());
		rueckforderungFormular.setKorrespondenzSprache(rueckforderungFormularJax.getKorrespondenzSprache());
		rueckforderungFormular.setBemerkungFuerVerfuegung(rueckforderungFormularJax.getBemerkungFuerVerfuegung());

		// OptimisticLocking: Version richtig behandeln
		return checkVersionSaveAndFlush(rueckforderungFormular, rueckforderungFormularJax.getVersion());
	}

	public List<JaxRueckforderungMitteilung> rueckforderungMitteilungenToJax(@Nonnull Set<RueckforderungMitteilung> rueckforderungMitteilungen, @Nonnull String institutionName) {
		return rueckforderungMitteilungen.stream().map(rueckforderungMitteilung -> rueckforderungMitteilungToJax(rueckforderungMitteilung,
			institutionName))
			.collect(Collectors.toList());
	}

	public JaxRueckforderungMitteilung rueckforderungMitteilungToJax(@Nonnull RueckforderungMitteilung rueckforderungMitteilung, @Nonnull String institutionName) {
		JaxRueckforderungMitteilung jaxMitteilung = new JaxRueckforderungMitteilung();
		convertAbstractFieldsToJAX(rueckforderungMitteilung, jaxMitteilung);
		jaxMitteilung.setBetreff(rueckforderungMitteilung.getBetreff());
		jaxMitteilung.setInhalt(RueckforderungMitteilung.getPATTERN().matcher(rueckforderungMitteilung.getInhalt()).replaceAll(Matcher.quoteReplacement(institutionName)));
		jaxMitteilung.setSendeDatum(rueckforderungMitteilung.getSendeDatum());
		return jaxMitteilung;
	}

	@Nonnull
	public Set<RueckforderungMitteilung> rueckforderungMitteilungenToEntity(@Nonnull List<JaxRueckforderungMitteilung> jaxRueckforderungMitteilungen, @Nonnull Set<RueckforderungMitteilung> rueckforderungMitteilungen) {
		final Set<RueckforderungMitteilung> convertedRueckforderungMitteilung = new TreeSet<>();
		for (final JaxRueckforderungMitteilung jaxRueckforderungMitteilung : jaxRueckforderungMitteilungen) {
			final RueckforderungMitteilung rueckforderungMitteilungToMergeWith = rueckforderungMitteilungen
				.stream()
				.filter(existingRueckforderungMitteilung -> existingRueckforderungMitteilung.getId().equals(jaxRueckforderungMitteilung.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(RueckforderungMitteilung::new);
			final RueckforderungMitteilung rueckforderungMitteilungToAdd =
				rueckforderungMitteilungToEntity(jaxRueckforderungMitteilung,
					rueckforderungMitteilungToMergeWith);
			final boolean added = convertedRueckforderungMitteilung.add(rueckforderungMitteilungToAdd);
			if (!added) {
				LOGGER.warn("dropped duplicate berechtigung {}", rueckforderungMitteilungToAdd);
			}
		}
		return convertedRueckforderungMitteilung;
	}

	public RueckforderungMitteilung rueckforderungMitteilungToEntity(
		@Nonnull JaxRueckforderungMitteilung jaxRueckforderungMitteilung,
		@Nonnull RueckforderungMitteilung rueckforderungMitteilung) {

		convertAbstractFieldsToEntity(jaxRueckforderungMitteilung, rueckforderungMitteilung);

		rueckforderungMitteilung.setBetreff(jaxRueckforderungMitteilung.getBetreff());
		rueckforderungMitteilung.setInhalt(jaxRueckforderungMitteilung.getInhalt());

		return rueckforderungMitteilung;
	}

	@Nonnull
	public List<JaxRueckforderungDokument> rueckforderungDokumentListToJax(@Nonnull List<RueckforderungDokument> rueckforderungDokumentList) {
		return rueckforderungDokumentList.stream()
			.map(this::rueckforderungDokumentToJax)
			.collect(Collectors.toList());
	}

	@Nonnull
	public JaxRueckforderungDokument rueckforderungDokumentToJax(@Nonnull RueckforderungDokument rueckforderungDokument) {
		JaxRueckforderungDokument jaxRueckforderungDokument =
			convertAbstractVorgaengerFieldsToJAX(rueckforderungDokument, new JaxRueckforderungDokument());
		convertFileToJax(rueckforderungDokument, jaxRueckforderungDokument);

		jaxRueckforderungDokument.setRueckforderungDokumentTyp(rueckforderungDokument.getRueckforderungDokumentTyp());
		jaxRueckforderungDokument.setTimestampUpload(rueckforderungDokument.getTimestampUpload());

		return jaxRueckforderungDokument;
	}
}
