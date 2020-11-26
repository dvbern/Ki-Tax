alter table antrag_status_history modify timestamp_von datetime not null;
alter table antrag_status_history modify timestamp_bis datetime null;
alter table antrag_status_history_aud modify timestamp_von datetime null;
alter table antrag_status_history_aud modify timestamp_bis datetime null;

alter table mahnung modify timestamp_abgeschlossen datetime null;
alter table mahnung_aud modify timestamp_abgeschlossen datetime null;