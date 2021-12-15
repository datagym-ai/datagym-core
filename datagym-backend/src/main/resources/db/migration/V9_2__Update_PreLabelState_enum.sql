alter table label_task modify column pre_label_state ENUM('WAITING','IN_PROGRESS', 'FINISHED', 'FAILED');
