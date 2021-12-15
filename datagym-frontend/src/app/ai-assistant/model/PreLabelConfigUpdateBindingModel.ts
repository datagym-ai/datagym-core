import {PreLabelClass} from './PreLabelClass';
import {PreLabelMapping} from './PreLabelMapping';

export class PreLabelConfigUpdateBindingModel {
  activateState: boolean;
  mappings: Record<string, PreLabelClass[]>;

  constructor(activateState:boolean, mappings: Record<string, PreLabelClass[]>) {
    this.activateState = activateState;
    this.mappings = mappings;
  }

  public static convertPreLabelMappingsToUpdateModel(activate:boolean, preLabelMappings: PreLabelMapping[]): PreLabelConfigUpdateBindingModel {
    const mappings: Record<string, PreLabelClass[]> = {};
    preLabelMappings.forEach((mapping: PreLabelMapping) => {
      const lcEntryId: string = mapping.lcEntryId;
      const preLabelClass = new PreLabelClass(mapping.preLabelClassKey, mapping.preLabelModel);

      if (!(lcEntryId in mappings) ) {
        mappings[lcEntryId] = [];
      }

      mappings[lcEntryId].push(preLabelClass);
    });

    return new PreLabelConfigUpdateBindingModel(activate, mappings);
  }
}
