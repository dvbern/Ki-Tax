<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#assign datumGeloescht = .now>
From: ${configuration.senderAddress}
To: " ${gesuchsteller.fullName} <${gesuchsteller.mail}>
Subject: <@base64Header>kiBon – Votre demande a été supprimée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon – Votre demande a été supprimée</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Votre demande via  <a href="www.kibon.ch">www.kibon.ch</a> nous est bien parvenue mais elle n'a pas été validée et il manque la confirmation des
		données. Vous avez déjà été informé-e à ce sujet.
	</p>
	<p>
		Vos données ont été automatiquement supprimées le ${datumGeloescht?date}.
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
