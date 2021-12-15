import { Pipe, PipeTransform } from '@angular/core';
import { DatasetDetail } from '../model/DatasetDetail';
import { DatasetList } from "../model/DatasetList";
import { BasicDataset } from "../model/BasicDataset";

@Pipe({
  name: 'datasetNameFilter',
  pure: false
})
export class DatasetNameFilterPipe implements PipeTransform {

  transform(datasets: DatasetList[], name ?: string): DatasetList[];
  transform(datasets: DatasetDetail[], name ?: string): DatasetDetail[];
  transform(datasets: BasicDataset[], name ?: string): BasicDataset[] {
    if (!datasets || !name) {
      return datasets;
    }

    const lowerName = name.toLocaleLowerCase();
    return datasets.filter((dataset: BasicDataset) =>
      dataset.name.toLocaleLowerCase().includes(lowerName)
    );
  }

}
