import { FilterBase } from './FilterBase';

export class FilterCheckbox extends FilterBase<string> {
  controlType = 'checkbox';
  type: string;

  constructor(options: any) {
    super(options);
    this.type = options['type'] || '';
  }
}
