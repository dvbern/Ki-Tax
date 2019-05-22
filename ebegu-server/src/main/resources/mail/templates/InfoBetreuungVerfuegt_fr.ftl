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
Subject: <@base64Header>${institution.name}: kiBon – Décision de prise en charge rendue</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>${institution.name}: kiBon – Décision de prise en charge rendue</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Une décision a été rendue pour l'offre de prise en charge extrafamiliale ci-après:
	</p>
	<table>
		<tbody>
		<tr>
			<td width="300">Cas:</td>
			<td width="300">${fall.getPaddedFallnummer()} ${gesuchsteller.nachname}</td>
		</tr>
		<tr>
			<td>Enfant:</td>
			<td>${kind.fullName}, ${birthday} </td>
		</tr>
		<tr>
			<td>Offre de prise en charge:</td>
			<td>${betreuung.getBetreuungsangebotTypTranslated("fr")}</td>
		</tr>
		<tr>
			<td>Institution:</td>
			<td>${institution.name}</td>
		</tr>
		<tr>
			<td>Période:</td>
			<td>${betreuung.extractGesuchsperiode().getGesuchsperiodeString()}</td>
		</tr>
		</tbody>
	</table>
	<br/>
	<p>
		Pour plus de détails, cliquez <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration
	.hostname}/gesuch/betreuungen/${betreuung.extractGesuch().id}">ici</a>.
	</p>
	<p>
		Nous vous présentons nos salutations les meilleures.<br/>
		Votre commune
	</p>
	<p>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>
