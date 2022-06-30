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

@XmlRootElement(name = "gemeindeStammdatenKorrespondenz")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JaxGemeindeStammdatenKorrespondenz extends JaxAbstractDTO {

	private static final long serialVersionUID = 1413073353878066142L;

	@NotNull
	private Integer senderAddressSpacingLeft;

	@NotNull
	private Integer senderAddressSpacingTop;

	@NotNull
	private Integer receiverAddressSpacingLeft;

	@NotNull
	private Integer receiverAddressSpacingTop;

	@NotNull
	private Integer logoWidth;

	@NotNull
	private Integer logoSpacingLeft;

	@NotNull
	private Integer logoSpacingTop;

	@NotNull
	public static JaxGemeindeStammdatenKorrespondenz from(@NotNull GemeindeStammdatenKorrespondenz stammdaten) {
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

	public void apply(@NotNull GemeindeStammdatenKorrespondenz entity) {
		entity.setSenderAddressSpacingLeft(senderAddressSpacingLeft);
		entity.setSenderAddressSpacingTop(senderAddressSpacingTop);
		entity.setReceiverAddressSpacingLeft(receiverAddressSpacingLeft);
		entity.setReceiverAddressSpacingTop(receiverAddressSpacingTop);
		entity.setLogoWidth(logoWidth);
		entity.setLogoSpacingLeft(logoSpacingLeft);
		entity.setLogoSpacingTop(logoSpacingTop);
	}
}
