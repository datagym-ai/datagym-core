create table pre_label_configuration (id varchar(255) not null, activate_state BOOLEAN DEFAULT FALSE not null, primary key (id));
create table pre_label_mapping_entry (id varchar(255) not null, pre_label_class_key varchar(255) not null, pre_label_model varchar(255) not null, lcEntry_id varchar(255) not null, preLabelConfig_id varchar(255) not null, primary key (id));
alter table project add column pre_label_configuration_id varchar(255);
alter table pre_label_mapping_entry add constraint fk_prelabelmapping_lcentry foreign key (lcEntry_id) references lc_entry (id);
alter table pre_label_mapping_entry add constraint fk_prelabelmapping_prelabelconfig foreign key (preLabelConfig_id) references pre_label_configuration (id);
alter table project add constraint fk_project_prelabelconfiguration foreign key (pre_label_configuration_id) references pre_label_configuration (id);