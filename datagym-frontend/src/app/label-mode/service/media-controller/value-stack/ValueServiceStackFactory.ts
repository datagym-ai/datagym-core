import {ValueStackFactory} from './ValueStackFactory';
import {EntryValueService} from '../../entry-value.service';
import {ValueStack} from './ValueStack';


export class ValueServiceStackFactory implements ValueStackFactory {

  public constructor(private valueService: EntryValueService) {}


  createValueStack(): ValueStack {
    return new ValueStack(
      this.valueService.geometries,
      this.valueService.mediaClassifications
    );
  }

}
