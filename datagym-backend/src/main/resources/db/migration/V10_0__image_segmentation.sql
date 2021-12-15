create table point_collection (id varchar(255) not null, segmentation_value_id varchar(255), pointcollection_uuid integer, primary key (id));
alter table point_collection add constraint fk_pointcollection_entryvaluesegmentation foreign key (segmentation_value_id) references lc_entry_value (id);
alter table point_pojo add column point_collection_id varchar(255);
alter table point_pojo add constraint fk_pointpojo_pointcollection foreign key (point_collection_id) references point_collection (id);
alter table lc_entry modify column type ENUM('POINT', 'LINE', 'POLYGON', 'RECTANGLE', 'SELECT', 'RADIO', 'CHECKLIST', 'FREETEXT', 'IMAGE_SEGMENTATION');