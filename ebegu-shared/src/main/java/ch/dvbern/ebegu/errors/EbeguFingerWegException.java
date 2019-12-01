package ch.dvbern.ebegu.errors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;

public class EbeguFingerWegException extends EbeguRuntimeException {

	private static final long serialVersionUID = -3366649279537043214L;

	public EbeguFingerWegException(@Nullable String methodName, @Nonnull ErrorCodeEnum code
	) {
		super(methodName, code);
		super.setLogLevel(KibonLogLevel.ERROR);
	}
}
