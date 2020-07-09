<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
From: ${configuration.senderAddress}
To: <@base64Header>${gesuchsteller.fullName}</@base64Header> <${gesuchsteller.mail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – Demande non confirmée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – Demande non confirmée</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Vous êtes enregistré-e sur  <a href="www.kibon.ch">www.kibon.ch</a> mais vous n'avez pas encore validé vos données.
	</p>
	<p>
		Par le présent courriel, nous souhaitons vous rappeler de clore votre demande dans le délai imparti. La confirmation des données doit nous être remise
		avant le début de la prise en charge afin que vous ne perdiez pas votre droit. Si vous ne confirmez pas vos données au moyen du formulaire ad hoc dans
		les ${anzahlTage} jours, votre requête sera automatiquement supprimée.
	</p>
	<p>
		Veuillez noter que le bon de garde est émis pour le mois suivant le dépôt de la demande, à condition que celle-ci soit assortie de tous les documents
		requis, et pour le début de la prise en charge dans le cadre de la nouvelle période.
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre commune ${gesuch.dossier.gemeinde.name}
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
