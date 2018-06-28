
    create table abwesenheit (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        primary key (id)
    );

    create table abwesenheit_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        primary key (id, rev)
    );

    create table abwesenheit_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        abwesenheitgs_id varchar(36),
        abwesenheitja_id varchar(36),
        betreuung_id varchar(36),
        primary key (id, rev)
    );

    create table abwesenheit_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        abwesenheitgs_id varchar(36),
        abwesenheitja_id varchar(36),
        betreuung_id varchar(36) not null,
        primary key (id)
    );

    create table adresse (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        gemeinde varchar(255),
        hausnummer varchar(100),
        land varchar(255) not null,
        organisation varchar(255),
        ort varchar(255) not null,
        plz varchar(100) not null,
        strasse varchar(255) not null,
        zusatzzeile varchar(255),
        primary key (id)
    );

    create table adresse_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        gemeinde varchar(255),
        hausnummer varchar(100),
        land varchar(255),
        organisation varchar(255),
        ort varchar(255),
        plz varchar(100),
        strasse varchar(255),
        zusatzzeile varchar(255),
        primary key (id, rev)
    );

    create table antrag_status_history_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        status varchar(255),
        timestamp_bis DATETIME(6),
        timestamp_von DATETIME(6),
        benutzer_id varchar(36),
        gesuch_id varchar(36),
        primary key (id, rev)
    );

    create table antrag_status_history (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        status varchar(255) not null,
        timestamp_bis DATETIME(6),
        timestamp_von DATETIME(6) not null,
        benutzer_id varchar(36) not null,
        gesuch_id varchar(36) not null,
        primary key (id)
    );

    create table application_property_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        name varchar(255),
        value varchar(4000),
        primary key (id, rev)
    );

    create table application_property (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        name varchar(255) not null,
        value varchar(4000) not null,
        primary key (id)
    );

    create table authorisierter_benutzer (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        auth_token varchar(255),
        first_login datetime not null,
        last_login datetime not null,
        role varchar(255) not null,
        samlidpentityid varchar(255),
        saml_name_id varchar(255),
        samlspentityid varchar(255),
        session_index varchar(255),
        username varchar(255) not null,
        benutzer_id varchar(36) not null,
        primary key (id)
    );

    create table belegung_ferieninsel_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        ferienname varchar(255),
        primary key (id, rev)
    );

    create table belegung_ferieninsel_belegung_ferieninsel_tag_aud (
        rev integer not null,
        belegung_ferieninsel_id varchar(36) not null,
        tage_id varchar(36) not null,
        revtype tinyint,
        primary key (rev, belegung_ferieninsel_id, tage_id)
    );

    create table belegung_ferieninsel_tag_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        tag date,
        primary key (id, rev)
    );

    create table belegung_tagesschule_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        eintrittsdatum date,
        primary key (id, rev)
    );

    create table belegung_tagesschule_modul_tagesschule_aud (
        rev integer not null,
        belegung_tagesschule_id varchar(36) not null,
        module_tagesschule_id varchar(36) not null,
        revtype tinyint,
        primary key (rev, belegung_tagesschule_id, module_tagesschule_id)
    );

    create table belegung_ferieninsel (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        ferienname varchar(255) not null,
        primary key (id)
    );

    create table belegung_ferieninsel_belegung_ferieninsel_tag (
        belegung_ferieninsel_id varchar(36) not null,
        tage_id varchar(36) not null
    );

    create table belegung_ferieninsel_tag (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        tag date not null,
        primary key (id)
    );

    create table belegung_tagesschule (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        eintrittsdatum date not null,
        primary key (id)
    );

    create table belegung_tagesschule_modul_tagesschule (
        belegung_tagesschule_id varchar(36) not null,
        module_tagesschule_id varchar(36) not null,
        primary key (belegung_tagesschule_id, module_tagesschule_id)
    );

    create table benutzer (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        email varchar(255) not null,
        gesperrt bit not null,
        nachname varchar(255) not null,
        username varchar(255) not null,
        vorname varchar(255) not null,
        mandant_id varchar(36) not null,
        primary key (id)
    );

    create table benutzer_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        email varchar(255),
        gesperrt bit,
        nachname varchar(255),
        username varchar(255),
        vorname varchar(255),
        mandant_id varchar(36),
        primary key (id, rev)
    );

    create table berechtigung (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        role varchar(255) not null,
        benutzer_id varchar(36) not null,
        institution_id varchar(36),
        traegerschaft_id varchar(36),
        primary key (id)
    );

    create table berechtigung_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        role varchar(255),
        benutzer_id varchar(36),
        institution_id varchar(36),
        traegerschaft_id varchar(36),
        primary key (id, rev)
    );

    create table berechtigung_history_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        geloescht bit,
        gesperrt bit,
        role varchar(255),
        username varchar(255),
        institution_id varchar(36),
        traegerschaft_id varchar(36),
        primary key (id, rev)
    );

    create table berechtigung_history (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        geloescht bit not null,
        gesperrt bit not null,
        role varchar(255) not null,
        username varchar(255),
        institution_id varchar(36),
        traegerschaft_id varchar(36),
        primary key (id)
    );

    create table betreuung (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        abwesenheit_mutiert bit,
        anmeldung_mutation_zustand varchar(255),
        betreuung_mutiert bit,
        betreuung_nummer integer not null,
        betreuungsstatus varchar(255) not null,
        datum_ablehnung date,
        datum_bestaetigung date,
        erweiterte_beduerfnisse bit not null,
        grund_ablehnung varchar(4000),
        gueltig bit not null,
        keine_detailinformationen bit not null,
        vertrag bit not null,
        belegung_ferieninsel_id varchar(36),
        belegung_tagesschule_id varchar(36),
        institution_stammdaten_id varchar(36) not null,
        kind_id varchar(36) not null,
        verfuegung_id varchar(36),
        primary key (id)
    );

    create table betreuung_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        abwesenheit_mutiert bit,
        anmeldung_mutation_zustand varchar(255),
        betreuung_mutiert bit,
        betreuung_nummer integer,
        betreuungsstatus varchar(255),
        datum_ablehnung date,
        datum_bestaetigung date,
        erweiterte_beduerfnisse bit,
        grund_ablehnung varchar(4000),
        gueltig bit,
        keine_detailinformationen bit,
        vertrag bit,
        belegung_ferieninsel_id varchar(36),
        belegung_tagesschule_id varchar(36),
        institution_stammdaten_id varchar(36),
        kind_id varchar(36),
        verfuegung_id varchar(36),
        primary key (id, rev)
    );

    create table betreuungsmitteilung_pensum_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        pensum integer,
        betreuungsmitteilung_id varchar(36),
        primary key (id, rev)
    );

    create table betreuungsmitteilung_pensum (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        pensum integer not null,
        betreuungsmitteilung_id varchar(36) not null,
        primary key (id)
    );

    create table betreuungspensum (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        pensum integer not null,
        nicht_eingetreten bit not null,
        primary key (id)
    );

    create table betreuungspensum_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        pensum integer,
        nicht_eingetreten bit,
        primary key (id, rev)
    );

    create table betreuungspensum_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        betreuung_id varchar(36),
        betreuungspensumgs_id varchar(36),
        betreuungspensumja_id varchar(36),
        primary key (id, rev)
    );

    create table betreuungspensum_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        betreuung_id varchar(36) not null,
        betreuungspensumgs_id varchar(36),
        betreuungspensumja_id varchar(36),
        primary key (id)
    );

    create table dokument (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        filename varchar(255) not null,
        filepfad varchar(255) not null,
        filesize varchar(255) not null,
        timestamp_upload datetime not null,
        dokument_grund_id varchar(36) not null,
        primary key (id)
    );

    create table dokument_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        filename varchar(255),
        filepfad varchar(255),
        filesize varchar(255),
        timestamp_upload datetime,
        dokument_grund_id varchar(36),
        primary key (id, rev)
    );

    create table dokument_grund_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        dokument_grund_typ varchar(255),
        dokument_typ varchar(255),
        full_name varchar(255),
        person_number integer,
        person_type varchar(255),
        tag varchar(255),
        gesuch_id varchar(36),
        primary key (id, rev)
    );

    create table dokument_grund (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        dokument_grund_typ varchar(255),
        dokument_typ varchar(255),
        full_name varchar(255),
        person_number integer,
        person_type varchar(255),
        tag varchar(255),
        gesuch_id varchar(36) not null,
        primary key (id)
    );

    create table dossier (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        dossier_nummer bigint not null,
        fall_id varchar(36) not null,
        gemeinde_id varchar(36) not null,
        verantwortlicherbg_id varchar(36),
        verantwortlicherts_id varchar(36),
        primary key (id)
    );

    create table dossier_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        dossier_nummer bigint,
        fall_id varchar(36),
        gemeinde_id varchar(36),
        verantwortlicherbg_id varchar(36),
        verantwortlicherts_id varchar(36),
        primary key (id, rev)
    );

    create table download_file (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        filename varchar(255) not null,
        filepfad varchar(255) not null,
        filesize varchar(255) not null,
        access_token varchar(36) not null,
        ip varchar(45) not null,
        lifespan varchar(255) not null,
        primary key (id)
    );

    create table ebegu_parameter_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        name varchar(255),
        value varchar(255),
        primary key (id, rev)
    );

    create table ebegu_vorlage_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        name varchar(255),
        pro_gesuchsperiode bit,
        vorlage_id varchar(36),
        primary key (id, rev)
    );

    create table ebegu_parameter (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        name varchar(255) not null,
        value varchar(255) not null,
        primary key (id)
    );

    create table ebegu_vorlage (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        name varchar(255) not null,
        pro_gesuchsperiode bit not null,
        vorlage_id varchar(36),
        primary key (id)
    );

    create table einkommensverschlechterung (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        bruttovermoegen decimal(19,2),
        erhaltene_alimente decimal(19,2),
        ersatzeinkommen decimal(19,2),
        familienzulage decimal(19,2),
        geleistete_alimente decimal(19,2),
        geschaeftsgewinn_basisjahr decimal(19,2),
        schulden decimal(19,2),
        steuererklaerung_ausgefuellt bit not null,
        steuerveranlagung_erhalten bit not null,
        geschaeftsgewinn_basisjahr_minus1 decimal(19,2),
        nettolohn_apr decimal(19,2),
        nettolohn_aug decimal(19,2),
        nettolohn_dez decimal(19,2),
        nettolohn_feb decimal(19,2),
        nettolohn_jan decimal(19,2),
        nettolohn_jul decimal(19,2),
        nettolohn_jun decimal(19,2),
        nettolohn_mai decimal(19,2),
        nettolohn_mrz decimal(19,2),
        nettolohn_nov decimal(19,2),
        nettolohn_okt decimal(19,2),
        nettolohn_sep decimal(19,2),
        nettolohn_zus decimal(19,2),
        primary key (id)
    );

    create table einkommensverschlechterung_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        bruttovermoegen decimal(19,2),
        erhaltene_alimente decimal(19,2),
        ersatzeinkommen decimal(19,2),
        familienzulage decimal(19,2),
        geleistete_alimente decimal(19,2),
        geschaeftsgewinn_basisjahr decimal(19,2),
        schulden decimal(19,2),
        steuererklaerung_ausgefuellt bit,
        steuerveranlagung_erhalten bit,
        geschaeftsgewinn_basisjahr_minus1 decimal(19,2),
        nettolohn_apr decimal(19,2),
        nettolohn_aug decimal(19,2),
        nettolohn_dez decimal(19,2),
        nettolohn_feb decimal(19,2),
        nettolohn_jan decimal(19,2),
        nettolohn_jul decimal(19,2),
        nettolohn_jun decimal(19,2),
        nettolohn_mai decimal(19,2),
        nettolohn_mrz decimal(19,2),
        nettolohn_nov decimal(19,2),
        nettolohn_okt decimal(19,2),
        nettolohn_sep decimal(19,2),
        nettolohn_zus decimal(19,2),
        primary key (id, rev)
    );

    create table einkommensverschlechterung_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        ekvgsbasis_jahr_plus1_id varchar(36),
        ekvgsbasis_jahr_plus2_id varchar(36),
        ekvjabasis_jahr_plus1_id varchar(36),
        ekvjabasis_jahr_plus2_id varchar(36),
        gesuchsteller_container_id varchar(36),
        primary key (id, rev)
    );

    create table einkommensverschlechterung_info_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        einkommensverschlechterung bit,
        ekv_basis_jahr_plus1annulliert bit,
        ekv_basis_jahr_plus2annulliert bit,
        ekv_fuer_basis_jahr_plus1 bit,
        ekv_fuer_basis_jahr_plus2 bit,
        gemeinsame_steuererklaerung_bjp1 bit,
        gemeinsame_steuererklaerung_bjp2 bit,
        grund_fuer_basis_jahr_plus1 varchar(255),
        grund_fuer_basis_jahr_plus2 varchar(255),
        stichtag_fuer_basis_jahr_plus1 date,
        stichtag_fuer_basis_jahr_plus2 date,
        primary key (id, rev)
    );

    create table einkommensverschlechterung_info_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        einkommensverschlechterung_infogs_id varchar(36),
        einkommensverschlechterung_infoja_id varchar(36),
        primary key (id, rev)
    );

    create table einkommensverschlechterung_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        ekvgsbasis_jahr_plus1_id varchar(36),
        ekvgsbasis_jahr_plus2_id varchar(36),
        ekvjabasis_jahr_plus1_id varchar(36),
        ekvjabasis_jahr_plus2_id varchar(36),
        gesuchsteller_container_id varchar(36) not null,
        primary key (id)
    );

    create table einkommensverschlechterung_info (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        einkommensverschlechterung bit not null,
        ekv_basis_jahr_plus1annulliert bit not null,
        ekv_basis_jahr_plus2annulliert bit not null,
        ekv_fuer_basis_jahr_plus1 bit not null,
        ekv_fuer_basis_jahr_plus2 bit not null,
        gemeinsame_steuererklaerung_bjp1 bit,
        gemeinsame_steuererklaerung_bjp2 bit,
        grund_fuer_basis_jahr_plus1 varchar(255),
        grund_fuer_basis_jahr_plus2 varchar(255),
        stichtag_fuer_basis_jahr_plus1 date,
        stichtag_fuer_basis_jahr_plus2 date,
        primary key (id)
    );

    create table einkommensverschlechterung_info_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        einkommensverschlechterung_infogs_id varchar(36),
        einkommensverschlechterung_infoja_id varchar(36),
        primary key (id)
    );

    create table erwerbspensum (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        pensum integer not null,
        bezeichnung varchar(255),
        taetigkeit varchar(255) not null,
        zuschlag_zu_erwerbspensum bit not null,
        zuschlagsgrund varchar(255),
        zuschlagsprozent integer,
        primary key (id)
    );

    create table erwerbspensum_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        pensum integer,
        bezeichnung varchar(255),
        taetigkeit varchar(255),
        zuschlag_zu_erwerbspensum bit,
        zuschlagsgrund varchar(255),
        zuschlagsprozent integer,
        primary key (id, rev)
    );

    create table erwerbspensum_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        erwerbspensumgs_id varchar(36),
        erwerbspensumja_id varchar(36),
        gesuchsteller_container_id varchar(36),
        primary key (id, rev)
    );

    create table erwerbspensum_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        erwerbspensumgs_id varchar(36),
        erwerbspensumja_id varchar(36),
        gesuchsteller_container_id varchar(36) not null,
        primary key (id)
    );

    create table fachstelle (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        behinderungsbestaetigung bit not null,
        beschreibung varchar(255),
        name varchar(100) not null,
        primary key (id)
    );

    create table fachstelle_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        behinderungsbestaetigung bit,
        beschreibung varchar(255),
        name varchar(100),
        primary key (id, rev)
    );

    create table fall (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        fall_nummer bigint not null,
        next_number_dossier integer not null,
        next_number_kind integer not null,
        besitzer_id varchar(36),
        mandant_id varchar(36) not null,
        primary key (id)
    );

    create table fall_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        fall_nummer bigint,
        next_number_dossier integer,
        next_number_kind integer,
        besitzer_id varchar(36),
        mandant_id varchar(36),
        primary key (id, rev)
    );

    create table familiensituation (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        aenderung_per date,
        familienstatus varchar(255) not null,
        gemeinsame_steuererklaerung bit,
        gesuchsteller_kardinalitaet varchar(255),
        sozialhilfe_bezueger bit,
        verguenstigung_gewuenscht bit,
        primary key (id)
    );

    create table familiensituation_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        aenderung_per date,
        familienstatus varchar(255),
        gemeinsame_steuererklaerung bit,
        gesuchsteller_kardinalitaet varchar(255),
        sozialhilfe_bezueger bit,
        verguenstigung_gewuenscht bit,
        primary key (id, rev)
    );

    create table familiensituation_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        familiensituation_erstgesuch_id varchar(36),
        familiensituationgs_id varchar(36),
        familiensituationja_id varchar(36),
        primary key (id, rev)
    );

    create table familiensituation_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        familiensituation_erstgesuch_id varchar(36),
        familiensituationgs_id varchar(36),
        familiensituationja_id varchar(36),
        primary key (id)
    );

    create table ferieninsel_stammdaten_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        anmeldeschluss date,
        ferienname varchar(255),
        gesuchsperiode_id varchar(36),
        primary key (id, rev)
    );

    create table ferieninsel_stammdaten_ferieninsel_zeitraum_aud (
        rev integer not null,
        ferieninsel_stammdaten_id varchar(36) not null,
        zeitraum_list_id varchar(36) not null,
        revtype tinyint,
        primary key (rev, ferieninsel_stammdaten_id, zeitraum_list_id)
    );

    create table ferieninsel_zeitraum_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        primary key (id, rev)
    );

    create table ferieninsel_stammdaten (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        anmeldeschluss date not null,
        ferienname varchar(255) not null,
        gesuchsperiode_id varchar(36) not null,
        primary key (id)
    );

    create table ferieninsel_stammdaten_ferieninsel_zeitraum (
        ferieninsel_stammdaten_id varchar(36) not null,
        zeitraum_list_id varchar(36) not null
    );

    create table ferieninsel_zeitraum (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        primary key (id)
    );

    create table finanzielle_situation_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        bruttovermoegen decimal(19,2),
        erhaltene_alimente decimal(19,2),
        ersatzeinkommen decimal(19,2),
        familienzulage decimal(19,2),
        geleistete_alimente decimal(19,2),
        geschaeftsgewinn_basisjahr decimal(19,2),
        schulden decimal(19,2),
        steuererklaerung_ausgefuellt bit,
        steuerveranlagung_erhalten bit,
        geschaeftsgewinn_basisjahr_minus1 decimal(19,2),
        geschaeftsgewinn_basisjahr_minus2 decimal(19,2),
        nettolohn decimal(19,2),
        primary key (id, rev)
    );

    create table finanzielle_situation_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        jahr integer,
        finanzielle_situationgs_id varchar(36),
        finanzielle_situationja_id varchar(36),
        gesuchsteller_container_id varchar(36),
        primary key (id, rev)
    );

    create table finanzielle_situation (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        bruttovermoegen decimal(19,2),
        erhaltene_alimente decimal(19,2),
        ersatzeinkommen decimal(19,2),
        familienzulage decimal(19,2),
        geleistete_alimente decimal(19,2),
        geschaeftsgewinn_basisjahr decimal(19,2),
        schulden decimal(19,2),
        steuererklaerung_ausgefuellt bit not null,
        steuerveranlagung_erhalten bit not null,
        geschaeftsgewinn_basisjahr_minus1 decimal(19,2),
        geschaeftsgewinn_basisjahr_minus2 decimal(19,2),
        nettolohn decimal(19,2),
        primary key (id)
    );

    create table finanzielle_situation_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        jahr integer not null,
        finanzielle_situationgs_id varchar(36),
        finanzielle_situationja_id varchar(36),
        gesuchsteller_container_id varchar(36) not null,
        primary key (id)
    );

    create table gemeinde (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        enabled bit not null,
        name varchar(255) not null,
        primary key (id)
    );

    create table gemeinde_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        enabled bit,
        name varchar(255),
        primary key (id, rev)
    );

    create table generated_dokument_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        filename varchar(255),
        filepfad varchar(255),
        filesize varchar(255),
        typ varchar(255),
        write_protected bit,
        gesuch_id varchar(36),
        primary key (id, rev)
    );

    create table generated_dokument (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        filename varchar(255) not null,
        filepfad varchar(255) not null,
        filesize varchar(255) not null,
        typ varchar(255) not null,
        write_protected bit not null,
        gesuch_id varchar(36) not null,
        primary key (id)
    );

    create table gesuch (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        bemerkungen varchar(4000),
        bemerkungen_pruefungstv varchar(4000),
        bemerkungenstv varchar(4000),
        datum_gewarnt_fehlende_quittung date,
        datum_gewarnt_nicht_freigegeben date,
        dokumente_hochgeladen bit not null,
        eingangsart varchar(255) not null,
        eingangsdatum date,
        eingangsdatumstv date,
        fin_sit_status varchar(255),
        freigabe_datum date,
        geprueftstv bit not null,
        gesperrt_wegen_beschwerde bit not null,
        gesuch_betreuungen_status varchar(255) not null,
        gueltig bit,
        hasfsdokument bit not null,
        laufnummer integer not null,
        status varchar(255) not null,
        timestamp_verfuegt datetime,
        typ varchar(255) not null,
        dossier_id varchar(36) not null,
        einkommensverschlechterung_info_container_id varchar(36),
        familiensituation_container_id varchar(36),
        gesuchsperiode_id varchar(36) not null,
        gesuchsteller1_id varchar(36),
        gesuchsteller2_id varchar(36),
        primary key (id)
    );

    create table gesuch_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        bemerkungen varchar(4000),
        bemerkungen_pruefungstv varchar(4000),
        bemerkungenstv varchar(4000),
        datum_gewarnt_fehlende_quittung date,
        datum_gewarnt_nicht_freigegeben date,
        dokumente_hochgeladen bit,
        eingangsart varchar(255),
        eingangsdatum date,
        eingangsdatumstv date,
        fin_sit_status varchar(255),
        freigabe_datum date,
        geprueftstv bit,
        gesperrt_wegen_beschwerde bit,
        gesuch_betreuungen_status varchar(255),
        gueltig bit,
        hasfsdokument bit,
        laufnummer integer,
        status varchar(255),
        timestamp_verfuegt datetime,
        typ varchar(255),
        dossier_id varchar(36),
        einkommensverschlechterung_info_container_id varchar(36),
        familiensituation_container_id varchar(36),
        gesuchsperiode_id varchar(36),
        gesuchsteller1_id varchar(36),
        gesuchsteller2_id varchar(36),
        primary key (id, rev)
    );

    create table gesuch_deletion_log (
        id varchar(36) not null,
        version bigint not null,
        cause varchar(255) not null,
        fall_nummer bigint not null,
        geburtsdatum date,
        gesuch_id varchar(36) not null,
        nachname varchar(255),
        timestamp_deleted datetime not null,
        user_deleted varchar(36) not null,
        vorname varchar(255),
        primary key (id)
    );

    create table gesuchsperiode (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        datum_aktiviert date,
        datum_erster_schultag date,
        datum_freischaltung_tagesschule date,
        status varchar(255) not null,
        primary key (id)
    );

    create table gesuchsperiode_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        datum_aktiviert date,
        datum_erster_schultag date,
        datum_freischaltung_tagesschule date,
        status varchar(255),
        primary key (id, rev)
    );

    create table gesuchsteller (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        geburtsdatum date not null,
        geschlecht varchar(255) not null,
        nachname varchar(255) not null,
        vorname varchar(255) not null,
        diplomatenstatus bit not null,
        ewk_abfrage_datum date,
        ewk_person_id varchar(255),
        mail varchar(255) not null,
        mobile varchar(255),
        telefon varchar(255),
        telefon_ausland varchar(255),
        primary key (id)
    );

    create table gesuchsteller_adresse_aud (
        id varchar(36) not null,
        rev integer not null,
        adresse_typ varchar(255),
        nicht_in_gemeinde bit,
        primary key (id, rev)
    );

    create table gesuchsteller_adresse_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gesuchsteller_adressegs_id varchar(36),
        gesuchsteller_adresseja_id varchar(36),
        gesuchsteller_container_id varchar(36),
        primary key (id, rev)
    );

    create table gesuchsteller_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        geburtsdatum date,
        geschlecht varchar(255),
        nachname varchar(255),
        vorname varchar(255),
        diplomatenstatus bit,
        ewk_abfrage_datum date,
        ewk_person_id varchar(255),
        mail varchar(255),
        mobile varchar(255),
        telefon varchar(255),
        telefon_ausland varchar(255),
        primary key (id, rev)
    );

    create table gesuchsteller_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gesuchstellergs_id varchar(36),
        gesuchstellerja_id varchar(36),
        primary key (id, rev)
    );

    create table gesuchsteller_adresse (
        adresse_typ varchar(255),
        nicht_in_gemeinde bit not null,
        id varchar(36) not null,
        primary key (id)
    );

    create table gesuchsteller_adresse_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gesuchsteller_adressegs_id varchar(36),
        gesuchsteller_adresseja_id varchar(36),
        gesuchsteller_container_id varchar(36) not null,
        primary key (id)
    );

    create table gesuchsteller_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gesuchstellergs_id varchar(36),
        gesuchstellerja_id varchar(36),
        primary key (id)
    );

    create table institution (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        active bit not null,
        mail varchar(255) not null,
        name varchar(255) not null,
        mandant_id varchar(36) not null,
        traegerschaft_id varchar(36),
        primary key (id)
    );

    create table institution_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        active bit,
        mail varchar(255),
        name varchar(255),
        mandant_id varchar(36),
        traegerschaft_id varchar(36),
        primary key (id, rev)
    );

    create table institution_stammdaten_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        betreuungsangebot_typ varchar(255),
        iban varchar(34),
        kontoinhaber varchar(255),
        oeffnungsstunden decimal(19,2),
        oeffnungstage decimal(19,2),
        adresse_id varchar(36),
        adresse_kontoinhaber_id varchar(36),
        institution_id varchar(36),
        institution_stammdaten_ferieninsel_id varchar(36),
        institution_stammdaten_tagesschule_id varchar(36),
        primary key (id, rev)
    );

    create table institution_stammdaten_ferieninsel_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        ausweichstandort_fruehlingsferien varchar(255),
        ausweichstandort_herbstferien varchar(255),
        ausweichstandort_sommerferien varchar(255),
        ausweichstandort_sportferien varchar(255),
        primary key (id, rev)
    );

    create table institution_stammdaten_tagesschule_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        primary key (id, rev)
    );

    create table institution_stammdaten (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        betreuungsangebot_typ varchar(255) not null,
        iban varchar(34),
        kontoinhaber varchar(255),
        oeffnungsstunden decimal(19,2),
        oeffnungstage decimal(19,2),
        adresse_id varchar(36) not null,
        adresse_kontoinhaber_id varchar(36),
        institution_id varchar(36) not null,
        institution_stammdaten_ferieninsel_id varchar(36),
        institution_stammdaten_tagesschule_id varchar(36),
        primary key (id)
    );

    create table institution_stammdaten_ferieninsel (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        ausweichstandort_fruehlingsferien varchar(255),
        ausweichstandort_herbstferien varchar(255),
        ausweichstandort_sommerferien varchar(255),
        ausweichstandort_sportferien varchar(255),
        primary key (id)
    );

    create table institution_stammdaten_tagesschule (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        primary key (id)
    );

    create table kind (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        geburtsdatum date not null,
        geschlecht varchar(255) not null,
        nachname varchar(255) not null,
        vorname varchar(255) not null,
        einschulung bit,
        familien_ergaenzende_betreuung bit not null,
        kinderabzug varchar(255) not null,
        muttersprache_deutsch bit,
        wohnhaft_im_gleichen_haushalt integer,
        pensum_fachstelle_id varchar(36),
        primary key (id)
    );

    create table kind_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        geburtsdatum date,
        geschlecht varchar(255),
        nachname varchar(255),
        vorname varchar(255),
        einschulung bit,
        familien_ergaenzende_betreuung bit,
        kinderabzug varchar(255),
        muttersprache_deutsch bit,
        wohnhaft_im_gleichen_haushalt integer,
        pensum_fachstelle_id varchar(36),
        primary key (id, rev)
    );

    create table kind_container_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        kind_mutiert bit,
        kind_nummer integer,
        next_number_betreuung integer,
        gesuch_id varchar(36),
        kindgs_id varchar(36),
        kindja_id varchar(36),
        primary key (id, rev)
    );

    create table kind_container (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        kind_mutiert bit,
        kind_nummer integer not null,
        next_number_betreuung integer not null,
        gesuch_id varchar(36) not null,
        kindgs_id varchar(36),
        kindja_id varchar(36),
        primary key (id)
    );

    create table mahnung (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        abgelaufen bit not null,
        bemerkungen varchar(4000) not null,
        datum_fristablauf date not null,
        mahnung_typ varchar(255) not null,
        timestamp_abgeschlossen DATETIME(6),
        gesuch_id varchar(36) not null,
        primary key (id)
    );

    create table mahnung_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        abgelaufen bit,
        bemerkungen varchar(4000),
        datum_fristablauf date,
        mahnung_typ varchar(255),
        timestamp_abgeschlossen DATETIME(6),
        gesuch_id varchar(36),
        primary key (id, rev)
    );

    create table mandant (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        name varchar(255) not null,
        primary key (id)
    );

    create table mandant_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        name varchar(255),
        primary key (id, rev)
    );

    create table mitteilung (
        dtype varchar(31) not null,
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        empfaenger_typ varchar(255) not null,
        message varchar(255),
        mitteilung_status varchar(255) not null,
        sender_typ varchar(255) not null,
        sent_datum datetime,
        subject varchar(255),
        applied bit,
        betreuung_id varchar(36),
        dossier_id varchar(36) not null,
        empfaenger_id varchar(36),
        sender_id varchar(36) not null,
        primary key (id)
    );

    create table mitteilung_aud (
        id varchar(36) not null,
        rev integer not null,
        dtype varchar(31) not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        empfaenger_typ varchar(255),
        message varchar(255),
        mitteilung_status varchar(255),
        sender_typ varchar(255),
        sent_datum datetime,
        subject varchar(255),
        betreuung_id varchar(36),
        dossier_id varchar(36),
        empfaenger_id varchar(36),
        sender_id varchar(36),
        applied bit,
        primary key (id, rev)
    );

    create table modul_tagesschule_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        modul_tagesschule_name varchar(255),
        wochentag varchar(255),
        zeit_bis time,
        zeit_von time,
        institution_stammdaten_tagesschule_id varchar(36),
        primary key (id, rev)
    );

    create table modul_tagesschule (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        modul_tagesschule_name varchar(255) not null,
        wochentag varchar(255) not null,
        zeit_bis time not null,
        zeit_von time not null,
        institution_stammdaten_tagesschule_id varchar(36) not null,
        primary key (id)
    );

    create table pain001dokument (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        filename varchar(255) not null,
        filepfad varchar(255) not null,
        filesize varchar(255) not null,
        typ varchar(255) not null,
        write_protected bit not null,
        zahlungsauftrag_id varchar(36) not null,
        primary key (id)
    );

    create table pain001dokument_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        filename varchar(255),
        filepfad varchar(255),
        filesize varchar(255),
        typ varchar(255),
        write_protected bit,
        zahlungsauftrag_id varchar(36),
        primary key (id, rev)
    );

    create table pensum_fachstelle_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        pensum integer,
        fachstelle_id varchar(36),
        primary key (id, rev)
    );

    create table pensum_fachstelle (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        pensum integer not null,
        fachstelle_id varchar(36) not null,
        primary key (id)
    );

    create table revinfo (
        rev integer not null auto_increment,
        revtstmp bigint,
        primary key (rev)
    );

    create table sequence (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        current_value bigint not null,
        sequence_type varchar(100) not null,
        mandant_id varchar(36) not null,
        primary key (id)
    );

    create table traegerschaft (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        active bit not null,
        mail varchar(255) not null,
        name varchar(255) not null,
        primary key (id)
    );

    create table traegerschaft_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        active bit,
        mail varchar(255),
        name varchar(255),
        primary key (id, rev)
    );

    create table verfuegung (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        generated_bemerkungen varchar(4000),
        kategorie_kein_pensum bit not null,
        kategorie_max_einkommen bit not null,
        kategorie_nicht_eintreten bit not null,
        kategorie_normal bit not null,
        kategorie_zuschlag_zum_erwerbspensum bit not null,
        manuelle_bemerkungen varchar(4000),
        primary key (id)
    );

    create table verfuegung_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        generated_bemerkungen varchar(4000),
        kategorie_kein_pensum bit,
        kategorie_max_einkommen bit,
        kategorie_nicht_eintreten bit,
        kategorie_normal bit,
        kategorie_zuschlag_zum_erwerbspensum bit,
        manuelle_bemerkungen varchar(4000),
        primary key (id, rev)
    );

    create table verfuegung_zeitabschnitt_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        abzug_fam_groesse decimal(19,2),
        anspruchberechtigtes_pensum integer,
        bemerkungen varchar(4000),
        betreuungspensum integer,
        betreuungsstunden decimal(19,2),
        einkommensjahr integer,
        elternbeitrag decimal(19,2),
        fam_groesse decimal(19,2),
        massgebendes_einkommen_vor_abzug_famgr decimal(19,2),
        vollkosten decimal(19,2),
        zahlungsstatus varchar(255),
        zu_spaet_eingereicht bit,
        verfuegung_id varchar(36),
        primary key (id, rev)
    );

    create table verfuegung_zeitabschnitt (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        abzug_fam_groesse decimal(19,2),
        anspruchberechtigtes_pensum integer not null,
        bemerkungen varchar(4000),
        betreuungspensum integer not null,
        betreuungsstunden decimal(19,2),
        einkommensjahr integer not null,
        elternbeitrag decimal(19,2),
        fam_groesse decimal(19,2),
        massgebendes_einkommen_vor_abzug_famgr decimal(19,2),
        vollkosten decimal(19,2),
        zahlungsstatus varchar(255) not null,
        zu_spaet_eingereicht bit not null,
        verfuegung_id varchar(36) not null,
        primary key (id)
    );

    create table vorlage (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        filename varchar(255) not null,
        filepfad varchar(255) not null,
        filesize varchar(255) not null,
        primary key (id)
    );

    create table vorlage_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        filename varchar(255),
        filepfad varchar(255),
        filesize varchar(255),
        primary key (id, rev)
    );

    create table wizard_step_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        bemerkungen varchar(4000),
        verfuegbar bit,
        wizard_step_name varchar(255),
        wizard_step_status varchar(255),
        gesuch_id varchar(36),
        primary key (id, rev)
    );

    create table wizard_step (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        bemerkungen varchar(4000),
        verfuegbar bit not null,
        wizard_step_name varchar(255) not null,
        wizard_step_status varchar(255) not null,
        gesuch_id varchar(36) not null,
        primary key (id)
    );

    create table workjob (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        execution_id bigint,
        metadata longtext,
        params varchar(255) not null,
        requesturi varchar(255) not null,
        result_data varchar(255),
        startinguser varchar(255) not null,
        status varchar(255) not null,
        triggering_ip varchar(45),
        work_job_type varchar(255) not null,
        primary key (id)
    );

    create table zahlung (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        betrag_total_zahlung decimal(19,2),
        status varchar(255) not null,
        institution_stammdaten_id varchar(36) not null,
        zahlungsauftrag_id varchar(36) not null,
        primary key (id)
    );

    create table zahlung_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        betrag_total_zahlung decimal(19,2),
        status varchar(255),
        institution_stammdaten_id varchar(36),
        zahlungsauftrag_id varchar(36),
        primary key (id, rev)
    );

    create table zahlungsauftrag (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        gueltig_ab date not null,
        gueltig_bis date not null,
        beschrieb varchar(255) not null,
        betrag_total_auftrag decimal(19,2),
        datum_faellig date not null,
        datum_generiert datetime not null,
        status varchar(255) not null,
        primary key (id)
    );

    create table zahlungsauftrag_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        gueltig_ab date,
        gueltig_bis date,
        beschrieb varchar(255),
        betrag_total_auftrag decimal(19,2),
        datum_faellig date,
        datum_generiert datetime,
        status varchar(255),
        primary key (id, rev)
    );

    create table zahlungsposition (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(36) not null,
        user_mutiert varchar(36) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        betrag decimal(19,2) not null,
        ignoriert bit not null,
        status varchar(255) not null,
        verfuegung_zeitabschnitt_id varchar(36) not null,
        zahlung_id varchar(36) not null,
        primary key (id)
    );

    create table zahlungsposition_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(36),
        user_mutiert varchar(36),
        vorgaenger_id varchar(36),
        betrag decimal(19,2),
        ignoriert bit,
        status varchar(255),
        verfuegung_zeitabschnitt_id varchar(36),
        zahlung_id varchar(36),
        primary key (id, rev)
    );

    alter table application_property
        add constraint UK_application_property_name unique (name);
