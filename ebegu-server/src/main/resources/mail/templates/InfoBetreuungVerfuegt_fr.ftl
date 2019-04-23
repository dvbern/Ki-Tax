<#-- @ftlvariable name="betreuung" type="ch.dvbern.ebegu.entities.Betreuung" -->
<#-- @ftlvariable name="kind" type="ch.dvbern.ebegu.entities.Kind" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="institution" type="ch.dvbern.ebegu.entities.Institution" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="birthday" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
From: ${configuration.senderAddress}
To: ${institution.name} <${empfaengerMail}>
Subject: <@base64Header>${institution.name}: kiBon – FR_Betreuung verfügt</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>FR_kiBon – Betreuung verfügt</title>

</head>

<body>

<div>
	<p>
		FR_Guten Tag
	</p>
	<p>
		Die folgende familienexterne Betreuung wurde verfügt:
	</p>
	<table>
		<tbody>
		<tr>
			<td width="300">FR_Fall:</td>
			<td width="300">${fall.getPaddedFallnummer()} ${gesuchsteller.nachname}</td>
		</tr>
		<tr>
			<td>FR_Kind:</td>
			<td>${kind.fullName}, ${birthday} </td>
		</tr>
		<tr>
			<td>FR_Betreuungsangebot:</td>
			<td>${betreuung.getBetreuungsangebotTypTranslated("fr")}</td>
		</tr>
		<tr>
			<td>FR_Institution:</td>
			<td>${institution.name}</td>
		</tr>
		<tr>
			<td>FR_Periode:</td>
			<td>${betreuung.extractGesuchsperiode().getGesuchsperiodeString()}</td>
		</tr>
		</tbody>
	</table>
	<br/>
	<p>
        FR_Die Details finden Sie <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/betreuungen/${betreuung.extractGesuch().id}">hier</a>.
	</p>
	<p>
        FR_Freundliche Grüsse <br/>
        FR_Ihre Gemeinde ${betreuung.extractGesuch().dossier.gemeinde.name}
	</p>
	<p>
        FR_Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
