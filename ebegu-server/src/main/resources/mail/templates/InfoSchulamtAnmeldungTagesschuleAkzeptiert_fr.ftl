<#-- @ftlvariable name="betreuung" type="ch.dvbern.ebegu.entities.Betreuung" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: <@base64Header>${gesuchsteller.fullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – Inscription pour ${betreuung.kind.kindJA.fullName} acceptée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – Inscription acceptée</title>

</head>

<body>

<div>
	<p>
		Chère famille,
	</p>
	<p>
		Votre inscription pour ${betreuung.kind.kindJA.fullName} à ${betreuung.institutionStammdaten.institution.name} a été acceptée. Vous pouvez consulter la liste des offres de prise en charge <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/verfuegenView/${betreuung.extractGesuch().id}/${betreuung.betreuungNummer}/${betreuung.kind.kindNummer}">ici</a>.
		Votre demande de réduction de tarif n'a pas encore été contrôlée. Vous recevrez une nouvelle confirmation avec les tarifs définitifs dès qu'elle aura été contrôlée.
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
