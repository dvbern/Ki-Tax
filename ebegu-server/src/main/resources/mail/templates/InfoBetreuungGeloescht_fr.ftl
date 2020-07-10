<#-- @ftlvariable name="betreuung" type="ch.dvbern.ebegu.entities.Betreuung" -->
<#-- @ftlvariable name="kind" type="ch.dvbern.ebegu.entities.Kind" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="institution" type="ch.dvbern.ebegu.entities.Institution" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="datumErstellung" type="java.lang.String" -->
<#-- @ftlvariable name="birthday" type="java.lang.String" -->
<#-- @ftlvariable name="status" type="java.lang.String" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
From: ${configuration.senderAddress}
To: <@base64Header>${institution.name}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>${institution.name}: kiBon <#if configuration.isDevmode>Système de test</#if> – Suppression de l'offre de prise en charge</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>${institution.name}: kiBon <#if configuration.isDevmode>Système de test</#if> – Suppression de l'offre de prise en charge</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		L'offre de prise en charge suivante a été supprimée:
	</p>
	<table>
		<tbody>
		<tr>
			<td width="300">Cas:</td>
			<td width="300">${fall.getPaddedFallnummer()} ${gesuchsteller.nachname}</td>
		</tr>
		<tr>
			<td>Enfant:</td>
			<td>${kind.fullName}, ${birthday}</td>
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
		<tr>
			<td>Statut de la prise en charge supprimée:</td>
			<td>${status}</td>
		</tr>
		</tbody>
	</table>
	<br/>
	<p>
		Les informations ont été saisies le ${datumErstellung}.
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre commune ${betreuung.extractGesuch().dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système ne donnent pas droit à un versement.</b><br><br>
		</#if>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>
