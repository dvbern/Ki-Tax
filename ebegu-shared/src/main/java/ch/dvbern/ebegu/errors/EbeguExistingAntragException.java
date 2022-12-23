package ch.dvbern.ebegu.errors;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EbeguExistingAntragException extends EbeguException {

	private static final long serialVersionUID = 5982371652348318396L;
	private final String dossierId;
	private final String gesuchperiodeId;

	public EbeguExistingAntragException(
		@Nullable String methodName,
		@NotNull ErrorCodeEnum errorCodeEnum,
		@NotNull String dossierId,
		@NotNull String gesuchperiodeId,
		@Nullable Serializable... args) {
		super(methodName, null, errorCodeEnum, args);

		this.dossierId = dossierId;
		this.gesuchperiodeId = gesuchperiodeId;
	}

	public EbeguExistingAntragException(
		@Nullable String methodName,
		@NotNull ErrorCodeEnum errorCodeEnum,
		@NotNull String dossierId,
		@NotNull String gesuchperiodeId,
		@Nullable Throwable cause,
		@Nullable Serializable... args) {
		super(methodName, null, errorCodeEnum, cause, args);

		this.dossierId = dossierId;
		this.gesuchperiodeId = gesuchperiodeId;
	}

	public String getDossierId() {
		return dossierId;
	}

	public String getGesuchperiodeId() {
		return gesuchperiodeId;
	}

	@Override
	@Nonnull
	public ErrorCodeEnum getErrorCodeEnum() {
		//kann nicht null sein, da im constructor @NotNull
		Objects.requireNonNull(super.getErrorCodeEnum());
		return super.getErrorCodeEnum();
	}
}
