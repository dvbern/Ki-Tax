<#-- @ftlvariable name="betreuung" type="ch.dvbern.ebegu.entities.Betreuung" -->
<#-- @ftlvariable name="kind" type="ch.dvbern.ebegu.entities.Kind" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="institution" type="ch.dvbern.ebegu.entities.Institution" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="birthday" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: <@base64Header>${institution.name}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>${institution.name}: kiBon <#if configuration.isDevmode>Testsystem</#if> – Betreuung verfügt</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>${institution.name}: kiBon <#if configuration.isDevmode>Testsystem</#if> – Betreuung verfügt</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Der folgende Betreuungsgutschein wurde verfügt:
	</p>
	<table>
		<tbody>
		<tr>
			<td width="300">Fall:</td>
			<td width="300">${fall.getPaddedFallnummer()} ${gesuchsteller.nachname}</td>
		</tr>
		<tr>
			<td>Kind:</td>
			<td>${kind.fullName}, ${birthday} </td>
		</tr>
		<tr>
			<td>Betreuungsangebot:</td>
			<td>${betreuung.getBetreuungsangebotTypTranslated("de")}</td>
		</tr>
		<tr>
			<td>Institution:</td>
			<td>${institution.name}</td>
		</tr>
		<tr>
			<td>Periode:</td>
			<td>${betreuung.extractGesuchsperiode().getGesuchsperiodeString()}</td>
		</tr>
		</tbody>
	</table>
	<br/>
	<p>
		Die Details finden Sie <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/gesuch/betreuungen/${betreuung.extractGesuch().id}">hier</a>.
	</p>
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${betreuung.extractGesuch().dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
