import {AfterViewInit, Component, Input} from '@angular/core';
import {ValidationErrors} from '@angular/forms';

export class ErrorMessage {
  constructor(
    public message: string,
    public param ?: {[key: string]: string | number}
  ) {}
}

/**
 * Type of error handler to 'cast' the error description
 * into an string so the template itself must not handle
 * all possible errors
 */
export declare type ErrorHandler = (error: any) => ErrorMessage;

/**
 * Defines a map of error handlers.
 */
export declare type ErrorHandlers = {
  [key: string]: ErrorHandler
};

@Component({
  selector: 'app-dg-input-errors',
  templateUrl: './dg-input-errors.component.html',
  styleUrls: ['./dg-input-errors.component.css']
})
export class DgInputErrorsComponent implements AfterViewInit {

  @Input()
  public errorHandlers: ErrorHandlers = {};

  @Input('errors')
  public validationErrors: ValidationErrors | null = null;

  @Input()
  public show: boolean = true;

  /**
   * register this error handlers as default error handler.
   * They can be overridden by @Input('errorHandlers')
   */
  private defaultErrorHandlers: ErrorHandlers = {
    min: ((error) => new ErrorMessage('GLOBAL.ERROR_FLAGS.MIN', error)),
    max: ((error) => new ErrorMessage('GLOBAL.ERROR_FLAGS.MAX', error)),
    required: (() => new ErrorMessage('GLOBAL.ERROR_FLAGS.REQUIRED')),
    maxlength: (error => new ErrorMessage('GLOBAL.ERROR_FLAGS.MAX_LENGTH', error)),
    forbidden: (error => new ErrorMessage('GLOBAL.ERROR_FLAGS.FORBIDDEN_KEYWORD', { keyword: error.actualValue })),
    forbiddenChar: (error => new ErrorMessage('GLOBAL.ERROR_FLAGS.FORBIDDEN_CHAR', error)),
    forbiddenChars: (error => new ErrorMessage('GLOBAL.ERROR_FLAGS.FORBIDDEN_CHARS', error)),
  };

  /**
   * register this error handlers as finally error handler.
   * They can not be overridden by @Input('errorHandlers')
   */
  private requiredErrorHandlers: ErrorHandlers = {};

  /**
   * This is the error handler stack used to display useful messages to the user.
   * In ngAfterViewInit(), defaultErrorHandlers & @Input('errorHandlers') are merged.
   */
  private handlers: ErrorHandlers = {};

  get hasErrors(): boolean {
    return !!this.validationErrors;
  }

  /**
   * Return a list of error messages created via
   * the registered errorHandlers or just the error
   * identifier if no error handler was found.
   */
  get errors(): ErrorMessage[] {
    const errors = this.validationErrors;
    const keys = Object.keys(errors || {});
    const handlers = Object.keys(this.handlers);

    return keys.map(key => {
      if (handlers.indexOf(key) === -1 || !this.handlers[key]) {
        const message = typeof errors[key] === 'string' ? errors[key] : key;
        return new ErrorMessage(message);
      }
      return this.handlers[key](errors[key]);
    });
  }

  constructor() { }

  ngAfterViewInit(): void {
    // merge the possible error handlers to one stack.
    this.handlers = {
      ...this.defaultErrorHandlers,
      ...this.errorHandlers,
      ...this.requiredErrorHandlers
    };
  }
}
