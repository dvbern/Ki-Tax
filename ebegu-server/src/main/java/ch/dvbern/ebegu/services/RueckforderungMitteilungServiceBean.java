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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
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

	@Override
	public void sendMitteilung(@Nonnull RueckforderungMitteilung rueckforderungMitteilung, @Nonnull List<RueckforderungStatus> statusList) {
		Collection<RueckforderungFormular> formulareWithStatus =
			rueckforderungFormularService.getRueckforderungFormulareByStatus(statusList);
		send(formulareWithStatus, rueckforderungMitteilung, statusList);
	}

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
		send(formulareWithStatus, rueckforderungMitteilung, statusNeu);
	}

	private void send(
		@Nonnull Collection<RueckforderungFormular> formulareWithStatus,
		@Nonnull RueckforderungMitteilung rueckforderungMitteilung,
		@Nonnull List<RueckforderungStatus> statusList
	) {
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

		sendMitteilungen(mitteilungen, statusList);
	}

	private void saveMitteilungenInFormulare(
		@Nonnull Collection<RueckforderungFormular> formulareWithStatus,
		@Nonnull RueckforderungMitteilung mitteilung
	) {
		for (RueckforderungFormular formular : formulareWithStatus) {
			rueckforderungFormularService.addMitteilung(formular, mitteilung);
		}
	}

	private void sendMitteilungen(
		@Nonnull Map<String, RueckforderungMitteilung> mitteilungen,
		@Nonnull List<RueckforderungStatus> statusList
	) {
		for (Entry<String, RueckforderungMitteilung> mitteilung : mitteilungen.entrySet()) {
			mailService.sendNotrechtGenerischeMitteilung(mitteilung.getValue(), mitteilung.getKey(), statusList);
		}
	}

	@Nonnull
	private Map<String, ArrayList<Institution>> makeEmpfaengerUnique(@Nonnull Collection<RueckforderungFormular> formulareWithStatus) {
		HashMap<String, ArrayList<Institution>> uniqueEmpfaenger = new HashMap<>();
		for (RueckforderungFormular formular : formulareWithStatus) {
			final String mail = formular.getInstitutionStammdaten().getMail();
			if (!uniqueEmpfaenger.containsKey(mail)) {
				uniqueEmpfaenger.put(mail, new ArrayList<>());
			}
			uniqueEmpfaenger.get(mail).add(formular.getInstitutionStammdaten().getInstitution());
		}
		return uniqueEmpfaenger;
	}

	@Nonnull
	private Map<String, RueckforderungMitteilung> prepareMitteilungen(
		@Nonnull Map<String, ArrayList<Institution>> uniqueEmpfaenger,
		@Nonnull RueckforderungMitteilung rueckforderungMitteilung
	) {
		final String DELIMITER = ", ";
		HashMap<String, RueckforderungMitteilung> mitteilungen = new HashMap<>();
		uniqueEmpfaenger.forEach((empfaenger, institutions) -> {
			RueckforderungMitteilung mitteilung = new RueckforderungMitteilung(rueckforderungMitteilung);
			String institutionenString =
				institutions.stream().map(Institution::getName).collect(Collectors.joining(DELIMITER));
			mitteilung.setInhalt(RueckforderungMitteilung.getPATTERN().matcher(mitteilung.getInhalt()).replaceAll(Matcher.quoteReplacement(institutionenString)));
			mitteilungen.put(empfaenger, mitteilung);
		});
		return mitteilungen;
	}
}
