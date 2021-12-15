import { FilterBase } from './FilterBase';

export class FilterDropdown extends FilterBase<string> {
  controlType = 'dropdown';
  options: { key: string, value: string }[] = [];

  constructor(options: any) {
    super(options);
    this.options = options['options'] || [];

    // if no value is defined but the placeholder is set,
    // find the key to the given placeholder and set as value.
    if (this.value === undefined && !!this.placeholder) {
      const chosen = this.options.find(o => o.value === this.placeholder);
      this.value = !!chosen ? chosen.key : this.value;
    }
  }

}
