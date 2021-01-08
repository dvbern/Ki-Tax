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

package ch.dvbern.ebegu.ws.ewk.sts;

import java.io.StringWriter;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.ws.tools.WSUtil;
import ch.dvbern.lib.date.converters.zoned.LocalDateTimeUTCConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility zur Authentifizierung mittels SEU BE ActiveDirectory STS (Security Token Service).
 *
 * Das Benutzertoken (SAML) kann anschliessend fuer einen Web Service Aufruf dem Security
 * Header beigefuegt werden.
 */
public final class SAMLAuthenticationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLAuthenticationUtil.class.getSimpleName());

	private static final String XML_SAML_NS = "urn:oasis:names:tc:SAML:1.0:assertion";
	private static final String ZERT_STS_NS = "http://sv.fin.be.ch/schemas/A7S/securityService/20071010/ZertSTSService";

	private static final String RENEWAL_STS_NS = "http://sv.fin.be.ch/schemas/a7s/securityService/20071010/RenewAssertionService";

	private SAMLAuthenticationUtil() {
		//util
	}

	public static STSAssertionExtractionResult extractSamlAssertionFromIssueResponse(SOAPMessage response) {

		SOAPElement responseElement = null;
		try {
			responseElement = (SOAPElement) response.getSOAPBody()
				.getChildElements(new QName(ZERT_STS_NS, "IssueAssertionResponse"))
				.next();


			return extractAssertionFromResponseElement(responseElement, ZERT_STS_NS);

		} catch (SOAPException e) {

			LOGGER.error("Could not extract the SOAP Response ");
			logSoapMessage(response);
			throw new EbeguRuntimeException("extractSamlAssertionAndRenewToken", "SOAP Message scheint keine verarbeitbare Assertion zu enthalten", e);
		}

	}

	public static STSAssertionExtractionResult extractSamlAssertionFromRenewalResponse(SOAPMessage soapMessage) {
		SOAPElement responseElement = null;
		try {
			responseElement = (SOAPElement) soapMessage.getSOAPBody()
				.getChildElements(new QName(RENEWAL_STS_NS, "RenewAssertionResponse"))
				.next();
			return extractAssertionFromResponseElement(responseElement, RENEWAL_STS_NS);
		} catch (SOAPException | NoSuchElementException e) {

			LOGGER.error("Could not extract the SOAP Response ");
			logSoapMessage(soapMessage);
			throw new EbeguRuntimeException("extractSamlAssertionAndRenewToken", "SOAP Message scheint keine verarbeitbare Assertion zu enthalten "+ e.getMessage(), e);
		}
	}

	private static STSAssertionExtractionResult extractAssertionFromResponseElement(SOAPElement responseElement, String renwalTokenNs) throws SOAPException {
		SOAPElement assertionElement = (SOAPElement) responseElement.getChildElements(
			new QName(XML_SAML_NS, "Assertion"))
			.next();

		String renewalToken = null;

			SOAPElement renewalTokenElement = (SOAPElement) responseElement
				.getChildElements(new QName(renwalTokenNs, "RenewalToken"))
				.next();
			renewalToken = renewalTokenElement.getTextContent();

		addMissingNamespaces(assertionElement);

		LocalDateTimeUTCConverter utcConverter = new LocalDateTimeUTCConverter();
		final String notBefore = getAttribute(assertionElement, "Conditions", "NotBefore");
		final String notAfter = getAttribute(assertionElement, "Conditions", "NotOnOrAfter");
		final String maxRenewal = SAMLAuthenticationUtil.findMaxRenewalTime(assertionElement);

		STSAssertionExtractionResult result = new STSAssertionExtractionResult(
			assertionElement,
			renewalToken,
			utcConverter.unmarshal(notBefore),
			utcConverter.unmarshal(notAfter),
			utcConverter.unmarshal(maxRenewal)
		);
		return result;
	}

	private static void logSoapMessage(SOAPMessage soapMessage) {
		try {
			LOGGER.error(nodeToString(soapMessage.getSOAPPart().getEnvelope()));
		} catch (SOAPException | TransformerException e) {
			LOGGER.warn("Could not create logoutput for soapEnvelop");
		}
	}



	public static String nodeToString(Node node) throws TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		return writer.toString();
	}



	private static void addMissingNamespaces(SOAPElement soapElement) throws SOAPException {
		soapElement.addNamespaceDeclaration("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		soapElement.addNamespaceDeclaration("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
	}

	private static String findMaxRenewalTime(SOAPElement assertionElem)   {

		SOAPElement attributeStatementElement = (SOAPElement) assertionElem.getChildElements(new QName(XML_SAML_NS, "AttributeStatement")).next();
		NodeList nodeList = attributeStatementElement.getElementsByTagName("Attribute");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String s = getAttribute(node, "AttributeName");
			if ("MaxRenewalTime".equals(s)) {
				Node firstChild = node.getFirstChild();
				if (firstChild != null) {
					return firstChild.getTextContent();
				}
			}
		}
		return null;
	}

	private static String getAttribute(Element element, String elementName, String attributeName) {
		NodeList nodeList = element.getElementsByTagName(elementName);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String s = getAttribute(node, attributeName);
			if (s != null) {
				return s;
			}
		}
		return null;
	}

	private static String getAttribute(Node node, String attributeName) {
		NamedNodeMap nnm = node.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Node item = nnm.item(i);
			if (attributeName.equals(item.getNodeName())) {
				return item.getNodeValue();
			}
		}
		return null;
	}

	/**
	 * util method to construct the soap message used to renew the asserton
	 * @param assertionElement assertion
	 * @param renewalToken renewal token that is still valid
	 * @return the constructed soap message
	 * @throws SOAPException if there was a problem constructing the soap message
	 */
	public static SOAPMessage createRenewalSoapMessage(SOAPElement assertionElement, String renewalToken) throws SOAPException {
		MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage soapMessage = factory.createMessage();
		SOAPPart part = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody soapBody = envelope.getBody();

		SOAPElement request = soapBody.addChildElement(new QName(RENEWAL_STS_NS, "RenewAssertion"));
		request.addChildElement("RenewalToken").setValue(renewalToken);
		request.addChildElement(assertionElement);
		WSUtil.correctAssertionNodes(request.getElementsByTagName("*"));
		return soapMessage;
	}

}
