    create table gemeinde_stammdaten_aud (
        id varchar(36) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        vorgaenger_id varchar(36),
        default_benutzerbg_id varchar(36),
        default_benutzerts_id varchar(36),
        gemeinde_id varchar(36),
        primary key (id, rev)
    );

    create table gemeinde_stammdaten (
        id varchar(36) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        default_benutzerbg_id varchar(36),
        default_benutzerts_id varchar(36),
        gemeinde_id varchar(36),
        primary key (id)
    );

    alter table gemeinde_stammdaten_aud
        add constraint FK_gemeindestammdaten_gemeinde_stammdaten_revinfo
        foreign key (rev)
        references revinfo (rev);

    alter table gemeinde_stammdaten
        add constraint FK_gemeindestammdaten_defaultbenutzerbg_id
        foreign key (default_benutzerbg_id)
        references benutzer (id);

    alter table gemeinde_stammdaten
        add constraint FK_gemeindestammdaten_defaultbenutzerts_id
        foreign key (default_benutzerts_id)
        references benutzer (id);

    alter table gemeinde_stammdaten
        add constraint FK_gemeindestammdaten_gemeinde_id
        foreign key (gemeinde_id)
        references gemeinde (id);