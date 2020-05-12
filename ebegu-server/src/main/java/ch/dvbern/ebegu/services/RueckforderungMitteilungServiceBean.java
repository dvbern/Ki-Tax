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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(RueckforderungMitteilungService.class)
public class RueckforderungMitteilungServiceBean extends AbstractBaseService implements RueckforderungMitteilungService {

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;

	@Inject
	private MailService mailService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Persistence persistence;

	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	@Override
	public void sendMitteilung(@Nonnull RueckforderungMitteilung rueckforderungMitteilung) {
		Collection<RueckforderungFormular> formulareWithStatus =
			rueckforderungFormularService.getRueckforderungFormulareByStatus(rueckforderungMitteilung.getGesendetAnStatusList());
		send(formulareWithStatus, rueckforderungMitteilung);
	}

	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	@Override
	public void sendEinladung(@Nonnull RueckforderungMitteilung rueckforderungMitteilung) {
		ArrayList<RueckforderungStatus> statusNeu = new ArrayList<>();
		statusNeu.add(RueckforderungStatus.NEU);
		Collection<RueckforderungFormular> formulareWithStatus =
			rueckforderungFormularService.getRueckforderungFormulareByStatus(statusNeu);
		for (RueckforderungFormular formular : formulareWithStatus) {
			formular.setStatus(RueckforderungStatus.EINGELADEN);
			rueckforderungFormularService.save(formular);
		}
		send(formulareWithStatus, rueckforderungMitteilung);
	}

	private void send(Collection<RueckforderungFormular> formulareWithStatus, RueckforderungMitteilung rueckforderungMitteilung) {
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"sendMessage", "Kein Benutzer eingeloggt"));
		LocalDateTime dateNow = LocalDateTime.now();

		rueckforderungMitteilung.setAbsender(currentBenutzer);
		rueckforderungMitteilung.setSendeDatum(dateNow);
		rueckforderungMitteilung = persistence.persist(rueckforderungMitteilung);
		saveMitteilungenInFormulare(formulareWithStatus, rueckforderungMitteilung);

		Map<String, ArrayList<Institution>> uniqueEmpfaenger = makeEmpfaengerUnique(formulareWithStatus);
		Map<String, RueckforderungMitteilung> mitteilungen = prepareMitteilungen(uniqueEmpfaenger,
			rueckforderungMitteilung);

		sendMitteilungen(mitteilungen);
	}

	private void saveMitteilungenInFormulare(Collection<RueckforderungFormular> formulareWithStatus,
		RueckforderungMitteilung mitteilung) {
		for (RueckforderungFormular formular : formulareWithStatus) {
			rueckforderungFormularService.addMitteilung(formular, mitteilung);
		}
	}

	private void sendMitteilungen(Map<String, RueckforderungMitteilung> mitteilungen) {
		for (Entry<String, RueckforderungMitteilung> mitteilung : mitteilungen.entrySet()) {
			mailService.sendNotrechtGenerischeMitteilung(mitteilung.getValue(), mitteilung.getKey());
		}
	}

	private Map<String, ArrayList<Institution>> makeEmpfaengerUnique(Collection<RueckforderungFormular> formulareWithStatus) {
		HashMap<String, ArrayList<Institution>> uniqueEmpfaenger = new HashMap<>();
		for (RueckforderungFormular formular : formulareWithStatus) {
			ArrayList<Institution> instList;
			final String mail = formular.getInstitutionStammdaten().getMail();
			if (uniqueEmpfaenger.containsKey(mail)) {
				instList = uniqueEmpfaenger.get(mail);
				instList.add(formular.getInstitutionStammdaten().getInstitution());
			} else {
				instList = new ArrayList<>();
				uniqueEmpfaenger.put(mail, instList);
			}
		}
		return uniqueEmpfaenger;
	}

	private Map<String, RueckforderungMitteilung> prepareMitteilungen(Map<String, ArrayList<Institution>> uniqueEmpfaenger,
		RueckforderungMitteilung rueckforderungMitteilung) {
		final String DELIMITER = ", ";
		HashMap<String, RueckforderungMitteilung> mitteilungen = new HashMap<>();
		for (Entry<String, ArrayList<Institution>> empfaenger : uniqueEmpfaenger.entrySet()) {
			RueckforderungMitteilung mitteilung = new RueckforderungMitteilung(rueckforderungMitteilung);
			String institutionenString =
				empfaenger.getValue().stream().map(Institution::getName).collect(Collectors.joining(DELIMITER));
			mitteilung.setInhalt(mitteilung.getInhalt().replace("<INSTITUTIONEN>", institutionenString));
			mitteilungen.put(empfaenger.getKey(), mitteilung);
		}
		return mitteilungen;
	}
}
