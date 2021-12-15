alter table image add column aws_e_tag varchar(255);
alter table image add column aws_key varchar(255);
alter table image add column last_error varchar(255);
alter table image add column last_error_timeStamp bigint;
alter table image add column awsS3Credentials_id varchar(255);
alter table image add constraint fk_awsS3Image_awscredentials foreign key (awsS3Credentials_id) references aws_S3_credentials (id);