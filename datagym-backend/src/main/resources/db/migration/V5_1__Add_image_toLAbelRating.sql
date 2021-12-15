truncate table labeler_rating;
alter table labeler_rating add column image_id varchar(255) not null;
alter table labeler_rating add constraint fk_labeler_rating_image foreign key (image_id) references image (id);