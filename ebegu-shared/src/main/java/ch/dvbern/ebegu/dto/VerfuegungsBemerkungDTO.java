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

package ch.dvbern.ebegu.dto;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * DTO für eine Verfügungsbemerkung
 */
public class VerfuegungsBemerkungDTO {

	@Nonnull
	private RuleValidity ruleValidity;

	@Nonnull
	private MsgKey msgKey;

	@Nullable
	private Object[] args = null;

	@Nonnull
	private Locale sprache;

	public VerfuegungsBemerkung(@Nonnull RuleValidity ruleValidity, @Nonnull MsgKey msgKey, @Nonnull Locale sprache, @Nullable Object... args) {
		this.ruleValidity = ruleValidity;
		this.msgKey = msgKey;
		this.sprache = sprache;
		this.args = args;
	}

	@Nonnull
	public MsgKey getMsgKey() {
		return msgKey;
	}

	public void setMsgKey(@Nonnull MsgKey msgKey) {
		this.msgKey = msgKey;
	}

	@Nonnull
	public Locale getSprache() {
		return sprache;
	}

	public void setSprache(@Nonnull Locale sprache) {
		this.sprache = sprache;
	}

	@Nonnull
	public RuleValidity getRuleValidity() {
		return ruleValidity;
	}

	public void setRuleValidity(@Nonnull RuleValidity ruleValidity) {
		this.ruleValidity = ruleValidity;
	}

	public String getTranslated() {
		if (args != null) {
			return ServerMessageUtil.translateEnumValue(msgKey, sprache, args);
		} else {
			return ServerMessageUtil.translateEnumValue(msgKey, sprache);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof VerfuegungsBemerkungDTO)) {
			return false;
		}
		VerfuegungsBemerkungDTO that = (VerfuegungsBemerkungDTO) o;
		return msgKey == that.msgKey &&
			Arrays.equals(args, that.args) &&
			Objects.equals(sprache, that.sprache) &&
			this.ruleValidity == that.ruleValidity;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(ruleValidity, msgKey, sprache);
		result = 31 * result + Arrays.hashCode(args);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("VerfuegungsBemerkungDTO{");
		sb.append("msgKey=").append(msgKey);
		sb.append(", ruleValidity=").append(ruleValidity);
		sb.append(", args=").append(Arrays.toString(args));
		sb.append('}');
		return sb.toString();
	}
}
