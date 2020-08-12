<#-- @ftlvariable name="rueckforderungFormular" type="ch.dvbern.ebegu.entities.RueckforderungFormular" -->
<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Provisorische Verfügung / Coronavirus et accueil extrafamilial : décision provisoire</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Verfügung / décision</title>

</head>

<body>

<div>
	<p>
		<b>
			Ausfallentschädigung im Bereich familienergänzende Kinderbetreuung für entgangene Betreuungsbeiträge
			infolge der Massnahmen zur Bekämpfung des Coronavirus (Covid-19)
		</b>
	</p>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Wir haben Ihr Gesuch um eine Ausfallentschädigung für entgangene Betreuungsbeiträge infolge der Massnahmen zur
		Bekämpfung des Coronavirus geprüft. Ihre Verfügung finden Sie unter <a href="<#if configuration
		.clientUsingHTTPS>https://<#else>http://</#if>${configuration
		.hostname}/corona-finanzierung/list/rueckforderung/${rueckforderungFormular.id}/verfuegungen">diesem Link</a>.
	</p>
	<p>
		Bitte beachten Sie, dass die Ausfallentschädigung unter Vorbehalt einer späteren Korrektur verfügt wurde.
		Eine Verfügung unter Vorbehalt wurde erlassen, weil a) die Prüfung des Gesuchs zum jetzi-gen Zeitpunkt nicht
		abgeschlossen ist, oder weil b) Sie die definitiven Abrechnungen über die Kurzar-beitsentschädigung und/oder
		die Covid-Erwerbsausfallentschädigung noch erwarten.
	</p>
	<p>
		Falls b) zutrifft, bitten wir Sie, die Abrechnungen nach Erhalt umgehend nachzureichen via Upload auf kiBon.
		Falls a) zutrifft werden Sie wieder von uns hören, Sie müssen nichts unternehmen.
	</p>
	<p>
		Freundliche Grüsse
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales<br>
		<a href="mailto:info.fam@be.ch">info.fam@be.ch</a><br>
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
		<b>
			Indemnités pour pertes financières en faveur des institutions d’accueil extra-familial pour enfants en
			compensation des contributions de garde non perçues en raison des mesures de lutte contre le coronavirus (COVID-19)
		</b>
	</p>
	<p>
		Mesdames, Messieurs,
	</p>
	<p>
		Votre demande d’indemnités pour pertes financières nous est bien parvenue et a retenu toute notre attention.
		Nous avons le plaisir de vous faire parvenir <a href="<#if configuration
		.clientUsingHTTPS>https://<#else>http://</#if>${configuration
		.hostname}/corona-finanzierung/list/rueckforderung/${rueckforderungFormular.id}/verfuegungen">la décision</a>.
		A noter que celle-ci est prise <b>sous réserve d’une correction ultérieure</b>. Si nous avons statué de cette manière,
		c’est soit parce que l’examen de votre demande n’est pas encore achevé, soit parce que vous n’avez pas encore reçu
		les décomptes définitifs relatifs aux indemnités en cas de réduction de l’horaire de travail et / ou à l’allocation
		pour perte de gain en cas de coronavirus.
	</p>
	<p>
		Dans le premier cas, vous n’avez aucune démarche à entreprendre : nous reprendrons spontanément contact avec vous.
		Dans le second cas, nous vous prions de nous remettre les décomptes via la plateforme kiBon dès que ceux-ci
		seront en votre possession.
	</p>
	<p>
		En vous remerciant de votre engagement en cette période de pandémie, nous vous prions d’agréer,
		Mesdames, Messieurs, nos salutations distinguées.
	</p>
	<p>
		Meilleurs salutations
	</p>
	<p>
		La division Famille</p>
	<p>
		Office de l’intégration et de l’action sociale
		Courriel : <a href="mailto:info.fam@be.ch">info.fam@be.ch</a>
		Tél : 031 633 78 91
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
