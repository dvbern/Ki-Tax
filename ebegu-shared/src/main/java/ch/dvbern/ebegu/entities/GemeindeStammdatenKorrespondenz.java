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

package ch.dvbern.ebegu.entities;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.ONE_MB;

@Audited
@Entity
@Table
@Getter
@Setter
public class GemeindeStammdatenKorrespondenz extends AbstractEntity {

	private static final long serialVersionUID = 4765675494503043015L;

	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private Integer senderAddressSpacingLeft;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private Integer senderAddressSpacingTop;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private Integer receiverAddressSpacingLeft;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private Integer receiverAddressSpacingTop;

	@Nullable
	@Column(nullable = true, length = ONE_MB) // 1 megabytes
	@Lob
	private byte[] logoContent;

	@Nullable
	@Column(nullable = true)
	private String logoName;

	@Nullable
	@Column(nullable = true)
	private String logoType;

	@Nullable
	@Column(nullable = true)
	private Integer logoWidth;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private Integer logoSpacingLeft;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private Integer logoSpacingTop;

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof GemeindeStammdatenKorrespondenz)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		GemeindeStammdatenKorrespondenz gemeindeStammdaten = (GemeindeStammdatenKorrespondenz) other;
		return Objects.equals(this.getId(), gemeindeStammdaten.getId());
	}

	@Nonnull
	public byte[] getLogoContent() {
		if (logoContent == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return Arrays.copyOf(logoContent, logoContent.length);
	}

	public void setLogoContent(@Nullable byte[] logoContent) {
		if (logoContent == null) {
			//noinspection ConstantConditions
			this.logoContent = null;
		} else {
			this.logoContent = Arrays.copyOf(logoContent, logoContent.length);
		}
	}
}
