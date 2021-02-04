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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;

public class JaxLastenausgleichTagesschuleAngabenGemeindeContainer extends JaxAbstractDTO {

	private static final long serialVersionUID = -1005681981708595973L;

	@NotNull @Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeStatus status;

	@NotNull @Nonnull
	private JaxGemeinde gemeinde;

	@NotNull @Nonnull
	private JaxGesuchsperiode gesuchsperiode;

	@NotNull @Nonnull
	private Boolean alleAngabenInKibonErfasst;

	@Nullable
	private String internerKommentar;

	@Nullable
	private JaxLastenausgleichTagesschuleAngabenGemeinde angabenDeklaration;

	@Nullable
	private JaxLastenausgleichTagesschuleAngabenGemeinde angabenKorrektur;

	@NotNull @Nonnull
	private Set<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> angabenInstitutionContainers = new HashSet<>();


	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.status = status;
	}

	@Nonnull
	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public Boolean getAlleAngabenInKibonErfasst() {
		return alleAngabenInKibonErfasst;
	}

	public void setAlleAngabenInKibonErfasst(@Nonnull Boolean alleAngabenInKibonErfasst) {
		this.alleAngabenInKibonErfasst = alleAngabenInKibonErfasst;
	}

	@Nullable
	public String getInternerKommentar() {
		return internerKommentar;
	}

	public void setInternerKommentar(@Nullable String internerKommentar) {
		this.internerKommentar = internerKommentar;
	}

	@Nullable
	public JaxLastenausgleichTagesschuleAngabenGemeinde getAngabenDeklaration() {
		return angabenDeklaration;
	}

	public void setAngabenDeklaration(@Nullable JaxLastenausgleichTagesschuleAngabenGemeinde angabenDeklaration) {
		this.angabenDeklaration = angabenDeklaration;
	}

	@Nullable
	public JaxLastenausgleichTagesschuleAngabenGemeinde getAngabenKorrektur() {
		return angabenKorrektur;
	}

	public void setAngabenKorrektur(@Nullable JaxLastenausgleichTagesschuleAngabenGemeinde angabenKorrektur) {
		this.angabenKorrektur = angabenKorrektur;
	}

	@Nonnull
	public Set<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> getAngabenInstitutionContainers() {
		return angabenInstitutionContainers;
	}

	public void setAngabenInstitutionContainers(@Nonnull Set<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> angabenInstitutionContainers) {
		this.angabenInstitutionContainers = angabenInstitutionContainers;
	}
}
