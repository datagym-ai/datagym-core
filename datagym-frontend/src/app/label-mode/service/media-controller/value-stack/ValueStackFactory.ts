import {ValueStack} from './ValueStack';

/**
 * Let the value service always access the current value stack.
 */
export interface ValueStackFactory {

  createValueStack(): ValueStack;

}
