<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${gesuchsteller.fullName} <${empfaengerMail}>
Subject: <@base64Header>kiBon – Votre demande a été traitée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon – Votre demande a été traitée</title>

</head>

<body>

<div>
	<p>
		Chère famille,
	</p>
	<p>
		Votre demande a été examinée et le montant du bon a été calculé. Vous pouvez consulter les résultats
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/verfuegen/${gesuch.id}">ici</a>.
	</p>
	<p>
		Veuillez agréer nos salutations les meilleures,	<br/>
		Votre commune ${gesuch.dossier.gemeinde.name}
	</p>
	<p>
		Merci de ne pas répondre à ce message automatique.
	</p>

</div>

</body>

</html>
