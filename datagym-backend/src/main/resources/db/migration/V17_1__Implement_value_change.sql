create table lc_entry_value_change
(
    DTYPE                  varchar(31)  not null,
    id                     varchar(255) not null,
    frame                  integer,
    frame_type             varchar(255),
    labeler                varchar(255) not null,
    timestamp              bigint       not null,
    radio_key              varchar(255),
    select_key             varchar(255),
    text                   varchar(255),
    points                 varchar(12288),
    point                  varchar(255),
    height                 double precision,
    width                  double precision,
    x                      double precision,
    y                      double precision,
    lc_entry_root_value_id varchar(255) not null,
    lc_entry_value_id      varchar(255) not null,
    primary key (id)
);
create table lc_entry_checklist_change_values
(
    checklist_change_value_id varchar(255) not null,
    checked_value             varchar(255)
);
alter table lc_entry_checklist_change_values
    add constraint fk_checklistchangevalues_checklistvalue foreign key (checklist_change_value_id) references lc_entry_value_change (id);
alter table lc_entry_value_change
    add constraint fk_entryvaluechange_lcentryrootvalue foreign key (lc_entry_root_value_id) references lc_entry_value (id);
alter table lc_entry_value_change
    add constraint fk_entryvaluechange_lcentryvalue foreign key (lc_entry_value_id) references lc_entry_value (id);