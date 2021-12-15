import { FilterBase } from './FilterBase';

export class FilterTextbox extends FilterBase<string> {
  controlType = 'textbox';
  type: string;
  autocomplete: boolean;

  constructor(options: any) {
    super(options);
    this.type = options['type'] || '';
    this.autocomplete = !!options['autocomplete'] || false;
  }
}
