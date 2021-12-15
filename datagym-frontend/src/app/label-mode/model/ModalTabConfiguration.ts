/**
 * This class is used within the InfoModalComponent to set the content
 * of the modal.
 */
export class ModalTabConfiguration {
  title?: string
  description?: string;
  icon?: string; // font awesome or dg-icons
  key?: string; // Uses KbdDirective to may apply <kdb> tags
  kbd?: string; // Enforces <kdb> tag
  action?: () => void
}
