
export class LimitPricingPlanViewModel {

  public id: string;
  public organisationId: string;
  public pricingPlanType: string;

  public projectLimit: number;
  public projectUsed: number;

  public labelLimit: number;
  public labelRemaining: number;

  public storageLimit: number;
  public storageUsed: number;

  public aiSegLimit: number;
  public aiSegRemaining: number;

  public apiAccess: boolean;
  public externalStorage: boolean;

  public lastReset: number;
  public timestamp: number;
}
