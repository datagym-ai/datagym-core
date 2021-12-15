alter table label_task modify column label_task_state ENUM('BACKLOG','WAITING', 'WAITING_CHANGED', 'IN_PROGRESS', 'SKIPPED', 'COMPLETED', 'REVIEWED', 'REVIEWED_SKIP') default 'BACKLOG' not null;
