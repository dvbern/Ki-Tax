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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.ws.tools;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSUtil {

	public static void transformAssertionHeaders(NodeList nodeList) throws SOAPException {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node.getNodeName().equals("Assertion")){
				SOAPElement assertion = (SOAPElement) node;
				assertion.addNamespaceDeclaration("saml","urn:oasis:names:tc:SAML:1.0:assertion");
				assertion.addNamespaceDeclaration("samlp","urn:oasis:names:tc:SAML:1.0:protocol");
				assertion.removeNamespaceDeclaration("ds");
			}
			if(node.getNodeName().equals("AttributeValue")){
				SOAPElement attributeValue = (SOAPElement) node;
				attributeValue.removeAttribute("xmlns");
			}
		}
	}
}
