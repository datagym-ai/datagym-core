alter table lc_entry_value add constraint fk_entryvalue_labeltask foreign key (label_task_id) references label_task (id);