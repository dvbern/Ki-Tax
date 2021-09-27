<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="id" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - Compensation des charges pour les écoles à journée continue</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - Compensation des charges pour les écoles à journée continue</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Nous vous informons que le canton vous a renvoyé votre formulaire pour la compensation des charges pour les écoles à journée continue pour le traiter à nouveau.
		Vous pouvez le consulter <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/lastenausgleich-ts/${id}/angaben-gemeinde">ici</a>.
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		kiBon - Team
	</p>
	<p>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>