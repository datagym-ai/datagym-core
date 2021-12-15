import { AppButtonInput } from '../button/button.component';

export interface DialogueModel {
  title: string,
  content: string,
  buttonLeft: AppButtonInput | string,
  buttonRight: AppButtonInput | string,
  titleParams?: {[key: string]: number},
  contentParams?: {[key: string]: string | number}
}
