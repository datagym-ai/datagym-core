import { TestBed } from '@angular/core/testing';

import { WorkspaceUtilityService } from './workspace-utility.service';

describe('WorkspaceUtilityService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: WorkspaceUtilityService = TestBed.inject(WorkspaceUtilityService);
    expect(service).toBeTruthy();
  });
});
