    create table rueckforderung_dokument_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        vorgaenger_id varchar(36),
        filename varchar(255),
        filepfad varchar(4000),
        filesize varchar(255),
        rueckforderung_dokument_typ integer,
        timestamp_upload datetime,
        rueckforderung_formular_id binary(16),
        primary key (id, rev)
    );

    create table rueckforderung_formular_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        stufe_1_institution_kostenuebernahme_betreuung decimal(19,2),
        stufe_2_institution_kostenuebernahme_betreuung decimal(19,2),
        stufe_1_kanton_kostenuebernahme_betreuung decimal(19,2),
        stufe_2_kanton_kostenuebernahme_betreuung decimal(19,2),
        stufe_1_stufe_1_institution_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_2_institution_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_1_kanton_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_2_kanton_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_1_institution_kostenuebernahme_anzahl_tage decimal(19,2),
        stufe_2_institution_kostenuebernahme_anzahl_tage decimal(19,2),
        stufe_1_kanton_kostenuebernahme_anzahl_tage decimal(19,2),
        stufe_2_kanton_kostenuebernahme_anzahl_tage decimal(19,2),
        status integer,
        institution_id binary(16),
        primary key (id, rev)
    );

    create table rueckforderung_formular_rueckforderung_mitteilung_aud (
        rev integer not null,
        rueckforderung_formular_id binary(16) not null,
        rueckforderung_mitteilung_id binary(16) not null,
        revtype tinyint,
        primary key (rev, rueckforderung_formular_id, rueckforderung_mitteilung_id)
    );

    create table rueckforderung_mitteilung_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        betreff varchar(255),
        gesendet_an_status varchar(255),
        inhalt varchar(255),
        sende_datum datetime,
        absender_id binary(16),
        primary key (id, rev)
    );

    create table rueckforderung_zahlung_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        betrag decimal(19,2),
        datum_ausgefuehrt datetime,
        zahlung_ausgefuehrt bit,
        rueckforderung_formular_id binary(16),
        primary key (id, rev)
    );

    create table rueckforderung_dokument (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        filename varchar(255) not null,
        filepfad varchar(4000) not null,
        filesize varchar(255) not null,
        rueckforderung_dokument_typ integer not null,
        timestamp_upload datetime not null,
        rueckforderung_formular_id binary(16) not null,
        primary key (id)
    );

    create table rueckforderung_formular (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        stufe_1_institution_kostenuebernahme_betreuung decimal(19,2),
        stufe_2_institution_kostenuebernahme_betreuung decimal(19,2),
        stufe_1_kanton_kostenuebernahme_betreuung decimal(19,2),
        stufe_2_kanton_kostenuebernahme_betreuung decimal(19,2),
        stufe_1_stufe_1_institution_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_2_institution_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_1_kanton_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_2_kanton_kostenuebernahme_anzahl_stunden decimal(19,2),
        stufe_1_institution_kostenuebernahme_anzahl_tage decimal(19,2),
        stufe_2_institution_kostenuebernahme_anzahl_tage decimal(19,2),
        stufe_1_kanton_kostenuebernahme_anzahl_tage decimal(19,2),
        stufe_2_kanton_kostenuebernahme_anzahl_tage decimal(19,2),
        status integer not null,
        institution_id binary(16) not null,
        primary key (id)
    );

    create table rueckforderung_formular_rueckforderung_mitteilung (
        rueckforderung_formular_id binary(16) not null,
        rueckforderung_mitteilung_id binary(16) not null,
        primary key (rueckforderung_formular_id, rueckforderung_mitteilung_id)
    );

    create table rueckforderung_mitteilung (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        betreff varchar(255) not null,
        gesendet_an_status varchar(255) not null,
        inhalt varchar(255) not null,
        sende_datum datetime not null,
        absender_id binary(16) not null,
        primary key (id)
    );

    create table rueckforderung_zahlung (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        betrag decimal(19,2) not null,
        datum_ausgefuehrt datetime not null,
        zahlung_ausgefuehrt bit not null,
        rueckforderung_formular_id binary(16) not null,
        primary key (id)
    );

    alter table rueckforderung_mitteilung 
        add constraint UK_rueckforderung_mitteilung_absender unique (absender_id);

    alter table rueckforderung_dokument_aud 
        add constraint FK_rueckforderung_dokument_rev
        foreign key (rev) 
        references revinfo (rev);

    alter table rueckforderung_formular_aud 
        add constraint FK_rueckforderung_formular_rev
        foreign key (rev) 
        references revinfo (rev);

    alter table rueckforderung_formular_rueckforderung_mitteilung_aud 
        add constraint FK_rueckforderung_formular_rueckforderung_mitteilung_rev
        foreign key (rev) 
        references revinfo (rev);

    alter table rueckforderung_mitteilung_aud 
        add constraint FK_rueckforderung_mitteilung_rev
        foreign key (rev) 
        references revinfo (rev);

    alter table rueckforderung_zahlung_aud 
        add constraint FK_rueckforderung_zahlung_rev
        foreign key (rev) 
        references revinfo (rev);

    alter table rueckforderung_dokument 
        add constraint FK_rueckforderungDokument_rueckforderungFormular_id 
        foreign key (rueckforderung_formular_id) 
        references rueckforderung_formular (id);

    alter table rueckforderung_formular 
        add constraint FK_rueckforderungFormular_institution_id 
        foreign key (institution_id) 
        references institution (id);

    alter table rueckforderung_formular_rueckforderung_mitteilung 
        add constraint FK_rueckforderung_formular_rueckforderung_mitteilung_formular_id 
        foreign key (rueckforderung_mitteilung_id) 
        references rueckforderung_mitteilung (id);

    alter table rueckforderung_formular_rueckforderung_mitteilung 
        add constraint FK_rueckforderung_formular_mitteilung_id
        foreign key (rueckforderung_formular_id) 
        references rueckforderung_formular (id);

    alter table rueckforderung_mitteilung 
        add constraint FK_RueckforderungMitteilung_Benutzer_id 
        foreign key (absender_id) 
        references benutzer (id);

    alter table rueckforderung_zahlung 
        add constraint FK_rueckforderungZahlung_rueckforderungFormular_id 
        foreign key (rueckforderung_formular_id) 
        references rueckforderung_formular (id);
