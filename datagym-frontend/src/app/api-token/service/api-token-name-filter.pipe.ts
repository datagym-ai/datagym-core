import { Pipe, PipeTransform } from '@angular/core';
import { ApiToken } from '../model/ApiToken';


@Pipe({
  pure: false,
  name: 'ApiTokenNameFilter'
})
export class ApiTokenNameFilterPipe implements PipeTransform {

  transform(tokens: ApiToken[], name ?: string): ApiToken[] {
    if (!tokens || !name) {
      return tokens;
    }

    const lowerName = name.toLocaleLowerCase();
    return tokens.filter((token: ApiToken) =>
      token.name.toLocaleLowerCase().includes(lowerName)
    );
  }

}