create index IX_authorisierter_benutzer on authorisierter_benutzer (benutzer_id);
create index IX_authorisierter_benutzer_token on authorisierter_benutzer (auth_token, benutzer_id);
create index IX_benutzer_username on benutzer (username);

    alter table benutzer
        add constraint UK_username unique (username);

    alter table betreuung
        add constraint UK_betreuung_kind_betreuung_nummer unique (betreuung_nummer, kind_id);

    alter table betreuung
        add constraint UK_betreuung_verfuegung_id unique (verfuegung_id);
create index IX_dossier_verantwortlicher_bg on dossier (verantwortlicherbg_id);
create index IX_dossier_verantwortlicher_ts on dossier (verantwortlicherts_id);

    alter table dossier
        add constraint UK_dossier_fall_gemeinde unique (fall_id, gemeinde_id);

    alter table ebegu_parameter
        add constraint UK_ebegu_parameter unique (name, gueltig_ab, gueltig_bis);

    alter table einkommensverschlechterung_container
        add constraint UK_einkommensverschlechterungcontainer_gesuchsteller unique (gesuchsteller_container_id);
create index IX_fall_fall_nummer on fall (fall_nummer);
create index IX_fall_besitzer on fall (besitzer_id);
create index IX_fall_mandant on fall (mandant_id);

    alter table fall
        add constraint UK_fall_nummer unique (fall_nummer);

    alter table fall
        add constraint UK_fall_besitzer unique (besitzer_id);

    alter table ferieninsel_stammdaten_ferieninsel_zeitraum
        add constraint UK_misbqu546cnkqs8b62v06r4yr unique (zeitraum_list_id);

    alter table finanzielle_situation_container
        add constraint UK_finanzielle_situation_container_gesuchsteller unique (gesuchsteller_container_id);

    alter table gemeinde
        add constraint UK_gemeinde_name unique (name);
