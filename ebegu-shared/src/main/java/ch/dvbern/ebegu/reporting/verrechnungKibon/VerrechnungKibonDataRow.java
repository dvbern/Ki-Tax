/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.verrechnungKibon;

/**
 * DTO für die Verrechnung von KiBon für eine Gemeinde in einer Gesuchsperiode
 */
public class VerrechnungKibonDataRow implements Comparable<VerrechnungKibonDataRow> {

	private String gemeinde;
	private String gesuchsperiode;
	private Long kinderTotal;
	private Long kinderBereitsVerrechnet;


	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(String gemeinde) {
		this.gemeinde = gemeinde;
	}

	public String getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(String gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public Long getKinderTotal() {
		return kinderTotal;
	}

	public void setKinderTotal(Long kinderTotal) {
		this.kinderTotal = kinderTotal;
	}

	public Long getKinderBereitsVerrechnet() {
		return kinderBereitsVerrechnet;
	}

	public void setKinderBereitsVerrechnet(Long kinderBereitsVerrechnet) {
		this.kinderBereitsVerrechnet = kinderBereitsVerrechnet;
	}

	@Override
	public int compareTo(VerrechnungKibonDataRow o) {
		int result = this.getGemeinde().compareTo(o.getGemeinde());
		if (result == 0) {
			result = this.getGesuchsperiode().compareTo(o.getGesuchsperiode());
		}
		return result;
	}
}
