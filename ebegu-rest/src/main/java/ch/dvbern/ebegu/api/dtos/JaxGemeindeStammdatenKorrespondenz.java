package ch.dvbern.ebegu.api.dtos;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@XmlRootElement(name = "gemeindeStammdatenKorrespondenz")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JaxGemeindeStammdatenKorrespondenz extends JaxAbstractDTO {

	private static final long serialVersionUID = 1413073353878066142L;

	@NotNull
	@NonNull
	private Integer senderAddressSpacingLeft;

	@NotNull
	@NonNull
	private Integer senderAddressSpacingTop;

	@NotNull
	@NonNull
	private Integer receiverAddressSpacingLeft;

	@NotNull
	@NonNull
	private Integer receiverAddressSpacingTop;

	@Nullable
	private Integer logoWidth;

	@NotNull
	@NonNull
	private Integer logoSpacingLeft;

	@NotNull
	@NonNull
	private Integer logoSpacingTop;

	@NonNull
	public static JaxGemeindeStammdatenKorrespondenz from(@NonNull GemeindeStammdatenKorrespondenz stammdaten) {
		return new JaxGemeindeStammdatenKorrespondenz(
			stammdaten.getSenderAddressSpacingLeft(),
			stammdaten.getSenderAddressSpacingTop(),
			stammdaten.getReceiverAddressSpacingLeft(),
			stammdaten.getReceiverAddressSpacingTop(),
			stammdaten.getLogoWidth(),
			stammdaten.getLogoSpacingLeft(),
			stammdaten.getLogoSpacingTop()
		);
	}

	public void apply(@NonNull GemeindeStammdatenKorrespondenz entity) {
		entity.setSenderAddressSpacingLeft(senderAddressSpacingLeft);
		entity.setSenderAddressSpacingTop(senderAddressSpacingTop);
		entity.setReceiverAddressSpacingLeft(receiverAddressSpacingLeft);
		entity.setReceiverAddressSpacingTop(receiverAddressSpacingTop);
		entity.setLogoWidth(logoWidth);
		entity.setLogoSpacingLeft(logoSpacingLeft);
		entity.setLogoSpacingTop(logoSpacingTop);
	}
}
