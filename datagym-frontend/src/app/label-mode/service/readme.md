
# Services structure:

For each service that makes some http-requests respect the following convention to support the demo mode:

- create an extra api service to encapsulate the http client.
- let the api service implement an interface that defines the encapsulated methods.
- create an extra preview class also implementing that interface but does not make any http requests
- create a factory method within the api service that returns within the demo url the preview class else the api service.

## Example:

Take a closer look at the `label-task.service.ts` within this directory:

Here are the files of interest:

| File | description | 
|---|---|
|`label-task.service.ts`| Manages the task
|`label-task-api.service.ts`| Implements the interface and contains the factory method
|`label-task-api-interface.ts`| Defines the methods for the api service 
|`label-task-api-preview.ts`| Implements the interface without http requests

This main service class `LabelTaskService` is responsible to load the task, set the task state and do some other stuff by calling the backend. But it doesn't depend on the `HttpClient`. Instead it requires the `LabelTaskApiService` located within the `label-task-api.service.ts` file.

In this file is also the factory method located:

```
/**
 * Factory to decide, which implementation of 'LabelTaskApiInterface' should be used.
 *
 * This method stays here to not raise some circular dependencies.
 *
 * @param router
 * @param http
 */
export function labelTaskApiServiceFactory(router: Router, http: HttpClient): LabelTaskApiInterface {

  const url = router.routerState.snapshot.url;
  if (PreviewModeUri.equals(url)) {
    return new LabelTaskApiPreview();
  }
  return new LabelTaskApiService(http);
}
```

Based on the currently used url this factory decide which implementation of the `LabelTaskApiInterface` should be returned. Within the preview mode all http requests should be suppressed. Therefore the `LabelTaskApiPreview` is returned. In every other case the default `LabelTaskApiService` is used.

To work properly the factory method must be registered:

````
/**
 * The default LabelTaskApiService implementation using the HttpClient
 * to communicate with the backend.
 */
@Injectable({
  providedIn: PreviewModeUri.PROVIDED_IN,
  useFactory: labelTaskApiServiceFactory,
  deps: [Router, HttpClient],
})
export class LabelTaskApiService implements LabelTaskApiInterface{
````

## Some additional Notes:

- The order within the deps property matters. This list must match the expected order of the factory method.
- The preview instance is not a service class. The `@Injectable` decorator is missing.
- The interface is used to force the preview class implement the same interface. The compiler would warn you if one is missing.
- Have fun with the preview mode :)
