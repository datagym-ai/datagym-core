alter table image
    add column total_frames bigint;
alter table image
    add column duration double precision;
alter table image
    add column codec_name varchar(255);
alter table image
    add column format_name varchar(255);
alter table image
    add column r_frame_rate varchar(255);
alter table image
    add column size bigint;
