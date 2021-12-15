
export enum MoveAllDirections {
  TO_WAITING = 'WAITING',
  TO_BACKLOG = 'BACKLOG'
}

export class MoveAllProjectsBindingModel {

  constructor(
    public projectId:string,
    public datasetId: string,
    public direction: MoveAllDirections
  ) {}

}
