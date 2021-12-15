alter table project add column media_type ENUM('IMAGE', 'VIDEO') default 'IMAGE' not null;
alter table dataset add column media_type ENUM('IMAGE', 'VIDEO') default 'IMAGE' not null;