<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="betrag1" type="java.lang.String" -->
<#-- @ftlvariable name="betrag2" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Zahlung freigegeben / Coronavirus et accueil extrafamilial : versement libéré</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung für Kitas und
		TFO: Zahlung freigegeben / Coronavirus et accueil extrafamilial : versement libéré</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Sie werden in den nächsten Tagen die folgende Zahlung erhalten:
	</p>
	<p>
		Für Stunden/ Plätze, welche nicht mehr angeboten wurden: CHF ${betrag1} <br>
		Für Elterngebühren für die Betreuung für coronabedingte Abwesenheiten: CHF ${betrag2}
	</p>
	<p>
	<p>
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">Hier</a>
		können Sie sehen, ob im Vergleich zu Ihrer Eingabe Änderungen vorgenommen wurden. Die definitive
		Abrechnung mit Begründung von Abweichungen zur Eingabe erfolgt nach der detaillierten Prüfung Ihres Gesuchs in
		der Stufe 2.
	</p>
	<p>
		<b>
			Bitte beachten Sie: Die zweite Stufe müssen Sie bis spätestens 17. Juli (privat betriebene Institutionen)
			bzw. 31. Juli 2020 (von der öffentlichen Handbetriebene Institutionen) freigeben.
		</b>
	</p>
	<p>
		Bei Rückfragen steht Ihnen die Abteilung Familie des Amtes für Integration und Soziales info.fam@be.ch zur
		Verfügung.
	</p>
	Mit freundlichen Grüssen
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales
		info.fam@be.ch
		031 633 78 91
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>

<div>
	<p>
		Mesdames, Messieurs,
	</p>
	<p>
		Le versement suivant sera effectué au cours des prochains jours :
	</p>
	<p>
    	${betrag1} francs pour les heures ou les places qui n’ont plus pu être proposées.<br>
    	${betrag2} francs de contributions parentales pour les frais de prise en charge des enfants absents en raison du coronavirus.
	</p>
	<p>
		Vous pouvez contrôler <a
				href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">ici</a>
		si les données saisies ont été modifiées. Le décompte définitif avec justification des
		écarts sera établi après examen détaillé de votre demande dans le cadre de la seconde étape.
	</p>
	<p>
		Délais pour le dépôt de la demande (étape 2) :
	</p>
	<ul>
		<li>Pour les institutions privées : le 17 juillet 2020
		</li>
		<li>Pour les institutions publiques : le 31 juillet 2020
		</li>
	</ul>
	<p>
		En restant à votre disposition en cas de question (info.fam@be.ch ; 031 633 78 91), nous vous présentons,
		Mesdames, Messieurs, nos salutations les meilleures.
	</p>
	<p>
		La division Famille de l’Office de l’intégration et de l’action sociale
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système
				ne donnent pas droit à un versement.</b><br><br>
		</p>
    </#if>
</div>

</body>

</html>
