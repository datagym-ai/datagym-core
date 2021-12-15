import { LabErrorDetail } from './LabErrorDetail';

/**
 * Error object thrown by the backend (eforce-exception-library)
 */
export class LabError {
  key: string;
  params: string[];
  msg: string;
  details: LabErrorDetail[];
}
