alter table lc_entry_value modify column label_source ENUM('AI_PRE_LABEL','API_UPLOAD','USER','API_KEY') default 'USER' not null;