create index IX_gesuch_timestamp_erstellt on gesuch (timestamp_erstellt);

    alter table gesuch
        add constraint UK_gueltiges_gesuch unique (dossier_id, gesuchsperiode_id, gueltig);
create index IX_institution_stammdaten_gueltig_ab on institution_stammdaten (gueltig_ab);
create index IX_institution_stammdaten_gueltig_bis on institution_stammdaten (gueltig_bis);

    alter table institution_stammdaten
        add constraint UK_institution_stammdaten_adresse_id unique (adresse_id);

    alter table institution_stammdaten
        add constraint UK_institution_stammdaten_adressekontoinhaber_id unique (adresse_kontoinhaber_id);
create index IX_kind_geburtsdatum on kind (geburtsdatum);

    alter table kind_container
        add constraint UK_kindcontainer_gesuch_kind_nummer unique (kind_nummer, gesuch_id);
create index sequence_ix1 on sequence (mandant_id);

    alter table sequence
        add constraint UK_sequence unique (sequence_type, mandant_id);

    alter table wizard_step
        add constraint UK_wizardstep_gesuch_stepname unique (wizard_step_name, gesuch_id);

    alter table abwesenheit_aud
        add constraint FKng1vhbp42873xg2wtpdl0oqk2
        foreign key (rev)
        references revinfo (rev);

    alter table abwesenheit_container_aud
        add constraint FK3wch3ne219lglatthsh2fuap3
        foreign key (rev)
        references revinfo (rev);

    alter table abwesenheit_container
        add constraint FK_abwesenheit_container_abwesenheit_gs
        foreign key (abwesenheitgs_id)
        references abwesenheit (id);

    alter table abwesenheit_container
        add constraint FK_abwesenheit_container_abwesenheit_ja
        foreign key (abwesenheitja_id)
        references abwesenheit (id);

    alter table abwesenheit_container
        add constraint FK_abwesenheit_container_betreuung_id
        foreign key (betreuung_id)
        references betreuung (id);

    alter table adresse_aud
        add constraint FKeartnqqce0eqjl4tiv5my51ee
        foreign key (rev)
        references revinfo (rev);

    alter table antrag_status_history_aud
        add constraint FKbji08vcwo43yh6vrwvxqdlwxp
        foreign key (rev)
        references revinfo (rev);

    alter table antrag_status_history
        add constraint FK_antragstatus_history_benutzer_id
        foreign key (benutzer_id)
        references benutzer (id);

    alter table antrag_status_history
        add constraint FK_antragstatus_history_antrag_id
        foreign key (gesuch_id)
        references gesuch (id);

    alter table application_property_aud
        add constraint FK68vs0wt0pakr900dxybu1tdy5
        foreign key (rev)
        references revinfo (rev);

    alter table authorisierter_benutzer
        add constraint FK_authorisierter_benutzer_benutzer_id
        foreign key (benutzer_id)
        references benutzer (id);

    alter table belegung_ferieninsel_aud
        add constraint FKocjn25rl9n0cm0r92nq7wj2tt
        foreign key (rev)
        references revinfo (rev);

    alter table belegung_ferieninsel_belegung_ferieninsel_tag_aud
        add constraint FKklgh9wrplff4566h3rm2dxfjx
        foreign key (rev)
        references revinfo (rev);

    alter table belegung_ferieninsel_tag_aud
        add constraint FK58gghrl3m413t84vpjvqj05hj
        foreign key (rev)
        references revinfo (rev);

    alter table belegung_tagesschule_aud
        add constraint FKe4ule2q27medxr46nue1kw5c9
        foreign key (rev)
        references revinfo (rev);

    alter table belegung_tagesschule_modul_tagesschule_aud
        add constraint FKcl9936gltea6ttlcfgdnxs63b
        foreign key (rev)
        references revinfo (rev);

    alter table belegung_ferieninsel_belegung_ferieninsel_tag
        add constraint FKciulwk88xhorhdgm3mhmsrmjv
        foreign key (tage_id)
        references belegung_ferieninsel_tag (id);

    alter table belegung_ferieninsel_belegung_ferieninsel_tag
        add constraint FK4p3ipudli5oswp4td00qs3gp3
        foreign key (belegung_ferieninsel_id)
        references belegung_ferieninsel (id);

    alter table belegung_tagesschule_modul_tagesschule
        add constraint FKak6ds6xn9iaa2jo0wf9ounfjg
        foreign key (module_tagesschule_id)
        references modul_tagesschule (id);

    alter table belegung_tagesschule_modul_tagesschule
        add constraint FKgn7dinsrgt9k4d51p04dgv87t
        foreign key (belegung_tagesschule_id)
        references belegung_tagesschule (id);

    alter table benutzer
        add constraint FK_benutzer_mandant_id
        foreign key (mandant_id)
        references mandant (id);

    alter table benutzer_aud
        add constraint FKswc4iahf0lnluku84fvkpp5wy
        foreign key (rev)
        references revinfo (rev);

    alter table berechtigung
        add constraint FK_Berechtigung_benutzer_id
        foreign key (benutzer_id)
        references benutzer (id);

    alter table berechtigung
        add constraint FK_Berechtigung_institution_id
        foreign key (institution_id)
        references institution (id);

    alter table berechtigung
        add constraint FK_Berechtigung_traegerschaft_id
        foreign key (traegerschaft_id)
        references traegerschaft (id);

    alter table berechtigung_aud
        add constraint FKrwxoku5p2f4w3qx4lakpmlbrl
        foreign key (rev)
        references revinfo (rev);

    alter table berechtigung_history_aud
        add constraint FK6sbg81ysce804v0l7crx7eiac
        foreign key (rev)
        references revinfo (rev);

    alter table berechtigung_history
        add constraint FK_berechtigung_history_institution_id
        foreign key (institution_id)
        references institution (id);

    alter table berechtigung_history
        add constraint FK_berechtigung_history_traegerschaft_id
        foreign key (traegerschaft_id)
        references traegerschaft (id);

    alter table betreuung
        add constraint FK_betreuung_belegung_ferieninsel_id
        foreign key (belegung_ferieninsel_id)
        references belegung_ferieninsel (id);

    alter table betreuung
        add constraint FK_betreuung_belegung_tagesschule_id
        foreign key (belegung_tagesschule_id)
        references belegung_tagesschule (id);

    alter table betreuung
        add constraint FK_betreuung_institution_stammdaten_id
        foreign key (institution_stammdaten_id)
        references institution_stammdaten (id);

    alter table betreuung
        add constraint FK_betreuung_kind_id
        foreign key (kind_id)
        references kind_container (id);

    alter table betreuung
        add constraint FK_betreuung_verfuegung_id
        foreign key (verfuegung_id)
        references verfuegung (id);

    alter table betreuung_aud
        add constraint FK2xh5vmlxot3aq29mla11no60y
        foreign key (rev)
        references revinfo (rev);

    alter table betreuungsmitteilung_pensum_aud
        add constraint FK9uwg4psfpfukay2oh88ejopob
        foreign key (rev)
        references revinfo (rev);

    alter table betreuungsmitteilung_pensum
        add constraint FKf3inckvoj5b2wm7sunf199im9
        foreign key (betreuungsmitteilung_id)
        references mitteilung (id);

    alter table betreuungspensum_aud
        add constraint FK7oh6pmmpm286dxqdvog63yhqy
        foreign key (rev)
        references revinfo (rev);

    alter table betreuungspensum_container_aud
        add constraint FKnktcdcxbvqxmj4jwmiyij369e
        foreign key (rev)
        references revinfo (rev);

    alter table betreuungspensum_container
        add constraint FK_betreuungspensum_container_betreuung_id
        foreign key (betreuung_id)
        references betreuung (id);

    alter table betreuungspensum_container
        add constraint FK_betreuungspensum_container_betreuungspensum_gs
        foreign key (betreuungspensumgs_id)
        references betreuungspensum (id);

    alter table betreuungspensum_container
        add constraint FK_betreuungspensum_container_betreuungspensum_ja
        foreign key (betreuungspensumja_id)
        references betreuungspensum (id);

    alter table dokument
        add constraint FK_dokument_dokumentgrund_id
        foreign key (dokument_grund_id)
        references dokument_grund (id);

    alter table dokument_aud
        add constraint FKk1lje21u4ksjamyfue7t3hyda
        foreign key (rev)
        references revinfo (rev);

    alter table dokument_grund_aud
        add constraint FKso4s0v3gmc9uc8woigagw35ru
        foreign key (rev)
        references revinfo (rev);

    alter table dokument_grund
        add constraint FK_dokumentGrund_gesuch_id
        foreign key (gesuch_id)
        references gesuch (id);

    alter table dossier
        add constraint FK_dossier_fall_id
        foreign key (fall_id)
        references fall (id);

    alter table dossier
        add constraint FK_dossier_gemeinde_id
        foreign key (gemeinde_id)
        references gemeinde (id);

    alter table dossier
        add constraint FK_dossier_verantwortlicher_bg_id
        foreign key (verantwortlicherbg_id)
        references benutzer (id);

    alter table dossier
        add constraint FK_dossier_verantwortlicher_ts_id
        foreign key (verantwortlicherts_id)
        references benutzer (id);

    alter table dossier_aud
        add constraint FKrr1jcn0gmtxqnn7la4n7j7phy
        foreign key (rev)
        references revinfo (rev);

    alter table ebegu_parameter_aud
        add constraint FKbk4j6vkydxdgc43m1bv82g4ka
        foreign key (rev)
        references revinfo (rev);

    alter table ebegu_vorlage_aud
        add constraint FKc611a7ud0633fxbbic18kdar4
        foreign key (rev)
        references revinfo (rev);

    alter table ebegu_vorlage
        add constraint FK_ebeguvorlage_vorlage_id
        foreign key (vorlage_id)
        references vorlage (id);

    alter table einkommensverschlechterung_aud
        add constraint FK56slkkb5612k09yofki48hsx7
        foreign key (rev)
        references revinfo (rev);

    alter table einkommensverschlechterung_container_aud
        add constraint FK5edp1jhvmj2bfe27smqm7rx8i
        foreign key (rev)
        references revinfo (rev);

    alter table einkommensverschlechterung_info_aud
        add constraint FKmm4qmk2w5n9kc9ne8eu9aip85
        foreign key (rev)
        references revinfo (rev);

    alter table einkommensverschlechterung_info_container_aud
        add constraint FKfdp2w03ouyhqq78c83rkh0f0m
        foreign key (rev)
        references revinfo (rev);

    alter table einkommensverschlechterung_container
        add constraint FK_einkommensverschlechterungcontainer_ekvGSBasisJahrPlus1_id
        foreign key (ekvgsbasis_jahr_plus1_id)
        references einkommensverschlechterung (id);

    alter table einkommensverschlechterung_container
        add constraint FK_einkommensverschlechterungcontainer_ekvGSBasisJahrPlus2_id
        foreign key (ekvgsbasis_jahr_plus2_id)
        references einkommensverschlechterung (id);

    alter table einkommensverschlechterung_container
        add constraint FK_einkommensverschlechterungcontainer_ekvJABasisJahrPlus1_id
        foreign key (ekvjabasis_jahr_plus1_id)
        references einkommensverschlechterung (id);

    alter table einkommensverschlechterung_container
        add constraint FK_einkommensverschlechterungcontainer_ekvJABasisJahrPlus2_id
        foreign key (ekvjabasis_jahr_plus2_id)
        references einkommensverschlechterung (id);

    alter table einkommensverschlechterung_container
        add constraint FK_einkommensverschlechterungcontainer_gesuchstellerContainer_id
        foreign key (gesuchsteller_container_id)
        references gesuchsteller_container (id);

    alter table einkommensverschlechterung_info_container
        add constraint FK_ekvinfocontainer_einkommensverschlechterunginfogs_id
        foreign key (einkommensverschlechterung_infogs_id)
        references einkommensverschlechterung_info (id);

    alter table einkommensverschlechterung_info_container
        add constraint FK_ekvinfocontainer_einkommensverschlechterunginfoja_id
        foreign key (einkommensverschlechterung_infoja_id)
        references einkommensverschlechterung_info (id);

    alter table erwerbspensum_aud
        add constraint FK1v0oqhw2c90hd8xiin6mn7box
        foreign key (rev)
        references revinfo (rev);

    alter table erwerbspensum_container_aud
        add constraint FKcn4wryotuuchxfinepa1aqg8j
        foreign key (rev)
        references revinfo (rev);

    alter table erwerbspensum_container
        add constraint FK_erwerbspensum_container_erwerbspensumgs_id
        foreign key (erwerbspensumgs_id)
        references erwerbspensum (id);

    alter table erwerbspensum_container
        add constraint FK_erwerbspensum_container_erwerbspensumja_id
        foreign key (erwerbspensumja_id)
        references erwerbspensum (id);

    alter table erwerbspensum_container
        add constraint FK_erwerbspensum_container_gesuchstellerContainer_id
        foreign key (gesuchsteller_container_id)
        references gesuchsteller_container (id);

    alter table fachstelle_aud
        add constraint FKkww9j18yvjyddmx6qv2vuekfl
        foreign key (rev)
        references revinfo (rev);

    alter table fall
        add constraint FK_fall_besitzer_id
        foreign key (besitzer_id)
        references benutzer (id);

    alter table fall
        add constraint FK_fall_mandant_id
        foreign key (mandant_id)
        references mandant (id);

    alter table fall_aud
        add constraint FKmkfehw6pmws35qorifenrvu7e
        foreign key (rev)
        references revinfo (rev);

    alter table familiensituation_aud
        add constraint FKh00xtricpc6bcs50m5njbuaky
        foreign key (rev)
        references revinfo (rev);

    alter table familiensituation_container_aud
        add constraint FK22okgcsp4dd71cqj1p5j3wi08
        foreign key (rev)
        references revinfo (rev);

    alter table familiensituation_container
        add constraint FK_familiensituation_container_familiensituation_erstgesuch_id
        foreign key (familiensituation_erstgesuch_id)
        references familiensituation (id);

    alter table familiensituation_container
        add constraint FK_familiensituation_container_familiensituation_GS_id
        foreign key (familiensituationgs_id)
        references familiensituation (id);

    alter table familiensituation_container
        add constraint FK_familiensituation_container_familiensituation_JA_id
        foreign key (familiensituationja_id)
        references familiensituation (id);

    alter table ferieninsel_stammdaten_aud
        add constraint FKjpnavkjleutq2k4tsrv45uc99
        foreign key (rev)
        references revinfo (rev);

    alter table ferieninsel_stammdaten_ferieninsel_zeitraum_aud
        add constraint FK8muysug9y7tup7n35hob4xlik
        foreign key (rev)
        references revinfo (rev);

    alter table ferieninsel_zeitraum_aud
        add constraint FKfwhjmm489aqn5n2xh6rgoay7j
        foreign key (rev)
        references revinfo (rev);

    alter table ferieninsel_stammdaten
        add constraint FK94jeg2xss3b2fo6tmtaxplp1l
        foreign key (gesuchsperiode_id)
        references gesuchsperiode (id);

    alter table ferieninsel_stammdaten_ferieninsel_zeitraum
        add constraint FKm8jw70nlbxuui4ve64fr2xpql
        foreign key (zeitraum_list_id)
        references ferieninsel_zeitraum (id);

    alter table ferieninsel_stammdaten_ferieninsel_zeitraum
        add constraint FKgp7ofx97oamawji11t4lry5m0
        foreign key (ferieninsel_stammdaten_id)
        references ferieninsel_stammdaten (id);

    alter table finanzielle_situation_aud
        add constraint FK15h5ck8wvo4ldvbmu4e6fe7wr
        foreign key (rev)
        references revinfo (rev);

    alter table finanzielle_situation_container_aud
        add constraint FK4ycpgbn7wpjuf1eg41de7a61y
        foreign key (rev)
        references revinfo (rev);

    alter table finanzielle_situation_container
        add constraint FK_finanzielleSituationContainer_finanzielleSituationGS_id
        foreign key (finanzielle_situationgs_id)
        references finanzielle_situation (id);

    alter table finanzielle_situation_container
        add constraint FK_finanzielleSituationContainer_finanzielleSituationJA_id
        foreign key (finanzielle_situationja_id)
        references finanzielle_situation (id);

    alter table finanzielle_situation_container
        add constraint FK_finanzielleSituationContainer_gesuchstellerContainer_id
        foreign key (gesuchsteller_container_id)
        references gesuchsteller_container (id);

    alter table gemeinde_aud
        add constraint FK7wkhewqd3f81aasnstuv0s3q8
        foreign key (rev)
        references revinfo (rev);

    alter table generated_dokument_aud
        add constraint FKklxgsrr8antlc27iicw59qnlk
        foreign key (rev)
        references revinfo (rev);

    alter table generated_dokument
        add constraint FK_generated_dokument_gesuch_id
        foreign key (gesuch_id)
        references gesuch (id);

    alter table gesuch
        add constraint FK_gesuch_dossier_id
        foreign key (dossier_id)
        references dossier (id);

    alter table gesuch
        add constraint FK_gesuch_einkommensverschlechterungInfoContainer_id
        foreign key (einkommensverschlechterung_info_container_id)
        references einkommensverschlechterung_info_container (id);

    alter table gesuch
        add constraint FK_gesuch_familiensituation_container_id
        foreign key (familiensituation_container_id)
        references familiensituation_container (id);

    alter table gesuch
        add constraint FK_antrag_gesuchsperiode_id
        foreign key (gesuchsperiode_id)
        references gesuchsperiode (id);

    alter table gesuch
        add constraint FK_gesuch_gesuchsteller_container1_id
        foreign key (gesuchsteller1_id)
        references gesuchsteller_container (id);

    alter table gesuch
        add constraint FK_gesuch_gesuchsteller_container2_id
        foreign key (gesuchsteller2_id)
        references gesuchsteller_container (id);

    alter table gesuch_aud
        add constraint FKau3w8os8gh8oyci7fpr5jrnoq
        foreign key (rev)
        references revinfo (rev);

    alter table gesuchsperiode_aud
        add constraint FKjd1ewwubxbgphr1pmogojntav
        foreign key (rev)
        references revinfo (rev);

    alter table gesuchsteller_adresse_aud
        add constraint FK3dh5st3mw7t9292dc1gx2d8id
        foreign key (id, rev)
        references adresse_aud (id, rev);

    alter table gesuchsteller_adresse_container_aud
        add constraint FKhqbwibfg9v4uu8kh8dxn03cad
        foreign key (rev)
        references revinfo (rev);

    alter table gesuchsteller_aud
        add constraint FK7j2ytwnvch3mv3gikt230uj9i
        foreign key (rev)
        references revinfo (rev);

    alter table gesuchsteller_container_aud
        add constraint FKtnsm6qs3duwtsk406umn8vl11
        foreign key (rev)
        references revinfo (rev);

    alter table gesuchsteller_adresse
        add constraint FKk7huw60njs23n1oqiwx7tu8dw
        foreign key (id)
        references adresse (id);

    alter table gesuchsteller_adresse_container
        add constraint FK_gesuchstelleradresse_container_gesuchstellergs_id
        foreign key (gesuchsteller_adressegs_id)
        references gesuchsteller_adresse (id);

    alter table gesuchsteller_adresse_container
        add constraint FK_gesuchstelleradresse_container_gesuchstellerja_id
        foreign key (gesuchsteller_adresseja_id)
        references gesuchsteller_adresse (id);

    alter table gesuchsteller_adresse_container
        add constraint FK_gesuchstelleradresse_container_gesuchstellerContainer_id
        foreign key (gesuchsteller_container_id)
        references gesuchsteller_container (id);

    alter table gesuchsteller_container
        add constraint FK_gesuchsteller_container_gesuchstellergs_id
        foreign key (gesuchstellergs_id)
        references gesuchsteller (id);

    alter table gesuchsteller_container
        add constraint FK_gesuchsteller_container_gesuchstellerja_id
        foreign key (gesuchstellerja_id)
        references gesuchsteller (id);

    alter table institution
        add constraint FK_institution_mandant_id
        foreign key (mandant_id)
        references mandant (id);

    alter table institution
        add constraint FK_institution_traegerschaft_id
        foreign key (traegerschaft_id)
        references traegerschaft (id);

    alter table institution_aud
        add constraint FKmh0q5supgbar7itjmlt47rom6
        foreign key (rev)
        references revinfo (rev);

    alter table institution_stammdaten_aud
        add constraint FK98l7foa2nq9ad51wvo1yc6kfp
        foreign key (rev)
        references revinfo (rev);

    alter table institution_stammdaten_ferieninsel_aud
        add constraint FK7covdo1m7i8nyrm9jkphrhd1f
        foreign key (rev)
        references revinfo (rev);

    alter table institution_stammdaten_tagesschule_aud
        add constraint FK5mqsaorwajyw5sw3b056ef4j7
        foreign key (rev)
        references revinfo (rev);

    alter table institution_stammdaten
        add constraint FK_institution_stammdaten_adresse_id
        foreign key (adresse_id)
        references adresse (id);

    alter table institution_stammdaten
        add constraint FK_institution_stammdaten_adressekontoinhaber_id
        foreign key (adresse_kontoinhaber_id)
        references adresse (id);

    alter table institution_stammdaten
        add constraint FK_institution_stammdaten_institution_id
        foreign key (institution_id)
        references institution (id);

    alter table institution_stammdaten
        add constraint FK_inst_stammdaten_inst_stammdaten_ferieninsel_id
        foreign key (institution_stammdaten_ferieninsel_id)
        references institution_stammdaten_ferieninsel (id);

    alter table institution_stammdaten
        add constraint FK_inst_stammdaten_inst_stammdaten_tagesschule_id
        foreign key (institution_stammdaten_tagesschule_id)
        references institution_stammdaten_tagesschule (id);

    alter table kind
        add constraint FK_kind_pensum_fachstelle_id
        foreign key (pensum_fachstelle_id)
        references pensum_fachstelle (id);

    alter table kind_aud
        add constraint FK8i4febfygx85q24mxbsva18ky
        foreign key (rev)
        references revinfo (rev);

    alter table kind_container_aud
        add constraint FKjrdaj5nksetx2qq6bj4f07vim
        foreign key (rev)
        references revinfo (rev);

    alter table kind_container
        add constraint FK_kind_container_gesuch_id
        foreign key (gesuch_id)
        references gesuch (id);

    alter table kind_container
        add constraint FK_kind_container_kindgs_id
        foreign key (kindgs_id)
        references kind (id);

    alter table kind_container
        add constraint FK_kind_container_kindja_id
        foreign key (kindja_id)
        references kind (id);

    alter table mahnung
        add constraint FK_mahnung_gesuch_id
        foreign key (gesuch_id)
        references gesuch (id);

    alter table mahnung_aud
        add constraint FKa4xsokkwkcbbesloibxkqirup
        foreign key (rev)
        references revinfo (rev);

    alter table mandant_aud
        add constraint FKdu77v3o68cjm2rpv18o9dtdr8
        foreign key (rev)
        references revinfo (rev);

    alter table mitteilung
        add constraint FK_mitteilung_betreuung_id
        foreign key (betreuung_id)
        references betreuung (id);

    alter table mitteilung
        add constraint FK_mitteilung_dossier_id
        foreign key (dossier_id)
        references dossier (id);

    alter table mitteilung
        add constraint FK_Mitteilung_empfaenger
        foreign key (empfaenger_id)
        references benutzer (id);

    alter table mitteilung
        add constraint FK_Mitteilung_sender
        foreign key (sender_id)
        references benutzer (id);

    alter table mitteilung_aud
        add constraint FKbx5q2psq93ddln9e5ltb81r0k
        foreign key (rev)
        references revinfo (rev);

    alter table modul_tagesschule_aud
        add constraint FKq5ttlty2w5mrv451jnux3hare
        foreign key (rev)
        references revinfo (rev);

    alter table modul_tagesschule
        add constraint FK_modul_tagesschule_inst_stammdaten_tagesschule_id
        foreign key (institution_stammdaten_tagesschule_id)
        references institution_stammdaten_tagesschule (id);

    alter table pain001dokument
        add constraint FK_pain001dokument_zahlungsauftrag_id
        foreign key (zahlungsauftrag_id)
        references zahlungsauftrag (id);

    alter table pain001dokument_aud
        add constraint FKixt7lodu7ftleyrbkdr960iae
        foreign key (rev)
        references revinfo (rev);

    alter table pensum_fachstelle_aud
        add constraint FK49d0rsee0nsvg5gobup5bsn0s
        foreign key (rev)
        references revinfo (rev);

    alter table pensum_fachstelle
        add constraint FK_pensum_fachstelle_fachstelle_id
        foreign key (fachstelle_id)
        references fachstelle (id);

    alter table sequence
        add constraint FK_sequence_mandant_id
        foreign key (mandant_id)
        references mandant (id);

    alter table traegerschaft_aud
        add constraint FK5vyi81krf34u05x83ddth451
        foreign key (rev)
        references revinfo (rev);

    alter table verfuegung_aud
        add constraint FKcofp5cmoodxyra4it64014lpw
        foreign key (rev)
        references revinfo (rev);

    alter table verfuegung_zeitabschnitt_aud
        add constraint FKlbycrj2yev60fc3f8yq5d4vt7
        foreign key (rev)
        references revinfo (rev);

    alter table verfuegung_zeitabschnitt
        add constraint FK_verfuegung_zeitabschnitt_verfuegung_id
        foreign key (verfuegung_id)
        references verfuegung (id);

    alter table vorlage_aud
        add constraint FKv3al533ofb3901bx7b1keq9j
        foreign key (rev)
        references revinfo (rev);

    alter table wizard_step_aud
        add constraint FKrvkybxdbw4651b7upsg0t8k53
        foreign key (rev)
        references revinfo (rev);

    alter table wizard_step
        add constraint FK_wizardstep_gesuch_id
        foreign key (gesuch_id)
        references gesuch (id);

    alter table zahlung
        add constraint FK_Zahlung_institutionStammdaten_id
        foreign key (institution_stammdaten_id)
        references institution_stammdaten (id);

    alter table zahlung
        add constraint FK_Zahlung_zahlungsauftrag_id
        foreign key (zahlungsauftrag_id)
        references zahlungsauftrag (id);

    alter table zahlung_aud
        add constraint FKrni77pdlbfrnr4hsiw6t6h9us
        foreign key (rev)
        references revinfo (rev);

    alter table zahlungsauftrag_aud
        add constraint FKth39aofu6ptxfp1red8yfefy4
        foreign key (rev)
        references revinfo (rev);

    alter table zahlungsposition
        add constraint FK_Zahlungsposition_verfuegungZeitabschnitt_id
        foreign key (verfuegung_zeitabschnitt_id)
        references verfuegung_zeitabschnitt (id);

    alter table zahlungsposition
        add constraint FK_Zahlungsposition_zahlung_id
        foreign key (zahlung_id)
        references zahlung (id);

    alter table zahlungsposition_aud
        add constraint FKc7nfbyddcgeayy5mqdo2m3s2j
        foreign key (rev)
        references revinfo (rev);
