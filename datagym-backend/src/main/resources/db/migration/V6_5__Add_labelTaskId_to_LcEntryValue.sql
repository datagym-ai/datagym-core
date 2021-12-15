alter table lc_entry_value add column label_task_id varchar(255);

UPDATE lc_entry_value as lv,
    (SELECT lt.id as `task_id`, lev.id as `value_id`
     FROM label_task as lt
              JOIN label_iteration li on lt.label_iteration_id = li.id
              JOIN lc_entry_value lev on li.id = lev.label_iteration_id
     where lev.image_id = lt.image_id) as tasks_with_values
SET lv.label_task_id = tasks_with_values.task_id
WHERE lv.id = tasks_with_values.value_id;

alter table lc_entry_value modify column label_task_id varchar(255) not null;