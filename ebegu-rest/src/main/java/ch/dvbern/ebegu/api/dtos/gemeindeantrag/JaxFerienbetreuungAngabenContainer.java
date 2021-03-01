/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;

public class JaxFerienbetreuungAngabenContainer extends JaxAbstractDTO {

	private static final long serialVersionUID = 2042610100265666798L;

	@Nonnull
	private FerienbetreuungAngabenStatus status;

	@Nonnull
	private JaxGemeinde gemeinde;

	@Nonnull
	private JaxGesuchsperiode gesuchsperiode;

	@Nonnull
	private JaxFerienbetreuungAngaben angabenDeklaration;

	@Nullable
	private JaxFerienbetreuungAngaben angabenKorrektur;

	@Nullable
	private String internerKommentar;

	@Nonnull
	public FerienbetreuungAngabenStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull FerienbetreuungAngabenStatus status) {
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
	public JaxFerienbetreuungAngaben getAngabenDeklaration() {
		return angabenDeklaration;
	}

	public void setAngabenDeklaration(@Nonnull JaxFerienbetreuungAngaben angabenDeklaration) {
		this.angabenDeklaration = angabenDeklaration;
	}

	@Nullable
	public JaxFerienbetreuungAngaben getAngabenKorrektur() {
		return angabenKorrektur;
	}

	public void setAngabenKorrektur(@Nullable JaxFerienbetreuungAngaben angabenKorrektur) {
		this.angabenKorrektur = angabenKorrektur;
	}

	@Nullable
	public String getInternerKommentar() {
		return internerKommentar;
	}

	public void setInternerKommentar(@Nullable String internerKommentar) {
		this.internerKommentar = internerKommentar;
	}
}
