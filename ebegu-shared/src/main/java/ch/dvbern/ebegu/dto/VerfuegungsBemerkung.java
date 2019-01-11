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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * DTO für eine Verfügungsbemerkung
 */
public class VerfuegungsBemerkung {

	@Nonnull
	private RuleKey ruleKey;

	@Nonnull
	private MsgKey msgKey;

	@Nullable
	private Object[] args = null;

	public VerfuegungsBemerkung(@Nonnull RuleKey ruleKey, @Nonnull MsgKey msgKey) {
		this.ruleKey = ruleKey;
		this.msgKey = msgKey;
	}

	public VerfuegungsBemerkung(@Nonnull RuleKey ruleKey, @Nonnull MsgKey msgKey, @Nonnull Object... args) {
		this.ruleKey = ruleKey;
		this.msgKey = msgKey;
		this.args = args;
	}

	@Nonnull
	public RuleKey getRuleKey() {
		return ruleKey;
	}

	public void setRuleKey(@Nonnull RuleKey ruleKey) {
		this.ruleKey = ruleKey;
	}

	@Nonnull
	public MsgKey getMsgKey() {
		return msgKey;
	}

	public void setMsgKey(@Nonnull MsgKey msgKey) {
		this.msgKey = msgKey;
	}

	public String getTranslated() {
		String bemerkungsText = null;
		if (args != null) {
			bemerkungsText = ServerMessageUtil.translateEnumValue(msgKey, args);
		} else {
			bemerkungsText = ServerMessageUtil.translateEnumValue(msgKey);
		}
		return ruleKey.name() + ": " + bemerkungsText;
	}
}
