-- Replace radio entries to select
UPDATE lc_entry
SET lc_entry.entry_type = 'select',
    lc_entry.type       = 'SELECT'
WHERE lc_entry.entry_type = 'radio';
-- Clone old radio key to select key
UPDATE lc_entry_value
SET lc_entry_value.select_key = lc_entry_value.radio_key
WHERE lc_entry_value.entry_values_type = 'radio_value';
-- Empty the old radio key value
UPDATE lc_entry_value
SET lc_entry_value.radio_key = null
WHERE lc_entry_value.entry_values_type = 'radio_value';
-- Change all radio values to select values
UPDATE lc_entry_value
SET lc_entry_value.entry_values_type = 'select_value'
WHERE lc_entry_value.entry_values_type = 'radio_value';

INSERT INTO lc_entry_select_options(lc_entry_select_options.lc_entry_select_id,
                                    lc_entry_select_options.lc_entry_select_value,
                                    lc_entry_select_options.lc_entry_select_key)
SELECT lc_entry_radio_id, lc_entry_radio_value, lc_entry_radio_key
FROM lc_entry_radio_options;

-- Delete all data from radio options
DELETE
FROM lc_entry_radio_options;