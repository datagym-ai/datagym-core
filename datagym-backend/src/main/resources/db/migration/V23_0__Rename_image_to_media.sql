ALTER TABLE image RENAME COLUMN image_type TO media_type;
ALTER TABLE image RENAME COLUMN image_name TO media_name;
ALTER TABLE image RENAME COLUMN invalid_image_reason TO invalid_media_reason;

ALTER TABLE lc_entry_value RENAME COLUMN image_id TO media_id;
ALTER TABLE dataset_image RENAME COLUMN image_id TO media_id;
ALTER TABLE labeler_rating RENAME COLUMN image_id TO media_id;
ALTER TABLE label_task RENAME COLUMN image_id TO media_id;

ALTER TABLE image RENAME TO media;
ALTER TABLE dataset_image RENAME TO dataset_media;

ALTER TABLE labeler_rating DROP FOREIGN KEY fk_labeler_rating_image;
ALTER TABLE labeler_rating
    ADD CONSTRAINT fk_labeler_rating_media FOREIGN KEY (media_id) REFERENCES media (id);

ALTER TABLE dataset_media DROP FOREIGN KEY fk_image_dataset;
ALTER TABLE dataset_media
    ADD CONSTRAINT fk_media_dataset FOREIGN KEY (media_id) REFERENCES media (id);

ALTER TABLE dataset_media DROP FOREIGN KEY fk_dataset_image;
ALTER TABLE dataset_media
    ADD CONSTRAINT fk_dataset_media FOREIGN KEY (media_id) REFERENCES media (id);

ALTER TABLE label_task DROP FOREIGN KEY fk_labeltask_image;
ALTER TABLE label_task
    ADD CONSTRAINT fk_labeltask_media FOREIGN KEY (media_id) REFERENCES media (id);

ALTER TABLE lc_entry_value DROP FOREIGN KEY fk_entryvalue_image;
ALTER TABLE lc_entry_value
    ADD CONSTRAINT fk_entryvalue_media FOREIGN KEY (media_id) REFERENCES media (id);
